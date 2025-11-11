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

import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.NOT_NULL_INJECT;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.constraint.JavaConstraintGenerator;
import com.navercorp.fixturemonkey.api.container.DecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.ArrayContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.DefaultObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.DefaultSingleContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.EntryContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.FunctionalInterfaceContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.MapContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.MapEntryElementContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.NoArgumentInterfaceJavaMethodPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.NullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.OptionalContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.SetContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.StreamContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.instantiator.InstantiatorProcessor;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.introspector.ConstantIntrospector;
import com.navercorp.fixturemonkey.api.introspector.NullArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.DoubleGenericTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.ExactPropertyMatcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperatorRetriever;
import com.navercorp.fixturemonkey.api.matcher.Matchers;
import com.navercorp.fixturemonkey.api.matcher.SingleGenericTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.TreeMatcherOperator;
import com.navercorp.fixturemonkey.api.property.CandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.property.CompositeCandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.property.ConcreteTypeCandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.api.type.Types.GeneratingWildcardType;
import com.navercorp.fixturemonkey.api.type.Types.UnidentifiableType;
import com.navercorp.fixturemonkey.api.validator.ArbitraryValidator;

@API(since = "0.6.0", status = Status.MAINTAINED)
public final class FixtureMonkeyOptions {
	private static final List<String> DEFAULT_JAVA_PACKAGES;
	public static final List<MatcherOperator<ContainerPropertyGenerator>> DEFAULT_CONTAINER_PROPERTY_GENERATORS =
		getDefaultContainerPropertyGenerators();
	public static final List<MatcherOperator<ArbitraryIntrospector>> DEFAULT_ARBITRARY_INTROSPECTORS =
		Arrays.asList(
			MatcherOperator.exactTypeMatchOperator(UnidentifiableType.class, NullArbitraryIntrospector.INSTANCE),
			new MatcherOperator<>(
				ConstantIntrospector.INSTANCE,
				ConstantIntrospector.INSTANCE
			),
			MatcherOperator.exactTypeMatchOperator(
				GeneratingWildcardType.class,
				context -> new ArbitraryIntrospectorResult(CombinableArbitrary.from(new Object()))
			)
		);
	public static final ObjectPropertyGenerator DEFAULT_OBJECT_PROPERTY_GENERATOR =
		DefaultObjectPropertyGenerator.INSTANCE;
	public static final PropertyNameResolver DEFAULT_PROPERTY_NAME_RESOLVER = PropertyNameResolver.IDENTITY;
	public static final int DEFAULT_ARBITRARY_CONTAINER_MAX_SIZE = 3;
	public static final List<MatcherOperator<PropertyGenerator>> DEFAULT_PROPERTY_GENERATORS =
		getDefaultPropertyGenerators();
	public static final List<MatcherOperator<CandidateConcretePropertyResolver>>
		DEFAULT_CANDIDATE_CONCRETE_PROPERTY_RESOLVERS = Arrays.asList(
		MatcherOperator.exactTypeMatchOperator(
			List.class,
			new ConcreteTypeCandidateConcretePropertyResolver<>(Collections.singletonList(ArrayList.class))
		),
		MatcherOperator.exactTypeMatchOperator(
			Set.class,
			new ConcreteTypeCandidateConcretePropertyResolver<>(Collections.singletonList(HashSet.class))
		)
	);
	public static final int DEFAULT_MAX_UNIQUE_GENERATION_COUNT = 1_000;
	public static final List<MatcherOperator<NullInjectGenerator>> DEFAULT_NULL_INJECT_GENERATORS =
		Collections.singletonList(
			new MatcherOperator<>(
				ConstantIntrospector.INSTANCE,
				context -> NOT_NULL_INJECT
			)
		);

	static {
		List<String> defaultJavaPackages = new ArrayList<>();
		defaultJavaPackages.add("java.lang");
		defaultJavaPackages.add("java.net");
		defaultJavaPackages.add("java.util");
		defaultJavaPackages.add("java.math");
		defaultJavaPackages.add("java.time");
		defaultJavaPackages.add("jdk.internal.reflect");
		defaultJavaPackages.add("sun.reflect");
		defaultJavaPackages.add("sun.util");
		DEFAULT_JAVA_PACKAGES = Collections.unmodifiableList(defaultJavaPackages);
	}

