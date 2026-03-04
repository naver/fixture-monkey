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

import static com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary.NOT_GENERATED;
import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.ALWAYS_NULL_INJECT;
import static com.navercorp.fixturemonkey.customizer.Values.NOT_NULL;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.lang.reflect.AnnotatedType;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.ArbitraryBuilders;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.ComplexObject;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.IntValue;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.Interface;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.InterfaceFieldImplementationValue;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.InterfaceImplementation;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.ListStringObject;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.MapHolder;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.NestedListHolder;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.NestedSimpleObject;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.NestedStringList;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.NestedStringListHolder;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.NullableObject;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.OptionalStringObject;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.Order;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.Pair;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.PairContainerPropertyGenerator;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.PairInterface;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.PairIntrospector;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.SimpleObject;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.SimpleObjectChild;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.SimpleStringObject;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.StringAndInt;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.StringArrayHolder;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.StringListHolder;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.StringMapHolder;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.StringValue;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.TwoEnum;
import com.navercorp.fixturemonkey.adapter.ValueProjectionAssembleSpecs.UniqueArbitraryGenerator;
import com.navercorp.fixturemonkey.api.container.DecomposableJavaContainer;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.api.generator.CompositeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.IntrospectedArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.MatchArbitraryGenerator;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.ExactTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.api.type.Types;

class ValueProjectionCustomizationTest {

	private static final long SEED = 12345L;

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.plugin(new JavaNodeTreeAdapterPlugin())
		.build();

	@Property
	void setSingleField() {
		String expected = "test-value";

		StringValue actual = SUT.giveMeBuilder(StringValue.class).set("value", expected).sample();

		then(actual).isNotNull();
		then(actual.getValue()).isEqualTo(expected);
	}

	@Property
	void setContainerField() {
		List<String> expected = java.util.Arrays.asList("a", "b", "c");

		StringListHolder actual = SUT.giveMeBuilder(StringListHolder.class).set("values", expected).sample();

		then(actual).isNotNull();
		then(actual.getValues()).isEqualTo(expected);
	}

	@Property
	void setNestedList() {
		List<List<String>> expected = java.util.Arrays.asList(
			java.util.Arrays.asList("a", "b"),
			java.util.Arrays.asList("c", "d", "e")
		);

		NestedListHolder actual = SUT.giveMeBuilder(NestedListHolder.class).set("nestedValues", expected).sample();

		then(actual).isNotNull();
		then(actual.getNestedValues()).isEqualTo(expected);
	}

	@Property
	void setMultipleFields() {
		Order actual = SUT.giveMeBuilder(Order.class)
			.set("orderId", "ORD-123")
			.set("quantity", 5)
			.set("product.name", "Test Product")
			.set("product.price", 1000)
			.sample();

		then(actual).isNotNull();
		then(actual.getOrderId()).isEqualTo("ORD-123");
		then(actual.getQuantity()).isEqualTo(5);
		then(actual.getProduct().getName()).isEqualTo("Test Product");
		then(actual.getProduct().getPrice()).isEqualTo(1000);
	}

	@Property
	void sizeFixesListSize() {
		StringListHolder actual = SUT.giveMeBuilder(StringListHolder.class).size("values", 5).sample();

		then(actual.getValues()).hasSize(5);
	}

	@Property
	void setListElementWithIndex() {
		StringListHolder actual = SUT.giveMeBuilder(StringListHolder.class)
			.size("values", 3)
			.set("values[1]", "modified")
			.sample();

		then(actual.getValues()).hasSize(3);
		then(actual.getValues().get(1)).isEqualTo("modified");
	}

	@Property
	void setMapField() {
		java.util.Map<String, Integer> expected = new java.util.HashMap<>();
		expected.put("one", 1);
		expected.put("two", 2);

		MapHolder actual = SUT.giveMeBuilder(MapHolder.class).set("mapping", expected).sample();

		then(actual.getMapping()).isEqualTo(expected);
	}

	@Property
	void sizeMapField() {
		MapHolder actual = SUT.giveMeBuilder(MapHolder.class).size("mapping", 3).sample();

		then(actual.getMapping()).hasSize(3);
	}

	@Property
	void setNull() {
		String actual = SUT.giveMeBuilder(StringValue.class).setNull("value").sample().getValue();

		then(actual).isNull();
	}

	@Property
	void setNullList() {
		List<String> actual = SUT.giveMeBuilder(StringListHolder.class).setNull("values").sample().getValues();

		then(actual).isNull();
	}

	@Property
	void setNestedStringListElement() {
		String expected = "nested-test";

		NestedStringListHolder actual = SUT.giveMeBuilder(NestedStringListHolder.class)
			.size("values", 1)
			.set("values[0].value", expected)
			.sample();

		then(actual.getValues()).hasSize(1);
		then(actual.getValues().get(0).getValue()).isEqualTo(expected);
	}

