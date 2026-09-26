/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.fixturemonkey.assembly;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.customizer.ScopeChain;
import com.navercorp.fixturemonkey.customizer.ScopeSelector;
import com.navercorp.fixturemonkey.planner.AnalysisResult.PostConditionFilter;
import com.navercorp.fixturemonkey.planner.AnalysisResult.PropertyCustomizer;
import com.navercorp.fixturemonkey.planner.AnalyzedScope;
import com.navercorp.fixturemonkey.planner.ManipulatorAnalyzer;
import com.navercorp.objectfarm.api.expression.IndexSelector;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.expression.Segment;
import com.navercorp.objectfarm.api.expression.Selector;

/**
 * Looks up the directives of each {@link AnalyzedScope} along the chain of a node during one assembly. A node gets a
 * scope's directive when a node on its chain is selected by the scope and the path from that node down to it matches
 * the directive's path.
 * <p>
 * The root scope's node is always the node a sample starts from, so its paths are looked up directly; its values and
 * sizes are not here, since they are expanded while planning. Members named after defined scopes see only the
 * defined scopes; the others see both.
 */
final class ScopeLookup {
	private static final ScopeLookup EMPTY = new ScopeLookup(RootEntry.EMPTY, Collections.emptyList());

	private final RootEntry root;
	private final List<DefinedScopeEntry> definedScopes;
	private final List<Map.Entry<ScopedPath, ValueCandidate>> definedScopeValues = new ArrayList<>();
	private final List<Map.Entry<ScopedPath, ValueCandidate>> definedScopeRootValues = new ArrayList<>();
	private final boolean hasDefinedScopeNotNull;
	private final boolean hasFilters;
	private final boolean hasCustomizers;

	private ScopeLookup(RootEntry root, List<DefinedScopeEntry> definedScopes) {
		this.root = root;
		this.definedScopes = definedScopes;
		boolean notNull = false;
		boolean filters = !root.analyzed.getFiltersByPath().isEmpty();
		boolean customizers = !root.analyzed.getCustomizersByPath().isEmpty();
		for (DefinedScopeEntry entry : definedScopes) {
			entry.collectValues(definedScopeValues, definedScopeRootValues);
			notNull |= !entry.analyzed.getNotNullPaths().isEmpty();
			filters |= !entry.analyzed.getFiltersByPath().isEmpty();
			customizers |= !entry.analyzed.getCustomizersByPath().isEmpty();
		}
		this.hasDefinedScopeNotNull = notNull;
		this.hasFilters = filters;
		this.hasCustomizers = customizers;
	}

	/**
	 * Lays out the directives of each scope. Directives are numbered in declaration order across the defined scopes,
	 * the one with the lowest precedence first, and within a defined scope its whole-object value comes before its
	 * field values, so its own field value outranks its whole-object one.
	 *
	 * @param rootScope             what the root scope declared, by path from the node a sample starts from
	 * @param analyzedDefinedScopes the defined scopes, the one with the lowest precedence first
	 * @return the lookup
	 */
	static ScopeLookup from(AnalyzedScope rootScope, List<AnalyzedScope> analyzedDefinedScopes) {
		RootEntry root = new RootEntry(rootScope);
		if (analyzedDefinedScopes.isEmpty()) {
			return root.isEmpty() ? EMPTY : new ScopeLookup(root, Collections.emptyList());
		}
		List<DefinedScopeEntry> entries = new ArrayList<>(analyzedDefinedScopes.size());
		int sequenceBase = 0;
		for (AnalyzedScope analyzed : analyzedDefinedScopes) {
			entries.add(new DefinedScopeEntry(analyzed, sequenceBase));
			sequenceBase += analyzed.getValuesByPath().size() + analyzed.getNotNullPaths().size();
		}
		return new ScopeLookup(root, entries);
	}

	/**
	 * The value of each path the defined scopes declared.
	 */
	List<Map.Entry<ScopedPath, ValueCandidate>> getDefinedScopeValues() {
		return definedScopeValues;
	}

	/**
	 * The whole-object value each defined scope declared.
	 */
	List<Map.Entry<ScopedPath, ValueCandidate>> getDefinedScopeRootValues() {
		return definedScopeRootValues;
	}

	/**
	 * The paths the root scope declared a customizer, a filter or a not-null at.
	 */
	Set<PathExpression> getRootDeclaredPaths() {
		Set<PathExpression> paths = new HashSet<>(root.analyzed.getCustomizersByPath().keySet());
		paths.addAll(root.analyzed.getFiltersByPath().keySet());
		paths.addAll(root.analyzed.getNotNullPaths());
		return paths;
	}

