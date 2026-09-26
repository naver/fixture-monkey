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
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;

class RuntimeSubtreeInstantiateTest {
	@Test
	void userInstantiateBuildsInterfaceImplementationChosenDuringAssembly() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new InterfacePlugin().interfaceImplements(Shape.class, Collections.singletonList(Circle.class)))
			.build();

		// when
		Shape actual = sut.giveMeBuilder(ShapeHolder.class)
			.instantiate(Circle.class, constructor().parameter(String.class, "name"))
			.set("shape.name", "user")
			.sample()
			.getShape();

		// then
		then(((Circle)actual).getOrigin()).isEqualTo("string-constructor");
		then(((Circle)actual).getName()).isEqualTo("user");
	}

	@Test
	void nestedInstantiateBuildsInterfaceImplementationChosenDuringAssembly() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new InterfacePlugin().interfaceImplements(Shape.class, Collections.singletonList(Circle.class)))
			.register(
				ShapeHolder.class,
				fm -> fm.giveMeBuilder(ShapeHolder.class)
					.instantiate(Circle.class, constructor().parameter(String.class, "name"))
			)
			.build();

		// when
		Shape actual = sut.giveMeOne(ShapeOwner.class).getShapeHolder().getShape();

		// then
		then(((Circle)actual).getOrigin()).isEqualTo("string-constructor");
		then(((Circle)actual).getName()).isNotNull();
	}

	@Test
	void userInstantiateBuildsReturnValueOfAnonymousInterface() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new InterfacePlugin())
			.build();

		// when
		TaggedView actual = sut.giveMeBuilder(ViewHolder.class)
			.instantiate(Tagged.class, constructor().parameter(String.class, "text"))
			.sample()
			.getView();

		// then
		then(actual).isNotNull();
		then(actual.getTagged().getOrigin()).isEqualTo("string-constructor");
	}

	@Test
	void userInstantiateBuildsElementsAddedWhenContainerGrowsDuringAssembly() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				ListOwner.class,
				fm -> fm.giveMeBuilder(ListOwner.class).set("taggedList.taggeds[5].text", "registered")
			)
			.build();

		// when
		List<Tagged> actual = sut.giveMeBuilder(ListOwnerHolder.class)
			.instantiate(Tagged.class, constructor().parameter(String.class, "text"))
			.sample()
			.getListOwner()
			.getTaggedList()
			.getTaggeds();

		// then
		then(actual).hasSizeGreaterThanOrEqualTo(6);
		then(actual).extracting(Tagged::getOrigin).containsOnly("string-constructor");
		then(actual.get(5).getText()).isEqualTo("registered");
	}

	@Test
	void nestedInstantiateAppliesToOwnerInsideInterfaceImplementationChosenDuringAssembly() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new InterfacePlugin().interfaceImplements(Box.class, Collections.singletonList(ScopedBox.class)))
			.register(
				Scoped.class,
				fm -> fm.giveMeBuilder(Scoped.class)
					.instantiate(Tagged.class, constructor().parameter(String.class, "text"))
			)
			.build();

		// when
		Box actual = sut.giveMeOne(BoxHolder.class).getBox();

		// then
		Tagged tagged = ((ScopedBox)actual).getScoped().getTagged();
		then(tagged.getOrigin()).isEqualTo("string-constructor");
		then(tagged.getText()).isNotNull();
	}

	@Test
	void userSizeAppliesInsideInterfaceImplementationChosenDuringAssembly() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new InterfacePlugin().interfaceImplements(Box.class, Collections.singletonList(ListBox.class)))
			.build();

		// when
		Box actual = sut.giveMeBuilder(BoxHolder.class)
			.size("box.items", 5)
			.sample()
			.getBox();

		// then
		then(((ListBox)actual).getItems()).hasSize(5);
	}

	public static class Tagged {
		private final String origin;
		private final String text;

		@ConstructorProperties("number")
		public Tagged(int number) {
			this.origin = "int-constructor";
			this.text = null;
		}

		public Tagged(String text) {
			this.origin = "string-constructor";
			this.text = text;
		}

		public String getOrigin() {
			return origin;
		}

		public String getText() {
			return text;
		}
	}

	public interface Shape {
	}

	public static class Circle implements Shape {
		private final String origin;
		private final String name;

		@ConstructorProperties("radius")
		public Circle(int radius) {
			this.origin = "int-constructor";
			this.name = null;
		}

		public Circle(String name) {
			this.origin = "string-constructor";
			this.name = name;
		}

		public String getOrigin() {
			return origin;
		}

		public String getName() {
			return name;
		}
	}

	public static class ShapeHolder {
		private final Shape shape;

		@ConstructorProperties("shape")
		public ShapeHolder(Shape shape) {
			this.shape = shape;
		}

		public Shape getShape() {
			return shape;
		}
	}

	public static class ShapeOwner {
		private final ShapeHolder shapeHolder;

		@ConstructorProperties("shapeHolder")
		public ShapeOwner(ShapeHolder shapeHolder) {
			this.shapeHolder = shapeHolder;
		}

		public ShapeHolder getShapeHolder() {
			return shapeHolder;
		}
	}

	public interface TaggedView {
		Tagged getTagged();
	}

	public static class ViewHolder {
		private final TaggedView view;

		@ConstructorProperties("view")
		public ViewHolder(TaggedView view) {
			this.view = view;
		}

		public TaggedView getView() {
			return view;
		}
	}

	public static class TaggedList {
		private final List<Tagged> taggeds;

		@ConstructorProperties("taggeds")
		public TaggedList(List<Tagged> taggeds) {
			this.taggeds = taggeds;
		}

		public List<Tagged> getTaggeds() {
			return taggeds;
		}
	}

	public static class ListOwner {
		private final TaggedList taggedList;

		@ConstructorProperties("taggedList")
		public ListOwner(TaggedList taggedList) {
			this.taggedList = taggedList;
		}

		public TaggedList getTaggedList() {
			return taggedList;
		}
	}

	public static class ListOwnerHolder {
		private final ListOwner listOwner;

		@ConstructorProperties("listOwner")
		public ListOwnerHolder(ListOwner listOwner) {
			this.listOwner = listOwner;
		}

		public ListOwner getListOwner() {
			return listOwner;
		}
	}

	public interface Box {
	}

	public static class Scoped {
		private final Tagged tagged;

		@ConstructorProperties("tagged")
		public Scoped(Tagged tagged) {
			this.tagged = tagged;
		}

		public Tagged getTagged() {
			return tagged;
		}
	}

	public static class ScopedBox implements Box {
		private final Scoped scoped;

		@ConstructorProperties("scoped")
		public ScopedBox(Scoped scoped) {
			this.scoped = scoped;
		}

		public Scoped getScoped() {
			return scoped;
		}
	}

	public static class ListBox implements Box {
		private final List<String> items;

		@ConstructorProperties("items")
		public ListBox(List<String> items) {
			this.items = items;
		}

		public List<String> getItems() {
			return items;
		}
	}

	public static class BoxHolder {
		private final Box box;

		@ConstructorProperties("box")
		public BoxHolder(Box box) {
			this.box = box;
		}

		public Box getBox() {
			return box;
		}
	}
}
