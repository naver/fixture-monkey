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
import com.navercorp.fixturemonkey.api.generator.NullObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.OptionalContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.SetContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.SingleValueObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.StreamContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.DoubleGenericTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.matcher.Matchers;
import com.navercorp.fixturemonkey.api.matcher.SingleGenericTypeMatcher;
import com.navercorp.fixturemonkey.api.property.MapEntryElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.api.type.Types.UnidentifiableType;
import com.navercorp.fixturemonkey.api.validator.ArbitraryValidator;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class GenerateOptions {
	private static final List<String> DEFAULT_JAVA_PACKAGES;
	public static final List<MatcherOperator<ObjectPropertyGenerator>> DEFAULT_OBJECT_PROPERTY_GENERATORS =
		getDefaultObjectPropertyGenerators();
	public static final List<MatcherOperator<ContainerPropertyGenerator>> DEFAULT_CONTAINER_PROPERTY_GENERATORS =
		getDefaultContainerPropertyGenerators();
	public static final ObjectPropertyGenerator DEFAULT_OBJECT_PROPERTY_GENERATOR =
		DefaultObjectPropertyGenerator.INSTANCE;
	public static final PropertyNameResolver DEFAULT_PROPERTY_NAME_RESOLVER = PropertyNameResolver.IDENTITY;
	public static final int DEFAULT_ARBITRARY_CONTAINER_MAX_SIZE = 3;
	public static final List<MatcherOperator<PropertyGenerator>> DEFAULT_PROPERTY_GENERATORS =
		getDefaultPropertyGenerators();
	public static final GenerateOptions DEFAULT_GENERATE_OPTIONS = GenerateOptions.builder().build();

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
	private final List<MatcherOperator<ArbitraryGenerator>> arbitraryGenerators;
	private final ArbitraryGenerator defaultArbitraryGenerator;
	private final ArbitraryValidator defaultArbitraryValidator;

	@SuppressWarnings("rawtypes")
	public GenerateOptions(
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
		List<MatcherOperator<ArbitraryGenerator>> arbitraryGenerators,
		ArbitraryGenerator defaultArbitraryGenerator,
		ArbitraryValidator defaultArbitraryValidator
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
		this.arbitraryGenerators = arbitraryGenerators;
		this.defaultArbitraryGenerator = defaultArbitraryGenerator;
		this.defaultArbitraryValidator = defaultArbitraryValidator;
	}

	public static GenerateOptionsBuilder builder() {
		return new GenerateOptionsBuilder();
	}

	public List<MatcherOperator<PropertyGenerator>> getPropertyGenerators() {
		return propertyGenerators;
	}

	public PropertyGenerator getPropertyGenerator(Property property) {
		return this.getPropertyGenerators().stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(this.getDefaultPropertyGenerator());
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

	public List<MatcherOperator<ArbitraryGenerator>> getArbitraryGenerators() {
		return this.arbitraryGenerators;
	}

	public ArbitraryGenerator getArbitraryGenerator(Property property) {
		return this.getArbitraryGenerators().stream()
			.filter(it -> it.match(property))
			.map(MatcherOperator::getOperator)
			.findFirst()
			.orElse(this.getDefaultArbitraryGenerator());
	}

	public ArbitraryGenerator getDefaultArbitraryGenerator() {
		return this.defaultArbitraryGenerator;
	}

	public ArbitraryValidator getDefaultArbitraryValidator() {
		return defaultArbitraryValidator;
	}

	public GenerateOptionsBuilder toBuilder() {
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
			.arbitraryGenerators(new ArrayList<>(this.arbitraryGenerators))
			.defaultArbitraryGenerator(this.defaultArbitraryGenerator)
			.defaultArbitraryValidator(defaultArbitraryValidator);
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
			new MatcherOperator<>(Matchers.ENUM_TYPE_MATCHER, SingleValueObjectPropertyGenerator.INSTANCE),
			MatcherOperator.exactTypeMatchOperator(
				UnidentifiableType.class,
				NullObjectPropertyGenerator.INSTANCE
			)
		);
	}

	private static List<MatcherOperator<ContainerPropertyGenerator>> getDefaultContainerPropertyGenerators() {
		return Arrays.asList(
			new MatcherOperator<>(
				property -> new AssignableTypeMatcher(Optional.class).match(property)
					&& new SingleGenericTypeMatcher().match(property),
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
				property -> new AssignableTypeMatcher(Stream.class).match(property)
					&& new SingleGenericTypeMatcher().match(property),
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
				property -> new AssignableTypeMatcher(Set.class).match(property)
					&& new SingleGenericTypeMatcher().match(property),
				SetContainerPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(
				property -> new AssignableTypeMatcher(Iterable.class).match(property)
					&& new SingleGenericTypeMatcher().match(property),
				DefaultSingleContainerPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(
				property -> new AssignableTypeMatcher(Iterator.class).match(property)
					&& new SingleGenericTypeMatcher().match(property),
				DefaultSingleContainerPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(
				property -> new AssignableTypeMatcher(Map.class).match(property)
					&& new DoubleGenericTypeMatcher().match(property),
				MapContainerPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(
				property -> new AssignableTypeMatcher(Entry.class).match(property)
					&& new DoubleGenericTypeMatcher().match(property),
				EntryContainerPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(
				property -> Types.getActualType(property.getType()).isArray()
					|| GenericArrayType.class.isAssignableFrom(property.getType().getClass()),
				ArrayContainerPropertyGenerator.INSTANCE
			),
			new MatcherOperator<>(
				property -> property.getClass() == MapEntryElementProperty.class,
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
