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
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

class SetValueAssignabilityTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.build();

	@Test
	void setValueNotAssignableToPropertyThrows() {
		thenThrownBy(() ->
			SUT.giveMeBuilder(StringHolder.class)
				.set("value", 123)
				.sample()
		)
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("not assignable");
	}

	@Test
	void setValueNotAssignableToObjectPropertyThrows() {
		thenThrownBy(() ->
			SUT.giveMeBuilder(PojoHolder.class)
				.set("value", "x")
				.sample()
		)
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("not assignable");
	}

	@Test
	void setLazyValueNotAssignableToPropertyThrows() {
		// given
		Supplier<String> lazyValue = () -> "x";

		thenThrownBy(() ->
			SUT.giveMeBuilder(SupplierHolder.class)
				.set("value", lazyValue)
				.sample()
		)
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("not assignable");
	}

	@Test
	void setContainerWithElementNotAssignableThrows() {
		thenThrownBy(() ->
			SUT.giveMeBuilder(NestedListHolder.class)
				.set("value", Collections.singletonList("x"))
				.sample()
		)
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("not assignable");
	}

	@Test
	void setOptionalWithContentNotAssignableThrows() {
		thenThrownBy(() ->
			SUT.giveMeBuilder(NestedOptionalHolder.class)
				.set("value", Optional.of("x"))
				.sample()
		)
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("not assignable");
	}

	@Test
	void setEntryWithValueNotAssignableThrows() {
		thenThrownBy(() ->
			SUT.giveMeBuilder(EntryHolder.class)
				.set("value", new SimpleEntry<>("k", "v"))
				.sample()
		)
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("not assignable");
	}

	@Test
	void setEntryWithListElementNotAssignableThrows() {
		thenThrownBy(() ->
			SUT.giveMeBuilder(EntryListHolder.class)
				.set("value", new SimpleEntry<>("k", Collections.singletonList("x")))
				.sample()
		)
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("not assignable");
	}

	@Test
	void setNestedContainerWithInnermostElementNotAssignableThrows() {
		thenThrownBy(() ->
			SUT.giveMeBuilder(NestedEntryHolder.class)
				.set("value", new SimpleEntry<>("k", Optional.of(Collections.singletonList("x"))))
				.sample()
		)
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("not assignable");
	}

	@Test
	void setMapWithValueNotAssignableThrows() {
		thenThrownBy(() ->
			SUT.giveMeBuilder(MapHolder.class)
				.set("value", Collections.singletonMap("k", "v"))
				.sample()
		)
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("not assignable");
	}

	@Test
	void registeredLazyContainerWithElementNotAssignableThrows() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.register(
				NestedListHolder.class,
				fm -> fm.giveMeBuilder(NestedListHolder.class).setLazy("value", () -> Collections.singletonList("x"))
			)
			.build();

		thenThrownBy(() -> sut.giveMeOne(RegisteredHolders.class))
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("not assignable");
	}

	@Test
	void registeredLazyOptionalWithContentNotAssignableThrows() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.register(
				NestedOptionalHolder.class,
				fm -> fm.giveMeBuilder(NestedOptionalHolder.class).setLazy("value", () -> Optional.of("x"))
			)
			.build();

		thenThrownBy(() -> sut.giveMeOne(RegisteredHolders.class))
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("not assignable");
	}

	@Test
	void registeredLazyContainerWithAssignableContentPasses() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.register(
				NestedListHolder.class,
				fm -> fm.giveMeBuilder(NestedListHolder.class)
					.setLazy("value", () -> Collections.singletonList(Arrays.asList("a", "b")))
			)
			.register(
				NestedOptionalHolder.class,
				fm -> fm.giveMeBuilder(NestedOptionalHolder.class).setLazy("value", () -> Optional.of(Optional.of("a")))
			)
			.build();

		// when
		RegisteredHolders actual = sut.giveMeOne(RegisteredHolders.class);

		// then
		then(actual.nestedList.value).isEqualTo(Collections.singletonList(Arrays.asList("a", "b")));
		then(actual.nestedOptional.value).hasValue(Optional.of("a"));
	}

	@Test
	void setSubtypeValuePasses() {
		// when
		AnimalHolder actual = SUT.giveMeBuilder(AnimalHolder.class)
			.set("value", new Dog())
			.sample();

		// then
		then(actual.value).isInstanceOf(Dog.class);
	}

	@Test
	void setBoxedValueToPrimitivePropertyPasses() {
		// when
		PrimitiveHolder actual = SUT.giveMeBuilder(PrimitiveHolder.class)
			.set("value", Integer.valueOf(3))
			.sample();

		// then
		then(actual.value).isEqualTo(3);
	}

	@Test
	void setContainerWithSubtypeElementsPasses() {
		// when
		AnimalListHolder actual = SUT.giveMeBuilder(AnimalListHolder.class)
			.set("value", Arrays.asList(new Dog(), new Dog()))
			.sample();

		// then
		then(actual.value).hasSize(2).allSatisfy(it -> then(it).isInstanceOf(Dog.class));
	}

	public static class StringHolder {
		private String value;
	}

	public static class PrimitiveHolder {
		private int value;
	}

	public static class PojoHolder {
		private Pojo value;
	}

	public static class Pojo {
		private String name;
	}

	public static class SupplierHolder {
		private Supplier<String> value;
	}

	public static class NestedListHolder {
		private List<List<String>> value;
	}

	public static class NestedOptionalHolder {
		private Optional<Optional<String>> value;
	}

	public static class RegisteredHolders {
		private NestedListHolder nestedList;
		private NestedOptionalHolder nestedOptional;
	}

	public static class EntryHolder {
		private Map.Entry<String, Integer> value;
	}

	public static class EntryListHolder {
		private Map.Entry<String, List<Integer>> value;
	}

	public static class NestedEntryHolder {
		private Map.Entry<String, Optional<List<Integer>>> value;
	}

	public static class MapHolder {
		private Map<String, Integer> value;
	}

	public static class AnimalHolder {
		private Animal value;
	}

	public static class AnimalListHolder {
		private List<Animal> value;
	}

	public interface Animal {
	}

	public static class Dog implements Animal {
		private String name;
	}
}
