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

package com.navercorp.fixturemonkey.api.option;

import static com.navercorp.fixturemonkey.api.constraint.JavaConstraintGenerator.DEFAULT_JAVA_CONSTRAINT_GENERATOR;
import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.DEFAULT_NOTNULL_ANNOTATION_TYPES;
import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.DEFAULT_NULLABLE_ANNOTATION_TYPES;
import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.DEFAULT_NULL_INJECT;
import static com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions.DEFAULT_ARBITRARY_INTROSPECTORS;
import static com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions.DEFAULT_MAX_UNIQUE_GENERATION_COUNT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.arbitrary.JavaTimeArbitraryGeneratorSet;
import com.navercorp.fixturemonkey.api.arbitrary.JavaTypeArbitraryGeneratorSet;
import com.navercorp.fixturemonkey.api.constraint.JavaConstraintGenerator;
import com.navercorp.fixturemonkey.api.container.DecomposableJavaContainer;
import com.navercorp.fixturemonkey.api.container.DecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.api.container.DefaultDecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.IntrospectedArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.JavaDefaultArbitraryGeneratorBuilder;
import com.navercorp.fixturemonkey.api.generator.MatchArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.NullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.instantiator.InstantiatorProcessor;
import com.navercorp.fixturemonkey.api.instantiator.JavaInstantiatorProcessor;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.MatchArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.TypedArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.jqwik.JavaArbitraryResolver;
import com.navercorp.fixturemonkey.api.jqwik.JavaTimeArbitraryResolver;
import com.navercorp.fixturemonkey.api.jqwik.JavaTimeTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.jqwik.JavaTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.jqwik.JqwikJavaArbitraryResolver;
import com.navercorp.fixturemonkey.api.jqwik.JqwikJavaTimeArbitraryGeneratorSet;
import com.navercorp.fixturemonkey.api.jqwik.JqwikJavaTimeArbitraryResolver;
import com.navercorp.fixturemonkey.api.jqwik.JqwikJavaTypeArbitraryGeneratorSet;
import com.navercorp.fixturemonkey.api.matcher.DefaultMatcherOperatorContainer;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.matcher.TreeMatcherOperator;
import com.navercorp.fixturemonkey.api.plugin.Plugin;
import com.navercorp.fixturemonkey.api.property.CandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.property.DefaultPropertyGenerator;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.validator.ArbitraryValidator;

