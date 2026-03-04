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

package com.navercorp.fixturemonkey.adapter;

import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.adapter.analysis.AdaptationResult;
import com.navercorp.fixturemonkey.adapter.analysis.AnalysisResult;
import com.navercorp.fixturemonkey.adapter.analysis.ManipulatorAnalyzer;
import com.navercorp.fixturemonkey.adapter.converter.ContainerInfoResolverConverter;
import com.navercorp.fixturemonkey.adapter.converter.PredicatePathConverter;
import com.navercorp.fixturemonkey.adapter.nodecandidate.ContainerPropertyGeneratorNodeGenerator;
import com.navercorp.fixturemonkey.adapter.nodecandidate.InterfaceMethodNodeCandidateGenerator;
import com.navercorp.fixturemonkey.adapter.nodecandidate.NameResolvingNodeCandidateGenerator;
import com.navercorp.fixturemonkey.adapter.nodecandidate.PropertyGeneratorNodeCandidateGenerator;
import com.navercorp.fixturemonkey.adapter.projection.ValueProjection;
import com.navercorp.fixturemonkey.adapter.property.JvmNodePropertyFactory;
import com.navercorp.fixturemonkey.adapter.tracing.TraceContext;
import com.navercorp.fixturemonkey.adapter.tracing.TraceContextResolutionListener;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.CandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.property.FieldProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.customizer.ArbitraryManipulator;
import com.navercorp.fixturemonkey.customizer.ContainerInfoManipulator;
import com.navercorp.fixturemonkey.customizer.ManipulatorSet;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.input.ContainerDetector;
import com.navercorp.objectfarm.api.node.AbstractTypeNodePromoter;
import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.GenericTypeResolver;
import com.navercorp.objectfarm.api.node.InterfaceResolver;
import com.navercorp.objectfarm.api.node.JavaDefaultNodePromoter;
import com.navercorp.objectfarm.api.node.JavaMapEntryNodePromoter;
import com.navercorp.objectfarm.api.node.JavaMapNodePromoter;
import com.navercorp.objectfarm.api.node.JavaNodeContext;
import com.navercorp.objectfarm.api.node.JavaObjectNodePromoter;
import com.navercorp.objectfarm.api.node.JvmNodeContext;
import com.navercorp.objectfarm.api.node.JvmNodePromoter;
import com.navercorp.objectfarm.api.node.LeafTypeResolver;
import com.navercorp.objectfarm.api.node.SeedState;
import com.navercorp.objectfarm.api.nodecandidate.JavaFieldNodeCandidateGenerator;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidateGenerator;
import com.navercorp.objectfarm.api.tree.ExpansionContext;
import com.navercorp.objectfarm.api.tree.JvmNodeCandidateTree;
import com.navercorp.objectfarm.api.tree.JvmNodeCandidateTreeContext;
import com.navercorp.objectfarm.api.tree.JvmNodeSubtreeContext;
import com.navercorp.objectfarm.api.tree.JvmNodeTree;
import com.navercorp.objectfarm.api.tree.JvmNodeTreeTransformer;
import com.navercorp.objectfarm.api.tree.PathInterfaceResolver;
import com.navercorp.objectfarm.api.tree.PathResolver;
import com.navercorp.objectfarm.api.tree.PathResolverContext;
import com.navercorp.objectfarm.api.tree.ResolutionListener;
import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.WildcardRawType;

