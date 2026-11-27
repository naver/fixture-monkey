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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.GenericTypeResolver;
import com.navercorp.objectfarm.api.tree.PathGenericTypeResolver;
import com.navercorp.objectfarm.api.tree.PathResolver;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.ReflectiveJvmType;
import com.navercorp.objectfarm.api.type.Types;
import com.navercorp.objectfarm.api.type.WildcardRawType;

/**
 * Converts type information to TransformPathResolver for generic type resolution.
 * <p>
 * This converter extracts generic type information from values or explicit type definitions
 * and creates GenericTypeResolver instances for tree transformation.
 * <p>
 * Example:
 * <pre>
 * // Container&lt;String&gt; at $.data resolves T to String
 * List&lt;JvmType&gt; typeArgs = List.of(new ReflectiveJvmType(String.class));
 * TransformPathResolver&lt;GenericTypeResolver&gt; resolver =
 *     GenericTypeResolverConverter.createResolver("$.data", typeArgs);
 * </pre>
 */
public final class GenericTypeResolverConverter {
	private static final JvmType UNINFERRED = new ReflectiveJvmType(Object.class);

	private GenericTypeResolverConverter() {
	}

	/**
	 * Creates a GenericTypeResolver for a specific path with explicit type variables.
	 *
	 * @param pathExpression the path expression where generic types should be resolved
	 * @param typeVariables  the resolved type variables to use
	 * @return a TransformPathResolver for generic type resolution
	 */
	public static PathResolver<GenericTypeResolver> createResolver(
		String pathExpression,
		List<? extends JvmType> typeVariables
	) {
		PathExpression pattern = PathExpression.of(pathExpression);

		GenericTypeResolver genericTypeResolver = jvmType -> new ReflectiveJvmType(
			jvmType.getRawType(),
			selectTypeArguments(jvmType.getTypeVariables(), typeVariables),
			jvmType.getAnnotations()
		);

		return new PathGenericTypeResolver(pattern, genericTypeResolver);
	}

	private static List<? extends JvmType> selectTypeArguments(
		List<? extends JvmType> declared,
		List<? extends JvmType> inferred
	) {
		// Type arguments are matched by position, so their counts must be the same.
		// A container set into an Object, Serializable or type variable property becomes
		// an implementation without type arguments, such as a raw ArrayList, so only inferred ones exist.
		if (declared.size() != inferred.size()) {
			return inferred;
		}
		List<JvmType> merged = new ArrayList<>(inferred.size());
		for (int i = 0; i < inferred.size(); i++) {
			merged.add(selectTypeArgument(declared.get(i), inferred.get(i)));
		}
		return merged;
	}

	private static JvmType selectTypeArgument(JvmType declaredType, JvmType inferredType) {
		if (shouldKeepDeclared(declaredType, inferredType)) {
			return declaredType;
		}
		if (!isJdkTypeOfDeclaredArity(declaredType, inferredType)) {
			return inferredType;
		}
		return new ReflectiveJvmType(
			inferredType.getRawType(),
			selectNestedTypeArguments(declaredType, inferredType),
			inferredType.getAnnotations()
		);
	}

	private static boolean shouldKeepDeclared(JvmType declaredType, JvmType inferredType) {
		Class<?> declaredRaw = declaredType.getRawType();
		Class<?> inferredRaw = inferredType.getRawType();
		if (isUninferred(inferredType)) {
			return true;
		}
		boolean inferredFitsDeclared = declaredRaw == WildcardRawType.class
			|| Types.isAssignable(inferredRaw, declaredRaw);
		if (!inferredFitsDeclared) {
			return true;
		}
		return Types.isJdkInternalType(inferredRaw)
			&& InterfaceResolverConverter.hasDefaultContainerResolver(declaredRaw);
	}

	private static boolean isUninferred(JvmType type) {
		return type.getRawType() == Object.class;
	}

	private static boolean isJdkTypeOfDeclaredArity(JvmType declaredType, JvmType inferredType) {
		Class<?> inferredRaw = inferredType.getRawType();
		int declaredArity = declaredType.getTypeVariables().size();
		return Types.isJavaType(inferredRaw)
			&& declaredArity > 0
			&& inferredRaw.getTypeParameters().length == declaredArity;
	}

	private static List<? extends JvmType> selectNestedTypeArguments(JvmType declaredType, JvmType inferredType) {
		List<? extends JvmType> declaredTypeArguments = declaredType.getTypeVariables();
		List<? extends JvmType> inferredTypeArguments = inferredType.getTypeVariables();
		if (inferredTypeArguments.isEmpty()) {
			return declaredTypeArguments;
		}
		return selectTypeArguments(declaredTypeArguments, inferredTypeArguments);
	}

