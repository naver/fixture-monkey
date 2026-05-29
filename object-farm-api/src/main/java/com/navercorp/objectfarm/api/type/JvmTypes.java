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
			return new ReflectiveJvmType(Types.getActualType(type), Collections.emptyList(), annotations);
		}

		if (type instanceof TypeVariable) {
			JvmType resolved = resolveTypeVariable(parentType, (TypeVariable<?>)type);
			if (annotations.isEmpty()) {
				return resolved;
			}
			List<Annotation> merged = new ArrayList<>(resolved.getAnnotations());
			for (Annotation annotation : annotations) {
				if (!merged.contains(annotation)) {
					merged.add(annotation);
				}
			}
			return new ReflectiveJvmType(
				resolved.getRawType(),
				resolved.getTypeVariables(),
				merged,
				resolved.getNullable()
			);
		}

		List<JvmType> typeArguments = resolveTypeArguments(parentType, type);

		return new ReflectiveJvmType(
			Types.getActualType(type),
			typeArguments,
			annotations
		);
	}

	/**
	 * Resolves a TypeVariable to its actual JvmType using the parent type's type variables.
	 * <p>
	 * The TypeVariable is matched by its declaring class' type parameters. If the immediate
	 * parent has no type variables (e.g., a concrete class extending a generic superclass),
	 * walks up the superclass chain.
	 */
	private static JvmType resolveTypeVariable(JvmType parentType, TypeVariable<?> typeVariable) {
		Class<?> declaringClass = null;
		java.lang.reflect.GenericDeclaration declaration = typeVariable.getGenericDeclaration();
		if (declaration instanceof Class<?>) {
			declaringClass = (Class<?>)declaration;
		}

		List<? extends JvmType> parentTypeVariables = parentType.getTypeVariables();
		Class<?> parentRawType = parentType.getRawType();

		// If the parent matches the declaring class directly, look up by index.
		if (declaringClass != null && declaringClass == parentRawType && !parentTypeVariables.isEmpty()) {
			TypeVariable<?>[] declaredVars = declaringClass.getTypeParameters();
			for (int i = 0; i < declaredVars.length && i < parentTypeVariables.size(); i++) {
				if (declaredVars[i].getName().equals(typeVariable.getName())) {
					return parentTypeVariables.get(i);
				}
			}
		}

		// Walk up the superclass/interface chain to find a parameterized super that
		// declares the TypeVariable and resolves it via the concrete subtype.
		if (declaringClass != null && parentRawType != null) {
			JvmType resolved = resolveTypeVariableViaSuperType(parentRawType, declaringClass, typeVariable);
			if (resolved != null) {
				return resolved;
			}
		}

		// Fallback: return the first parent type variable if any (legacy behavior).
		if (!parentTypeVariables.isEmpty()) {
			return parentTypeVariables.get(0);
		}
		return new ReflectiveJvmType(Object.class);
	}

	@Nullable
	private static JvmType resolveTypeVariableViaSuperType(
		Class<?> startType,
		Class<?> declaringClass,
		TypeVariable<?> typeVariable
	) {
		// Walk superclass first, then interfaces.
		Type genericSuper = startType.getGenericSuperclass();
		Class<?> rawSuper = startType.getSuperclass();
		if (genericSuper instanceof ParameterizedType && rawSuper != null) {
			JvmType result = matchTypeVariableInParameterizedSuper(
				(ParameterizedType)genericSuper, rawSuper, declaringClass, typeVariable
			);
			if (result != null) {
				return result;
			}
		}
		if (rawSuper != null && rawSuper != Object.class) {
			JvmType resolved = resolveTypeVariableViaSuperType(rawSuper, declaringClass, typeVariable);
			if (resolved != null) {
				return resolved;
			}
		}
		Type[] genericInterfaces = startType.getGenericInterfaces();
		Class<?>[] rawInterfaces = startType.getInterfaces();
		for (int i = 0; i < genericInterfaces.length; i++) {
			if (genericInterfaces[i] instanceof ParameterizedType) {
				JvmType result = matchTypeVariableInParameterizedSuper(
					(ParameterizedType)genericInterfaces[i],
					rawInterfaces[i],
					declaringClass,
					typeVariable
				);
				if (result != null) {
					return result;
				}
			}
			JvmType resolved = resolveTypeVariableViaSuperType(rawInterfaces[i], declaringClass, typeVariable);
			if (resolved != null) {
				return resolved;
			}
		}
		return null;
	}

	@Nullable
	private static JvmType matchTypeVariableInParameterizedSuper(
		ParameterizedType parameterizedSuper,
		Class<?> rawSuper,
		Class<?> declaringClass,
		TypeVariable<?> typeVariable
	) {
		if (rawSuper != declaringClass) {
			return null;
		}
		TypeVariable<?>[] declaredVars = declaringClass.getTypeParameters();
		Type[] actualArgs = parameterizedSuper.getActualTypeArguments();
		for (int i = 0; i < declaredVars.length && i < actualArgs.length; i++) {
			if (declaredVars[i].getName().equals(typeVariable.getName())) {
				if (actualArgs[i] instanceof Class<?>) {
					return new ReflectiveJvmType((Class<?>)actualArgs[i]);
				}
				return new ReflectiveJvmType(Types.getActualType(actualArgs[i]));
			}
		}
		return null;
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
			return new ReflectiveJvmType(rawType, typeArguments, Collections.emptyList());
		}

		return new ReflectiveJvmType(Types.getActualType(type));
	}

	/**
	 * Resolves an erased type with a specific parent type variable.
	 */
	private static JvmType resolveErasedType(Type type, @Nullable JvmType parentTypeVar) {
		if (type instanceof TypeVariable) {
			if (parentTypeVar == null) {
				return new ReflectiveJvmType(Object.class);
			}
			return parentTypeVar;
		}

		if (type instanceof ParameterizedType) {
			Class<?> rawType = Types.getActualType(type);
			JvmType dummyParent = new ReflectiveJvmType(Object.class);
			List<JvmType> typeArguments = resolveTypeArguments(dummyParent, type);
			return new ReflectiveJvmType(rawType, typeArguments, Collections.emptyList());
		}

		return new ReflectiveJvmType(Types.getActualType(type));
	}
}