	private final MatcherOperatorRetriever<PropertyGenerator> propertyGenerators;
	private final PropertyGenerator defaultPropertyGenerator;
	private final MatcherOperatorRetriever<ObjectPropertyGenerator> objectPropertyGenerators;
	private final ObjectPropertyGenerator defaultObjectPropertyGenerator;
	private final MatcherOperatorRetriever<ContainerPropertyGenerator> containerPropertyGenerators;
	private final MatcherOperatorRetriever<PropertyNameResolver> propertyNameResolvers;
	private final PropertyNameResolver defaultPropertyNameResolver;
	private final MatcherOperatorRetriever<NullInjectGenerator> nullInjectGenerators;
	private final NullInjectGenerator defaultNullInjectGenerator;
	private final MatcherOperatorRetriever<ArbitraryContainerInfoGenerator> arbitraryContainerInfoGenerators;
	private final ArbitraryContainerInfoGenerator defaultArbitraryContainerInfoGenerator;
	private final ArbitraryGenerator defaultArbitraryGenerator;
	private final ArbitraryValidator defaultArbitraryValidator;
	private final DecomposedContainerValueFactory decomposedContainerValueFactory;
	private final int generateMaxTries;
	private final int generateUniqueMaxTries;
	private final JavaConstraintGenerator javaConstraintGenerator;
	private final InstantiatorProcessor instantiatorProcessor;
	private final MatcherOperatorRetriever<CandidateConcretePropertyResolver> candidateConcretePropertyResolvers;
	private final boolean enableLoggingFail;
	private final List<TreeMatcherOperator<BuilderContextInitializer>> builderContextInitializers;

	public FixtureMonkeyOptions(
		MatcherOperatorRetriever<PropertyGenerator> propertyGenerators,
		PropertyGenerator defaultPropertyGenerator,
		MatcherOperatorRetriever<ObjectPropertyGenerator> objectPropertyGenerators,
		ObjectPropertyGenerator defaultObjectPropertyGenerator,
		MatcherOperatorRetriever<ContainerPropertyGenerator> containerPropertyGenerators,
		MatcherOperatorRetriever<PropertyNameResolver> propertyNameResolvers,
		PropertyNameResolver defaultPropertyNameResolver,
		MatcherOperatorRetriever<NullInjectGenerator> nullInjectGenerators,
		NullInjectGenerator defaultNullInjectGenerator,
		MatcherOperatorRetriever<ArbitraryContainerInfoGenerator> arbitraryContainerInfoGenerators,
		ArbitraryContainerInfoGenerator defaultArbitraryContainerInfoGenerator,
		ArbitraryGenerator defaultArbitraryGenerator,
		ArbitraryValidator defaultArbitraryValidator,
		DecomposedContainerValueFactory decomposedContainerValueFactory,
		int generateMaxTries,
		int generateUniqueMaxTries,
		JavaConstraintGenerator javaConstraintGenerator,
		InstantiatorProcessor instantiatorProcessor,
		MatcherOperatorRetriever<CandidateConcretePropertyResolver> candidateConcretePropertyResolvers,
		boolean enableLoggingFail,
		List<TreeMatcherOperator<BuilderContextInitializer>> builderContextCustomizer
	) {
		this.propertyGenerators = propertyGenerators;
		this.defaultPropertyGenerator = defaultPropertyGenerator;
		this.objectPropertyGenerators = objectPropertyGenerators;
		this.defaultObjectPropertyGenerator = defaultObjectPropertyGenerator;
		this.containerPropertyGenerators = containerPropertyGenerators;
		this.propertyNameResolvers = propertyNameResolvers;
		this.defaultPropertyNameResolver = defaultPropertyNameResolver;
		this.nullInjectGenerators = nullInjectGenerators;
		this.defaultNullInjectGenerator = defaultNullInjectGenerator;
		this.arbitraryContainerInfoGenerators = arbitraryContainerInfoGenerators;
		this.defaultArbitraryContainerInfoGenerator = defaultArbitraryContainerInfoGenerator;
		this.defaultArbitraryGenerator = defaultArbitraryGenerator;
		this.defaultArbitraryValidator = defaultArbitraryValidator;
		this.decomposedContainerValueFactory = decomposedContainerValueFactory;
		this.generateMaxTries = generateMaxTries;
		this.generateUniqueMaxTries = generateUniqueMaxTries;
		this.javaConstraintGenerator = javaConstraintGenerator;
		this.instantiatorProcessor = instantiatorProcessor;
		this.candidateConcretePropertyResolvers = candidateConcretePropertyResolvers;
		this.enableLoggingFail = enableLoggingFail;
		this.builderContextInitializers = builderContextCustomizer;
	}

	public static FixtureMonkeyOptionsBuilder builder() {
		return new FixtureMonkeyOptionsBuilder();
	}

	public List<MatcherOperator<PropertyGenerator>> getPropertyGenerators() {
		return propertyGenerators.getList();
	}

	@Nullable
	public PropertyGenerator getOptionalPropertyGenerator(Property property) {
		return this.getPropertyGenerators().stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(null);
	}