@SuppressWarnings("UnusedReturnValue")
@API(since = "0.6.0", status = Status.MAINTAINED)
public final class FixtureMonkeyOptionsBuilder {
	private DefaultMatcherOperatorContainer<PropertyGenerator> propertyGenerators;
	private PropertyGenerator defaultPropertyGenerator = new DefaultPropertyGenerator();
	private DefaultMatcherOperatorContainer<ObjectPropertyGenerator> arbitraryObjectPropertyGenerators;
	private DefaultMatcherOperatorContainer<ContainerPropertyGenerator> containerPropertyGenerators;
	private ObjectPropertyGenerator defaultObjectPropertyGenerator;
	private DefaultMatcherOperatorContainer<PropertyNameResolver> propertyNameResolvers;
	private PropertyNameResolver defaultPropertyNameResolver;
	private DefaultMatcherOperatorContainer<NullInjectGenerator> nullInjectGenerators;
	private NullInjectGenerator defaultNullInjectGenerator;
	private DefaultMatcherOperatorContainer<ArbitraryContainerInfoGenerator> arbitraryContainerInfoGenerators;
	private ArbitraryContainerInfoGenerator defaultArbitraryContainerInfoGenerator;
	private ArbitraryGenerator defaultArbitraryGenerator;
	private UnaryOperator<ArbitraryGenerator> defaultArbitraryGeneratorOperator = it -> it;
	private DefaultMatcherOperatorContainer<ArbitraryIntrospector> arbitraryIntrospectors;
	private final JavaDefaultArbitraryGeneratorBuilder javaDefaultArbitraryGeneratorBuilder =
		IntrospectedArbitraryGenerator.javaBuilder();
	private boolean defaultNotNull = false;
	private boolean nullableContainer = false;
	private boolean nullableElement = false;
	private boolean enableLoggingFail = true;
	private UnaryOperator<NullInjectGenerator> defaultNullInjectGeneratorOperator = it -> it;
	private ArbitraryValidator defaultArbitraryValidator = (obj) -> {
	};
	private DecomposedContainerValueFactory decomposedContainerValueFactory =
		new DefaultDecomposedContainerValueFactory(
			(obj) -> {
				throw new IllegalArgumentException(
					"given type is not supported container : " + obj.getClass().getTypeName());
			}
		);
	private final Map<Class<?>, DecomposedContainerValueFactory> decomposableContainerFactoryMap = new HashMap<>();
	private int generateMaxTries = CombinableArbitrary.DEFAULT_MAX_TRIES;
	private int generateUniqueMaxTries = DEFAULT_MAX_UNIQUE_GENERATION_COUNT;
	private JavaConstraintGenerator javaConstraintGenerator = DEFAULT_JAVA_CONSTRAINT_GENERATOR;
	private final List<UnaryOperator<JavaConstraintGenerator>> javaConstraintGeneratorCustomizers = new ArrayList<>();
	private JavaTypeArbitraryGenerator javaTypeArbitraryGenerator = new JavaTypeArbitraryGenerator() {
	};
	@Nullable
	private JavaArbitraryResolver javaArbitraryResolver = null;
	private JavaTimeTypeArbitraryGenerator javaTimeTypeArbitraryGenerator = new JavaTimeTypeArbitraryGenerator() {
	};
	@Nullable
	private JavaTimeArbitraryResolver javaTimeArbitraryResolver = null;
	@Nullable
	private Function<JavaConstraintGenerator, JavaTypeArbitraryGeneratorSet> generateJavaTypeArbitrarySet = null;
	@Nullable
	private Function<JavaConstraintGenerator, JavaTimeArbitraryGeneratorSet> generateJavaTimeArbitrarySet = null;
	private InstantiatorProcessor instantiatorProcessor = new JavaInstantiatorProcessor();
	private DefaultMatcherOperatorContainer<CandidateConcretePropertyResolver> candidateConcretePropertyResolvers;
	private List<TreeMatcherOperator<BuilderContextInitializer>> builderContextInitializers = new ArrayList<>();

	@SuppressWarnings({"argument"})
	FixtureMonkeyOptionsBuilder() {
		propertyGenerators = createMatcherOperatorRegistry(
			new ArrayList<>(FixtureMonkeyOptions.DEFAULT_PROPERTY_GENERATORS));
		containerPropertyGenerators = createMatcherOperatorRegistry(
			new ArrayList<>(FixtureMonkeyOptions.DEFAULT_CONTAINER_PROPERTY_GENERATORS));
		nullInjectGenerators = createMatcherOperatorRegistry(
			new ArrayList<>(FixtureMonkeyOptions.DEFAULT_NULL_INJECT_GENERATORS));
		arbitraryObjectPropertyGenerators = createMatcherOperatorRegistry(new ArrayList<>());
		propertyNameResolvers = createMatcherOperatorRegistry(new ArrayList<>());
		arbitraryContainerInfoGenerators = createMatcherOperatorRegistry(new ArrayList<>());
		arbitraryIntrospectors = createMatcherOperatorRegistry(new ArrayList<>(DEFAULT_ARBITRARY_INTROSPECTORS));
		candidateConcretePropertyResolvers = createMatcherOperatorRegistry(
			new ArrayList<>(FixtureMonkeyOptions.DEFAULT_CANDIDATE_CONCRETE_PROPERTY_RESOLVERS));

		new JdkVariantOptions().apply(this);
	}

