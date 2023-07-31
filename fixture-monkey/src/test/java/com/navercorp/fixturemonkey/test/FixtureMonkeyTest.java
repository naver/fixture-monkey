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

package com.navercorp.fixturemonkey.test;

import static com.navercorp.fixturemonkey.customizer.Values.NOT_NULL;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.ArbitraryBuilders;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.customizer.Values;
import com.navercorp.fixturemonkey.test.ExpressionGeneratorTestSpecs.StringValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ChildValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ComplexObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.EnumObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.GenericChildTwoValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.GenericChildValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.GenericSimpleObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.GenericStringWrapperValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.GenericValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.GenericWrapperValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.IntValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.Interface;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.InterfaceFieldValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.InterfaceImplementation;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ListStringObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.NestedStringList;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.NullableObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ObjectValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.RecursiveLeftObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SelfRecursiveListObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SelfRecursiveObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SimpleObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StaticFieldObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringAndInt;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringPair;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ThirdNestedListStringObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.TwoEnum;

class FixtureMonkeyTest {
	private static final FixtureMonkey SUT = FixtureMonkey.create();

	@Property
	void sampleWithType() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class).sample();

		// then
		then(actual.getList()).isNotNull();
		then(actual.getMap()).isNotNull();
		then(actual.getMapEntry()).isNotNull();
	}

	@Property
	void sampleWithTypeReference() {
		TypeReference<ComplexObject> type = new TypeReference<ComplexObject>() {
		};

		// when
		ComplexObject actual = SUT.giveMeBuilder(type).sample();

		// then
		then(actual.getList()).isNotNull();
		then(actual.getMap()).isNotNull();
		then(actual.getMapEntry()).isNotNull();
	}

	@Property
	void set() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("str", "str")
			.sample();

		// then
		then(actual.getStr()).isEqualTo("str");
	}

	@Property
	void setDecomposedValue() {
		// given
		SimpleObject expected = new SimpleObject();
		expected.setInstant(Instant.now());
		expected.setOptionalString(Optional.of("test"));

		// when
		SimpleObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("object", expected)
			.set("object.str", "str")
			.sample()
			.getObject();

		// then
		then(actual.getInstant()).isEqualTo(expected.getInstant());
		then(actual.getOptionalString()).isEqualTo(expected.getOptionalString());
		then(actual.getStr()).isEqualTo("str");
	}

	@Property
	void setArbitrary() {
		// given
		SimpleObject expected = new SimpleObject();
		expected.setInstant(Instant.now());
		expected.setOptionalString(Optional.of("test"));

		// when
		SimpleObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("object", Arbitraries.just(expected))
			.set("object.str", "str")
			.sample()
			.getObject();

		// then
		then(actual.getInstant()).isEqualTo(expected.getInstant());
		then(actual.getOptionalString()).isEqualTo(expected.getOptionalString());
		then(actual.getStr()).isEqualTo("str");
	}

	@Property
	void setOptional() {
		// given
		Optional<String> optional = Optional.of("test");

		// when
		ArbitraryBuilder<SimpleObject> optionalString = SUT.giveMeBuilder(SimpleObject.class)
			.set("optionalString", optional);
		Optional<String> actual = optionalString
			.sample()
			.getOptionalString();

		then(actual).isEqualTo(optional);
	}

	@Property
	void setDecomposedList() {
		// given
		List<String> expected = new ArrayList<>();
		expected.add("a");
		expected.add("b");
		expected.add("c");
		expected.add("d");
		expected.add("e");

		// when
		List<String> actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("strList", expected)
			.sample()
			.getStrList();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setDecomposedSet() {
		// given
		Set<String> expected = new HashSet<>();
		expected.add("a");
		expected.add("b");
		expected.add("c");
		expected.add("d");
		expected.add("e");

		// when
		Set<String> actual = SUT.giveMeBuilder(new TypeReference<Set<String>>() {
			})
			.set(expected)
			.sample();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setDecomposedMap() {
		// given
		Map<String, SimpleObject> expected = new HashMap<>();
		expected.put("a", new SimpleObject());
		expected.put("b", new SimpleObject());
		expected.put("c", new SimpleObject());
		expected.put("d", new SimpleObject());
		expected.put("e", new SimpleObject());

		// when
		Map<String, SimpleObject> actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("map", expected)
			.sample()
			.getMap();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setDecomposedMapEntry() {
		// given
		Map.Entry<String, SimpleObject> expected = new SimpleEntry<>("a", new SimpleObject());

		// when
		Map.Entry<String, SimpleObject> actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("mapEntry", expected)
			.sample()
			.getMapEntry();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setDecomposedOptional() {
		// given
		Optional<String> expected = Optional.of("test");

		// when
		Optional<String> actual = SUT.giveMeBuilder(SimpleObject.class)
			.set("optionalString", expected)
			.sample()
			.getOptionalString();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setDecomposedOptionalEmpty() {
		// given
		Optional<String> expected = Optional.empty();

		// when
		Optional<String> actual = SUT.giveMeBuilder(SimpleObject.class)
			.set("optionalString", expected)
			.sample()
			.getOptionalString();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setDecomposedOptionalInt() {
		// given
		OptionalInt expected = OptionalInt.of(0);

		// when
		OptionalInt actual = SUT.giveMeBuilder(SimpleObject.class)
			.set("optionalInt", expected)
			.sample()
			.getOptionalInt();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setDecomposedOptionalLong() {
		// given
		OptionalLong expected = OptionalLong.of(0L);

		// when
		OptionalLong actual = SUT.giveMeBuilder(SimpleObject.class)
			.set("optionalLong", expected)
			.sample()
			.getOptionalLong();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setDecomposedOptionalDouble() {
		// given
		OptionalDouble expected = OptionalDouble.of(0.d);

		// when
		OptionalDouble actual = SUT.giveMeBuilder(SimpleObject.class)
			.set("optionalDouble", expected)
			.sample()
			.getOptionalDouble();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setAllFields() {
		// when
		StringPair actual = SUT.giveMeBuilder(StringPair.class)
			.set("*", "str")
			.sample();

		then(actual.getValue1()).isEqualTo("str");
		then(actual.getValue2()).isEqualTo("str");
	}

	@Property
	void sizeZero() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.size("strList", 0)
			.sample();

		// then
		then(actual.getStrList()).hasSize(0);
	}

	@Property
	void size() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.size("strList", 10)
			.sample();

		// then
		then(actual.getStrList()).hasSize(10);
	}

	@Property
	void sizeArray() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.size("strArray", 10)
			.sample();

		// then
		then(actual.getStrArray()).hasSize(10);
	}

	@Property
	void sizeMinMax() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.size("strList", 3, 8)
			.sample();

		// then
		then(actual.getStrList()).hasSizeBetween(3, 8);
	}

	@Property
	void sizeMinIsBiggerThanMax() {
		// when
		thenThrownBy(() ->
			SUT.giveMeBuilder(ComplexObject.class)
				.size("strList", 5, 1)
				.sample()
		).isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("should be min > max");
	}

	@Property
	void minSize() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.minSize("strList", 10)
			.sample();

		// then
		then(actual.getStrList()).hasSizeGreaterThanOrEqualTo(10);
	}

	@Property
	void maxSize() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.maxSize("strList", 10)
			.sample();

		// then
		then(actual.getStrList()).hasSizeLessThanOrEqualTo(10);
	}

	@Property
	void maxSizeZero() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.maxSize("strList", 0)
			.sample();

		// then
		then(actual.getStrList()).hasSizeLessThanOrEqualTo(0);
	}

	@Property
	void notFixedSampleReturnsDiff() {
		// when
		ArbitraryBuilder<ComplexObject> fixedArbitraryBuilder = SUT.giveMeBuilder(ComplexObject.class);

		// then
		ComplexObject sample1 = fixedArbitraryBuilder.sample();
		ComplexObject sample2 = fixedArbitraryBuilder.sample();
		then(sample1).isNotEqualTo(sample2);
	}

	@Property
	void fixedSampleReturnsSame() {
		// when
		ArbitraryBuilder<ComplexObject> fixedArbitraryBuilder = SUT.giveMeBuilder(ComplexObject.class)
			.fixed();

		// then
		ComplexObject sample1 = fixedArbitraryBuilder.sample();
		ComplexObject sample2 = fixedArbitraryBuilder.sample();
		then(sample1).isEqualTo(sample2);
	}

	@Property
	void arbitraryFixedSampleReturnsSame() {
		// when
		ArbitraryBuilder<ComplexObject> fixedArbitraryBuilder = SUT.giveMeBuilder(ComplexObject.class)
			.set("str", Arbitraries.of("value1", "value2"))
			.fixed();

		// then
		ComplexObject sample1 = fixedArbitraryBuilder.sample();
		ComplexObject sample2 = fixedArbitraryBuilder.sample();
		then(sample1).isEqualTo(sample2);
	}

	@Property
	void apply() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("str", "set")
			.apply((it, builder) ->
				builder.size("strList", 1)
					.set("strList[0]", it.getStr())
			)
			.sample()
			.getStrList()
			.get(0);

		then(actual).isEqualTo("set");
	}

	@Property
	void applyWithoutAnyManipulators() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.apply((it, builder) ->
				builder.size("strList", 1)
					.set("strList[0]", it.getStr())
			)
			.sample();

		// then
		String actualStr = actual.getStr();
		List<String> actualStrList = actual.getStrList();
		then(actualStrList).hasSize(1);
		then(actualStrList.get(0)).isEqualTo(actualStr);
	}

	@Property
	void applyNotAffectedManipulatorsAfterApply() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("str", "set")
			.apply((it, builder) ->
				builder.size("strList", 1)
					.set("strList[0]", it.getStr())
			)
			.set("str", "afterApply")
			.sample()
			.getStrList()
			.get(0);

		then(actual).isEqualTo("set");
	}

	@Property
	void acceptIfAlwaysTrue() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.acceptIf(
				it -> true,
				builder -> builder.set("str", "set")
			)
			.sample()
			.getStr();

		then(actual).isEqualTo("set");
	}

	@Property
	void acceptIf() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("str", "set")
			.acceptIf(
				it -> "set".equals(it.getStr()),
				builder -> builder
					.size("strList", 1)
					.set("strList[0]", "set")
			)
			.sample()
			.getStrList()
			.get(0);

		then(actual).isEqualTo("set");
	}

	@Property
	void setRootJavaType() {
		// given
		String expected = "test";

		// when
		String actual = SUT.giveMeBuilder(String.class)
			.set(expected)
			.sample();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setRootComplexType() {
		ComplexObject expected = new ComplexObject();
		expected.setStr("test");

		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.set(expected)
			.sample();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setWithLimit() {
		// when
		List<String> actual = SUT.giveMeBuilder(ComplexObject.class)
			.size("strList", 3)
			.set("strList[*]", "test", 1)
			.sample()
			.getStrList();

		// then
		then(actual).anyMatch("test"::equals);
		then(actual).anyMatch(it -> !"test".equals(it));
	}

	@Property
	void setNull() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNull("str")
			.sample()
			.getStr();

		// then
		then(actual).isNull();
	}

	@Property
	void setNullList() {
		// when
		List<String> actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNull("strList")
			.sample()
			.getStrList();

		// then
		then(actual).isNull();
	}

	@Property
	void setNullMap() {
		// when
		Map<String, SimpleObject> actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNull("map")
			.sample()
			.getMap();

		// then
		then(actual).isNull();
	}

	@Property
	void setNullMapEntry() {
		// when
		Map.Entry<String, SimpleObject> actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNull("mapEntry")
			.sample()
			.getMapEntry();

		// then
		then(actual).isNull();
	}

	@Property
	void setNotNullString() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNotNull("str")
			.sample()
			.getStr();

		// then
		then(actual).isNotNull();
	}

	@Property
	void setNotNullList() {
		// when
		List<String> actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNotNull("strList")
			.sample()
			.getStrList();

		// then
		then(actual).isNotNull();
	}

	@Property
	void setNotNullMap() {
		// when
		Map<String, SimpleObject> actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNotNull("map")
			.sample()
			.getMap();

		// then
		then(actual).isNotNull();
	}

	@Property
	void setNotNullMapEntry() {
		// when
		Map.Entry<String, SimpleObject> actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNotNull("mapEntry")
			.sample()
			.getMapEntry();

		// then
		then(actual).isNotNull();
	}

	@Property
	void setPostCondition() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.setPostCondition("str", String.class, it -> it.length() > 5)
			.sample()
			.getStr();

		// then
		then(actual).hasSizeGreaterThan(5);
	}

	@Property
	void setPostConditionRoot() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.setPostCondition(it -> it.getStr() != null && it.getStr().length() > 5)
			.sample()
			.getStr();

		// then
		then(actual).hasSizeGreaterThan(5);
	}

	@Property
	void setPostConditionWrongTypeThrows() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(ComplexObject.class)
				.setPostCondition("str", Integer.class, it -> it > 5)
				.sample()
		).isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Wrong type filter is applied.");
	}

	@Property
	void mapWhenNull() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNull("str")
			.map(ComplexObject::getStr)
			.sample();

		then(actual).isNull();
	}

	@Property
	void mapWhenNotNull() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNotNull("str")
			.map(ComplexObject::getStr)
			.sample();

		then(actual).isNotNull();
	}

	@Property
	void mapToFixedValue() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.map(it -> "test")
			.sample();

		then(actual).isEqualTo("test");
	}

	@Property
	void mapKeyIsNotNull() {
		// when
		Set<String> actual = SUT.giveMeBuilder(new TypeReference<Map<String, String>>() {
			})
			.sample()
			.keySet();

		then(actual).allMatch(Objects::nonNull);
	}

	@Property(tries = 10)
	void sampleAfterMapTwiceReturnsDiff() {
		ArbitraryBuilder<String> arbitraryBuilder = SUT.giveMeBuilder(ComplexObject.class)
			.set("str", Arbitraries.strings().ascii().filter(it -> !it.isEmpty()))
			.map(ComplexObject::getStr);

		// when
		String actual = arbitraryBuilder.sample();

		// then
		String notExpected = arbitraryBuilder.sample();
		then(actual).isNotEqualTo(notExpected);
	}

	@Property
	void giveMeBuilderWithValue() {
		SimpleObject expected = new SimpleObject();
		expected.setStr("test");
		expected.setOptionalInt(OptionalInt.of(-1));

		SimpleObject actual = SUT.giveMeBuilder(expected)
			.sample();

		then(actual).isEqualTo(expected);
	}

	@Property
	void zipList() {
		// given
		List<ArbitraryBuilder<?>> list = new ArrayList<>();
		list.add(SUT.giveMeBuilder(StringValue.class));
		list.add(SUT.giveMeBuilder(IntValue.class));

		// when
		StringAndInt actual = ArbitraryBuilders.zip(
			list,
			(l) -> {
				StringAndInt result = new StringAndInt();
				result.setValue1((StringValue)l.get(0));
				result.setValue2((IntValue)l.get(1));
				return result;
			}
		).sample();

		then(actual.getValue1()).isNotNull();
		then(actual.getValue2()).isNotNull();
	}

	@Property
	void zipEmptyListThrows() {
		// given
		List<ArbitraryBuilder<?>> list = new ArrayList<>();

		thenThrownBy(
			() -> ArbitraryBuilders.zip(
				list,
				(l) -> new StringAndInt()
			).sample()
		).isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("zip should be used in more than two ArbitraryBuilders, given size");
	}

	@Property
	void zipThree() {
		// given
		ArbitraryBuilder<StringValue> s1 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s1");
		ArbitraryBuilder<StringValue> s2 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s2");
		ArbitraryBuilder<StringValue> s3 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s3");

		// when
		NestedStringList actual = ArbitraryBuilders.zip(s1, s2, s3, (a1, a2, a3) -> {
			List<StringValue> list = new ArrayList<>();
			list.add(a1);
			list.add(a2);
			list.add(a3);

			NestedStringList result = new NestedStringList();
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
		// given
		ArbitraryBuilder<StringValue> s1 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s1");
		ArbitraryBuilder<StringValue> s2 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s2");
		ArbitraryBuilder<StringValue> s3 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s3");

		// when
		NestedStringList actual = s1.zipWith(s2, s3, (a1, a2, a3) -> {
			List<StringValue> list = new ArrayList<>();
			list.add(a1);
			list.add(a2);
			list.add(a3);

			NestedStringList result = new NestedStringList();
			result.setValues(list);
			return result;
		}).sample();

		then(actual.getValues()).hasSize(3);
		then(actual.getValues().get(0).getValue()).isEqualTo("s1");
		then(actual.getValues().get(1).getValue()).isEqualTo("s2");
		then(actual.getValues().get(2).getValue()).isEqualTo("s3");
	}

	@Property
	void zipFour() {
		// given
		ArbitraryBuilder<StringValue> s1 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s1");
		ArbitraryBuilder<StringValue> s2 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s2");
		ArbitraryBuilder<StringValue> s3 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s3");
		ArbitraryBuilder<StringValue> s4 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s4");

		// when
		NestedStringList actual = ArbitraryBuilders.zip(s1, s2, s3, s4, (a1, a2, a3, a4) -> {
			List<StringValue> list = new ArrayList<>();
			list.add(a1);
			list.add(a2);
			list.add(a3);
			list.add(a4);

			NestedStringList result = new NestedStringList();
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
		// given
		ArbitraryBuilder<StringValue> s1 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s1");
		ArbitraryBuilder<StringValue> s2 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s2");
		ArbitraryBuilder<StringValue> s3 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s3");
		ArbitraryBuilder<StringValue> s4 = SUT.giveMeBuilder(StringValue.class)
			.set("value", "s4");

		// when
		NestedStringList actual = s1.zipWith(s2, s3, s4, (a1, a2, a3, a4) -> {
			List<StringValue> list = new ArrayList<>();
			list.add(a1);
			list.add(a2);
			list.add(a3);
			list.add(a4);

			NestedStringList result = new NestedStringList();
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
	void zipWith() {
		// given
		ArbitraryBuilder<String> stringArbitraryBuilder = SUT.giveMeBuilder(String.class);

		// when
		String actual = SUT.giveMeBuilder(Integer.class)
			.zipWith(stringArbitraryBuilder, (integer, string) -> integer + "" + string)
			.sample();

		then(actual).isNotNull();
	}

	@Property
	void zipTwo() {
		// given
		ArbitraryBuilder<String> stringArbitraryBuilder = SUT.giveMeBuilder(String.class);
		ArbitraryBuilder<Integer> integerArbitraryBuilder = SUT.giveMeBuilder(Integer.class);

		// when
		String actual = ArbitraryBuilders.zip(
			stringArbitraryBuilder,
			integerArbitraryBuilder,
			(integer, string) -> integer + "" + string
		).sample();

		then(actual).isNotNull();
	}

	@Property
	void zipReturnsNew() {
		// given
		ArbitraryBuilder<String> stringArbitraryBuilder = SUT.giveMeBuilder(String.class);
		ArbitraryBuilder<Integer> integerArbitraryBuilder = SUT.giveMeBuilder(Integer.class);

		// when
		Arbitrary<String> zippedArbitraryBuilder = ArbitraryBuilders.zip(
			stringArbitraryBuilder,
			integerArbitraryBuilder,
			(integer, string) -> integer + "" + string
		).build();

		// then
		String result1 = zippedArbitraryBuilder.sample();
		String result2 = zippedArbitraryBuilder.sample();
		then(result1).isNotEqualTo(result2);
	}

	@Property
	void setNullFixedReturnsNull() {
		// when
		SimpleObject actual = SUT.giveMeBuilder(SimpleObject.class)
			.setNull("$")
			.fixed()
			.sample();

		then(actual).isNull();
	}

	@Property
	void setNullFixedAndNotNullReturnsNotNull() {
		// when
		SimpleObject actual = SUT.giveMeBuilder(SimpleObject.class)
			.setNull("$")
			.fixed()
			.setNotNull("$")
			.sample();

		then(actual).isNotNull();
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
	void sizeSmallerRemains() {
		String expected = "test";

		List<String> actual = SUT.giveMeBuilder(ComplexObject.class)
			.size("strList", 2)
			.set("strList[0]", expected)
			.set("strList[1]", expected)
			.size("strList", 1)
			.sample()
			.getStrList();

		then(actual).hasSize(1);
		then(actual.get(0)).isEqualTo(expected);
	}

	@Property
	void applySetElementNull() {
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.apply((obj, builder) -> builder.size("strList", 1)
				.setNull("strList[0]")
			)
			.sample()
			.getStrList()
			.get(0);

		then(actual).isNull();
	}

	@Property
	void setAndSetNull() {
		// when
		String actual = SUT.giveMeBuilder(SimpleObject.class)
			.set("str", "test")
			.setNull("str")
			.sample()
			.getStr();

		then(actual).isNull();
	}

	@Property
	void setAfterBuildNotAffected() {
		// given
		ArbitraryBuilder<SimpleObject> builder = SUT.giveMeBuilder(SimpleObject.class);
		Arbitrary<SimpleObject> buildArbitrary = builder.build();

		// when
		ArbitraryBuilder<SimpleObject> actual = builder.set("str", "set");

		// then
		SimpleObject actualSample = actual.sample();
		SimpleObject buildSample = buildArbitrary.sample();
		then(actualSample).isNotEqualTo(buildSample);
		then(actualSample.getStr()).isEqualTo("set");
	}

	@Property
	void applySampleTwiceReturnsDiff() {
		ArbitraryBuilder<SimpleObject> builder = SUT.giveMeBuilder(SimpleObject.class)
			.apply((obj, b) -> {
			});

		SimpleObject actual = builder.sample();

		SimpleObject expected = builder.sample();
		then(actual).isNotEqualTo(expected);
	}

	@Property
	void setPrimitiveToReference() {
		// when
		int integer = SUT.giveMeBuilder(SimpleObject.class)
			.set("integer", Integer.valueOf("1234"))
			.sample()
			.getInteger();

		then(integer).isEqualTo(1234);
	}

	@Property
	void setReferenceToPrimitive() {
		// when
		int integer = SUT.giveMeBuilder(SimpleObject.class)
			.set("wrapperInteger", 1234)
			.sample()
			.getWrapperInteger();

		then(integer).isEqualTo(1234);
	}

	@Property
	void copyValidOnly() {
		thenNoException()
			.isThrownBy(() -> SUT.giveMeBuilder(ListStringObject.class)
				.size("values", 0)
				.validOnly(false)
				.copy()
				.sample());
	}

	@Property
	void generatePrimitiveArray() {
		// when
		int[] actual = SUT.giveMeBuilder(ComplexObject.class)
			.fixed()
			.sample()
			.getIntArray();

		then(actual).isNotNull();
	}

	@Property
	void setFieldWhichObjectIsFixedNull() {
		String expected = "test";

		String actual = SUT.giveMeBuilder(ComplexObject.class)
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

		String actual = SUT.giveMeBuilder(SimpleObject.class)
			.set("$", Arbitraries.just(null))
			.set("str", expected)
			.sample()
			.getStr();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setLazyValue() {
		ArbitraryBuilder<String> variable = SUT.giveMeBuilder(String.class);
		ArbitraryBuilder<String> builder = SUT.giveMeBuilder(String.class)
			.setLazy("$", variable::sample);
		variable.set("test");

		String actual = builder.sample();

		then(actual).isEqualTo("test");
	}

	@Property
	void setLazyValueWithLimit() {
		ArbitraryBuilder<String> variable = SUT.giveMeBuilder(String.class);

		// when
		ArbitraryBuilder<ComplexObject> builder = SUT.giveMeBuilder(ComplexObject.class)
			.size("strList", 3)
			.setLazy("strList[*]", variable::sample, 1);
		variable.set("test");
		List<String> actual = builder.sample().getStrList();

		// then
		then(actual).anyMatch("test"::equals);
		then(actual).anyMatch(it -> !"test".equals(it));
	}

	@Property(tries = 1)
	void setLazyValueSampleReturnsDifferentValue() {
		// when
		ArbitraryBuilder<String> builder = SUT.giveMeBuilder(String.class)
			.setLazy("$", () -> SUT.giveMeOne(String.class));
		String expected = builder.sample();
		String actual = builder.sample();

		then(actual).isNotEqualTo(expected);
	}

	@Property
	void setArbitraryBuilder() {
		String expected = "test";

		String actual = SUT.giveMeBuilder(SimpleObject.class)
			.set("str", SUT.giveMeBuilder(String.class).set("$", expected))
			.sample()
			.getStr();

		then(actual).isEqualTo(expected);
	}

	@Property
	void giveMeListTypeApply() {
		// when
		SimpleObject actual = SUT.giveMeBuilder(new TypeReference<List<SimpleObject>>() {
			})
			.size("$", 1)
			.apply((it, builder) -> builder.set("$[0].str", it.get(0).getInteger() + ""))
			.sample()
			.get(0);

		then(actual.getStr()).isEqualTo(actual.getInteger() + "");
	}

	@Property
	void setIterable() {
		String expected = "test";

		Iterable<String> actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("strIterable", Collections.singletonList(expected))
			.sample()
			.getStrIterable();

		then(actual.iterator().next()).isEqualTo(expected);
	}

	@Property
	void setIterator() {
		String expected = "test";

		Iterator<String> actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("strIterator", Stream.of(expected).iterator())
			.sample()
			.getStrIterator();

		then(actual.next()).isEqualTo(expected);
	}

	@Property
	void setStream() {
		String expected = "test";

		Stream<String> actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("strStream", Stream.of(expected))
			.sample()
			.getStrStream();

		then(actual.collect(Collectors.toList()).get(0)).isEqualTo(expected);
	}

	@Property
	void sampleListWildcardEnum() {
		// when
		List<? extends EnumObject> actual = SUT.giveMeOne(new TypeReference<List<? extends EnumObject>>() {
		});

		then(actual).isNotNull();
	}

	@Property
	void fixedRangedSizeReturnsSameSize() {
		ArbitraryBuilder<ComplexObject> fixedArbitraryBuilder = SUT.giveMeBuilder(ComplexObject.class)
			.size("strList", 1, 5)
			.fixed();

		List<String> actual = fixedArbitraryBuilder
			.sample()
			.getStrList();

		List<String> expected = fixedArbitraryBuilder
			.sample()
			.getStrList();
		then(actual).isEqualTo(expected);
	}

	@Property
	void giveMeManipulatedObjectRemainsManipulator() {
		ListStringObject expected = SUT.giveMeBuilder(ListStringObject.class)
			.size("values", 2, 2)
			.sample();

		ListStringObject actual = SUT.giveMeBuilder(expected)
			.set("values[1]", "test")
			.sample();

		then(actual.getValues().get(1)).isEqualTo("test");
	}

	@Property
	void sampleNotGeneratingStaticField() {
		// when, then
		thenNoException().isThrownBy(() -> SUT.giveMeOne(StaticFieldObject.class));
	}

	@Property
	void setDecomposeContainerTwice() {
		List<String> strings = new ArrayList<>();
		strings.add("test");

		List<String> actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("strList", strings)
			.set("strList", new ArrayList<>())
			.sample()
			.getStrList();

		then(actual).isEmpty();
	}

	@Property
	void setEmptyMap() {
		Map<String, SimpleObject> map = new HashMap<>();
		map.put("test", new SimpleObject());

		Map<String, SimpleObject> actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("map", map)
			.set("map", new HashMap<>())
			.sample()
			.getMap();

		then(actual).isEmpty();
	}

	@Property
	void sampleEnumSet() {
		// when
		Set<TwoEnum> actual = SUT.giveMeOne(new TypeReference<Set<TwoEnum>>() {
		});

		then(actual).hasSizeLessThanOrEqualTo(2);
	}

	@Property
	void setNullFixed() {
		String expected = "test";

		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNull("object")
			.fixed()
			.set("object.str", expected)
			.sample()
			.getObject()
			.getStr();

		then(actual).isEqualTo(expected);

	}

	@Property
	void applyFixedSize() {
		String expectedElement = "test";

		List<String> actual = SUT.giveMeBuilder(ComplexObject.class)
			.minSize("strList", 1)
			.apply((it, builder) -> builder.set("strList[*]", expectedElement))
			.sample()
			.getStrList();

		then(actual).allMatch(expectedElement::equals);
	}

	@Property
	void sampleUri() {
		// when
		URI actual = SUT.giveMeOne(URI.class);

		then(actual).isNotNull();
	}

	@Property
	void sizeNotSetEmptyList() {
		// when
		List<String> actual = SUT.giveMeBuilder(ComplexObject.class)
			.size("strList", 1)
			.set("strList", new ArrayList<>())
			.sample()
			.getStrList();

		then(actual).isEmpty();
	}

	@Property
	void sampleNullableContainerReturnsNotNull() {
		// when
		List<String> values = SUT.giveMeOne(NullableObject.class)
			.getValues();

		then(values).isNotNull();
	}

	@Property
	void sampleUniqueSet() {
		// when
		Set<String> actual = SUT.giveMeBuilder(new TypeReference<Set<String>>() {
			})
			.size("$", 200)
			.sample();

		then(actual).hasSize(200);
	}

	@Property
	void sampleUniqueMap() {
		// when
		Map<String, String> actual = SUT.giveMeBuilder(new TypeReference<Map<String, String>>() {
			})
			.size("$", 200)
			.sample();

		then(actual).hasSize(200);
	}

	@Property
	void sampleEnumMap() {
		Map<TwoEnum, String> values = SUT.giveMeOne(new TypeReference<Map<TwoEnum, String>>() {
		});

		then(values).hasSizeLessThanOrEqualTo(2);
	}

	@Property
	void sizeEnumSetGreaterThanEnumSizeThrows() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(new TypeReference<Set<TwoEnum>>() {
				})
				.size("$", 3)
				.sample()
		).isExactlyInstanceOf(IllegalArgumentException.class);
	}

	@Property
	void sizeEnumMapGreaterThanEnumSizeThrows() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(new TypeReference<Map<TwoEnum, String>>() {
				})
				.size("$", 3)
				.sample()
		).isExactlyInstanceOf(IllegalArgumentException.class);
	}

	@Property
	void sampleEnumMapInMap() {
		thenNoException().isThrownBy(
			() -> SUT.giveMeOne(
				new TypeReference<Map<String, Map<TwoEnum, String>>>() {
				})
		);
	}

	@Property
	void setInterface() {
		InterfaceImplementation expected = new InterfaceImplementation();
		expected.setValue("test");

		Interface actual = SUT.giveMeBuilder(InterfaceFieldValue.class)
			.set("value", expected)
			.sample()
			.getValue();

		then(actual).isEqualTo(expected);
	}

	@Property
	void sampleEnumSetList() {
		thenNoException().isThrownBy(
			() -> SUT.giveMeBuilder(new TypeReference<List<Set<TwoEnum>>>() {
				})
				.size("$", 3)
				.sample()
		);
	}

	@Property
	void sampleSelfRecursiveObject() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.build();

		SelfRecursiveObject actual = sut.giveMeOne(SelfRecursiveObject.class);

		then(actual.getValue()).isNotNull();
		then(actual.getRecursive()).isNotNull();
		then(actual.getRecursive().getValue()).isNotNull();
	}

	@Property
	void sampleSelfRecursiveList() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.build();

		SelfRecursiveListObject actual = sut.giveMeOne(SelfRecursiveListObject.class);

		then(actual.getValue()).isNotNull();
		then(actual.getRecursives()).isNotNull();
	}

	@Property
	void sampleRecursiveObject() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.build();

		RecursiveLeftObject actual = sut.giveMeOne(RecursiveLeftObject.class);

		then(actual.getValue()).isNotNull();
		then(actual.getRecursive()).isNotNull();
		then(actual.getRecursive().getValue()).isNotNull();
	}

	@Property
	void sampleGeneric() {
		String actual = SUT.giveMeBuilder(new TypeReference<GenericValue<String>>() {
			})
			.setNotNull("value")
			.sample()
			.getValue();

		then(actual).isNotNull();
	}

	@Property
	void sampleGenericWildcardExtends() {
		String actual = SUT.giveMeBuilder(new TypeReference<GenericValue<? extends String>>() {
			})
			.setNotNull("value")
			.sample()
			.getValue();

		then(actual).isNotNull();
	}

	@Property
	void sampleStringGenericField() {
		GenericStringWrapperValue actual = SUT.giveMeOne(GenericStringWrapperValue.class);

		then(actual).isNotNull();
	}

	@Property
	void sampleGenericField() {
		GenericWrapperValue<String> actual = SUT.giveMeOne(new TypeReference<GenericWrapperValue<String>>() {
		});

		then(actual).isNotNull();
	}

	@Property
	void sampleGenericChild() {
		GenericChildValue actual = SUT.giveMeOne(GenericChildValue.class);

		then(actual).isNotNull();
	}

	@Property
	void sampleTwoGenericChild() {
		GenericChildTwoValue actual = SUT.giveMeOne(GenericChildTwoValue.class);

		then(actual).isNotNull();
	}

	@Property
	void sampleObjectField() {
		ObjectValue actual = SUT.giveMeOne(ObjectValue.class);

		then(actual).isNotNull();
	}

	@Property
	void sampleChildValue() {
		ChildValue actual = SUT.giveMeOne(ChildValue.class);

		then(actual).isNotNull();
	}

	@Property
	void sampleWildcard() {
		thenNoException()
			.isThrownBy(() -> SUT.giveMeOne(new TypeReference<List<? extends SimpleObject>>() {
			}));
	}

	@Property
	void nestedSize() {
		// when
		List<List<String>> actual = SUT.giveMeBuilder(new TypeReference<List<List<String>>>() {
			})
			.size("$[0]", 10)
			.size("$", 2)
			.sample();

		// then
		then(actual).hasSize(2);
		then(actual.get(0)).hasSize(10);
	}

	@Property
	void sampleMapValueWildcardListString() {
		thenNoException()
			.isThrownBy(() -> SUT.giveMeOne(new TypeReference<Map<String, ? extends List<String>>>() {
			}));
	}

	@Property
	void sampleGenericSimpleObject() {
		thenNoException()
			.isThrownBy(() -> SUT.giveMeOne(new TypeReference<GenericSimpleObject>() {
			}));
	}

	@Property
	void sizeThirdNestedNested() {
		List<String> actual = SUT.giveMeBuilder(ThirdNestedListStringObject.class)
			.size("values", 1)
			.size("values[0].values", 1)
			.size("values[0].values[0].values", 1)
			.sample()
			.getValues()
			.get(0)
			.getValues()
			.get(0)
			.getValues();

		then(actual).hasSize(1);
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
	void sizeAfterSetEmptyListReturnsSized() {
		List<String> actual = SUT.giveMeBuilder(new TypeReference<List<String>>() {
			})
			.set("$", new ArrayList<>())
			.size("$", 3)
			.sample();

		then(actual).hasSize(3);
	}

	@Property
	void greaterSizeAfterSetReturnsSizeRemainsSet() {
		List<String> set = new ArrayList<>();
		set.add("1");

		List<String> actual = SUT.giveMeBuilder(new TypeReference<List<String>>() {
			})
			.set("$", set)
			.size("$", 3)
			.sample();

		then(actual).hasSize(3);
		then(actual.get(0)).isEqualTo("1");
	}

	@Property
	void smallerSizeAfterSetReturnsSizeRemainsSet() {
		List<String> set = new ArrayList<>();
		set.add("1");
		set.add("2");
		set.add("3");
		set.add("4");
		set.add("5");

		List<String> actual = SUT.giveMeBuilder(new TypeReference<List<String>>() {
			})
			.set("$", set)
			.size("$", 3)
			.sample();

		then(actual).hasSize(3);
		then(actual.get(0)).isEqualTo("1");
		then(actual.get(1)).isEqualTo("2");
		then(actual.get(2)).isEqualTo("3");
	}

	@Property
	void setNotNull() {
		String actual = SUT.giveMeBuilder(SimpleObject.class)
			.set("str", NOT_NULL)
			.sample()
			.getStr();

		then(actual).isNotNull();
	}

	@Property
	void setNotNullZoneId() {
		ZoneId actual = SUT.giveMeBuilder(SimpleObject.class)
			.setNotNull("zoneId")
			.sample()
			.getZoneId();

		then(actual).isNotNull();
	}

	@Property
	void setJustSubPropertyNotChanged() {
		String notExpected = "test";

		String actual = SUT.giveMeBuilder(SimpleObject.class)
			.set("$", Values.just(new SimpleObject()))
			.set("str", notExpected)
			.sample()
			.getStr();

		then(actual).isNotEqualTo(notExpected);
	}

	@Property
	void setPostConditionPrimitiveType() {
		int actual = SUT.giveMeBuilder(SimpleObject.class)
			.setPostCondition("integer", Integer.class, i -> i > 0)
			.sample()
			.getInteger();

		then(actual).isGreaterThan(0);
	}
}
