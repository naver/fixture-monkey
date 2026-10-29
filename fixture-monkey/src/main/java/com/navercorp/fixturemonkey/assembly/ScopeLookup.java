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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.customizer.DirectivePrecedence;
import com.navercorp.fixturemonkey.customizer.Scope;
import com.navercorp.fixturemonkey.customizer.ScopeChain;
import com.navercorp.fixturemonkey.customizer.ScopeSelector;
import com.navercorp.fixturemonkey.planner.AnalysisResult;
import com.navercorp.fixturemonkey.planner.AnalysisResult.PostConditionFilter;
import com.navercorp.fixturemonkey.planner.AnalysisResult.PropertyCustomizer;
import com.navercorp.fixturemonkey.planner.AnalyzedScope;
import com.navercorp.objectfarm.api.expression.PathExpression;

/**
 * Looks up the directives of each {@link AnalyzedScope} along the chain of a node during one assembly. A node gets a
 * scope's directive when a node on its chain is selected by the scope and the path from that node down to it matches
 * the directive's path.
 * <p>
 * The root scope's node is always the node a sample starts from, so its paths are looked up directly, along with
 * what planning expanded it into: its just values, the containers it sized and the order of its values. Its values
 * themselves are candidates in {@link CandidateLookup}. Members named after defined scopes see only the defined
 * scopes; the others see both.
 * <p>
 * It also keeps the nodes assembly finds must not be null, so whether a node must not be null is answered here
 * alone.
 */
final class ScopeLookup {
	private static final int ANY_SCOPE_DEPTH = Integer.MAX_VALUE;

	private final RootEntry root;
	private final List<DefinedScopeEntry> definedScopes;
	private final List<Map.Entry<ScopedPath, ValueCandidate>> definedScopeValues = new ArrayList<>();
	private final Set<ScopedPath> definedScopeJustPaths = new HashSet<>();
	private final boolean hasDefinedScopeNotNull;
	private final boolean hasFilters;
	private final boolean hasCustomizers;
	private final Set<PathExpression> notNullRequiredPaths = new HashSet<>();
	private final Map<Integer, List<ScopeSelector>> higherPriorityDefinedScopesDeclaringInsideByPriority =
		new HashMap<>();

