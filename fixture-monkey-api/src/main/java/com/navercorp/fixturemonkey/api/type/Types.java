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

package com.navercorp.fixturemonkey.api.type;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public class Types {
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

	public static Class<?> getActualType(Type type) {
		if (type.getClass() == Class.class) {
			return (Class<?>)type;
		}

		if (WildcardType.class.isAssignableFrom(type.getClass())) {
			WildcardType wildcardType = (WildcardType)type;
			Type upperBound = wildcardType.getUpperBounds()[0];
			return getActualType(upperBound);
		}

		if (TypeVariable.class.isAssignableFrom(type.getClass())) {
			GenericDeclaration genericDeclaration = ((TypeVariable<?>)type).getGenericDeclaration();
			if (genericDeclaration.getClass() == Class.class) {
				return (Class<?>)genericDeclaration;
			} else {
				// Method? Constructor?
				throw new UnsupportedOperationException(
					"Unsupported TypeVariable's generationDeclaration type. type: " + genericDeclaration.getClass()
				);
			}
		}

		if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
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

		if (AnnotatedWildcardType.class.isAssignableFrom(annotatedType.getClass())) {
			AnnotatedWildcardType wildcardType = (AnnotatedWildcardType)type;
			return getGenericsTypes(wildcardType.getAnnotatedUpperBounds()[0]);
		}

		if (TypeVariable.class.isAssignableFrom(type.getClass())) {
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

		if (AnnotatedParameterizedType.class.isAssignableFrom(annotatedType.getClass())) {
			AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType)annotatedType;
			AnnotatedType[] rawTypes = parameterizedType.getAnnotatedActualTypeArguments();
			if (rawTypes == null) {
				return Collections.emptyList();
			}

			return Arrays.asList(rawTypes);
		}

		throw new UnsupportedOperationException(
			"Unsupported Type to get genericsTypes. annotatedType: " + annotatedType
		);
	}

	public static AnnotatedType resolveWithTypeReferenceGenerics(AnnotatedType ownerType, Field field) {
		if (!(ownerType instanceof AnnotatedParameterizedType)) {
			AnnotatedType fieldAnnotatedType = field.getAnnotatedType();
			if (TypeVariable.class.isAssignableFrom(fieldAnnotatedType.getType().getClass())) {
				return new AnnotatedType() {
					@Override
					public Type getType() {
						return Object.class;
					}

					@Override
					public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
						return fieldAnnotatedType.getAnnotation(annotationClass);
					}

					@Override
					public Annotation[] getAnnotations() {
						return fieldAnnotatedType.getAnnotations();
					}

					@Override
					public Annotation[] getDeclaredAnnotations() {
						return fieldAnnotatedType.getDeclaredAnnotations();
					}
				};
			} else {
				return fieldAnnotatedType;
			}
		}

		AnnotatedParameterizedType ownerAnnotatedParameterizedType = (AnnotatedParameterizedType)ownerType;
		AnnotatedType[] ownerGenericsTypes = ownerAnnotatedParameterizedType.getAnnotatedActualTypeArguments();
		if (ownerGenericsTypes == null || ownerGenericsTypes.length == 0) {
			return field.getAnnotatedType();
		}

		ParameterizedType parameterizedType = (ParameterizedType)ownerAnnotatedParameterizedType.getType();
		Class<?> ownerActualType = Types.getActualType(parameterizedType.getRawType());
		List<Type> ownerTypeVariableParameters = Arrays.asList(ownerActualType.getTypeParameters());

		Type fieldGenericsType = field.getGenericType();
		if (TypeVariable.class.isAssignableFrom(fieldGenericsType.getClass())) {
			int index = ownerTypeVariableParameters.indexOf(fieldGenericsType);
			return ownerGenericsTypes[index];
		}

		if (!(fieldGenericsType instanceof ParameterizedType)) {
			return field.getAnnotatedType();
		}

		AnnotatedParameterizedType fieldParameterizedType = (AnnotatedParameterizedType)field.getAnnotatedType();
		AnnotatedType[] fieldGenericsTypes = fieldParameterizedType.getAnnotatedActualTypeArguments();
		if (fieldGenericsTypes == null || fieldGenericsTypes.length == 0) {
			return fieldParameterizedType;
		}

		AnnotatedType[] resolvedGenericsTypes = new AnnotatedType[fieldGenericsTypes.length];
		Type[] resolvedTypes = new Type[fieldGenericsTypes.length];
		for (int i = 0; i < fieldGenericsTypes.length; i++) {
			AnnotatedType generics = fieldGenericsTypes[i];
			if (generics instanceof AnnotatedParameterizedType || generics.getType().getClass() == Class.class) {
				resolvedGenericsTypes[i] = generics;
				resolvedTypes[i] = generics.getType();
				continue;
			}

			if (TypeVariable.class.isAssignableFrom(generics.getType().getClass())) {
				TypeVariable<?> typeVariable = (TypeVariable<?>)generics.getType();
				int index = ownerTypeVariableParameters.indexOf(typeVariable);
				generics = ownerGenericsTypes[index];
				resolvedGenericsTypes[i] = generics;
				resolvedTypes[i] = generics.getType();
			}
		}

		ParameterizedType type = (ParameterizedType)fieldParameterizedType.getType();
		Type resolveType = new ParameterizedType() {
			@Override
			public Type[] getActualTypeArguments() {
				return resolvedTypes;
			}

			@Override
			public Type getRawType() {
				return type.getRawType();
			}

			@Override
			public Type getOwnerType() {
				return type.getOwnerType();
			}
		};

		return new AnnotatedParameterizedType() {
			@Override
			public AnnotatedType[] getAnnotatedActualTypeArguments() {
				return resolvedGenericsTypes;
			}

			// For compatibility with JDK >= 9. A breaking change in the JDK :-(
			// @Override
			@SuppressWarnings("Since15")
			public AnnotatedType getAnnotatedOwnerType() {
				// TODO: Return annotatedType.getAnnotatedOwnerType() as soon as Java >= 9 is being used
				return null;
			}

			@Override
			public Type getType() {
				return resolveType;
			}

			@Override
			public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
				return fieldParameterizedType.getAnnotation(annotationClass);
			}

			@Override
			public Annotation[] getAnnotations() {
				return fieldParameterizedType.getAnnotations();
			}

			@Override
			public Annotation[] getDeclaredAnnotations() {
				return fieldParameterizedType.getDeclaredAnnotations();
			}
		};
	}

	public static AnnotatedType resolveWithTypeReferenceGenerics(
		AnnotatedType ownerType,
		PropertyDescriptor propertyDescriptor
	) {
		if (!(ownerType instanceof AnnotatedParameterizedType)) {
			AnnotatedType propertyAnnotatedType = TypeCache.getAnnotatedType(propertyDescriptor);
			if (TypeVariable.class.isAssignableFrom(propertyAnnotatedType.getType().getClass())) {
				return new AnnotatedType() {
					@Override
					public Type getType() {
						return Object.class;
					}

					@Override
					public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
						return propertyAnnotatedType.getAnnotation(annotationClass);
					}

					@Override
					public Annotation[] getAnnotations() {
						return propertyAnnotatedType.getAnnotations();
					}

					@Override
					public Annotation[] getDeclaredAnnotations() {
						return propertyAnnotatedType.getDeclaredAnnotations();
					}
				};
			} else {
				return propertyAnnotatedType;
			}
		}

		AnnotatedParameterizedType ownerAnnotatedParameterizedType = (AnnotatedParameterizedType)ownerType;
		AnnotatedType[] ownerGenericsTypes = ownerAnnotatedParameterizedType.getAnnotatedActualTypeArguments();
		if (ownerGenericsTypes == null || ownerGenericsTypes.length == 0) {
			return TypeCache.getAnnotatedType(propertyDescriptor);
		}

		ParameterizedType parameterizedType = (ParameterizedType)ownerAnnotatedParameterizedType.getType();
		Class<?> ownerActualType = Types.getActualType(parameterizedType.getRawType());
		List<Type> ownerTypeVariableParameters = Arrays.asList(ownerActualType.getTypeParameters());

		Method readMethod = propertyDescriptor.getReadMethod();
		Type methodGenericsType = readMethod.getGenericReturnType();
		if (TypeVariable.class.isAssignableFrom(methodGenericsType.getClass())) {
			int index = ownerTypeVariableParameters.indexOf(methodGenericsType);
			return ownerGenericsTypes[index];
		}

		if (!(methodGenericsType instanceof ParameterizedType)) {
			return TypeCache.getAnnotatedType(propertyDescriptor);
		}

		AnnotatedParameterizedType propertyParameterizedType =
			(AnnotatedParameterizedType)TypeCache.getAnnotatedType(propertyDescriptor);
		AnnotatedType[] propertyGenericsTypes = propertyParameterizedType.getAnnotatedActualTypeArguments();
		if (propertyGenericsTypes == null || propertyGenericsTypes.length == 0) {
			return propertyParameterizedType;
		}

		AnnotatedType[] resolvedGenericsTypes = new AnnotatedType[propertyGenericsTypes.length];
		Type[] resolvedTypes = new Type[propertyGenericsTypes.length];
		for (int i = 0; i < propertyGenericsTypes.length; i++) {
			AnnotatedType generics = propertyGenericsTypes[i];
			if (generics instanceof AnnotatedParameterizedType || generics.getType().getClass() == Class.class) {
				resolvedGenericsTypes[i] = generics;
				resolvedTypes[i] = generics.getType();
				continue;
			}

			if (TypeVariable.class.isAssignableFrom(generics.getType().getClass())) {
				TypeVariable<?> typeVariable = (TypeVariable<?>)generics.getType();
				int index = ownerTypeVariableParameters.indexOf(typeVariable);
				generics = ownerGenericsTypes[index];
				resolvedGenericsTypes[i] = generics;
				resolvedTypes[i] = generics.getType();
			}
		}

		ParameterizedType type = (ParameterizedType)propertyParameterizedType.getType();
		Type resolveType = new ParameterizedType() {
			@Override
			public Type[] getActualTypeArguments() {
				return resolvedTypes;
			}

			@Override
			public Type getRawType() {
				return type.getRawType();
			}

			@Override
			public Type getOwnerType() {
				return type.getOwnerType();
			}
		};

		return new AnnotatedParameterizedType() {
			@Override
			public AnnotatedType[] getAnnotatedActualTypeArguments() {
				return resolvedGenericsTypes;
			}

			// For compatibility with JDK >= 9. A breaking change in the JDK :-(
			// @Override
			@SuppressWarnings("Since15")
			public AnnotatedType getAnnotatedOwnerType() {
				// TODO: Return annotatedType.getAnnotatedOwnerType() as soon as Java >= 9 is being used
				return null;
			}

			@Override
			public Type getType() {
				return resolveType;
			}

			@Override
			public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
				return propertyParameterizedType.getAnnotation(annotationClass);
			}

			@Override
			public Annotation[] getAnnotations() {
				return propertyParameterizedType.getAnnotations();
			}

			@Override
			public Annotation[] getDeclaredAnnotations() {
				return propertyParameterizedType.getDeclaredAnnotations();
			}
		};
	}

	public static AnnotatedType getArrayComponentAnnotatedType(AnnotatedType annotatedType) {
		if (!(annotatedType instanceof AnnotatedArrayType)) {
			throw new IllegalArgumentException(
				"given type is not Array type, annotatedType: " + annotatedType
			);
		}
		AnnotatedArrayType annotatedArrayType = (AnnotatedArrayType)annotatedType;

		return annotatedArrayType.getAnnotatedGenericComponentType();
	}

	public static Class<?> getArrayComponentType(AnnotatedType annotatedType) {
		return getActualType(getArrayComponentAnnotatedType(annotatedType));
	}

	public static AnnotatedType generateAnnotatedTypeWithoutAnnotation(Type type) {
		return new AnnotatedType() {
			@Override
			public Type getType() {
				return type;
			}

			@Override
			public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
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

	public static class UnidentifiableType {
		private UnidentifiableType() {
		}
	}

	public static Class<?> primitiveToWrapper(final Class<?> cls) {
		Class<?> convertedClass = cls;
		if (cls != null && cls.isPrimitive()) {
			convertedClass = primitiveWrapperMap.get(cls);
		}
		return convertedClass;
	}

	public static Class<?> wrapperToPrimitive(final Class<?> cls) {
		return wrapperPrimitiveMap.get(cls);
	}

	public static boolean isAssignable(Class<?> cls, Class<?> toClass) {
		return isAssignable(cls, toClass, true);
	}

	public static boolean isAssignable(Class<?> cls, Class<?> toClass, boolean autoboxing) {
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
}
