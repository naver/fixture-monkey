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

import static com.navercorp.fixturemonkey.api.type.AnnotatedTypes.newAnnotatedTypeVariable;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.ObjectTypeReference;

@API(since = "0.4.0", status = Status.INTERNAL)
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

	public static Class<?> getActualType(Type type) {
		if (type.getClass() == Class.class) {
			return (Class<?>)type;
		}

		if (GenericArrayType.class.isAssignableFrom(type.getClass())) {
			GenericArrayType genericArrayType = (GenericArrayType)type;
			return getActualType(genericArrayType.getGenericComponentType());
		}

		if (WildcardType.class.isAssignableFrom(type.getClass())) {
			WildcardType wildcardType = (WildcardType)type;
			Type upperBound = wildcardType.getUpperBounds()[0];
			if (upperBound == Object.class) {
				return GeneratingWildcardType.class;
			}
			return getActualType(upperBound);
		}

		if (TypeVariable.class.isAssignableFrom(type.getClass())) {
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
			AnnotatedWildcardType wildcardType = (AnnotatedWildcardType)annotatedType;
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

		if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
			Type[] actualTypeArguments = ((ParameterizedType)type).getActualTypeArguments();

			return Arrays.stream(actualTypeArguments)
				.map(Types::generateAnnotatedTypeWithoutAnnotation)
				.collect(Collectors.toList());
		}

		if (WildcardType.class.isAssignableFrom(type.getClass())) {
			return Collections.singletonList(
				Types.generateAnnotatedTypeWithoutAnnotation(GeneratingWildcardType.class)
			);
		}

		if (GenericArrayType.class.isAssignableFrom(type.getClass())) {
			Type genericComponentType = ((GenericArrayType)type).getGenericComponentType();
			return getGenericsTypes(Types.generateAnnotatedTypeWithoutAnnotation(genericComponentType));
		}

		throw new UnsupportedOperationException(
			"Unsupported Type to get genericsTypes. annotatedType: " + annotatedType
		);
	}

	public static AnnotatedType resolveWithTypeReferenceGenerics(
		AnnotatedType parentAnnotatedType,
		AnnotatedType currentAnnotatedType
	) {
		Type genericType = currentAnnotatedType.getType();
		if (!(genericType instanceof TypeVariable
			|| genericType instanceof GenericArrayType
			|| genericType instanceof AnnotatedParameterizedType
			|| genericType instanceof ParameterizedType)) {
			return currentAnnotatedType; // If mo generics
		}

		AnnotatedType ownerTypeGenerics = resolvesParentTypeGenerics(parentAnnotatedType, currentAnnotatedType);
		if (ownerTypeGenerics != null) {
			return ownerTypeGenerics;
		}

		Class<?> actualOwnerType = Types.getActualType(parentAnnotatedType.getType());
		AnnotatedType annotatedSuperClassType = actualOwnerType.getAnnotatedSuperclass();
		if (annotatedSuperClassType != null
			&& ParameterizedType.class.isAssignableFrom(annotatedSuperClassType.getType().getClass())) {
			return resolvesParentTypeGenerics(
				Types.getActualType(parentAnnotatedType.getType()).getAnnotatedSuperclass(),
				currentAnnotatedType
			);
		}

		List<AnnotatedType> genericsTypes = Types.getGenericsTypes(parentAnnotatedType);
		if (genericsTypes.isEmpty()) {
			if (currentAnnotatedType instanceof AnnotatedTypeVariable) {
				return generateAnnotatedTypeWithoutAnnotation(Object.class);
			}

			return currentAnnotatedType;
		}

		return newAnnotatedTypeVariable(currentAnnotatedType, genericsTypes);
	}

	@Nullable
	private static AnnotatedType resolvesParentTypeGenerics(
		AnnotatedType parentAnnotatedType,
		AnnotatedType currentAnnotatedType
	) {
		if (!(parentAnnotatedType instanceof AnnotatedParameterizedType)) {
			return null;
		}

		AnnotatedParameterizedType parentAnnotatedParameterizedType = (AnnotatedParameterizedType)parentAnnotatedType;
		AnnotatedType[] parentGenericsTypes = parentAnnotatedParameterizedType.getAnnotatedActualTypeArguments();
		if (parentGenericsTypes == null || parentGenericsTypes.length == 0) {
			return currentAnnotatedType;
		}

		ParameterizedType parentParameterizedType = (ParameterizedType)parentAnnotatedParameterizedType.getType();
		Class<?> parentActualType = Types.getActualType(parentParameterizedType.getRawType());
		List<Type> parentTypeVariableParameters = Arrays.asList(parentActualType.getTypeParameters());

		Type genericType = currentAnnotatedType.getType();
		if (TypeVariable.class.isAssignableFrom(genericType.getClass())) {
			int index = parentTypeVariableParameters.indexOf(genericType);
			return parentGenericsTypes[index];
		}

		if (genericType instanceof GenericArrayType) {
			return resolveGenericsArrayType(parentAnnotatedType, currentAnnotatedType, parentGenericsTypes);
		}

		AnnotatedParameterizedType fieldParameterizedType = (AnnotatedParameterizedType)currentAnnotatedType;
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
				for (int t = 0; t < parentTypeVariableParameters.size(); t++) {
					Type parentTypeVariable = parentTypeVariableParameters.get(t);
					if (parentTypeVariable.getTypeName().equals(typeVariable.getTypeName())) {
						generics = parentGenericsTypes[t];
						break;
					}
				}
				resolvedGenericsTypes[i] = generics;
				resolvedTypes[i] = generics.getType();
			}
		}

		ParameterizedType type = (ParameterizedType)fieldParameterizedType.getType();
		Type resolveType = new GenericType(type.getRawType(), resolvedTypes, type.getOwnerType());

		return AnnotatedTypes.newAnnotatedParameterizedType(
			resolvedGenericsTypes,
			resolveType,
			fieldParameterizedType.getAnnotations(),
			fieldParameterizedType.getDeclaredAnnotations(),
			parentAnnotatedType
		);
	}

	private static AnnotatedArrayType resolveGenericsArrayType(
		AnnotatedType parentAnnotatedType,
		AnnotatedType currentAnnotatedType,
		AnnotatedType[] ownerGenericsTypes
	) {
		GenericArrayType genericArrayType = (GenericArrayType)currentAnnotatedType.getType();
		ParameterizedType genericComponentType = (ParameterizedType)genericArrayType.getGenericComponentType();

		Type[] types = new Type[genericComponentType.getActualTypeArguments().length];
		for (int i = 0; i < ownerGenericsTypes.length; i++) {
			types[i] = ownerGenericsTypes[i].getType();
		}

		ParameterizedType genericComponentTypeWithGeneric = new GenericType(
			genericComponentType.getRawType(),
			types,
			genericComponentType.getOwnerType()
		);

		Type resolveType = (GenericArrayType)() -> genericComponentTypeWithGeneric;

		return AnnotatedTypes.newAnnotatedArrayType(
			Types.generateAnnotatedTypeWithoutAnnotation(genericComponentTypeWithGeneric),
			resolveType,
			currentAnnotatedType.getAnnotations(),
			currentAnnotatedType.getDeclaredAnnotations(),
			parentAnnotatedType
		);
	}

	public static AnnotatedType resolveWithTypeReferenceGenerics(
		AnnotatedType parentType,
		PropertyDescriptor propertyDescriptor
	) {
		return resolveWithTypeReferenceGenerics(parentType, TypeCache.getAnnotatedType(propertyDescriptor));
	}

	public static AnnotatedType getArrayComponentAnnotatedType(AnnotatedType annotatedType) {
		if ((annotatedType instanceof AnnotatedArrayType)) {
			AnnotatedArrayType annotatedArrayType = (AnnotatedArrayType)annotatedType;

			return annotatedArrayType.getAnnotatedGenericComponentType();
		}

		Class<?> type = Types.getActualType(annotatedType.getType());
		if (type.isArray()) {
			return generateAnnotatedTypeWithoutAnnotation(type.getComponentType());
		}

		throw new IllegalArgumentException(
			"given type is not Array type, annotatedType: " + annotatedType
		);
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

	public static <T> T defaultIfNull(@Nullable T obj, Supplier<T> defaultValue) {
		return obj != null ? obj : defaultValue.get();
	}

	public static class UnidentifiableType {
		private UnidentifiableType() {
		}
	}

	public static class GeneratingWildcardType {
		private GeneratingWildcardType() {
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

	/**
	 * It is same as {@code toClass.isAssignableFrom(cls)}.
	 */
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

	public static List<Type> getGenericsTypes(ParameterizedType parameterizedType) {
		return Arrays.asList(parameterizedType.getActualTypeArguments());
	}

	public static boolean isIntegerType(Class<?> type) {
		return Integer.class.isAssignableFrom(type)
			|| int.class.isAssignableFrom(type)
			|| Long.class.isAssignableFrom(type)
			|| long.class.isAssignableFrom(type)
			|| Byte.class.isAssignableFrom(type)
			|| byte.class.isAssignableFrom(type)
			|| Short.class.isAssignableFrom(type)
			|| short.class.isAssignableFrom(type)
			|| BigInteger.class.isAssignableFrom(type);
	}

	public static boolean isDecimalType(Class<?> type) {
		return Float.class.isAssignableFrom(type)
			|| float.class.isAssignableFrom(type)
			|| Double.class.isAssignableFrom(type)
			|| double.class.isAssignableFrom(type)
			|| BigDecimal.class.isAssignableFrom(type);
	}

	public static boolean isDateTimeType(Class<?> type) {
		return Calendar.class.isAssignableFrom(type)
			|| Date.class.isAssignableFrom(type)
			|| Instant.class.isAssignableFrom(type)
			|| LocalDateTime.class.isAssignableFrom(type)
			|| ZonedDateTime.class.isAssignableFrom(type)
			|| OffsetDateTime.class.isAssignableFrom(type);
	}

	public static boolean isDateType(Class<?> type) {
		return Year.class.isAssignableFrom(type)
			|| YearMonth.class.isAssignableFrom(type)
			|| LocalDate.class.isAssignableFrom(type)
			|| MonthDay.class.isAssignableFrom(type);
	}

	public static boolean isTimeType(Class<?> type) {
		return LocalTime.class.isAssignableFrom(type)
			|| OffsetTime.class.isAssignableFrom(type);
	}

	public static boolean isJavaType(Class<?> type) {
		return type.isPrimitive() || isJavaPackage(type);
	}

	private static boolean isJavaPackage(Class<?> type) {
		return type.getPackage() != null
			&& (type.getPackage().getName().startsWith("java") || type.getPackage().getName().startsWith("sun"));
	}

	public static boolean isAssignableTypes(Class<?>[] froms, Class<?>[] tos) {
		if (froms.length != tos.length) {
			return false;
		}

		for (int i = 0; i < froms.length; i++) {
			Class<?> from = froms[i];
			Class<?> to = tos[i];

			if (!isAssignable(from, to)) {
				return false;
			}
		}
		return true;
	}

	public static TypeReference<?> toTypeReference(AnnotatedType annotatedType) {
		return new TypeReference<Object>() {
			@Override
			public Type getType() {
				return annotatedType.getType();
			}

			@Override
			public AnnotatedType getAnnotatedType() {
				return annotatedType;
			}
		};
	}

	public static <T> T nullSafe(@Nullable T obj) {
		if (obj == null) {
			throw new IllegalArgumentException("given value should not be null");
		}
		return obj;
	}

	public static JvmType toJvmType(AnnotatedType annotatedType, List<Annotation> annotations) {
		List<Annotation> concatAnnotations =
			Stream.concat(annotations.stream(), Arrays.stream(annotatedType.getAnnotations()))
				.collect(Collectors.toList());

		return new JavaType(
			Types.getActualType(annotatedType),
			Types.getGenericsTypes(annotatedType).stream()
				.map(
					genericAnnotatedType -> new JavaType(new ObjectTypeReference<Object>() {
						@Override
						public AnnotatedType getAnnotatedType() {
							return genericAnnotatedType;
						}
					}))
				.collect(Collectors.toList()),
			concatAnnotations,
			annotatedType
		);
	}
}
