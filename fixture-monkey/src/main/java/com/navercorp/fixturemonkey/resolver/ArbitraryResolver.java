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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

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
import com.navercorp.fixturemonkey.customizer.ManipulatorSet;
import com.navercorp.fixturemonkey.customizer.PathDirective;
import com.navercorp.fixturemonkey.customizer.SizeDirective;
import com.navercorp.fixturemonkey.planner.AnalysisResult;
import com.navercorp.fixturemonkey.planner.AssemblyPlan;
import com.navercorp.fixturemonkey.planner.AssemblyPlanner;
import com.navercorp.fixturemonkey.planner.TypedValueExtractor;
import com.navercorp.fixturemonkey.projection.AssembleContext;
import com.navercorp.fixturemonkey.projection.ValueProjection;
import com.navercorp.fixturemonkey.tracing.AssemblyTraceBuilder;
import com.navercorp.fixturemonkey.tracing.AssemblyTracer;
import com.navercorp.fixturemonkey.tracing.TraceContext;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.Types;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ArbitraryResolver {

	private final MonkeyContext monkeyContext;
	private final AssemblyPlanner assemblyPlanner;
	private final AssemblyTracer tracer;
	private final Map<Class<?>, Set<Property>> inferredPropertiesCache;

	public ArbitraryResolver(
		MonkeyContext monkeyContext,
		AssemblyPlanner assemblyPlanner,
		AssemblyTracer tracer,
		@Nullable Map<Class<?>, Set<Property>> inferredPropertiesCache
	) {
		this.monkeyContext = monkeyContext;
		this.assemblyPlanner = assemblyPlanner;
		this.tracer = tracer;
		this.inferredPropertiesCache =
			inferredPropertiesCache != null ? inferredPropertiesCache : new ConcurrentHashMap<>();
	}

	public CombinableArbitrary<?> resolve(
		TreeRootProperty rootProperty,
		ArbitraryBuilderContext activeContext,
		List<PriorityMatcherOperator<ArbitraryBuilderContext>> standbyContexts
	) {
		FixtureMonkeyOptions options = monkeyContext.getFixtureMonkeyOptions();
		List<PathDirective> activeDirectives = activeContext.getDirectives();

		return new RootArbitrary<>(
			rootProperty,
			() -> generate(rootProperty, activeContext, activeDirectives, standbyContexts, options),
			options.getGenerateMaxTries(),
			options.getDefaultArbitraryValidator(),
			activeContext::isValidOnly,
			() -> {
			}
		);
	}

	private CombinableArbitrary<Object> generate(
		TreeRootProperty rootProperty,
		ArbitraryBuilderContext activeContext,
		List<PathDirective> activeDirectives,
		List<PriorityMatcherOperator<ArbitraryBuilderContext>> standbyContexts,
		FixtureMonkeyOptions options
	) {
		long prepStart = System.nanoTime();

		boolean hasRegisteredBuilders = !monkeyContext.getRegisteredArbitraryBuilders().isEmpty();
		boolean hasStandbyContexts = !standbyContexts.isEmpty();

		// Fast path: when no registered builders and no standby contexts,
		// skip all register-related preparation (inferPossibleProperties, collectRelevantTypes,
		// typed values/container sizes collection, registered property configurers/introspectors)
		if (!hasRegisteredBuilders && !hasStandbyContexts) {
			return generateFastPath(
				rootProperty,
				activeContext,
				activeDirectives,
				options,
				prepStart
			);
		}

		return generateFullPath(
			rootProperty,
			activeContext,
			activeDirectives,
			standbyContexts,
			options,
			prepStart
		);
	}

	/**
	 * Fast path when no registered builders or standby contexts exist.
	 * Skips all register-related preparation for significantly better performance.
	 */
	private CombinableArbitrary<Object> generateFastPath(
		TreeRootProperty rootProperty,
		ArbitraryBuilderContext activeContext,
		List<PathDirective> activeDirectives,
		FixtureMonkeyOptions options,
		long prepStart
	) {
		// Set validOnly from inferred property annotations (only if builderContextInitializers exist)
		if (!options.getBuilderContextInitializers().isEmpty()) {
			Set<Property> inferredProperties = inferPossibleProperties(rootProperty, new CycleDetector());
			Set<Annotation> allAnnotations = inferredProperties
				.stream()
				.flatMap(p -> p.getAnnotations().stream())
				.collect(Collectors.toSet());
			options
				.getBuilderContextInitializers()
				.stream()
				.filter(it -> it.match(new DefaultTreeMatcherMetadata(allAnnotations)))
				.findFirst()
				.map(TreeMatcherOperator::getOperator)
				.ifPresent(it -> activeContext.setOptionValidOnly(it.isValidOnly()));
		}

		// Active context container info only (no registered builders to merge)
		List<SizeDirective> sizeDirectives = new ArrayList<>(activeContext.getSizeDirectives());

		Set<String> activePaths = sizeDirectives
			.stream()
			.map(m -> m.path().toExpression())
			.collect(Collectors.toSet());

		Map<Class<?>, ArbitraryIntrospector> introspectorsByType = activeContext.getArbitraryIntrospectorsByType();

		ManipulatorSet manipulatorSet = new ManipulatorSet(
			new ArrayList<>(activeDirectives),
			Collections.emptyMap(),
			Collections.emptyMap(),
			activeContext.getPropertyConfigurers(),
			introspectorsByType,
			activeContext.isFixed()
		);
		JvmType rootJvmType = rootProperty.getJvmType();

		long prepNanos = System.nanoTime() - prepStart;

		return assembleAdapterResult(
			rootProperty,
			activeContext,
			activeDirectives,
			options,
			manipulatorSet,
			rootJvmType,
			sizeDirectives,
			activePaths,
			Collections.emptyMap(),
			Collections.emptyMap(),
			Collections.emptySet(),
			introspectorsByType,
			prepNanos
		);
	}

	/**
	 * Full path for adapter generation with registered builders and standby contexts.
	 */
	private CombinableArbitrary<Object> generateFullPath(
		TreeRootProperty rootProperty,
		ArbitraryBuilderContext activeContext,
		List<PathDirective> activeDirectives,
		List<PriorityMatcherOperator<ArbitraryBuilderContext>> standbyContexts,
		FixtureMonkeyOptions options,
		long prepStart
	) {
		// Infer possible properties for type-based matching
		Set<Property> inferredProperties = inferPossibleProperties(rootProperty, new CycleDetector());

		// Set validOnly from all inferred property annotations (gathered from the entire tree)
		Set<Annotation> allAnnotations = inferredProperties
			.stream()
			.flatMap(p -> p.getAnnotations().stream())
			.collect(Collectors.toSet());
		options
			.getBuilderContextInitializers()
			.stream()
			.filter(it -> it.match(new DefaultTreeMatcherMetadata(allAnnotations)))
			.findFirst()
			.map(TreeMatcherOperator::getOperator)
			.ifPresent(it -> activeContext.setOptionValidOnly(it.isValidOnly()));

		// Get registered property builders for type-based matching
		List<PriorityMatcherOperator<ArbitraryBuilderContext>> registeredPropertyContexts =
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

		// 1. Collect SizeDirectives from registered root builders
		List<SizeDirective> standbySizeDirectives = standbyContexts
			.stream()
			.map(PriorityMatcherOperator::getOperator)
			.flatMap(ctx -> ctx.getSizeDirectives().stream())
			.collect(Collectors.toList());

		// 2. Collect SizeDirectives from registered property builders
		// Use inferred properties for matching
		List<SizeDirective> registeredPropertySizeDirectives = registeredPropertyContexts
			.stream()
			.filter(it -> inferredProperties.stream().anyMatch(it::match))
			.map(PriorityMatcherOperator::getOperator)
			.flatMap(ctx -> ctx.getSizeDirectives().stream())
			.collect(Collectors.toList());

		// 3. Collect SizeDirectives from the active context
		List<SizeDirective> activeSizeDirectives = new ArrayList<>(activeContext.getSizeDirectives());

		// 4. Merge all SizeDirectives (active overrides registered when paths overlap)
		Set<String> activePaths = activeSizeDirectives
			.stream()
			.map(m -> m.path().toExpression())
			.collect(Collectors.toSet());

		List<SizeDirective> nonOverlappingStandbyDirectives = standbySizeDirectives
			.stream()
			.filter(m -> !activePaths.contains(m.path().toExpression()))
			.collect(Collectors.toList());

		List<SizeDirective> nonOverlappingPropertyDirectives = registeredPropertySizeDirectives
			.stream()
			.filter(m -> !activePaths.contains(m.path().toExpression()))
			.collect(Collectors.toList());

		List<SizeDirective> mergedSizeDirectives = new ArrayList<>();
		mergedSizeDirectives.addAll(activeSizeDirectives);
		mergedSizeDirectives.addAll(nonOverlappingStandbyDirectives);
		mergedSizeDirectives.addAll(nonOverlappingPropertyDirectives);

		// Compute relevant types to filter register entries
		// Only collect register operations for types that exist in the sample target's type tree
		Set<Class<?>> relevantTypes = collectRelevantTypes(rootProperty);

		// 5. Collect type-based container sizes
		Map<JvmType, Map<String, ArbitraryContainerInfo>> typedContainerSizes = new HashMap<>();
		for (PriorityMatcherOperator<ArbitraryBuilderContext> registered : registeredPropertyContexts) {
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

			for (SizeDirective directive : registered.getOperator().getSizeDirectives()) {
				String fieldPath = directive.path().toExpression();

				// Note: activePaths check is NOT applied here because typedContainerSizes
				// are type-scoped (keyed by JvmType). A registered builder for ListStringObject
				// with path "$.values" should not be blocked by an active path "$.values" that
				// refers to a different type (e.g., NestedListStringObject.values).
				// Path-based container size resolution (EXACT_PATH) already takes priority
				// over type-based resolution (TYPE_BASED) in JvmNodeTreeTransformer.

				String fieldName = fieldPath.startsWith("$.") ? fieldPath.substring(2) : fieldPath;
				ArbitraryContainerInfo containerInfo = directive.containerInfo();

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

		// 6. Collect type-based set values
		List<PriorityMatcherOperator<ArbitraryBuilderContext>> sortedByPriority = new ArrayList<>(
			registeredPropertyContexts
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
			List<PathDirective> directives = registered.getOperator().getDirectives();
			for (PathDirective directive : directives) {
				TypedValueExtractor.extract(directive, jvmType, typedValues);
			}
		}

		// Only use activeDirectives (registered manipulators are skipped
		// because ManipulatorAnalyzer already handles type-based matching via typedValues)

		// Collect registered property configurers using inferredProperties
		Map<Class<?>, List<Property>> registeredPropertyConfigurers = registeredPropertyContexts
			.stream()
			.filter(it -> inferredProperties.stream().anyMatch(it::match))
			.map(PriorityMatcherOperator::getOperator)
			.map(ArbitraryBuilderContext::getPropertyConfigurers)
			.findFirst()
			.orElse(Collections.emptyMap());

		Map<Class<?>, List<Property>> mergedPropertyConfigurers = new HashMap<>(registeredPropertyConfigurers);
		mergedPropertyConfigurers.putAll(activeContext.getPropertyConfigurers());

		// Collect registered introspectors using inferredProperties
		Map<Class<?>, ArbitraryIntrospector> registeredIntrospectors = registeredPropertyContexts
			.stream()
			.filter(it -> inferredProperties.stream().anyMatch(it::match))
			.map(PriorityMatcherOperator::getOperator)
			.map(ArbitraryBuilderContext::getArbitraryIntrospectorsByType)
			.findFirst()
			.orElse(Collections.emptyMap());

		Map<Class<?>, ArbitraryIntrospector> mergedIntrospectors = new HashMap<>(registeredIntrospectors);
		mergedIntrospectors.putAll(activeContext.getArbitraryIntrospectorsByType());

		List<PathDirective> joinedDirectives = new ArrayList<>(activeDirectives);
		joinedDirectives.addAll(mergedSizeDirectives);
		ManipulatorSet manipulatorSet = new ManipulatorSet(
			joinedDirectives,
			typedContainerSizes,
			typedValues,
			mergedPropertyConfigurers,
			mergedIntrospectors,
			activeContext.isFixed()
		);
		JvmType rootJvmType = rootProperty.getJvmType();

		long prepNanos = System.nanoTime() - prepStart;

		return assembleAdapterResult(
			rootProperty,
			activeContext,
			activeDirectives,
			options,
			manipulatorSet,
			rootJvmType,
			mergedSizeDirectives,
			activePaths,
			typedValues,
			typedContainerSizes,
			relevantTypes,
			mergedIntrospectors,
			prepNanos
		);
	}

	/**
	 * Common adapter assembly logic shared by fast path and full path.
	 */
	@SuppressWarnings({"unchecked", "argument", "dereference.of.nullable"})
	private CombinableArbitrary<Object> assembleAdapterResult(
		TreeRootProperty rootProperty,
		ArbitraryBuilderContext activeContext,
		List<PathDirective> directives,
		FixtureMonkeyOptions options,
		ManipulatorSet manipulatorSet,
		JvmType rootJvmType,
		List<SizeDirective> sizeDirectives,
		Set<String> activePaths,
		Map<JvmType, Map<String, @Nullable Object>> typedValues,
		Map<JvmType, Map<String, ArbitraryContainerInfo>> typedContainerSizes,
		Set<Class<?>> relevantTypes,
		Map<Class<?>, ArbitraryIntrospector> mergedIntrospectors,
		long prepNanos
	) {
		// Create trace context early to capture resolution events
		TraceContext traceContext = tracer.createTraceContext();

		// Measure total adapter time
		long adapterStartTime = System.nanoTime();

		AssemblyPlan assemblyPlan = assemblyPlanner.plan(
			rootJvmType,
			manipulatorSet,
			options,
			traceContext
		);

		AnalysisResult analysisResult = assemblyPlan.getAnalysisResult();
		ValueProjection values = assemblyPlan.getValues();

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
			.runtimeTreeFactory(this.assemblyPlanner)
			.pathResolverContext(assemblyPlan.getResolverContext())
			.nodeMetadataCache(this.assemblyPlanner.nodeMetadataCache())
			.userContainerSizePaths(userContainerSizePaths)
			.build();

		// Measure assembly time
		long assemblyStartTime = System.nanoTime();
		CombinableArbitrary<Object> result = (CombinableArbitrary<Object>)values.assemble(assembleContext);
		long assemblyTimeNanos = System.nanoTime() - assemblyStartTime;

		// Calculate total adapter time
		long totalAdapterTimeNanos = System.nanoTime() - adapterStartTime;

		// Build and invoke trace with collected data
		AssemblyTraceBuilder.buildAndInvoke(
			traceContext,
			directives,
			sizeDirectives,
			analysisResult,
			assemblyPlan,
			prepNanos,
			assemblyTimeNanos,
			totalAdapterTimeNanos,
			typedValues,
			typedContainerSizes,
			relevantTypes,
			activeContext.isFixed(),
			tracer
		);

		return result;
	}

	private Set<Property> inferPossibleProperties(Property property, CycleDetector cycleDetector) {
		Class<?> actualType = com.navercorp.fixturemonkey.api.type.Types.normalizeRawType(
			property.getJvmType().getRawType()
		);

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

		Class<?> actualType = com.navercorp.fixturemonkey.api.type.Types.normalizeRawType(
			property.getJvmType().getRawType()
		);
		if (Types.isJavaType(actualType)) {
			collectedProperties.add(property);
			return collectedProperties;
		}

		if (assemblyPlanner.isLeafType(actualType)) {
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
		JvmType jvmType = property.getJvmType();
		Class<?> rawType = jvmType.getRawType();
		types.add(rawType);

		// Always recurse into type arguments (e.g., List<StringValue> → StringValue)
		for (JvmType typeArg : jvmType.getTypeVariables()) {
			Class<?> argType = typeArg.getRawType();
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

		// Recurse into fields of non-Java types to discover nested types
		if (
			!Types.isJavaType(rawType)
				&& !assemblyPlanner.isLeafType(rawType)
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
