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

package com.navercorp.fixturemonkey.projection;

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

import com.navercorp.fixturemonkey.api.context.MonkeyGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorLoggingContext;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.option.InterfaceSelectionStrategy;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.TreeRootProperty;
import com.navercorp.fixturemonkey.planner.AnalysisResult;
import com.navercorp.fixturemonkey.planner.RuntimeTreeFactory;
import com.navercorp.fixturemonkey.property.JvmNodePropertyFactory;
import com.navercorp.fixturemonkey.tracing.TraceContext;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.expression.Segment;
import com.navercorp.objectfarm.api.expression.TypeSelector;
import com.navercorp.objectfarm.api.input.ObjectValueExtractor;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.tree.JvmNodeTree;
import com.navercorp.objectfarm.api.tree.PathResolverContext;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Mutable per-call state shared across all helpers cooperating during a single
 * {@link ValueProjection#assemble} pass.
 *
 * <p>Package-private and split out from {@link ValueProjectionAssembler} so that helpers
 * (path matching, lazy resolution, type metadata, map/interface assembly) can read the same
 * state without duplicating the parameter list.</p>
 */
final class AssemblyState {

	final JvmNodeTree nodeTree;
	final Map<PathExpression, ValueCandidate> candidatesByPath;
	final TreeRootProperty rootProperty;
	final FixtureMonkeyOptions options;
	final MonkeyGeneratorContext monkeyGeneratorContext;
	final ArbitraryGeneratorLoggingContext loggingContext;
	final Set<PathExpression> justPaths;
	final Set<PathExpression> notNullPaths;
	final Map<PathExpression, List<AnalysisResult.PostConditionFilter>> filtersByPath;
	final Map<PathExpression, Integer> limitsByPath;
	final InterfaceSelectionStrategy interfaceSelectionStrategy;
	final long assemblySeed;
	final AtomicInteger interfaceSelectionCounter;
	final Map<PathExpression, List<AnalysisResult.PropertyCustomizer>> customizersByPath;
	final TraceContext traceContext;
	final Map<Class<?>, ArbitraryIntrospector> introspectorsByType;

	final Map<JvmNode, JvmNodeTree> concreteTreeByNode;

	final Map<JvmType, Boolean> containerTypeCache;

	final Map<JvmNode, Property> propertyByNode;

	final Set<PathExpression> userContainerSizePaths;

	final PathIndex pathIndex;

	final @Nullable RuntimeTreeFactory runtimeTreeFactory;

	final @Nullable PathResolverContext pathResolverContext;

	final Function<JvmNode, Property> nodePropertyFactory;

	final Map<JvmNode, Property> propertyPathPropertyByNode;

	final Map<String, JvmNode> nodeByPath;

	final Map<LazyValueHolder, Object> resolvedLazyCache;

	final List<Map.Entry<PathExpression, ValueCandidate>> wildcardEntries;

	final List<Map.Entry<PathExpression, ValueCandidate>> typeSelectorEntries;

	final List<Map.Entry<PathExpression, ValueCandidate>> rootTypeSelectors;

	final ValueDecomposer valueDecomposer;

	final @Nullable ConcurrentHashMap<JvmType, CachedTypeMetadata> typeMetadataCache;

	AssemblyState(
		JvmNodeTree nodeTree,
		Map<PathExpression, ValueCandidate> candidatesByPath,
		TreeRootProperty rootProperty,
		FixtureMonkeyOptions options,
		MonkeyGeneratorContext monkeyGeneratorContext,
		ArbitraryGeneratorLoggingContext loggingContext,
		Set<PathExpression> justPaths,
		Set<PathExpression> notNullPaths,
		Map<PathExpression, List<AnalysisResult.PostConditionFilter>> filtersByPath,
		Map<PathExpression, Integer> limitsByPath,
		InterfaceSelectionStrategy interfaceSelectionStrategy,
		Map<PathExpression, List<AnalysisResult.PropertyCustomizer>> customizersByPath,
		TraceContext traceContext,
		Map<Class<?>, ArbitraryIntrospector> introspectorsByType,
		@Nullable RuntimeTreeFactory runtimeTreeFactory,
		@Nullable PathResolverContext pathResolverContext,
		@Nullable ConcurrentHashMap<?, ?> nodeMetadataCache,
		Set<PathExpression> userContainerSizePaths
	) {
		this.nodeTree = nodeTree;
		this.candidatesByPath = candidatesByPath;
		this.rootProperty = rootProperty;
		this.options = options;
		this.monkeyGeneratorContext = monkeyGeneratorContext;
		this.loggingContext = loggingContext;
		this.justPaths = justPaths;
		this.notNullPaths = notNullPaths;
		this.filtersByPath = filtersByPath;
		this.limitsByPath = limitsByPath;
		this.interfaceSelectionStrategy = interfaceSelectionStrategy;
		this.assemblySeed = ThreadLocalRandom.current().nextLong();
		this.interfaceSelectionCounter = new AtomicInteger(0);
		this.customizersByPath = customizersByPath;
		this.traceContext = traceContext;
		this.introspectorsByType = introspectorsByType;
		this.runtimeTreeFactory = runtimeTreeFactory;
		this.pathResolverContext = pathResolverContext;

		this.containerTypeCache = new HashMap<>();
		this.propertyByNode = new IdentityHashMap<>();
		this.concreteTreeByNode = new IdentityHashMap<>();
		this.propertyPathPropertyByNode = new IdentityHashMap<>();
		this.nodeByPath = new HashMap<>();
		this.resolvedLazyCache = new IdentityHashMap<>();
		this.valueDecomposer = new ValueDecomposer(
			candidatesByPath,
			limitsByPath,
			nodeTree,
			new ObjectValueExtractor()
		);
		this.userContainerSizePaths = userContainerSizePaths;
		this.pathIndex = new PathIndex(
			new HashSet<>(candidatesByPath.keySet()),
			userContainerSizePaths,
			new HashSet<>(customizersByPath.keySet()),
			notNullPaths
		);

		List<Map.Entry<PathExpression, ValueCandidate>> wildcards = new ArrayList<>();
		List<Map.Entry<PathExpression, ValueCandidate>> typeSelectors = new ArrayList<>();
		List<Map.Entry<PathExpression, ValueCandidate>> rootTypeOnly = new ArrayList<>();
		for (Map.Entry<PathExpression, ValueCandidate> entry : candidatesByPath.entrySet()) {
			PathExpression pattern = entry.getKey();
			if (pattern.hasWildcard()) {
				wildcards.add(entry);
			} else if (pattern.hasTypeSelector()) {
				typeSelectors.add(entry);
				List<Segment> segments = pattern.getSegments();
				if (segments.size() == 1
					&& segments.get(0).isSingleSelector()
					&& segments.get(0).getFirstSelector() instanceof TypeSelector) {
					rootTypeOnly.add(entry);
				}
			}
		}
		this.wildcardEntries = wildcards;
		this.typeSelectorEntries = typeSelectors;
		this.rootTypeSelectors = rootTypeOnly;

		this.nodePropertyFactory = new JvmNodePropertyFactory(child -> {
			JvmNode parent = nodeTree.getParent(child);
			if (parent != null) {
				return parent;
			}
			JvmNodeTree concreteTree = this.concreteTreeByNode.get(child);
			if (concreteTree != null) {
				return concreteTree.getParent(child);
			}
			return null;
		});

		if (nodeMetadataCache != null) {
			@SuppressWarnings("unchecked")
			ConcurrentHashMap<JvmType, CachedTypeMetadata> typedCache =
				(ConcurrentHashMap<JvmType, CachedTypeMetadata>)nodeMetadataCache;
			this.typeMetadataCache = typedCache;
		} else {
			this.typeMetadataCache = null;
		}
	}
}