	public FixtureMonkeyOptionsBuilder propertyGenerators(List<MatcherOperator<PropertyGenerator>> propertyGenerators) {
		this.propertyGenerators = createMatcherOperatorRegistry(propertyGenerators);
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstPropertyGenerator(
		MatcherOperator<PropertyGenerator> propertyGenerator
	) {
		propertyGenerators.addFirst(propertyGenerator);
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstPropertyGenerator(
		Matcher matcher,
		PropertyGenerator propertyGenerator
	) {
		return this.insertFirstPropertyGenerator(
			new MatcherOperator<>(matcher, propertyGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder insertFirstPropertyGenerator(
		Class<?> type,
		PropertyGenerator propertyGenerator
	) {
		return this.insertFirstPropertyGenerator(
			MatcherOperator.assignableTypeMatchOperator(type, propertyGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder defaultPropertyGenerator(PropertyGenerator propertyGenerator) {
		this.defaultPropertyGenerator = propertyGenerator;
		return this;
	}

	public FixtureMonkeyOptionsBuilder arbitraryObjectPropertyGenerators(
		List<MatcherOperator<ObjectPropertyGenerator>> arbitraryObjectPropertyGenerators
	) {
		this.arbitraryObjectPropertyGenerators = createMatcherOperatorRegistry(arbitraryObjectPropertyGenerators);
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryObjectPropertyGenerator(
		MatcherOperator<ObjectPropertyGenerator> arbitraryObjectPropertyGenerator
	) {
		this.arbitraryObjectPropertyGenerators.addFirst(arbitraryObjectPropertyGenerator);
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryObjectPropertyGenerator(
		Matcher matcher,
		ObjectPropertyGenerator objectPropertyGenerator
	) {
		return this.insertFirstArbitraryObjectPropertyGenerator(
			new MatcherOperator<>(matcher, objectPropertyGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryObjectPropertyGenerator(
		Class<?> type,
		ObjectPropertyGenerator objectPropertyGenerator
	) {
		return this.insertFirstArbitraryObjectPropertyGenerator(
			MatcherOperator.assignableTypeMatchOperator(type, objectPropertyGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder arbitraryContainerPropertyGenerators(
		List<MatcherOperator<ContainerPropertyGenerator>> arbitraryContainerPropertyGenerators
	) {
		this.containerPropertyGenerators = createMatcherOperatorRegistry(arbitraryContainerPropertyGenerators);
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryContainerPropertyGenerator(
		MatcherOperator<ContainerPropertyGenerator> arbitraryContainerPropertyGenerator
	) {

		this.containerPropertyGenerators.addFirst(arbitraryContainerPropertyGenerator);
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryContainerPropertyGenerator(
		Matcher matcher,
		ContainerPropertyGenerator containerPropertyGenerator
	) {
		return this.insertFirstArbitraryContainerPropertyGenerator(
			new MatcherOperator<>(matcher, containerPropertyGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryContainerPropertyGenerator(
		Class<?> type,
		ContainerPropertyGenerator containerPropertyGenerator
	) {
		return this.insertFirstArbitraryContainerPropertyGenerator(
			MatcherOperator.assignableTypeMatchOperator(type, containerPropertyGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder defaultObjectPropertyGenerator(
		ObjectPropertyGenerator defaultObjectPropertyGenerator
	) {
		this.defaultObjectPropertyGenerator = defaultObjectPropertyGenerator;
		return this;
	}

	public FixtureMonkeyOptionsBuilder propertyNameResolvers(
		List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers
	) {
		this.propertyNameResolvers = createMatcherOperatorRegistry(propertyNameResolvers);
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstPropertyNameResolver(
		MatcherOperator<PropertyNameResolver> propertyNameResolver
	) {
		this.propertyNameResolvers.addFirst(propertyNameResolver);
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstPropertyNameResolver(
		Matcher matcher,
		PropertyNameResolver propertyNameResolver
	) {
		return this.insertFirstPropertyNameResolver(new MatcherOperator<>(matcher, propertyNameResolver));
	}

	public FixtureMonkeyOptionsBuilder insertFirstPropertyNameResolver(
		Class<?> type,
		PropertyNameResolver propertyNameResolver
	) {
		return this.insertFirstPropertyNameResolver(
			MatcherOperator.assignableTypeMatchOperator(type, propertyNameResolver)
		);
	}

	public FixtureMonkeyOptionsBuilder defaultPropertyNameResolver(PropertyNameResolver defaultPropertyNameResolver) {
		this.defaultPropertyNameResolver = defaultPropertyNameResolver;
		return this;
	}

	public PropertyNameResolver getDefaultPropertyNameResolver() {
		return defaultPropertyNameResolver;
	}

	public FixtureMonkeyOptionsBuilder nullInjectGenerators(
		List<MatcherOperator<NullInjectGenerator>> nullInjectGenerators
	) {
		this.nullInjectGenerators = createMatcherOperatorRegistry(nullInjectGenerators);
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstNullInjectGenerators(
		MatcherOperator<NullInjectGenerator> nullInjectGenerator
	) {
		this.nullInjectGenerators.addFirst(nullInjectGenerator);
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstNullInjectGenerators(
		Matcher matcher,
		NullInjectGenerator nullInjectGenerator
	) {
		return this.insertFirstNullInjectGenerators(new MatcherOperator<>(matcher, nullInjectGenerator));
	}

	public FixtureMonkeyOptionsBuilder insertFirstNullInjectGenerators(
		Class<?> type,
		NullInjectGenerator nullInjectGenerator
	) {
		return this.insertFirstNullInjectGenerators(
			MatcherOperator.assignableTypeMatchOperator(type, nullInjectGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder defaultNullInjectGenerator(NullInjectGenerator defaultNullInjectGenerator) {
		this.defaultNullInjectGenerator = defaultNullInjectGenerator;
		return this;
	}

	public FixtureMonkeyOptionsBuilder defaultNullInjectGeneratorOperator(
		UnaryOperator<NullInjectGenerator> defaultNullInjectGeneratorOperator
	) {
		this.defaultNullInjectGeneratorOperator = defaultNullInjectGeneratorOperator;
		return this;
	}

	public FixtureMonkeyOptionsBuilder arbitraryContainerInfoGenerators(
		List<MatcherOperator<ArbitraryContainerInfoGenerator>> arbitraryContainerInfoGenerators
	) {
		this.arbitraryContainerInfoGenerators = createMatcherOperatorRegistry(arbitraryContainerInfoGenerators);
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryContainerInfoGenerator(
		MatcherOperator<ArbitraryContainerInfoGenerator> arbitraryContainerInfoGenerator
	) {
		this.arbitraryContainerInfoGenerators.addFirst(arbitraryContainerInfoGenerator);
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryContainerInfoGenerator(
		Matcher matcher,
		ArbitraryContainerInfoGenerator arbitraryContainerInfoGenerator
	) {
		return this.insertFirstArbitraryContainerInfoGenerator(
			new MatcherOperator<>(matcher, arbitraryContainerInfoGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryContainerInfoGenerator(
		Class<?> type,
		ArbitraryContainerInfoGenerator arbitraryContainerInfoGenerator
	) {
		return this.insertFirstArbitraryContainerInfoGenerator(
			MatcherOperator.assignableTypeMatchOperator(type, arbitraryContainerInfoGenerator)
		);
	}

	public FixtureMonkeyOptionsBuilder defaultArbitraryContainerInfoGenerator(
		ArbitraryContainerInfoGenerator defaultArbitraryContainerInfoGenerator) {
		this.defaultArbitraryContainerInfoGenerator = defaultArbitraryContainerInfoGenerator;
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryIntrospector(
		MatcherOperator<ArbitraryIntrospector> arbitraryIntrospector
	) {
		this.arbitraryIntrospectors.addFirst(arbitraryIntrospector);
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryIntrospector(
		Matcher matcher,
		ArbitraryIntrospector arbitraryIntrospector
	) {
		return this.insertFirstArbitraryIntrospector(
			new MatcherOperator<>(
				matcher,
				arbitraryIntrospector
			)
		);
	}

	public FixtureMonkeyOptionsBuilder insertFirstArbitraryIntrospector(
		Class<?> type,
		ArbitraryIntrospector arbitraryIntrospector
	) {
		return this.insertFirstArbitraryIntrospector(
			MatcherOperator.assignableTypeMatchOperator(type, arbitraryIntrospector)
		);
	}

	public FixtureMonkeyOptionsBuilder defaultArbitraryGenerator(
		UnaryOperator<ArbitraryGenerator> defaultArbitraryGeneratorOperator
	) {
		this.defaultArbitraryGeneratorOperator = defaultArbitraryGeneratorOperator;
		return this;
	}

	public FixtureMonkeyOptionsBuilder priorityIntrospector(
		UnaryOperator<ArbitraryIntrospector> priorityIntrospector
	) {
		this.javaDefaultArbitraryGeneratorBuilder.priorityIntrospector(priorityIntrospector);
		return this;
	}

	public FixtureMonkeyOptionsBuilder containerIntrospector(
		UnaryOperator<ArbitraryIntrospector> containerIntrospector
	) {
		this.javaDefaultArbitraryGeneratorBuilder.containerIntrospector(containerIntrospector);
		return this;
	}

	public FixtureMonkeyOptionsBuilder objectIntrospector(
		UnaryOperator<ArbitraryIntrospector> objectIntrospector
	) {
		this.javaDefaultArbitraryGeneratorBuilder.objectIntrospector(objectIntrospector);
		return this;
	}

	public FixtureMonkeyOptionsBuilder fallbackIntrospector(
		UnaryOperator<ArbitraryIntrospector> fallbackIntrospector
	) {
		this.javaDefaultArbitraryGeneratorBuilder.fallbackIntrospector(fallbackIntrospector);
		return this;
	}

	public FixtureMonkeyOptionsBuilder plugin(Plugin plugin) {
		plugin.accept(this);
		return this;
	}

	public FixtureMonkeyOptionsBuilder defaultNotNull(boolean defaultNotNull) {
		this.defaultNotNull = defaultNotNull;
		return this;
	}

	public FixtureMonkeyOptionsBuilder nullableContainer(boolean nullableContainer) {
		this.nullableContainer = nullableContainer;
		return this;
	}

	public FixtureMonkeyOptionsBuilder nullableElement(boolean nullableElement) {
		this.nullableElement = nullableElement;
		return this;
	}

	public FixtureMonkeyOptionsBuilder enableLoggingFail(boolean enableLoggingFail) {
		this.enableLoggingFail = enableLoggingFail;
		return this;
	}

	public FixtureMonkeyOptionsBuilder defaultArbitraryValidator(ArbitraryValidator arbitraryValidator) {
		this.defaultArbitraryValidator = arbitraryValidator;
		return this;
	}

	public FixtureMonkeyOptionsBuilder decomposedContainerValueFactory(
		DecomposedContainerValueFactory decomposedContainerValueFactory
	) {
		this.decomposedContainerValueFactory = decomposedContainerValueFactory;
		return this;
	}

	public FixtureMonkeyOptionsBuilder addDecomposedContainerValueFactory(
		Class<?> type,
		DecomposedContainerValueFactory additionalDecomposedContainerValueFactory
	) {
		decomposableContainerFactoryMap.put(type, additionalDecomposedContainerValueFactory);
		return this;
	}

	public FixtureMonkeyOptionsBuilder generateMaxTries(int generateMaxCount) {
		this.generateMaxTries = generateMaxCount;
		return this;
	}

	public FixtureMonkeyOptionsBuilder generateUniqueMaxTries(int generateUniqueMaxTries) {
		this.generateUniqueMaxTries = generateUniqueMaxTries;
		return this;
	}

	public FixtureMonkeyOptionsBuilder javaConstraintGenerator(JavaConstraintGenerator javaConstraintGenerator) {
		this.javaConstraintGenerator = javaConstraintGenerator;
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstJavaConstraintGeneratorCustomizer(
		UnaryOperator<JavaConstraintGenerator> javaConstraintGeneratorCustomizer
	) {
		this.javaConstraintGeneratorCustomizers.add(javaConstraintGeneratorCustomizer);
		return this;
	}

	public FixtureMonkeyOptionsBuilder javaTypeArbitraryGeneratorSet(
		Function<JavaConstraintGenerator, JavaTypeArbitraryGeneratorSet> generateJavaTypeArbitrarySet
	) {
		this.generateJavaTypeArbitrarySet = generateJavaTypeArbitrarySet;
		return this;
	}

	public FixtureMonkeyOptionsBuilder javaTimeArbitraryGeneratorSet(
		Function<JavaConstraintGenerator, JavaTimeArbitraryGeneratorSet> generateJavaTimeArbitrarySet
	) {
		this.generateJavaTimeArbitrarySet = generateJavaTimeArbitrarySet;
		return this;
	}

	public FixtureMonkeyOptionsBuilder instantiatorProcessor(
		InstantiatorProcessor instantiatorProcessor
	) {
		this.instantiatorProcessor = instantiatorProcessor;
		return this;
	}

	public FixtureMonkeyOptionsBuilder candidateConcretePropertyResolvers(
		List<MatcherOperator<CandidateConcretePropertyResolver>> candidateConcretePropertyResolvers
	) {
		this.candidateConcretePropertyResolvers = createMatcherOperatorRegistry(candidateConcretePropertyResolvers);
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstCandidateConcretePropertyResolvers(
		MatcherOperator<CandidateConcretePropertyResolver> candidateConcretePropertyResolver
	) {
		this.candidateConcretePropertyResolvers.addFirst(candidateConcretePropertyResolver);
		return this;
	}

	public FixtureMonkeyOptionsBuilder builderContextInitializers(
		List<TreeMatcherOperator<BuilderContextInitializer>> builderContextInitializers
	) {
		this.builderContextInitializers = builderContextInitializers;
		return this;
	}

	public FixtureMonkeyOptionsBuilder insertFirstBuilderContextInitializer(
		TreeMatcherOperator<BuilderContextInitializer> builderContextInitializer
	) {
		this.builderContextInitializers = insertFirst(
			this.builderContextInitializers,
			builderContextInitializer
		);
		return this;
	}

	public FixtureMonkeyOptions build() {
		ObjectPropertyGenerator defaultObjectPropertyGenerator = defaultIfNull(
			this.defaultObjectPropertyGenerator,
			() -> FixtureMonkeyOptions.DEFAULT_OBJECT_PROPERTY_GENERATOR
		);
		PropertyNameResolver defaultPropertyNameResolver = defaultIfNull(
			this.defaultPropertyNameResolver,
			() -> FixtureMonkeyOptions.DEFAULT_PROPERTY_NAME_RESOLVER
		);

		NullInjectGenerator defaultNullInjectGenerator = defaultIfNull(
			this.defaultNullInjectGenerator,
			() -> new DefaultNullInjectGenerator(
				DEFAULT_NULL_INJECT,
				nullableContainer,
				defaultNotNull,
				nullableElement,
				new HashSet<>(DEFAULT_NULLABLE_ANNOTATION_TYPES),
				new HashSet<>(DEFAULT_NOTNULL_ANNOTATION_TYPES)
			)
		);

		if (defaultNullInjectGeneratorOperator != null) {
			defaultNullInjectGenerator = defaultNullInjectGeneratorOperator.apply(defaultNullInjectGenerator);
		}

		ArbitraryContainerInfoGenerator defaultArbitraryContainerInfoGenerator = defaultIfNull(
			this.defaultArbitraryContainerInfoGenerator,
			() -> context -> new ArbitraryContainerInfo(0, FixtureMonkeyOptions.DEFAULT_ARBITRARY_CONTAINER_MAX_SIZE)
		);

		for (Function<JavaConstraintGenerator, JavaConstraintGenerator> it : javaConstraintGeneratorCustomizers) {
			this.javaConstraintGenerator = it.apply(this.javaConstraintGenerator);
		}

		JavaConstraintGenerator resolvedJavaConstraintGenerator = this.javaConstraintGenerator;

		JavaArbitraryResolver javaArbitraryResolver = defaultIfNull(
			this.javaArbitraryResolver,
			() -> new JqwikJavaArbitraryResolver(resolvedJavaConstraintGenerator)
		);

		this.generateJavaTypeArbitrarySet = defaultIfNull(
			this.generateJavaTypeArbitrarySet,
			() -> constraintGenerator ->
				new JqwikJavaTypeArbitraryGeneratorSet(
					this.javaTypeArbitraryGenerator,
					javaArbitraryResolver
				)
		);

		javaDefaultArbitraryGeneratorBuilder.javaTypeArbitraryGeneratorSet(
			generateJavaTypeArbitrarySet.apply(resolvedJavaConstraintGenerator)
		);

		JavaTimeArbitraryResolver javaTimeArbitraryResolver = defaultIfNull(
			this.javaTimeArbitraryResolver,
			() -> new JqwikJavaTimeArbitraryResolver(resolvedJavaConstraintGenerator)
		);

		this.generateJavaTimeArbitrarySet = defaultIfNull(
			this.generateJavaTimeArbitrarySet,
			() -> constraintGenerator ->
				new JqwikJavaTimeArbitraryGeneratorSet(
					this.javaTimeTypeArbitraryGenerator,
					javaTimeArbitraryResolver
				)
		);

		javaDefaultArbitraryGeneratorBuilder.javaTimeArbitraryGeneratorSet(
			generateJavaTimeArbitrarySet.apply(resolvedJavaConstraintGenerator)
		);

		ArbitraryGenerator defaultArbitraryGenerator =
			defaultIfNull(this.defaultArbitraryGenerator, this.javaDefaultArbitraryGeneratorBuilder::build);

		List<ArbitraryIntrospector> typedArbitraryIntrospectors = arbitraryIntrospectors
			.getList()
			.stream()
			.map(TypedArbitraryIntrospector::new)
			.collect(Collectors.toList());

		ArbitraryGenerator introspectedGenerator =
			new IntrospectedArbitraryGenerator(new MatchArbitraryIntrospector(typedArbitraryIntrospectors));

		defaultArbitraryGenerator = new MatchArbitraryGenerator(
			Arrays.asList(introspectedGenerator, defaultArbitraryGenerator)
		);

		DecomposedContainerValueFactory decomposedContainerValueFactory = new DefaultDecomposedContainerValueFactory(
			obj -> {
				Class<?> actualType = obj.getClass();
				for (
					Map.Entry<Class<?>, DecomposedContainerValueFactory> entry :
					this.decomposableContainerFactoryMap.entrySet()
				) {
					Class<?> type = entry.getKey();
					DecomposableJavaContainer decomposedValue;
					try {
						decomposedValue = entry.getValue().from(obj);
					} catch (IllegalArgumentException ex) {
						continue;
					}

					if (type.isAssignableFrom(actualType)) {
						return decomposedValue;
					}
				}
				return this.decomposedContainerValueFactory.from(obj);
			}
		);

		defaultArbitraryGenerator = defaultArbitraryGeneratorOperator.apply(defaultArbitraryGenerator);

		return new FixtureMonkeyOptions(
			propertyGenerators,
			this.defaultPropertyGenerator,
			arbitraryObjectPropertyGenerators,
			defaultObjectPropertyGenerator,
			containerPropertyGenerators,
			propertyNameResolvers,
			defaultPropertyNameResolver,
			this.nullInjectGenerators,
			defaultNullInjectGenerator,
			this.arbitraryContainerInfoGenerators,
			defaultArbitraryContainerInfoGenerator,
			defaultArbitraryGenerator,
			this.defaultArbitraryValidator,
			decomposedContainerValueFactory,
			this.generateMaxTries,
			this.generateUniqueMaxTries,
			resolvedJavaConstraintGenerator,
			this.instantiatorProcessor,
			this.candidateConcretePropertyResolvers,
			this.enableLoggingFail,
			this.builderContextInitializers
		);
	}

	private <T> DefaultMatcherOperatorContainer<T> createMatcherOperatorRegistry(
		List<MatcherOperator<T>> matcherOperators) {
		DefaultMatcherOperatorContainer<T> registry = new DefaultMatcherOperatorContainer<>();
		matcherOperators.forEach(registry::addLast);

		return registry;
	}

	private static <T> T defaultIfNull(@Nullable T obj, Supplier<T> defaultValue) {
		return obj != null ? obj : defaultValue.get();
	}

	private static <T> List<T> insertFirst(List<T> list, T value) {
		List<T> result = new ArrayList<>();
		result.add(value);
		result.addAll(list);
		return result;
	}
}
