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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.GenericTypeResolver;
import com.navercorp.objectfarm.api.tree.PathGenericTypeResolver;
import com.navercorp.objectfarm.api.tree.PathResolver;
import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.Types;

/**
 * Converts type information to TransformPathResolver for generic type resolution.
 * <p>
 * This converter extracts generic type information from values or explicit type definitions
 * and creates GenericTypeResolver instances for tree transformation.
 * <p>
 * Example:
 * <pre>
 * // Container&lt;String&gt; at $.data resolves T to String
 * List&lt;JvmType&gt; typeArgs = List.of(new JavaType(String.class));
 * TransformPathResolver&lt;GenericTypeResolver&gt; resolver =
 *     GenericTypeResolverConverter.createResolver("$.data", typeArgs);
 * </pre>
 */
public final class GenericTypeResolverConverter {
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

		GenericTypeResolver genericTypeResolver = jvmType -> new JavaType(
			jvmType.getRawType(),
			typeVariables,
			jvmType.getAnnotations()
		);

		return new PathGenericTypeResolver(pattern, genericTypeResolver);
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
	 * Extracts type arguments from a value's generic superclass or interfaces.
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

		return extractCustomGenericTypeArguments(value.getClass());
	}

	/**
	 * Extracts type arguments from a Collection by examining its first element.
	 *
	 * @param collection the collection to examine
	 * @return list containing the element type, or empty list if collection is empty
	 */
	public static List<JvmType> extractCollectionTypeArguments(Collection<?> collection) {
		if (collection.isEmpty()) {
			return Collections.emptyList();
		}

		Iterator<?> iterator = collection.iterator();
		Object firstElement = iterator.next();
		if (firstElement == null) {
			return Collections.emptyList();
		}

		List<JvmType> elementTypeArgs = extractTypeArgumentsFromValue(firstElement);
		JvmType elementType = elementTypeArgs.isEmpty()
			? new JavaType(firstElement.getClass())
			: new JavaType(firstElement.getClass(), elementTypeArgs, Collections.emptyList());
		return Collections.singletonList(elementType);
	}

	/**
	 * Extracts type arguments from a Map by examining its first entry.
	 *
	 * @param map the map to examine
	 * @return list containing key and value types, or empty list if map is empty
	 */
	public static List<JvmType> extractMapTypeArguments(Map<?, ?> map) {
		if (map.isEmpty()) {
			return Collections.emptyList();
		}

		Map.Entry<?, ?> firstEntry = map.entrySet().iterator().next();
		Object key = firstEntry.getKey();
		Object entryValue = firstEntry.getValue();

		if (key == null || entryValue == null) {
			return Collections.emptyList();
		}

		List<JvmType> typeArguments = new ArrayList<>();
		List<JvmType> keyTypeArgs = extractTypeArgumentsFromValue(key);
		typeArguments.add(keyTypeArgs.isEmpty()
			? new JavaType(key.getClass())
			: new JavaType(key.getClass(), keyTypeArgs, Collections.emptyList()));
		List<JvmType> valueTypeArgs = extractTypeArgumentsFromValue(entryValue);
		typeArguments.add(valueTypeArgs.isEmpty()
			? new JavaType(entryValue.getClass())
			: new JavaType(entryValue.getClass(), valueTypeArgs, Collections.emptyList()));
		return typeArguments;
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
					result.add(new JavaType(rawType, nestedArgs, Collections.emptyList()));
				} else {
					result.add(new JavaType(rawType));
				}
			} catch (UnsupportedOperationException e) {
				continue;
			}
		}

		return result;
	}
}
