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

import static com.navercorp.fixturemonkey.customizer.Values.NOT_NULL;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Property;
import net.jqwik.api.PropertyDefaults;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.customizer.Values;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ComplexObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.DeepObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.Interface;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.InterfaceHolder;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.InterfaceImplementation;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SimpleObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringListWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringPair;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringWrapperList;

@PropertyDefaults(tries = 10)
class CustomizationAdapterTest {

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.defaultNotNull(true)
		.plugin(new JavaNodeTreeAdapterPlugin())
		.build();

	@Property
	void set() {
		// when
		StringWrapper actual = SUT.giveMeBuilder(StringWrapper.class).set("value", "str").sample();

		// then
		then(actual.getValue()).isEqualTo("str");
	}

	@Property
	void setDecomposedList() {
		// given
		List<String> expected = java.util.Arrays.asList("a", "b", "c", "d", "e");

		// when
		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class).set("values", expected).sample().getValues();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setNotNullString() {
		// when
		String actual = SUT.giveMeBuilder(StringWrapper.class).setNotNull("value").sample().getValue();

		// then
		then(actual).isNotNull();
	}

	@Property
	void setNotNullList() {
		// when
		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class).setNotNull("values").sample().getValues();

		// then
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
	void setAndSetNull() {
		// when
		String actual = SUT.giveMeBuilder(StringWrapper.class)
			.set("value", "test")
			.setNull("value")
			.sample()
			.getValue();

		then(actual).isNull();
	}

	@Property
	void setRootJavaType() {
		// given
		String expected = "test";

		// when
		String actual = SUT.giveMeBuilder(String.class).set(expected).sample();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setStringWrapperListElement() {
		String expected = "nested-test";

		// when
		StringWrapperList actual = SUT.giveMeBuilder(StringWrapperList.class)
			.size("values", 1)
			.set("values[0].value", expected)
			.sample();

		// then
		then(actual.getValues()).hasSize(1);
		then(actual.getValues().get(0).getValue()).isEqualTo(expected);
	}

	@Property
	void setComplexObjectField() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class).set("str", "test-str").sample();

