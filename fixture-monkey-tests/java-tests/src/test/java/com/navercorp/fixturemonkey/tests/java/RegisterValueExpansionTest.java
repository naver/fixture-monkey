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

import java.beans.ConstructorProperties;
import java.util.Arrays;

import org.junit.jupiter.api.RepeatedTest;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;
import com.navercorp.fixturemonkey.jackson.plugin.JacksonPlugin;

class RegisterValueExpansionTest {
	@RepeatedTest(5)
	void decomposedRegisteredValueKeepsFieldRenamedByPlugin() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JacksonPlugin())
			.defaultNotNull(true)
			.register(
				Child.class,
				fm -> fm.giveMeBuilder(Child.class).set("$", new Child("registered", "registered"))
			)
			.build();

		// when
		Child actual = sut.giveMeBuilder(Holder.class)
			.set("child.other", "user")
			.sample()
			.getChild();

		// then
		then(actual.getUserName()).isEqualTo("registered");
		then(actual.getOther()).isEqualTo("user");
	}

	@RepeatedTest(10)
	void decomposedRegisteredValueAtInterfaceKeepsUserDirectiveInsideIt() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new InterfacePlugin().interfaceImplements(Shape.class, Arrays.asList(Circle.class, Square.class)))
			.register(
				ShapeHolder.class,
				fm -> fm.giveMeBuilder(ShapeHolder.class).set("$", new ShapeHolder(new Circle("registered", 3)))
			)
			.build();

		// when
		Shape actual = sut.giveMeBuilder(ShapeOwner.class)
			.set("holder.shape.name", "user")
			.sample()
			.getHolder()
			.getShape();

		// then
		then(actual).isInstanceOf(Circle.class);
		then(((Circle)actual).getName()).isEqualTo("user");
		then(((Circle)actual).getRadius()).isEqualTo(3);
	}

	public interface Shape {
	}

	public static class Circle implements Shape {
		private final String name;
		private final int radius;

		@ConstructorProperties({"name", "radius"})
		public Circle(String name, int radius) {
			this.name = name;
			this.radius = radius;
		}

		public String getName() {
			return name;
		}

		public int getRadius() {
			return radius;
		}
	}

	public static class Square implements Shape {
		private final String name;

		@ConstructorProperties("name")
		public Square(String name) {
			this.name = name;
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
		private final ShapeHolder holder;

		@ConstructorProperties("holder")
		public ShapeOwner(ShapeHolder holder) {
			this.holder = holder;
		}

		public ShapeHolder getHolder() {
			return holder;
		}
	}

	public static class Child {
		@JsonProperty("user_name")
		private String userName;
		private String other;

		public Child() {
		}

		public Child(String userName, String other) {
			this.userName = userName;
			this.other = other;
		}

		public String getUserName() {
			return userName;
		}

		public String getOther() {
			return other;
		}
	}

	public static class Holder {
		private Child child;

		public Child getChild() {
			return child;
		}
	}
}
