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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
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

	public <T> void traverse(ArbitraryNode<T> node, boolean keyOfMapStructure, FieldNameResolver fieldNameResolver) {
		LazyValue<T> value = node.getValue();
		if (value != null) {
			value.clear();
		}
		doTraverse(node, keyOfMapStructure, fieldNameResolver);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private <T> void doTraverse(
		ArbitraryNode<T> node,
		boolean keyOfMapStructure,
		FieldNameResolver fieldNameResolver
	) {
		node.getChildren().clear();
		LazyValue<T> nowValue = node.getValue();
		ArbitraryType<T> nowNodeType = node.getType();
		Class<?> clazz = nowNodeType.getType();

		if (isTraversable(nowNodeType)) {
			List<Field> fields = getFields(clazz);
			for (Field field : fields) {
				ArbitraryType arbitraryType = getArbitraryType(field);
				double nullInject = arbitraryOption.getNullInject();
				boolean defaultNotNull = arbitraryOption.isDefaultNotNull();
				boolean nullable = isNullableField(field, defaultNotNull);
				LazyValue<?> nextValue = getNextValue(nowValue, field);
				nullable = nextValue == null && nullable;

				ArbitraryNode<?> nextNode = ArbitraryNode.builder()
					.type(arbitraryType)
					.fieldName(fieldNameResolver.resolveFieldName(field))
					.nullable(nullable)
					.nullInject(nullInject)
					.keyOfMapStructure(keyOfMapStructure)
					.value(nextValue)
					.build();

				node.addChildNode(nextNode);
				doTraverse(nextNode, false, fieldNameResolver);
			}
		} else if (nowNodeType.isContainer()) {
			if (nowNodeType.isMap() || nowNodeType.isMapEntry()) {
				mapArbitrary(node, fieldNameResolver);
			} else if (nowNodeType.isArray()) {
				arrayArbitrary(node, fieldNameResolver);
			} else if (nowNodeType.isOptional()) {
				traverseContainer(node, fieldNameResolver, OptionalArbitraryNodeGenerator.INSTANCE);
			} else {
				traverseContainer(node, fieldNameResolver, DefaultContainerArbitraryNodeGenerator.INSTANCE);
			}
			// TODO: noGeneric
		} else {
			if (nowValue != null) {
				node.setArbitrary(Arbitraries.just(nowValue.get()));
			} else if (arbitraryOption.isDefaultArbitraryType(nowNodeType.getType())) {
				Arbitrary<T> registeredArbitrary = registeredArbitrary(node);
				node.setArbitrary(registeredArbitrary);
			} else if (nowNodeType.isEnum()) {
				Arbitrary<T> arbitrary = (Arbitrary<T>)Arbitraries.of((Class<Enum>)clazz);
				node.setArbitrary(arbitrary);
			} else if (nowNodeType.isInterface() || nowNodeType.isAbstract()) {
				InterfaceSupplier interfaceSupplier =
					arbitraryOption.getInterfaceSupplierOrDefault(nowNodeType.getType());
				node.setArbitrary(Arbitraries.just((T)interfaceSupplier.get()));
			} else {
				node.setArbitrary(Arbitraries.just(null));
			}
		}
	}

	@Nullable
	private <T> LazyValue<?> getNextValue(LazyValue<T> currentValue, Field field) {
		if (currentValue == null) {
			return null;
		}
		return currentValue.isEmpty() ? null : new LazyValue<>(extractValue(currentValue.get(), field));
	}

	private List<Field> getFields(Class<?> clazz) {
		if (clazz == null) {
			return Collections.emptyList();
		}

		return ReflectionUtils.findFields(
			clazz,
			this::availableField,
			ReflectionUtils.HierarchyTraversalMode.TOP_DOWN
		);
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
	private boolean isNullableField(Field field, boolean defaultNotNull) {
		ArbitraryType arbitraryType = getArbitraryType(field);
		boolean nullable = arbitraryOption.getNullableArbitraryEvaluator().isNullable(field);
		if (arbitraryType.isContainer()) {
			return nullable && arbitraryOption.isNullableContainer();
		} else if (arbitraryType.isPrimitive()) {
			return false;
		} else if (arbitraryType.getAnnotation(NotEmpty.class) != null
			|| (field.getType() == String.class && arbitraryType.getAnnotation(NotBlank.class) != null)
		) {
			return false;
		} else {
			if (arbitraryType.getAnnotation(Nullable.class) != null) {
				return true;
			}
			if (!nullable) {
				return false;
			}

			boolean hasNotNullAnnotations = arbitraryType.getAnnotations().stream()
				.noneMatch(it -> arbitraryOption.isNonNullAnnotation((Annotation)it));

			if (!hasNotNullAnnotations) {
				return false;
			}

			return !defaultNotNull;
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private ArbitraryType getArbitraryType(Field field) {
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
	private <T, U> void arrayArbitrary(ArbitraryNode<T> currentNode, FieldNameResolver fieldNameResolver) {
		ArbitraryType<T> clazz = currentNode.getType();
		String fieldName = currentNode.getFieldName();

		ArbitraryType<U> childType = clazz.getArrayFixtureType();

		LazyValue<T> lazyValue = currentNode.getValue();
		ContainerSizeConstraint containerSizeConstraint = currentNode.getContainerSizeConstraint();
		int elementSize = Integer.MAX_VALUE;
		int currentIndex = 0;

		if (lazyValue != null) {
			T value = lazyValue.get();
			if (value == null) {
				currentNode.setArbitrary(Arbitraries.just(null));
				return;
			}
			int length = Array.getLength(value);

			if (containerSizeConstraint != null) {
				elementSize = containerSizeConstraint.getArbitraryElementSize();
			}

			for (currentIndex = 0; currentIndex < length && currentIndex < elementSize; currentIndex++) {
				U nextValue = (U)Array.get(value, currentIndex);
				ArbitraryNode<U> nextNode = ArbitraryNode.<U>builder()
					.type(childType)
					.fieldName(fieldName)
					.indexOfIterable(currentIndex)
					.value(nextValue)
					.build();
				currentNode.addChildNode(nextNode);
				doTraverse(nextNode, false, fieldNameResolver);
			}

			if (containerSizeConstraint == null) {
				currentNode.setContainerSizeConstraint(new ContainerSizeConstraint(length, length));
				return;
			}
		}

		currentNode.initializeElementSize();
		if (elementSize == Integer.MAX_VALUE) {
			elementSize = containerSizeConstraint.getArbitraryElementSize();
		}

		for (int i = currentIndex; i < elementSize; i++) {
			ArbitraryNode<U> genericFrame = ArbitraryNode.<U>builder()
				.type(childType)
				.fieldName(fieldName)
				.indexOfIterable(i)
				.nullable(false)
				.nullInject(0.f)
				.build();

			currentNode.addChildNode(genericFrame);
			doTraverse(genericFrame, false, fieldNameResolver);
		}
	}

	private <T, K, V> void mapArbitrary(ArbitraryNode<T> currentNode, FieldNameResolver fieldNameResolver) {
		LazyValue<T> lazyValue = currentNode.getValue();
		if (lazyValue != null) {
			currentNode.setArbitrary(Arbitraries.just(lazyValue.get()));
			return;
		}
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
			doTraverse(keyFrame, true, fieldNameResolver);

			ArbitraryNode<V> valueFrame = ArbitraryNode.<V>builder()
				.type(valueType)
				.fieldName(fieldName)
				.indexOfIterable(i)
				.nullable(false)
				.nullInject(0.f)
				.build();

			currentNode.addChildNode(valueFrame);
			doTraverse(valueFrame, false, fieldNameResolver);
		}
	}

	private <T, U> void traverseContainer(
		ArbitraryNode<T> currentNode,
		FieldNameResolver fieldNameResolver,
		ContainerArbitraryNodeGenerator containerArbitraryNodeGenerator
	) {
		List<ArbitraryNode<U>> nodes = containerArbitraryNodeGenerator.generate(currentNode, fieldNameResolver);
		for (ArbitraryNode<U> node : nodes) {
			currentNode.addChildNode(node);
			// TODO: map일 때 key 처리
			doTraverse(node, false, fieldNameResolver);
		}
	}

	private boolean availableField(Field field) {
		return !Modifier.isStatic(field.getModifiers());
	}

	private boolean isTraversable(ArbitraryType<?> type) {
		Class<?> clazz = type.getType();
		if (clazz == null) {
			return false;
		}

		return arbitraryOption.isExceptGeneratePackage(clazz)
			&& !arbitraryOption.isDefaultArbitraryType(clazz)
			&& !type.isContainer()
			&& !type.isOptional()
			&& !type.isEnum()
			&& !type.isInterface()
			&& !type.isAbstract();
	}
}