	@Property
	void setMultipleNestedStringListElements() {
		NestedStringListHolder actual = SUT.giveMeBuilder(NestedStringListHolder.class)
			.size("values", 3)
			.set("values[0].value", "first")
			.set("values[1].value", "second")
			.set("values[2].value", "third")
			.sample();

		then(actual.getValues()).hasSize(3);
		then(actual.getValues().get(0).getValue()).isEqualTo("first");
		then(actual.getValues().get(1).getValue()).isEqualTo("second");
		then(actual.getValues().get(2).getValue()).isEqualTo("third");
	}

	@Property
	void sizeSmallerRemains() {
		List<String> actual = SUT.giveMeBuilder(StringListHolder.class)
			.size("values", 2)
			.set("values[0]", "test")
			.set("values[1]", "test2")
			.size("values", 1)
			.sample()
			.getValues();

		then(actual).hasSize(1);
		then(actual.get(0)).isEqualTo("test");
	}

	@Property
	void sizeSmallerRemovesOutOfRangeValues() {
		StringListHolder actual = SUT.giveMeBuilder(StringListHolder.class)
			.size("values", 3)
			.set("values[0]", "keep")
			.set("values[1]", "remove")
			.set("values[2]", "remove")
			.size("values", 1)
			.sample();

		then(actual.getValues()).hasSize(1);
		then(actual.getValues().get(0)).isEqualTo("keep");
	}

	@Property
	void defaultNotNull() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		String actual = sut.giveMeOne(SimpleObject.class).getStr();