	private ScopeLookup(RootEntry root, List<DefinedScopeEntry> definedScopes) {
		this.root = root;
		this.definedScopes = definedScopes;
		boolean notNull = false;
		boolean filters = !root.analyzed.getFiltersByPath().isEmpty();
		boolean customizers = !root.analyzed.getCustomizersByPath().isEmpty();
		for (DefinedScopeEntry entry : definedScopes) {
			entry.collectValues(definedScopeValues, definedScopeJustPaths);
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
	 * the one with the lowest precedence first. Within a defined scope, what it declared for its whole scope, such as
	 * its customizers, comes first, then its values, not-null paths and container sizes in the order it declared
	 * them.
	 *
	 * @param analysisResult        what the root scope declared and what planning expanded it into, by path from the
	 *                              node a sample starts from
	 * @param analyzedDefinedScopes the defined scopes, the one with the lowest precedence first
	 * @return the lookup
	 */
	static ScopeLookup from(AnalysisResult analysisResult, List<AnalyzedScope> analyzedDefinedScopes) {
		RootEntry root = new RootEntry(analysisResult);
		if (analyzedDefinedScopes.isEmpty()) {
			return new ScopeLookup(root, Collections.emptyList());
		}
		List<DefinedScopeEntry> entries = new ArrayList<>(analyzedDefinedScopes.size());
		int sequenceBase = 0;
		for (AnalyzedScope analyzed : analyzedDefinedScopes) {
			entries.add(new DefinedScopeEntry(analyzed, sequenceBase));
			sequenceBase += 2 + analyzed.getLatestSequence();
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
	 * Returns whether a defined scope set a {@code Values.just} value at {@code path}, used as it is.
	 */
	boolean isDefinedScopeJust(ScopedPath path) {
		return definedScopeJustPaths.contains(path);
	}

	/**
	 * The paths the root scope declared a customizer, a filter or a not-null at.
	 */
	Set<PathExpression> getRootCustomizerFilterNotNullPaths() {
		Set<PathExpression> paths = new HashSet<>(root.analyzed.getCustomizersByPath().keySet());
		paths.addAll(root.analyzed.getFiltersByPath().keySet());
		paths.addAll(root.analyzed.getNotNullPaths());
		return paths;
	}

	/**
	 * The paths the root scope declared a container size at.
	 */
	Set<PathExpression> getRootContainerSizePaths() {
		return root.containerSizePaths;
	}

	/**
	 * Returns whether the root scope declared a size for the container at {@code path}, at that path or through a
	 * wildcard reaching it.
	 */
	boolean isRootSizedContainer(PathExpression path) {
		if (root.containerSizePaths.contains(path)) {
			return true;
		}
		for (PathExpression sizePath : root.containerSizePaths) {
			if (sizePath.hasWildcard() && sizePath.matches(path)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether the root scope set a {@code Values.just} value at {@code path}.
	 */
	boolean isRootJust(PathExpression path) {
		return root.justPaths.contains(path);
	}

	/**
	 * Returns whether {@code path} lies inside a {@code Values.just} value the root scope set. Such a value is used as
	 * it is, so values declared inside it are ignored: the only exception to the more specific path winning.
	 */
	boolean isUnderRootJust(PathExpression path) {
		return PathMatcher.isUnderExcludedPath(path, root.justPaths);
	}

	/**
	 * Returns the declaration order of the value the root scope set at {@code path}, or 0 when it set none there.
	 */
	int rootValueOrderAt(PathExpression path) {
		return root.valueOrderByPath.getOrDefault(path, 0);
	}

	/**
	 * Records that the node at {@code path} must not be null: it has no value, but a defined scope above it declared
	 * it not null or declared something below it, which applies only if the node exists.
	 */
	void markNotNullRequired(PathExpression path) {
		notNullRequiredPaths.add(path);
	}

	/**
	 * Returns whether the node at {@code path} must not be null: the root scope declared it not null, or assembly
	 * recorded it with {@link #markNotNullRequired}.
	 */
	boolean isNotNullRequired(PathExpression path) {
		return notNullRequiredPaths.contains(path) || isRootNotNull(path);
	}

	/**
	 * Returns whether the root scope declared {@code path} not null.
	 */
	private boolean isRootNotNull(PathExpression path) {
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
	 * Returns whether a defined scope's not-null path reaches the node at the end of {@code chain}.
	 */
	boolean hasDefinedScopeNotNullAt(ScopeChain chain) {
		if (!hasDefinedScopeNotNull) {
			return false;
		}
		for (DefinedScopeEntry entry : definedScopes) {
			for (PathExpression notNullPath : entry.analyzed.getNotNullPaths()) {
				if (chain.scopeDepthOf(entry.selector(), notNullPath) >= 0) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns whether a not-null reaching the node at the end of {@code chain} outranks a directive of
	 * {@code precedence} whose scope node sits {@code scopeDepth} deep: one the root scope or a defined scope declared,
	 * compared by {@link DirectivePrecedence}, or one assembly recorded with {@link #markNotNullRequired}, which
	 * outranks any defined scope's directive.
	 */
	boolean hasOutrankingNotNullAt(
		ScopeChain chain,
		PathExpression path,
		DirectivePrecedence precedence,
		int scopeDepth
	) {
		if (!precedence.isRootScope() && notNullRequiredPaths.contains(path)) {
			return true;
		}
		for (PathExpression notNullPath : root.analyzed.getNotNullPaths()) {
			DirectivePrecedence notNull = DirectivePrecedence.rootScope(root.analyzed.getNotNullSequence(notNullPath));
			if (RootEntry.reachesFrom(notNullPath, path)
				&& notNull.outranks(Scope.ROOT_DEPTH, precedence, scopeDepth)) {
				return true;
			}
		}
		for (DefinedScopeEntry entry : definedScopes) {
			for (PathExpression notNullPath : entry.analyzed.getNotNullPaths()) {
				int notNullDepth = chain.scopeDepthOf(entry.selector(), notNullPath);
				if (notNullDepth >= 0
					&& entry.notNullPrecedence(notNullPath).outranks(notNullDepth, precedence, scopeDepth)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns whether a defined scope's value or not-null path reaches below the node at the end of {@code chain}, so
	 * the node must exist for it to apply.
	 */
	boolean hasDefinedScopeDirectiveBelow(ScopeChain chain) {
		for (Map.Entry<ScopedPath, ValueCandidate> value : definedScopeValues) {
			if (value.getKey().segmentBelow(chain, ANY_SCOPE_DEPTH) != null) {
				return true;
			}
		}
		if (!hasDefinedScopeNotNull) {
			return false;
		}
		for (DefinedScopeEntry entry : definedScopes) {
			for (PathExpression notNullPath : entry.analyzed.getNotNullPaths()) {
				if (chain.segmentBelow(entry.selector(), notNullPath, ANY_SCOPE_DEPTH) != null) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns whether a defined scope's value or not-null path reaching below the node at the end of {@code chain}
	 * outranks a directive of {@code precedence} whose scope node sits {@code scopeDepth} deep.
	 */
	boolean hasOutrankingValueBelow(ScopeChain chain, DirectivePrecedence precedence, int scopeDepth) {
		for (Map.Entry<ScopedPath, ValueCandidate> value : definedScopeValues) {
			int valueDepth = value.getKey().scopeDepthBelow(chain, ANY_SCOPE_DEPTH);
			if (valueDepth >= 0 && value.getValue().precedence.outranks(valueDepth, precedence, scopeDepth)) {
				return true;
			}
		}
		if (!hasDefinedScopeNotNull) {
			return false;
		}
		for (DefinedScopeEntry entry : definedScopes) {
			for (PathExpression notNullPath : entry.analyzed.getNotNullPaths()) {
				DirectivePrecedence notNull = entry.notNullPrecedence(notNullPath);
				int notNullDepth = chain.scopeDepthBelow(entry.selector(), notNullPath, ANY_SCOPE_DEPTH);
				if (notNullDepth >= 0 && notNull.outranks(notNullDepth, precedence, scopeDepth)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns whether a defined scope's directive reaching below the node at the end of {@code chain} outranks a value
	 * of {@code precedence} whose scope node sits {@code scopeDepth} deep, so the value is taken apart for the
	 * directive to apply: a value or not-null path, a container size, or a customizer no later value of its scope
	 * replaced.
	 */
	boolean hasOutrankingDirectiveBelow(ScopeChain chain, DirectivePrecedence precedence, int scopeDepth) {
		if (hasOutrankingValueBelow(chain, precedence, scopeDepth)) {
			return true;
		}
		for (DefinedScopeEntry entry : definedScopes) {
			DirectivePrecedence scopePrecedence = entry.scopePrecedence();
			for (PathExpression sizePath : entry.analyzed.getContainerSizesByPath().keySet()) {
				int sizeDepth = chain.scopeDepthBelow(entry.selector(), sizePath, ANY_SCOPE_DEPTH);
				if (sizeDepth >= 0 && entry.sizePrecedence(sizePath).outranks(sizeDepth, precedence, scopeDepth)) {
					return true;
				}
			}
			if (!hasCustomizers) {
				continue;
			}
			for (Map.Entry<PathExpression, List<PropertyCustomizer>> declared
				: entry.analyzed.getCustomizersByPath().entrySet()) {
				int customizerDepth = chain.scopeDepthBelow(entry.selector(), declared.getKey(), ANY_SCOPE_DEPTH);
				if (customizerDepth < 0
					|| !entry.declared(precedence)
					&& !scopePrecedence.outranks(customizerDepth, precedence, scopeDepth)) {
					continue;
				}
				for (PropertyCustomizer customizer : declared.getValue()) {
					if (!customizer.isOverriddenAt(chain, customizerDepth)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Returns whether a defined scope's size of the container at the end of {@code chain} outranks a value of
	 * {@code precedence} whose scope node sits {@code scopeDepth} deep, so the container keeps that size over the
	 * element count of the value.
	 */
	boolean hasOutrankingSizeAt(ScopeChain chain, DirectivePrecedence precedence, int scopeDepth) {
		for (DefinedScopeEntry entry : definedScopes) {
			for (PathExpression sizePath : entry.analyzed.getContainerSizesByPath().keySet()) {
				int sizeDepth = chain.scopeDepthOf(entry.selector(), sizePath);
				if (sizeDepth >= 0 && entry.sizePrecedence(sizePath).outranks(sizeDepth, precedence, scopeDepth)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the selectors of the defined scopes of a lower priority number than {@code priority} that declared a
	 * value, a not-null path, a filter, a customizer or a container size. Such a scope may select a node below a node
	 * whose value comes from a scope of {@code priority}, so such a value is taken apart for what the scope declared
	 * inside it to win.
	 */
	List<ScopeSelector> higherPriorityDefinedScopesDeclaringInside(int priority) {
		return higherPriorityDefinedScopesDeclaringInsideByPriority.computeIfAbsent(priority, valuePriority -> {
			List<ScopeSelector> scopes = new ArrayList<>();
			for (DefinedScopeEntry entry : definedScopes) {
				AnalyzedScope analyzed = entry.analyzed;
				boolean declaresInside =
					declaresValueDirectives(analyzed) || !analyzed.getContainerSizesByPath().isEmpty();
				if (analyzed.getPriority() < valuePriority && declaresInside) {
					scopes.add(entry.selector());
				}
			}
			return scopes.isEmpty() ? Collections.emptyList() : scopes;
		});
	}

	private static boolean declaresValueDirectives(AnalyzedScope analyzed) {
		return !analyzed.getValuesByPath().isEmpty()
			|| !analyzed.getNotNullPaths().isEmpty()
			|| !analyzed.getFiltersByPath().isEmpty()
			|| !analyzed.getCustomizersByPath().isEmpty();
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
	 * applies unless a value declared after it in its scope replaced the node, or the node's value comes from another
	 * scope and outranks the customizer.
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
				if (scopeDepth >= 0 && !outranksScopeDirective(value, valueScopeDepth, entry, scopeDepth)) {
					addNotOverridden(applied, declared.getValue(), chain, scopeDepth);
				}
			}
		}
		for (List<PropertyCustomizer> declared
			: root.at(root.analyzed.getCustomizersByPath(), root.customizersHavePatterns, path)) {
			addNotOverridden(applied, declared, chain, Scope.ROOT_DEPTH);
		}
		return applied;
	}

	/**
	 * Returns whether {@code value}, decided for a node, outranks what {@code entry} declared for its whole scope at a
	 * scope node {@code scopeDepth} deep, such as a customizer, so that directive does not apply to the value. A value
	 * of the same scope never does: the order inside the scope decides there.
	 */
	private static boolean outranksScopeDirective(
		@Nullable ValueCandidate value,
		int valueScopeDepth,
		DefinedScopeEntry entry,
		int scopeDepth
	) {
		return value != null
			&& !entry.declared(value.precedence)
			&& value.precedence.outranks(valueScopeDepth, entry.scopePrecedence(), scopeDepth);
	}

	private static void addNotOverridden(
		List<PropertyCustomizer> applied,
		List<PropertyCustomizer> declared,
		ScopeChain chain,
		int scopeDepth
	) {
		for (PropertyCustomizer customizer : declared) {
			if (!customizer.isOverriddenAt(chain, scopeDepth)) {
				applied.add(customizer);
			}
		}
	}

	/**
	 * Returns whether a defined scope's filter reaches below the node at the end of {@code chain} from a scope node
	 * at most {@code scopeDepthLimit} deep, or from any scope node for a scope of a lower priority number than
	 * {@code priority}. A filter checks every value it reaches, wherever it was declared; the root scope's filters are
	 * found through {@link #getRootCustomizerFilterNotNullPaths()}.
	 */
	boolean hasDefinedScopeFilterBelow(ScopeChain chain, int scopeDepthLimit, int priority) {
		if (!hasFilters) {
			return false;
		}
		for (DefinedScopeEntry entry : definedScopes) {
			int limit = entry.analyzed.getPriority() < priority ? ANY_SCOPE_DEPTH : scopeDepthLimit;
			for (PathExpression filterPath : entry.analyzed.getFiltersByPath().keySet()) {
				if (chain.scopeDepthBelow(entry.selector(), filterPath, limit) >= 0) {
					return true;
				}
			}
		}
		return false;
	}

	private static final class RootEntry {
		private final AnalyzedScope analyzed;
		private final Set<PathExpression> justPaths;
		private final Set<PathExpression> containerSizePaths;
		private final Map<PathExpression, Integer> valueOrderByPath;
		private final boolean notNullHasPatterns;
		private final boolean filtersHavePatterns;
		private final boolean customizersHavePatterns;

		private RootEntry(AnalysisResult analysisResult) {
			this.analyzed = analysisResult.getAnalyzedRootScope();
			this.justPaths = Collections.unmodifiableSet(new HashSet<>(analysisResult.getJustPaths()));
			this.containerSizePaths =
				Collections.unmodifiableSet(analysisResult.getLatestSizeDirectiveByPath().keySet());
			this.valueOrderByPath = analysisResult.getValueOrderByPath();
			this.notNullHasPatterns = hasPatterns(analyzed.getNotNullPaths());
			this.filtersHavePatterns = hasPatterns(analyzed.getFiltersByPath().keySet());
			this.customizersHavePatterns = hasPatterns(analyzed.getCustomizersByPath().keySet());
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

		private static boolean hasPatterns(Collection<? extends PathExpression> paths) {
			for (PathExpression path : paths) {
				if (path.hasWildcard()) {
					return true;
				}
			}
			return false;
		}
	}

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

		/**
		 * The precedence of what the scope declared for its whole scope rather than in order: its customizers, which
		 * come before its other directives.
		 */
		private DirectivePrecedence scopePrecedence() {
			return precedence(sequenceBase);
		}

		private DirectivePrecedence valuePrecedence(PathExpression path) {
			return precedence(sequenceBase + 1 + analyzed.getValueSequence(path));
		}

		private DirectivePrecedence notNullPrecedence(PathExpression path) {
			return precedence(sequenceBase + 1 + analyzed.getNotNullSequence(path));
		}

		private DirectivePrecedence sizePrecedence(PathExpression path) {
			return precedence(sequenceBase + 1 + analyzed.getContainerSizeSequence(path));
		}

		/**
		 * Returns whether the directive of {@code precedence} was declared in this scope.
		 */
		private boolean declared(DirectivePrecedence precedence) {
			int sequence = precedence.sequence();
			return !precedence.isRootScope()
				&& sequence >= sequenceBase
				&& sequence <= sequenceBase + 1 + analyzed.getLatestSequence();
		}

		private void collectValues(List<Map.Entry<ScopedPath, ValueCandidate>> values, Set<ScopedPath> justPaths) {
			for (Map.Entry<PathExpression, @Nullable Object> value : analyzed.getValuesByPath().entrySet()) {
				Map.Entry<ScopedPath, ValueCandidate> candidate = candidate(value.getKey(), value.getValue());
				values.add(candidate);
				if (analyzed.isJust(value.getKey())) {
					justPaths.add(candidate.getKey());
				}
			}
		}

		private Map.Entry<ScopedPath, ValueCandidate> candidate(PathExpression relativePath, @Nullable Object value) {
			return new AbstractMap.SimpleImmutableEntry<>(
				new ScopedPath(selector(), relativePath),
				new ValueCandidate(value, valuePrecedence(relativePath))
			);
		}
	}
}
