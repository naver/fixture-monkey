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

import static com.navercorp.fixturemonkey.api.instantiator.Instantiator.constructor;
import static org.assertj.core.api.BDDAssertions.then;

import java.beans.ConstructorProperties;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;

class RegisterCustomMatcherTest {
	private static final FixtureMonkey TYPE_MATCHER_SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.register(
			new MatcherOperator<>(
				property -> property.getJvmType().getRawType() == Child.class,
				fm -> fm.giveMeBuilder(Child.class).set("name", "custom")
			)
		)
		.build();

	private static final FixtureMonkey NAME_MATCHER_SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.register(
			new MatcherOperator<>(
				property -> "child".equals(property.getName()),
				fm -> fm.giveMeBuilder(Child.class).set("name", "named")
			)
		)
		.build();

	@Test
	void customMatcherBuilderAppliesToEveryMatchingProperty() {
		// when
		Parent actual = TYPE_MATCHER_SUT.giveMeOne(Parent.class);

		// then
		then(actual.getChild().getName()).isEqualTo("custom");
		then(actual.getOther().getName()).isEqualTo("custom");
	}

	@Test
	void customMatcherBuilderAppliesWhenSamplingMatchingType() {
		// when
		Child actual = TYPE_MATCHER_SUT.giveMeOne(Child.class);

		// then
		then(actual.getName()).isEqualTo("custom");
	}

	@Test
	void customMatcherBuilderAppliesOnlyToMatchingProperty() {
		// when
		Parent actual = NAME_MATCHER_SUT.giveMeOne(Parent.class);

		// then
		then(actual.getChild().getName()).isEqualTo("named");
		then(actual.getOther().getName()).isNotEqualTo("named");
	}

	@Test
	void userSetWinsOverCustomMatcherBuilder() {
		// when
		Parent actual = TYPE_MATCHER_SUT.giveMeBuilder(Parent.class)
			.set("child.name", "user")
			.sample();

		// then
		then(actual.getChild().getName()).isEqualTo("user");
		then(actual.getOther().getName()).isEqualTo("custom");
	}