		then(actual).isNotNull();
	}

	@Property
	void pushExceptGenerateType() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.pushExceptGenerateType(new ExactTypeMatcher(String.class))
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isNull();
	}

	@Property
	void addExceptGenerateClass() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.addExceptGenerateClass(Timestamp.class)
			.build();

		Timestamp actual = sut.giveMeOne(Timestamp.class);

		then(actual).isNull();
	}

	@Property
	void addExceptGenerateClassNotGenerateField() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.addExceptGenerateClass(String.class)
			.build();

		String actual = sut.giveMeOne(SimpleObject.class).getStr();

		then(actual).isNull();
	}

	@Property
	void addExceptGeneratePackage() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.addExceptGeneratePackage("java.lang")
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isNull();
	}

	@Property
	void addExceptGeneratePackageNotGenerateField() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.addExceptGeneratePackage("java.lang")
			.build();

		String actual = sut.giveMeOne(SimpleObject.class).getStr();

		then(actual).isNull();
	}

	@Property
	void pushAssignableTypeNullInjectGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.pushAssignableTypeNullInjectGenerator(SimpleObject.class, context -> 1.0d)
			.build();

		SimpleObjectChild actual = sut.giveMeOne(SimpleObjectChild.class);

		then(actual).isNull();
	}

	@Property
	void pushExactTypeNullInjectGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.pushExactTypeNullInjectGenerator(SimpleObject.class, context -> 1.0d)
			.build();

		SimpleObject actual = sut.giveMeOne(SimpleObject.class);

		then(actual).isNull();
	}

	@Property
	void pushNullInjectGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.pushNullInjectGenerator(MatcherOperator.exactTypeMatchOperator(SimpleObject.class, context -> 1.0d))
			.build();

		SimpleObject actual = sut.giveMeOne(SimpleObject.class);

		then(actual).isNull();
	}

	@Property
	void defaultNullInjectGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.defaultNullInjectGenerator(context -> 1.0d)
			.build();

		SimpleObject actual = sut.giveMeOne(SimpleObject.class);

		then(actual).isNull();
	}

	@Property
	void pushExactTypePropertyNameResolver() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.pushExactTypePropertyNameResolver(String.class, property -> "string")
			.build();
		String expected = "test";

		String actual = sut.giveMeBuilder(SimpleObject.class).set("string", expected).sample().getStr();

		then(actual).isEqualTo(expected);
	}

	@Property
	void pushAssignableTypePropertyNameResolver() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.pushAssignableTypePropertyNameResolver(Interface.class, property -> "interface")
			.build();
		InterfaceImplementation expected = new InterfaceImplementation();
		expected.setValue("test");

		InterfaceImplementation actual = sut
			.giveMeBuilder(InterfaceFieldImplementationValue.class)
			.set("interface", expected)
			.sample()
			.getValue();

		then(actual).isEqualTo(expected);
	}

	@Property
	void pushPropertyNameResolver() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.pushPropertyNameResolver(MatcherOperator.exactTypeMatchOperator(String.class, property -> "string"))
			.build();
		String expected = "test";

		String actual = sut.giveMeBuilder(SimpleObject.class).set("string", expected).sample().getStr();

		then(actual).isEqualTo(expected);
	}

	@Property
	void defaultPropertyNameResolver() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.defaultPropertyNameResolver(property -> "'" + property.getName() + "'")
			.build();
		String expected = "test";

		String actual = sut.giveMeBuilder(SimpleObject.class).set("'str'", expected).sample().getStr();

		then(actual).isEqualTo(expected);
	}

	@Property
	void pushArbitraryContainerInfoGenerator() {
		// given
		MatcherOperator<ArbitraryContainerInfoGenerator> containerInfoGenerator = new MatcherOperator<>(
			property -> {
				if (
					Types.getActualType(property.getType()).isArray()
						|| Types.getGenericsTypes(property.getAnnotatedType()).isEmpty()
				) {
					return false;
				}

				AnnotatedType elementType = Types.getGenericsTypes(property.getAnnotatedType()).get(0);
				Class<?> type = Types.getActualType(elementType);
				return type.isAssignableFrom(String.class);
			},
			context -> new ArbitraryContainerInfo(5, 5)
		);

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.pushArbitraryContainerInfoGenerator(containerInfoGenerator)
			.build();

		// when
		List<String> actual = sut.giveMeOne(ComplexObject.class).getStrList();

		// then
		then(actual).hasSize(5);
	}

	@Property
	void pushArbitraryContainerInfoGeneratorNotMatching() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.pushArbitraryContainerInfoGenerator(
				new MatcherOperator<>(
					property -> {
						if (
							Types.getActualType(property.getType()).isArray()
								|| Types.getGenericsTypes(property.getAnnotatedType()).isEmpty()
						) {
							return false;
						}

						AnnotatedType elementType = Types.getGenericsTypes(property.getAnnotatedType()).get(0);
						Class<?> type = Types.getActualType(elementType);
						return type.isAssignableFrom(String.class);
					},
					context -> new ArbitraryContainerInfo(5, 5)
				)
			)
			.build();

		List<SimpleObject> actual = sut.giveMeOne(ComplexObject.class).getList();

		then(actual).hasSizeBetween(0, 3);
	}

	@Property
	void defaultArbitraryContainerMaxSize() {
		// given
		ArbitraryContainerInfoGenerator containerInfoGenerator = context -> new ArbitraryContainerInfo(0, 1);

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.defaultArbitraryContainerInfoGenerator(containerInfoGenerator)
			.build();

		// when
		List<SimpleObject> actual = sut.giveMeOne(ComplexObject.class).getList();

		// then
		then(actual).hasSizeLessThanOrEqualTo(1);
	}

	@Property
	void defaultArbitraryContainerInfo() {
		// given
		ArbitraryContainerInfoGenerator containerInfoGenerator = context -> new ArbitraryContainerInfo(3, 3);

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.defaultArbitraryContainerInfoGenerator(containerInfoGenerator)
			.build();

		// when
		List<SimpleObject> actual = sut.giveMeOne(ComplexObject.class).getList();

		// then
		then(actual).hasSize(3);
	}

	@Property
	void sampleEnumMapWithEnumSizeIsLessThanContainerInfoMaxSize() {
		ArbitraryContainerInfoGenerator containerInfoGenerator = context -> new ArbitraryContainerInfo(0, 5);

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.defaultArbitraryContainerInfoGenerator(containerInfoGenerator)
			.build();

		Map<TwoEnum, String> values = sut.giveMeOne(new TypeReference<Map<TwoEnum, String>>() {
		});

		then(values).hasSizeLessThanOrEqualTo(2);
	}

	@Property
	void sampleEnumMapWithEnumSizeIsLessThanContainerInfoMinSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(3, 5))
			.build();

		// when
		Map<TwoEnum, String> values = sut.giveMeOne(new TypeReference<Map<TwoEnum, String>>() {
		});

		// then
		then(values).hasSizeLessThanOrEqualTo(2);
	}

	@Property
	void sampleEnumSetWithEnumSizeIsLessThanContainerInfoMinSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(3, 5))
			.build();

		// when
		Set<TwoEnum> values = sut.giveMeOne(new TypeReference<Set<TwoEnum>>() {
		});

		// then
		then(values).hasSizeLessThanOrEqualTo(2);
	}

	@Property
	void alterDefaultArbitraryGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.defaultArbitraryGenerator(generator ->
				new MatchArbitraryGenerator(
					Arrays.asList(
						new IntrospectedArbitraryGenerator(context -> new ArbitraryIntrospectorResult(NOT_GENERATED)),
						generator
					)
				)
			)
			.build();

		// when
		String actual = sut.giveMeOne(String.class);

		// then
		then(actual).isNull();
	}

	@Property
	void skipArbitraryGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.defaultArbitraryGenerator(generator ->
				new CompositeArbitraryGenerator(Arrays.asList(context -> NOT_GENERATED, generator))
			)
			.defaultNotNull(true)
			.build();

		// when
		String actual = sut.giveMeOne(String.class);

		// then
		then(actual).isNotNull();
	}

	@Property
	void uniqueArbitraryGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.defaultArbitraryGenerator(UniqueArbitraryGenerator::new)
			.build();

		// when
		List<String> actual = sut.giveMe(String.class, 100);

		// then
		Set<String> expected = new HashSet<>(actual);
		then(actual).hasSameSizeAs(expected);
	}

	@Property
	void allArbitraryGeneratorSkipReturnsNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.defaultArbitraryGenerator(generator ->
				(new CompositeArbitraryGenerator(Arrays.asList(context -> NOT_GENERATED, context -> NOT_GENERATED)))
			)
			.build();

		// when
		String actual = sut.giveMeOne(String.class);

		// then
		then(actual).isNull();
	}

	@Property
	void defaultNotNullNotWorksWhenSetDefaultNullInjectGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.defaultNullInjectGenerator(
				new DefaultNullInjectGenerator(
					ALWAYS_NULL_INJECT,
					false,
					false,
					false,
					Collections.emptySet(),
					Collections.emptySet()
				)
			)
			.defaultNotNull(true)
			.build();

		String actual = sut.giveMeOne(SimpleObject.class).getStr();

		then(actual).isNull();
	}

	@Property
	void nullableElement() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.defaultNullInjectGenerator(
				new DefaultNullInjectGenerator(1.0d, false, false, true, Collections.emptySet(), Collections.emptySet())
			)
			.build();

		List<String> actual = sut.giveMeOne(new TypeReference<List<String>>() {
		});

		then(actual).allMatch(Objects::isNull);
	}

	@Property
	void sampleNullableContainerWhenOptionNullableContainerIsSetReturnsNull() {
		DefaultNullInjectGenerator nullInjectGenerator = new DefaultNullInjectGenerator(
			ALWAYS_NULL_INJECT,
			true,
			false,
			false,
			Collections.emptySet(),
			Collections.emptySet()
		);
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.defaultNullInjectGenerator(nullInjectGenerator)
			.build();

		List<String> values = sut.giveMeOne(NullableObject.class).getValues();

		then(values).isNull();
	}

	@Property
	void setSimple() {
		SimpleStringObject actual = SUT.giveMeBuilder(SimpleStringObject.class).set("str", "str").sample();

		then(actual.getStr()).isEqualTo("str");
	}

	@Property
	void setDecomposedValue() {
		SimpleStringObject expected = new SimpleStringObject();
		expected.setStr("original");

		SimpleStringObject actual = SUT.giveMeBuilder(NestedSimpleObject.class)
			.set("object", expected)
			.set("object.str", "modified")
			.sample()
			.getObject();

		then(actual.getStr()).isEqualTo("modified");
	}

	@Property
	void setArbitrary() {
		SimpleStringObject expected = new SimpleStringObject();
		expected.setStr("original");

		SimpleStringObject actual = SUT.giveMeBuilder(NestedSimpleObject.class)
			.set("object", Arbitraries.just(expected))
			.set("object.str", "modified")
			.sample()
			.getObject();

		then(actual.getStr()).isEqualTo("modified");
	}

	@Property
	void setOptional() {
		Optional<String> optional = Optional.of("test");

		Optional<String> actual = SUT.giveMeBuilder(OptionalStringObject.class)
			.set("optionalString", optional)
			.sample()
			.getOptionalString();

		then(actual).isEqualTo(optional);
	}

	@Property
	void setDecomposedList() {
		List<String> expected = new ArrayList<>();
		expected.add("a");
		expected.add("b");
		expected.add("c");

		List<String> actual = SUT.giveMeBuilder(StringListHolder.class).set("values", expected).sample().getValues();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setDecomposedSet() {
		Set<String> expected = new HashSet<>();
		expected.add("a");
		expected.add("b");
		expected.add("c");

		Set<String> actual = SUT.giveMeBuilder(new TypeReference<Set<String>>() {
		}).set(expected).sample();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setDecomposedMap() {
		Map<String, String> expected = new HashMap<>();
		expected.put("a", "1");
		expected.put("b", "2");

		Map<String, String> actual = SUT.giveMeBuilder(StringMapHolder.class)
			.set("mapping", expected)
			.sample()
			.getMapping();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setDecomposedOptional() {
		Optional<String> expected = Optional.of("test");

		Optional<String> actual = SUT.giveMeBuilder(OptionalStringObject.class)
			.set("optionalString", expected)
			.sample()
			.getOptionalString();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setDecomposedOptionalEmpty() {
		Optional<String> expected = Optional.empty();

		Optional<String> actual = SUT.giveMeBuilder(OptionalStringObject.class)
			.set("optionalString", expected)
			.sample()
			.getOptionalString();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setNullMap() {
		Map<String, String> actual = SUT.giveMeBuilder(StringMapHolder.class).setNull("mapping").sample().getMapping();

		then(actual).isNull();
	}

	@Property
	void setNotNullString() {
		String actual = SUT.giveMeBuilder(SimpleStringObject.class).setNotNull("str").sample().getStr();

		then(actual).isNotNull();
	}

	@Property
	void setNotNullList() {
		List<String> actual = SUT.giveMeBuilder(StringListHolder.class).setNotNull("values").sample().getValues();

		then(actual).isNotNull();
	}

	@Property
	void setNotNullMap() {
		Map<String, String> actual = SUT.giveMeBuilder(StringMapHolder.class)
			.setNotNull("mapping")
			.sample()
			.getMapping();

		then(actual).isNotNull();
	}

	@Property
	void setNotNullValue() {
		String actual = SUT.giveMeBuilder(SimpleStringObject.class).set("str", NOT_NULL).sample().getStr();

		then(actual).isNotNull();
	}

	@Property
	void setRootJavaType() {
		String expected = "test";

		String actual = SUT.giveMeBuilder(String.class).set(expected).sample();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setRootComplexType() {
		SimpleStringObject expected = new SimpleStringObject();
		expected.setStr("test");

		SimpleStringObject actual = SUT.giveMeBuilder(SimpleStringObject.class).set(expected).sample();

		then(actual.getStr()).isEqualTo(expected.getStr());
	}

	@Property
	void setListElement() {
		String expected = "test";

		String actual = SUT.giveMeBuilder(new TypeReference<List<String>>() {
			})
			.size("$", 1)
			.set("$[0]", expected)
			.sample()
			.get(0);

		then(actual).isEqualTo(expected);
	}

	@Property
	void setAndSetNull() {
		String actual = SUT.giveMeBuilder(SimpleStringObject.class).set("str", "test").setNull("str").sample().getStr();

		then(actual).isNull();
	}

	@Property
	void setAfterBuildNotAffected() {
		ArbitraryBuilder<SimpleStringObject> builder = SUT.giveMeBuilder(SimpleStringObject.class);
		net.jqwik.api.Arbitrary<SimpleStringObject> buildArbitrary = builder.build();

		ArbitraryBuilder<SimpleStringObject> actual = builder.set("str", "set");

		SimpleStringObject actualSample = actual.sample();
		SimpleStringObject buildSample = buildArbitrary.sample();
		then(actualSample.getStr()).isEqualTo("set");
	}

	@Property
	void setLazyValue() {
		ArbitraryBuilder<String> variable = SUT.giveMeBuilder(String.class);
		ArbitraryBuilder<String> builder = SUT.giveMeBuilder(String.class).setLazy("$", variable::sample);
		variable.set("test");

		String actual = builder.sample();

		then(actual).isEqualTo("test");
	}

	@Property
	void setArbitraryBuilder() {
		String expected = "test";

		String actual = SUT.giveMeBuilder(SimpleStringObject.class)
			.set("str", SUT.giveMeBuilder(String.class).set("$", expected))
			.sample()
			.getStr();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setDecomposeContainerTwice() {
		List<String> strings = new ArrayList<>();
		strings.add("test");

		List<String> actual = SUT.giveMeBuilder(StringListHolder.class)
			.set("values", strings)
			.set("values", new ArrayList<>())
			.sample()
			.getValues();

		then(actual).isEmpty();
	}

	@Property
	void setEmptyMap() {
		Map<String, String> map = new HashMap<>();
		map.put("test", "value");

		Map<String, String> actual = SUT.giveMeBuilder(StringMapHolder.class)
			.set("mapping", map)
			.set("mapping", new HashMap<>())
			.sample()
			.getMapping();

		then(actual).isEmpty();
	}

	@Property
	void setFieldWhichObjectIsFixedNull() {
		String expected = "test";

		String actual = SUT.giveMeBuilder(NestedSimpleObject.class)
			.set("object", Arbitraries.just(null))
			.set("object.str", expected)
			.sample()
			.getObject()
			.getStr();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setFieldWhichRootIsFixedNull() {
		String expected = "test";

		String actual = SUT.giveMeBuilder(SimpleStringObject.class)
			.set("$", Arbitraries.just(null))
			.set("str", expected)
			.sample()
			.getStr();

		then(actual).isEqualTo(expected);
	}

	@Property
	void sizeZero() {
		StringListHolder actual = SUT.giveMeBuilder(StringListHolder.class).size("values", 0).sample();

		then(actual.getValues()).hasSize(0);
	}

	@Property
	void size() {
		StringListHolder actual = SUT.giveMeBuilder(StringListHolder.class).size("values", 10).sample();

		then(actual.getValues()).hasSize(10);
	}

	@Property
	void sizeArray() {
		StringArrayHolder actual = SUT.giveMeBuilder(StringArrayHolder.class).size("values", 10).sample();

		then(actual.getValues()).hasSize(10);
	}

	@Property
	void sizeMinMax() {
		StringListHolder actual = SUT.giveMeBuilder(StringListHolder.class).size("values", 3, 8).sample();

		then(actual.getValues()).hasSizeBetween(3, 8);
	}

	@Property
	void sizeMinIsBiggerThanMax() {
		thenThrownBy(() -> SUT.giveMeBuilder(StringListHolder.class).size("values", 5, 1).sample())
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("should be min > max");
	}

	@Property
	void minSize() {
		StringListHolder actual = SUT.giveMeBuilder(StringListHolder.class).minSize("values", 10).sample();

		then(actual.getValues()).hasSizeGreaterThanOrEqualTo(10);
	}

	@Property
	void maxSize() {
		StringListHolder actual = SUT.giveMeBuilder(StringListHolder.class).maxSize("values", 10).sample();

		then(actual.getValues()).hasSizeLessThanOrEqualTo(10);
	}

	@Property
	void maxSizeZero() {
		StringListHolder actual = SUT.giveMeBuilder(StringListHolder.class).maxSize("values", 0).sample();

		then(actual.getValues()).hasSizeLessThanOrEqualTo(0);
	}

	@Property
	void nestedSize() {
		List<List<String>> actual = SUT.giveMeBuilder(new TypeReference<List<List<String>>>() {
			})
			.size("$[0]", 10)
			.size("$", 2)
			.sample();

		then(actual).hasSize(2);
		then(actual.get(0)).hasSize(10);
	}

	@Property
	void sizeAfterSetReturnsSet() {
		List<String> actual = SUT.giveMeBuilder(new TypeReference<List<String>>() {
			})
			.size("$", 3)
			.set("$", new ArrayList<>())
			.sample();

		then(actual).isEmpty();
	}

	@Property
	void sizeNotSetEmptyList() {
		List<String> actual = SUT.giveMeBuilder(StringListHolder.class)
			.size("values", 1)
			.set("values", new ArrayList<>())
			.sample()
			.getValues();

		then(actual).isEmpty();
	}

	@Property
	void mapWhenNull() {
		String actual = SUT.giveMeBuilder(SimpleStringObject.class)
			.setNull("str")
			.map(SimpleStringObject::getStr)
			.sample();

		then(actual).isNull();
	}

	@Property
	void mapWhenNotNull() {
		String actual = SUT.giveMeBuilder(SimpleStringObject.class)
			.setNotNull("str")
			.map(SimpleStringObject::getStr)
			.sample();

		then(actual).isNotNull();
	}

	@Property
	void mapToFixedValue() {
		String actual = SUT.giveMeBuilder(SimpleStringObject.class)
			.map(it -> "test")
			.sample();

		then(actual).isEqualTo("test");
	}

	@Property
	void mapKeyIsNotNull() {
		Set<String> actual = SUT.giveMeBuilder(new TypeReference<Map<String, String>>() {
		}).sample().keySet();

		then(actual).allMatch(Objects::nonNull);
	}

	@Property(tries = 1)
	void sampleAfterMapTwiceReturnsDiff() {
		ArbitraryBuilder<String> arbitraryBuilder = SUT.giveMeBuilder(SimpleStringObject.class)
			.set(
				"str",
				Arbitraries.strings()
					.ascii()
					.filter(it -> !it.isEmpty())
			)
			.map(SimpleStringObject::getStr);

		String actual = arbitraryBuilder.sample();

		String notExpected = arbitraryBuilder.sample();
		then(actual).isNotEqualTo(notExpected);
	}

	@Property
	void zipList() {
		List<ArbitraryBuilder<?>> list = new ArrayList<>();
		list.add(SUT.giveMeBuilder(StringValue.class));
		list.add(SUT.giveMeBuilder(IntValue.class));

		StringAndInt actual = ArbitraryBuilders.zip(list, l -> {
			StringAndInt result = new StringAndInt();
			result.setValue1((StringValue)l.get(0));
			result.setValue2((IntValue)l.get(1));
			return result;
		}).sample();

		then(actual.getValue1()).isNotNull();
		then(actual.getValue2()).isNotNull();
	}

	@Property
	void zipEmptyListThrows() {
		List<ArbitraryBuilder<?>> list = new ArrayList<>();

		thenThrownBy(() -> ArbitraryBuilders.zip(list, l -> new StringAndInt()).sample())
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("zip should be used in more than two ArbitraryBuilders, given size");
	}

	@Property
	void zipThree() {
		ArbitraryBuilder<StringValue> s1 = SUT.giveMeBuilder(StringValue.class).set("value", "s1");
		ArbitraryBuilder<StringValue> s2 = SUT.giveMeBuilder(StringValue.class).set("value", "s2");
		ArbitraryBuilder<StringValue> s3 = SUT.giveMeBuilder(StringValue.class).set("value", "s3");

		NestedStringListHolder actual = ArbitraryBuilders.zip(s1, s2, s3, (a1, a2, a3) -> {
			List<StringValue> list = new ArrayList<>();
			list.add(a1);
			list.add(a2);
			list.add(a3);

			NestedStringListHolder result = new NestedStringListHolder();
			result.setValues(list);
			return result;
		}).sample();

		then(actual.getValues()).hasSize(3);
		then(actual.getValues().get(0).getValue()).isEqualTo("s1");
		then(actual.getValues().get(1).getValue()).isEqualTo("s2");
		then(actual.getValues().get(2).getValue()).isEqualTo("s3");
	}

	@Property
	void zipWithThree() {
		ArbitraryBuilder<StringValue> s1 = SUT.giveMeBuilder(StringValue.class).set("value", "s1");
		ArbitraryBuilder<StringValue> s2 = SUT.giveMeBuilder(StringValue.class).set("value", "s2");
		ArbitraryBuilder<StringValue> s3 = SUT.giveMeBuilder(StringValue.class).set("value", "s3");

		NestedStringListHolder actual = s1
			.zipWith(s2, s3, (a1, a2, a3) -> {
				List<StringValue> list = new ArrayList<>();
				list.add(a1);
				list.add(a2);
				list.add(a3);

				NestedStringListHolder result = new NestedStringListHolder();
				result.setValues(list);
				return result;
			})
			.sample();

		then(actual.getValues()).hasSize(3);
		then(actual.getValues().get(0).getValue()).isEqualTo("s1");
		then(actual.getValues().get(1).getValue()).isEqualTo("s2");
		then(actual.getValues().get(2).getValue()).isEqualTo("s3");
	}

	@Property
	void zipFour() {
		ArbitraryBuilder<StringValue> s1 = SUT.giveMeBuilder(StringValue.class).set("value", "s1");
		ArbitraryBuilder<StringValue> s2 = SUT.giveMeBuilder(StringValue.class).set("value", "s2");
		ArbitraryBuilder<StringValue> s3 = SUT.giveMeBuilder(StringValue.class).set("value", "s3");
		ArbitraryBuilder<StringValue> s4 = SUT.giveMeBuilder(StringValue.class).set("value", "s4");

		NestedStringListHolder actual = ArbitraryBuilders.zip(s1, s2, s3, s4, (a1, a2, a3, a4) -> {
			List<StringValue> list = new ArrayList<>();
			list.add(a1);
			list.add(a2);
			list.add(a3);
			list.add(a4);

			NestedStringListHolder result = new NestedStringListHolder();
			result.setValues(list);
			return result;
		}).sample();

		then(actual.getValues()).hasSize(4);
		then(actual.getValues().get(0).getValue()).isEqualTo("s1");
		then(actual.getValues().get(1).getValue()).isEqualTo("s2");
		then(actual.getValues().get(2).getValue()).isEqualTo("s3");
		then(actual.getValues().get(3).getValue()).isEqualTo("s4");
	}

	@Property
	void zipWithFour() {
		ArbitraryBuilder<StringValue> s1 = SUT.giveMeBuilder(StringValue.class).set("value", "s1");
		ArbitraryBuilder<StringValue> s2 = SUT.giveMeBuilder(StringValue.class).set("value", "s2");
		ArbitraryBuilder<StringValue> s3 = SUT.giveMeBuilder(StringValue.class).set("value", "s3");
		ArbitraryBuilder<StringValue> s4 = SUT.giveMeBuilder(StringValue.class).set("value", "s4");

		NestedStringListHolder actual = s1
			.zipWith(s2, s3, s4, (a1, a2, a3, a4) -> {
				List<StringValue> list = new ArrayList<>();
				list.add(a1);
				list.add(a2);
				list.add(a3);
				list.add(a4);

				NestedStringListHolder result = new NestedStringListHolder();
				result.setValues(list);
				return result;
			})
			.sample();

		then(actual.getValues()).hasSize(4);
		then(actual.getValues().get(0).getValue()).isEqualTo("s1");
		then(actual.getValues().get(1).getValue()).isEqualTo("s2");
		then(actual.getValues().get(2).getValue()).isEqualTo("s3");
		then(actual.getValues().get(3).getValue()).isEqualTo("s4");
	}

	@Property
	void zipWith() {
		ArbitraryBuilder<String> stringArbitraryBuilder = SUT.giveMeBuilder(String.class);

		String actual = SUT.giveMeBuilder(Integer.class)
			.zipWith(stringArbitraryBuilder, (integer, string) -> integer + string)
			.sample();

		then(actual).isNotNull();
	}

	@Property
	void zipTwo() {
		ArbitraryBuilder<String> stringArbitraryBuilder = SUT.giveMeBuilder(String.class);
		ArbitraryBuilder<Integer> integerArbitraryBuilder = SUT.giveMeBuilder(Integer.class);

		String actual = ArbitraryBuilders.zip(
			stringArbitraryBuilder,
			integerArbitraryBuilder,
			(integer, string) -> integer + string
		).sample();

		then(actual).isNotNull();
	}

	@Property
	void zipReturnsNew() {
		ArbitraryBuilder<String> stringArbitraryBuilder = SUT.giveMeBuilder(String.class);
		ArbitraryBuilder<Integer> integerArbitraryBuilder = SUT.giveMeBuilder(Integer.class);

		net.jqwik.api.Arbitrary<String> zippedArbitraryBuilder = ArbitraryBuilders.zip(
			stringArbitraryBuilder,
			integerArbitraryBuilder,
			(integer, string) -> integer + string
		).build();

		String result1 = zippedArbitraryBuilder.sample();
		String result2 = zippedArbitraryBuilder.sample();
		then(result1).isNotEqualTo(result2);
	}

	@Property(tries = 1)
	void notFixedSampleReturnsDiff() {
		ArbitraryBuilder<SimpleStringObject> fixedArbitraryBuilder = SUT.giveMeBuilder(SimpleStringObject.class);

		SimpleStringObject sample1 = fixedArbitraryBuilder.sample();
		SimpleStringObject sample2 = fixedArbitraryBuilder.sample();
		then(sample1).isNotEqualTo(sample2);
	}

	@Property
	void fixedSampleReturnsSame() {
		ArbitraryBuilder<SimpleStringObject> fixedArbitraryBuilder = SUT.giveMeBuilder(
			SimpleStringObject.class
		).fixed();

		SimpleStringObject sample1 = fixedArbitraryBuilder.sample();
		SimpleStringObject sample2 = fixedArbitraryBuilder.sample();
		then(sample1.getStr()).isEqualTo(sample2.getStr());
	}

	@Property
	void arbitraryFixedSampleReturnsSame() {
		ArbitraryBuilder<SimpleStringObject> fixedArbitraryBuilder = SUT.giveMeBuilder(SimpleStringObject.class)
			.set("str", Arbitraries.of("value1", "value2"))
			.fixed();

		SimpleStringObject sample1 = fixedArbitraryBuilder.sample();
		SimpleStringObject sample2 = fixedArbitraryBuilder.sample();
		then(sample1.getStr()).isEqualTo(sample2.getStr());
	}

	@Property
	void setNullFixedReturnsNull() {
		SimpleStringObject actual = SUT.giveMeBuilder(SimpleStringObject.class).setNull("$").fixed().sample();

		then(actual).isNull();
	}

	@Property
	void fixedRangedSizeReturnsSameSize() {
		ArbitraryBuilder<StringListHolder> fixedArbitraryBuilder = SUT.giveMeBuilder(StringListHolder.class)
			.size("values", 1, 5)
			.fixed();

		List<String> actual = fixedArbitraryBuilder.sample().getValues();

		List<String> expected = fixedArbitraryBuilder.sample().getValues();
		then(actual).isEqualTo(expected);
	}

	@Property
	void setNullFixed() {
		String expected = "test";

		String actual = SUT.giveMeBuilder(NestedSimpleObject.class)
			.setNull("object")
			.fixed()
			.set("object.str", expected)
			.sample()
			.getObject()
			.getStr();

		then(actual).isEqualTo(expected);
	}

	@Property
	void giveMeBuilderWithValue() {
		SimpleStringObject expected = new SimpleStringObject();
		expected.setStr("test");

		SimpleStringObject actual = SUT.giveMeBuilder(expected).sample();

		then(actual.getStr()).isEqualTo(expected.getStr());
	}

	@Property
	void copyValidOnly() {
		thenNoException().isThrownBy(() ->
			SUT.giveMeBuilder(ListStringObject.class).size("values", 0).validOnly(false).copy().sample()
		);
	}

	@Property
	void generateNewContainer() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushAssignableTypeContainerPropertyGenerator(Pair.class, new PairContainerPropertyGenerator())
			.pushContainerIntrospector(new PairIntrospector())
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		// when
		Pair<String, String> pair = sut.giveMeBuilder(new TypeReference<Pair<String, String>>() {
		}).sample();

		// then
		then(pair).isNotNull();
	}

	@Property
	void decomposeNewContainer() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushAssignableTypeContainerPropertyGenerator(Pair.class, new PairContainerPropertyGenerator())
			.pushContainerIntrospector(new PairIntrospector())
			.defaultDecomposedContainerValueFactory(obj -> {
				if (obj instanceof Pair) {
					Pair<?, ?> pair = (Pair<?, ?>)obj;
					List<Object> list = new ArrayList<>();
					list.add(pair.getFirst());
					list.add(pair.getSecond());
					return new DecomposableJavaContainer(list, 2);
				}
				throw new IllegalArgumentException(
					"given type is not supported container : " + obj.getClass().getTypeName()
				);
			})
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();
		ArbitraryBuilder<Pair<String, String>> builder = sut
			.giveMeBuilder(new TypeReference<Pair<String, String>>() {
			})
			.fixed();

		// when
		Pair<String, String> actual1 = builder.sample();
		Pair<String, String> actual2 = builder.sample();

		// then
		then(actual1).isEqualTo(actual2);
	}

	@Property
	void decomposeNewContainerByAddContainerType() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.addContainerType(Pair.class, new PairContainerPropertyGenerator(), new PairIntrospector(), obj -> {
				Pair<?, ?> pair = (Pair<?, ?>)obj;
				List<Object> list = new ArrayList<>();
				list.add(pair.getFirst());
				list.add(pair.getSecond());
				return new DecomposableJavaContainer(list, 2);
			})
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();
		ArbitraryBuilder<Pair<String, String>> builder = sut
			.giveMeBuilder(new TypeReference<Pair<String, String>>() {
			})
			.fixed();

		// when
		Pair<String, String> actual1 = builder.sample();
		Pair<String, String> actual2 = builder.sample();

		// then
		then(actual1).isEqualTo(actual2);
	}

	@Property
	void decomposeNewContainerByAddContainerTypeInterface() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.addContainerType(
				PairInterface.class,
				new PairContainerPropertyGenerator(),
				new PairIntrospector(),
				obj -> {
					Pair<?, ?> pair = (Pair<?, ?>)obj;
					List<Object> list = new ArrayList<>();
					list.add(pair.getFirst());
					list.add(pair.getSecond());
					return new DecomposableJavaContainer(list, 2);
				}
			)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		// when
		// then
		thenNoException().isThrownBy(() ->
			sut
				.giveMeBuilder(new TypeReference<Pair<String, String>>() {
				})
				.set(sut.giveMeOne(new TypeReference<Pair<String, String>>() {
				}))
		);
	}

	@Property
	void setContainerThenSize_shouldNotLockConcreteType() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		StringValue element = new StringValue();
		element.setValue("test");

		// when
		NestedStringList actual = sut.giveMeBuilder(NestedStringList.class)
			.set("values", Collections.singletonList(element))
			.size("values", 1, 10)
			.sample();

		// then
		then(actual.getValues()).isNotNull();
		then(actual.getValues().get(0)).isInstanceOf(StringValue.class);
	}
}
