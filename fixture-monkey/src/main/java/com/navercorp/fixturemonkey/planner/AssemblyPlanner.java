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

package com.navercorp.fixturemonkey.planner;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.resolver.AbstractTypeResolver;
import com.navercorp.fixturemonkey.plugin.LeafTypeRegistry;
import com.navercorp.fixturemonkey.planner.AssemblyPlan;
import com.navercorp.fixturemonkey.planner.AnalysisResult;
import com.navercorp.fixturemonkey.planner.ManipulatorAnalyzer;
import com.navercorp.fixturemonkey.nodecandidate.InterfaceMethodNodeCandidateGenerator;
import com.navercorp.fixturemonkey.projection.ValueProjection;
import com.navercorp.fixturemonkey.tracing.TraceContext;
import com.navercorp.fixturemonkey.tracing.TraceContextResolutionListener;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.CandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.random.Randoms;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.customizer.PathDirective;
import com.navercorp.fixturemonkey.customizer.SizeDirective;
import com.navercorp.fixturemonkey.customizer.ManipulatorSet;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.input.ContainerDetector;
import com.navercorp.objectfarm.api.node.InterfaceResolver;
import com.navercorp.objectfarm.api.node.JavaNodeContext;
import com.navercorp.objectfarm.api.node.JvmNodeContext;
import com.navercorp.objectfarm.api.node.JvmNodePromoter;
import com.navercorp.objectfarm.api.node.LeafTypeResolver;
import com.navercorp.objectfarm.api.node.SeedState;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidateGenerator;
import com.navercorp.objectfarm.api.tree.ExpansionContext;
import com.navercorp.objectfarm.api.tree.JvmNodeCandidateTree;
import com.navercorp.objectfarm.api.tree.JvmNodeCandidateTreeContext;
import com.navercorp.objectfarm.api.tree.JvmNodeSubtreeContext;
import com.navercorp.objectfarm.api.tree.JvmNodeTree;
import com.navercorp.objectfarm.api.tree.JvmNodeTreeTransformer;
import com.navercorp.objectfarm.api.tree.PathResolver;
import com.navercorp.objectfarm.api.tree.PathResolverContext;
import com.navercorp.objectfarm.api.tree.ResolutionListener;
import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Default assembly planner. Also exposes {@link RuntimeTreeFactory} and {@link LeafTypeRegistry}.
 * <p>
 * This implementation:
 * <ol>
 *   <li>Analyzes {@link PathDirective}s to extract interface/generic type resolution info,
 *       including {@link SizeDirective} → {@code ContainerSizeResolver} translation</li>
 *   <li>Builds a {@code JvmNodeCandidateTree} from the root type</li>
 *   <li>Transforms the candidate tree into a {@code JvmNodeTree} with resolved types and
 *       container sizes</li>
 * </ol>
 * <p>
 * The {@link PathResolverContext} produced for each plan is carried back inside
 * {@link AssemblyPlan} and passed explicitly to {@link RuntimeTreeFactory#createAnonymousNodeTree}
 * at the call site, replacing the previous {@code ThreadLocal} smuggling channel.
 * <p>
 * Directive order is preserved end-to-end via {@link PathDirective#sequence()}, so later
 * directives override earlier ones at the same path.
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
public final class AssemblyPlanner implements RuntimeTreeFactory, LeafTypeRegistry {
	private static final ContainerDetector CONTAINER_DETECTOR = ContainerDetector.standard();

	private final SeedState seedState;
	// Tracks the global Random instance used for the most recent adapt() call.
	// When Randoms.newGlobalSeed() creates a new instance (e.g. via @Seed), reference inequality
	// triggers a SeedState reset so seeded reruns produce deterministic container sizes.
	@Nullable
	private Random lastSeenRandom;

	// Performance optimization cache for (JvmNodeContext, JvmNodeCandidateTree) — keyed by (type, options identity).
	// NOTE: AssemblyPlan/JvmNodeTree are NOT cached because container sizes must vary on each call.
	private final TreeContextCache treeCache;

	// Cross-call cache for assembly node metadata (Property, resolvers, isContainerType)
	// Type-erased here since CachedNodeMetadata is package-private in projection package
	private final ConcurrentHashMap<Object, Object> nodeMetadataCache = new ConcurrentHashMap<>();

	// Additional leaf type resolvers (e.g., KotlinLeafTypeResolver for Kotlin support).
	// Held here for isLeafType lookups; node-context construction reads them via NodeContextFactory.
	private final List<LeafTypeResolver> additionalLeafTypeResolvers;

	private final ContainerSizeResolverFactory containerSizeResolverFactory;
	private final ContainerValuePruner containerValuePruner;
	private final AbstractTypeResolver abstractTypeResolver;
	private final PathResolverContextFactory pathResolverContextFactory;

	/**
	 * Creates a new adapter with the specified seed.
	 *
	 * @param seed the seed for random generation
	 */
	public AssemblyPlanner(long seed) {
		this(seed, new JvmNodeCandidateTreeContext(), Collections.emptyList(), Collections.emptyList(), null);
	}

	/**
	 * Creates a new adapter with all configurable components.
	 *
	 * @param seed                       the seed for random generation
	 * @param treeContext                the tree context for caching subtree information
	 * @param additionalPromoters        additional node promoters (e.g., KotlinNodePromoter)
	 * @param additionalLeafTypeResolvers additional leaf type resolvers (e.g., KotlinLeafTypeResolver)
	 * @param candidateGeneratorWrapper  wraps the candidate generator with platform-specific
	 *                                   isSupported checks (e.g., KotlinNodeCandidateGenerator)
	 */
	public AssemblyPlanner(
		long seed,
		JvmNodeCandidateTreeContext treeContext,
		List<JvmNodePromoter> additionalPromoters,
		List<LeafTypeResolver> additionalLeafTypeResolvers,
		@Nullable UnaryOperator<JvmNodeCandidateGenerator> candidateGeneratorWrapper
	) {
		this.seedState = new SeedState(seed);
		List<JvmNodePromoter> resolvedPromoters = additionalPromoters != null
			? additionalPromoters
			: Collections.emptyList();
		this.additionalLeafTypeResolvers =
			additionalLeafTypeResolvers != null ? additionalLeafTypeResolvers : Collections.emptyList();
		this.containerSizeResolverFactory = new ContainerSizeResolverFactory(seedState);
		this.containerValuePruner = new ContainerValuePruner(CONTAINER_DETECTOR);
		NodeContextFactory nodeContextFactory = new NodeContextFactory(
			seedState,
			containerSizeResolverFactory,
			resolvedPromoters,
			this.additionalLeafTypeResolvers,
			candidateGeneratorWrapper
		);
		this.treeCache = new TreeContextCache(treeContext, nodeContextFactory);
		this.abstractTypeResolver = new AbstractTypeResolver(seedState);
		this.pathResolverContextFactory = new PathResolverContextFactory(
			containerSizeResolverFactory,
			CONTAINER_DETECTOR
		);
	}

	public AssemblyPlan plan(
		JvmType rootType,
		ManipulatorSet manipulatorSet,
		@Nullable FixtureMonkeyOptions options,
		@Nullable TraceContext traceContext
	) {
		// When tracing is enabled, skip caching to ensure all resolution events are captured
		ResolutionListener resolutionListener = TraceContextResolutionListener.of(traceContext);

		resetSeedStateIfRandomChanged();
		if (manipulatorSet.isEmpty()) {
			return buildDefaultAssemblyPlan(rootType, options, resolutionListener, manipulatorSet.isFixed());
		}

		return buildAssemblyPlan(rootType, manipulatorSet, options, resolutionListener);
	}

	private synchronized void resetSeedStateIfRandomChanged() {
		Random current = Randoms.current();
		if (current != lastSeenRandom) {
			seedState.reset(Randoms.currentSeed());
			lastSeenRandom = current;
		}
	}

	private AssemblyPlan buildDefaultAssemblyPlan(
		JvmType rootType,
		@Nullable FixtureMonkeyOptions options,
		ResolutionListener resolutionListener,
		boolean isFixed
	) {
		JvmType resolvedRootType = walkCandidateChain(rootType, options);

		long treeBuildStart = System.nanoTime();

		JvmNodeContext context = treeCache.getOrBuildNodeContext(
			resolvedRootType,
			options,
			Collections.emptyMap(),
			Collections.emptyMap()
		);
		JvmNodeCandidateTree candidateTree = treeCache.getOrBuildCandidateTree(resolvedRootType, context, options, false);

		PathResolverContext.Builder resolverContextBuilder = PathResolverContext.builder().resolutionListener(
			resolutionListener
		);
		if (isFixed) {
			resolverContextBuilder.defaultContainerSizeResolver(
				containerSizeResolverFactory.createFixedContainerSizeResolver(options)
			);
		}
		PathResolverContext resolverContext = resolverContextBuilder.build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(
			context,
			treeCache.getTreeContext(),
			resolverContext,
			null, // No expansion context for empty manipulators
			treeCache.getSubtreeContext()
		);

		JvmNodeTree nodeTree = transformer.transform(candidateTree);
		long treeBuildTimeNanos = System.nanoTime() - treeBuildStart;

		AnalysisResult analysisResult = ManipulatorAnalyzer.emptyResult();
		ValueProjection valueProjection = ValueProjection.of(nodeTree, Collections.emptyMap());

		return new AssemblyPlan(
			nodeTree,
			valueProjection,
			analysisResult,
			0,
			treeBuildTimeNanos,
			false,
			resolverContext
		);
	}

	private AssemblyPlan buildAssemblyPlan(
		JvmType rootType,
		ManipulatorSet manipulatorSet,
		@Nullable FixtureMonkeyOptions options,
		ResolutionListener resolutionListener
	) {
		List<PathDirective> directives = manipulatorSet.getDirectives();

		@SuppressWarnings({"argument", "methodref.return", "return"})
		Function<Property, String> nameResolver = options != null
			? property -> options.getPropertyNameResolver(property).resolve(property)
			: (Property p) -> p.getName();
		long analyzeStart = System.nanoTime();
		AnalysisResult analysisResult = ManipulatorAnalyzer.analyze(directives, nameResolver);
		long analyzeTimeNanos = System.nanoTime() - analyzeStart;

		// Resolve interface/abstract class to concrete implementation
		// For non-container abstract types, check if there's a "$" path InterfaceResolver
		// from explicit set(concreteValue) - it takes precedence over default resolution
		JvmType resolvedRootType = resolveRootType(rootType, analysisResult, options);

		Map<PathExpression, @Nullable Object> prunedValuesByPath =
			containerValuePruner.pruneValuesExceedingContainerSize(
				analysisResult.getValuesByPath(),
				analysisResult.getLatestSizeDirectiveByPath(),
				analysisResult.getValueOrderByPath()
			);

		// Remove child values under just paths — Values.just() makes the value immutable,
		// so child path values (e.g., $.string) should not override the just value (e.g., $).
		ContainerValuePruner.pruneChildrenOfJustPaths(prunedValuesByPath, analysisResult.getJustPaths());

		Map<JvmType, Map<String, ArbitraryContainerInfo>> mergedTypedContainerSizes =
			pathResolverContextFactory.inferAndMergeTypedContainerSizes(
				manipulatorSet.getTypedValues(),
				manipulatorSet.getTypedContainerSizes()
			);

		PathResolverContext resolverContext = pathResolverContextFactory.build(
			analysisResult,
			mergedTypedContainerSizes,
			resolutionListener,
			manipulatorSet.isFixed(),
			options
		);

		// Use fresh treeCache.getTreeContext() when propertyConfigurers or introspectorsByType are present
		// to avoid caching issues (cached subtrees don't respect custom property generators)
		boolean hasCustomConfigurers =
			!manipulatorSet.getPropertyConfigurers().isEmpty()
				|| !manipulatorSet.getArbitraryIntrospectorsByType().isEmpty();

		JvmNodeContext context = treeCache.getOrBuildNodeContext(
			resolvedRootType,
			options,
			manipulatorSet.getPropertyConfigurers(),
			manipulatorSet.getArbitraryIntrospectorsByType()
		);

		ExpansionContext expansionContext = null;
		Set<PathExpression> userPaths = analysisResult.getValuesByPath().keySet();
		if (!userPaths.isEmpty()) {
			expansionContext = new ExpansionContext(userPaths);
		}

		JvmNodeCandidateTreeContext effectiveTreeContext = hasCustomConfigurers
			? new JvmNodeCandidateTreeContext()
			: treeCache.getTreeContext();

		long treeBuildStart = System.nanoTime();
		JvmNodeCandidateTree candidateTree = treeCache.getOrBuildCandidateTree(
			resolvedRootType,
			context,
			options,
			hasCustomConfigurers
		);

		JvmNodeSubtreeContext effectiveSubtreeContext = hasCustomConfigurers
			? null
			: treeCache.getSubtreeContext();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(
			context,
			effectiveTreeContext,
			resolverContext,
			expansionContext,
			effectiveSubtreeContext
		);

		JvmNodeTree nodeTree = transformer.transform(candidateTree);
		long treeBuildTimeNanos = System.nanoTime() - treeBuildStart;

		ValueProjection valueProjection = ValueProjection.fromPathExpressionMap(nodeTree, prunedValuesByPath);

		return new AssemblyPlan(
			nodeTree,
			valueProjection,
			analysisResult,
			analyzeTimeNanos,
			treeBuildTimeNanos,
			false,
			resolverContext
		);
	}

	/**
	 * Resolves the planning-phase root type.
	 * <p>
	 * For non-container abstract/interface roots, an explicit {@code $}-path
	 * {@link InterfaceResolver} from {@code set(concreteValue)} takes precedence (last-wins).
	 * Otherwise — including for {@link Collection}/{@link Map} roots whose element types are
	 * resolved later by {@link JvmNodeTreeTransformer} — delegates to
	 * {@link AbstractTypeResolver#resolve}.
	 */
	private JvmType resolveRootType(
		JvmType rootType,
		AnalysisResult analysisResult,
		@Nullable FixtureMonkeyOptions options
	) {
		Class<?> rawType = rootType.getRawType();
		boolean abstractOrInterface =
			Modifier.isInterface(rawType.getModifiers()) || Modifier.isAbstract(rawType.getModifiers());
		if (!abstractOrInterface
			|| Collection.class.isAssignableFrom(rawType)
			|| Map.class.isAssignableFrom(rawType)) {
			return walkCandidateChain(rootType, options);
		}

		PathExpression rootPath = PathExpression.of("$");
		List<PathResolver<InterfaceResolver>> resolvers = analysisResult.getInterfaceResolvers();
		for (int i = resolvers.size() - 1; i >= 0; i--) {
			PathResolver<InterfaceResolver> resolver = resolvers.get(i);
			if (resolver.matches(rootPath)) {
				JvmType resolved = resolver.getCustomizer().resolve(rootType);
				if (resolved != null) {
					return resolved;
				}
			}
		}

		return walkCandidateChain(rootType, options);
	}

	/**
	 * Adapts {@link FixtureMonkeyOptions} to {@link AbstractTypeResolver#resolve} inputs and
	 * passes through the input type when {@code options} is null.
	 */
	private JvmType walkCandidateChain(JvmType type, @Nullable FixtureMonkeyOptions options) {
		if (options == null) {
			return type;
		}
		@SuppressWarnings("deprecation")
		Function<Property, @Nullable CandidateConcretePropertyResolver> resolverLookup =
			options::getCandidateConcretePropertyResolver;
		return abstractTypeResolver.resolve(type, resolverLookup, options.getMaxRecursionDepth());
	}

	@Override
	public @Nullable JvmNodeTree createConcreteNodeTree(JvmType concreteType, @Nullable FixtureMonkeyOptions options) {
		if (options == null) {
			return null;
		}

		JvmNodeContext context = treeCache.getOrBuildNodeContext(
			concreteType,
			options,
			Collections.emptyMap(),
			Collections.emptyMap()
		);

		JvmNodeCandidateTree candidateTree = treeCache.getOrBuildConcreteCandidateTree(concreteType, context, options);

		PathResolverContext localContext = PathResolverContext.builder().build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(
			context,
			treeCache.getTreeContext(),
			localContext,
			null,
			treeCache.getSubtreeContext()
		);

		return transformer.transform(candidateTree);
	}

	@Override
	public @Nullable JvmNodeTree createAnonymousNodeTree(
		JvmType interfaceType,
		@Nullable FixtureMonkeyOptions options,
		PathResolverContext resolverContext
	) {
		if (options == null) {
			return null;
		}

		JavaNodeContext baseContext = (JavaNodeContext)treeCache.getOrBuildNodeContext(
			interfaceType,
			options,
			Collections.emptyMap(),
			Collections.emptyMap()
		);

		JavaNodeContext anonymousContext = baseContext.withAdditionalGenerator(
			new InterfaceMethodNodeCandidateGenerator()
		);

		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(interfaceType, anonymousContext)
			.withTreeContext(treeCache.getTreeContext())
			.withPreBuildResolvedTypes(true)
			.withSkipAbstractLeafCheck(true)
			.build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(
			anonymousContext,
			treeCache.getTreeContext(),
			resolverContext,
			null,
			treeCache.getSubtreeContext()
		);

		return transformer.transform(candidateTree);
	}

	@Override
	public boolean isLeafType(Class<?> type) {
		if (Types.isJavaType(type)) {
			return true;
		}

		JvmType jvmType = new JavaType(type);
		for (LeafTypeResolver resolver : additionalLeafTypeResolvers) {
			if (resolver.isLeafType(jvmType)) {
				return true;
			}
		}

		return false;
	}

	public void clearCache() {
		treeCache.clear();
		nodeMetadataCache.clear();
	}

	/**
	 * Returns the cross-call node metadata cache used by {@code Assembler} for assembly optimization.
	 * <p>
	 * The cache maps {@code JvmType} instances to derived metadata (Property, resolvers, etc.)
	 * across multiple assembly calls.
	 *
	 * @return the node metadata cache
	 */
	public ConcurrentHashMap<?, ?> nodeMetadataCache() {
		return nodeMetadataCache;
	}

}
