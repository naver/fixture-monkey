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

package com.navercorp.fixturemonkey.resolver;

import static com.navercorp.fixturemonkey.api.property.DefaultPropertyGenerator.FIELD_PROPERTY_GENERATOR;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.adapter.NodeTreeAdapter;
import com.navercorp.fixturemonkey.adapter.analysis.AdaptationResult;
import com.navercorp.fixturemonkey.adapter.analysis.AnalysisResult;
import com.navercorp.fixturemonkey.adapter.analysis.TypedValueExtractor;
import com.navercorp.fixturemonkey.adapter.converter.PredicatePathConverter;
import com.navercorp.fixturemonkey.adapter.projection.AssembleContext;
import com.navercorp.fixturemonkey.adapter.projection.ValueProjection;
import com.navercorp.fixturemonkey.adapter.tracing.AdapterTraceBuilder;
import com.navercorp.fixturemonkey.adapter.tracing.AdapterTracer;
import com.navercorp.fixturemonkey.adapter.tracing.TraceContext;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.context.MonkeyContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.DefaultTreeMatcherMetadata;
import com.navercorp.fixturemonkey.api.matcher.ExactTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.matcher.PriorityMatcherOperator;
import com.navercorp.fixturemonkey.api.matcher.TreeMatcherOperator;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.TreeRootProperty;
import com.navercorp.fixturemonkey.builder.ArbitraryBuilderContext;
import com.navercorp.fixturemonkey.builder.ArbitraryBuilderContextProvider;
import com.navercorp.fixturemonkey.customizer.ApplyNodeCountManipulator;
import com.navercorp.fixturemonkey.customizer.ArbitraryManipulator;
import com.navercorp.fixturemonkey.customizer.ContainerInfoManipulator;
import com.navercorp.fixturemonkey.customizer.ManipulatorSet;
import com.navercorp.fixturemonkey.customizer.MonkeyManipulatorFactory;
import com.navercorp.fixturemonkey.customizer.NodeManipulator;
import com.navercorp.fixturemonkey.customizer.NodeSetDecomposedValueManipulator;
import com.navercorp.fixturemonkey.customizer.NodeSetLazyManipulator;
import com.navercorp.fixturemonkey.tree.NextNodePredicate;
import com.navercorp.fixturemonkey.tree.ObjectNode;
import com.navercorp.fixturemonkey.tree.ObjectTree;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.Types;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ArbitraryResolver {

	private final ManipulatorOptimizer manipulatorOptimizer;
	private final MonkeyManipulatorFactory monkeyManipulatorFactory;
	private final MonkeyContext monkeyContext;

	private final @Nullable NodeTreeAdapter nodeTreeAdapter;

	private final AdapterTracer adapterTracer;
	private final Map<Class<?>, Set<Property>> inferredPropertiesCache;

	public ArbitraryResolver(
		ManipulatorOptimizer manipulatorOptimizer,
		MonkeyManipulatorFactory monkeyManipulatorFactory,
		MonkeyContext monkeyContext
	) {
		this(manipulatorOptimizer, monkeyManipulatorFactory, monkeyContext, null, AdapterTracer.noOp(), null);
	}

	public ArbitraryResolver(
		ManipulatorOptimizer manipulatorOptimizer,
		MonkeyManipulatorFactory monkeyManipulatorFactory,
		MonkeyContext monkeyContext,
		@Nullable NodeTreeAdapter nodeTreeAdapter
	) {
		this(
			manipulatorOptimizer,
			monkeyManipulatorFactory,
			monkeyContext,
			nodeTreeAdapter,
			AdapterTracer.noOp(),
			null
		);
	}

	public ArbitraryResolver(
		ManipulatorOptimizer manipulatorOptimizer,
		MonkeyManipulatorFactory monkeyManipulatorFactory,
		MonkeyContext monkeyContext,
		@Nullable NodeTreeAdapter nodeTreeAdapter,
		AdapterTracer adapterTracer
	) {
		this(manipulatorOptimizer, monkeyManipulatorFactory, monkeyContext, nodeTreeAdapter, adapterTracer, null);
	}

	public ArbitraryResolver(
		ManipulatorOptimizer manipulatorOptimizer,
		MonkeyManipulatorFactory monkeyManipulatorFactory,
		MonkeyContext monkeyContext,
		@Nullable NodeTreeAdapter nodeTreeAdapter,
		AdapterTracer adapterTracer,
		@Nullable Map<Class<?>, Set<Property>> inferredPropertiesCache
	) {
		this.manipulatorOptimizer = manipulatorOptimizer;
		this.monkeyManipulatorFactory = monkeyManipulatorFactory;
		this.monkeyContext = monkeyContext;
		this.nodeTreeAdapter = nodeTreeAdapter;
		this.adapterTracer = adapterTracer;
		this.inferredPropertiesCache =
			inferredPropertiesCache != null ? inferredPropertiesCache : new ConcurrentHashMap<>();
	}

	@SuppressWarnings("unchecked")
	public CombinableArbitrary<?> resolve(
		TreeRootProperty rootProperty,
		ArbitraryBuilderContext activeContext,
		List<PriorityMatcherOperator<ArbitraryBuilderContext>> standbyContexts
	) {
		FixtureMonkeyOptions fixtureMonkeyOptions = monkeyContext.getFixtureMonkeyOptions();

		// Early check for adapter path - skip ObjectTree creation entirely
		if (nodeTreeAdapter != null) {
			return resolveWithAdapter(rootProperty, activeContext, standbyContexts, fixtureMonkeyOptions);
		}

		List<ArbitraryManipulator> activeManipulators = activeContext.getManipulators();

		return new ResolvedCombinableArbitrary<>(
			rootProperty,
			() -> {
				// TODO: Fragmented registered
				Set<Property> inferredProperties = inferPossibleProperties(rootProperty, new CycleDetector());

				Map<Class<?>, List<Property>> registeredPropertyConfigurer = monkeyContext
					.getRegisteredArbitraryBuilders()
					.stream()
					.filter(it -> inferredProperties.stream().anyMatch(it::match))
					.map(it -> ((ArbitraryBuilderContextProvider)it.getOperator()).getActiveContext())
					.map(ArbitraryBuilderContext::getPropertyConfigurers)
					.findFirst() // registered are stored in reverse order, so we take the first one
					.orElse(Collections.emptyMap());

				Map<Class<?>, ArbitraryIntrospector> registeredIntrospectors = monkeyContext
					.getRegisteredArbitraryBuilders()
					.stream()
					.filter(it -> inferredProperties.stream().anyMatch(it::match))
					.map(it -> ((ArbitraryBuilderContextProvider)it.getOperator()).getActiveContext())
					.map(ArbitraryBuilderContext::getArbitraryIntrospectorsByType)
					.findFirst() // registered are stored in reverse order, so we take the first one
					.orElse(Collections.emptyMap());

				ObjectTree objectTree = new ObjectTree(
					rootProperty,
					activeContext.newGenerateFixtureContext(registeredIntrospectors),
					activeContext.newTraverseContext(rootProperty, registeredPropertyConfigurer)
				);

				fixtureMonkeyOptions
					.getBuilderContextInitializers()
					.stream()
					.filter(it -> it.match(new DefaultTreeMatcherMetadata(objectTree.getMetadata().getAnnotations())))
					.findFirst()
					.map(TreeMatcherOperator::getOperator)
					.ifPresent(it -> activeContext.setOptionValidOnly(it.isValidOnly()));

				return objectTree;
			},
			objectTree -> {
				Map<Property, List<ObjectNode>> rootNodesByProperty = Collections.singletonMap(
					rootProperty,
					Collections.singletonList(objectTree.getMetadata().getRootNode())
				);

				List<ArbitraryManipulator> registeredRootManipulators =
					monkeyManipulatorFactory.newRegisteredArbitraryManipulators(standbyContexts, rootNodesByProperty);

				List<PriorityMatcherOperator<ArbitraryBuilderContext>> registeredPropertyArbitraryBuilderContexts =
					monkeyContext
						.getRegisteredArbitraryBuilders()
						.stream()
						.map(it ->
							new PriorityMatcherOperator<>(
								it.getMatcher(),
								((ArbitraryBuilderContextProvider)it.getOperator()).getActiveContext(),
								it.getPriority()
							)
						)
						.collect(Collectors.toList());

				List<ArbitraryManipulator> registeredPropertyManipulators =
					monkeyManipulatorFactory.newRegisteredArbitraryManipulators(
						registeredPropertyArbitraryBuilderContexts,
						objectTree.getMetadata().getNodesByProperty()
					);

				List<ArbitraryManipulator> registeredManipulators = new ArrayList<>();
				registeredManipulators.addAll(registeredRootManipulators);
				registeredManipulators.addAll(registeredPropertyManipulators);

				// Track the count of registered manipulators for filtering later
				int registeredManipulatorCount = registeredManipulators.size();

				List<ArbitraryManipulator> joinedManipulators = Stream.concat(
					registeredManipulators.stream(),
					activeManipulators.stream()
				).collect(Collectors.toList());

				// Adapter integration point - analyze manipulators and build JvmNodeTree
				if (nodeTreeAdapter != null) {
					// Start measuring preparation time
					long prepStartTime = System.nanoTime();

					// Check if there's a root lazy manipulator in active manipulators
					// If so, skip registered property container info because the lazy callback will set its own sizes
					boolean hasRootLazyManipulator = activeManipulators
						.stream()
						.anyMatch(m -> {
							NodeManipulator nm = m.getNodeManipulator();
							// Unwrap ApplyNodeCountManipulator if present
							if (nm instanceof ApplyNodeCountManipulator) {
								nm = ((ApplyNodeCountManipulator)nm).getNodeManipulator();
							}
							if (nm instanceof NodeSetLazyManipulator) {
								List<NextNodePredicate> predicates = PredicatePathConverter.extractPredicates(
									m.getNodeResolver()
								);
								String path = PredicatePathConverter.toExpression(predicates);
								return "$".equals(path);
							}
							return false;
						});

					// Also check if registered root builders have root lazy manipulators
					// If so, their container info should not override user's container info
					boolean registeredHasRootLazy = standbyContexts
						.stream()
						.flatMap(ctx -> ctx.getOperator().getManipulators().stream())
						.anyMatch(m -> {
							NodeManipulator nm = m.getNodeManipulator();
							if (nm instanceof ApplyNodeCountManipulator) {
								nm = ((ApplyNodeCountManipulator)nm).getNodeManipulator();
							}
							if (nm instanceof NodeSetLazyManipulator) {
								List<NextNodePredicate> predicates = PredicatePathConverter.extractPredicates(
									m.getNodeResolver()
								);
								String path = PredicatePathConverter.toExpression(predicates);
								return "$".equals(path);
							}
							return false;
						});

					// Get active container info first (needed for filtering)
					List<ContainerInfoManipulator> activeContainerInfoManipulatorsPreview = activeContext
						.getContainerInfoManipulators()
						.stream()
						.filter(it -> it instanceof ContainerInfoManipulator)
						.map(it -> (ContainerInfoManipulator)it)
						.collect(Collectors.toList());

					Set<String> activePathsPreview = activeContainerInfoManipulatorsPreview
						.stream()
						.map(m -> PredicatePathConverter.toExpression(m.getNextNodePredicates()))
						.collect(Collectors.toSet());

					// 1. Registered root builders에서 ContainerInfoManipulator 수집
					List<ContainerInfoManipulator> registeredRootContainerInfoManipulators = standbyContexts
						.stream()
						.map(PriorityMatcherOperator::getOperator)
						.flatMap(ctx -> ctx.getContainerInfoManipulators().stream())
						.filter(it -> it instanceof ContainerInfoManipulator)
						.map(it -> (ContainerInfoManipulator)it)
						.collect(Collectors.toList());

					// 2. Registered property builders에서 ContainerInfoManipulator 수집
					List<ContainerInfoManipulator> registeredPropertyContainerInfoManipulators =
						registeredPropertyArbitraryBuilderContexts
							.stream()
							.filter(it ->
								objectTree.getMetadata().getNodesByProperty().keySet().stream().anyMatch(it::match)
							)
							.map(PriorityMatcherOperator::getOperator)
							.flatMap(ctx -> ctx.getContainerInfoManipulators().stream())
							.filter(it -> it instanceof ContainerInfoManipulator)
							.map(it -> (ContainerInfoManipulator)it)
							.collect(Collectors.toList());

					// 3. Active context에서 ContainerInfoManipulator 수집
					List<ContainerInfoManipulator> activeContainerInfoManipulators = activeContext
						.getContainerInfoManipulators()
						.stream()
						.filter(it -> it instanceof ContainerInfoManipulator)
						.map(it -> (ContainerInfoManipulator)it)
						.collect(Collectors.toList());

					// 4. 모든 ContainerInfoManipulator 병합 (registered -> active 순서)
					// Active takes precedence: remove registered entries with same path as active
					Set<String> activePaths = activeContainerInfoManipulators
						.stream()
						.map(m -> PredicatePathConverter.toExpression(m.getNextNodePredicates()))
						.collect(Collectors.toSet());

					List<ContainerInfoManipulator> filteredRegisteredRoot = registeredRootContainerInfoManipulators
						.stream()
						.filter(m ->
							!activePaths.contains(PredicatePathConverter.toExpression(m.getNextNodePredicates()))
						)
						.collect(Collectors.toList());

					List<ContainerInfoManipulator> filteredRegisteredProperty =
						registeredPropertyContainerInfoManipulators
							.stream()
							.filter(m ->
								!activePaths.contains(PredicatePathConverter.toExpression(m.getNextNodePredicates()))
							)
							.collect(Collectors.toList());

					List<ContainerInfoManipulator> containerInfoManipulators = new ArrayList<>();
					containerInfoManipulators.addAll(activeContainerInfoManipulators);
					containerInfoManipulators.addAll(filteredRegisteredRoot);
					containerInfoManipulators.addAll(filteredRegisteredProperty);

					// Compute relevant types to filter register entries
					// Only collect register operations for types that exist in the sample target's type tree
					Set<Class<?>> relevantTypes = collectRelevantTypes(rootProperty);

					// 5. 타입 기반 컨테이너 크기 수집 (registered property builders에서)
					// Skip paths that are already set by active container info manipulators
					Map<JvmType, Map<String, ArbitraryContainerInfo>> typedContainerSizes = new HashMap<>();
					for (PriorityMatcherOperator<
						ArbitraryBuilderContext
						> registered : registeredPropertyArbitraryBuilderContexts) {
						Matcher matcher = registered.getMatcher();

						Class<?> targetType = null;
						if (matcher instanceof ExactTypeMatcher) {
							targetType = ((ExactTypeMatcher)matcher).getType();
						} else if (matcher instanceof AssignableTypeMatcher) {
							targetType = ((AssignableTypeMatcher)matcher).getAnchorType();
						}

						if (targetType == null || !isRelevantType(targetType, matcher, relevantTypes)) {
							continue;
						}

						JvmType jvmType = resolveJvmTypeForMatcher(targetType, matcher, relevantTypes);

						for (Object cim : registered.getOperator().getContainerInfoManipulators()) {
							if (!(cim instanceof ContainerInfoManipulator)) {
								continue;
							}
							ContainerInfoManipulator manipulator = (ContainerInfoManipulator)cim;

							// Extract field name from path (e.g., "$.values" -> "values")
							String fieldPath = PredicatePathConverter.toExpression(manipulator.getNextNodePredicates());

							// Skip if active context has a container info for this path
							// Active takes precedence over registered
							if (activePaths.contains(fieldPath)) {
								continue;
							}

							String fieldName = fieldPath.startsWith("$.") ? fieldPath.substring(2) : fieldPath;

							// Get container info (preserving min/max range)
							ArbitraryContainerInfo containerInfo = manipulator.getContainerInfo();

							// Merge with existing info if present (for minSize/maxSize called separately)
							Map<String, ArbitraryContainerInfo> fieldSizes = typedContainerSizes.computeIfAbsent(
								jvmType,
								k -> new HashMap<>()
							);
							ArbitraryContainerInfo existing = fieldSizes.get(fieldName);
							if (existing != null) {
								// Merge: take max of mins and min of maxes to find valid range
								int mergedMin = Math.max(
									existing.getElementMinSize(),
									containerInfo.getElementMinSize()
								);
								int mergedMax = Math.min(
									existing.getElementMaxSize(),
									containerInfo.getElementMaxSize()
								);
								// Ensure valid range
								if (mergedMin > mergedMax) {
									mergedMax = mergedMin;
								}
								containerInfo = new ArbitraryContainerInfo(mergedMin, mergedMax);
							}
							fieldSizes.put(fieldName, containerInfo);
						}
					}

					// 6. 타입 기반 set 값 수집 (registered property builders에서)
					// Sort by priority (lower number = higher priority)
					List<PriorityMatcherOperator<ArbitraryBuilderContext>> sortedByPriority = new ArrayList<>(
						registeredPropertyArbitraryBuilderContexts
					);
					// Sort by descending priority (higher number = lower priority processed first).
					// populateFromNodeManipulator uses put() (last-write-wins), so higher-priority
					// registers (lower number) are processed last and override lower-priority ones.
					sortedByPriority.sort(
						Comparator.comparingInt(
							PriorityMatcherOperator<ArbitraryBuilderContext>::getPriority
						).reversed()
					);

					Map<JvmType, Map<String, @Nullable Object>> typedValues = new HashMap<>();
					for (PriorityMatcherOperator<ArbitraryBuilderContext> registered : sortedByPriority) {
						Matcher matcher = registered.getMatcher();

						Class<?> targetType = null;
						if (matcher instanceof ExactTypeMatcher) {
							targetType = ((ExactTypeMatcher)matcher).getType();
						} else if (matcher instanceof AssignableTypeMatcher) {
							targetType = ((AssignableTypeMatcher)matcher).getAnchorType();
						}

						if (targetType == null || !isRelevantType(targetType, matcher, relevantTypes)) {
							continue;
						}

						// Skip if this registered builder has a root lazy manipulator (from thenApply)
						// and user has active container info - the lazy would produce wrong container sizes
						boolean thisBuilderHasRootLazy = registered
							.getOperator()
							.getManipulators()
							.stream()
							.anyMatch(m -> {
								NodeManipulator nm = m.getNodeManipulator();
								if (nm instanceof ApplyNodeCountManipulator) {
									nm = ((ApplyNodeCountManipulator)nm).getNodeManipulator();
								}
								if (nm instanceof NodeSetLazyManipulator) {
									List<NextNodePredicate> predicates = PredicatePathConverter.extractPredicates(
										m.getNodeResolver()
									);
									String path = PredicatePathConverter.toExpression(predicates);
									return "$".equals(path);
								}
								return false;
							});

						if (thisBuilderHasRootLazy && !activePathsPreview.isEmpty()) {
							// Skip typed values from this builder - user's container sizes should win
							continue;
						}

						JvmType jvmType = resolveJvmTypeForMatcher(targetType, matcher, relevantTypes);
						List<ArbitraryManipulator> manips = registered.getOperator().getManipulators();
						for (ArbitraryManipulator manipulator : manips) {
							// Extract values from set() calls
							// Higher priority (lower number) wins - don't overwrite existing values
							TypedValueExtractor.extract(manipulator, jvmType, typedValues);
						}
					}

					// Filter out root decomposed value manipulators from REGISTERED builders only
					// when:
					// 1. Active has a root lazy manipulator (thenApply) - the lazy will produce correct result
					// 2. OR active has container info for paths - registered decomposed value may have wrong sizes
					// We do NOT filter active manipulators because thenApply creates decomposed values
					// that already respect the active container sizes.
					List<ArbitraryManipulator> filteredJoinedManipulators = joinedManipulators;
					boolean shouldFilterRegisteredDecomposed = hasRootLazyManipulator || !activePaths.isEmpty();
					if (shouldFilterRegisteredDecomposed) {
						final int regCount = registeredManipulatorCount;
						filteredJoinedManipulators = new ArrayList<>();
						for (int i = 0; i < joinedManipulators.size(); i++) {
							ArbitraryManipulator manipulator = joinedManipulators.get(i);
							boolean isFromRegistered = i < regCount;

							NodeManipulator nm = manipulator.getNodeManipulator();
							// Unwrap ApplyNodeCountManipulator if present
							if (nm instanceof ApplyNodeCountManipulator) {
								nm = ((ApplyNodeCountManipulator)nm).getNodeManipulator();
							}

							if (nm instanceof NodeSetDecomposedValueManipulator && isFromRegistered) {
								List<NextNodePredicate> predicates = PredicatePathConverter.extractPredicates(
									manipulator.getNodeResolver()
								);
								String path = PredicatePathConverter.toExpression(predicates);
								// Skip root decomposed value from registered if:
								// - Active has root lazy manipulator (will produce correct result)
								// - OR active has container info changes (decomposed value has wrong sizes)
								if ("$".equals(path)) {
									continue;
								}
							}
							filteredJoinedManipulators.add(manipulator);
						}
					}

					// Collect registered property configurers (from registered builders)
					Map<Class<?>, List<Property>> registeredPropertyConfigurers =
						registeredPropertyArbitraryBuilderContexts
							.stream()
							.filter(it ->
								objectTree.getMetadata().getNodesByProperty().keySet().stream().anyMatch(it::match)
							)
							.map(PriorityMatcherOperator::getOperator)
							.map(ArbitraryBuilderContext::getPropertyConfigurers)
							.findFirst()
							.orElse(Collections.emptyMap());

					// Merge property configurers: active context takes precedence
					Map<Class<?>, List<Property>> mergedPropertyConfigurers = new HashMap<>(
						registeredPropertyConfigurers
					);
					mergedPropertyConfigurers.putAll(activeContext.getPropertyConfigurers());

					// Collect registered introspectors (from registered builders)
					Map<Class<?>, ArbitraryIntrospector> registeredIntrospectors =
						registeredPropertyArbitraryBuilderContexts
							.stream()
							.filter(it ->
								objectTree.getMetadata().getNodesByProperty().keySet().stream().anyMatch(it::match)
							)
							.map(PriorityMatcherOperator::getOperator)
							.map(ArbitraryBuilderContext::getArbitraryIntrospectorsByType)
							.findFirst()
							.orElse(Collections.emptyMap());

					// Merge introspectors: active context takes precedence
					Map<Class<?>, ArbitraryIntrospector> mergedIntrospectors = new HashMap<>(registeredIntrospectors);
					mergedIntrospectors.putAll(activeContext.getArbitraryIntrospectorsByType());

					ManipulatorSet manipulatorSet = new ManipulatorSet(
						filteredJoinedManipulators,
						containerInfoManipulators,
						typedContainerSizes,
						typedValues,
						mergedPropertyConfigurers,
						mergedIntrospectors,
						activeContext.isFixed()
					);
					JvmType rootJvmType = toJvmType(rootProperty);

					// Calculate preparation time
					long prepTimeNanos = System.nanoTime() - prepStartTime;

					// Create trace context early to capture resolution events
					TraceContext traceContext = adapterTracer.createTraceContext();

					// Measure total adapter time
					long adapterStartTime = System.nanoTime();

					AdaptationResult adaptationResult = nodeTreeAdapter.adapt(
						rootJvmType,
						manipulatorSet,
						fixtureMonkeyOptions,
						traceContext
					);

					AnalysisResult analysisResult = adaptationResult.getAnalysisResult();

					// Get immutable ValueProjection from adaptation result
					// Lazy values are now evaluated and decomposed in ManipulatorAnalyzer,
					// following the same flow as other values (no special handling needed here)
					ValueProjection values = adaptationResult.getValues();

					// Strict mode validation: check for paths that don't exist in the type structure
					if (analysisResult.isStrictMode()) {
						Set<String> invalidPaths = values.getUnresolvedNonWildcardPaths();
						if (!invalidPaths.isEmpty()) {
							throw new IllegalArgumentException(
								"No matching results for given NodeResolvers. " + "Invalid paths: " + invalidPaths
							);
						}
					}

					// Assemble using ValueProjection
					// Path resolution follows "more specific path wins" rule.
					// Only justPaths (from Values.just()) are treated as truly immutable.
					traceContext.setRootType(rootJvmType.getRawType().getName());

					Set<PathExpression> userContainerSizePaths = activePaths
						.stream()
						.map(PathExpression::of)
						.collect(Collectors.toSet());

					// Convert typedValues to TypeSelector-based PathExpression entries
					TypedValueExtractor.ConversionResult typedPathConversion =
						TypedValueExtractor.convertToPathExpressions(typedValues);

					AssembleContext assembleContext = AssembleContext.builder(monkeyContext)
						.rootProperty(rootProperty)
						.justPaths(new HashSet<>(analysisResult.getJustPaths()))
						.notNullPaths(analysisResult.getNotNullPaths())
						.filtersByPath(analysisResult.getFiltersByPath())
						.limitsByPath(new HashMap<>(analysisResult.getLimitsByPath()))
						.valueOrderByPath(analysisResult.getValueOrderByPath())
						.customizersByPath(analysisResult.getCustomizersByPath())
						.typedPathValues(typedPathConversion.values)
						.typedPathOrders(typedPathConversion.orders)
						.introspectorsByType(mergedIntrospectors)
						.traceContext(traceContext)
						.nodeTreeAdapter((NodeTreeAdapter)fixtureMonkeyOptions.getNodeTreeAdapter())
						.userContainerSizePaths(userContainerSizePaths)
						.build();

					// Measure assembly time
					long assemblyStartTime = System.nanoTime();
					CombinableArbitrary<Object> result = (CombinableArbitrary<Object>)values.assemble(assembleContext);
					long assemblyTimeNanos = System.nanoTime() - assemblyStartTime;

					// Calculate total adapter time
					long totalAdapterTimeNanos = System.nanoTime() - adapterStartTime;

					// Build and invoke trace with collected data
					AdapterTraceBuilder.buildAndInvoke(
						traceContext,
						filteredJoinedManipulators,
						containerInfoManipulators,
						analysisResult,
						adaptationResult,
						prepTimeNanos,
						assemblyTimeNanos,
						totalAdapterTimeNanos,
						typedValues,
						typedContainerSizes,
						relevantTypes,
						activeContext.isFixed(),
						adapterTracer
					);

					return result;
				}

				List<ArbitraryManipulator> optimizedManipulator = manipulatorOptimizer
					.optimize(joinedManipulators)
					.getManipulators();

				for (ArbitraryManipulator manipulator : optimizedManipulator) {
					manipulator.manipulate(objectTree);
				}
				return (CombinableArbitrary<Object>)objectTree.generate();
			},
			fixtureMonkeyOptions.getGenerateMaxTries(),
			fixtureMonkeyOptions.getDefaultArbitraryValidator(),
			activeContext::isValidOnly
		);
	}

	/**
	 * Optimized adapter resolution path that skips ObjectTree creation entirely.
	 * This is called when nodeTreeAdapter is enabled.
	 */
	@SuppressWarnings("unchecked")
	private CombinableArbitrary<?> resolveWithAdapter(
		TreeRootProperty rootProperty,
		ArbitraryBuilderContext activeContext,
		List<PriorityMatcherOperator<ArbitraryBuilderContext>> standbyContexts,
		FixtureMonkeyOptions fixtureMonkeyOptions
	) {
		List<ArbitraryManipulator> activeManipulators = activeContext.getManipulators();

		// Use AdapterCombinableArbitrary which doesn't require ObjectTree
		return new AdapterCombinableArbitrary<>(
			rootProperty,
			() ->
				generateWithAdapter(
					rootProperty,
					activeContext,
					activeManipulators,
					standbyContexts,
					fixtureMonkeyOptions
				),
			fixtureMonkeyOptions.getGenerateMaxTries(),
			fixtureMonkeyOptions.getDefaultArbitraryValidator(),
			activeContext::isValidOnly,
			() -> {
			} // onRetry - adapter path doesn't need special retry logic
		);
	}

	/**
	 * Generate a CombinableArbitrary using the adapter path without ObjectTree.
	 * This is the core adapter logic extracted from the original resolve method.
	 */
	@SuppressWarnings("unchecked")
	private CombinableArbitrary<Object> generateWithAdapter(
		TreeRootProperty rootProperty,
		ArbitraryBuilderContext activeContext,
		List<ArbitraryManipulator> activeManipulators,
		List<PriorityMatcherOperator<ArbitraryBuilderContext>> standbyContexts,
		FixtureMonkeyOptions fixtureMonkeyOptions
	) {
		// Start measuring preparation time
		long prepStartTime = System.nanoTime();

		boolean hasRegisteredBuilders = !monkeyContext.getRegisteredArbitraryBuilders().isEmpty();
		boolean hasStandbyContexts = !standbyContexts.isEmpty();

		// Fast path: when no registered builders and no standby contexts,
		// skip all register-related preparation (inferPossibleProperties, collectRelevantTypes,
		// typed values/container sizes collection, registered property configurers/introspectors)
		if (!hasRegisteredBuilders && !hasStandbyContexts) {
			return generateWithAdapterFastPath(
				rootProperty,
				activeContext,
				activeManipulators,
				fixtureMonkeyOptions,
				prepStartTime
			);
		}

		return generateWithAdapterFullPath(
			rootProperty,
			activeContext,
			activeManipulators,
			standbyContexts,
			fixtureMonkeyOptions,
			prepStartTime
		);
	}

	/**
	 * Fast path for adapter generation when no registered builders or standby contexts exist.
	 * Skips all register-related preparation for significantly better performance.
	 */
	@SuppressWarnings("unchecked")
	private CombinableArbitrary<Object> generateWithAdapterFastPath(
		TreeRootProperty rootProperty,
		ArbitraryBuilderContext activeContext,
		List<ArbitraryManipulator> activeManipulators,
		FixtureMonkeyOptions fixtureMonkeyOptions,
		long prepStartTime
	) {
		// Set validOnly from inferred property annotations (only if builderContextInitializers exist)
		if (!fixtureMonkeyOptions.getBuilderContextInitializers().isEmpty()) {
			Set<Property> inferredProperties = inferPossibleProperties(rootProperty, new CycleDetector());
			Set<Annotation> allAnnotations = inferredProperties
				.stream()
				.flatMap(p -> p.getAnnotations().stream())
				.collect(Collectors.toSet());
			fixtureMonkeyOptions
				.getBuilderContextInitializers()
				.stream()
				.filter(it -> it.match(new DefaultTreeMatcherMetadata(allAnnotations)))
				.findFirst()
				.map(TreeMatcherOperator::getOperator)
				.ifPresent(it -> activeContext.setOptionValidOnly(it.isValidOnly()));
		}

		// Active context container info only (no registered builders to merge)
		List<ContainerInfoManipulator> containerInfoManipulators = activeContext
			.getContainerInfoManipulators()
			.stream()
			.filter(it -> it instanceof ContainerInfoManipulator)
			.map(it -> (ContainerInfoManipulator)it)
			.collect(Collectors.toList());

		Set<String> activePaths = containerInfoManipulators
			.stream()
			.map(m -> PredicatePathConverter.toExpression(m.getNextNodePredicates()))
			.collect(Collectors.toSet());

		Map<Class<?>, ArbitraryIntrospector> introspectorsByType = activeContext.getArbitraryIntrospectorsByType();

		ManipulatorSet manipulatorSet = new ManipulatorSet(
			new ArrayList<>(activeManipulators),
			containerInfoManipulators,
			Collections.emptyMap(),
			Collections.emptyMap(),
			activeContext.getPropertyConfigurers(),
			introspectorsByType,
			activeContext.isFixed()
		);
		JvmType rootJvmType = toJvmType(rootProperty);

		long prepTimeNanos = System.nanoTime() - prepStartTime;

		return assembleAdapterResult(
			rootProperty,
			activeContext,
			activeManipulators,
			fixtureMonkeyOptions,
			manipulatorSet,
			rootJvmType,
			containerInfoManipulators,
			activePaths,
			Collections.emptyMap(),
			Collections.emptyMap(),
			Collections.emptySet(),
			introspectorsByType,
			prepTimeNanos
		);
	}

	/**
	 * Full path for adapter generation with registered builders and standby contexts.
	 */
	@SuppressWarnings("unchecked")
	private CombinableArbitrary<Object> generateWithAdapterFullPath(
		TreeRootProperty rootProperty,
		ArbitraryBuilderContext activeContext,
		List<ArbitraryManipulator> activeManipulators,
		List<PriorityMatcherOperator<ArbitraryBuilderContext>> standbyContexts,
		FixtureMonkeyOptions fixtureMonkeyOptions,
		long prepStartTime
	) {
		// Infer possible properties for type-based matching (without ObjectTree)
		Set<Property> inferredProperties = inferPossibleProperties(rootProperty, new CycleDetector());

		// Set validOnly from all inferred property annotations (matching non-adapter path behavior
		// where MetadataCollector.collect() gathers annotations from the entire tree)
		Set<Annotation> allAnnotations = inferredProperties
			.stream()
			.flatMap(p -> p.getAnnotations().stream())
			.collect(Collectors.toSet());
		fixtureMonkeyOptions
			.getBuilderContextInitializers()
			.stream()
			.filter(it -> it.match(new DefaultTreeMatcherMetadata(allAnnotations)))
			.findFirst()
			.map(TreeMatcherOperator::getOperator)
			.ifPresent(it -> activeContext.setOptionValidOnly(it.isValidOnly()));

		// Get registered property builders for type-based matching
		List<PriorityMatcherOperator<ArbitraryBuilderContext>> registeredPropertyArbitraryBuilderContexts =
			monkeyContext
				.getRegisteredArbitraryBuilders()
				.stream()
				.map(it ->
					new PriorityMatcherOperator<>(
						it.getMatcher(),
						((ArbitraryBuilderContextProvider)it.getOperator()).getActiveContext(),
						it.getPriority()
					)
				)
				.collect(Collectors.toList());

		// 1. Registered root builders에서 ContainerInfoManipulator 수집
		List<ContainerInfoManipulator> registeredRootContainerInfoManipulators = standbyContexts
			.stream()
			.map(PriorityMatcherOperator::getOperator)
			.flatMap(ctx -> ctx.getContainerInfoManipulators().stream())
			.filter(it -> it instanceof ContainerInfoManipulator)
			.map(it -> (ContainerInfoManipulator)it)
			.collect(Collectors.toList());

		// 2. Registered property builders에서 ContainerInfoManipulator 수집
		// Use inferred properties for matching instead of objectTree.getNodesByProperty()
		List<ContainerInfoManipulator> registeredPropertyContainerInfoManipulators =
			registeredPropertyArbitraryBuilderContexts
				.stream()
				.filter(it -> inferredProperties.stream().anyMatch(it::match))
				.map(PriorityMatcherOperator::getOperator)
				.flatMap(ctx -> ctx.getContainerInfoManipulators().stream())
				.filter(it -> it instanceof ContainerInfoManipulator)
				.map(it -> (ContainerInfoManipulator)it)
				.collect(Collectors.toList());

		// 3. Active context에서 ContainerInfoManipulator 수집
		List<ContainerInfoManipulator> activeContainerInfoManipulators = activeContext
			.getContainerInfoManipulators()
			.stream()
			.filter(it -> it instanceof ContainerInfoManipulator)
			.map(it -> (ContainerInfoManipulator)it)
			.collect(Collectors.toList());

		// 4. 모든 ContainerInfoManipulator 병합 (registered -> active 순서)
		Set<String> activePaths = activeContainerInfoManipulators
			.stream()
			.map(m -> PredicatePathConverter.toExpression(m.getNextNodePredicates()))
			.collect(Collectors.toSet());

		List<ContainerInfoManipulator> filteredRegisteredRoot = registeredRootContainerInfoManipulators
			.stream()
			.filter(m -> !activePaths.contains(PredicatePathConverter.toExpression(m.getNextNodePredicates())))
			.collect(Collectors.toList());

		List<ContainerInfoManipulator> filteredRegisteredProperty = registeredPropertyContainerInfoManipulators
			.stream()
			.filter(m -> !activePaths.contains(PredicatePathConverter.toExpression(m.getNextNodePredicates())))
			.collect(Collectors.toList());

		List<ContainerInfoManipulator> containerInfoManipulators = new ArrayList<>();
		containerInfoManipulators.addAll(activeContainerInfoManipulators);
		containerInfoManipulators.addAll(filteredRegisteredRoot);
		containerInfoManipulators.addAll(filteredRegisteredProperty);

		// Compute relevant types to filter register entries
		// Only collect register operations for types that exist in the sample target's type tree
		Set<Class<?>> relevantTypes = collectRelevantTypes(rootProperty);

		// 5. 타입 기반 컨테이너 크기 수집
		Map<JvmType, Map<String, ArbitraryContainerInfo>> typedContainerSizes = new HashMap<>();
		for (PriorityMatcherOperator<ArbitraryBuilderContext> registered : registeredPropertyArbitraryBuilderContexts) {
			Matcher matcher = registered.getMatcher();

			Class<?> targetType = null;
			if (matcher instanceof ExactTypeMatcher) {
				targetType = ((ExactTypeMatcher)matcher).getType();
			} else if (matcher instanceof AssignableTypeMatcher) {
				targetType = ((AssignableTypeMatcher)matcher).getAnchorType();
			}

			if (targetType == null || !isRelevantType(targetType, matcher, relevantTypes)) {
				continue;
			}

			JvmType jvmType = resolveJvmTypeForMatcher(targetType, matcher, relevantTypes);

			for (Object cim : registered.getOperator().getContainerInfoManipulators()) {
				if (!(cim instanceof ContainerInfoManipulator)) {
					continue;
				}
				ContainerInfoManipulator manipulator = (ContainerInfoManipulator)cim;

				String fieldPath = PredicatePathConverter.toExpression(manipulator.getNextNodePredicates());

				// Note: activePaths check is NOT applied here because typedContainerSizes
				// are type-scoped (keyed by JvmType). A registered builder for ListStringObject
				// with path "$.values" should not be blocked by an active path "$.values" that
				// refers to a different type (e.g., NestedListStringObject.values).
				// Path-based container size resolution (EXACT_PATH) already takes priority
				// over type-based resolution (TYPE_BASED) in JvmNodeTreeTransformer.

				String fieldName = fieldPath.startsWith("$.") ? fieldPath.substring(2) : fieldPath;
				ArbitraryContainerInfo containerInfo = manipulator.getContainerInfo();

				Map<String, ArbitraryContainerInfo> fieldSizes = typedContainerSizes.computeIfAbsent(jvmType, k ->
					new HashMap<>()
				);
				ArbitraryContainerInfo existing = fieldSizes.get(fieldName);
				if (existing != null) {
					int mergedMin = Math.max(existing.getElementMinSize(), containerInfo.getElementMinSize());
					int mergedMax = Math.min(existing.getElementMaxSize(), containerInfo.getElementMaxSize());
					if (mergedMin > mergedMax) {
						mergedMax = mergedMin;
					}
					containerInfo = new ArbitraryContainerInfo(mergedMin, mergedMax);
				}
				fieldSizes.put(fieldName, containerInfo);
			}
		}

		// 6. 타입 기반 set 값 수집
		List<PriorityMatcherOperator<ArbitraryBuilderContext>> sortedByPriority = new ArrayList<>(
			registeredPropertyArbitraryBuilderContexts
		);
		// Sort by descending priority (higher number = lower priority processed first).
		// populateFromNodeManipulator uses put() (last-write-wins), so higher-priority
		// registers (lower number) are processed last and override lower-priority ones.
		sortedByPriority.sort(
			Comparator.comparingInt(PriorityMatcherOperator<ArbitraryBuilderContext>::getPriority).reversed()
		);

		Map<JvmType, Map<String, @Nullable Object>> typedValues = new HashMap<>();
		for (PriorityMatcherOperator<ArbitraryBuilderContext> registered : sortedByPriority) {
			Matcher matcher = registered.getMatcher();

			Class<?> targetType = null;
			if (matcher instanceof ExactTypeMatcher) {
				targetType = ((ExactTypeMatcher)matcher).getType();
			} else if (matcher instanceof AssignableTypeMatcher) {
				targetType = ((AssignableTypeMatcher)matcher).getAnchorType();
			}

			if (targetType == null || !isRelevantType(targetType, matcher, relevantTypes)) {
				continue;
			}

			// For AssignableTypeMatcher, use the actual relevant subtype as the JvmType key
			// so that typed values are matched against the concrete type in the tree
			JvmType jvmType = resolveJvmTypeForMatcher(targetType, matcher, relevantTypes);
			List<ArbitraryManipulator> manips = registered.getOperator().getManipulators();
			for (ArbitraryManipulator manipulator : manips) {
				TypedValueExtractor.extract(manipulator, jvmType, typedValues);
			}
		}

		// Adapter path: only use activeManipulators (registered manipulators are skipped
		// because ManipulatorAnalyzer already handles type-based matching via typedValues)
		List<ArbitraryManipulator> filteredJoinedManipulators = new ArrayList<>(activeManipulators);

		// Collect registered property configurers using inferredProperties
		Map<Class<?>, List<Property>> registeredPropertyConfigurers = registeredPropertyArbitraryBuilderContexts
			.stream()
			.filter(it -> inferredProperties.stream().anyMatch(it::match))
			.map(PriorityMatcherOperator::getOperator)
			.map(ArbitraryBuilderContext::getPropertyConfigurers)
			.findFirst()
			.orElse(Collections.emptyMap());

		Map<Class<?>, List<Property>> mergedPropertyConfigurers = new HashMap<>(registeredPropertyConfigurers);
		mergedPropertyConfigurers.putAll(activeContext.getPropertyConfigurers());

		// Collect registered introspectors using inferredProperties
		Map<Class<?>, ArbitraryIntrospector> registeredIntrospectors = registeredPropertyArbitraryBuilderContexts
			.stream()
			.filter(it -> inferredProperties.stream().anyMatch(it::match))
			.map(PriorityMatcherOperator::getOperator)
			.map(ArbitraryBuilderContext::getArbitraryIntrospectorsByType)
			.findFirst()
			.orElse(Collections.emptyMap());

		Map<Class<?>, ArbitraryIntrospector> mergedIntrospectors = new HashMap<>(registeredIntrospectors);
		mergedIntrospectors.putAll(activeContext.getArbitraryIntrospectorsByType());

		ManipulatorSet manipulatorSet = new ManipulatorSet(
			filteredJoinedManipulators,
			containerInfoManipulators,
			typedContainerSizes,
			typedValues,
			mergedPropertyConfigurers,
			mergedIntrospectors,
			activeContext.isFixed()
		);
		JvmType rootJvmType = toJvmType(rootProperty);

		long prepTimeNanos = System.nanoTime() - prepStartTime;

		return assembleAdapterResult(
			rootProperty,
			activeContext,
			filteredJoinedManipulators,
			fixtureMonkeyOptions,
			manipulatorSet,
			rootJvmType,
			containerInfoManipulators,
			activePaths,
			typedValues,
			typedContainerSizes,
			relevantTypes,
			mergedIntrospectors,
			prepTimeNanos
		);
	}

	/**
	 * Common adapter assembly logic shared by fast path and full path.
	 */
	@SuppressWarnings({"unchecked", "argument", "dereference.of.nullable"})
	private CombinableArbitrary<Object> assembleAdapterResult(
		TreeRootProperty rootProperty,
		ArbitraryBuilderContext activeContext,
		List<ArbitraryManipulator> manipulators,
		FixtureMonkeyOptions fixtureMonkeyOptions,
		ManipulatorSet manipulatorSet,
		JvmType rootJvmType,
		List<ContainerInfoManipulator> containerInfoManipulators,
		Set<String> activePaths,
		Map<JvmType, Map<String, @Nullable Object>> typedValues,
		Map<JvmType, Map<String, ArbitraryContainerInfo>> typedContainerSizes,
		Set<Class<?>> relevantTypes,
		Map<Class<?>, ArbitraryIntrospector> mergedIntrospectors,
		long prepTimeNanos
	) {
		// Create trace context early to capture resolution events
		TraceContext traceContext = adapterTracer.createTraceContext();

		// Measure total adapter time
		long adapterStartTime = System.nanoTime();

		AdaptationResult adaptationResult = nodeTreeAdapter.adapt(
			rootJvmType,
			manipulatorSet,
			fixtureMonkeyOptions,
			traceContext
		);

		AnalysisResult analysisResult = adaptationResult.getAnalysisResult();
		ValueProjection values = adaptationResult.getValues();

		// Strict mode validation
		if (analysisResult.isStrictMode()) {
			Set<String> invalidPaths = values.getUnresolvedNonWildcardPaths();
			if (!invalidPaths.isEmpty()) {
				throw new IllegalArgumentException(
					"No matching results for given NodeResolvers. " + "Invalid paths: " + invalidPaths
				);
			}
		}

		// Assemble using ValueProjection
		traceContext.setRootType(rootJvmType.getRawType().getName());

		Set<PathExpression> userContainerSizePaths = activePaths
			.stream()
			.map(PathExpression::of)
			.collect(Collectors.toSet());

		// Convert typedValues to TypeSelector-based PathExpression entries
		TypedValueExtractor.ConversionResult typedPathConversion = TypedValueExtractor.convertToPathExpressions(
			typedValues
		);

		AssembleContext assembleContext = AssembleContext.builder(monkeyContext)
			.rootProperty(rootProperty)
			.justPaths(new HashSet<>(analysisResult.getJustPaths()))
			.notNullPaths(analysisResult.getNotNullPaths())
			.filtersByPath(analysisResult.getFiltersByPath())
			.limitsByPath(new HashMap<>(analysisResult.getLimitsByPath()))
			.valueOrderByPath(analysisResult.getValueOrderByPath())
			.customizersByPath(analysisResult.getCustomizersByPath())
			.typedPathValues(typedPathConversion.values)
			.typedPathOrders(typedPathConversion.orders)
			.introspectorsByType(mergedIntrospectors)
			.traceContext(traceContext)
			.nodeTreeAdapter((NodeTreeAdapter)fixtureMonkeyOptions.getNodeTreeAdapter())
			.userContainerSizePaths(userContainerSizePaths)
			.build();

		// Measure assembly time
		long assemblyStartTime = System.nanoTime();
		CombinableArbitrary<Object> result = (CombinableArbitrary<Object>)values.assemble(assembleContext);
		long assemblyTimeNanos = System.nanoTime() - assemblyStartTime;

		// Calculate total adapter time
		long totalAdapterTimeNanos = System.nanoTime() - adapterStartTime;

		// Build and invoke trace with collected data
		AdapterTraceBuilder.buildAndInvoke(
			traceContext,
			manipulators,
			containerInfoManipulators,
			analysisResult,
			adaptationResult,
			prepTimeNanos,
			assemblyTimeNanos,
			totalAdapterTimeNanos,
			typedValues,
			typedContainerSizes,
			relevantTypes,
			activeContext.isFixed(),
			adapterTracer
		);

		return result;
	}

	private Set<Property> inferPossibleProperties(Property property, CycleDetector cycleDetector) {
		Class<?> actualType = Types.getActualType(property.getType());

		// Check cache for root-level calls (cache key is the actual type)
		Set<Property> cached = inferredPropertiesCache.get(actualType);
		if (cached != null) {
			return cached;
		}

		Set<Property> result = doInferPossibleProperties(property, cycleDetector);
		inferredPropertiesCache.put(actualType, result);
		return result;
	}

	private Set<Property> doInferPossibleProperties(Property property, CycleDetector cycleDetector) {
		Set<Property> collectedProperties = new HashSet<>();

		Class<?> actualType = Types.getActualType(property.getType());
		if (Types.isJavaType(actualType)) {
			collectedProperties.add(property);
			return collectedProperties;
		}

		if (nodeTreeAdapter != null && nodeTreeAdapter.isLeafType(actualType)) {
			collectedProperties.add(property);
			return collectedProperties;
		}

		cycleDetector.checkCycle(property, p -> {
			collectedProperties.add(p);

			Set<Property> leafChildProperties = FIELD_PROPERTY_GENERATOR.generateChildProperties(p)
				.stream()
				.flatMap(it -> doInferPossibleProperties(it, cycleDetector).stream())
				.collect(Collectors.toSet());
			collectedProperties.addAll(leafChildProperties);
		});

		return collectedProperties;
	}

	/**
	 * Collects all reachable types from a root property, including container element types.
	 * Recursively descends into type arguments and their fields to find all types
	 * that could be matched by register operations.
	 */
	private Set<Class<?>> collectRelevantTypes(Property rootProperty) {
		Set<Class<?>> types = new HashSet<>();
		Set<Class<?>> visitedForFields = new HashSet<>();
		collectRelevantTypesFromProperty(rootProperty, types, visitedForFields);
		return types;
	}

	private void collectRelevantTypesFromProperty(
		Property property,
		Set<Class<?>> types,
		Set<Class<?>> visitedForFields
	) {
		AnnotatedType annotatedType = property.getAnnotatedType();
		Class<?> rawType = Types.getActualType(annotatedType.getType());
		types.add(rawType);

		// Always recurse into type arguments (e.g., List<StringValue> → StringValue)
		if (annotatedType instanceof java.lang.reflect.AnnotatedParameterizedType) {
			for (AnnotatedType typeArg : (
				(java.lang.reflect.AnnotatedParameterizedType)annotatedType
			).getAnnotatedActualTypeArguments()) {
				Class<?> argType = Types.getActualType(typeArg.getType());
				if (!visitedForFields.contains(argType)) {
					collectRelevantTypesFromProperty(
						new com.navercorp.fixturemonkey.api.property.TypeParameterProperty(typeArg),
						types,
						visitedForFields
					);
				} else {
					types.add(argType);
				}
			}
		}

		// Recurse into fields of non-Java types to discover nested types
		if (
			!Types.isJavaType(rawType)
				&& !(nodeTreeAdapter != null && nodeTreeAdapter.isLeafType(rawType))
				&& visitedForFields.add(rawType)
		) {
			for (Property child : FIELD_PROPERTY_GENERATOR.generateChildProperties(property)) {
				collectRelevantTypesFromProperty(child, types, visitedForFields);
			}
		}
	}

	/**
	 * Checks if a registered type is relevant to the current type tree.
	 * For ExactTypeMatcher, checks direct containment.
	 * For AssignableTypeMatcher, also checks if any relevant type is a subtype of the target.
	 */
	private boolean isRelevantType(Class<?> targetType, Matcher matcher, Set<Class<?>> relevantTypes) {
		if (relevantTypes.contains(targetType)) {
			return true;
		}
		if (matcher instanceof AssignableTypeMatcher) {
			return relevantTypes.stream().anyMatch(targetType::isAssignableFrom);
		}
		return false;
	}

	/**
	 * Resolves the JvmType to use as a key for typed values/container sizes.
	 * For AssignableTypeMatcher where the anchor type is not directly in relevantTypes,
	 * uses the first matching subtype from relevantTypes so that the values are applied
	 * to the concrete type in the tree.
	 */
	private JvmType resolveJvmTypeForMatcher(Class<?> targetType, Matcher matcher, Set<Class<?>> relevantTypes) {
		if (relevantTypes.contains(targetType)) {
			return new JavaType(targetType);
		}
		if (matcher instanceof AssignableTypeMatcher) {
			for (Class<?> relevantType : relevantTypes) {
				if (targetType.isAssignableFrom(relevantType)) {
					return new JavaType(relevantType);
				}
			}
		}
		return new JavaType(targetType);
	}

	/**
	 * Converts a Property to a JvmType.
	 */
	private JvmType toJvmType(Property property) {
		return new JavaType(Types.toTypeReference(property.getAnnotatedType()));
	}

	private static final class CycleDetector {

		private final Set<Property> properties;

		public CycleDetector() {
			this.properties = new HashSet<>();
		}

		private void checkCycle(Property property, Consumer<Property> action) {
			if (properties.contains(property)) {
				return;
			}

			properties.add(property);
			try {
				action.accept(property);
			} finally {
				properties.remove(property);
			}
		}
	}
}