	/**
	 * Creates a GenericTypeResolver that gives {@code Object} type arguments to a raw generic type,
	 * for a value whose type arguments cannot be inferred, such as a list of elements of different classes.
	 * <p>
	 * A type that has type arguments, such as one declared as {@code List<String>}, is left as it is,
	 * so that its declared element type is kept.
	 *
	 * @param pathExpression the path expression where the value is set
	 * @return a TransformPathResolver for generic type resolution
	 */
	public static PathResolver<GenericTypeResolver> createObjectTypeArgumentsResolver(String pathExpression) {
		GenericTypeResolver genericTypeResolver = jvmType -> {
			int typeParameterCount = jvmType.getRawType().getTypeParameters().length;
			if (!jvmType.getTypeVariables().isEmpty() || typeParameterCount == 0) {
				return null;
			}
			return new ReflectiveJvmType(
				jvmType.getRawType(),
				Collections.nCopies(typeParameterCount, UNINFERRED),
				jvmType.getAnnotations()
			);
		};
		return new PathGenericTypeResolver(PathExpression.of(pathExpression), genericTypeResolver);
	}

	/**
	 * Creates a GenericTypeResolver that resolves to a specific complete type.
	 *
	 * @param pathExpression the path expression where generic types should be resolved
	 * @param resolvedType   the fully resolved type (including generic parameters)
	 * @return a TransformPathResolver for generic type resolution
	 */
	public static PathResolver<GenericTypeResolver> createResolver(
		String pathExpression,
		JvmType resolvedType
	) {
		PathExpression pattern = PathExpression.of(pathExpression);

		GenericTypeResolver genericTypeResolver = jvmType -> {
			if (jvmType.getRawType().equals(resolvedType.getRawType())) {
				return resolvedType;
			}
			return null;
		};

		return new PathGenericTypeResolver(pattern, genericTypeResolver);
	}

	/**
	 * Extracts generic type arguments from a value and creates a resolver.
	 *
	 * @param pathExpression the path expression where the value is set
	 * @param value          the value to extract generic information from
	 * @return a TransformPathResolver for generic type resolution, or null if not applicable
	 */
	@Nullable
	public static PathResolver<GenericTypeResolver> fromValue(
		String pathExpression,
		Object value
	) {
		List<JvmType> typeArguments = extractTypeArgumentsFromValue(value);
		if (typeArguments.isEmpty()) {
			return null;
		}

		return createResolver(pathExpression, typeArguments);
	}

	/**
	 * Extracts type arguments from a value.
	 * <p>
	 * The type arguments of a Collection, Map, {@link Map.Entry} or {@link Optional} are inferred from its contents.
	 * JDK classes without type parameters and enums have none: the type arguments of their supertypes,
	 * such as {@code Comparable<String>} or {@code Enum<E>}, are not theirs.
	 * Other values use the type arguments of their generic superclass or interfaces.
	 *
	 * @param value the value to extract type arguments from
	 * @return list of type arguments, or empty list if not found
	 */
	public static List<JvmType> extractTypeArgumentsFromValue(Object value) {
		if (value instanceof Map) {
			return extractMapTypeArguments((Map<?, ?>)value);
		}

		if (value instanceof Collection) {
			return extractCollectionTypeArguments((Collection<?>)value);
		}

		if (value instanceof Map.Entry) {
			Map.Entry<?, ?> entry = (Map.Entry<?, ?>)value;
			return extractMapTypeArguments(Collections.singletonMap(entry.getKey(), entry.getValue()));
		}

		if (value instanceof Optional) {
			Optional<?> optional = (Optional<?>)value;
			return optional.isPresent()
				? extractCollectionTypeArguments(Collections.singletonList(optional.get()))
				: Collections.emptyList();
		}

		Class<?> valueClass = value.getClass();
		boolean isJdkNonGenericClass = Types.isJavaType(valueClass) && valueClass.getTypeParameters().length == 0;
		if (isJdkNonGenericClass || Enum.class.isAssignableFrom(valueClass)) {
			return Collections.emptyList();
		}

		return extractCustomGenericTypeArguments(valueClass);
	}