	public PropertyGenerator getDefaultPropertyGenerator() {
		return defaultPropertyGenerator;
	}

	public List<MatcherOperator<ObjectPropertyGenerator>> getObjectPropertyGenerators() {
		return objectPropertyGenerators.getList();
	}

	public ObjectPropertyGenerator getObjectPropertyGenerator(Property property) {
		return objectPropertyGenerators.getListByProperty(property)
			.stream()
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(this.getDefaultObjectPropertyGenerator());
	}

	public ObjectPropertyGenerator getDefaultObjectPropertyGenerator() {
		return defaultObjectPropertyGenerator;
	}

	public List<MatcherOperator<ContainerPropertyGenerator>> getContainerPropertyGenerators() {
		return containerPropertyGenerators.getList();
	}

	@Nullable
	public ContainerPropertyGenerator getContainerPropertyGenerator(Property property) {
		return containerPropertyGenerators.getListByProperty(property)
			.stream()
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(null);
	}

	public List<MatcherOperator<PropertyNameResolver>> getPropertyNameResolvers() {
		return this.propertyNameResolvers.getList();
	}

	public PropertyNameResolver getPropertyNameResolver(Property property) {
		return this.propertyNameResolvers.getListByProperty(property).stream()
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(this.getDefaultPropertyNameResolver());
	}

	public PropertyNameResolver getDefaultPropertyNameResolver() {
		return this.defaultPropertyNameResolver;
	}

	public List<MatcherOperator<NullInjectGenerator>> getNullInjectGenerators() {
		return this.nullInjectGenerators.getList();
	}

	public NullInjectGenerator getNullInjectGenerator(Property property) {
		return this.getNullInjectGenerators().stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(this.getDefaultNullInjectGenerator());
	}

	public NullInjectGenerator getDefaultNullInjectGenerator() {
		return this.defaultNullInjectGenerator;
	}

	public List<MatcherOperator<ArbitraryContainerInfoGenerator>> getArbitraryContainerInfoGenerators() {
		return this.arbitraryContainerInfoGenerators.getList();
	}

	public ArbitraryContainerInfoGenerator getArbitraryContainerInfoGenerator(Property property) {
		return this.getArbitraryContainerInfoGenerators().stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(this.getDefaultArbitraryContainerInfoGenerator());
	}

	public ArbitraryContainerInfoGenerator getDefaultArbitraryContainerInfoGenerator() {
		return this.defaultArbitraryContainerInfoGenerator;
	}

	public ArbitraryGenerator getDefaultArbitraryGenerator() {
		return this.defaultArbitraryGenerator;
	}

	public ArbitraryValidator getDefaultArbitraryValidator() {
		return defaultArbitraryValidator;
	}

	public DecomposedContainerValueFactory getDecomposedContainerValueFactory() {
		return decomposedContainerValueFactory;
	}

	public int getGenerateMaxTries() {
		return generateMaxTries;
	}

	public int getGenerateUniqueMaxTries() {
		return generateUniqueMaxTries;
	}

	public JavaConstraintGenerator getJavaConstraintGenerator() {
		return javaConstraintGenerator;
	}

	public InstantiatorProcessor getInstantiatorProcessor() {
		return instantiatorProcessor;
	}

	public boolean isEnableLoggingFail() {
		return enableLoggingFail;
	}

	public List<TreeMatcherOperator<BuilderContextInitializer>> getBuilderContextInitializers() {
		return builderContextInitializers;
	}

	public List<MatcherOperator<CandidateConcretePropertyResolver>> getCandidateConcretePropertyResolvers() {
		return candidateConcretePropertyResolvers.getList();
	}

	/**
	 * Use {@link #getCandidateConcretePropertyResolvers()} instead.
	 */
	@Nullable
	@Deprecated
	public CandidateConcretePropertyResolver getCandidateConcretePropertyResolver(Property property) {
		List<CandidateConcretePropertyResolver> candidateConcretePropertyResolverList =
			this.candidateConcretePropertyResolvers.getListByProperty(property)
				.stream()
				.map(MatcherOperator::getOperator)
				.collect(Collectors.toList());

		if (candidateConcretePropertyResolverList.isEmpty()) {
			return null;
		}

		return new CompositeCandidateConcretePropertyResolver(candidateConcretePropertyResolverList);
	}

