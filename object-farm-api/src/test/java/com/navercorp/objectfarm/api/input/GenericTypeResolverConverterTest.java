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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.navercorp.objectfarm.api.node.GenericTypeResolver;
import com.navercorp.objectfarm.api.tree.PathResolver;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.ReflectiveJvmType;

class GenericTypeResolverConverterTest {
	private static final JvmType STRING = new ReflectiveJvmType(String.class);
	private static final JvmType LIST_OF_STRING = new ReflectiveJvmType(
		List.class,
		Collections.singletonList(STRING),
		Collections.emptyList()
	);
	private static final JvmType LIST_OF_LIST_OF_STRING = new ReflectiveJvmType(
		List.class,
		Collections.singletonList(LIST_OF_STRING),
		Collections.emptyList()
	);

	@Test
	void keepsDeclaredElementTypeWhenElementIsNonInstantiableJdkList() {
		// given
		List<List<String>> value = Arrays.asList(Arrays.asList("a", "b"), Arrays.asList("c", "d"));
		PathResolver<GenericTypeResolver> resolver = GenericTypeResolverConverter.fromValue("$.values", value);

		// when
		JvmType actual = resolver.getCustomizer().resolve(LIST_OF_LIST_OF_STRING);

		// then
		then(actual.getRawType()).isEqualTo(List.class);
		then(actual.getTypeVariables()).hasSize(1);
		then(actual.getTypeVariables().get(0).getRawType()).isEqualTo(List.class);
		then(actual.getTypeVariables().get(0).getTypeVariables().get(0).getRawType()).isEqualTo(String.class);
	}

	@Test
	void keepsDeclaredElementTypeWhenElementIsUnmodifiableList() {
		// given
		List<List<String>> value = Collections.singletonList(
			Collections.unmodifiableList(new ArrayList<>(Arrays.asList("a", "b")))
		);
		PathResolver<GenericTypeResolver> resolver = GenericTypeResolverConverter.fromValue("$.values", value);

		// when
		JvmType actual = resolver.getCustomizer().resolve(LIST_OF_LIST_OF_STRING);

		// then
		then(actual.getTypeVariables().get(0).getRawType()).isEqualTo(List.class);
	}

	@Test
	void usesInferredElementTypeWhenElementIsInstantiable() {
		// given
		List<List<String>> value = Collections.singletonList(new LinkedList<>(Arrays.asList("a", "b")));
		PathResolver<GenericTypeResolver> resolver = GenericTypeResolverConverter.fromValue("$.values", value);

		// when
		JvmType actual = resolver.getCustomizer().resolve(LIST_OF_LIST_OF_STRING);

		// then
		then(actual.getTypeVariables().get(0).getRawType()).isEqualTo(LinkedList.class);
	}

	@Test
	void usesInferredElementTypeWhenDeclaredTypeIsNotAContainerInterface() {
		// given
		List<Object> value = Collections.singletonList(Arrays.asList("a", "b"));
		PathResolver<GenericTypeResolver> resolver = GenericTypeResolverConverter.fromValue("$.values", value);
		JvmType listOfObject = new ReflectiveJvmType(
			List.class,
			Collections.singletonList(new ReflectiveJvmType(Object.class)),
			Collections.emptyList()
		);

		// when
		JvmType actual = resolver.getCustomizer().resolve(listOfObject);

		// then
		Type inferred = actual.getTypeVariables().get(0).getRawType();
		then(inferred).isEqualTo(Arrays.asList("a", "b").getClass());
	}

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
	void collectionOfDifferentSubclassesIsNotResolved() {
		// given
		List<Shape> shapes = Arrays.asList(new Circle(), new Square());

		// when
		List<JvmType> actual = GenericTypeResolverConverter.extractCollectionTypeArguments(shapes);

		// then
		then(actual).isEmpty();
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
	void collectionOfDifferentSubclassesWithNullsIsNotResolved() {
		// given
		List<Shape> shapes = Arrays.asList(null, new Circle(), null, new Square());

		// when
		List<JvmType> actual = GenericTypeResolverConverter.extractCollectionTypeArguments(shapes);

		// then
		then(actual).isEmpty();
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
	void mapOfDifferentSubclassValuesIsNotResolved() {
		// given
		Map<String, Shape> shapes = new LinkedHashMap<>();
		shapes.put("circle", new Circle());
		shapes.put("square", new Square());

		// when
		List<JvmType> actual = GenericTypeResolverConverter.extractMapTypeArguments(shapes);

		// then
		then(actual).isEmpty();
	}

	@Test
	void collectionOfSameClassSkipsNullElements() {
		// given
		List<Shape> shapes = Arrays.asList(null, new Circle(), null, new Circle());

		// when
		List<JvmType> actual = GenericTypeResolverConverter.extractCollectionTypeArguments(shapes);

		// then
		then(actual).hasSize(1);
		then(actual.get(0).getRawType()).isEqualTo(Circle.class);
	}

	@Test
	void collectionOfSuperclassAndSubclassIsNotResolved() {
		// given
		List<Base> values = Arrays.asList(new Base(), new Dog());

		// when
		List<JvmType> actual = GenericTypeResolverConverter.extractCollectionTypeArguments(values);

		// then
		then(actual).isEmpty();
	}

	@Test
	void collectionOfSubclassAndSuperclassIsNotResolved() {
		// given
		List<Base> values = Arrays.asList(new Dog(), new Base());

		// when
		List<JvmType> actual = GenericTypeResolverConverter.extractCollectionTypeArguments(values);

		// then
		then(actual).isEmpty();
	}

	@Test
	void mixedImplementationsWithUnrelatedSuperclassDoNotCreateResolver() {
		// given
		List<Animal> animals = Arrays.asList(new Dog(), new Cat());

		// when
		PathResolver<GenericTypeResolver> actual = GenericTypeResolverConverter.fromValue("$.animals", animals);

		// then
		then(actual).isNull();
	}

	@Test
	void mapOfDifferentSubclassKeysIsNotResolved() {
		// given
		Map<Shape, String> shapes = new LinkedHashMap<>();
		shapes.put(new Circle(), "circle");
		shapes.put(new Square(), "square");

		// when
		List<JvmType> actual = GenericTypeResolverConverter.extractMapTypeArguments(shapes);

		// then
		then(actual).isEmpty();
	}

	@Test
	void mapOfSameClassResolvesKeyAndValueTypes() {
		// given
		Map<String, Shape> shapes = new LinkedHashMap<>();
		shapes.put("first", new Circle());
		shapes.put("second", new Circle());

		// when
		List<JvmType> actual = GenericTypeResolverConverter.extractMapTypeArguments(shapes);

		// then
		then(actual).hasSize(2);
		then(actual.get(0).getRawType()).isEqualTo(String.class);
		then(actual.get(1).getRawType()).isEqualTo(Circle.class);
	}

	abstract static class Shape {
	}

	static class Circle extends Shape {
	}

	static class Square extends Shape {
	}

	interface Animal {
	}

	static class Base {
	}

	static class Dog extends Base implements Animal {
	}

	static class Cat extends Base implements Animal {
	}
}
