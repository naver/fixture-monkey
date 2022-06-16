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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.LabMonkey;
import com.navercorp.fixturemonkey.test.CollectionSpecTestSpecs.MapObject;
import com.navercorp.fixturemonkey.test.CollectionSpecTestSpecs.Sample;
import com.navercorp.fixturemonkey.test.CollectionSpecTestSpecs.SimpleObject;

class CollectionSpecTest {
	private static final LabMonkey SUT = LabMonkey.labMonkey();

	@Property
	void mapAddKey() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.spec("strMap", m -> {
				m.key("key");
			})
			.sample();

		then(actual.getStrMap().containsKey("key")).isTrue();
	}

	@Property
	void mapAddValue() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.spec("strMap", m -> {
				m.value("value");
			})
			.sample();

		then(actual.getStrMap().containsValue("value")).isTrue();
	}

	@Property
	void mapPut() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.spec("strMap", m -> {
				m.entry("key", "value");
			})
			.sample();

		then(actual.getStrMap().get("key")).isEqualTo("value");
	}

	@Property
	void mapAddNullValue() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.spec("strMap", m -> {
				m.value(null);
			})
			.sample();

		then(actual.getStrMap().containsValue(null)).isTrue();
	}

	@Property
	void mapAddNullKeyThrows() {
		thenThrownBy(() ->
			SUT.giveMeBuilder(MapObject.class)
				.spec("strMap", m -> {
					m.key(null);
				})
				.sample()
		).isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Map key cannot be null.");
	}

	@Property
	void mapAddKeyAddKey() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.spec("mapKeyMap", m -> {
				m.key(k-> {
					k.key("key");
				});
			})
			.sample();

		List<String> keyList = actual.getMapKeyMap().keySet().stream()
			.flatMap(it->it.keySet().stream()).collect(Collectors.toList());
		then(keyList).contains("key");
	}

	@Property
	void mapAddKeyAddValue() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.spec("mapKeyMap", m -> {
				m.key(k-> {
					k.value("value");
				});
			})
			.sample();

		List<String> keyList = actual.getMapKeyMap().keySet().stream()
			.flatMap(it->it.values().stream()).collect(Collectors.toList());
		then(keyList).contains("value");
	}

	@Property
	void mapAddValueAddKey() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.spec("mapValueMap", m -> {
				m.value(v-> {
					v.key("key");
				});
			})
			.sample();

		List<String> valueList = actual.getMapValueMap().values().stream()
			.flatMap(it-> it.keySet().stream()).collect(Collectors.toList());
		then(valueList).contains("key");
	}

	@Property
	void mapAddValueAddValue() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.spec("mapValueMap", m -> {
				m.value(v-> {
					v.value("value");
				});
			})
			.sample();

		List<String> valueList = actual.getMapValueMap().values().stream()
			.flatMap(it-> it.values().stream()).collect(Collectors.toList());
		then(valueList).contains("value");
	}

	@Property
	void mapSetValueSize() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.spec("listValueMap", m -> {
				m.value(v-> {
					v.size(10);
				});
			})
			.sample();
		List<Integer> sizeList = actual.getListValueMap().values().stream()
			.map(List::size).collect(Collectors.toList());
		then(sizeList).contains(10);
	}

	@Property
	void mapSetValueListElement() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.spec("listValueMap", m -> {
				m.value(v->{
					v.size(1);
					v.listElement(0, "test");
				});
			})
			.sample();
		List<String> elementList = actual.getListValueMap().values().stream()
			.flatMap(List::stream).collect(Collectors.toList());
		then(elementList).contains("test");
	}

	@Property
	void mapSetValueField() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.spec("objectValueMap", m -> {
				m.value(v->{
					v.field("str", "test");
				});
			})
			.sample();

		List<String> fieldList = actual.getObjectValueMap().values().stream().filter(Objects::nonNull)
			.map(SimpleObject::getStr).collect(Collectors.toList());

		then(fieldList).contains("test");
	}

	@Property
	void sampleTest() {
		Sample actual = SUT.giveMeBuilder(Sample.class)
			.spec("listListStr", m -> {
				m.size(1);
				m.listElement(0, l->{
					l.size(1);
					l.listElement(0, "test");
				});
			})
			.sample();

		then(actual);
	}

	@Property
	void sampleTest2() {
		Sample actual = SUT.giveMeBuilder(Sample.class)
			.spec("complexObject", m -> {
				m.field("simpleObject", o->{
					o.field("str", "test");
				});
			})
			.sample();

		then(actual);
	}


	@Property
	void mapComplexAdd() {
		Map<String, String> map = new HashMap<>();
		map.put("key1", "val1");
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.spec("mapKeyValueMap", m -> {
				m.value(map);
				m.value(v-> {
					v.value("value3");
					v.key("key3");
				});
				m.key(map);
				m.key(k-> {
					k.key("aneo");
				});
			})
			.sample();

		then(actual);
	}
}
