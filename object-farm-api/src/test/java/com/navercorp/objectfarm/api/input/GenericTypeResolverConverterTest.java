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

package com.navercorp.objectfarm.api.input;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.navercorp.objectfarm.api.type.JvmType;

class GenericTypeResolverConverterTest {
	@Test
	void collectionOfSameClassResolvesToThatClass() {
		// given
		List<Shape> shapes = Arrays.asList(new Circle(), new Circle());

		// when
		List<JvmType> actual = GenericTypeResolverConverter.extractCollectionTypeArguments(shapes);

		// then
		then(actual).hasSize(1);
		then(actual.get(0).getRawType()).isEqualTo(Circle.class);
	}

	@Test
	void collectionOfDifferentSubclassesResolvesToCommonSuperclass() {
		// given
		List<Shape> shapes = Arrays.asList(new Circle(), new Square());

		// when
		List<JvmType> actual = GenericTypeResolverConverter.extractCollectionTypeArguments(shapes);

		// then
		then(actual).hasSize(1);
		then(actual.get(0).getRawType()).isEqualTo(Shape.class);
	}

	@Test
	void collectionOfUnrelatedClassesIsNotResolved() {
		// given
		List<Object> values = Arrays.asList(1, "one");

		// when
		List<JvmType> actual = GenericTypeResolverConverter.extractCollectionTypeArguments(values);

		// then
		then(actual).isEmpty();
	}

	@Test
	void collectionSkipsNullElements() {
		// given
		List<Shape> shapes = Arrays.asList(null, new Circle(), null, new Square());

		// when
		List<JvmType> actual = GenericTypeResolverConverter.extractCollectionTypeArguments(shapes);

		// then
		then(actual).hasSize(1);
		then(actual.get(0).getRawType()).isEqualTo(Shape.class);
	}

	@Test
	void collectionOfOnlyNullsIsNotResolved() {
		// when
		List<JvmType> actual = GenericTypeResolverConverter.extractCollectionTypeArguments(
			Collections.singletonList(null)
		);

		// then
		then(actual).isEmpty();
	}

	@Test
	void mapOfDifferentSubclassValuesResolvesToCommonSuperclass() {
		// given
		Map<String, Shape> shapes = new LinkedHashMap<>();
		shapes.put("circle", new Circle());
		shapes.put("square", new Square());

		// when
		List<JvmType> actual = GenericTypeResolverConverter.extractMapTypeArguments(shapes);

		// then
		then(actual).hasSize(2);
		then(actual.get(0).getRawType()).isEqualTo(String.class);
		then(actual.get(1).getRawType()).isEqualTo(Shape.class);
	}

	abstract static class Shape {
	}

	static class Circle extends Shape {
	}

	static class Square extends Shape {
	}
}
