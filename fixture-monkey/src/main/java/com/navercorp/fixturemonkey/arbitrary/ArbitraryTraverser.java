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

import static com.navercorp.fixturemonkey.TypeSupports.extractFields;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.ArbitraryOption;
import com.navercorp.fixturemonkey.api.property.FieldProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.generator.AnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.AnnotationSource;
import com.navercorp.fixturemonkey.generator.FieldNameResolver;

public final class ArbitraryTraverser {
	public static final ArbitraryTraverser INSTANCE = new ArbitraryTraverser(ArbitraryOption.DEFAULT_ARBITRARY_OPTIONS);
	private final ArbitraryOption arbitraryOption;

	public ArbitraryTraverser(ArbitraryOption arbitraryOption) {
		this.arbitraryOption = arbitraryOption;
	}

	public <T> void traverse(
		ArbitraryTree<T> tree,
		boolean keyOfMapStructure,
		PropertyNameResolver propertyNameResolver
	) {
		this.traverse(tree.getHead(), keyOfMapStructure, propertyNameResolver);
	}

	public <T> void traverse(
		ArbitraryNode<T> node,
		boolean keyOfMapStructure,
		PropertyNameResolver propertyNameResolver
	) {
		LazyValue<T> value = node.getValue();
		if (value != null) {
			value.clear();
		}
		doTraverse(node, keyOfMapStructure, true, propertyNameResolver);
	}

	/**
	 * Deprecated Use traverse(ArbitraryTree, boolean, FieldNameResolver) instead.
	 */
	@Deprecated
	public <T> void traverse(ArbitraryTree<T> tree, boolean keyOfMapStructure, FieldNameResolver fieldNameResolver) {
		this.traverse(tree.getHead(), keyOfMapStructure, fieldNameResolver);
	}

