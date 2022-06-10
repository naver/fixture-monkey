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

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.LabMonkey;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.builder.ArbitraryBuilder;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04TestSpecs.ComplexObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04TestSpecs.SimpleObject;

class FixtureMonkeyV04Test {
	private static final LabMonkey SUT = LabMonkey.labMonkey();

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
		then(actual.getStr()).isEqualTo(expected.getStr());
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
		Set<String> actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("strSet", expected)
			.sample()
			.getStrSet();

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

	@Property
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
	void strictModeSetWrongExpressionThrows() {
		LabMonkey labMonkey = LabMonkey.labMonkeyBuilder().useStrictMode().build();

		thenThrownBy(
			() -> labMonkey.giveMeBuilder(ComplexObject.class)
				.set("nonExistentField", 0)
				.sample()
		).isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No matching results for given expression.");
	}
}