/**
 * Default implementation of NodeTreeAdapter.
 * <p>
 * This adapter:
 * <ol>
 *   <li>Analyzes ArbitraryManipulator to extract interface/generic type resolution info</li>
 *   <li>Converts ContainerInfoManipulator to ContainerSizeResolver</li>
 *   <li>Builds a JvmNodeCandidateTree from the root type</li>
 *   <li>Transforms the candidate tree to JvmNodeTree with resolved types and container sizes</li>
 * </ol>
 * <p>
 * The transformation respects the manipulatingSequence order of ContainerInfoManipulators.
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
public final class DefaultNodeTreeAdapter implements NodeTreeAdapter {
	private static final ContainerDetector CONTAINER_DETECTOR = ContainerDetector.standard();

	private final SeedState seedState;
	private final JvmNodeCandidateTreeContext treeContext;

	// Performance optimization caches - keyed by (type, options identity)
	// NOTE: AdaptationResult/JvmNodeTree are NOT cached because container sizes must vary on each call.
	private final Map<ContextCacheKey, JvmNodeContext> nodeContextCache;
	private final Map<ContextCacheKey, JvmNodeCandidateTree> candidateTreeCache;

	// Cache for concrete type JvmNodeCandidateTrees (used when generating with interface implementations)
	private final Map<ContextCacheKey, JvmNodeCandidateTree> concreteTypeCandidateTreeCache;

	// Cache for promoted POJO subtree snapshots — reuses immutable node structures across transform() calls
	private final JvmNodeSubtreeContext subtreeContext =
		new JvmNodeSubtreeContext();
	// Cross-call cache for assembly node metadata (Property, resolvers, isContainerType)
	// Type-erased here since CachedNodeMetadata is package-private in projection package
	private final ConcurrentHashMap<Object, Object> nodeMetadataCache = new ConcurrentHashMap<>();

	// Additional node promoters (e.g., KotlinNodePromoter for Kotlin support)
	private final List<JvmNodePromoter> additionalPromoters;

	// Additional leaf type resolvers (e.g., KotlinLeafTypeResolver for Kotlin support)
	private final List<LeafTypeResolver> additionalLeafTypeResolvers;

	// Wraps a JvmNodeCandidateGenerator with platform-specific isSupported checks
	// (e.g., KotlinNodeCandidateGenerator that only supports Kotlin types)
	private final @Nullable UnaryOperator<JvmNodeCandidateGenerator> candidateGeneratorWrapper;

	private final ContainerSizeResolverFactory containerSizeResolverFactory;
	private final ContainerValuePruner containerValuePruner;

	// Thread-local storage for the current PathResolverContext during adaptation.
	// Set in buildAdaptationResult(), read in createAnonymousNodeTree() during assembly.
	@SuppressWarnings({"type.argument", "assignment"})
	private final ThreadLocal<PathResolverContext> currentResolverContext = new ThreadLocal<>();

	/**
	 * Creates a new adapter with the specified seed.
	 *
	 * @param seed the seed for random generation
	 */
	public DefaultNodeTreeAdapter(long seed) {
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
	public DefaultNodeTreeAdapter(
		long seed,
		JvmNodeCandidateTreeContext treeContext,
		List<JvmNodePromoter> additionalPromoters,
		List<LeafTypeResolver> additionalLeafTypeResolvers,
		@Nullable UnaryOperator<JvmNodeCandidateGenerator> candidateGeneratorWrapper
	) {
		this.seedState = new SeedState(seed);
		this.treeContext = treeContext;
		this.nodeContextCache = new ConcurrentHashMap<>();
		this.candidateTreeCache = new ConcurrentHashMap<>();
		this.concreteTypeCandidateTreeCache = new ConcurrentHashMap<>();
		this.additionalPromoters = additionalPromoters != null ? additionalPromoters : Collections.emptyList();
		this.additionalLeafTypeResolvers =
			additionalLeafTypeResolvers != null ? additionalLeafTypeResolvers : Collections.emptyList();
		this.candidateGeneratorWrapper = candidateGeneratorWrapper;
		this.containerSizeResolverFactory = new ContainerSizeResolverFactory(seedState);
		this.containerValuePruner = new ContainerValuePruner(CONTAINER_DETECTOR);
	}

	@Override
	public AdaptationResult adapt(JvmType rootType, ManipulatorSet manipulatorSet) {
		return adapt(rootType, manipulatorSet, null);
	}

	@Override
	public AdaptationResult adapt(
		JvmType rootType, ManipulatorSet manipulatorSet, @Nullable FixtureMonkeyOptions options
	) {
		// NOTE: AdaptationResult is NOT cached because container sizes must vary on each call.
		// JvmNodeCandidateTree and JvmNodeContext are still cached for performance.
		if (manipulatorSet.isEmpty()) {
			return buildDefaultAdaptationResult(rootType, options, ResolutionListener.noOp(), manipulatorSet.isFixed());
		}

		return buildAdaptationResult(rootType, manipulatorSet, options, ResolutionListener.noOp());
	}

	@Override
	public AdaptationResult adapt(
		JvmType rootType,
		ManipulatorSet manipulatorSet,
		@Nullable FixtureMonkeyOptions options,
		@Nullable TraceContext traceContext
	) {
		// When tracing is enabled, skip caching to ensure all resolution events are captured
		ResolutionListener resolutionListener = TraceContextResolutionListener.of(traceContext);

		if (manipulatorSet.isEmpty()) {
			return buildDefaultAdaptationResult(rootType, options, resolutionListener, manipulatorSet.isFixed());
		}

		return buildAdaptationResult(rootType, manipulatorSet, options, resolutionListener);
	}

	private AdaptationResult buildDefaultAdaptationResult(
		JvmType rootType,
		@Nullable FixtureMonkeyOptions options,
		ResolutionListener resolutionListener,
		boolean isFixed
	) {
		JvmType resolvedRootType = resolveAbstractType(rootType, options);

		long treeBuildStart = System.nanoTime();

		JvmNodeContext context = getOrBuildNodeContext(
			resolvedRootType,
			options,
			Collections.emptyMap(),
			Collections.emptyMap()
		);
		JvmNodeCandidateTree candidateTree = getOrBuildCandidateTree(resolvedRootType, context, options, false);

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
			treeContext,
			resolverContext,
			null, // No expansion context for empty manipulators
			subtreeContext
		);

		JvmNodeTree nodeTree = transformer.transform(candidateTree);
		long treeBuildTimeNanos = System.nanoTime() - treeBuildStart;

		AnalysisResult analysisResult = ManipulatorAnalyzer.emptyResult();
		ValueProjection valueProjection = ValueProjection.of(nodeTree, Collections.emptyMap());

		return new AdaptationResult(nodeTree, valueProjection, analysisResult, 0, treeBuildTimeNanos);
	}

	private AdaptationResult buildAdaptationResult(
		JvmType rootType,
		ManipulatorSet manipulatorSet,
		@Nullable FixtureMonkeyOptions options,
		ResolutionListener resolutionListener
	) {
		List<ArbitraryManipulator> arbitraryManipulators = manipulatorSet.getArbitraryManipulators();

		ManipulatorAnalyzer.DecomposeNameResolver nameResolver =
			options != null
				? property -> options.getPropertyNameResolver(property).resolve(property)
				: Property::getName;
		long analyzeStart = System.nanoTime();
		AnalysisResult analysisResult = ManipulatorAnalyzer.analyze(
			arbitraryManipulators,
			nameResolver
		);
		long analyzeTimeNanos = System.nanoTime() - analyzeStart;

		// Resolve interface/abstract class to concrete implementation
		// For non-container abstract types, check if there's a "$" path InterfaceResolver
		// from explicit set(concreteValue) - it takes precedence over default resolution
		JvmType resolvedRootType = resolveRootTypeWithAnalysisResult(rootType, analysisResult, options);

		List<ContainerInfoManipulator> containerManipulators = manipulatorSet.getContainerInfoManipulators();

		Map<ContainerInfoManipulator, PathExpression> containerPathCache = new IdentityHashMap<>();
		for (ContainerInfoManipulator m : containerManipulators) {
			containerPathCache.put(m, PredicatePathConverter.convert(m.getNextNodePredicates()));
		}

		List<PathResolver<ContainerSizeResolver>> containerResolvers = ContainerInfoResolverConverter.convert(
			containerManipulators,
			containerPathCache
		);

		Map<PathExpression, Integer> globalValueSequences = new HashMap<>(analysisResult.getValueOrderByPath());

		Map<PathExpression, @Nullable Object> prunedValuesByPath =
			containerValuePruner.pruneValuesExceedingContainerSize(
				analysisResult.getValuesByPath(),
				containerManipulators,
				globalValueSequences,
				containerPathCache
			);

		// Remove child values under just paths — Values.just() makes the value immutable,
		// so child path values (e.g., $.string) should not override the just value (e.g., $).
		ContainerValuePruner.pruneChildrenOfJustPaths(prunedValuesByPath, analysisResult.getJustPaths());

		Map<JvmType, Map<String, ArbitraryContainerInfo>> mergedTypedContainerSizes =
			inferAndMergeTypedContainerSizes(
				manipulatorSet.getTypedValues(),
				manipulatorSet.getTypedContainerSizes()
			);

		PathResolverContext resolverContext = createResolverContext(
			containerResolvers,
			analysisResult,
			containerManipulators,
			globalValueSequences,
			mergedTypedContainerSizes,
			resolutionListener,
			manipulatorSet.isFixed(),
			options,
			containerPathCache
		);

		// Use fresh treeContext when propertyConfigurers or introspectorsByType are present
		// to avoid caching issues (cached subtrees don't respect custom property generators)
		boolean hasCustomConfigurers =
			!manipulatorSet.getPropertyConfigurers().isEmpty()
				|| !manipulatorSet.getArbitraryIntrospectorsByType().isEmpty();

		JvmNodeContext context = getOrBuildNodeContext(
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
			: treeContext;

		long treeBuildStart = System.nanoTime();
		JvmNodeCandidateTree candidateTree = getOrBuildCandidateTree(
			resolvedRootType,
			context,
			options,
			hasCustomConfigurers
		);

		JvmNodeSubtreeContext effectiveSubtreeContext = hasCustomConfigurers
			? null
			: subtreeContext;

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

		currentResolverContext.set(resolverContext);

		return new AdaptationResult(nodeTree, valueProjection, analysisResult, analyzeTimeNanos, treeBuildTimeNanos);
	}

	private JvmType resolveAbstractType(JvmType type, @Nullable FixtureMonkeyOptions options) {
		if (options == null) {
			return type;
		}

		JvmType currentType = type;
		Set<Class<?>> visited = new HashSet<>();
		int maxDepth = options.getMaxRecursionDepth();

		for (int depth = 0; depth < maxDepth; depth++) {
			Class<?> rawType = currentType.getRawType();

			if (!visited.add(rawType)) {
				return currentType;
			}

			if (!Modifier.isInterface(rawType.getModifiers()) && !Modifier.isAbstract(rawType.getModifiers())) {
				return currentType;
			}

			Property property = JvmNodePropertyFactory.fromType(currentType);
			@SuppressWarnings("deprecation")
			CandidateConcretePropertyResolver resolver = options.getCandidateConcretePropertyResolver(property);

			if (resolver == null) {
				return currentType;
			}

			List<Property> candidates = resolver.resolve(property);
			if (candidates == null || candidates.isEmpty()) {
				return currentType;
			}

			Random random = seedState.snapshot().randomFor(currentType.hashCode());
			Property selected = candidates.get(random.nextInt(candidates.size()));

			currentType = Types.toJvmType(selected.getAnnotatedType(), selected.getAnnotations());
		}

		return currentType;
	}

	/**
	 * Resolves the root type using InterfaceResolvers from analysis result.
	 * If there's a "$" path InterfaceResolver (from explicit set(concreteValue)),
	 * it takes precedence over the default resolution.
	 * <p>
	 * This only applies to non-container abstract/interface types.
	 * Container types (List, Set, Map) should use the default resolution.
	 */
	private JvmType resolveRootTypeWithAnalysisResult(
		JvmType rootType,
		AnalysisResult analysisResult,
		@Nullable FixtureMonkeyOptions options
	) {
		Class<?> rawType = rootType.getRawType();

		if (!Modifier.isInterface(rawType.getModifiers()) && !Modifier.isAbstract(rawType.getModifiers())) {
			return rootType;
		}

		if (Collection.class.isAssignableFrom(rawType) || Map.class.isAssignableFrom(rawType)) {
			return resolveAbstractType(rootType, options);
		}

		// Check if there's a "$" path InterfaceResolver from explicit set(concreteValue).
		// Iterate from the end so the last set() call wins.
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

		return resolveAbstractType(rootType, options);
	}

	private PathResolverContext createResolverContext(
		List<PathResolver<ContainerSizeResolver>> containerResolvers,
		AnalysisResult analysisResult,
		List<ContainerInfoManipulator> containerManipulators,
		Map<PathExpression, Integer> globalValueSequences,
		Map<JvmType, Map<String, ArbitraryContainerInfo>> typedContainerSizes,
		ResolutionListener resolutionListener,
		boolean isFixed,
		@Nullable FixtureMonkeyOptions options,
		Map<ContainerInfoManipulator, PathExpression> containerPathCache
	) {
		PathResolverContext.Builder builder = PathResolverContext.builder().resolutionListener(resolutionListener);

		Map<PathExpression, Integer> sizeSequenceByPath = containerSizeResolverFactory.collectSizeSequences(
			containerManipulators,
			containerPathCache
		);

		List<Map.Entry<PathExpression, Integer>> wildcardSizeSequences = new ArrayList<>();
		for (Map.Entry<PathExpression, Integer> entry : sizeSequenceByPath.entrySet()) {
			if (entry.getKey().hasWildcard()) {
				wildcardSizeSequences.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
			}
		}

		for (PathResolver<ContainerSizeResolver> resolver : containerResolvers) {
			builder.addContainerSizeResolver(resolver);
		}

		containerSizeResolverFactory.addLazyContainerSizeResolvers(
			builder,
			analysisResult,
			sizeSequenceByPath,
			wildcardSizeSequences
		);
		containerSizeResolverFactory.addManipulatorContainerSizeResolvers(
			builder,
			containerManipulators,
			globalValueSequences,
			sizeSequenceByPath,
			containerPathCache
		);

		Map<PathExpression, Integer> valueOrderByPath = analysisResult.getValueOrderByPath();
		for (PathResolver<InterfaceResolver> resolver : analysisResult.getInterfaceResolvers()) {
			// Skip interface resolvers for container paths where a later size() constraint exists.
			// When set("values", singletonList(X)) creates a List → SingletonList resolver,
			// but size("values", 1, 10) comes later, the concrete type should not be locked.
			if (resolver instanceof PathInterfaceResolver) {
				PathExpression resolverPath = ((PathInterfaceResolver)resolver).getPattern();
				Integer sizeSeq = sizeSequenceByPath.get(resolverPath);
				Integer valueSeq = valueOrderByPath.get(resolverPath);
				if (sizeSeq != null && valueSeq != null && sizeSeq > valueSeq) {
					continue;
				}
			}
			builder.addInterfaceResolver(resolver);
		}

		for (PathResolver<GenericTypeResolver> resolver : analysisResult.getGenericTypeResolvers()) {
			builder.addGenericTypeResolver(resolver);
		}

		containerSizeResolverFactory.addTypedContainerSizeResolvers(builder, typedContainerSizes);

		if (isFixed) {
			builder.defaultContainerSizeResolver(
				containerSizeResolverFactory.createFixedContainerSizeResolver(options)
			);
		}

		return builder.build();
	}

	private Map<JvmType, Map<String, ArbitraryContainerInfo>> inferAndMergeTypedContainerSizes(
		Map<JvmType, Map<String, @Nullable Object>> typedValues,
		Map<JvmType, Map<String, ArbitraryContainerInfo>> explicitTypedContainerSizes
	) {
		Map<JvmType, Map<String, ArbitraryContainerInfo>> merged = new HashMap<>(explicitTypedContainerSizes);

		for (Map.Entry<JvmType, Map<String, @Nullable Object>> typeEntry : typedValues.entrySet()) {
			JvmType ownerType = typeEntry.getKey();
			for (Map.Entry<String, @Nullable Object> fieldEntry : typeEntry.getValue().entrySet()) {
				String fieldName = fieldEntry.getKey();
				Object value = fieldEntry.getValue();

				OptionalInt containerSize = CONTAINER_DETECTOR.getContainerSize(value);
				if (!containerSize.isPresent()) {
					continue;
				}

				int size = containerSize.getAsInt();
				merged.computeIfAbsent(ownerType, k -> new HashMap<>())
					.putIfAbsent(fieldName, new ArbitraryContainerInfo(size, size));
			}
		}
		return merged;
	}

	private JvmNodeCandidateTree getOrBuildCandidateTree(
		JvmType resolvedRootType,
		JvmNodeContext context,
		@Nullable FixtureMonkeyOptions options,
		boolean hasCustomConfigurers
	) {
		if (hasCustomConfigurers) {
			return buildCandidateTree(resolvedRootType, context, new JvmNodeCandidateTreeContext());
		}

		ContextCacheKey cacheKey = new ContextCacheKey(resolvedRootType, options);
		return candidateTreeCache.computeIfAbsent(cacheKey, key ->
			buildCandidateTree(resolvedRootType, context, treeContext)
		);
	}

	private JvmNodeCandidateTree buildCandidateTree(
		JvmType type,
		JvmNodeContext context,
		JvmNodeCandidateTreeContext candidateTreeContext
	) {
		return new JvmNodeCandidateTree.Builder(type, context)
			.withTreeContext(candidateTreeContext)
			.withPreBuildResolvedTypes(true)
			.build();
	}

	private JvmNodeContext getOrBuildNodeContext(
		JvmType rootType,
		@Nullable FixtureMonkeyOptions options,
		Map<Class<?>, List<Property>> propertyConfigurers,
		Map<Class<?>, ArbitraryIntrospector> introspectorsByType
	) {
		if (!propertyConfigurers.isEmpty() || !introspectorsByType.isEmpty()) {
			return buildNodeContext(rootType, options, propertyConfigurers, introspectorsByType);
		}

		ContextCacheKey cacheKey = new ContextCacheKey(rootType, options);
		return nodeContextCache.computeIfAbsent(cacheKey, key ->
			buildNodeContext(rootType, options, propertyConfigurers, introspectorsByType)
		);
	}

	private JvmNodeContext buildNodeContext(
		JvmType rootType,
		@Nullable FixtureMonkeyOptions options,
		Map<Class<?>, List<Property>> propertyConfigurers,
		Map<Class<?>, ArbitraryIntrospector> introspectorsByType
	) {
		ContainerSizeResolver containerSizeResolver = containerSizeResolverFactory.createContainerSizeResolver(options);
		List<JvmNodePromoter> allPromoters = createNodePromoters();

		JavaNodeContext.Builder builder = JavaNodeContext.builder()
			.seedState(seedState)
			.nodePromoters(Collections.singletonList(new JavaDefaultNodePromoter(allPromoters)))
			.containerSizeResolver(containerSizeResolver);

		if (options != null) {
			ArbitraryGenerator defaultGenerator = options.getDefaultArbitraryGenerator();
			Property rootProperty = JvmNodePropertyFactory.fromType(rootType);
			PropertyGenerator rootPropertyGenerator = defaultGenerator.getRequiredPropertyGenerator(rootProperty);

			// Build the name resolver from options (e.g., @JsonProperty support via JacksonPropertyNameResolver).
			// Always non-null: type-specific resolvers may exist even when the default is IDENTITY.
			NameResolvingNodeCandidateGenerator.ChildNameResolver nameResolver = createNodeNameResolver(options);

			if (rootPropertyGenerator != null) {
				// Create a per-type delegating PropertyGenerator instead of binding to the root type.
				// This ensures each type gets the correct PropertyGenerator from the introspector chain
				// (e.g., PriorityConstructorArbitraryIntrospector creates per-type ConstructorArbitraryIntrospectors).
				PropertyGenerator fallbackPropertyGenerator = options.getDefaultPropertyGenerator();

				PropertyGenerator basePropertyGenerator = property -> {
					PropertyGenerator gen = defaultGenerator.getRequiredPropertyGenerator(property);
					if (gen != null) {
						List<Property> children = gen.generateChildProperties(property);
						if (children != null) {
							return children;
						}
					}
					return fallbackPropertyGenerator.generateChildProperties(property);
				};
				List<MatcherOperator<PropertyGenerator>> propertyGenerators = options.getPropertyGenerators();

				PropertyGenerator compositeGenerator = createCompositePropertyGenerator(
					basePropertyGenerator,
					propertyConfigurers,
					introspectorsByType,
					propertyGenerators
				);
				JvmNodeCandidateGenerator candidateGenerator = new PropertyGeneratorNodeCandidateGenerator(
					compositeGenerator
				);
				candidateGenerator = new NameResolvingNodeCandidateGenerator(candidateGenerator, nameResolver);
				if (candidateGeneratorWrapper != null) {
					// Wrap with platform-specific isSupported check
					// (e.g., KotlinNodeCandidateGenerator that only supports Kotlin types).
					// With first-match-wins in buildTree, the wrapped generator handles supported types.
					builder.addCustomGenerator(candidateGeneratorWrapper.apply(candidateGenerator));
					// Also set as objectPropertyGenerator for non-Kotlin types (e.g., Java's File class)
					// that need the introspector's requiredPropertyGenerator.
					builder.objectPropertyGenerator(candidateGenerator);
				} else {
					builder.objectPropertyGenerator(candidateGenerator);
				}

				// Exclude types from leaf treatment if any extra generator recognizes them.
				// This allows Java standard types (e.g., java.io.File, java.sql.Timestamp) to be expanded
				// when using constructor-based introspectors with a requiredPropertyGenerator.
				builder.leafTypeExclusion(jvmType -> {
					Class<?> rawType = jvmType.getRawType();
					if (rawType.isPrimitive() || rawType.isArray() || rawType.isEnum()) {
						return false;
					}

					if (propertyConfigurers.containsKey(rawType) || introspectorsByType.containsKey(rawType)) {
						return true;
					}

					if (!propertyGenerators.isEmpty()) {
						Property property = JvmNodePropertyFactory.fromType(jvmType);
						for (MatcherOperator<PropertyGenerator> op : propertyGenerators) {
							if (op.match(property)) {
								return true;
							}
						}
					}

					// Check if the global objectIntrospector (via basePropertyGenerator)
					// produces children for this type. This handles cases like
					// PriorityConstructorArbitraryIntrospector expanding java.sql.Timestamp
					// via its Timestamp(long) constructor.
					try {
						Property property = JvmNodePropertyFactory.fromType(jvmType);
						List<Property> children = basePropertyGenerator.generateChildProperties(property);
						if (children != null && !children.isEmpty()) {
							return true;
						}
					} catch (Exception e) {
						// Keep as leaf if generator fails (e.g., inaccessible constructor)
					}

					return false;
				});
			} else {
				JvmNodeCandidateGenerator candidateGenerator = new NameResolvingNodeCandidateGenerator(
					new JavaFieldNodeCandidateGenerator(),
					nameResolver
				);
				if (candidateGeneratorWrapper != null) {
					builder.addCustomGenerator(candidateGeneratorWrapper.apply(candidateGenerator));
					builder.objectPropertyGenerator(candidateGenerator);
				} else {
					builder.objectPropertyGenerator(candidateGenerator);
				}
			}

			builder.interfaceResolver(createGlobalInterfaceResolver(options));

			builder.maxDepth(options.getMaxRecursionDepth());

			// Convert ContainerPropertyGenerators to a single fallback JvmContainerNodeGenerator.
			// This generator is added as a custom generator but only matches types
			// not already handled by the default generators (Array, Collection, Map, etc.).
			List<MatcherOperator<ContainerPropertyGenerator>> containerPropertyGenerators =
				options.getContainerPropertyGenerators();
			if (!containerPropertyGenerators.isEmpty()) {
				builder.addContainerNodeGenerator(
					new ContainerPropertyGeneratorNodeGenerator(containerPropertyGenerators)
				);
			}

			// Marker types used by dedicated introspectors in DEFAULT_ARBITRARY_INTROSPECTORS.
			// In the non-adapter path, these are handled by short-circuit introspectors
			// that produce values directly without child property generation.
			builder.addLeafTypeResolver(jvmType -> {
				Class<?> rt = jvmType.getRawType();
				return (rt == Types.GeneratingWildcardType.class
					|| rt == Types.UnidentifiableType.class
					|| rt == WildcardRawType.class);
			});

			for (LeafTypeResolver leafTypeResolver : additionalLeafTypeResolvers) {
				builder.addLeafTypeResolver(leafTypeResolver);
			}
		}

		return builder.build();
	}

	/**
	 * Creates a ChildNameResolver that resolves property names using FixtureMonkeyOptions.
	 * <p>
	 * To give the resolver access to field-level information (name + annotations like {@code @JsonProperty}),
	 * a {@link FieldProperty} is reflectively created from the parent class and candidate name.
	 * If the field is not found (e.g., constructor-only parameters), falls back to
	 * {@link JvmNodePropertyFactory#fromType(JvmType)} which provides annotations but no name.
	 *
	 * @return the child name resolver
	 */
	private static NameResolvingNodeCandidateGenerator.ChildNameResolver createNodeNameResolver(
		FixtureMonkeyOptions options
	) {
		return (parentType, childType, candidateName) -> {
			Property parentProperty = JvmNodePropertyFactory.fromType(parentType);
			PropertyNameResolver resolver = options.getPropertyNameResolver(parentProperty);

			Property childProperty = null;
			if (candidateName != null) {
				java.lang.reflect.Field field = TypeCache.getFieldsByName(parentType.getRawType()).get(candidateName);
				if (field != null) {
					childProperty = new FieldProperty(field);
				}
			}
			if (childProperty == null) {
				childProperty = JvmNodePropertyFactory.fromType(childType);
			}
			return resolver.resolve(childProperty);
		};
	}

	/**
	 * Creates the combined list of node promoters.
	 * Additional promoters (e.g., KotlinNodePromoter) are checked first to allow language-specific handling.
	 */
	private List<JvmNodePromoter> createNodePromoters() {
		List<JvmNodePromoter> basePromoters = Arrays.asList(
			new JavaMapNodePromoter(),
			new JavaMapEntryNodePromoter(),
			new AbstractTypeNodePromoter(),
			new JavaObjectNodePromoter()
		);

		if (additionalPromoters.isEmpty()) {
			return basePromoters;
		}

		List<JvmNodePromoter> allPromoters = new ArrayList<>(additionalPromoters.size() + basePromoters.size());
		allPromoters.addAll(additionalPromoters);
		allPromoters.addAll(basePromoters);
		return allPromoters;
	}

	/**
	 * Creates a composite PropertyGenerator that checks configurations in order:
	 * 1. propertyConfigurers (explicit property list from instantiate())
	 * 2. introspectorsByType (introspector's getRequiredPropertyGenerator())
	 * 3. propertyGenerators (from pushExactTypePropertyGenerator())
	 * 4. baseGenerator (default behavior)
	 */
	private PropertyGenerator createCompositePropertyGenerator(
		PropertyGenerator baseGenerator,
		Map<Class<?>, List<Property>> propertyConfigurers,
		Map<Class<?>, ArbitraryIntrospector> introspectorsByType,
		List<MatcherOperator<PropertyGenerator>> propertyGenerators
	) {
		if (propertyConfigurers.isEmpty() && introspectorsByType.isEmpty() && propertyGenerators.isEmpty()) {
			return baseGenerator;
		}

		return property -> {
			Class<?> actualType = Types.getActualType(property.getType());

			List<Property> configuredProperties = propertyConfigurers.get(actualType);
			if (configuredProperties != null) {
				return configuredProperties;
			}

			ArbitraryIntrospector introspector = introspectorsByType.get(actualType);
			if (introspector != null) {
				PropertyGenerator introspectorGenerator = introspector.getRequiredPropertyGenerator(property);
				if (introspectorGenerator != null) {
					List<Property> introspectorProperties = introspectorGenerator.generateChildProperties(property);
					if (introspectorProperties != null) {
						return introspectorProperties;
					}
				}
			}

			if (!propertyGenerators.isEmpty()) {
				for (MatcherOperator<PropertyGenerator> matcherOperator : propertyGenerators) {
					if (matcherOperator.match(property)) {
						List<Property> matchedProperties = matcherOperator
							.getOperator()
							.generateChildProperties(property);
						if (matchedProperties != null) {
							return matchedProperties;
						}
					}
				}
			}

			return baseGenerator.generateChildProperties(property);
		};
	}

	private InterfaceResolver createGlobalInterfaceResolver(FixtureMonkeyOptions options) {
		return new InterfaceResolver() {
			@Override
			public @Nullable JvmType resolve(JvmType type) {
				List<JvmType> candidates = resolveAll(type);
				if (candidates.isEmpty()) {
					return null;
				}
				Random typeRandom = seedState.snapshot().randomFor(type.hashCode());
				return candidates.get(typeRandom.nextInt(candidates.size()));
			}

			@Override
			public List<JvmType> resolveAll(JvmType type) {
				Class<?> rawType = type.getRawType();
				if (!Modifier.isInterface(rawType.getModifiers()) && !Modifier.isAbstract(rawType.getModifiers())) {
					return Collections.emptyList();
				}

				Property property = JvmNodePropertyFactory.fromType(type);
				@SuppressWarnings("deprecation")
				CandidateConcretePropertyResolver resolver = options.getCandidateConcretePropertyResolver(property);

				if (resolver == null) {
					return Collections.emptyList();
				}

				List<Property> candidates = resolver.resolve(property);
				if (candidates == null || candidates.isEmpty()) {
					return Collections.emptyList();
				}

				return candidates
					.stream()
					.map(p -> Types.toJvmType(p.getAnnotatedType(), p.getAnnotations()))
					.collect(Collectors.toList());
			}
		};
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>JvmNodeCandidateTree is cached, but JvmNodeTree is created fresh
	 * each time to ensure container sizes vary.</p>
	 */
	@Override
	public @Nullable JvmNodeTree createConcreteNodeTree(JvmType concreteType, @Nullable FixtureMonkeyOptions options) {
		if (options == null) {
			return null;
		}

		ContextCacheKey cacheKey = new ContextCacheKey(concreteType, options);

		JvmNodeContext context = getOrBuildNodeContext(
			concreteType,
			options,
			Collections.emptyMap(),
			Collections.emptyMap()
		);

		JvmNodeCandidateTree candidateTree = concreteTypeCandidateTreeCache.computeIfAbsent(cacheKey, key ->
			buildCandidateTree(concreteType, context, treeContext)
		);

		PathResolverContext resolverContext = PathResolverContext.builder().build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(
			context,
			treeContext,
			resolverContext,
			null,
			subtreeContext
		);

		return transformer.transform(candidateTree);
	}

	@Override
	public @Nullable JvmNodeTree createAnonymousNodeTree(
		JvmType interfaceType,
		@Nullable FixtureMonkeyOptions options
	) {
		if (options == null) {
			return null;
		}

		JavaNodeContext baseContext = (JavaNodeContext)getOrBuildNodeContext(
			interfaceType,
			options,
			Collections.emptyMap(),
			Collections.emptyMap()
		);

		JavaNodeContext anonymousContext = baseContext.withAdditionalGenerator(
			new InterfaceMethodNodeCandidateGenerator()
		);

		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(interfaceType, anonymousContext)
			.withTreeContext(treeContext)
			.withPreBuildResolvedTypes(true)
			.withSkipAbstractLeafCheck(true)
			.build();

		PathResolverContext resolverContext = currentResolverContext.get();
		if (resolverContext == null) {
			resolverContext = PathResolverContext.builder().build();
		}

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(
			anonymousContext,
			treeContext,
			resolverContext,
			null,
			subtreeContext
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
		nodeContextCache.clear();
		candidateTreeCache.clear();
		concreteTypeCandidateTreeCache.clear();
		subtreeContext.clear();
		nodeMetadataCache.clear();
	}

	@Override
	public ConcurrentHashMap<?, ?> getNodeMetadataCache() {
		return nodeMetadataCache;
	}

	/**
	 * Cache key for JvmNodeCandidateTree and empty manipulator results.
	 * Uses options identity hash because for the same FixtureMonkey instance,
	 * options is always the same object reference.
	 */
	private static final class ContextCacheKey {

		private final JvmType type;
		private final int optionsIdentity;
		private final int hashCode;

		ContextCacheKey(JvmType type, @Nullable FixtureMonkeyOptions options) {
			this.type = type;
			this.optionsIdentity = options != null ? System.identityHashCode(options) : 0;
			this.hashCode = computeHashCode();
		}

		private int computeHashCode() {
			int result = type.hashCode();
			result = 31 * result + optionsIdentity;
			return result;
		}

		@Override
		public boolean equals(@Nullable Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			ContextCacheKey that = (ContextCacheKey)obj;
			return optionsIdentity == that.optionsIdentity && Objects.equals(type, that.type);
		}

		@Override
		public int hashCode() {
			return hashCode;
		}
	}
}