	/**
	 * Deprecated Use traverse(ArbitraryNode, boolean, FieldNameResolver) instead.
	 */
	@Deprecated
	public <T> void traverse(ArbitraryNode<T> node, boolean keyOfMapStructure, FieldNameResolver fieldNameResolver) {
		LazyValue<T> value = node.getValue();
		if (value != null) {
			value.clear();
		}
		doTraverse(node, keyOfMapStructure, true, new PropertyNameResolverAdapter(fieldNameResolver));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private <T> void doTraverse(
		ArbitraryNode<T> node,
		boolean keyOfMapStructure,
		boolean active,
		PropertyNameResolver propertyNameResolver
	) {
		node.getChildren().clear();
		initializeDefaultArbitrary(node);
		LazyValue<T> nowValue = node.getValue();
		ArbitraryType<T> nowNodeType = node.getType();
		Class<?> clazz = nowNodeType.getType();
		ContainerArbitraryNodeGenerator containerArbitraryNodeGenerator =
			arbitraryOption.getContainerArbitraryNodeGenerator(nowNodeType.getType());

		if (isTraversable(nowNodeType)) {
			List<Field> fields = extractFields(clazz);
			for (Field field : fields) {
				Property property = new FieldProperty(field);
				ArbitraryType arbitraryType = new ArbitraryType(
					property.getType(),
					property.getAnnotatedType(),
					property.getAnnotations()
				);
				double nullInject = arbitraryOption.getNullInject();
				boolean defaultNotNull = arbitraryOption.isDefaultNotNull();
				boolean nullable = isNullableField(arbitraryType, field, defaultNotNull);
				LazyValue<?> nextValue = getNextValue(nowValue, property);
				nullable = nextValue == null && nullable;
				boolean nextActive = (nextValue == null || !nextValue.isEmpty()) && active;
				if (node.isDecomposedAsNull()) {
					node.setArbitrary(Arbitraries.just(null));
				}

				ArbitraryNode<?> nextNode = ArbitraryNode.builder()
					.type(arbitraryType)
					.propertyName(propertyNameResolver.resolve(property))
					.nullable(nullable)
					.nullInject(nullInject)
					.keyOfMapStructure(keyOfMapStructure)
					.value(nextValue)
					.active(nextActive)
					.build();

				node.addChildNode(nextNode);
				doTraverse(nextNode, false, active, propertyNameResolver);
			}
		} else if (nowNodeType.isContainer() || containerArbitraryNodeGenerator != null) {
			if (containerArbitraryNodeGenerator != null) {
				traverseContainer(node, active, propertyNameResolver, containerArbitraryNodeGenerator);
			} else if (nowNodeType.isMap() || nowNodeType.isMapEntry()) {
				traverseContainer(node, active, propertyNameResolver, MapArbitraryNodeGenerator.INSTANCE);
			} else if (nowNodeType.isArray()) {
				traverseContainer(node, active, propertyNameResolver, ArrayArbitraryNodeGenerator.INSTANCE);
			} else if (nowNodeType.isOptional()) {
				traverseContainer(node, active, propertyNameResolver, OptionalArbitraryNodeGenerator.INSTANCE);
			} else {
				traverseContainer(
					node, active, propertyNameResolver, DefaultContainerArbitraryNodeGenerator.INSTANCE
				);
			}
		} else {
			if (nowValue != null) {
				node.setManipulated(true);
				node.setArbitrary(Arbitraries.just(nowValue.get()));
			} else if (arbitraryOption.isDefaultArbitraryType(nowNodeType.getType())
				&& arbitraryOption.isGeneratableClass(clazz)
			) {
				Arbitrary<T> registeredArbitrary = registeredArbitrary(node);
				node.setArbitrary(registeredArbitrary);
			} else if (nowNodeType.isEnum()) {
				Arbitrary<T> arbitrary = (Arbitrary<T>)Arbitraries.of((Class<Enum>)clazz);
				node.setArbitrary(arbitrary);
			} else if (nowNodeType.isInterface() || nowNodeType.isAbstract()) {
				InterfaceSupplier interfaceSupplier =
					arbitraryOption.getInterfaceSupplierOrDefault(nowNodeType.getType());
				node.setArbitrary(Arbitraries.just((T)interfaceSupplier.get(nowNodeType.getType())));
			} else {
				node.setArbitrary(Arbitraries.just(null));
			}
		}
	}

	private <T> void traverseContainer(
		ArbitraryNode<T> currentNode,
		boolean active,
		PropertyNameResolver propertyNameResolver,
		ContainerArbitraryNodeGenerator containerArbitraryNodeGenerator
	) {
		List<ArbitraryNode<?>> nodes = containerArbitraryNodeGenerator.generate(currentNode);
		for (ArbitraryNode<?> node : nodes) {
			currentNode.addChildNode(node);
			doTraverse(node, node.isKeyOfMapStructure(), active, propertyNameResolver);
		}
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

		AnnotationSource annotationSource = new AnnotationSource(type.getAnnotations());

		Map<Class<?>, AnnotatedArbitraryGenerator<?>> annotatedArbitraryMap =
			arbitraryOption.getAnnotatedArbitraryMap();

		return (Arbitrary<T>)Optional.ofNullable(annotatedArbitraryMap.get(clazz))
			.map(arbitraryGenerator -> arbitraryGenerator.generate(annotationSource))
			.orElseThrow(() -> new IllegalArgumentException("Class is not registered " + clazz.getName()));
	}

	@Nullable
	private <T> LazyValue<?> getNextValue(LazyValue<T> currentValue, Property property) {
		if (currentValue == null) {
			return null;
		}
		return currentValue.isEmpty()
			? new LazyValue<>((Object)null)
			: new LazyValue<>(property.getValue(currentValue.get()));
	}

	@SuppressWarnings("unchecked")
	private <T> void initializeDefaultArbitrary(ArbitraryNode<T> node) {
		Class<?> clazz = node.getType().getType();
		ArbitraryBuilder<?> defaultArbitraryBuilder = arbitraryOption.getDefaultArbitraryBuilder(clazz);

		if (defaultArbitraryBuilder != null && !node.isHead() && node.getValue() == null) {
			node.setValue(() -> (T)defaultArbitraryBuilder.sample());
			node.setManipulated(true); // fixed value would not inject as null
		}
	}

	private boolean isNullableField(ArbitraryType<?> arbitraryType, Field field, boolean defaultNotNull) {
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

	private boolean isTraversable(ArbitraryType<?> type) {
		Class<?> clazz = type.getType();
		if (clazz == null) {
			return false;
		}

		return arbitraryOption.isExceptGeneratablePackage(clazz)
			&& arbitraryOption.isGeneratableClass(clazz)
			&& !arbitraryOption.isDefaultArbitraryType(clazz)
			&& arbitraryOption.getContainerArbitraryNodeGenerator(clazz) == null
			&& !type.isContainer()
			&& !type.isOptional()
			&& !type.isEnum()
			&& !type.isInterface()
			&& !type.isAbstract();
	}

	private static class PropertyNameResolverAdapter implements FieldNameResolver, PropertyNameResolver {
		private final FieldNameResolver fieldNameResolver;

		PropertyNameResolverAdapter(FieldNameResolver fieldNameResolver) {
			this.fieldNameResolver = fieldNameResolver;
		}

		@Override
		public String resolveFieldName(Field field) {
			return this.fieldNameResolver.resolveFieldName(field);
		}

		@Override
		public String resolve(Property property) {
			// 아직까지는 FieldProperty 만 존재한다.
			FieldProperty fieldProperty = (FieldProperty)property;
			return this.fieldNameResolver.resolveFieldName(fieldProperty.getField());
		}
	}
}
