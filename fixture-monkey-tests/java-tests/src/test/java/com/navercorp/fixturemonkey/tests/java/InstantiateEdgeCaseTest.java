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
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;
import com.navercorp.fixturemonkey.api.type.TypeReference;

class InstantiateEdgeCaseTest {
	@Test
	void userInstantiateAppliesToElementsBeyondSetLimit() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.build();

		// when
		List<Tagged> actual = sut.giveMeBuilder(TaggedList.class)
			.instantiate(Tagged.class, constructor().parameter(String.class))
			.size("taggeds", 3)
			.set("taggeds[*]", new Tagged("fixed"), 1)
			.sample()
			.getTaggeds();

		// then
		then(actual).extracting(Tagged::getOrigin).containsOnly("string-constructor");
	}

	@Test
	void userInstantiateAppliesToInterfaceImplementation() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new InterfacePlugin().interfaceImplements(Shape.class, Collections.singletonList(Circle.class)))
			.build();

		// when
		Shape actual = sut.giveMeBuilder(ShapeHolder.class)
			.instantiate(Circle.class, constructor().parameter(String.class))
			.sample()
			.getShape();

		// then
		then(actual).isInstanceOf(Circle.class);
		then(((Circle)actual).getOrigin()).isEqualTo("string-constructor");
	}

	@Test
	void userSetAppliesInsideSubtreeRebuiltForNestedInstantiate() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				Scoped.class,
				fm -> fm.giveMeBuilder(Scoped.class)
					.instantiate(Tagged.class, constructor().parameter(String.class, "text"))
			)
			.build();

		// when
		Tagged actual = sut.giveMeBuilder(ScopeHolder.class)
			.set("scoped.tagged.text", "user")
			.sample()
			.getScoped()
			.getTagged();

		// then
		then(actual.getOrigin()).isEqualTo("string-constructor");
		then(actual.getText()).isEqualTo("user");
	}

	@Test
	void nestedInstantiateAppliesInsideEachContainerElementOwner() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				Scoped.class,
				fm -> fm.giveMeBuilder(Scoped.class).instantiate(Tagged.class, constructor().parameter(String.class))
			)
			.build();

		// when
		List<Scoped> actual = sut.giveMeBuilder(ScopedList.class)
			.size("scopeds", 2)
			.sample()
			.getScopeds();

		// then
		then(actual).extracting(it -> it.getTagged().getOrigin()).containsOnly("string-constructor");
	}

	@Test
	void userSizeAppliesInsideSubtreeRebuiltForNestedInstantiate() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				BagScope.class,
				fm -> fm.giveMeBuilder(BagScope.class)
					.instantiate(
						Bag.class,
						constructor().parameter(new TypeReference<List<String>>() {
						}, "items")
					)
			)
			.build();

		// when
		Bag actual = sut.giveMeBuilder(BagHolder.class)
			.size("bagScope.bag.items", 5)
			.sample()
			.getBagScope()
			.getBag();

		// then
		then(actual.getOrigin()).isEqualTo("list-constructor");
		then(actual.getItems()).hasSize(5);
	}

	@Test
	void nestedInstantiateAppliesToTypeTheDefaultIntrospectorCannotConstruct() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				PlainScoped.class,
				fm -> fm.giveMeBuilder(PlainScoped.class)
					.instantiate(Plain.class, constructor().parameter(String.class, "text"))
			)
			.build();

		// when
		Plain actual = sut.giveMeOne(PlainScopeHolder.class).getPlainScoped().getPlain();

		// then
		then(actual.getOrigin()).isEqualTo("string-constructor");
		then(actual.getText()).isNotNull();
	}

	@Test
	void nestedInstantiateAppliesToMapValueInsideOwner() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				MapScoped.class,
				fm -> fm.giveMeBuilder(MapScoped.class)
					.instantiate(Tagged.class, constructor().parameter(String.class, "text"))
			)
			.build();

		// when
		Map<String, Tagged> actual = sut.giveMeBuilder(MapScopeHolder.class)
			.size("mapScoped.taggedByKey", 2)
			.sample()
			.getMapScoped()
			.getTaggedByKey();

		// then
		then(actual.values()).extracting(Tagged::getOrigin).containsOnly("string-constructor");
		then(actual.values()).extracting(Tagged::getText).doesNotContainNull();
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

	public interface Shape {
	}

	public static class Circle implements Shape {
		private final String origin;

		@ConstructorProperties("radius")
		public Circle(int radius) {
			this.origin = "int-constructor";
		}

		public Circle(String name) {
			this.origin = "string-constructor";
		}

		public String getOrigin() {
			return origin;
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

	public static class ScopeHolder {
		private final Scoped scoped;

		@ConstructorProperties("scoped")
		public ScopeHolder(Scoped scoped) {
			this.scoped = scoped;
		}

		public Scoped getScoped() {
			return scoped;
		}
	}

	public static class ScopedList {
		private final List<Scoped> scopeds;

		@ConstructorProperties("scopeds")
		public ScopedList(List<Scoped> scopeds) {
			this.scopeds = scopeds;
		}

		public List<Scoped> getScopeds() {
			return scopeds;
		}
	}

	public static class Bag {
		private final String origin;
		private final List<String> items;

		@ConstructorProperties("count")
		public Bag(int count) {
			this.origin = "int-constructor";
			this.items = Collections.emptyList();
		}

		public Bag(List<String> items) {
			this.origin = "list-constructor";
			this.items = items;
		}

		public String getOrigin() {
			return origin;
		}

		public List<String> getItems() {
			return items;
		}
	}

	public static class BagScope {
		private final Bag bag;

		@ConstructorProperties("bag")
		public BagScope(Bag bag) {
			this.bag = bag;
		}

		public Bag getBag() {
			return bag;
		}
	}

	public static class BagHolder {
		private final BagScope bagScope;

		@ConstructorProperties("bagScope")
		public BagHolder(BagScope bagScope) {
			this.bagScope = bagScope;
		}

		public BagScope getBagScope() {
			return bagScope;
		}
	}

	public static class Plain {
		private final String origin;
		private final String text;

		public Plain(int number) {
			this.origin = "int-constructor";
			this.text = null;
		}

		public Plain(String text) {
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

	public static class PlainScoped {
		private final Plain plain;

		@ConstructorProperties("plain")
		public PlainScoped(Plain plain) {
			this.plain = plain;
		}

		public Plain getPlain() {
			return plain;
		}
	}

	public static class PlainScopeHolder {
		private final PlainScoped plainScoped;

		@ConstructorProperties("plainScoped")
		public PlainScopeHolder(PlainScoped plainScoped) {
			this.plainScoped = plainScoped;
		}

		public PlainScoped getPlainScoped() {
			return plainScoped;
		}
	}

	public static class MapScoped {
		private final Map<String, Tagged> taggedByKey;

		@ConstructorProperties("taggedByKey")
		public MapScoped(Map<String, Tagged> taggedByKey) {
			this.taggedByKey = taggedByKey;
		}

		public Map<String, Tagged> getTaggedByKey() {
			return taggedByKey;
		}
	}

	public static class MapScopeHolder {
		private final MapScoped mapScoped;

		@ConstructorProperties("mapScoped")
		public MapScopeHolder(MapScoped mapScoped) {
			this.mapScoped = mapScoped;
		}

		public MapScoped getMapScoped() {
			return mapScoped;
		}
	}
}
