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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;

class StrictModeTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.plugin(new InterfacePlugin()
			.abstractClassExtends(Shape.class, Collections.singletonList(Circle.class))
			.interfaceImplements(Animal.class, Collections.singletonList(Dog.class)))
		.useExpressionStrictMode()
		.build();

	private static final FixtureMonkey SET_VALUE_SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.plugin(new InterfacePlugin()
			.abstractClassExtends(Value.class, Arrays.asList(StringValue.class, IntValue.class)))
		.useExpressionStrictMode()
		.build();

	@Test
	void setFieldOfAbstractProperty() {
		// when
		Canvas actual = SUT.giveMeBuilder(Canvas.class)
			.set("main.id", "x")
			.sample();

		// then
		then(actual.getMain().getId()).isEqualTo("x");
	}

	@Test
	void setFieldOfAbstractListElement() {
		// when
		ShapeDrawing actual = SUT.giveMeBuilder(ShapeDrawing.class)
			.size("shapes", 1)
			.set("shapes[0].id", "x")
			.sample();

		// then
		then(actual.getShapes().get(0).getId()).isEqualTo("x");
	}

	@Test
	void setSubtypeFieldOfAbstractListElement() {
		// when
		ShapeDrawing actual = SUT.giveMeBuilder(ShapeDrawing.class)
			.size("shapes", 1)
			.set("shapes[0].tags", Collections.singletonList("a"))
			.sample();

		// then
		then(((Circle)actual.getShapes().get(0)).getTags()).containsExactly("a");
	}

	@Test
	void setImplementationFieldOfInterfaceListElement() {
		// when
		Zoo actual = SUT.giveMeBuilder(Zoo.class)
			.size("animals", 1)
			.set("animals[0].name", "Rex")
			.sample();

		// then
		then(((Dog)actual.getAnimals().get(0)).getName()).isEqualTo("Rex");
	}

	@Test
	void setImplementationFieldOfInterfaceProperty() {
		// when
		Owner actual = SUT.giveMeBuilder(Owner.class)
			.set("pet.name", "Rex")
			.sample();

		// then
		then(((Dog)actual.getPet()).getName()).isEqualTo("Rex");
	}

	@Test
	void setNullAbstractPropertyAfterSettingItsField() {
		// when
		Canvas actual = SUT.giveMeBuilder(Canvas.class)
			.set("main.id", "x")
			.setNull("main")
			.sample();

		// then
		then(actual.getMain()).isNull();
	}

	@Test
	void setNullOwnerOfAbstractPropertyAfterSettingItsField() {
		// when
		Frame actual = SUT.giveMeBuilder(Frame.class)
			.set("canvas.main.id", "x")
			.setNull("canvas")
			.sample();

		// then
		then(actual.getCanvas()).isNull();
	}

	@Test
	void setUnknownFieldOfAbstractPropertyThrows() {
		thenThrownBy(() -> SUT.giveMeBuilder(Canvas.class)
			.set("main.nonexistent", "x")
			.sample()
		)
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No matching results for given NodeResolvers");
	}

	@Test
	void setUnknownFieldOfInterfaceListElementThrows() {
		thenThrownBy(() -> SUT.giveMeBuilder(Zoo.class)
			.size("animals", 1)
			.set("animals[0].nonexistent", "x")
			.sample()
		)
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No matching results for given NodeResolvers");
	}

	@Test
	void setListToObjectRoot() {
		// when
		Object actual = SET_VALUE_SUT.giveMeBuilder(Object.class)
			.set("$", new ArrayList<>(Arrays.asList(1, 2)))
			.sample();

		// then
		then(actual).isEqualTo(Arrays.asList(1, 2));
	}

	@Test
	void setMixedTypeListToObjectRoot() {
		// when
		Object actual = SET_VALUE_SUT.giveMeBuilder(Object.class)
			.set("$", new ArrayList<>(Arrays.asList("a", 1)))
			.sample();

		// then
		then(actual).isEqualTo(Arrays.asList("a", 1));
	}

	@Test
	void setNestedMixedTypeListToObjectRoot() {
		// given
		List<List<Object>> value = new ArrayList<>(Arrays.asList(
			new ArrayList<>(Collections.singletonList("a")),
			new ArrayList<>(Collections.singletonList(1))
		));

		// when
		Object actual = SET_VALUE_SUT.giveMeBuilder(Object.class)
			.set("$", value)
			.sample();

		// then
		then(actual).isEqualTo(Arrays.asList(Collections.singletonList("a"), Collections.singletonList(1)));
	}

	@Test
	void setMixedTypeListToObjectPropertyThenSetElement() {
		// when
		ObjectHolder actual = SET_VALUE_SUT.giveMeBuilder(ObjectHolder.class)
			.set("value", new ArrayList<>(Arrays.asList("a", 1)))
			.set("value[1]", 2)
			.sample();

		// then
		then(actual.getValue()).isEqualTo(Arrays.asList("a", 2));
	}

	@Test
	void setEmptyListToObjectPropertyThenGrowIt() {
		// when
		ObjectHolder actual = SET_VALUE_SUT.giveMeBuilder(ObjectHolder.class)
			.set("value", new ArrayList<>())
			.size("value", 2)
			.sample();

		// then
		then(actual.getValue()).isInstanceOfSatisfying(List.class, it -> then(it).hasSize(2));
	}

	@Test
	void setSubclassToObjectRootThenSetItsField() {
		// given
		NamedChild value = new NamedChild();
		value.nickname = "nick";

		// when
		Object actual = SET_VALUE_SUT.giveMeBuilder(Object.class)
			.set("$", value)
			.set("name", "x")
			.sample();

		// then
		then(actual).isInstanceOfSatisfying(NamedChild.class, it -> {
			then(it.getName()).isEqualTo("x");
			then(it.getNickname()).isEqualTo("nick");
		});
	}

	@Test
	void setNestedMixedImplementationListPropertyThenSetInnerField() {
		// given
		List<List<Value>> values = Arrays.asList(
			new ArrayList<>(Collections.singletonList(new StringValue())),
			new ArrayList<>(Collections.singletonList(new IntValue()))
		);

		// when
		NestedValueListHolder actual = SET_VALUE_SUT.giveMeBuilder(NestedValueListHolder.class)
			.set("values", values)
			.set("values[1][0].number", 7)
			.sample();

		// then
		then(actual.getValues().get(0).get(0)).isInstanceOf(StringValue.class);
		then(actual.getValues().get(1).get(0)).isInstanceOfSatisfying(
			IntValue.class,
			it -> then(it.getNumber()).isEqualTo(7)
		);
	}

	@Test
	void setNestedEmptyListPropertyThenGrowInnerList() {
		// when
		NestedStringListHolder actual = SET_VALUE_SUT.giveMeBuilder(NestedStringListHolder.class)
			.set("values", Arrays.asList(new ArrayList<String>(), new ArrayList<String>()))
			.size("values[0]", 2)
			.sample();

		// then
		then(actual.getValues().get(0)).hasSize(2);
		then(actual.getValues().get(1)).isEmpty();
	}

	public abstract static class Shape {
		private String id;

		public String getId() {
			return id;
		}
	}

	public static class Circle extends Shape {
		private List<String> tags;

		public List<String> getTags() {
			return tags;
		}
	}

	public static class Canvas {
		private Shape main;

		public Shape getMain() {
			return main;
		}
	}

	public static class Frame {
		private Canvas canvas;

		public Canvas getCanvas() {
			return canvas;
		}
	}

	public static class ShapeDrawing {
		private List<Shape> shapes;

		public List<Shape> getShapes() {
			return shapes;
		}
	}

	public interface Animal {
	}

	public static class Dog implements Animal {
		private String name;

		public String getName() {
			return name;
		}
	}

	public static class Zoo {
		private List<Animal> animals;

		public List<Animal> getAnimals() {
			return animals;
		}
	}

	public static class Owner {
		private Animal pet;

		public Animal getPet() {
			return pet;
		}
	}

	public static class ObjectHolder {
		private Object value;

		public Object getValue() {
			return value;
		}
	}

	public static class Named {
		private String name;

		public String getName() {
			return name;
		}
	}

	public static class NamedChild extends Named {
		private String nickname;

		public String getNickname() {
			return nickname;
		}
	}

	public abstract static class Value {
	}

	public static class StringValue extends Value {
		private String text;

		public String getText() {
			return text;
		}
	}

	public static class IntValue extends Value {
		private int number;

		public int getNumber() {
			return number;
		}
	}

	public static class NestedValueListHolder {
		private List<List<Value>> values;

		public List<List<Value>> getValues() {
			return values;
		}
	}

	public static class NestedStringListHolder {
		private List<List<String>> values;

		public List<List<String>> getValues() {
			return values;
		}
	}
}
