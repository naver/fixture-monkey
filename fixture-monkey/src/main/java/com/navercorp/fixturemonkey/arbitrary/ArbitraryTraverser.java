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

package com.navercorp.fixturemonkey.arbitrary;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.constraints.NotEmpty;

import org.junit.platform.commons.util.ReflectionUtils;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.ArbitraryOption;
import com.navercorp.fixturemonkey.generator.AnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.AnnotationSource;
import com.navercorp.fixturemonkey.generator.FieldNameResolver;

public final class ArbitraryTraverser {
	public static final ArbitraryTraverser INSTANCE = new ArbitraryTraverser(ArbitraryOption.DEFAULT_FIXTURE_OPTIONS);
	private final ArbitraryOption arbitraryOption;

	public ArbitraryTraverser(ArbitraryOption arbitraryOption) {
		this.arbitraryOption = arbitraryOption;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public <T> void traverse(
		ArbitraryNode<T> node,
		boolean keyOfMapStructure,
		FieldNameResolver fieldNameResolver
	) {
		node.getChildren().clear();
		T value = node.getValueSupplier().get();
		ArbitraryType<T> currentNodeType = node.getType();
		Class<?> clazz = currentNodeType.getType();

		List<Field> fields = ReflectionUtils.findFields(clazz, this::availableField,
			ReflectionUtils.HierarchyTraversalMode.TOP_DOWN);

		if (isTraversable(currentNodeType)) {
			for (Field field : fields) {
				ArbitraryType arbitraryType = getFixtureType(field);
				double nullInject = arbitraryOption.getNullInject();
				boolean nullable = isNullableField(field);
				Object nextValue;
				if (value != null) {
					nextValue = extractValue(value, field);
					nullable = false;
				} else {
					nextValue = null;
				}

				ArbitraryNode<?> nextFrame = ArbitraryNode.builder()
					.type(arbitraryType)
					.fieldName(fieldNameResolver.resolveFieldName(field))
					.nullable(nullable)
					.nullInject(nullInject)
					.keyOfMapStructure(keyOfMapStructure)
					.valueSupplier(() -> nextValue)
					.build();

				node.addChildNode(nextFrame);
				traverse(nextFrame, false, fieldNameResolver);
			}
		} else {
			if (!currentNodeType.isContainer() && value != null) {
				node.setArbitrary(Arbitraries.just(value));
			} else if (arbitraryOption.isDefaultArbitraryType(currentNodeType.getType())) {
				Arbitrary<T> registeredArbitrary = registeredArbitrary(node);
				node.setArbitrary(registeredArbitrary);
			} else if (currentNodeType.isContainer()) {
				if (currentNodeType.isMap() || currentNodeType.isMapEntry()) {
					if (value != null) {
						node.setArbitrary(Arbitraries.just(value));
						return;
					}
					mapArbitrary(node, fieldNameResolver);
				} else if (currentNodeType.isArray()) {
					arrayArbitrary(node, fieldNameResolver);
				} else {
					containerArbitrary((ArbitraryNode<? extends Collection>)node, fieldNameResolver);
				}
			} else if (clazz.isEnum()) {
				Arbitrary<T> arbitrary = (Arbitrary<T>)Arbitraries.of((Class<Enum>)clazz);
				node.setArbitrary(arbitrary);
			} else {
				node.setArbitrary(Arbitraries.just(null));
			}
			// TODO: noGeneric
		}
	}

	private <T> Object extractValue(T value, Field field) {
		try {
			field.setAccessible(true);
			return field.get(value);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Can not extract value");
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private boolean isNullableField(Field field) {
		ArbitraryType arbitraryType = getFixtureType(field);
		boolean nullable = arbitraryOption.getNullableArbitraryEvaluator().isNullable(field);
		if (arbitraryType.isContainer()) {
			return nullable && arbitraryOption.isNullableContainer();
		} else if (arbitraryType.isPrimitive()) {
			return false;
		} else if (arbitraryType.getAnnotation(NotEmpty.class) != null) {
			return false;
		} else {
			if (!nullable) {
				return false;
			}

			return arbitraryType.getAnnotations().stream()
				.noneMatch(it -> arbitraryOption.isNonNullAnnotation((Annotation)it));
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private ArbitraryType getFixtureType(Field field) {
		List<Annotation> annotations = Arrays.asList(field.getAnnotations());
		return new ArbitraryType(field.getType(), field.getAnnotatedType(), annotations);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private <T> Arbitrary<T> registeredArbitrary(ArbitraryNode<T> currentNode) {
		ArbitraryType type = currentNode.getType();
		AnnotatedType argsType = type.getAnnotatedType();

		Class<T> clazz;
		if (argsType != null) {
			clazz = (Class<T>)argsType.getType();
		} else {
			clazz = type.getType();
		}

		AnnotationSource annotationSource = new AnnotationSource(argsType);

		Map<Class<?>, AnnotatedArbitraryGenerator<?>> annotatedArbitraryMap =
			arbitraryOption.getAnnotatedArbitraryMap();

		return (Arbitrary<T>)Optional.ofNullable(annotatedArbitraryMap.get(clazz))
			.map(arbitraryGenerator -> arbitraryGenerator.generate(annotationSource))
			.orElseThrow(() -> new IllegalArgumentException("Class is not registered " + clazz.getName()));
	}

	@SuppressWarnings("unchecked")
	private <T, U> void containerArbitrary(
		ArbitraryNode<T> currentNode,
		FieldNameResolver fieldNameResolver
	) {
		ArbitraryType<T> clazz = currentNode.getType();
		String fieldName = currentNode.getFieldName();

		ArbitraryType<U> childType = clazz.getGenericFixtureType(0);

		T value = currentNode.getValueSupplier().get();

		if (value != null) {
			Iterator<U> iterator;
			if (value instanceof Collection || value instanceof Iterator) {
				if (value instanceof Collection) {
					iterator = ((Collection<U>)value).iterator();
				} else {
					iterator = (Iterator<U>)value;
				}
				int index = 0;
				while (iterator.hasNext()) {
					U nextObject = iterator.next();
					ArbitraryNode<U> nextNode = ArbitraryNode.<U>builder()
						.type(childType)
						.valueSupplier(
							() -> nextObject
						)
						.fieldName(fieldName)
						.indexOfIterable(index)
						.build();
					currentNode.addChildNode(nextNode);
					traverse(nextNode, false, fieldNameResolver);
					index++;
				}
				return;
			}
		}

		currentNode.initializeElementSize();

		int elementSize = currentNode.getContainerSizeConstraint().getArbitraryElementSize();

		for (int i = 0; i < elementSize; i++) {
			ArbitraryNode<U> genericFrame = ArbitraryNode.<U>builder()
				.type(childType)
				.fieldName(fieldName)
				.indexOfIterable(i)
				.nullable(false)
				.nullInject(0.f)
				.build();

			currentNode.addChildNode(genericFrame);
			traverse(genericFrame, false, fieldNameResolver);
		}
	}

	@SuppressWarnings("unchecked")
	private <T, U> void arrayArbitrary(ArbitraryNode<T> currentNode, FieldNameResolver fieldNameResolver) {
		ArbitraryType<T> clazz = currentNode.getType();
		String fieldName = currentNode.getFieldName();

		ArbitraryType<U> childType = clazz.getArrayFixtureType();

		T value = currentNode.getValueSupplier().get();
		if (value != null) {
			int length = Array.getLength(value);
			for (int i = 0; i < length; i++) {
				U nextValue = (U)Array.get(value, i);
				ArbitraryNode<U> nextNode = ArbitraryNode.<U>builder()
					.type(childType)
					.fieldName(fieldName)
					.indexOfIterable(i)
					.valueSupplier(() -> nextValue)
					.build();
				currentNode.addChildNode(nextNode);
				traverse(nextNode, false, fieldNameResolver);
			}
			return;
		}

		currentNode.initializeElementSize();

		int elementSize = currentNode.getContainerSizeConstraint().getArbitraryElementSize();

		for (int i = 0; i < elementSize; i++) {
			ArbitraryNode<U> genericFrame = ArbitraryNode.<U>builder()
				.type(childType)
				.fieldName(fieldName)
				.indexOfIterable(i)
				.nullable(false)
				.nullInject(0.f)
				.build();

			currentNode.addChildNode(genericFrame);
			traverse(genericFrame, false, fieldNameResolver);
		}
	}

	private <T, K, V> void mapArbitrary(ArbitraryNode<T> currentNode, FieldNameResolver fieldNameResolver) {
		ArbitraryType<T> clazz = currentNode.getType();
		String fieldName = currentNode.getFieldName();

		ArbitraryType<K> keyType = clazz.getGenericFixtureType(0);
		ArbitraryType<V> valueType = clazz.getGenericFixtureType(1);

		currentNode.initializeElementSize();

		int elementSize = currentNode.getContainerSizeConstraint().getArbitraryElementSize();

		if (clazz.isMapEntry()) {
			elementSize = 1;
		}

		for (int i = 0; i < elementSize; i++) {
			ArbitraryNode<K> keyFrame = ArbitraryNode.<K>builder()
				.type(keyType)
				.fieldName(fieldName)
				.indexOfIterable(i)
				.keyOfMapStructure(true)
				.nullable(false)
				.nullInject(0.f)
				.build();

			currentNode.addChildNode(keyFrame);
			traverse(keyFrame, true, fieldNameResolver);

			ArbitraryNode<V> valueFrame = ArbitraryNode.<V>builder()
				.type(valueType)
				.fieldName(fieldName)
				.indexOfIterable(i)
				.nullable(false)
				.nullInject(0.f)
				.build();

			currentNode.addChildNode(valueFrame);
			traverse(valueFrame, false, fieldNameResolver);
		}
	}

	private boolean availableField(Field field) {
		return !Modifier.isStatic(field.getModifiers());
	}

	private boolean isTraversable(ArbitraryType<?> type) {
		Class<?> clazz = type.getType();
		if (clazz.getPackage() == null) {
			return false; // primitive
		}

		return arbitraryOption.isExceptGeneratePackage(clazz)
			&& !arbitraryOption.isDefaultArbitraryType(clazz)
			&& !type.isContainer()
			&& !type.isOptional()
			&& !type.isEnum();
	}
}
