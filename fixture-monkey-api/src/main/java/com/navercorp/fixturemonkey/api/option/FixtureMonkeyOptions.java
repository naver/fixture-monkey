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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

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
import com.navercorp.fixturemonkey.api.generator.MapContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.MapEntryElementContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.NoArgumentInterfaceJavaMethodPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.NullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.OptionalContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.SetContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.SingleValueObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.StreamContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.instantiator.InstantiatorProcessor;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.introspector.NullArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.DoubleGenericTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.ExactPropertyMatcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.matcher.Matchers;
import com.navercorp.fixturemonkey.api.matcher.SingleGenericTypeMatcher;
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
	public static final List<MatcherOperator<ObjectPropertyGenerator>> DEFAULT_OBJECT_PROPERTY_GENERATORS =
		getDefaultObjectPropertyGenerators();
	public static final List<MatcherOperator<ContainerPropertyGenerator>> DEFAULT_CONTAINER_PROPERTY_GENERATORS =
		getDefaultContainerPropertyGenerators();
	public static final List<MatcherOperator<ArbitraryIntrospector>> DEFAULT_ARBITRARY_INTROSPECTORS =
		Arrays.asList(
			MatcherOperator.exactTypeMatchOperator(UnidentifiableType.class, NullArbitraryIntrospector.INSTANCE),
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
	public static final FixtureMonkeyOptions DEFAULT_GENERATE_OPTIONS = FixtureMonkeyOptions.builder().build();
	public static final int DEFAULT_MAX_UNIQUE_GENERATION_COUNT = 1_000;

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

	private final List<MatcherOperator<PropertyGenerator>> propertyGenerators;
	private final PropertyGenerator defaultPropertyGenerator;
	private final List<MatcherOperator<ObjectPropertyGenerator>> objectPropertyGenerators;
	private final ObjectPropertyGenerator defaultObjectPropertyGenerator;
	private final List<MatcherOperator<ContainerPropertyGenerator>> containerPropertyGenerators;
	private final List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers;
	private final PropertyNameResolver defaultPropertyNameResolver;
	private final List<MatcherOperator<NullInjectGenerator>> nullInjectGenerators;
	private final NullInjectGenerator defaultNullInjectGenerator;
	private final List<MatcherOperator<ArbitraryContainerInfoGenerator>> arbitraryContainerInfoGenerators;
	private final ArbitraryContainerInfoGenerator defaultArbitraryContainerInfoGenerator;
	private final ArbitraryGenerator defaultArbitraryGenerator;
	private final ArbitraryValidator defaultArbitraryValidator;
	private final DecomposedContainerValueFactory decomposedContainerValueFactory;
	private final int generateMaxTries;
	private final int generateUniqueMaxTries;
	private final JavaConstraintGenerator javaConstraintGenerator;
	private final InstantiatorProcessor instantiatorProcessor;

	public FixtureMonkeyOptions(
		List<MatcherOperator<PropertyGenerator>> propertyGenerators,
		PropertyGenerator defaultPropertyGenerator,
		List<MatcherOperator<ObjectPropertyGenerator>> objectPropertyGenerators,
		ObjectPropertyGenerator defaultObjectPropertyGenerator,
		List<MatcherOperator<ContainerPropertyGenerator>> containerPropertyGenerators,
		List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers,
		PropertyNameResolver defaultPropertyNameResolver,
		List<MatcherOperator<NullInjectGenerator>> nullInjectGenerators,
		NullInjectGenerator defaultNullInjectGenerator,
		List<MatcherOperator<ArbitraryContainerInfoGenerator>> arbitraryContainerInfoGenerators,
		ArbitraryContainerInfoGenerator defaultArbitraryContainerInfoGenerator,
		ArbitraryGenerator defaultArbitraryGenerator,
		ArbitraryValidator defaultArbitraryValidator,
		DecomposedContainerValueFactory decomposedContainerValueFactory,
		int generateMaxTries,
		int generateUniqueMaxTries,
		JavaConstraintGenerator javaConstraintGenerator,
		InstantiatorProcessor instantiatorProcessor
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
	}

	public static FixtureMonkeyOptionsBuilder builder() {
		return new FixtureMonkeyOptionsBuilder();
	}

	public List<MatcherOperator<PropertyGenerator>> getPropertyGenerators() {
		return propertyGenerators;
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
		return objectPropertyGenerators;
	}

	public ObjectPropertyGenerator getObjectPropertyGenerator(Property property) {
		return this.getObjectPropertyGenerators().stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(this.getDefaultObjectPropertyGenerator());
	}

	public ObjectPropertyGenerator getDefaultObjectPropertyGenerator() {
		return defaultObjectPropertyGenerator;
	}

	public List<MatcherOperator<ContainerPropertyGenerator>> getContainerPropertyGenerators() {
		return containerPropertyGenerators;
	}

	@Nullable
	public ContainerPropertyGenerator getContainerPropertyGenerator(Property property) {
		return this.getContainerPropertyGenerators().stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(null);
	}

	public List<MatcherOperator<PropertyNameResolver>> getPropertyNameResolvers() {
		return this.propertyNameResolvers;
	}

	public PropertyNameResolver getPropertyNameResolver(Property property) {
		return this.getPropertyNameResolvers().stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(this.getDefaultPropertyNameResolver());
	}

	public PropertyNameResolver getDefaultPropertyNameResolver() {
		return this.defaultPropertyNameResolver;
	}

	public List<MatcherOperator<NullInjectGenerator>> getNullInjectGenerators() {
		return this.nullInjectGenerators;
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
		return this.arbitraryContainerInfoGenerators;
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

	public FixtureMonkeyOptionsBuilder toBuilder() {
		return builder()
			.defaultPropertyGenerator(defaultPropertyGenerator)
			.arbitraryObjectPropertyGenerators(objectPropertyGenerators)
			.defaultObjectPropertyGenerator(defaultObjectPropertyGenerator)
			.arbitraryContainerPropertyGenerators(containerPropertyGenerators)
			.propertyNameResolvers(new ArrayList<>(this.propertyNameResolvers))
			.defaultPropertyNameResolver(this.defaultPropertyNameResolver)
			.nullInjectGenerators(new ArrayList<>(this.nullInjectGenerators))
			.defaultNullInjectGenerator(this.defaultNullInjectGenerator)
			.arbitraryContainerInfoGenerators(new ArrayList<>(this.arbitraryContainerInfoGenerators))
			.defaultArbitraryContainerInfoGenerator(this.defaultArbitraryContainerInfoGenerator)
			.defaultArbitraryValidator(defaultArbitraryValidator)
			.decomposedContainerValueFactory(decomposedContainerValueFactory)
			.javaConstraintGenerator(javaConstraintGenerator)
			.instantiatorProcessor(instantiatorProcessor);
	}

	private static List<MatcherOperator<ObjectPropertyGenerator>> getDefaultObjectPropertyGenerators(
	) {
		return Arrays.asList(
			new MatcherOperator<>(
				property -> {
					Class<?> actualType = Types.getActualType(property.getType());
					return actualType.isPrimitive()
						|| DEFAULT_JAVA_PACKAGES.stream()
						.anyMatch(actualType.getPackage().getName()::startsWith);
				},
				SingleValueObjectPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(Matchers.ENUM_TYPE_MATCHER, SingleValueObjectPropertyGenerator.INSTANCE)
		);
	}

	private static List<MatcherOperator<ContainerPropertyGenerator>> getDefaultContainerPropertyGenerators() {
		return Arrays.asList(
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

	private static List<MatcherOperator<PropertyGenerator>> getDefaultPropertyGenerators() {
		return Collections.singletonList(
			new MatcherOperator<>(
				p -> Modifier.isInterface(Types.getActualType(p.getType()).getModifiers()),
				new NoArgumentInterfaceJavaMethodPropertyGenerator()
			)
		);
	}
}
