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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.context.MonkeyContext;
import com.navercorp.fixturemonkey.api.matcher.DefaultTreeMatcherMetadata;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.matcher.TreeMatcherOperator;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.TreeRootProperty;
import com.navercorp.fixturemonkey.assembly.AssembleContext;
import com.navercorp.fixturemonkey.assembly.ValueProjectionAssembler;
import com.navercorp.fixturemonkey.builder.ArbitraryBuilderContext;
import com.navercorp.fixturemonkey.customizer.Scope;
import com.navercorp.fixturemonkey.customizer.ScopeSet;
import com.navercorp.fixturemonkey.planner.AnalysisResult;
import com.navercorp.fixturemonkey.planner.AssemblyPlan;
import com.navercorp.fixturemonkey.planner.AssemblyPlanner;
import com.navercorp.fixturemonkey.planner.ValueProjection;
import com.navercorp.fixturemonkey.tracing.AssemblyTracer;
import com.navercorp.fixturemonkey.tracing.TraceContext;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.Types;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ArbitraryResolver {

	private final MonkeyContext monkeyContext;
	private final AssemblyPlanner assemblyPlanner;
	private final AssemblyTracer tracer;
	private final Map<Class<?>, Set<Property>> inferredPropertiesCache;
	private final Supplier<List<Scope>> registeredScopes;
	private final Map<JvmType, List<Scope>> definedScopesByRootType;

	public ArbitraryResolver(
		MonkeyContext monkeyContext,
		AssemblyPlanner assemblyPlanner,
		AssemblyTracer tracer,
		@Nullable Map<Class<?>, Set<Property>> inferredPropertiesCache,
		Supplier<List<Scope>> registeredScopes,
		Map<JvmType, List<Scope>> definedScopesByRootType
	) {
		this.monkeyContext = monkeyContext;
		this.assemblyPlanner = assemblyPlanner;
		this.tracer = tracer;
		this.inferredPropertiesCache =
			inferredPropertiesCache != null ? inferredPropertiesCache : new ConcurrentHashMap<>();
		this.registeredScopes = registeredScopes;
		this.definedScopesByRootType = definedScopesByRootType;
	}

	public CombinableArbitrary<?> resolve(TreeRootProperty rootProperty, ArbitraryBuilderContext activeContext) {
		FixtureMonkeyOptions options = monkeyContext.getFixtureMonkeyOptions();

		return new RootArbitrary<>(
			rootProperty,
			() -> generate(rootProperty, activeContext, options),
			options.getGenerateMaxTries(),
			options.getDefaultArbitraryValidator(),
			activeContext::isValidOnly
		);
	}

	private CombinableArbitrary<Object> generate(
		TreeRootProperty rootProperty,
		ArbitraryBuilderContext activeContext,
		FixtureMonkeyOptions options
	) {
		long prepStart = System.nanoTime();
		applyValidOnlyOption(rootProperty, activeContext, options);
		ScopeSet scopeSet = new ScopeSet(activeContext.toRootScope(), definedScopesOf(rootProperty));
		long prepNanos = System.nanoTime() - prepStart;

		return assemble(rootProperty, activeContext, options, scopeSet, prepNanos);
	}

	private void applyValidOnlyOption(
		TreeRootProperty rootProperty,
		ArbitraryBuilderContext activeContext,
		FixtureMonkeyOptions options
	) {
		if (options.getBuilderContextInitializers().isEmpty()) {
			return;
		}
		Set<Annotation> allAnnotations = inferPossibleProperties(rootProperty, new CycleDetector())
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

	/**
	 * Returns a defined scope for each register() builder whose type can appear in the sample. The register() builders
	 * do not change once registered, so the scopes are found once for each root type.
	 */
	private List<Scope> definedScopesOf(TreeRootProperty rootProperty) {
		List<Scope> scopes = registeredScopes.get();
		if (scopes.isEmpty()) {
			return Collections.emptyList();
		}
		return definedScopesByRootType.computeIfAbsent(
			rootProperty.getJvmType(),
			rootType -> definedScopesAmong(scopes, collectRelevantTypes(rootProperty))
		);
	}

	private static List<Scope> definedScopesAmong(List<Scope> scopes, Set<Class<?>> relevantTypes) {
		List<Scope> definedScopes = new ArrayList<>();
		for (Scope scope : scopes) {
			if (scope.getSelector().mayAppearAmong(relevantTypes)) {
				definedScopes.add(scope);
			}
		}
		return Collections.unmodifiableList(definedScopes);
	}

	@SuppressWarnings({"unchecked", "argument", "dereference.of.nullable"})
	private CombinableArbitrary<Object> assemble(
		TreeRootProperty rootProperty,
		ArbitraryBuilderContext activeContext,
		FixtureMonkeyOptions options,
		ScopeSet scopeSet,
		long prepNanos
	) {
		JvmType rootJvmType = rootProperty.getJvmType();
		TraceContext traceContext = tracer.createTraceContext();
		long adapterStartTime = System.nanoTime();

		AssemblyPlan assemblyPlan =
			assemblyPlanner.plan(rootJvmType, scopeSet, activeContext.isFixed(), options, traceContext);

		AnalysisResult analysisResult = assemblyPlan.getAnalysisResult();
		ValueProjection values = assemblyPlan.getValues();

		if (analysisResult.isStrictMode()) {
			Set<String> invalidPaths = values.getUnresolvedNonWildcardPaths();
			if (!invalidPaths.isEmpty()) {
				throw new IllegalArgumentException(
					"No matching results for given NodeResolvers. " + "Invalid paths: " + invalidPaths
				);
			}
		}

		traceContext.setRootType(rootJvmType.getRawType().getName());

		AssembleContext assembleContext = AssembleContext.builder(monkeyContext, assemblyPlan)
			.rootProperty(rootProperty)
			.traceContext(traceContext)
			.build();

		long assemblyStartTime = System.nanoTime();
		CombinableArbitrary<Object> result =
			(CombinableArbitrary<Object>)ValueProjectionAssembler.assemble(assembleContext);
		long assemblyTimeNanos = System.nanoTime() - assemblyStartTime;

		long totalAdapterTimeNanos = System.nanoTime() - adapterStartTime;

		AssemblyTraceBuilder.buildAndInvoke(
			traceContext,
			assemblyPlan,
			prepNanos,
			assemblyTimeNanos,
			totalAdapterTimeNanos,
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

			for (Property candidate : resolveCandidateProperties(p)) {
				collectedProperties.addAll(doInferPossibleProperties(candidate, cycleDetector));
			}
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

			for (Property candidate : resolveCandidateProperties(property)) {
				collectRelevantTypesFromProperty(candidate, types, visitedForFields);
			}
		}
	}

	private List<Property> resolveCandidateProperties(Property property) {
		Class<?> rawType = property.getJvmType().getRawType();
		if (rawType.isPrimitive()
			|| (!Modifier.isInterface(rawType.getModifiers()) && !Modifier.isAbstract(rawType.getModifiers()))) {
			return Collections.emptyList();
		}

		return monkeyContext.getFixtureMonkeyOptions()
			.getCandidateConcretePropertyResolvers()
			.stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.map(it -> it.resolve(property))
			.filter(Objects::nonNull)
			.flatMap(List::stream)
			.filter(it -> it.getJvmType().getRawType() != rawType)
			.collect(Collectors.toList());
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
