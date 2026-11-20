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

package com.navercorp.fixturemonkey.tests.java;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

class SetOptionalAndEntryDecompositionTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@Test
	void setEntryWithObjectValue() {
		// when
		EntryValueHolder actual = SUT.giveMeBuilder(EntryValueHolder.class)
			.set("entry", new SimpleEntry<>("k", child("a")))
			.sample();

		// then
		then(actual.entry.getKey()).isEqualTo("k");
		then(actual.entry.getValue().name).isEqualTo("a");
		then(actual.entry.getValue().tags).containsExactly("x", "y");
	}

	@Test
	void setEntryWithObjectKey() {
		// when
		EntryKeyHolder actual = SUT.giveMeBuilder(EntryKeyHolder.class)
			.set("entry", new SimpleEntry<>(child("a"), "v"))
			.sample();

		// then
		then(actual.entry.getKey().name).isEqualTo("a");
		then(actual.entry.getKey().tags).containsExactly("x", "y");
		then(actual.entry.getValue()).isEqualTo("v");
	}

	@Test
	void setEntryWithListValue() {
		// when
		EntryListHolder actual = SUT.giveMeBuilder(EntryListHolder.class)
			.set("entry", new SimpleEntry<>("k", Arrays.asList("a", "b")))
			.sample();

		// then
		then(actual.entry.getKey()).isEqualTo("k");
		then(actual.entry.getValue()).containsExactly("a", "b");
	}

	@Test
	void setOptionalWithObjectValue() {
		// when
		OptionalChildHolder actual = SUT.giveMeBuilder(OptionalChildHolder.class)
			.set("child", Optional.of(child("a")))
			.sample();

		// then
		then(actual.child).hasValueSatisfying(it -> {
			then(it.name).isEqualTo("a");
			then(it.tags).containsExactly("x", "y");
		});
	}

	@Test
	void setOptionalWithListValue() {
		// when
		OptionalListHolder actual = SUT.giveMeBuilder(OptionalListHolder.class)
			.set("values", Optional.of(Arrays.asList("a", "b")))
			.sample();

		// then
		then(actual.values).hasValueSatisfying(it -> then(it).containsExactly("a", "b"));
	}

	@Test
	void registeredOptionalWithListValue() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				OptionalListHolder.class,
				fixture -> fixture.giveMeBuilder(OptionalListHolder.class)
					.set("values", Optional.of(Arrays.asList("a", "b", "c")))
			)
			.build();

		// when
		OptionalListHolder actual = sut.giveMeOne(OptionalListHolder.class);

		// then
		then(actual.values).hasValueSatisfying(it -> then(it).containsExactly("a", "b", "c"));
	}

	@Test
	void setOptionalWithNestedContainers() {
		// when
		NestedOptionalHolder actual = SUT.giveMeBuilder(NestedOptionalHolder.class)
			.set("values", Optional.of(Arrays.asList(Optional.of("a"), Optional.of("b"))))
			.sample();

		// then
		then(actual.values).hasValue(Arrays.asList(Optional.of("a"), Optional.of("b")));
	}

	@Test
	void setEntryWithNestedContainers() {
		// when
		NestedEntryHolder actual = SUT.giveMeBuilder(NestedEntryHolder.class)
			.set("entry", new SimpleEntry<>("k", Optional.of(Arrays.asList("a", "b"))))
			.sample();

		// then
		then(actual.entry.getKey()).isEqualTo("k");
		then(actual.entry.getValue()).hasValue(Arrays.asList("a", "b"));
	}

	@Test
	void setEntryToObjectProperty() {
		// when
		ObjectHolder actual = SUT.giveMeBuilder(ObjectHolder.class)
			.set("value", new SimpleEntry<>("k", "v"))
			.sample();

		// then
		then(actual.value).isEqualTo(new SimpleEntry<>("k", "v"));
	}

	@Test
	void setSetOfEntriesToObjectProperty() {
		// when
		ObjectHolder actual = SUT.giveMeBuilder(ObjectHolder.class)
			.set("value", new HashSet<>(Collections.singleton(new SimpleEntry<>("k", "v"))))
			.sample();

		// then
		then(actual.value).isEqualTo(Collections.singleton(new SimpleEntry<>("k", "v")));
	}

	@Test
	void setEntryToObjectListElement() {
		// when
		ObjectListHolder actual = SUT.giveMeBuilder(ObjectListHolder.class)
			.size("values", 1)
			.set("values[0]", new SimpleEntry<>("k", "v"))
			.sample();

		// then
		then(actual.values).containsExactly(new SimpleEntry<>("k", "v"));
	}

	@Test
	void setEntryToObjectMapValue() {
		// when
		ObjectValueMapHolder actual = SUT.giveMeBuilder(ObjectValueMapHolder.class)
			.set("values", Collections.singletonMap("a", new SimpleEntry<>("k", "v")))
			.sample();

		// then
		then(actual.values).isEqualTo(Collections.singletonMap("a", new SimpleEntry<>("k", "v")));
	}

	@Test
	void setEntryToObjectMapKey() {
		// when
		ObjectKeyMapHolder actual = SUT.giveMeBuilder(ObjectKeyMapHolder.class)
			.set("values", Collections.singletonMap(new SimpleEntry<>("k", "v"), "a"))
			.sample();

		// then
		then(actual.values).isEqualTo(Collections.singletonMap(new SimpleEntry<>("k", "v"), "a"));
	}

	@Test
	void setMixedTypeListToObjectProperty() {
		// when
		ObjectHolder actual = SUT.giveMeBuilder(ObjectHolder.class)
			.set("value", new ArrayList<>(Arrays.asList("a", 1)))
			.sample();

		// then
		then(actual.value).isEqualTo(Arrays.asList("a", 1));
	}

	@Test
	void setNestedMixedTypeListToObjectProperty() {
		// when
		ObjectHolder actual = SUT.giveMeBuilder(ObjectHolder.class)
			.set("value", nestedMixedTypeList())
			.sample();

		// then
		then(actual.value).isEqualTo(Arrays.asList(Collections.singletonList("a"), Collections.singletonList(1)));
	}

	@Test
	void setEmptyListToObjectPropertyThenGrowIt() {
		// when
		ObjectHolder actual = SUT.giveMeBuilder(ObjectHolder.class)
			.set("value", new ArrayList<>())
			.size("value", 2)
			.sample();

		// then
		then(actual.value).isInstanceOfSatisfying(List.class, it -> then(it).hasSize(2));
	}

	@Test
	void setLinkedListToListPropertyThenGrowIt() {
		// when
		ObjectListHolder actual = SUT.giveMeBuilder(ObjectListHolder.class)
			.set("values", new LinkedList<>(Arrays.asList("a", "b")))
			.size("values", 3)
			.sample();

		// then
		then(actual.values).isInstanceOf(LinkedList.class).hasSize(3).startsWith("a", "b");
	}

	@Test
	void setOptionalWithListToObjectContent() {
		// when
		OptionalObjectHolder actual = SUT.giveMeBuilder(OptionalObjectHolder.class)
			.set("value", Optional.of(new ArrayList<>(Arrays.asList("a", "b"))))
			.sample();

		// then
		then(actual.value).hasValue(Arrays.asList("a", "b"));
	}

	@Test
	void setOptionalWithJdkInternalListToObjectContent() {
		// when
		OptionalObjectHolder actual = SUT.giveMeBuilder(OptionalObjectHolder.class)
			.set("value", Optional.of(Arrays.asList("a", "b")))
			.sample();

		// then
		then(actual.value).hasValue(Arrays.asList("a", "b"));
	}

	private static Child child(String name) {
		Child child = new Child();
		child.name = name;
		child.tags = new ArrayList<>(Arrays.asList("x", "y"));
		return child;
	}

	private static List<List<Object>> nestedMixedTypeList() {
		return new ArrayList<>(Arrays.asList(
			new ArrayList<>(Collections.singletonList("a")),
			new ArrayList<>(Collections.singletonList(1))
		));
	}

	public static class Child {
		private String name;
		private List<String> tags;
	}

	public static class EntryValueHolder {
		private Map.Entry<String, Child> entry;
	}

	public static class EntryKeyHolder {
		private Map.Entry<Child, String> entry;
	}

	public static class EntryListHolder {
		private Map.Entry<String, List<String>> entry;
	}

	public static class OptionalChildHolder {
		private Optional<Child> child;
	}

	public static class OptionalListHolder {
		private Optional<List<String>> values;
	}

	public static class NestedOptionalHolder {
		private Optional<List<Optional<String>>> values;
	}

	public static class NestedEntryHolder {
		private Map.Entry<String, Optional<List<String>>> entry;
	}

	public static class ObjectHolder {
		private Object value;
	}

	public static class ObjectListHolder {
		private List<Object> values;
	}

	public static class ObjectValueMapHolder {
		private Map<String, Object> values;
	}

	public static class ObjectKeyMapHolder {
		private Map<Object, String> values;
	}

	public static class OptionalObjectHolder {
		private Optional<Object> value;
	}
}