	public FixtureMonkeyOptionsBuilder toBuilder() {
		return builder()
			.defaultPropertyGenerator(defaultPropertyGenerator)
			.arbitraryObjectPropertyGenerators(this.objectPropertyGenerators.getList())
			.defaultObjectPropertyGenerator(defaultObjectPropertyGenerator)
			.arbitraryContainerPropertyGenerators(this.containerPropertyGenerators.getList())
			.propertyNameResolvers(this.propertyNameResolvers.getList())
			.defaultPropertyNameResolver(this.defaultPropertyNameResolver)
			.nullInjectGenerators(new ArrayList<>(this.nullInjectGenerators.getList()))
			.defaultNullInjectGenerator(this.defaultNullInjectGenerator)
			.arbitraryContainerInfoGenerators(new ArrayList<>(this.arbitraryContainerInfoGenerators.getList()))
			.defaultArbitraryContainerInfoGenerator(this.defaultArbitraryContainerInfoGenerator)
			.defaultArbitraryValidator(defaultArbitraryValidator)
			.decomposedContainerValueFactory(decomposedContainerValueFactory)
			.javaConstraintGenerator(javaConstraintGenerator)
			.instantiatorProcessor(instantiatorProcessor)
			.candidateConcretePropertyResolvers(new ArrayList<>(candidateConcretePropertyResolvers.getList()))
			.builderContextInitializers(builderContextInitializers);
	}

	private static List<MatcherOperator<ContainerPropertyGenerator>> getDefaultContainerPropertyGenerators() {
		return Arrays.asList(
			new MatcherOperator<>(
				new AssignableTypeMatcher(Supplier.class).intersect(new SingleGenericTypeMatcher())
					.union(new AssignableTypeMatcher(Function.class).intersect(new DoubleGenericTypeMatcher())),
				FunctionalInterfaceContainerPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(
				new AssignableTypeMatcher(Optional.class).intersect(new SingleGenericTypeMatcher()),
				OptionalContainerPropertyGenerator.INSTANCE
			),
			MatcherOperator.exactTypeMatchOperator(
				OptionalInt.class,
				OptionalContainerPropertyGenerator.INSTANCE
			),
			MatcherOperator.exactTypeMatchOperator(
				OptionalLong.class,
				OptionalContainerPropertyGenerator.INSTANCE
			),
			MatcherOperator.exactTypeMatchOperator(
				OptionalDouble.class,
				OptionalContainerPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(
				new AssignableTypeMatcher(Stream.class).intersect(new SingleGenericTypeMatcher()),
				StreamContainerPropertyGenerator.INSTANCE
			),
			MatcherOperator.assignableTypeMatchOperator(
				IntStream.class,
				StreamContainerPropertyGenerator.INSTANCE
			),
			MatcherOperator.assignableTypeMatchOperator(
				LongStream.class,
				StreamContainerPropertyGenerator.INSTANCE
			),
			MatcherOperator.assignableTypeMatchOperator(
				DoubleStream.class,
				StreamContainerPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(
				new AssignableTypeMatcher(Set.class).intersect(new SingleGenericTypeMatcher()),
				SetContainerPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(
				new AssignableTypeMatcher(Iterable.class).intersect(new SingleGenericTypeMatcher()),
				DefaultSingleContainerPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(
				new AssignableTypeMatcher(Iterator.class).intersect(new SingleGenericTypeMatcher()),
				DefaultSingleContainerPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(
				new AssignableTypeMatcher(Map.class).intersect(new DoubleGenericTypeMatcher()),
				MapContainerPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(
				new AssignableTypeMatcher(Entry.class).intersect(new DoubleGenericTypeMatcher()),
				EntryContainerPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(
				property -> Types.getActualType(property.getType()).isArray()
					|| GenericArrayType.class.isAssignableFrom(property.getType().getClass()),
				ArrayContainerPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(
				new ExactPropertyMatcher(MapEntryElementProperty.class),
				MapEntryElementContainerPropertyGenerator.INSTANCE
			)
		);
	}

	@SuppressWarnings("nullness")
	private static List<MatcherOperator<PropertyGenerator>> getDefaultPropertyGenerators() {
		return Arrays.asList(
			new MatcherOperator<>(ConstantIntrospector.INSTANCE, EmptyPropertyGenerator.INSTANCE),
			new MatcherOperator<>(
				property -> {
					Class<?> actualType = Types.getActualType(property.getType());
					return actualType.isPrimitive()
						|| DEFAULT_JAVA_PACKAGES.stream()
						.anyMatch(actualType.getPackage().getName()::startsWith);
				},
				EmptyPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(Matchers.ENUM_TYPE_MATCHER, EmptyPropertyGenerator.INSTANCE),
			new MatcherOperator<>(
				p -> Modifier.isInterface(Types.getActualType(p.getType()).getModifiers()),
				new NoArgumentInterfaceJavaMethodPropertyGenerator()
			)
		);
	}

	private static class EmptyPropertyGenerator implements PropertyGenerator {
		private static final PropertyGenerator INSTANCE = new EmptyPropertyGenerator();

		@Override
		public List<Property> generateChildProperties(Property property) {
			return Collections.emptyList();
		}
	}
}
