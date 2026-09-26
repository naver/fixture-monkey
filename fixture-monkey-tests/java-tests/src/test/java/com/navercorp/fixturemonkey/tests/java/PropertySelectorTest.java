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

import static com.navercorp.fixturemonkey.api.experimental.JavaGetterMethodPropertySelector.javaGetter;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.beans.ConstructorProperties;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.tests.java.specs.ImmutableSpecs.ContainerObject;
import com.navercorp.fixturemonkey.tests.java.specs.ImmutableSpecs.JavaTypeObject;
import com.navercorp.fixturemonkey.tests.java.specs.ImmutableSpecs.RootJavaTypeObject;
import com.navercorp.fixturemonkey.tests.java.specs.NestedClassSpecs.Inner;

class PropertySelectorTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	private static final FixtureMonkey USER = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	private static final FixtureMonkey STRICT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.useExpressionStrictMode()
		.register(StrictModeChild.class, fm -> fm.giveMeBuilder(StrictModeChild.class).set("name", "registered"))
		.build();

	@Test
	void nestedObject() {
		Inner actual = SUT.giveMeOne(Inner.class);

		then(actual).isNotNull();
	}

	@Test
	void setJavaGetter() {
		String actual = SUT.giveMeBuilder(JavaTypeObject.class)
			.set(javaGetter(JavaTypeObject::getString), "test")
			.sample()
			.getString();

		then(actual).isEqualTo("test");
	}

	@Test
	void setJavaGetterInto() {
		String actual = SUT.giveMeBuilder(RootJavaTypeObject.class)
			.set(javaGetter(RootJavaTypeObject::getValue).into(JavaTypeObject::getString), "test")
			.sample()
			.getValue()
			.getString();

		then(actual).isEqualTo("test");
	}

	@Test
	void setJavaGetterCollection() {
		String actual = SUT.giveMeBuilder(ContainerObject.class)
			.size("list", 1)
			.set(javaGetter(ContainerObject::getList).index(String.class, 0), "test")
			.sample()
			.getList()
			.get(0);

		then(actual).isEqualTo("test");
	}

	@Test
	void setJavaGetterCollectionElement() {
		String actual = SUT.giveMeBuilder(ContainerObject.class)
			.size("complexList", 1)
			.set(
				javaGetter(ContainerObject::getComplexList)
					.index(JavaTypeObject.class, 0)
					.into(JavaTypeObject::getString), "test"
			)
			.sample()
			.getComplexList()
			.get(0)
			.getString();

		then(actual).isEqualTo("test");
	}

	@Test
	void setJavaGetterCollectionAllElement() {
		String expected = "test";

		List<String> actual = SUT.giveMeBuilder(ContainerObject.class)
			.size("complexList", 3)
			.set(
				javaGetter(ContainerObject::getComplexList)
					.allIndex(JavaTypeObject.class)
					.into(JavaTypeObject::getString), expected
			)
			.sample()
			.getComplexList()
			.stream()
			.map(JavaTypeObject::getString)
			.collect(Collectors.toList());

		then(actual).allMatch(expected::equals);
	}

	@Test
	void typedJavaGetter() {
		String expected = "expected";

		String actual = SUT.giveMeBuilder(JavaTypeObject.class)
			.customizeProperty(javaGetter(JavaTypeObject::getString), arb -> arb.map(it -> expected))
			.sample()
			.getString();

		then(actual).isEqualTo(expected);
	}

	@Test
	void nestedTypedJavaGetter() {
		String expected = "expected";

		String actual = SUT.giveMeBuilder(RootJavaTypeObject.class)
			.customizeProperty(javaGetter(RootJavaTypeObject::getValue).into(JavaTypeObject::getString),
				arb -> arb.map(it -> expected))
			.sample()
			.getValue()
			.getString();

		then(actual).isEqualTo(expected);
	}

	@Test
	void indexTypedJavaGetter() {
		String expected = "expected";

		String actual = SUT.giveMeBuilder(ContainerObject.class)
			.size("list", 1)
			.customizeProperty(javaGetter(ContainerObject::getList).index(String.class, 0),
				arb -> arb.map(it -> expected))
			.sample()
			.getList()
			.get(0);

		then(actual).isEqualTo(expected);
	}

	@Test
	void setExp() {
		String actual = SUT.giveMeJavaBuilder(JavaTypeObject.class)
			.setExpGetter(JavaTypeObject::getString, "test")
			.sample()
			.getString();

		then(actual).isEqualTo("test");
	}

	@Test
	void setExpCollectionElement() {
		String actual = SUT.giveMeJavaBuilder(ContainerObject.class)
			.size("complexList", 1)
			.setExpGetter(
				javaGetter(ContainerObject::getComplexList)
					.index(JavaTypeObject.class, 0)
					.into(JavaTypeObject::getString),
				"test"
			)
			.sample()
			.getComplexList()
			.get(0)
			.getString();

		then(actual).isEqualTo("test");
	}

	@RepeatedTest(10)
	void setFieldWildcardAppliesToEveryField() {
		// when
		Child actual = USER.giveMeBuilder(Parent.class).set("child.*", "fixed").sample().getChild();

		// then
		then(actual.getName()).isEqualTo("fixed");
		then(actual.getNick()).isEqualTo("fixed");
	}

	@RepeatedTest(10)
	void registeredSetFieldWildcardAppliesToEveryField() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(Child.class, fm -> fm.giveMeBuilder(Child.class).set("*", "fixed"))
			.build();

		// when
		Child actual = sut.giveMeOne(Parent.class).getChild();

		// then
		then(actual.getName()).isEqualTo("fixed");
		then(actual.getNick()).isEqualTo("fixed");
	}

	@RepeatedTest(10)
	void setIndexUnionAppliesToEachIndex() {
		// when
		List<String> actual = USER.giveMeBuilder(Outer.class)
			.size("holder.first", 3)
			.set("holder.first[0,1]", "fixed")
			.sample()
			.getHolder()
			.getFirst();

		// then
		then(actual).hasSize(3);
		then(actual.get(0)).isEqualTo("fixed");
		then(actual.get(1)).isEqualTo("fixed");
	}

	@RepeatedTest(10)
	void registeredSetIndexUnionAppliesToEachIndex() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				Holder.class,
				fm -> fm.giveMeBuilder(Holder.class).size("first", 3).set("first[0,1]", "fixed")
			)
			.build();

		// when
		List<String> actual = sut.giveMeOne(Outer.class).getHolder().getFirst();

		// then
		then(actual).hasSize(3);
		then(actual.get(0)).isEqualTo("fixed");
		then(actual.get(1)).isEqualTo("fixed");
	}

	@RepeatedTest(10)
	void sizeFieldWildcardAppliesToEveryContainer() {
		// when
		Holder actual = USER.giveMeBuilder(Outer.class).size("holder.*", 4).sample().getHolder();

		// then
		then(actual.getFirst()).hasSize(4);
		then(actual.getSecond()).hasSize(4);
	}

	@RepeatedTest(10)
	void registeredSizeFieldWildcardAppliesToEveryContainer() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(Holder.class, fm -> fm.giveMeBuilder(Holder.class).size("*", 4))
			.build();

		// when
		Holder actual = sut.giveMeOne(Outer.class).getHolder();

		// then
		then(actual.getFirst()).hasSize(4);
		then(actual.getSecond()).hasSize(4);
	}

	@RepeatedTest(10)
	void userWildcardPostConditionAppliesToEveryElement() {
		// when
		List<String> actual = USER.giveMeBuilder(ValuesHolder.class)
			.size("values", 5)
			.setPostCondition("values[*]", String.class, it -> it.length() < 2)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(5).allSatisfy(it -> then(it).hasSizeLessThan(2));
	}

	@RepeatedTest(10)
	void registeredWildcardPostConditionAppliesToEveryElement() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				ValuesHolder.class,
				fm -> fm.giveMeBuilder(ValuesHolder.class)
					.size("values", 5)
					.setPostCondition("values[*]", String.class, it -> it.length() < 2)
			)
			.build();

		// when
		List<String> actual = sut.giveMeOne(ValuesWrapper.class).getHolder().getValues();

		// then
		then(actual).hasSize(5).allSatisfy(it -> then(it).hasSizeLessThan(2));
	}

	@Test
	void strictModeAcceptsIndexUnionPath() {
		// when
		List<String> actual = STRICT.giveMeBuilder(StrictModeHolder.class)
			.size("first", 3)
			.set("first[0,1]", "fixed")
			.sample()
			.getFirst();

		// then
		then(actual.get(0)).isEqualTo("fixed");
		then(actual.get(1)).isEqualTo("fixed");
	}

	@Test
	void strictModeRejectsUnknownPathEvenWithRegisteredBuilder() {
		// when, then
		thenThrownBy(() -> STRICT.giveMeBuilder(StrictModeParent.class).set("child.unknown", "x").sample())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("child.unknown");
	}

	@Test
	void strictModeAcceptsPathInsideRegisteredType() {
		// when
		StrictModeChild actual = STRICT.giveMeBuilder(StrictModeParent.class)
			.set("child.name", "user")
			.sample()
			.getChild();

		// then
		then(actual.getName()).isEqualTo("user");
	}

	@RepeatedTest(10)
	void userSizeAppliesOnlyToTargetedSibling() {
		// when
		BagHolder actual = SUT.giveMeBuilder(BagHolder.class)
			.size("bag.items", 5)
			.sample();

		// then
		then(actual.getBag().getItems()).hasSize(5);
		then(actual.getOther().getItems()).hasSizeLessThan(5);
	}

	@RepeatedTest(10)
	void userSizesOnSiblingsApplyIndependently() {
		// when
		BagHolder actual = SUT.giveMeBuilder(BagHolder.class)
			.size("bag.items", 4)
			.size("other.items", 1)
			.sample();

		// then
		then(actual.getBag().getItems()).hasSize(4);
		then(actual.getOther().getItems()).hasSize(1);
	}

	@Test
	void defaultNotNullAppliesInsideSameTypeSiblingsOfContainerElement() {
		// when
		List<Line> actual = SUT.giveMe(LineList.class, 50)
			.stream()
			.flatMap(it -> it.getLines().stream())
			.collect(Collectors.toList());

		// then
		then(actual).isNotEmpty();
		then(actual).allSatisfy(line -> {
			then(line.getItem().getTags()).isNotNull();
			then(line.getGift().getTags()).isNotNull();
		});
	}

	@Data
	public static class Parent {
		private Child child;
	}

	@Data
	public static class Child {
		private String name;
		private String nick;
	}

	@Data
	public static class Outer {
		private Holder holder;
	}

	@Data
	public static class Holder {
		private List<String> first;
		private List<String> second;
	}

	@Data
	public static class ValuesWrapper {
		private ValuesHolder holder;
	}

	@Data
	public static class ValuesHolder {
		private List<String> values;
	}

	@Data
	public static class StrictModeParent {
		private StrictModeChild child;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class StrictModeChild {
		private String name;
	}

	@Data
	public static class StrictModeHolder {
		private List<String> first;
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

	public static class Item {
		private final List<String> tags;

		@ConstructorProperties("tags")
		public Item(List<String> tags) {
			this.tags = tags;
		}

		public List<String> getTags() {
			return tags;
		}
	}

	public static class Line {
		private final Item item;
		private final Item gift;

		@ConstructorProperties({"item", "gift"})
		public Line(Item item, Item gift) {
			this.item = item;
			this.gift = gift;
		}

		public Item getItem() {
			return item;
		}

		public Item getGift() {
			return gift;
		}
	}

	public static class LineList {
		private final List<Line> lines;
		private final Item main;

		@ConstructorProperties({"lines", "main"})
		public LineList(List<Line> lines, Item main) {
			this.lines = lines;
			this.main = main;
		}

		public Item getMain() {
			return main;
		}

		public List<Line> getLines() {
			return lines;
		}
	}
}
