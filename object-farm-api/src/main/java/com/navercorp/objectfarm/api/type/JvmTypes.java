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

package com.navercorp.objectfarm.api.type;

import java.lang.annotation.Annotation;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;

public abstract class JvmTypes {
	public static JvmType resolveJvmType(JvmType parentType, Type type, List<Annotation> annotations) {
		if (!Types.isGenericType(type)) {
			return new JavaType(Types.getActualType(type));
		}

		if (type instanceof TypeVariable) {
			return resolveTypeVariable(parentType, (TypeVariable<?>)type);
		}

		List<JvmType> typeArguments = resolveTypeArguments(parentType, type);

		return new JavaType(
			Types.getActualType(type),
			typeArguments,
			annotations
		);
	}

	/**
	 * Resolves a TypeVariable to its actual JvmType using the parent type's type variables.
	 */
	private static JvmType resolveTypeVariable(JvmType parentType, TypeVariable<?> typeVariable) {
		List<? extends JvmType> parentTypeVariables = parentType.getTypeVariables();
		if (parentTypeVariables.isEmpty()) {
			return new JavaType(Object.class);
		}
		return parentTypeVariables.get(0);
	}

	/**
	 * Resolves type arguments preserving nested generic type information.
	 */
	private static List<JvmType> resolveTypeArguments(JvmType parentType, Type genericType) {
		if (genericType instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType)genericType;
			Type[] typeArgs = parameterizedType.getActualTypeArguments();

			List<JvmType> result = new ArrayList<>();
			for (Type typeArg : typeArgs) {
				result.add(resolveType(parentType, typeArg));
			}
			return Collections.unmodifiableList(result);
		}

		if (genericType instanceof GenericArrayType) {
			Type componentType = ((GenericArrayType)genericType).getGenericComponentType();
			if (componentType instanceof ParameterizedType) {
				ParameterizedType parameterizedType = (ParameterizedType)componentType;

				List<? extends JvmType> parentTypeVariables = parentType.getTypeVariables();
				Type[] typeArgs = parameterizedType.getActualTypeArguments();

				List<JvmType> result = new ArrayList<>();
				for (int i = 0; i < typeArgs.length; i++) {
					JvmType parentTypeVar = i < parentTypeVariables.size()
						? parentTypeVariables.get(i)
						: null;
					result.add(resolveErasedType(typeArgs[i], parentTypeVar));
				}
				return Collections.unmodifiableList(result);
			}
		}

		if (genericType instanceof TypeVariable) {
			return Collections.singletonList(
				resolveTypeVariable(parentType, (TypeVariable<?>)genericType)
			);
		}

		throw new IllegalArgumentException("Unsupported generic type: " + genericType);
	}

	/**
	 * Resolves a Type to JvmType, recursively handling nested generic types.
	 */
	private static JvmType resolveType(JvmType parentType, Type type) {
		if (type instanceof TypeVariable) {
			return resolveTypeVariable(parentType, (TypeVariable<?>)type);
		}

		if (type instanceof ParameterizedType) {
			Class<?> rawType = Types.getActualType(type);
			List<JvmType> typeArguments = resolveTypeArguments(parentType, type);
			return new JavaType(rawType, typeArguments, Collections.emptyList());
		}

		return new JavaType(Types.getActualType(type));
	}

	/**
	 * Resolves an erased type with a specific parent type variable.
	 */
	private static JvmType resolveErasedType(Type type, @Nullable JvmType parentTypeVar) {
		if (type instanceof TypeVariable) {
			if (parentTypeVar == null) {
				return new JavaType(Object.class);
			}
			return parentTypeVar;
		}

		if (type instanceof ParameterizedType) {
			Class<?> rawType = Types.getActualType(type);
			JvmType dummyParent = new JavaType(Object.class);
			List<JvmType> typeArguments = resolveTypeArguments(dummyParent, type);
			return new JavaType(rawType, typeArguments, Collections.emptyList());
		}

		return new JavaType(Types.getActualType(type));
	}
}
