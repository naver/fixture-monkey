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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

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
import com.navercorp.fixturemonkey.nodecandidate.ContainerPropertyGeneratorNodeGenerator;
import com.navercorp.fixturemonkey.nodecandidate.NameResolvingNodeCandidateGenerator;
import com.navercorp.fixturemonkey.nodecandidate.PropertyGeneratorNodeCandidateGenerator;
import com.navercorp.fixturemonkey.property.JvmNodePropertyFactory;
import com.navercorp.objectfarm.api.node.AbstractTypeNodePromoter;
import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
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
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.WildcardRawType;

/**
 * Builds {@link JvmNodeContext} instances configured with promoters, generators, name resolvers,
 * leaf type resolvers, and interface resolvers.
 * <p>
 * Construction follows {@link FixtureMonkeyOptions} together with per-call
 * {@code propertyConfigurers} and {@code introspectorsByType} overrides.
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
public final class NodeContextFactory {
	/**
	 * Marker types used by dedicated introspectors in DEFAULT_ARBITRARY_INTROSPECTORS.
	 * In the non-adapter path, these are handled by short-circuit introspectors
	 * that produce values directly without child property generation.
	 */
	private static final LeafTypeResolver MARKER_TYPE_LEAF_RESOLVER = jvmType -> {
		Class<?> rt = jvmType.getRawType();
		return rt == Types.GeneratingWildcardType.class
			|| rt == Types.UnidentifiableType.class
			|| rt == WildcardRawType.class;
	};

	private final SeedState seedState;
	private final ContainerSizeResolverFactory containerSizeResolverFactory;
	private final List<JvmNodePromoter> additionalPromoters;
	private final List<LeafTypeResolver> additionalLeafTypeResolvers;
	private final @Nullable UnaryOperator<JvmNodeCandidateGenerator> candidateGeneratorWrapper;

	NodeContextFactory(
		SeedState seedState,
		ContainerSizeResolverFactory containerSizeResolverFactory,
		List<JvmNodePromoter> additionalPromoters,
		List<LeafTypeResolver> additionalLeafTypeResolvers,
		@Nullable UnaryOperator<JvmNodeCandidateGenerator> candidateGeneratorWrapper
	) {
		this.seedState = seedState;
		this.containerSizeResolverFactory = containerSizeResolverFactory;
		this.additionalPromoters = additionalPromoters;
		this.additionalLeafTypeResolvers = additionalLeafTypeResolvers;
		this.candidateGeneratorWrapper = candidateGeneratorWrapper;
	}

	/**
	 * Builds a {@link JvmNodeContext} for the given root type and configuration.
	 */
	public JvmNodeContext build(
		JvmType rootType,
		@Nullable FixtureMonkeyOptions options,
		Map<Class<?>, List<Property>> propertyConfigurers,
		Map<Class<?>, ArbitraryIntrospector> introspectorsByType
	) {
		ContainerSizeResolver containerSizeResolver = containerSizeResolverFactory.createContainerSizeResolver(options);

		JavaNodeContext.Builder builder = JavaNodeContext.builder()
			.seedState(seedState)
			.nodePromoters(Collections.singletonList(new JavaDefaultNodePromoter(createNodePromoters())))
			.containerSizeResolver(containerSizeResolver);

		if (options != null) {
			configureOptions(builder, rootType, options, propertyConfigurers, introspectorsByType);
		}

		return builder.build();
	}

	private void configureOptions(
		JavaNodeContext.Builder builder,
		JvmType rootType,
		FixtureMonkeyOptions options,
		Map<Class<?>, List<Property>> propertyConfigurers,
		Map<Class<?>, ArbitraryIntrospector> introspectorsByType
	) {
		NameResolvingNodeCandidateGenerator.ChildNameResolver nameResolver = createNodeNameResolver(options);
		configureCandidateGenerator(
			builder, rootType, options, propertyConfigurers, introspectorsByType, nameResolver
		);

		builder.interfaceResolver(new GlobalInterfaceResolver(seedState, options));
		builder.maxDepth(options.getMaxRecursionDepth());

		configureContainerGenerators(builder, options);
		configureLeafResolvers(builder);
	}

	private void configureCandidateGenerator(
		JavaNodeContext.Builder builder,
		JvmType rootType,
		FixtureMonkeyOptions options,
		Map<Class<?>, List<Property>> propertyConfigurers,
		Map<Class<?>, ArbitraryIntrospector> introspectorsByType,
		NameResolvingNodeCandidateGenerator.ChildNameResolver nameResolver
	) {
		ArbitraryGenerator defaultGenerator = options.getDefaultArbitraryGenerator();
		Property rootProperty = JvmNodePropertyFactory.fromType(rootType);

		if (defaultGenerator.getRequiredPropertyGenerator(rootProperty) == null) {
			JvmNodeCandidateGenerator candidateGenerator = new NameResolvingNodeCandidateGenerator(
				new JavaFieldNodeCandidateGenerator(),
				nameResolver
			);
			applyCandidateGenerator(builder, candidateGenerator);
			return;
		}

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
		JvmNodeCandidateGenerator candidateGenerator = new NameResolvingNodeCandidateGenerator(
			new PropertyGeneratorNodeCandidateGenerator(compositeGenerator),
			nameResolver
		);
		applyCandidateGenerator(builder, candidateGenerator);

		builder.leafTypeExclusion(
			jvmType -> shouldExcludeFromLeaf(
				jvmType, propertyConfigurers, introspectorsByType, propertyGenerators, basePropertyGenerator
			)
		);
	}

	private void applyCandidateGenerator(JavaNodeContext.Builder builder, JvmNodeCandidateGenerator candidateGenerator) {
		if (candidateGeneratorWrapper != null) {
			// Wrap with platform-specific isSupported check
			// (e.g., KotlinNodeCandidateGenerator that only supports Kotlin types).
			// With first-match-wins in buildTree, the wrapped generator handles supported types.
			builder.addCustomGenerator(candidateGeneratorWrapper.apply(candidateGenerator));
		}
		// Also set as objectPropertyGenerator for non-Kotlin types (e.g., Java's File class)
		// that need the introspector's requiredPropertyGenerator.
		builder.objectPropertyGenerator(candidateGenerator);
	}

	/**
	 * Determines whether a type should be excluded from leaf treatment.
	 * <p>
	 * Excluded types are expanded into child properties — e.g., Java standard types
	 * ({@code java.io.File}, {@code java.sql.Timestamp}) when constructor-based introspectors
	 * have a requiredPropertyGenerator that knows how to expand them.
	 */
	private static boolean shouldExcludeFromLeaf(
		JvmType jvmType,
		Map<Class<?>, List<Property>> propertyConfigurers,
		Map<Class<?>, ArbitraryIntrospector> introspectorsByType,
		List<MatcherOperator<PropertyGenerator>> propertyGenerators,
		PropertyGenerator basePropertyGenerator
	) {
		Class<?> rawType = Types.normalizeRawType(jvmType.getRawType());
		if (rawType.isPrimitive() || rawType.isArray() || rawType.isEnum()) {
			return false;
		}

		if (propertyConfigurers.containsKey(rawType) || introspectorsByType.containsKey(rawType)) {
			return true;
		}

		Property property = JvmNodePropertyFactory.fromType(jvmType);
		for (MatcherOperator<PropertyGenerator> op : propertyGenerators) {
			if (op.match(property)) {
				return true;
			}
		}

		// Check if the global objectIntrospector (via basePropertyGenerator)
		// produces children for this type. This handles cases like
		// PriorityConstructorArbitraryIntrospector expanding java.sql.Timestamp
		// via its Timestamp(long) constructor.
		try {
			List<Property> children = basePropertyGenerator.generateChildProperties(property);
			return children != null && !children.isEmpty();
		} catch (RuntimeException e) {
			// Keep as leaf if generator fails (e.g., inaccessible constructor)
			return false;
		}
	}

	private static void configureContainerGenerators(JavaNodeContext.Builder builder, FixtureMonkeyOptions options) {
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
	}

	private void configureLeafResolvers(JavaNodeContext.Builder builder) {
		builder.addLeafTypeResolver(MARKER_TYPE_LEAF_RESOLVER);
		for (LeafTypeResolver leafTypeResolver : additionalLeafTypeResolvers) {
			builder.addLeafTypeResolver(leafTypeResolver);
		}
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
				Field field = TypeCache.getFieldsByName(parentType.getRawType()).get(candidateName);
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
	private static PropertyGenerator createCompositePropertyGenerator(
		PropertyGenerator baseGenerator,
		Map<Class<?>, List<Property>> propertyConfigurers,
		Map<Class<?>, ArbitraryIntrospector> introspectorsByType,
		List<MatcherOperator<PropertyGenerator>> propertyGenerators
	) {
		if (propertyConfigurers.isEmpty() && introspectorsByType.isEmpty() && propertyGenerators.isEmpty()) {
			return baseGenerator;
		}

		return property -> {
			Class<?> actualType = Types.normalizeRawType(property.getJvmType().getRawType());

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

	private static final class GlobalInterfaceResolver implements InterfaceResolver {
		private final SeedState seedState;
		private final FixtureMonkeyOptions options;

		GlobalInterfaceResolver(SeedState seedState, FixtureMonkeyOptions options) {
			this.seedState = seedState;
			this.options = options;
		}

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
				.map(Property::getJvmType)
				.collect(Collectors.toList());
		}
	}
}