		// then
		then(actual.getStr()).isEqualTo("test-str");
	}

	@Property
	void setDecomposedMap() {
		// given
		Map<String, SimpleObject> expected = new java.util.HashMap<>();
		expected.put("a", new SimpleObject());
		expected.put("b", new SimpleObject());

		// when
		Map<String, SimpleObject> actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("map", expected)
			.sample()
			.getMap();

		then(actual).isEqualTo(expected);
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
		ArbitraryBuilder<SimpleObject> optionalString = SUT.giveMeBuilder(SimpleObject.class).set(
			"optionalString",
			optional
		);
		Optional<String> actual = optionalString.sample().getOptionalString();

		then(actual).isEqualTo(optional);
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
		}).set(expected).sample();

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
	void setDecomposedSupplier() {
		// given
		Supplier<String> expected = () -> "test";

		// when
		Supplier<String> actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("strSupplier", Values.just(expected))
			.sample()
			.getStrSupplier();

		then(actual.get()).isEqualTo(expected.get());
	}

	@Property
	void setDecomposedNestedStrSupplier() {
		// given
		Supplier<Supplier<String>> expected = () -> () -> "test";

		// when
		Supplier<Supplier<String>> actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("nestedStrSupplier", Values.just(expected))
			.sample()
			.getNestedStrSupplier();

		then(actual.get()).isEqualTo(expected.get());
	}

	@Property
	void setAllFields() {
		// when
		StringPair actual = SUT.giveMeBuilder(StringPair.class).set("*", "str").sample();

		then(actual.getValue1()).isEqualTo("str");
		then(actual.getValue2()).isEqualTo("str");
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
	void setRootComplexType() {
		ComplexObject expected = new ComplexObject();
		expected.setStr("test");

		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class).set(expected).sample();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setNullMap() {
		// when
		Map<String, SimpleObject> actual = SUT.giveMeBuilder(ComplexObject.class).setNull("map").sample().getMap();

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
	void setNullSupplier() {
		// when
		Supplier<String> actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNull("strSupplier")
			.sample()
			.getStrSupplier();

		// then
		then(actual).isNull();
	}

	@Property
	void setNotNullMap() {
		// when
		Map<String, SimpleObject> actual = SUT.giveMeBuilder(ComplexObject.class).setNotNull("map").sample().getMap();

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
	void setNotNullSupplier() {
		// when
		Supplier<String> actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNotNull("strSupplier")
			.sample()
			.getStrSupplier();

		// then
		then(actual).isNotNull();
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
		int integer = SUT.giveMeBuilder(SimpleObject.class).set("wrapperInteger", 1234).sample().getWrapperInteger();

		then(integer).isEqualTo(1234);
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
	void setInterface() {
		InterfaceImplementation expected = new InterfaceImplementation();
		expected.setValue("test");

		Interface actual = SUT.giveMeBuilder(InterfaceHolder.class).set("value", expected).sample().getValue();

		then(actual).isEqualTo(expected);
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
	void setLazyValue() {
		ArbitraryBuilder<String> variable = SUT.giveMeBuilder(String.class);
		ArbitraryBuilder<String> builder = SUT.giveMeBuilder(String.class).setLazy("$", variable::sample);
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

	@Property
	void setLazyValueSampleReturnsDifferentValue() {
		// when
		ArbitraryBuilder<String> builder = SUT.giveMeBuilder(String.class).setLazy("$", () ->
			SUT.giveMeOne(String.class)
		);
		String expected = builder.sample();
		String actual = builder.sample();

		then(actual).isNotEqualTo(expected);
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
	void setNotNull() {
		String actual = SUT.giveMeBuilder(SimpleObject.class).set("str", NOT_NULL).sample().getStr();

		then(actual).isNotNull();
	}

	@Property
	void setSupplierObjectField() {
		String actual = SUT.giveMeBuilder(new TypeReference<Supplier<SimpleObject>>() {
			})
			.set("str", "expected")
			.sample()
			.get()
			.getStr();

		then(actual).isEqualTo("expected");
	}

	@Property
	void setNestedSupplierObjectField() {
		String actual = SUT.giveMeBuilder(new TypeReference<Supplier<Supplier<SimpleObject>>>() {
			})
			.set("str", "expected")
			.sample()
			.get()
			.get()
			.getStr();

		then(actual).isEqualTo("expected");
	}

	@Property
	void setSupplierObjectFieldUsingRootExp() {
		String actual = SUT.giveMeBuilder(new TypeReference<Supplier<SimpleObject>>() {
			})
			.set("$.str", "expected")
			.sample()
			.get()
			.getStr();

		then(actual).isEqualTo("expected");
	}

	@Property
	void setOptionalObjectField() {
		String actual = SUT.giveMeBuilder(new TypeReference<Optional<SimpleObject>>() {
			})
			.set("str", "expected")
			.sample()
			.get()
			.getStr();

		then(actual).isEqualTo("expected");
	}

	@Property
	void setOptionalObjectFieldUsingRootExp() {
		String actual = SUT.giveMeBuilder(new TypeReference<Optional<SimpleObject>>() {
			})
			.set("$.str", "expected")
			.sample()
			.get()
			.getStr();

		then(actual).isEqualTo("expected");
	}

	@Property
	void setDecomposeContainerTwice() {
		List<String> strings = new java.util.ArrayList<>();
		strings.add("test");

		List<String> actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("strList", strings)
			.set("strList", new java.util.ArrayList<>())
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
	void setPostConditionPrimitiveType() {
		int actual = SUT.giveMeBuilder(SimpleObject.class)
			.setPostCondition("integer", Integer.class, i -> i > 0)
			.sample()
			.getInteger();

		then(actual).isGreaterThan(0);
	}

	@Property
	void setNotNullZoneId() {
		ZoneId actual = SUT.giveMeBuilder(SimpleObject.class).setNotNull("zoneId").sample().getZoneId();

		then(actual).isNotNull();
	}

	@Property
	void setNull() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class).setNull("str").sample().getStr();

		// then
		then(actual).isNull();
	}

	@Property
	void setNullFixedAndNotNullReturnsNotNull() {
		// when
		SimpleObject actual = SUT.giveMeBuilder(SimpleObject.class).setNull("$").fixed().setNotNull("$").sample();

		then(actual).isNotNull();
	}

	@Property
	void setNullList() {
		// when
		List<String> actual = SUT.giveMeBuilder(ComplexObject.class).setNull("strList").sample().getStrList();

		// then
		then(actual).isNull();
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
		thenThrownBy(() ->
			SUT.giveMeBuilder(ComplexObject.class)
				.setPostCondition("str", Integer.class, it -> it > 5)
				.sample()
		)
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Wrong type filter is applied.");
	}

	@Property
	void setNullNestedObject() {
		// when
		SimpleObject actual = SUT.giveMeBuilder(ComplexObject.class).setNull("object").sample().getObject();

		// then
		then(actual).isNull();
	}

	@Property
	void setNullIntArray() {
		// when
		int[] actual = SUT.giveMeBuilder(ComplexObject.class).setNull("intArray").sample().getIntArray();

		// then
		then(actual).isNull();
	}

	@Property
	void setNullStringArray() {
		// when
		String[] actual = SUT.giveMeBuilder(ComplexObject.class).setNull("strArray").sample().getStrArray();

		// then
		then(actual).isNull();
	}

	@Property
	void setNullRootType() {
		// when
		SimpleObject actual = SUT.giveMeBuilder(SimpleObject.class).setNull("$").sample();

		// then
		then(actual).isNull();
	}

	@Property
	void setNullOptional() {
		// when
		Optional<String> actual = SUT.giveMeBuilder(new TypeReference<Optional<String>>() {
		}).setNull("$").sample();

		// then
		then(actual).isNull();
	}

	@Property
	void setNullNestedField() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class).setNull("object.str").sample().getObject().getStr();

		// then
		then(actual).isNull();
	}

	@Property
	void setNullListElement() {
		// given
		List<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");

		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("strList", list)
			.setNull("strList[0]")
			.sample();

		// then
		then(actual.getStrList().get(0)).isNull();
		then(actual.getStrList().get(1)).isEqualTo("b");
	}

	@Property
	void setNullListAllElements() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class).size("strList", 3).setNull("strList[*]").sample();

		// then
		then(actual.getStrList()).hasSize(3);
		then(actual.getStrList()).containsOnlyNulls();
	}

	@Property
	void setNotNullNestedObject() {
		// when
		SimpleObject actual = SUT.giveMeBuilder(ComplexObject.class).setNotNull("object").sample().getObject();

		// then
		then(actual).isNotNull();
	}

	@Property
	void setNotNullIntArray() {
		// when
		int[] actual = SUT.giveMeBuilder(ComplexObject.class).setNotNull("intArray").sample().getIntArray();

		// then
		then(actual).isNotNull();
	}

	@Property
	void setNotNullStringArray() {
		// when
		String[] actual = SUT.giveMeBuilder(ComplexObject.class).setNotNull("strArray").sample().getStrArray();

		// then
		then(actual).isNotNull();
	}

	@Property
	void setNotNullRootType() {
		// when
		SimpleObject actual = SUT.giveMeBuilder(SimpleObject.class).setNotNull("$").sample();

		// then
		then(actual).isNotNull();
	}

	@Property
	void setNotNullNestedField() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class).setNotNull("object.str").sample().getObject().getStr();

		// then
		then(actual).isNotNull();
	}

	@Property
	void setNullThenSet() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class).setNull("str").set("str", "override").sample().getStr();

		// then
		then(actual).isEqualTo("override");
	}

	@Property
	void setThenSetNullThenSet() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("str", "first")
			.setNull("str")
			.set("str", "last")
			.sample()
			.getStr();

		// then
		then(actual).isEqualTo("last");
	}

	@Property
	void setNullAndSetNotNullDifferentFields() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class).setNull("str").setNotNull("object").sample();

		// then
		then(actual.getStr()).isNull();
		then(actual.getObject()).isNotNull();
	}

	@Property
	void setNullThenSetNotNullSameField() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class).setNull("str").setNotNull("str").sample().getStr();

		// then
		then(actual).isNotNull();
	}

	@Property
	void setNotNullThenSetNullSameField() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class).setNotNull("str").setNull("str").sample().getStr();

		// then
		then(actual).isNull();
	}

	@Property
	void setChildThenSetNullParent() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("object.str", "child")
			.setNull("object")
			.sample();

		// then
		then(actual.getObject()).isNull();
	}

	@Property
	void setDeepChildThenSetNullAncestor() {
		// when
		DeepObject actual = SUT.giveMeBuilder(DeepObject.class)
			.set("policy.threshold.limit", BigDecimal.TEN)
			.setNull("policy")
			.sample();

		// then
		then(actual.getPolicy()).isNull();
	}

	@Property
	void setNullThenSetLazy() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNull("str")
			.setLazy("str", () -> "lazy")
			.sample()
			.getStr();

		// then
		then(actual).isEqualTo("lazy");
	}

	@Property
	void setLazyThenSetNull() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.setLazy("str", () -> "lazy")
			.setNull("str")
			.sample()
			.getStr();

		// then
		then(actual).isNull();
	}

	@Property
	void setNotNullWithPostCondition() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNotNull("str")
			.setPostCondition("str", String.class, it -> it.length() > 0)
			.sample()
			.getStr();

		// then
		then(actual).isNotNull();
		then(actual).hasSizeGreaterThan(0);
	}

	@Property
	void setNotNullThenSet() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNotNull("str")
			.set("str", "override")
			.sample()
			.getStr();

		// then
		then(actual).isEqualTo("override");
	}

	@Property
	void setThenSetNotNull() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class).set("str", "value").setNotNull("str").sample().getStr();

		// then
		then(actual).isNotNull();
	}

	@Property
	void setNotNullThenSetLazy() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNotNull("str")
			.setLazy("str", () -> "lazy")
			.sample()
			.getStr();

		// then
		then(actual).isEqualTo("lazy");
	}

	@Property
	void setLazyThenSetNotNull() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.setLazy("str", () -> "lazy")
			.setNotNull("str")
			.sample()
			.getStr();

		// then
		then(actual).isNotNull();
	}

	@Property
	void setNotNullThenSize() {
		// when
		List<String> actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNotNull("strList")
			.size("strList", 3)
			.sample()
			.getStrList();

		// then
		then(actual).hasSize(3);
	}

	@Property
	void sizeThenSetNotNull() {
		// when
		List<String> actual = SUT.giveMeBuilder(ComplexObject.class)
			.size("strList", 3)
			.setNotNull("strList")
			.sample()
			.getStrList();

		// then
		then(actual).isNotNull();
	}

	@Property
	void sizeThenSetNull() {
		// when
		List<String> actual = SUT.giveMeBuilder(ComplexObject.class)
			.size("strList", 3)
			.setNull("strList")
			.sample()
			.getStrList();

		// then
		then(actual).isNull();
	}

	@Property
	void setNullThenPostCondition() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNull("str")
			.setPostCondition("str", String.class, it -> it == null || it.length() > 0)
			.sample();

		// then
		then(actual.getStr()).isNull();
	}

	@Property
	void postConditionThenSetNotNull() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.setPostCondition("str", String.class, it -> it.length() > 0)
			.setNotNull("str")
			.sample()
			.getStr();

		// then
		then(actual).isNotNull();
	}

	@Property
	void setNullSetThenSize() {
		// when
		Set<String> actual = SUT.giveMeBuilder(new TypeReference<Set<String>>() {
		}).setNull("$").size("$", 3).sample();

		// then
		then(actual).hasSize(3);
	}

	@Property
	void setNullArrayThenSize() {
		// when
		String[] actual = SUT.giveMeBuilder(new TypeReference<String[]>() {
		}).setNull("$").size("$", 3).sample();

		// then
		then(actual).hasSize(3);
	}

	@Property
	void setNullParentThenSetChild() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNull("object")
			.set("object.str", "child")
			.sample();

		// then
		then(actual.getObject()).isNotNull();
		then(actual.getObject().getStr()).isEqualTo("child");
	}

	@Property
	void setNotNullParentThenSetChild() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNotNull("object")
			.set("object.str", "child")
			.sample();

		// then
		then(actual.getObject()).isNotNull();
		then(actual.getObject().getStr()).isEqualTo("child");
	}

	@Property
	void setChildThenSetNotNullParent() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("object.str", "child")
			.setNotNull("object")
			.sample();

		// then
		then(actual.getObject()).isNotNull();
	}

	@Property
	void setNullContainerThenSetNullElement() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.size("strList", 3)
			.setNull("strList")
			.setNull("strList[0]")
			.sample();

		// then
		then(actual.getStrList()).isNotNull();
		then(actual.getStrList()).hasSize(3);
		then(actual.getStrList().get(0)).isNull();
	}

	@Property
	void setNullElementThenSetNullContainer() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.size("strList", 3)
			.setNull("strList[0]")
			.setNull("strList")
			.sample();

		// then
		then(actual.getStrList()).isNull();
	}

	@Property
	void postConditionThenSetNull() {
		thenThrownBy(() ->
			SUT.giveMeBuilder(ComplexObject.class)
				.setPostCondition("str", String.class, it -> it != null && it.length() > 0)
				.setNull("str")
				.sample()
		).isExactlyInstanceOf(IllegalArgumentException.class);
	}

	@Property
	void setNotNullListElement() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.size("strList", 3)
			.setNotNull("strList[0]")
			.sample();

		// then
		then(actual.getStrList().get(0)).isNotNull();
	}

	@Property
	void setNotNullListAllElements() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.size("strList", 3)
			.setNotNull("strList[*]")
			.sample();

		// then
		then(actual.getStrList()).hasSize(3);
		then(actual.getStrList()).allMatch(it -> it != null);
	}

	@Property
	void setNotNullTwiceSameField() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class).setNotNull("str").setNotNull("str").sample().getStr();

		// then
		then(actual).isNotNull();
	}

	@Property
	void setNullThenSetNullThenSetNotNull() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNull("str")
			.setNull("str")
			.setNotNull("str")
			.sample()
			.getStr();

		// then
		then(actual).isNotNull();
	}

	@Property
	void setNullThenSetLazyThenPostCondition() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNull("str")
			.setLazy("str", () -> "lazy")
			.setPostCondition("str", String.class, it -> it != null && it.length() > 0)
			.sample()
			.getStr();

		// then
		then(actual).isEqualTo("lazy");
	}

	@Property
	void setLazyThenPostConditionThenSetNull() {
		thenThrownBy(() ->
			SUT.giveMeBuilder(ComplexObject.class)
				.setLazy("str", () -> "lazy")
				.setPostCondition("str", String.class, it -> it != null && it.length() > 0)
				.setNull("str")
				.sample()
		).isExactlyInstanceOf(IllegalArgumentException.class);
	}

	@Property
	void postConditionThenSetNullThenSetLazy() {
		// when
		String actual = SUT.giveMeBuilder(ComplexObject.class)
			.setPostCondition("str", String.class, it -> it != null && it.length() > 0)
			.setNull("str")
			.setLazy("str", () -> "lazy")
			.sample()
			.getStr();

		// then
		then(actual).isEqualTo("lazy");
	}

	@Property
	void setNotNullThenSizeThenPostCondition() {
		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.setNotNull("strList")
			.size("strList", 2)
			.setPostCondition("strList", List.class, it -> it != null && it.size() == 2)
			.sample();

		// then
		then(actual.getStrList()).isNotNull();
		then(actual.getStrList()).hasSize(2);
	}

	@Property
	void setLazyContainerThenSize() {
		// when
		StringListWrapper actual = SUT.giveMeBuilder(StringListWrapper.class)
			.setLazy("values", () -> new ArrayList<>(Arrays.asList("a", "b")))
			.size("values", 5)
			.sample();

		// then
		then(actual.getValues()).hasSize(5);
	}

	@Property
	void sizeThenSetLazyElement() {
		// when
		StringListWrapper actual = SUT.giveMeBuilder(StringListWrapper.class)
			.size("values", 3)
			.setLazy("values[0]", () -> "LAZY_FIRST")
			.sample();

		// then
		then(actual.getValues()).hasSize(3);
		then(actual.getValues().get(0)).isEqualTo("LAZY_FIRST");
	}

	@Property
	void setNonPublicJdkListThenApplyOverridesElements() {
		// given
		List<String> arraysAsList = Arrays.asList("a", "b");

		// when
		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class)
			.set("values", arraysAsList)
			.thenApply((obj, builder) ->
				builder.set("values[0]", "override")
			)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(2);
		then(actual.get(0)).isEqualTo("override");
		then(actual.get(1)).isEqualTo("b");
	}

	@Property
	void setCollectionsSingletonListThenApplyOverridesElements() {
		// given
		List<String> singletonList = Collections.singletonList("original");

		// when
		List<String> actual = SUT.giveMeBuilder(StringListWrapper.class)
			.set("values", singletonList)
			.thenApply((obj, builder) ->
				builder.set("values[0]", "override")
			)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(1);
		then(actual.get(0)).isEqualTo("override");
	}

	@Property
	void setIterableWithArraysAsListThenApplyOverridesElements() {
		// given
		List<String> arraysAsList = Arrays.asList("a", "b");

		// when
		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.set("strIterable", arraysAsList)
			.thenApply((obj, builder) ->
				builder.set("strIterable[0]", "override")
			)
			.sample();

		// then
		List<String> values = new ArrayList<>();
		actual.getStrIterable().forEach(values::add);
		then(values).hasSize(2);
		then(values.get(0)).isEqualTo("override");
		then(values.get(1)).isEqualTo("b");
	}
}