	/**
	 * Returns whether the root scope declared {@code path} not null.
	 */
	boolean isRootNotNull(PathExpression path) {
		return RootEntry.reaches(root.analyzed.getNotNullPaths(), root.notNullHasPatterns, path);
	}

	/**
	 * Returns whether a defined scope that declared a value selects the node at the end of {@code chain}.
	 */
	boolean isSelectedByDefinedScopeWithValues(ScopeChain chain) {
		for (DefinedScopeEntry entry : definedScopes) {
			if (!entry.analyzed.getValuesByPath().isEmpty() && chain.selects(entry.selector(), chain.depth())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the scope depth of the outermost defined scope's not-null path that reaches the node at the end of
	 * {@code chain}.
	 *
	 * @return the scope depth, or {@link Integer#MAX_VALUE} when none reaches it
	 */
	int definedScopeNotNullDepth(ScopeChain chain) {
		int outermost = Integer.MAX_VALUE;
		if (!hasDefinedScopeNotNull) {
			return outermost;
		}
		for (DefinedScopeEntry entry : definedScopes) {
			for (PathExpression notNullPath : entry.analyzed.getNotNullPaths()) {
				int depth = chain.scopeDepthOf(entry.selector(), notNullPath);
				if (depth >= 0 && depth < outermost) {
					outermost = depth;
				}
			}
		}
		return outermost;
	}

	/**
	 * Returns whether a defined scope's value at {@code scopeDepth} yields to a defined scope's not-null path: one
	 * from an outer scope node, or one of higher precedence from the same scope node.
	 */
	boolean yieldsToDefinedScopeNotNull(
		ValueCandidate candidate,
		int scopeDepth,
		int notNullDepth,
		ScopeChain chain
	) {
		if (notNullDepth != scopeDepth) {
			return notNullDepth < scopeDepth;
		}
		for (DefinedScopeEntry entry : definedScopes) {
			int sequence = entry.notNullSequenceBase();
			for (PathExpression notNullPath : entry.analyzed.getNotNullPaths()) {
				DirectivePrecedence notNull = entry.precedence(sequence++);
				if (notNull.compareTo(candidate.precedence) > 0
					&& chain.scopeDepthOf(entry.selector(), notNullPath) == scopeDepth) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns whether a defined scope's value or not-null path reaches below the node at the end of {@code chain}
	 * from a scope node at most {@code scopeDepthLimit} deep.
	 */
	boolean hasDefinedScopeDirectiveBelow(ScopeChain chain, int scopeDepthLimit) {
		for (Map.Entry<ScopedPath, ValueCandidate> value : definedScopeValues) {
			if (value.getKey().segmentBelow(chain, scopeDepthLimit) != null) {
				return true;
			}
		}
		if (!hasDefinedScopeNotNull) {
			return false;
		}
		for (DefinedScopeEntry entry : definedScopes) {
			for (PathExpression notNullPath : entry.analyzed.getNotNullPaths()) {
				if (chain.segmentBelow(entry.selector(), notNullPath, scopeDepthLimit) != null) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns how many elements a container needs so that every element a defined scope's value or not-null path
	 * names by index exists.
	 */
	int definedScopeRequiredElementCount(ScopeChain chain) {
		int required = 0;
		for (Map.Entry<ScopedPath, ValueCandidate> value : definedScopeValues) {
			required = Math.max(required, indexOf(value.getKey().segmentBelow(chain, Integer.MAX_VALUE)) + 1);
		}
		if (!hasDefinedScopeNotNull) {
			return required;
		}
		for (DefinedScopeEntry entry : definedScopes) {
			for (PathExpression notNullPath : entry.analyzed.getNotNullPaths()) {
				Segment segment = chain.segmentBelow(entry.selector(), notNullPath, Integer.MAX_VALUE);
				required = Math.max(required, indexOf(segment) + 1);
			}
		}
		return required;
	}

	private static int indexOf(@Nullable Segment segment) {
		if (segment == null || !segment.isSingleSelector()) {
			return -1;
		}
		Selector selector = segment.getFirstSelector();
		return selector instanceof IndexSelector ? ((IndexSelector)selector).getIndex() : -1;
	}

	boolean hasFilters() {
		return hasFilters;
	}

	List<PostConditionFilter> filtersAt(ScopeChain chain, PathExpression path) {
		List<PostConditionFilter> filters = new ArrayList<>();
		for (DefinedScopeEntry entry : definedScopes) {
			for (Map.Entry<PathExpression, List<PostConditionFilter>> declared
				: entry.analyzed.getFiltersByPath().entrySet()) {
				if (chain.scopeDepthOf(entry.selector(), declared.getKey()) >= 0) {
					filters.addAll(declared.getValue());
				}
			}
		}
		for (List<PostConditionFilter> declared
			: root.at(root.analyzed.getFiltersByPath(), root.filtersHavePatterns, path)) {
			filters.addAll(declared);
		}
		return filters;
	}

	boolean hasCustomizers() {
		return hasCustomizers;
	}

	/**
	 * Returns whether a customizer reaches the node at the end of {@code chain}, whether or not it applies there.
	 */
	boolean hasCustomizerAt(ScopeChain chain, PathExpression path) {
		if (!hasCustomizers) {
			return false;
		}
		if (!root.at(root.analyzed.getCustomizersByPath(), root.customizersHavePatterns, path).isEmpty()) {
			return true;
		}
		for (DefinedScopeEntry entry : definedScopes) {
			for (PathExpression customizerPath : entry.analyzed.getCustomizersByPath().keySet()) {
				if (chain.scopeDepthOf(entry.selector(), customizerPath) >= 0) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the customizers that apply to the node at the end of {@code chain}, defined scopes' first: a customizer
	 * applies unless a value declared after it in its scope replaced the node, or the node's value comes from a scope
	 * that outranks the customizer's.
	 *
	 * @param value           the value decided for the node, or null when the node is generated
	 * @param valueScopeDepth the depth of the scope node the value came from, 0 for the root scope
	 */
	List<PropertyCustomizer> customizersAppliedAt(
		ScopeChain chain,
		PathExpression path,
		@Nullable ValueCandidate value,
		int valueScopeDepth
	) {
		List<PropertyCustomizer> applied = new ArrayList<>();
		for (DefinedScopeEntry entry : definedScopes) {
			for (Map.Entry<PathExpression, List<PropertyCustomizer>> declared
				: entry.analyzed.getCustomizersByPath().entrySet()) {
				int scopeDepth = chain.scopeDepthOf(entry.selector(), declared.getKey());
				if (scopeDepth >= 0) {
					addApplied(applied, declared.getValue(), chain, scopeDepth, entry.analyzed, value, valueScopeDepth);
				}
			}
		}
		for (List<PropertyCustomizer> declared
			: root.at(root.analyzed.getCustomizersByPath(), root.customizersHavePatterns, path)) {
			addApplied(applied, declared, chain, 0, root.analyzed, value, valueScopeDepth);
		}
		return applied;
	}

	private static void addApplied(
		List<PropertyCustomizer> applied,
		List<PropertyCustomizer> declared,
		ScopeChain chain,
		int scopeDepth,
		AnalyzedScope scope,
		@Nullable ValueCandidate value,
		int valueScopeDepth
	) {
		boolean outranked = value != null
			&& value.precedence.scopeOutranks(valueScopeDepth, scope.getPriority(), scopeDepth);
		if (outranked) {
			return;
		}
		for (PropertyCustomizer customizer : declared) {
			if (!customizer.isOverriddenAt(chain, scopeDepth)) {
				applied.add(customizer);
			}
		}
	}

	/**
	 * Returns whether a defined scope's filter reaches below the node at the end of {@code chain} from a scope node
	 * at most {@code scopeDepthLimit} deep. A filter checks every value it reaches, wherever it was declared; the root
	 * scope's filters are found through {@link #getRootDeclaredPaths()}.
	 */
	boolean hasDefinedScopeFilterBelow(ScopeChain chain, int scopeDepthLimit) {
		if (!hasFilters) {
			return false;
		}
		for (DefinedScopeEntry entry : definedScopes) {
			for (PathExpression filterPath : entry.analyzed.getFiltersByPath().keySet()) {
				if (chain.scopeDepthBelow(entry.selector(), filterPath, scopeDepthLimit) >= 0) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns whether a defined scope's customizer reaches below the node at the end of {@code chain} from a scope
	 * node at most {@code scopeDepthLimit} deep, with no value declared after it replacing the node. The root scope's
	 * customizers are found through {@link #getRootDeclaredPaths()}.
	 */
	boolean hasDefinedScopeCustomizerBelow(ScopeChain chain, int scopeDepthLimit) {
		if (!hasCustomizers) {
			return false;
		}
		for (DefinedScopeEntry entry : definedScopes) {
			for (Map.Entry<PathExpression, List<PropertyCustomizer>> declared
				: entry.analyzed.getCustomizersByPath().entrySet()) {
				int scopeDepth = chain.scopeDepthBelow(entry.selector(), declared.getKey(), scopeDepthLimit);
				if (scopeDepth < 0) {
					continue;
				}
				for (PropertyCustomizer customizer : declared.getValue()) {
					if (!customizer.isOverriddenAt(chain, scopeDepth)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// The root scope, whose node is always the node a sample starts from: an exact path is found by hash, a pattern
	// ([*], .*, a union) is matched against the path
	private static final class RootEntry {
		private static final RootEntry EMPTY = new RootEntry(ManipulatorAnalyzer.emptyResult().getAnalyzedRootScope());

		private final AnalyzedScope analyzed;
		private final boolean notNullHasPatterns;
		private final boolean filtersHavePatterns;
		private final boolean customizersHavePatterns;

		private RootEntry(AnalyzedScope analyzed) {
			this.analyzed = analyzed;
			this.notNullHasPatterns = hasPatterns(analyzed.getNotNullPaths());
			this.filtersHavePatterns = hasPatterns(analyzed.getFiltersByPath().keySet());
			this.customizersHavePatterns = hasPatterns(analyzed.getCustomizersByPath().keySet());
		}

		private boolean isEmpty() {
			return analyzed.getNotNullPaths().isEmpty()
				&& analyzed.getFiltersByPath().isEmpty()
				&& analyzed.getCustomizersByPath().isEmpty()
				&& analyzed.getLimitsByPath().isEmpty();
		}

		private <V> List<V> at(Map<PathExpression, V> byPath, boolean hasPatterns, PathExpression path) {
			if (!hasPatterns) {
				V value = byPath.get(path);
				return value != null ? Collections.singletonList(value) : Collections.emptyList();
			}
			List<V> matched = new ArrayList<>();
			for (Map.Entry<PathExpression, V> declared : byPath.entrySet()) {
				if (reachesFrom(declared.getKey(), path)) {
					matched.add(declared.getValue());
				}
			}
			return matched;
		}

		private static boolean reaches(Set<PathExpression> paths, boolean hasPatterns, PathExpression path) {
			if (!hasPatterns) {
				return paths.contains(path);
			}
			for (PathExpression declared : paths) {
				if (reachesFrom(declared, path)) {
					return true;
				}
			}
			return false;
		}

		private static boolean reachesFrom(PathExpression declared, PathExpression path) {
			return declared.equals(path) || (declared.hasWildcard() && declared.matches(path));
		}

		private static boolean hasPatterns(Collection<PathExpression> paths) {
			for (PathExpression path : paths) {
				if (path.hasWildcard()) {
					return true;
				}
			}
			return false;
		}
	}

	// A defined scope: its directives are matched against each scope node on the chain. Its values and not-null paths
	// are numbered from sequenceBase, the whole-object value first, then the field values, then the not-null paths
	private static final class DefinedScopeEntry {
		private final AnalyzedScope analyzed;
		private final int sequenceBase;

		private DefinedScopeEntry(AnalyzedScope analyzed, int sequenceBase) {
			this.analyzed = analyzed;
			this.sequenceBase = sequenceBase;
		}

		private ScopeSelector selector() {
			return analyzed.getSelector();
		}

		private DirectivePrecedence precedence(int sequence) {
			return DirectivePrecedence.definedScope(analyzed.getPriority(), sequence);
		}

		private int notNullSequenceBase() {
			return sequenceBase + analyzed.getValuesByPath().size();
		}

		private void collectValues(
			List<Map.Entry<ScopedPath, ValueCandidate>> values,
			List<Map.Entry<ScopedPath, ValueCandidate>> rootValues
		) {
			Map<PathExpression, @Nullable Object> declared = analyzed.getValuesByPath();
			int sequence = sequenceBase;
			if (declared.containsKey(PathExpression.root())) {
				Map.Entry<ScopedPath, ValueCandidate> rootValue = candidate(
					PathExpression.root(),
					declared.get(PathExpression.root()),
					sequence++
				);
				values.add(rootValue);
				rootValues.add(rootValue);
			}
			for (Map.Entry<PathExpression, @Nullable Object> value : declared.entrySet()) {
				if (!value.getKey().isRoot()) {
					values.add(candidate(value.getKey(), value.getValue(), sequence++));
				}
			}
		}

		private Map.Entry<ScopedPath, ValueCandidate> candidate(
			PathExpression relativePath,
			@Nullable Object value,
			int sequence
		) {
			return new AbstractMap.SimpleImmutableEntry<>(
				new ScopedPath(selector(), relativePath),
				new ValueCandidate(value, precedence(sequence))
			);
		}
	}
}