	/**
	 * Extracts type arguments from a Collection by examining its elements.
	 * <p>
	 * When every non-null element has the same class, that class (with its own type arguments) is the element type.
	 * When the elements are instances of different classes, no type argument is inferred and the declared
	 * element type is kept so that each element can resolve to its own runtime type.
	 * When the elements have the same class but different type arguments, such as lists of different element types,
	 * the type arguments they disagree on are {@code Object}, and declared type arguments are kept over them.
	 *
	 * @param collection the collection to examine
	 * @return list containing the element type, or empty list if it cannot be inferred
	 */
	public static List<JvmType> extractCollectionTypeArguments(Collection<?> collection) {
		JvmType elementType = inferElementType(collection);
		if (elementType == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(elementType);
	}

	/**
	 * Extracts type arguments from a Map by examining its entries.
	 * <p>
	 * Key and value types are inferred the same way as collection element types.
	 *
	 * @param map the map to examine
	 * @return list containing key and value types, or empty list if either cannot be inferred
	 */
	public static List<JvmType> extractMapTypeArguments(Map<?, ?> map) {
		JvmType keyType = inferElementType(map.keySet());
		JvmType valueType = inferElementType(map.values());
		if (keyType == null || valueType == null) {
			return Collections.emptyList();
		}

		List<JvmType> typeArguments = new ArrayList<>();
		typeArguments.add(keyType);
		typeArguments.add(valueType);
		return typeArguments;
	}

	@Nullable
	private static JvmType inferElementType(Collection<?> elements) {
		Optional<?> representative = elements.stream()
			.filter(Objects::nonNull)
			.findFirst();
		if (!representative.isPresent()) {
			return null;
		}

		Class<?> elementClass = representative.get().getClass();
		boolean homogeneous = elements.stream()
			.allMatch(element -> element == null || element.getClass() == elementClass);
		if (!homogeneous) {
			return null;
		}

		List<JvmType> elementTypeArgs = extractTypeArgumentsFromValue(representative.get());
		if (hasTypeArgumentsOfItsOwn(elementClass)) {
			for (Object element : elements) {
				if (element != null && element != representative.get()) {
					elementTypeArgs = mergeTypeArguments(elementTypeArgs, extractTypeArgumentsFromValue(element));
				}
			}
		}
		return elementTypeArgs.isEmpty()
			? new ReflectiveJvmType(elementClass)
			: new ReflectiveJvmType(elementClass, elementTypeArgs, Collections.emptyList());
	}

	private static boolean hasTypeArgumentsOfItsOwn(Class<?> valueClass) {
		return Collection.class.isAssignableFrom(valueClass)
			|| Map.class.isAssignableFrom(valueClass)
			|| Map.Entry.class.isAssignableFrom(valueClass)
			|| Optional.class.isAssignableFrom(valueClass);
	}

	private static List<JvmType> mergeTypeArguments(List<JvmType> left, List<JvmType> right) {
		if (left.isEmpty() || left.equals(right)) {
			return right.isEmpty() ? left : right;
		}
		if (right.isEmpty() || left.size() != right.size()) {
			return left;
		}
		List<JvmType> merged = new ArrayList<>(left.size());
		for (int i = 0; i < left.size(); i++) {
			merged.add(mergeType(left.get(i), right.get(i)));
		}
		return merged;
	}

	private static JvmType mergeType(JvmType left, JvmType right) {
		if (left.equals(right)) {
			return left;
		}
		if (left.getRawType() != right.getRawType()) {
			return UNINFERRED;
		}
		List<JvmType> typeArguments = mergeTypeArguments(
			new ArrayList<>(left.getTypeVariables()),
			new ArrayList<>(right.getTypeVariables())
		);
		return typeArguments.isEmpty()
			? new ReflectiveJvmType(left.getRawType())
			: new ReflectiveJvmType(left.getRawType(), typeArguments, Collections.emptyList());
	}

	/**
	 * Extracts type arguments from a class's generic superclass or interfaces.
	 *
	 * @param clazz the class to examine
	 * @return list of type arguments, or empty list if not found
	 */
	public static List<JvmType> extractCustomGenericTypeArguments(Class<?> clazz) {
		Type genericSuperclass = clazz.getGenericSuperclass();
		if (genericSuperclass instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType)genericSuperclass;
			return extractFromParameterizedType(parameterizedType);
		}

		for (Type genericInterface : clazz.getGenericInterfaces()) {
			if (genericInterface instanceof ParameterizedType) {
				ParameterizedType parameterizedType = (ParameterizedType)genericInterface;
				return extractFromParameterizedType(parameterizedType);
			}
		}

		return Collections.emptyList();
	}

	/**
	 * Extracts type arguments from a ParameterizedType.
	 *
	 * @param parameterizedType the parameterized type to extract from
	 * @return list of type arguments
	 */
	public static List<JvmType> extractFromParameterizedType(ParameterizedType parameterizedType) {
		Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
		List<JvmType> result = new ArrayList<>();

		for (Type typeArg : actualTypeArguments) {
			if (typeArg instanceof TypeVariable) {
				continue;
			}
			try {
				Class<?> rawType = Types.getActualType(typeArg);
				if (typeArg instanceof ParameterizedType) {
					List<JvmType> nestedArgs = extractFromParameterizedType((ParameterizedType)typeArg);
					result.add(new ReflectiveJvmType(rawType, nestedArgs, Collections.emptyList()));
				} else {
					result.add(new ReflectiveJvmType(rawType));
				}
			} catch (UnsupportedOperationException e) {
				continue;
			}
		}

		return result;
	}
}
