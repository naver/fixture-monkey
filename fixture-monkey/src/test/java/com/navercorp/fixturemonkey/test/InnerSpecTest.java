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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.LabMonkey;
import com.navercorp.fixturemonkey.test.InnerSpecTestSpecs.ListObject;
import com.navercorp.fixturemonkey.test.InnerSpecTestSpecs.MapObject;
import com.navercorp.fixturemonkey.test.InnerSpecTestSpecs.ObjectObject;
import com.navercorp.fixturemonkey.test.InnerSpecTestSpecs.SimpleObject;

class InnerSpecTest {
	private static final LabMonkey SUT = LabMonkey.labMonkey();

	@Property
	void mapAddKey() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner("strMap", m -> m.key("key"))
			.sample();

		then(actual.getStrMap().containsKey("key")).isTrue();
	}

	@Property
	void mapAddValue() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner("strMap", m -> m.value("value"))
			.sample();

		then(actual.getStrMap().containsValue("value")).isTrue();
	}

	@Property
	void mapPut() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner("strMap", m -> m.entry("key", "value"))
			.sample();

		then(actual.getStrMap().get("key")).isEqualTo("value");
	}

	@Property
	void mapPutTwice() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner("strMap", m -> {
				m.entry("key1", "value1");
				m.entry("key2", "value2");
			})
			.sample();

		then(actual.getStrMap().get("key1")).isEqualTo("value1");
		then(actual.getStrMap().get("key2")).isEqualTo("value2");
	}

	@Property
	void mapAddNullValue() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner("strMap", m -> m.value(null))
			.sample();

		then(actual.getStrMap().containsValue(null)).isTrue();
	}

	@SuppressWarnings("ConstantConditions")
	@Property
	void mapAddNullKeyThrows() {
		thenThrownBy(() ->
			SUT.giveMeBuilder(MapObject.class)
				.setInner("strMap", m -> m.key(null))
				.sample()
		).isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Map key cannot be null.");
	}

	@Property
	void mapAddKeyAddKey() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner("mapKeyMap", m -> m.key(k -> k.key("key")))
			.sample();

		List<String> keyList = actual.getMapKeyMap().keySet().stream()
			.flatMap(it -> it.keySet().stream()).collect(Collectors.toList());
		then(keyList).contains("key");
	}

	@Property
	void mapAddKeyAddValue() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner("mapKeyMap", m -> m.key(k -> k.value("value")))
			.sample();

		List<String> keyList = actual.getMapKeyMap().keySet().stream()
			.flatMap(it -> it.values().stream()).collect(Collectors.toList());
		then(keyList).contains("value");
	}

	@Property
	void mapAddValueAddKey() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner("mapValueMap", m -> m.value(v -> v.key("key")))
			.sample();

		List<String> valueList = actual.getMapValueMap().values().stream()
			.flatMap(it -> it.keySet().stream()).collect(Collectors.toList());
		then(valueList).contains("key");
	}

	@Property
	void mapAddValueAddValue() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner("mapValueMap", m -> m.value(v -> v.value("value")))
			.sample();

		List<String> valueList = actual.getMapValueMap().values().stream()
			.flatMap(it -> it.values().stream()).collect(Collectors.toList());
		then(valueList).contains("value");
	}

	@Property
	void mapSetValueSize() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner("listValueMap", m -> m.value(v -> v.size(10)))
			.sample();

		List<Integer> sizeList = actual.getListValueMap().values().stream()
			.map(List::size).collect(Collectors.toList());
		then(sizeList).contains(10);
	}

	@Property
	void mapSetValueListElement() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner("listValueMap", m ->
				m.value(v -> {
					v.size(1);
					v.listElement(0, "test");
				})
			)
			.sample();

		List<String> elementList = actual.getListValueMap().values().stream()
			.flatMap(List::stream).collect(Collectors.toList());
		then(elementList).contains("test");
	}

	@Property
	void mapSetValueProperty() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner("objectValueMap", m ->
				m.value(v -> v.property("str", "test"))
			)
			.sample();

		List<String> fieldList = actual.getObjectValueMap().values().stream().filter(Objects::nonNull)
			.map(SimpleObject::getStr).collect(Collectors.toList());
		then(fieldList).contains("test");
	}

	@Property
	void mapSetEntryKeyAddEntry() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner("mapValueMap", m ->
				m.entry("key1", v -> v.entry("key2", "value"))
			)
			.sample();

		Map<String, String> value = actual.getMapValueMap().get("key1");
		then(value.get("key2")).isEqualTo("value");
	}

	@SuppressWarnings("OptionalGetWithoutIsPresent")
	@Property
	void mapSetEntryValueAddEntry() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner("mapKeyMap", m ->
				m.entry(
					k -> k.entry("key", "value2"),
					"value1"
				)
			)
			.sample();

		Map<String, String> expected = actual.getMapKeyMap().entrySet()
			.stream()
			.filter(it -> "value1".equals(it.getValue()))
			.findAny()
			.get()
			.getKey();

		then(expected.get("key")).isEqualTo("value2");
	}

	@Property
	void mapSetEntryValueNull() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setInner("strMap", m ->
				m.entry("key", null)
			)
			.sample();

		then(actual.getStrMap().get("key")).isNull();
	}

	@Property
	void listSetListElementSetListElement() {
		ListObject actual = SUT.giveMeBuilder(ListObject.class)
			.setInner("listListStr", m -> {
				m.size(1);
				m.listElement(0, l -> {
					l.size(1);
					l.listElement(0, "test");
				});
			})
			.sample();

		then(actual.getListListStr().get(0).get(0)).isEqualTo("test");
	}

	@Property
	void objectSetPropertySetProperty() {
		ObjectObject actual = SUT.giveMeBuilder(ObjectObject.class)
			.setInner("complexObject", m ->
				m.property("simpleObject", o -> o.property("str", "test"))
			)
			.sample();

		then(actual.getComplexObject().getSimpleObject().getStr()).isEqualTo("test");
	}
}
