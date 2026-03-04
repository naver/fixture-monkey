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
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

public abstract class Types {
	private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<>();

	static {
		primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
		primitiveWrapperMap.put(Byte.TYPE, Byte.class);
		primitiveWrapperMap.put(Character.TYPE, Character.class);
		primitiveWrapperMap.put(Short.TYPE, Short.class);
		primitiveWrapperMap.put(Integer.TYPE, Integer.class);
		primitiveWrapperMap.put(Long.TYPE, Long.class);
		primitiveWrapperMap.put(Double.TYPE, Double.class);
		primitiveWrapperMap.put(Float.TYPE, Float.class);
	}

	private static final Map<Class<?>, Class<?>> wrapperPrimitiveMap = new HashMap<>();

	static {
		primitiveWrapperMap.forEach((primitiveClass, wrapperClass) -> {
			if (!primitiveClass.equals(wrapperClass)) {
				wrapperPrimitiveMap.put(wrapperClass, primitiveClass);
			}
		});
	}

	public static Class<?> getActualType(AnnotatedType annotatedType) {
		return getActualType(annotatedType.getType());
	}

	/**
	 * Resolves a wildcard annotated type to its upper bound.
	 * For `? extends SimpleObject`, returns the annotated type for `SimpleObject`.
	 * For non-wildcard types, returns the original type unchanged.
	 *
	 * @param annotatedType the annotated type to resolve
	 * @return the resolved annotated type (upper bound for wildcards, original for others)
	 */
	public static AnnotatedType resolveWildcardType(AnnotatedType annotatedType) {
		if (isAssignable(annotatedType.getClass(), AnnotatedWildcardType.class)) {
			AnnotatedWildcardType wildcardType = (AnnotatedWildcardType)annotatedType;
			return wildcardType.getAnnotatedUpperBounds()[0];
		}
		return annotatedType;
	}

