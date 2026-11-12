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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.FixtureMonkeyBuilder;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;

class ContainerElementSubtypeTest {
	@Test
	void setMixedSubtypesIntoAbstractList() {
		// given
		FixtureMonkey sut = builder().build();
		Circle circle = sut.giveMeOne(Circle.class);
		Square square = sut.giveMeOne(Square.class);

		// when
		Canvas actual = sut.giveMeBuilder(Canvas.class)
			.set("shapes", Arrays.asList(circle, square))
			.sample();

		// then
		then(actual.getShapes()).hasExactlyElementsOfTypes(Circle.class, Square.class);
	}

	@Test
	void setMixedSubtypesIntoAbstractListInStrictMode() {
		// given
		FixtureMonkey sut = builder()
			.useExpressionStrictMode()
			.build();
		Circle circle = sut.giveMeOne(Circle.class);
		Square square = sut.giveMeOne(Square.class);

		// when
		Canvas actual = sut.giveMeBuilder(Canvas.class)
			.set("shapes", Arrays.asList(circle, square))
			.sample();

		// then
		then(actual.getShapes()).hasExactlyElementsOfTypes(Circle.class, Square.class);
	}

	@Test
	void setMixedSubtypesIntoAbstractListKeepsElementOrder() {
		// given
		FixtureMonkey sut = builder().build();
		Circle circle = sut.giveMeOne(Circle.class);
		Square square = sut.giveMeOne(Square.class);

		// when
		Canvas actual = sut.giveMeBuilder(Canvas.class)
			.set("shapes", Arrays.asList(square, circle, square))
			.sample();

		// then
		then(actual.getShapes()).hasExactlyElementsOfTypes(Square.class, Circle.class, Square.class);
	}

	@Test
	void setMixedSubtypesIntoAbstractListKeepsElementValues() {
		// given
		FixtureMonkey sut = builder().build();
		Circle circle = sut.giveMeOne(Circle.class);
		Square square = sut.giveMeOne(Square.class);

		// when
		Canvas actual = sut.giveMeBuilder(Canvas.class)
			.set("shapes", Arrays.asList(circle, square))
			.sample();

		// then
		then(((Circle)actual.getShapes().get(0)).getRadius()).isEqualTo(circle.getRadius());
		then(((Square)actual.getShapes().get(1)).getSide()).isEqualTo(square.getSide());
	}

	@Test
	void setMixedImplementationsIntoInterfaceList() {
		// given
		FixtureMonkey sut = builder().build();
		Dot dot = sut.giveMeOne(Dot.class);
		Line line = sut.giveMeOne(Line.class);

		// when
		Drawing actual = sut.giveMeBuilder(Drawing.class)
			.set("figures", Arrays.asList(dot, line))
			.sample();

		// then
		then(actual.getFigures()).hasExactlyElementsOfTypes(Dot.class, Line.class);
	}

	@Test
	void setMixedSubtypesIntoAbstractMapValues() {
		// given
		FixtureMonkey sut = builder().build();
		Map<String, Shape> shapes = new LinkedHashMap<>();
		shapes.put("circle", sut.giveMeOne(Circle.class));
		shapes.put("square", sut.giveMeOne(Square.class));

		// when
		NamedCanvas actual = sut.giveMeBuilder(NamedCanvas.class)
			.set("shapes", shapes)
			.sample();

		// then
		then(actual.getShapes().get("circle")).isInstanceOf(Circle.class);
		then(actual.getShapes().get("square")).isInstanceOf(Square.class);
	}

	private static FixtureMonkeyBuilder builder() {
		return FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.plugin(new InterfacePlugin()
				.abstractClassExtends(Shape.class, Arrays.asList(Circle.class, Square.class))
				.interfaceImplements(Figure.class, Arrays.asList(Dot.class, Line.class)));
	}

	public abstract static class Shape {
		private String id;

		public String getId() {
			return id;
		}
	}

	public static class Circle extends Shape {
		private Double radius;

		public Double getRadius() {
			return radius;
		}
	}

	public static class Square extends Shape {
		private Double side;

		public Double getSide() {
			return side;
		}
	}

	public static class Canvas {
		private List<Shape> shapes;

		public List<Shape> getShapes() {
			return shapes;
		}
	}

	public static class NamedCanvas {
		private Map<String, Shape> shapes;

		public Map<String, Shape> getShapes() {
			return shapes;
		}
	}

	public interface Figure {
	}

	public static class Dot implements Figure {
		private Integer position;

		public Integer getPosition() {
			return position;
		}
	}

	public static class Line implements Figure {
		private Integer length;

		public Integer getLength() {
			return length;
		}
	}

	public static class Drawing {
		private List<Figure> figures;

		public List<Figure> getFigures() {
			return figures;
		}
	}
}
