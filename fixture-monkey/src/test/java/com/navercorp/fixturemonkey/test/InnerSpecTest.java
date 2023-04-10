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
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.customizer.InnerSpec;
import com.navercorp.fixturemonkey.test.InnerSpecTestSpecs.ComplexObject;
import com.navercorp.fixturemonkey.test.InnerSpecTestSpecs.ComplexObjectObject;
import com.navercorp.fixturemonkey.test.InnerSpecTestSpecs.IntegerMapObject;
import com.navercorp.fixturemonkey.test.InnerSpecTestSpecs.ListStringObject;
import com.navercorp.fixturemonkey.test.InnerSpecTestSpecs.MapObject;
import com.navercorp.fixturemonkey.test.InnerSpecTestSpecs.NestedKeyMapObject;
import com.navercorp.fixturemonkey.test.InnerSpecTestSpecs.NestedListStringObject;
import com.navercorp.fixturemonkey.test.InnerSpecTestSpecs.SimpleObject;

class InnerSpecTest {
	private static final FixtureMonkey SUT = FixtureMonkey.create();

	@Property
	void key() {
		// when
		Map<String, String> actual = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("strMap", m -> m.minSize(1).key("key"))
			)
			.sample()
			.getStrMap();

		then(actual.keySet()).contains("key");
	}

	@Property
	void value() {
		// when
		Map<String, String> actual = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("strMap", m -> m.minSize(1).value("value"))
			)
			.sample()
			.getStrMap();

		then(actual.values()).contains("value");
	}

	@Property
	void entry() {
		// when
		Map<String, String> actual = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("strMap", m -> m.minSize(1).entry("key", "value"))
			)
			.sample()
			.getStrMap();

		then(actual.get("key")).isEqualTo("value");
	}

	@Property
	void keys() {
		// when
		Map<String, String> actual = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("strMap", m -> m.minSize(3).keys("key1", "key2", "key3"))
			)
			.sample()
			.getStrMap();

		then(actual.keySet()).containsAll(
			Stream.of("key1", "key2", "key3").collect(Collectors.toCollection(HashSet::new))
		);
	}

	@Property
	void values() {
		// when
		Map<String, String> actual = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("strMap", m -> m.minSize(3).values("value1", "value2", "value3"))
			)
			.sample()
			.getStrMap();

		then(actual.values()).containsAll(
			Stream.of("value1", "value2", "value3").collect(Collectors.toCollection(HashSet::new))
		);
	}

	@Property
	void entries() {
		// when
		Map<String, String> actual = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("strMap", m -> m.minSize(2).entries("key1", "value1", "key2", "value2"))
			)
			.sample()
			.getStrMap();

		then(actual.get("key1")).isEqualTo("value1");
		then(actual.get("key2")).isEqualTo("value2");
	}

	@Property
	void entryTwice() {
		// when
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("strMap",
						m -> m.minSize(2)
							.entry("key1", "value1")
							.entry("key2", "value2")
					)
			)
			.sample();

		then(actual.getStrMap().get("key1")).isEqualTo("value1");
		then(actual.getStrMap().get("key2")).isEqualTo("value2");
	}

	@Property
	void valueNull() {
		// when
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("strMap", m -> m.minSize(1).value(null))
			)
			.sample();

		then(actual.getStrMap().containsValue(null)).isTrue();
	}

	@Property
	void keyNullThrows() {
		thenThrownBy(() ->
			SUT.giveMeBuilder(MapObject.class)
				.setInner(
					new InnerSpec()
						.property("strMap", m -> m.minSize(1).key(null))
				)
				.sample()
		).isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Map key cannot be null.");
	}

	@Property
	void keyInKey() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(1, 3))
			.build();

		NestedKeyMapObject actual = sut.giveMeBuilder(NestedKeyMapObject.class)
			.setInner(
				new InnerSpec()
					.property("mapKeyMap", m -> m.key(k -> k.key("key")))
			)
			.sample();

		List<String> keyList = actual.getMapKeyMap().keySet().stream()
			.flatMap(it -> it.keySet().stream()).collect(Collectors.toList());
		then(keyList).contains("key");
	}

	@Property
	void valueInKey() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(1, 3))
			.build();

		NestedKeyMapObject actual = sut.giveMeBuilder(NestedKeyMapObject.class)
			.setInner(
				new InnerSpec().property("mapKeyMap", m -> m.key(k -> k.value("value")))
			)
			.sample();

		List<String> keyList = actual.getMapKeyMap().keySet().stream()
			.flatMap(it -> it.values().stream()).collect(Collectors.toList());
		then(keyList).contains("value");
	}

	@Property
	void keyInValue() {
		// when
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("mapValueMap", m -> m.minSize(1).value(v -> v.minSize(1).key("key")))
			)
			.sample();

		List<String> valueList = actual.getMapValueMap().values().stream()
			.flatMap(it -> it.keySet().stream()).collect(Collectors.toList());
		then(valueList).contains("key");
	}

	@Property
	void valueInValue() {
		// when
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("mapValueMap", m -> m.minSize(1).value(v -> v.minSize(1).value("value")))
			)
			.sample();

		List<String> valueList = actual.getMapValueMap().values().stream()
			.flatMap(it -> it.values().stream()).collect(Collectors.toList());
		then(valueList).contains("value");
	}

	@Property
	void sizeInValue() {
		// when
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("listValueMap", m -> m.size(1)
						.value(v -> v.size(10)))
			)
			.sample();

		List<Integer> sizeList = actual.getListValueMap().values().stream()
			.map(List::size).collect(Collectors.toList());
		then(sizeList).contains(10);
	}

	@Property
	void listElementInValue() {
		// when
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("listValueMap", m -> m.size(1)
						.value(v -> {
							v.size(1);
							v.listElement(0, "test");
						}))
			)
			.sample();

		List<String> elementList = actual.getListValueMap().values().stream()
			.flatMap(List::stream).collect(Collectors.toList());
		then(elementList).contains("test");
	}

	@Property
	void propertyInValue() {
		// when
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("objectValueMap", m -> m.size(1)
						.value(v -> v.property("str", "test"))
					)
			)
			.sample();

		List<String> fieldList = actual.getObjectValueMap().values().stream().filter(Objects::nonNull)
			.map(SimpleObject::getStr).collect(Collectors.toList());
		then(fieldList).contains("test");
	}

	@Property
	void entryInEntryValue() {
		// when
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("mapValueMap",
						m -> m.minSize(1).entry("key1", v -> v.minSize(1).entry("key2", "value"))
					)
			)
			.sample();

		Map<String, String> value = actual.getMapValueMap().get("key1");
		then(value.get("key2")).isEqualTo("value");
	}

	@SuppressWarnings("OptionalGetWithoutIsPresent")
	@Property
	void entryInEntryKey() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(1, 3))
			.build();

		// when
		NestedKeyMapObject actual = sut.giveMeBuilder(NestedKeyMapObject.class)
			.setInner(
				new InnerSpec()
					.property("mapKeyMap",
						m -> m.entry(k -> k.entry("key", "value2"), "value1")
					)
			)
			.sample();

		// then
		Map<String, String> expected = actual.getMapKeyMap().entrySet()
			.stream()
			.filter(it -> "value1".equals(it.getValue()))
			.findAny()
			.get()
			.getKey();
		then(expected.get("key")).isEqualTo("value2");
	}

	@Property
	void entryValueSetNull() {
		// when
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("strMap",
						m -> m.size(1).entry("key", null)
					)
			)
			.sample();

		then(actual.getStrMap().get("key")).isNull();
	}

	@Property
	void listElementInListElement() {
		// when
		NestedListStringObject actual = SUT.giveMeBuilder(NestedListStringObject.class)
			.setInner(
				new InnerSpec()
					.property("values",
						m -> {
							m.size(1);
							m.listElement(0, l -> {
								l.size(1);
								l.listElement(0, "test");
							});
						})
			)
			.sample();

		then(actual.getValues().get(0).get(0)).isEqualTo("test");
	}

	@Property
	void propertyInProperty() {
		// when
		ComplexObjectObject actual = SUT.giveMeBuilder(ComplexObjectObject.class)
			.setInner(
				new InnerSpec()
					.property("value",
						m -> m.property("value", o -> o.property("str", "test"))
					)
			)
			.sample();

		then(actual.getValue().getValue().getStr()).isEqualTo("test");
	}

	@Property
	void sizeAndEntry() {
		// when
		Map<String, String> actual = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("strMap",
						m -> {
							m.size(4);
							m.entry("key", "test");
						}
					)
			)
			.sample()
			.getStrMap();

		then(actual).hasSize(4);
		then(actual.get("key")).isEqualTo("test");
	}

	@Property
	void entryAndSize() {
		// when
		Map<String, String> actual = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("strMap",
						m -> {
							m.entry("key", "test");
							m.size(4);
						}
					)
			)
			.sample()
			.getStrMap();

		then(actual).hasSize(4);
		then(actual.get("key")).isEqualTo("test");
	}

	@Property
	void sizeTwiceReturnsLatterSize() {
		// when
		Map<String, String> actual = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("strMap",
						m -> {
							m.size(1);
							m.entry("key", "test");
							m.size(0);
						}
					)
			)
			.sample()
			.getStrMap();

		then(actual).hasSize(0);
	}

	@Property
	void keyLazy() {
		ArbitraryBuilder<String> variable = SUT.giveMeBuilder(String.class);
		ArbitraryBuilder<MapObject> builder = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("strMap",
						m -> m.size(1).keyLazy(variable::sample)
					)
			);
		variable.set("key");

		MapObject actual = builder.sample();

		then(actual.getStrMap().containsKey("key")).isTrue();
	}

	@Property
	void valueLazy() {
		ArbitraryBuilder<String> variable = SUT.giveMeBuilder(String.class);
		ArbitraryBuilder<MapObject> builder = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("strMap",
						m -> m.minSize(1).valueLazy(variable::sample)
					)
			);
		variable.set("value");

		MapObject actual = builder.sample();

		then(actual.getStrMap().containsValue("value")).isTrue();
	}

	@Property
	void entryLazy() {
		ArbitraryBuilder<String> keyVariable = SUT.giveMeBuilder(String.class);
		ArbitraryBuilder<String> valueVariable = SUT.giveMeBuilder(String.class);
		ArbitraryBuilder<MapObject> builder = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("strMap",
						m -> m.minSize(1).entryLazy(keyVariable::sample, valueVariable::sample)
					)
			);
		keyVariable.set("key");
		valueVariable.set("value");

		MapObject actual = builder.sample();

		then(actual.getStrMap().get("key")).isEqualTo("value");
	}

	@Property
	void keyLazyNullThrows() {
		thenThrownBy(() ->
			SUT.giveMeBuilder(MapObject.class)
				.setInner(
					new InnerSpec()
						.property("strMap",
							m -> m.minSize(1).keyLazy(() -> null)
						)
				)
				.sample()
		).isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Map key cannot be null.");
	}

	@Property
	void allKeyLazy() {
		IntegerMapObject actual = SUT.giveMeBuilder(IntegerMapObject.class)
			.setInner(
				new InnerSpec()
					.property("integerMap",
						m -> m.allKeyLazy(() -> Arbitraries.integers().between(0, 100))
					)
			)
			.sample();

		then(actual.getIntegerMap().keySet()).allMatch(it -> it >= 0 && it <= 100);
	}

	@Property
	void allValueLazy() {
		IntegerMapObject actual = SUT.giveMeBuilder(IntegerMapObject.class)
			.setInner(
				new InnerSpec()
					.property("integerMap",
						m -> m.allValueLazy(() -> Arbitraries.integers().between(0, 100))
					)
			)
			.sample();

		then(actual.getIntegerMap().values()).allMatch(it -> it >= 0 && it <= 100);
	}

	@Property
	void allEntry() {
		IntegerMapObject actual = SUT.giveMeBuilder(IntegerMapObject.class)
			.setInner(
				new InnerSpec()
					.property("integerMap",
						m -> m.allEntry(
							() -> Arbitraries.integers().between(0, 100),
							100
						)
					)
			)
			.sample();

		then(actual.getIntegerMap().keySet()).allMatch(it -> it >= 0 && it <= 100);
		then(actual.getIntegerMap().values()).allMatch(it -> it == 100);
	}

	@Property
	void allEntryLazy() {
		IntegerMapObject actual = SUT.giveMeBuilder(IntegerMapObject.class)
			.setInner(
				new InnerSpec()
					.property("integerMap",
						m -> m.allEntryLazy(
							() -> Arbitraries.integers().between(0, 100),
							() -> Arbitraries.integers().between(0, 100)
						)
					)
			)
			.sample();

		then(actual.getIntegerMap().keySet()).allMatch(it -> it >= 0 && it <= 100);
		then(actual.getIntegerMap().values()).allMatch(it -> it >= 0 && it <= 100);
	}

	@Property
	void allKey() {
		String expected = "test";

		List<String> actual = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("objectKeyMap",
						m -> m.allKey(v ->
							v.property("str", expected)
						)
					)
			)
			.sample()
			.getObjectKeyMap()
			.keySet()
			.stream()
			.map(SimpleObject::getStr)
			.collect(Collectors.toList());

		then(actual).allMatch(expected::equals);
	}

	@Property
	void allValue() {
		String expected = "test";

		Collection<String> actual = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("strMap",
						m -> m.allValue(expected)
					)
			)
			.sample()
			.getStrMap()
			.values();

		then(actual).allMatch(expected::equals);
	}

	@Property
	void allValueInner() {
		String expected = "test";

		List<String> actual = SUT.giveMeBuilder(MapObject.class)
			.setInner(
				new InnerSpec()
					.property("objectValueMap",
						m -> m.allValue(v ->
							v.property("str", expected)
						)
					)
			)
			.sample()
			.getObjectValueMap()
			.values()
			.stream()
			.map(SimpleObject::getStr)
			.collect(Collectors.toList());

		then(actual).allMatch(expected::equals);
	}

	@Property
	void allListElement() {
		String expected = "test";

		// when
		List<String> actual = SUT.giveMeBuilder(new TypeReference<List<String>>() {
			})
			.setInner(
				new InnerSpec()
					.allListElement(expected)
			)
			.sample();

		then(actual).allMatch(expected::equals);
	}

	@Property
	void allListElementInnerSpec() {
		String expected = "test";

		// when
		List<String> actual = SUT.giveMeBuilder(new TypeReference<List<List<String>>>() {
			})
			.setInner(
				new InnerSpec()
					.allListElement(inner -> inner.allListElement(expected))
			)
			.sample()
			.stream()
			.flatMap(Collection::stream)
			.collect(Collectors.toList());

		then(actual).allMatch(expected::equals);
	}

	@Property
	void setPostCondition() {
		SimpleObject actual = SUT.giveMeBuilder(SimpleObject.class)
			.setInner(
				new InnerSpec()
					.property(
						"str",
						inner -> inner.postCondition(String.class, it -> it.length() > 5)
					)
			)
			.sample();

		then(actual.getStr()).hasSizeGreaterThan(5);
	}

	@Property
	void inner() {
		InnerSpec innerSpec = new InnerSpec().property("str", "test");

		SimpleObject actual = SUT.giveMeBuilder(SimpleObject.class)
			.setInner(new InnerSpec().inner(innerSpec))
			.sample();

		then(actual.getStr()).isEqualTo("test");
	}

	@Property
	void propertyInner() {
		InnerSpec innerSpec = new InnerSpec().property("str", "test");

		ComplexObject actual = SUT.giveMeBuilder(ComplexObject.class)
			.setInner(new InnerSpec().property("value", innerSpec))
			.sample();

		then(actual.getValue().getStr()).isEqualTo("test");
	}

	@Property
	void listElementInMaxSize() {
		String expected = "expected";

		List<String> actual = SUT.giveMeBuilder(new TypeReference<List<String>>() {
			})
			.setInner(
				new InnerSpec()
					.maxSize(2)
					.listElement(0, expected)
					.listElement(1, expected)
			)
			.sample();

		then(actual).allMatch(expected::equals);
	}

	@Property
	void setAfterSizeReturnsSet() {
		List<String> actual = SUT.giveMeBuilder(ListStringObject.class)
			.setInner(
				new InnerSpec()
					.property("values", it -> it.size(2))
					.property("values", new ArrayList<>())
			)
			.sample()
			.getValues();

		then(actual).isEmpty();
	}

	@Property
	void sizeAfterSetReturnsSize() {
		List<String> actual = SUT.giveMeBuilder(ListStringObject.class)
			.setInner(
				new InnerSpec()
					.property("values", new ArrayList<>())
					.property("values", it -> it.size(2))
			)
			.sample()
			.getValues();

		then(actual).hasSize(2);
	}

	@Property
	void sizeAfterSetWithSeparateInnerSpecReturnsSize() {
		List<String> actual = SUT.giveMeBuilder(ListStringObject.class)
			.setInner(
				new InnerSpec()
					.property("values", new ArrayList<>())
			)
			.setInner(
				new InnerSpec()
					.property("values", it -> it.size(2))
			)
			.sample()
			.getValues();

		then(actual).hasSize(2);
	}

	@Property
	void setAfterSetWithSeparateInnerSpecReturnsSet() {
		List<String> actual = SUT.giveMeBuilder(ListStringObject.class)
			.setInner(
				new InnerSpec()
					.property("values", it -> it.size(2))
			)
			.setInner(
				new InnerSpec()
					.property("values", new ArrayList<>())
			)
			.sample()
			.getValues();

		then(actual).isEmpty();
	}

	@Property
	void innerSpecIncrementsSequence() {
		List<String> actual = SUT.giveMeBuilder(ListStringObject.class)
			.setInner(
				new InnerSpec()
					.property("values", it -> it.size(1))
					.property("values", it -> it.size(2))
					.property("values", it -> it.size(3))
			)
			.size("values", 5)
			.sample()
			.getValues();

		then(actual).hasSize(5);
	}

	@Property
	void setNotNull() {
		String actual = SUT.giveMeBuilder(SimpleObject.class)
			.setInner(new InnerSpec().property("str", NOT_NULL))
			.sample()
			.getStr();

		then(actual).isNotNull();
	}
}