	/**
	 * Checks if an AnnotatedType contains wildcards in its type arguments.
	 * For example, `List<? extends SimpleObject>` returns true,
	 * while `List<SimpleObject>` returns false.
	 *
	 * @param annotatedType the annotated type to check
	 * @return true if the type contains wildcards in its type arguments
	 */
	public static boolean containsWildcardTypeArguments(AnnotatedType annotatedType) {
		if (isAssignable(annotatedType.getClass(), AnnotatedWildcardType.class)) {
			return true;
		}

		if (isAssignable(annotatedType.getClass(), AnnotatedParameterizedType.class)) {
			AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType)annotatedType;
			AnnotatedType[] typeArgs = parameterizedType.getAnnotatedActualTypeArguments();
			if (typeArgs != null) {
				for (AnnotatedType typeArg : typeArgs) {
					if (containsWildcardTypeArguments(typeArg)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public static Class<?> getActualType(Type type) {
		if (type.getClass() == Class.class) {
			return (Class<?>)type;
		}

		if (isAssignable(type.getClass(), GenericArrayType.class)) {
			GenericArrayType genericArrayType = (GenericArrayType)type;
			return Array.newInstance(getActualType(genericArrayType.getGenericComponentType()), 0).getClass();
		}

		if (isAssignable(type.getClass(), WildcardType.class)) {
			WildcardType wildcardType = (WildcardType)type;
			Type upperBound = wildcardType.getUpperBounds()[0];
			if (upperBound == Object.class) {
				return WildcardRawType.class;
			}
			return getActualType(upperBound);
		}

		if (isAssignable(type.getClass(), TypeVariable.class)) {
			GenericDeclaration genericDeclaration = ((TypeVariable<?>)type).getGenericDeclaration();
			if (genericDeclaration.getClass() == Class.class) {
				return Object.class;
			} else {
				// Method? Constructor?
				throw new UnsupportedOperationException(
					"Unsupported TypeVariable's generationDeclaration type. type: " + genericDeclaration.getClass()
				);
			}
		}

		if (isAssignable(type.getClass(), ParameterizedType.class)) {
			ParameterizedType parameterizedType = (ParameterizedType)type;
			Type rawType = parameterizedType.getRawType();
			return getActualType(rawType);
		}

		throw new UnsupportedOperationException(
			"Unsupported Type to get actualType. type: " + type.getClass()
		);
	}

	public static List<AnnotatedType> getGenericsTypes(AnnotatedType annotatedType) {
		Type type = annotatedType.getType();
		if (type.getClass() == Class.class) {
			return Collections.emptyList();
		}

		if (isAssignable(annotatedType.getClass(), AnnotatedWildcardType.class)) {
			AnnotatedWildcardType wildcardType = (AnnotatedWildcardType)annotatedType;
			return getGenericsTypes(wildcardType.getAnnotatedUpperBounds()[0]);
		}

		if (isAssignable(type.getClass(), TypeVariable.class)) {
			GenericDeclaration genericDeclaration = ((TypeVariable<?>)type).getGenericDeclaration();
			if (genericDeclaration.getClass() == Class.class) {
				return Collections.emptyList();
			} else {
				// Method? Constructor?
				throw new UnsupportedOperationException(
					"Unsupported TypeVariable's generationDeclaration type. type: " + genericDeclaration.getClass()
				);
			}
		}

		if (isAssignable(annotatedType.getClass(), AnnotatedParameterizedType.class)) {
			AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType)annotatedType;
			AnnotatedType[] rawTypes = parameterizedType.getAnnotatedActualTypeArguments();
			if (rawTypes == null) {
				return Collections.emptyList();
			}

			return Arrays.asList(rawTypes);
		}

		if (isAssignable(type.getClass(), ParameterizedType.class)) {
			Type[] actualTypeArguments = ((ParameterizedType)type).getActualTypeArguments();

			return Arrays.stream(actualTypeArguments)
				.map(Types::generateAnnotatedTypeWithoutAnnotation)
				.collect(Collectors.toList());
		}

		if (isAssignable(type.getClass(), WildcardType.class)) {
			return Collections.singletonList(
				Types.generateAnnotatedTypeWithoutAnnotation(WildcardRawType.class)
			);
		}

		if (isAssignable(type.getClass(), GenericArrayType.class)) {
			Type genericComponentType = ((GenericArrayType)type).getGenericComponentType();
			return getGenericsTypes(Types.generateAnnotatedTypeWithoutAnnotation(genericComponentType));
		}

		throw new UnsupportedOperationException(
			"Unsupported Type to get genericsTypes. annotatedType: " + annotatedType
		);
	}

	public static @Nullable Class<?> primitiveToWrapper(final @Nullable Class<?> cls) {
		Class<?> convertedClass = cls;
		if (cls != null && cls.isPrimitive()) {
			convertedClass = primitiveWrapperMap.get(cls);
		}
		return convertedClass;
	}

	public static @Nullable Class<?> wrapperToPrimitive(final Class<?> cls) {
		return wrapperPrimitiveMap.get(cls);
	}

	/**
	 * It is same as {@code toClass.isAssignableFrom(cls)}.
	 */
	public static boolean isAssignable(Class<?> cls, Class<?> toClass) {
		return isAssignable(cls, toClass, true);
	}

	public static boolean isAssignable(@Nullable Class<?> cls, @Nullable Class<?> toClass, boolean autoboxing) {
		if (toClass == null) {
			return false;
		}
		// have to check for null, as isAssignableFrom doesn't
		if (cls == null) {
			return !toClass.isPrimitive();
		}
		// autoboxing:
		if (autoboxing) {
			if (cls.isPrimitive() && !toClass.isPrimitive()) {
				cls = primitiveToWrapper(cls);
				if (cls == null) {
					return false;
				}
			}
			if (toClass.isPrimitive() && !cls.isPrimitive()) {
				cls = wrapperToPrimitive(cls);
				if (cls == null) {
					return false;
				}
			}
		}
		if (cls.equals(toClass)) {
			return true;
		}
		if (cls.isPrimitive()) {
			if (!toClass.isPrimitive()) {
				return false;
			}
			if (Integer.TYPE.equals(cls)) {
				return Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
			}
			if (Long.TYPE.equals(cls)) {
				return Float.TYPE.equals(toClass) || Double.TYPE.equals(toClass);
			}
			if (Boolean.TYPE.equals(cls)) {
				return false;
			}
			if (Double.TYPE.equals(cls)) {
				return false;
			}
			if (Float.TYPE.equals(cls)) {
				return Double.TYPE.equals(toClass);
			}
			if (Character.TYPE.equals(cls) || Short.TYPE.equals(cls)) {
				return Integer.TYPE.equals(toClass) || Long.TYPE.equals(toClass) || Float.TYPE.equals(toClass)
					|| Double.TYPE.equals(toClass);
			}
			if (Byte.TYPE.equals(cls)) {
				return Short.TYPE.equals(toClass) || Integer.TYPE.equals(toClass) || Long.TYPE.equals(toClass)
					|| Float.TYPE.equals(toClass)
					|| Double.TYPE.equals(toClass);
			}
			// should never get here
			return false;
		}
		return toClass.isAssignableFrom(cls);
	}

	public static AnnotatedType generateAnnotatedTypeWithoutAnnotation(Type type) {
		return new AnnotatedType() {
			@Override
			public Type getType() {
				return type;
			}

			@Override
			public @Nullable <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
				return null;
			}

			@Override
			public Annotation[] getAnnotations() {
				return new Annotation[0];
			}

			@Override
			public Annotation[] getDeclaredAnnotations() {
				return new Annotation[0];
			}
		};
	}

	public static boolean isGenericType(Type type) {
		return type instanceof ParameterizedType
			|| type instanceof GenericArrayType
			|| type instanceof TypeVariable;
	}

	public static ObjectTypeReference<?> toTypeReference(AnnotatedType annotatedType) {
		return new ObjectTypeReference<Object>() {
			@Override
			public AnnotatedType getAnnotatedType() {
				return annotatedType;
			}
		};
	}

	public static boolean isJavaType(Class<?> type) {
		return type.isPrimitive() || isJavaPackage(type);
	}

	private static boolean isJavaPackage(Class<?> type) {
		return type.getPackage() != null
			&& (type.getPackage().getName().startsWith("java") || type.getPackage().getName().startsWith("sun"));
	}

	/**
	 * Checks if the class is a terminal type that doesn't need interface resolution.
	 * Terminal types include:
	 * <ul>
	 *   <li>String</li>
	 *   <li>Final classes with Object as direct superclass</li>
	 * </ul>
	 * <p>
	 * Note: Primitive wrappers (Integer, Long, etc.) are NOT terminal types because
	 * they extend Number, not Object, and thus need interface resolution.
	 *
	 * @param clazz the class to check
	 * @return true if the class is a terminal type
	 */
	public static boolean isTerminalType(Class<?> clazz) {
		if (clazz == String.class) {
			return true;
		}

		// Final classes with Object as direct superclass don't need resolution
		if (Modifier.isFinal(clazz.getModifiers()) && clazz.getSuperclass() == Object.class) {
			return true;
		}

		return false;
	}

	/**
	 * Checks if the class is a JDK value type that is always treated as a leaf node
	 * and never expanded into a node tree. These types don't need interface resolution
	 * even though they may extend non-Object superclasses (e.g., Long extends Number).
	 * <p>
	 * This includes:
	 * <ul>
	 *   <li>Primitive wrappers (Boolean, Byte, Short, Integer, Long, Float, Double, Character)</li>
	 *   <li>Primitive types</li>
	 *   <li>Enums</li>
	 * </ul>
	 *
	 * @param clazz the class to check
	 * @return true if the class is a JDK value type
	 */
	public static boolean isJdkValueType(Class<?> clazz) {
		if (clazz.isPrimitive() || clazz.isEnum()) {
			return true;
		}

		// Primitive wrappers — these all extend Number or Object but are leaf types
		return clazz == Boolean.class
			|| clazz == Byte.class
			|| clazz == Short.class
			|| clazz == Integer.class
			|| clazz == Long.class
			|| clazz == Float.class
			|| clazz == Double.class
			|| clazz == Character.class;
	}
}
