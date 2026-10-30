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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.FixtureMonkeyBuilder;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;
import com.navercorp.fixturemonkey.customizer.Values;

class SetObjectIsolationTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@Test
	void setObjectIsNotSharedAcrossSamples() {
		// given
		Child supplied = child("a");
		ArbitraryBuilder<Parent> builder = SUT.giveMeBuilder(Parent.class)
			.set("child", supplied);

		// when
		Parent first = builder.sample();
		Parent second = builder.sample();
		first.child.name = "changed";

		// then
		then(second.child.name).isEqualTo("a");
		then(supplied.name).isEqualTo("a");
	}

	@Test
	void setObjectContainerFieldIsNotSharedAcrossSamples() {
		// given
		Child supplied = child("a");
		ArbitraryBuilder<Parent> builder = SUT.giveMeBuilder(Parent.class)
			.set("child", supplied);

		// when
		Parent first = builder.sample();
		Parent second = builder.sample();
		first.child.tags.add("c");

		// then
		then(second.child.tags).containsExactly("x", "y");
		then(supplied.tags).containsExactly("x", "y");
	}

	@Test
	void setObjectListElementIsNotSharedAcrossSamples() {
		// given
		List<Child> supplied = new ArrayList<>(Arrays.asList(child("a"), child("b")));
		ArbitraryBuilder<Parent> builder = SUT.giveMeBuilder(Parent.class)
			.set("children", supplied);

		// when
		Parent first = builder.sample();
		Parent second = builder.sample();
		first.children.get(0).name = "changed";

		// then
		then(second.children.get(0).name).isEqualTo("a");
		then(supplied.get(0).name).isEqualTo("a");
	}

	@Test
	void setMapObjectValueIsNotSharedAcrossSamples() {
		// given
		Map<String, Child> supplied = new HashMap<>();
		supplied.put("k", child("a"));
		ArbitraryBuilder<Parent> builder = SUT.giveMeBuilder(Parent.class)
			.set("childMap", supplied);

		// when
		Parent first = builder.sample();
		Parent second = builder.sample();
		first.childMap.get("k").name = "changed";

		// then
		then(second.childMap.get("k").name).isEqualTo("a");
		then(supplied.get("k").name).isEqualTo("a");
	}

	@Test
	void setRootObjectIsNotSharedAcrossSamples() {
		// given
		Child supplied = child("a");
		ArbitraryBuilder<Child> builder = SUT.giveMeBuilder(Child.class)
			.set("$", supplied);

		// when
		Child first = builder.sample();
		Child second = builder.sample();
		first.name = "changed";

		// then
		then(second.name).isEqualTo("a");
		then(supplied.name).isEqualTo("a");
	}

	@Test
	void registeredObjectIsNotSharedAcrossSamples() {
		// given
		Child supplied = child("a");
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(Parent.class, fixture -> fixture.giveMeBuilder(Parent.class).set("child", supplied))
			.build();
		ArbitraryBuilder<Parent> builder = sut.giveMeBuilder(Parent.class);

		// when
		Parent first = builder.sample();
		Parent second = builder.sample();
		first.child.name = "changed";

		// then
		then(second.child.name).isEqualTo("a");
		then(supplied.name).isEqualTo("a");
	}

	@Test
	void setInterfaceFieldIsNotSharedAcrossSamples() {
		// given
		Dog supplied = dog("a");
		ArbitraryBuilder<AnimalHolder> builder = animalFixtureMonkey(null)
			.giveMeBuilder(AnimalHolder.class)
			.set("animal", supplied);

		// when
		AnimalHolder first = builder.sample();
		AnimalHolder second = builder.sample();
		((Dog)first.animal).name = "changed";

		// then
		then(second.animal).isInstanceOfSatisfying(Dog.class, it -> then(it.name).isEqualTo("a"));
		then(supplied.name).isEqualTo("a");
	}

	@Test
	void registeredInterfaceFieldIsNotSharedAcrossSamples() {
		// given
		Dog supplied = dog("a");
		ArbitraryBuilder<AnimalHolder> builder = animalFixtureMonkey(supplied).giveMeBuilder(AnimalHolder.class);

		// when
		AnimalHolder first = builder.sample();
		AnimalHolder second = builder.sample();
		((Dog)first.animal).name = "changed";

		// then
		then(second.animal).isInstanceOfSatisfying(Dog.class, it -> then(it.name).isEqualTo("a"));
		then(supplied.name).isEqualTo("a");
	}

	@Test
	void registeredAbstractClassFieldIsNotSharedAcrossSamples() {
		// given
		Cat supplied = new Cat();
		supplied.name = "a";
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new InterfacePlugin().abstractClassExtends(AbstractAnimal.class, Arrays.asList(Cat.class)))
			.register(
				AnimalHolder.class,
				fixture -> fixture.giveMeBuilder(AnimalHolder.class).set("abstractAnimal", supplied)
			)
			.build();
		ArbitraryBuilder<AnimalHolder> builder = sut.giveMeBuilder(AnimalHolder.class);

		// when
		AnimalHolder first = builder.sample();
		AnimalHolder second = builder.sample();
		first.abstractAnimal.name = "changed";

		// then
		then(second.abstractAnimal).isInstanceOfSatisfying(Cat.class, it -> then(it.name).isEqualTo("a"));
		then(supplied.name).isEqualTo("a");
	}

	@Test
	void setLazyObjectReturningSameInstanceIsNotSharedAcrossSamples() {
		// given
		Child supplied = child("a");
		ArbitraryBuilder<Parent> builder = SUT.giveMeBuilder(Parent.class)
			.setLazy("child", () -> supplied);

		// when
		Parent first = builder.sample();
		Parent second = builder.sample();
		first.child.name = "changed";

		// then
		then(second.child.name).isEqualTo("a");
		then(supplied.name).isEqualTo("a");
	}

	@Test
	void setLambdaKeepsSuppliedInstance() {
		// given
		Function<String, String> supplied = value -> value + "!";

		// when
		FunctionHolder actual = SUT.giveMeBuilder(FunctionHolder.class)
			.set("function", supplied)
			.sample();

		// then
		then(actual.function).isSameAs(supplied);
		then(actual.function.apply("a")).isEqualTo("a!");
	}

	@Test
	void setAnonymousClassKeepsSuppliedInstance() {
		// given
		Function<String, String> supplied = new Function<String, String>() {
			@Override
			public String apply(String value) {
				return value + "?";
			}
		};

		// when
		FunctionHolder actual = SUT.giveMeBuilder(FunctionHolder.class)
			.set("function", supplied)
			.sample();

		// then
		then(actual.function).isSameAs(supplied);
		then(actual.function.apply("a")).isEqualTo("a?");
	}

	@Test
	void setJustObjectKeepsSuppliedInstance() {
		// given
		Child supplied = child("a");

		// when
		Parent actual = SUT.giveMeBuilder(Parent.class)
			.set("child", Values.just(supplied))
			.sample();

		// then
		then(actual.child).isSameAs(supplied);
	}

	private static Child child(String name) {
		Child child = new Child();
		child.name = name;
		child.tags = new ArrayList<>(Arrays.asList("x", "y"));
		return child;
	}

	private static FixtureMonkey animalFixtureMonkey(@Nullable Dog registered) {
		FixtureMonkeyBuilder builder = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new InterfacePlugin().interfaceImplements(Animal.class, Arrays.asList(Dog.class)));
		if (registered != null) {
			builder.register(
				AnimalHolder.class,
				fixture -> fixture.giveMeBuilder(AnimalHolder.class).set("animal", registered)
			);
		}
		return builder.build();
	}

	private static Dog dog(String name) {
		Dog dog = new Dog();
		dog.name = name;
		return dog;
	}

	public static class Parent {
		private Child child;
		private List<Child> children;
		private Map<String, Child> childMap;
	}

	public static class Child {
		private String name;
		private List<String> tags;
	}

	public static class FunctionHolder {
		private Function<String, String> function;
	}

	public static class AnimalHolder {
		private Animal animal;
		private AbstractAnimal abstractAnimal;
	}

	public interface Animal {
	}

	public static class Dog implements Animal {
		private String name;
	}

	public abstract static class AbstractAnimal {
		protected String name;
	}

	public static class Cat extends AbstractAnimal {
	}
}
