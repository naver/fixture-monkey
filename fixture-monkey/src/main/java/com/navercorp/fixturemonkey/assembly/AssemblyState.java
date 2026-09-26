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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.customizer.ScopeChain;
import com.navercorp.fixturemonkey.decompose.DecomposedContainerDetector;
import com.navercorp.fixturemonkey.decompose.PropertyFieldExtractor;
import com.navercorp.fixturemonkey.planner.LazyValueHolder;
import com.navercorp.fixturemonkey.property.JvmNodePropertyFactory;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.input.ContainerDetector;
import com.navercorp.objectfarm.api.input.ObjectValueExtractor;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Mutable per-call state shared across all helpers cooperating during a single
 * {@link ValueProjectionAssembler#assemble} pass.
 *
 * <p>Package-private and split out from {@link ValueProjectionAssembler} so that helpers
 * (path matching, lazy resolution, type metadata, map/interface assembly) can read the same
 * state without duplicating the parameter list.</p>
 */
final class AssemblyState {

	final AssembleContext context;
	final Map<PathExpression, ValueCandidate> candidatesByPath;
	/**
	 * The paths assembly found must not be null, apart from what the scopes declared not null.
	 */
	final Set<PathExpression> requiredNotNullPaths;
	final LimitCounter limits;
	final long assemblySeed;
	final AtomicInteger interfaceSelectionCounter;

	final AssemblyTree assemblyTree;

	final Map<JvmType, Boolean> containerTypeCache;

	final Map<JvmNode, Property> generationPropertyCache;

	final PathIndex pathIndex;

	final Function<JvmNode, Property> generationPropertyFactory;

	private final Map<JvmNode, Property> matchingPropertyCache;

	private final Function<JvmNode, Property> matchingPropertyFactory;

	final Map<JvmNode, Property> propertyPathPropertyByNode;

	final Map<PathExpression, Integer> decomposedScopeDepthByPath;

	final Map<LazyValueHolder, Object> resolvedLazyCache;

	final List<Map.Entry<PathExpression, ValueCandidate>> wildcardEntries;

	final ScopeLookup scopes;

	final ValueDecomposer valueDecomposer;

	final ConcurrentHashMap<JvmType, CachedTypeMetadata> typeMetadataCache;

	AssemblyState(AssembleContext context) {
		this.context = context;
		this.candidatesByPath = rootScopeCandidates(context);
		this.scopes = ScopeLookup.from(context.getAnalyzedRootScope(), context.getAnalyzedDefinedScopes());
		this.requiredNotNullPaths = new HashSet<>();
		this.limits = new LimitCounter(context.getAnalyzedRootScope(), context.getAnalyzedDefinedScopes());
		this.assemblySeed = ThreadLocalRandom.current().nextLong();
		this.interfaceSelectionCounter = new AtomicInteger(0);

		this.containerTypeCache = new HashMap<>();
		this.generationPropertyCache = new IdentityHashMap<>();
		this.assemblyTree = new AssemblyTree(context.getPlan().getNodeTree());
		this.propertyPathPropertyByNode = new IdentityHashMap<>();
		this.decomposedScopeDepthByPath = new HashMap<>();
		this.resolvedLazyCache = new IdentityHashMap<>();
		FixtureMonkeyOptions options = context.getOptions();
		ContainerDetector containerDetector =
			new DecomposedContainerDetector(options.getDecomposedContainerValueFactory());
		this.valueDecomposer = new ValueDecomposer(
			candidatesByPath,
			limits,
			new ObjectValueExtractor(
				new PropertyFieldExtractor(
					property -> options.getPropertyNameResolver(property).resolve(property),
					context.getPlan().getInlinedValueResolver()
				),
				containerDetector
			),
			containerDetector,
			assemblyTree
		);
		this.pathIndex = new PathIndex(
			new HashSet<>(candidatesByPath.keySet()),
			context.getRootContainerSizePaths(),
			scopes.getRootDeclaredPaths()
		);

		List<Map.Entry<PathExpression, ValueCandidate>> wildcards = new ArrayList<>();
		for (Map.Entry<PathExpression, ValueCandidate> entry : candidatesByPath.entrySet()) {
			if (entry.getKey().hasWildcard()) {
				wildcards.add(entry);
			}
		}
		this.wildcardEntries = wildcards;

		this.generationPropertyFactory = new JvmNodePropertyFactory(assemblyTree::parentOf);
		this.matchingPropertyCache = new IdentityHashMap<>();
		this.matchingPropertyFactory = JvmNodePropertyFactory.forMatching(assemblyTree::parentOf);

		@SuppressWarnings("unchecked")
		ConcurrentHashMap<JvmType, CachedTypeMetadata> typeMetadataCache =
			(ConcurrentHashMap<JvmType, CachedTypeMetadata>)context.getPlan().getTypeMetadataCache();
		this.typeMetadataCache = typeMetadataCache;
	}

	/**
	 * Returns the property of a node for generating its value, with the type chosen to be built for it.
	 */
	// The root scope's values were expanded while planning, so each is a candidate at its path from the root
	private static Map<PathExpression, ValueCandidate> rootScopeCandidates(AssembleContext context) {
		Map<PathExpression, @Nullable Object> values = context.getPlan().getValues().getValuesByPath();
		Map<PathExpression, Integer> orderByPath = context.getRootValueOrderByPath();
		Map<PathExpression, ValueCandidate> candidates = new HashMap<>();
		for (Map.Entry<PathExpression, @Nullable Object> entry : values.entrySet()) {
			PathExpression path = entry.getKey();
			DirectivePrecedence precedence = DirectivePrecedence.rootScope(orderByPath.getOrDefault(path, 0));
			candidates.put(path, new ValueCandidate(entry.getValue(), precedence));
		}
		return candidates;
	}

	Property generationPropertyOf(JvmNode node) {
		return generationPropertyCache.computeIfAbsent(node, generationPropertyFactory);
	}

	/**
	 * Returns the property of a node for matching what it is, with the type it is declared with.
	 */
	Property matchingPropertyOf(JvmNode node) {
		return matchingPropertyCache.computeIfAbsent(node, matchingPropertyFactory);
	}

	/**
	 * Returns the chain of the nodes placed along {@code path}, where the scopes that apply at its end are looked up.
	 */
	ScopeChain chainOf(PathExpression path) {
		return ScopeChain.ofPath(path, assemblyTree::nodeAt, this::matchingPropertyOf);
	}
}
