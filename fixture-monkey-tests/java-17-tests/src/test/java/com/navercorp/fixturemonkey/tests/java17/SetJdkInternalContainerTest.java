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

package com.navercorp.fixturemonkey.tests.java17;

import static org.assertj.core.api.BDDAssertions.then;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;
import com.navercorp.fixturemonkey.api.type.TypeReference;

class SetJdkInternalContainerTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.build();

	@Test
	void setListOfOneElementToObjectProperty() {
		// when
		ObjectValue actual = SUT.giveMeBuilder(ObjectValue.class)
			.set("value", List.of("a"))
			.sample();

		// then
		then(actual.getValue()).isEqualTo(List.of("a"));
	}

	@Test
	void setListOfThreeElementsToObjectProperty() {
		// when
		ObjectValue actual = SUT.giveMeBuilder(ObjectValue.class)
			.set("value", List.of("a", "b", "c"))
			.sample();

		// then
		then(actual.getValue()).isEqualTo(List.of("a", "b", "c"));
	}

	@Test
	void setSetOfOneElementToObjectProperty() {
		// when
		ObjectValue actual = SUT.giveMeBuilder(ObjectValue.class)
			.set("value", Set.of("a"))
			.sample();

		// then
		then(actual.getValue()).isEqualTo(Set.of("a"));
	}

	@Test
	void setSetOfThreeElementsToObjectProperty() {
		// when
		ObjectValue actual = SUT.giveMeBuilder(ObjectValue.class)
			.set("value", Set.of("a", "b", "c"))
			.sample();

		// then
		then(actual.getValue()).isEqualTo(Set.of("a", "b", "c"));
	}

	@Test
	void setMapOfOneEntryToObjectProperty() {
		// when
		ObjectValue actual = SUT.giveMeBuilder(ObjectValue.class)
			.set("value", Map.of("a", 1))
			.sample();

		// then
		then(actual.getValue()).isEqualTo(Map.of("a", 1));
	}

	@Test
	void setMapOfThreeEntriesToObjectProperty() {
		// when
		ObjectValue actual = SUT.giveMeBuilder(ObjectValue.class)
			.set("value", Map.of("a", 1, "b", 2, "c", 3))
			.sample();

		// then
		then(actual.getValue()).isEqualTo(Map.of("a", 1, "b", 2, "c", 3));
	}

	@Test
	void setNestedListOfToObjectProperty() {
		// when
		ObjectValue actual = SUT.giveMeBuilder(ObjectValue.class)
			.set("value", List.of(List.of("a")))
			.sample();

		// then
		then(actual.getValue()).isEqualTo(List.of(List.of("a")));
	}

	@Test
	void setSetOfToObjectListElement() {
		// when
		ObjectList actual = SUT.giveMeBuilder(ObjectList.class)
			.set("values", List.of(Set.of("a")))
			.sample();

		// then
		then(actual.getValues()).isEqualTo(List.of(Set.of("a")));
	}

	@Test
	void setListOfOneElementToObjectMapValue() {
		// when
		ObjectMap actual = SUT.giveMeBuilder(ObjectMap.class)
			.set("values", Map.of("k", List.of("a")))
			.sample();

		// then
		then(actual.getValues()).isEqualTo(Map.of("k", List.of("a")));
	}

	@Test
	void setListOfThreeElementsToObjectMapValue() {
		// when
		ObjectMap actual = SUT.giveMeBuilder(ObjectMap.class)
			.set("values", Map.of("k", List.of("a", "b", "c")))
			.sample();

		// then
		then(actual.getValues()).isEqualTo(Map.of("k", List.of("a", "b", "c")));
	}

	@Test
	void setJdkInternalNavigableSetToObjectPropertyUsesRegisteredImplementation() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.plugin(new InterfacePlugin().interfaceImplements(NavigableSet.class, List.of(ConcurrentSkipListSet.class)))
			.build();

		// when
		ObjectValue actual = sut.giveMeBuilder(ObjectValue.class)
			.set("value", Collections.unmodifiableNavigableSet(new TreeSet<>(List.of("a"))))
			.sample();

		// then
		then(actual.getValue())
			.isInstanceOf(ConcurrentSkipListSet.class)
			.asInstanceOf(InstanceOfAssertFactories.ITERABLE)
			.containsExactly("a");
	}

	@Test
	void setListOfToSerializableProperty() {
		// when
		SerializableValue actual = SUT.giveMeBuilder(SerializableValue.class)
			.set("value", List.of("a"))
			.sample();

		// then
		then(actual.getValue()).isEqualTo(List.of("a"));
	}

	@Test
	void setSetOfToGenericTypeVariableProperty() {
		// when
		GenericValue<Serializable> actual = SUT.giveMeBuilder(new TypeReference<GenericValue<Serializable>>() {
			})
			.set("value", Set.of("a"))
			.sample();

		// then
		then(actual.getValue()).isEqualTo(Set.of("a"));
	}

	@Test
	void setListOfToSerializableListElement() {
		// when
		SerializableList actual = SUT.giveMeBuilder(SerializableList.class)
			.set("values", List.of(List.of("a")))
			.sample();

		// then
		then(actual.getValues()).isEqualTo(List.of(List.of("a")));
	}

	@Test
	void setListOfToWildcardListElement() {
		// when
		WildcardList actual = SUT.giveMeBuilder(WildcardList.class)
			.set("values", List.of(List.of("a")))
			.sample();

		// then
		then(actual.getValues()).isEqualTo(List.of(List.of("a")));
	}

	@Test
	void setListOfToObjectMapKey() {
		// when
		ObjectKeyMap actual = SUT.giveMeBuilder(ObjectKeyMap.class)
			.set("values", Map.of(List.of("a"), "v"))
			.sample();

		// then
		then(actual.getValues()).isEqualTo(Map.of(List.of("a"), "v"));
	}

	@Test
	void setEmptyListOfToObjectProperty() {
		// when
		ObjectValue actual = SUT.giveMeBuilder(ObjectValue.class)
			.set("value", List.of())
			.sample();

		// then
		then(actual.getValue()).isEqualTo(List.of());
	}

	@Test
	void setArraysAsListToObjectProperty() {
		// when
		ObjectValue actual = SUT.giveMeBuilder(ObjectValue.class)
			.set("value", Arrays.asList("a", "b"))
			.sample();

		// then
		then(actual.getValue()).isEqualTo(List.of("a", "b"));
	}

	@Test
	void setUnmodifiableListToObjectProperty() {
		// when
		ObjectValue actual = SUT.giveMeBuilder(ObjectValue.class)
			.set("value", Collections.unmodifiableList(new ArrayList<>(List.of("a"))))
			.sample();

		// then
		then(actual.getValue()).isEqualTo(List.of("a"));
	}

	@Test
	void setJdkInternalNavigableSetToObjectPropertyUsesSortedImplementation() {
		// when
		ObjectValue actual = SUT.giveMeBuilder(ObjectValue.class)
			.set("value", Collections.unmodifiableNavigableSet(new TreeSet<>(List.of("b", "a"))))
			.sample();

		// then
		then(actual.getValue())
			.isInstanceOf(TreeSet.class)
			.asInstanceOf(InstanceOfAssertFactories.ITERABLE)
			.containsExactly("a", "b");
	}

	@Test
	void setMapValuesToObjectProperty() {
		// when
		ObjectValue actual = SUT.giveMeBuilder(ObjectValue.class)
			.set("value", new HashMap<>(Map.of("k", 1)).values())
			.sample();

		// then
		then(actual.getValue())
			.asInstanceOf(InstanceOfAssertFactories.ITERABLE)
			.containsExactly(1);
	}

	@Test
	void setListWithNullElementToObjectProperty() {
		// when
		ObjectValue actual = SUT.giveMeBuilder(ObjectValue.class)
			.set("value", Arrays.asList("a", null))
			.sample();

		// then
		then(actual.getValue()).isEqualTo(Arrays.asList("a", null));
	}

	@Test
	void setListOfToObjectPropertyThenSetElement() {
		// when
		ObjectValue actual = SUT.giveMeBuilder(ObjectValue.class)
			.set("value", List.of("a", "b"))
			.set("value[0]", "z")
			.sample();

		// then
		then(actual.getValue()).isEqualTo(List.of("z", "b"));
	}

	@Test
	void setLazyListOfToObjectProperty() {
		// when
		ObjectValue actual = SUT.giveMeBuilder(ObjectValue.class)
			.setLazy("value", () -> List.of("a"))
			.sample();

		// then
		then(actual.getValue()).isEqualTo(List.of("a"));
	}

	@Test
	void setMapEntryToObjectProperty() {
		// when
		ObjectValue actual = SUT.giveMeBuilder(ObjectValue.class)
			.set("value", Map.entry("k", "v"))
			.sample();

		// then
		then(actual.getValue()).isEqualTo(Map.entry("k", "v"));
	}

	public static class ObjectValue {
		private Object value;

		public Object getValue() {
			return value;
		}
	}

	public static class SerializableValue {
		private Serializable value;

		public Serializable getValue() {
			return value;
		}
	}

	public static class GenericValue<T> {
		private T value;

		public T getValue() {
			return value;
		}
	}

	public static class SerializableList {
		private List<Serializable> values;

		public List<Serializable> getValues() {
			return values;
		}
	}

	public static class WildcardList {
		private List<? extends Object> values;

		public List<? extends Object> getValues() {
			return values;
		}
	}

	public static class ObjectKeyMap {
		private Map<Object, String> values;

		public Map<Object, String> getValues() {
			return values;
		}
	}

	public static class ObjectList {
		private List<Object> values;

		public List<Object> getValues() {
			return values;
		}
	}

	public static class ObjectMap {
		private Map<String, Object> values;

		public Map<String, Object> getValues() {
			return values;
		}
	}
}
