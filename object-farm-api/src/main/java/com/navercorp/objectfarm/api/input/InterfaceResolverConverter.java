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

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.InterfaceResolver;
import com.navercorp.objectfarm.api.tree.PathInterfaceResolver;
import com.navercorp.objectfarm.api.tree.PathResolver;
import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Converts value information to TransformPathResolver for interface resolution.
 * <p>
 * This converter analyzes values to extract interface resolution information.
 * When a value is set at a path that would be an interface type, the actual type of the value
 * is used to resolve the interface.
 * <p>
 * Example:
 * <pre>
 * // Resolve Animal interface to Dog type at $.animal
 * TransformPathResolver&lt;InterfaceResolver&gt; resolver =
 *     InterfaceResolverConverter.fromValue("$.animal", new Dog());
 * </pre>
 */
public final class InterfaceResolverConverter {
	private InterfaceResolverConverter() {
	}

	/**
	 * Creates an InterfaceResolver for a specific path and concrete type.
	 *
	 * @param pathExpression the path expression where the interface is resolved
	 * @param concreteType   the concrete type to resolve to
	 * @return a TransformPathResolver for interface resolution
	 */
	public static PathResolver<InterfaceResolver> createResolver(
		String pathExpression,
		Class<?> concreteType
	) {
		PathExpression pattern = PathExpression.of(pathExpression);

		InterfaceResolver interfaceResolver = interfaceType -> {
			if (!interfaceType.getRawType().isAssignableFrom(concreteType)) {
				return null;
			}

			// Non-instantiable JDK types (e.g., Arrays$ArrayList, Collections$SingletonList)
			// should be skipped only when a default resolver can handle the interface type.
			// Collection/Map subtypes have default implementations (ArrayList, HashMap),
			// but other interfaces (e.g., Iterable) do not.
			if (!isInstantiable(concreteType) && hasDefaultContainerResolver(interfaceType.getRawType())) {
				return null;
			}

			// Preserve type variables from the original interface type
			// e.g., List<String> → ArrayList<String> (not raw ArrayList)
			return new JavaType(concreteType, interfaceType.getTypeVariables(), interfaceType.getAnnotations());
		};

		return new PathInterfaceResolver(pattern, interfaceResolver);
	}

	/**
	 * Creates an InterfaceResolver for a specific path and resolved JvmType.
	 *
	 * @param pathExpression the path expression where the interface is resolved
	 * @param resolvedType   the resolved JvmType (with generic information if available)
	 * @return a TransformPathResolver for interface resolution
	 */
	public static PathResolver<InterfaceResolver> createResolver(
		String pathExpression,
		JvmType resolvedType
	) {
		PathExpression pattern = PathExpression.of(pathExpression);

		InterfaceResolver interfaceResolver = interfaceType -> {
			if (interfaceType.getRawType().isAssignableFrom(resolvedType.getRawType())) {
				return resolvedType;
			}
			return null;
		};

		return new PathInterfaceResolver(pattern, interfaceResolver);
	}

	/**
	 * Extracts interface resolution information from a value and creates a resolver.
	 *
	 * @param pathExpression the path expression where the value is set
	 * @param value          the value being set (may be null)
	 * @return a TransformPathResolver for interface resolution, or null if value is null
	 */
	@Nullable
	public static PathResolver<InterfaceResolver> fromValue(
		String pathExpression,
		@Nullable Object value
	) {
		if (value == null) {
			return null;
		}

		return createResolver(pathExpression, value.getClass());
	}

	/**
	 * Extracts interface resolution information from a value and its generic type information.
	 *
	 * @param pathExpression the path expression where the value is set
	 * @param value          the value being set
	 * @param typeVariables  the type variables to preserve (e.g., for List&lt;String&gt;)
	 * @return a TransformPathResolver for interface resolution, or null if value is null
	 */
	@Nullable
	public static PathResolver<InterfaceResolver> fromValueWithGenerics(
		String pathExpression,
		@Nullable Object value,
		List<? extends JvmType> typeVariables
	) {
		if (value == null) {
			return null;
		}

		JvmType resolvedType = new JavaType(
			value.getClass(),
			typeVariables,
			Collections.emptyList()
		);

		return createResolver(pathExpression, resolvedType);
	}

	private static boolean hasDefaultContainerResolver(Class<?> interfaceType) {
		return Iterable.class.isAssignableFrom(interfaceType)
			|| Collection.class.isAssignableFrom(interfaceType)
			|| Map.class.isAssignableFrom(interfaceType);
	}

	private static boolean isInstantiable(Class<?> type) {
		if (Modifier.isPublic(type.getModifiers())) {
			return true;
		}

		// Non-public JDK internal types (e.g., Arrays$ArrayList, Collections$SingletonList)
		// cannot be instantiated by introspectors — skip them so the default resolver is used.
		String name = type.getName();
		return !name.startsWith("java.") && !name.startsWith("javax.") && !name.startsWith("sun.");
	}
}