	@Test
	void customMatcherBuilderSizeAppliesOnlyToMatchingProperty() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				new MatcherOperator<>(
					property -> "bag".equals(property.getName()),
					fm -> fm.giveMeBuilder(Bag.class).size("items", 5)
				)
			)
			.build();

		// when
		BagHolder actual = sut.giveMeOne(BagHolder.class);

		// then
		then(actual.getBag().getItems()).hasSize(5);
		then(actual.getOther().getItems()).hasSizeLessThan(5);
	}

	@Test
	void customMatcherBuilderInstantiateAppliesOnlyInsideMatchingProperty() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				new MatcherOperator<>(
					property -> "box".equals(property.getName()),
					fm -> fm.giveMeBuilder(TaggedBox.class)
						.instantiate(Tagged.class, constructor().parameter(String.class, "text"))
				)
			)
			.build();

		// when
		BoxHolder actual = sut.giveMeOne(BoxHolder.class);

		// then
		then(actual.getBox().getTagged().getOrigin()).isEqualTo("string-constructor");
		then(actual.getOther().getTagged().getOrigin()).isEqualTo("int-constructor");
	}

	@Test
	void customMatcherBuilderInstantiateAppliesToMatchingPropertyItself() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				new MatcherOperator<>(
					property -> "tagged".equals(property.getName()),
					fm -> fm.giveMeBuilder(Tagged.class)
						.instantiate(Tagged.class, constructor().parameter(String.class, "text"))
				)
			)
			.build();

		// when
		TaggedPair actual = sut.giveMeOne(TaggedPair.class);

		// then
		then(actual.getTagged().getOrigin()).isEqualTo("string-constructor");
		then(actual.getOther().getOrigin()).isEqualTo("int-constructor");
	}

	@Test
	void customMatcherBuilderAppliesToMatchingPropertiesAtDifferentDepths() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				new MatcherOperator<>(
					property -> property.getAnnotation(Special.class).isPresent(),
					fm -> fm.giveMeBuilder(Child.class).set("name", "special")
				)
			)
			.build();

		// when
		SpecialHolder actual = sut.giveMeOne(SpecialHolder.class);

		// then
		then(actual.getMain().getName()).isEqualTo("special");
		then(actual.getWrapper().getInner().getName()).isEqualTo("special");
		then(actual.getPlain().getName()).isNotEqualTo("special");
	}

	@Test
	void outerMatchedNodeWinsWhenMatchedNodesAreNested() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				new MatcherOperator<>(
					property -> property.getAnnotation(Special.class).isPresent(),
					fm -> fm.giveMeBuilder(Group.class)
						.set("name", "inner")
						.set("child.name", "outer")
				)
			)
			.build();

		// when
		Group actual = sut.giveMeOne(GroupHolder.class).getGroup();

		// then
		then(actual.getName()).isEqualTo("inner");
		then(actual.getChild().getName()).isEqualTo("outer");
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD, ElementType.PARAMETER})
	public @interface Special {
	}

	public static class Child {
		private final String name;

		@ConstructorProperties("name")
		public Child(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public static class Parent {
		private final Child child;
		private final Child other;

		@ConstructorProperties({"child", "other"})
		public Parent(Child child, Child other) {
			this.child = child;
			this.other = other;
		}

		public Child getChild() {
			return child;
		}

		public Child getOther() {
			return other;
		}
	}

	public static class Bag {
		private final List<String> items;

		@ConstructorProperties("items")
		public Bag(List<String> items) {
			this.items = items;
		}

		public List<String> getItems() {
			return items;
		}
	}

	public static class BagHolder {
		private final Bag bag;
		private final Bag other;

		@ConstructorProperties({"bag", "other"})
		public BagHolder(Bag bag, Bag other) {
			this.bag = bag;
			this.other = other;
		}

		public Bag getBag() {
			return bag;
		}

		public Bag getOther() {
			return other;
		}
	}

	public static class Tagged {
		private final String origin;

		@ConstructorProperties("number")
		public Tagged(int number) {
			this.origin = "int-constructor";
		}

		public Tagged(String text) {
			this.origin = "string-constructor";
		}

		public String getOrigin() {
			return origin;
		}
	}

	public static class TaggedBox {
		private final Tagged tagged;

		@ConstructorProperties("tagged")
		public TaggedBox(Tagged tagged) {
			this.tagged = tagged;
		}

		public Tagged getTagged() {
			return tagged;
		}
	}

	public static class BoxHolder {
		private final TaggedBox box;
		private final TaggedBox other;

		@ConstructorProperties({"box", "other"})
		public BoxHolder(TaggedBox box, TaggedBox other) {
			this.box = box;
			this.other = other;
		}

		public TaggedBox getBox() {
			return box;
		}

		public TaggedBox getOther() {
			return other;
		}
	}

	public static class TaggedPair {
		private final Tagged tagged;
		private final Tagged other;

		@ConstructorProperties({"tagged", "other"})
		public TaggedPair(Tagged tagged, Tagged other) {
			this.tagged = tagged;
			this.other = other;
		}

		public Tagged getTagged() {
			return tagged;
		}

		public Tagged getOther() {
			return other;
		}
	}

	public static class Wrapper {
		@Special
		private final Child inner;

		@ConstructorProperties("inner")
		public Wrapper(Child inner) {
			this.inner = inner;
		}

		public Child getInner() {
			return inner;
		}
	}

	public static class SpecialHolder {
		@Special
		private final Child main;
		private final Child plain;
		private final Wrapper wrapper;

		@ConstructorProperties({"main", "plain", "wrapper"})
		public SpecialHolder(Child main, Child plain, Wrapper wrapper) {
			this.main = main;
			this.plain = plain;
			this.wrapper = wrapper;
		}

		public Child getMain() {
			return main;
		}

		public Child getPlain() {
			return plain;
		}

		public Wrapper getWrapper() {
			return wrapper;
		}
	}

	public static class Group {
		private final String name;
		@Special
		private final Child child;

		@ConstructorProperties({"name", "child"})
		public Group(String name, Child child) {
			this.name = name;
			this.child = child;
		}

		public String getName() {
			return name;
		}

		public Child getChild() {
			return child;
		}
	}

	public static class GroupHolder {
		@Special
		private final Group group;

		@ConstructorProperties("group")
		public GroupHolder(Group group) {
			this.group = group;
		}

		public Group getGroup() {
			return group;
		}
	}
}
