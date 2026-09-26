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
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;

class RuntimeSubtreeTest {
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

	@Test
	void userSetAppliesDeepInsideRecursiveTypeWhenPlanned() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.build();

		// when
		String actual = sut.giveMeBuilder(NodeHolder.class)
			.set("node.next.next.value", "deep")
			.sample()
			.getNode()
			.getNext()
			.getNext()
			.getValue();

		// then
		then(actual).isEqualTo("deep");
	}

	@Test
	void userSetAppliesDeepInsideRecursiveImplementationChosenDuringAssembly() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new InterfacePlugin().interfaceImplements(Box.class, Collections.singletonList(NodeBox.class)))
			.build();

		// when
		Box actual = sut.giveMeBuilder(BoxHolder.class)
			.set("box.next.next.value", "deep")
			.sample()
			.getBox();

		// then
		then(((NodeBox)actual).getNext().getNext().getValue()).isEqualTo("deep");
	}

	@Test
	@Timeout(value = 30, unit = TimeUnit.SECONDS)
	void cycleThroughImplementationChosenDuringAssemblyTerminates() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.plugin(new InterfacePlugin().interfaceImplements(Box.class, Collections.singletonList(OuterBox.class)))
			.build();

		// when
		Outer actual = sut.giveMeOne(Outer.class);

		// then
		then(actual).isNotNull();
	}

	@Test
	void userInstantiateAppliesInsideTypeWithoutPropertyGeneratorWhenPlanned() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.pushExactTypeArbitraryIntrospector(TaggedBox.class, new DelegatingIntrospector())
			.defaultNotNull(true)
			.build();

		// when
		Tagged actual = sut.giveMeBuilder(TaggedBoxHolder.class)
			.instantiate(Tagged.class, constructor().parameter(String.class, "value"))
			.sample()
			.getTaggedBox()
			.getTagged();

		// then
		then(actual.getOrigin()).isEqualTo("string-constructor");
		then(actual.getText()).isNotNull();
	}

	@Test
	void userInstantiateAppliesInsideImplementationWithoutPropertyGeneratorChosenDuringAssembly() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.pushExactTypeArbitraryIntrospector(TaggedBox.class, new DelegatingIntrospector())
			.plugin(new InterfacePlugin().interfaceImplements(Box.class, Collections.singletonList(TaggedBox.class)))
			.defaultNotNull(true)
			.build();

		// when
		Box actual = sut.giveMeBuilder(BoxHolder.class)
			.instantiate(Tagged.class, constructor().parameter(String.class, "value"))
			.sample()
			.getBox();

		// then
		Tagged tagged = ((TaggedBox)actual).getTagged();
		then(tagged.getOrigin()).isEqualTo("string-constructor");
		then(tagged.getText()).isNotNull();
	}

	@Test
	void userSetOnChildOfSelfRecursiveImplementationApplies() {
		// given
		FixtureMonkey sut = compositeFixtureMonkey();

		// when
		Box actual = sut.giveMeBuilder(BoxHolder.class)
			.set("box.child.name", "user")
			.sample()
			.getBox();

		// then
		Box child = ((Composite)actual).getChild();
		then(child).isInstanceOf(Composite.class);
		then(((Composite)child).getName()).isEqualTo("user");
	}

	@Test
	void userSetDeepInsideSelfRecursiveImplementationApplies() {
		// given
		FixtureMonkey sut = compositeFixtureMonkey();

		// when
		Box actual = sut.giveMeBuilder(BoxHolder.class)
			.set("box.child.child.name", "user")
			.sample()
			.getBox();

		// then
		Box grandchild = ((Composite)((Composite)actual).getChild()).getChild();
		then(((Composite)grandchild).getName()).isEqualTo("user");
	}

	@Test
	void selfRecursiveImplementationIsAsDeepAsSelfRecursiveConcreteType() {
		// given
		FixtureMonkey sut = compositeFixtureMonkey();

		// when
		int concreteDepth = depth(sut.giveMeOne(ConcreteCompositeHolder.class).getComposite());
		int implementationDepth = depth(sut.giveMeOne(BoxHolder.class).getBox());

		// then
		then(implementationDepth).isEqualTo(concreteDepth);
	}

	private static FixtureMonkey compositeFixtureMonkey() {
		return FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new InterfacePlugin().interfaceImplements(Box.class, Collections.singletonList(Composite.class)))
			.build();
	}

	private static int depth(Object composite) {
		int depth = 0;
		Object current = composite;
		while (current != null) {
			depth++;
			current = current instanceof Composite
				? ((Composite)current).getChild()
				: ((ConcreteComposite)current).getChild();
		}
		return depth;
	}

	@Test
	void registeredSizeOfSampleRootAppliesInsideInterfaceImplementationChosenDuringAssembly() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new InterfacePlugin().interfaceImplements(Box.class, Collections.singletonList(ListBox.class)))
			.register(BoxHolder.class, fm -> fm.giveMeBuilder(BoxHolder.class).size("box.items", 5))
			.build();

		// when
		Box actual = sut.giveMeOne(BoxHolder.class).getBox();

		// then
		then(((ListBox)actual).getItems()).hasSize(5);
	}

	@Test
	void registeredSizeOfNestedTypeAppliesInsideInterfaceImplementationChosenDuringAssembly() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new InterfacePlugin().interfaceImplements(Box.class, Collections.singletonList(ListBox.class)))
			.register(BoxHolder.class, fm -> fm.giveMeBuilder(BoxHolder.class).size("box.items", 5))
			.build();

		// when
		Box actual = sut.giveMeOne(BoxHolderOwner.class).getBoxHolder().getBox();

		// then
		then(((ListBox)actual).getItems()).hasSize(5);
	}

	@Test
	void registeredSizeOfInterfaceImplementationAppliesInsideIt() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new InterfacePlugin().interfaceImplements(Box.class, Collections.singletonList(ListBox.class)))
			.register(ListBox.class, fm -> fm.giveMeBuilder(ListBox.class).size("items", 5))
			.build();

		// when
		Box actual = sut.giveMeOne(BoxHolder.class).getBox();

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

	public static class DelegatingIntrospector implements ArbitraryIntrospector {
		@Override
		public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
			return ConstructorPropertiesArbitraryIntrospector.INSTANCE.introspect(context);
		}
	}

	public static class NodeBox implements Box {
		private final NodeBox next;
		private final String value;

		@ConstructorProperties({"next", "value"})
		public NodeBox(NodeBox next, String value) {
			this.next = next;
			this.value = value;
		}

		public NodeBox getNext() {
			return next;
		}

		public String getValue() {
			return value;
		}
	}

	public static class NodeHolder {
		private final NodeBox node;

		@ConstructorProperties("node")
		public NodeHolder(NodeBox node) {
			this.node = node;
		}

		public NodeBox getNode() {
			return node;
		}
	}

	public static class Outer {
		private final Box box;

		@ConstructorProperties("box")
		public Outer(Box box) {
			this.box = box;
		}

		public Box getBox() {
			return box;
		}
	}

	public static class OuterBox implements Box {
		private final Outer outer;

		@ConstructorProperties("outer")
		public OuterBox(Outer outer) {
			this.outer = outer;
		}

		public Outer getOuter() {
			return outer;
		}
	}

	public static class TaggedBox implements Box {
		private final Tagged tagged;

		@ConstructorProperties("tagged")
		public TaggedBox(Tagged tagged) {
			this.tagged = tagged;
		}

		public Tagged getTagged() {
			return tagged;
		}
	}

	public static class TaggedBoxHolder {
		private final TaggedBox taggedBox;

		@ConstructorProperties("taggedBox")
		public TaggedBoxHolder(TaggedBox taggedBox) {
			this.taggedBox = taggedBox;
		}

		public TaggedBox getTaggedBox() {
			return taggedBox;
		}
	}

	public static class Composite implements Box {
		private final Box child;
		private final String name;

		@ConstructorProperties({"child", "name"})
		public Composite(Box child, String name) {
			this.child = child;
			this.name = name;
		}

		public Box getChild() {
			return child;
		}

		public String getName() {
			return name;
		}
	}

	public static class ConcreteComposite {
		private final ConcreteComposite child;
		private final String name;

		@ConstructorProperties({"child", "name"})
		public ConcreteComposite(ConcreteComposite child, String name) {
			this.child = child;
			this.name = name;
		}

		public ConcreteComposite getChild() {
			return child;
		}

		public String getName() {
			return name;
		}
	}

	public static class ConcreteCompositeHolder {
		private final ConcreteComposite composite;

		@ConstructorProperties("composite")
		public ConcreteCompositeHolder(ConcreteComposite composite) {
			this.composite = composite;
		}

		public ConcreteComposite getComposite() {
			return composite;
		}
	}

	public static class BoxHolderOwner {
		private final BoxHolder boxHolder;

		@ConstructorProperties("boxHolder")
		public BoxHolderOwner(BoxHolder boxHolder) {
			this.boxHolder = boxHolder;
		}

		public BoxHolder getBoxHolder() {
			return boxHolder;
		}
	}
}
