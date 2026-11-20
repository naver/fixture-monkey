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

package com.navercorp.objectfarm.api.node;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.ReflectiveJvmType;
import com.navercorp.objectfarm.api.type.Types;

/**
 * Resolves the type a node is built with from the type it is given, by the same rule wherever the node is.
 * <ol>
 *   <li>The interface is resolved by the path-based {@link InterfaceResolver} when one is given. When it resolves
 *   nothing, a container falls back to the default resolver of the {@link JvmNodeContext} and any other type is
 *   kept.</li>
 *   <li>Without a path-based one, a container or a non-abstract type is resolved by the default resolver.
 *   An interface or abstract type that is not a container is kept, so that its implementation is chosen at
 *   assembly time for each sample.</li>
 *   <li>A JDK internal container is resolved to the implementation of its nearest container supertype.</li>
 *   <li>The type arguments are resolved by the path-based {@link GenericTypeResolver} when one is given,
 *   otherwise by the default one.</li>
 * </ol>
 * The annotations of the given type are kept on the resolved type, such as the annotations of the field a container
 * is declared with.
 */
public final class ConcreteTypeResolver {
	private final JvmNodeContext context;

	/**
	 * Creates a resolver using the default resolvers of the given context.
	 *
	 * @param context the context providing the default resolvers and which types are containers
	 */
	public ConcreteTypeResolver(JvmNodeContext context) {
		this.context = context;
	}

	/**
	 * Resolves the type a node of the given type is built with.
	 *
	 * @param type                    the type to resolve
	 * @param pathInterfaceResolver   the interface resolver for the path of the node, or null if none
	 * @param pathGenericTypeResolver the generic type resolver for the path of the node, or null if none
	 * @return the resolved type, or the given type itself when nothing is resolved
	 */
	public JvmType resolve(
		JvmType type,
		@Nullable InterfaceResolver pathInterfaceResolver,
		@Nullable GenericTypeResolver pathGenericTypeResolver
	) {
		Class<?> rawType = type.getRawType();
		boolean isAbstractType =
			Modifier.isInterface(rawType.getModifiers()) || Modifier.isAbstract(rawType.getModifiers());
		boolean isContainerType = context.isContainerType(type);

		JvmType afterInterface = resolveInterfaceType(type, pathInterfaceResolver, isAbstractType, isContainerType);
		if (isInaccessibleContainerType(afterInterface)) {
			afterInterface = resolveByNearestContainerSupertype(afterInterface);
		}
		JvmType resolvedType = resolveGenericType(afterInterface, pathGenericTypeResolver);

		if (resolvedType.equals(type)) {
			return type;
		}
		return mergeAnnotations(type, resolvedType);
	}

	private JvmType resolveInterfaceType(
		JvmType type,
		@Nullable InterfaceResolver pathBasedResolver,
		boolean isAbstractType,
		boolean isContainerType
	) {
		if (pathBasedResolver != null) {
			JvmType resolved = pathBasedResolver.resolve(type);
			if (resolved != null) {
				return resolved;
			}
			return isContainerType ? resolveByDefault(type) : type;
		}
		if (!isAbstractType || isContainerType) {
			return resolveByDefault(type);
		}
		return type;
	}

	private boolean isInaccessibleContainerType(JvmType type) {
		return Types.isJdkInternalType(type.getRawType()) && context.isContainerType(type);
	}

	private JvmType resolveByNearestContainerSupertype(JvmType type) {
		InterfaceResolver defaultResolver = context.getInterfaceResolver();
		if (defaultResolver == null) {
			return type;
		}

		Deque<Class<?>> supertypes = new ArrayDeque<>(getDirectSupertypes(type.getRawType()));
		Set<Class<?>> visited = new HashSet<>();
		while (!supertypes.isEmpty()) {
			Class<?> supertype = supertypes.removeFirst();
			if (!visited.add(supertype)) {
				continue;
			}
			JvmType supertypeJvmType = new ReflectiveJvmType(
				supertype,
				supertype.getTypeParameters().length == type.getTypeVariables().size()
					? type.getTypeVariables()
					: Collections.emptyList(),
				type.getAnnotations()
			);
			if (!context.isContainerType(supertypeJvmType)) {
				continue;
			}
			JvmType resolved = defaultResolver.resolve(supertypeJvmType);
			if (resolved != null && !Types.isJdkInternalType(resolved.getRawType())) {
				return resolved;
			}
			supertypes.addAll(getDirectSupertypes(supertype));
		}
		return type;
	}

	private static List<Class<?>> getDirectSupertypes(Class<?> type) {
		List<Class<?>> supertypes = new ArrayList<>();
		Class<?> superclass = type.getSuperclass();
		if (superclass != null) {
			supertypes.add(superclass);
		}
		Collections.addAll(supertypes, type.getInterfaces());
		return supertypes;
	}

	private JvmType resolveByDefault(JvmType type) {
		InterfaceResolver defaultResolver = context.getInterfaceResolver();
		if (defaultResolver == null) {
			return type;
		}
		JvmType resolved = defaultResolver.resolve(type);
		return resolved != null ? resolved : type;
	}

	private JvmType resolveGenericType(JvmType type, @Nullable GenericTypeResolver pathBasedResolver) {
		if (pathBasedResolver != null) {
			JvmType resolved = pathBasedResolver.resolve(type);
			if (resolved != null) {
				return resolved;
			}
		} else {
			GenericTypeResolver defaultResolver = context.getGenericTypeResolver();
			if (defaultResolver != null) {
				JvmType resolved = defaultResolver.resolve(type);
				if (resolved != null) {
					return resolved;
				}
			}
		}
		return type;
	}

	private static JvmType mergeAnnotations(JvmType originalType, JvmType resolvedType) {
		List<Annotation> originalAnnotations = originalType.getAnnotations();
		if (originalAnnotations.isEmpty()) {
			return resolvedType;
		}

		Set<Class<? extends Annotation>> resolvedAnnotationTypes = resolvedType
			.getAnnotations()
			.stream()
			.map(Annotation::annotationType)
			.collect(Collectors.toSet());

		List<Annotation> merged = new ArrayList<>(resolvedType.getAnnotations());
		for (Annotation annotation : originalAnnotations) {
			if (!resolvedAnnotationTypes.contains(annotation.annotationType())) {
				merged.add(annotation);
			}
		}
		return new ReflectiveJvmType(resolvedType.getRawType(), resolvedType.getTypeVariables(), merged);
	}
}
