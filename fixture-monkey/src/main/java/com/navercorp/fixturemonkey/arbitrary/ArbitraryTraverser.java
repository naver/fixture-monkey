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

import com.fasterxml.jackson.annotation.JsonProperty;

import com.navercorp.fixturemonkey.ArbitraryOption;
import com.navercorp.fixturemonkey.generator.AnnotatedArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.AnnotationSource;

public final class ArbitraryTraverser {
	public static final ArbitraryTraverser INSTANCE = new ArbitraryTraverser(ArbitraryOption.DEFAULT_FIXTURE_OPTIONS);
	private final ArbitraryOption arbitraryOption;

	public ArbitraryTraverser(ArbitraryOption arbitraryOption) {
		this.arbitraryOption = arbitraryOption;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public <T> void traverse(
		ArbitraryNode<T> node,
		boolean keyOfMapStructure
	) {
		node.getChildren().clear();
		ArbitraryType<T> currentNodeType = node.getType();
		Class<?> clazz = currentNodeType.getType();
		List<Field> fields = ReflectionUtils.findFields(clazz, this::availableField,
			ReflectionUtils.HierarchyTraversalMode.TOP_DOWN);

		if (isTraversable(currentNodeType)) {
			for (Field field : fields) {
				ArbitraryType arbitraryType = getFixtureType(field);
				double nullInject = arbitraryOption.getNullInject();
				boolean nullable = isNullableField(field);

				ArbitraryNode<?> nextFrame = ArbitraryNode.builder()
					.type(arbitraryType)
					.fieldName(resolveFieldName(field))
					.nullable(nullable)
					.nullInject(nullInject)
					.keyOfMapStructure(keyOfMapStructure)
					.build();

				node.addChildNode(nextFrame);
				traverse(nextFrame, false);
			}
		} else {
			if (arbitraryOption.isDefaultArbitraryType(currentNodeType.getType())) {
				Arbitrary<T> registeredArbitrary = registeredArbitrary(node);
				node.setArbitrary(registeredArbitrary);
			} else if (currentNodeType.isContainer()) {
				if (currentNodeType.isMap() || currentNodeType.isMapEntry()) {
					mapArbitrary(node);
				} else if (currentNodeType.isArray()) {
					arrayArbitrary(node);
				} else {
					containerArbitrary((ArbitraryNode<? extends Collection>)node);
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

	@SuppressWarnings({"rawtypes", "unchecked"})
	public <T> void decompose(
		Object value,
		ArbitraryNode<T> node
	) {
		node.getChildren().clear();
		ArbitraryType<T> currentNodeType = node.getType();
		Class<?> clazz = currentNodeType.getType();
		List<Field> fields = ReflectionUtils.findFields(clazz, this::availableField,
			ReflectionUtils.HierarchyTraversalMode.TOP_DOWN);
		node.setArbitrary((Arbitrary<T>)Arbitraries.just(value));
		if (isTraversable(currentNodeType)) {
			for (Field field : fields) {
				ArbitraryType arbitraryType = getFixtureType(field);
				Object currentValue = extractValue(value, field);

				ArbitraryNode<?> nextFrame = ArbitraryNode.builder()
					.type(arbitraryType)
					.fieldName(resolveFieldName(field))
					.build();

				node.addChildNode(nextFrame);
				decompose(currentValue, nextFrame);
			}
		} else {
			if (currentNodeType.isContainer()) {
				if (value instanceof Collection || value instanceof Iterator) {
					Iterator iterator;

					if (value instanceof Collection) {
						iterator = ((Collection<?>)value).iterator();
					} else {
						iterator = (Iterator)value;
					}
					int index = 0;
					while (iterator.hasNext()) {
						Object nextObject = iterator.next();
						ArbitraryType childType = currentNodeType.getGenericFixtureType(0);
						ArbitraryNode<?> genericFrame = ArbitraryNode.builder()
							.type(childType)
							.fieldName(node.getFieldName())
							.indexOfIterable(index)
							.arbitrary(Arbitraries.just(nextObject))
							.build();
						node.addChildNode(genericFrame);
						decompose(nextObject, genericFrame);
						index++;
					}
				} else if (currentNodeType.isArray()) {
					int length = Array.getLength(value);
					for (int i = 0; i < length; i++) {
						Object nextObject = Array.get(value, i);
						ArbitraryType childType = currentNodeType.getArrayFixtureType();
						ArbitraryNode<?> genericFrame = ArbitraryNode.builder()
							.type(childType)
							.fieldName(node.getFieldName())
							.indexOfIterable(i)
							.arbitrary(Arbitraries.just(nextObject))
							.build();
						node.addChildNode(genericFrame);
						decompose(nextObject, genericFrame);
					}
				}
			}
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

	private <T extends Collection<U>, U> void containerArbitrary(ArbitraryNode<T> currentNode) {
		ArbitraryType<T> clazz = currentNode.getType();
		String fieldName = currentNode.getFieldName();

		ArbitraryType<U> childType = clazz.getGenericFixtureType(0);

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
			traverse(genericFrame, false);
		}
	}

	private <T, U> void arrayArbitrary(ArbitraryNode<T> currentNode) {
		ArbitraryType<T> clazz = currentNode.getType();
		String fieldName = currentNode.getFieldName();

		ArbitraryType<U> childType = clazz.getArrayFixtureType();

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
			traverse(genericFrame, false);
		}
	}

	private <T, K, V> void mapArbitrary(ArbitraryNode<T> currentNode) {
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
			traverse(keyFrame, true);

			ArbitraryNode<V> valueFrame = ArbitraryNode.<V>builder()
				.type(valueType)
				.fieldName(fieldName)
				.indexOfIterable(i)
				.nullable(false)
				.nullInject(0.f)
				.build();

			currentNode.addChildNode(valueFrame);
			traverse(valueFrame, false);
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

	private String resolveFieldName(Field field) {
		JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
		if (jsonProperty == null) {
			return field.getName();
		} else {
			return jsonProperty.value();
		}
	}
}
