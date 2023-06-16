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

package com.navercorp.fixturemonkey.api.property;

import java.beans.PropertyDescriptor;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.DefaultPropertyGenerator;
import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class PropertyCache {
	@Deprecated // It would be removed when getProperties is removed.
	private static final DefaultPropertyGenerator DEFAULT_PROPERTY_GENERATOR = new DefaultPropertyGenerator();

	@Deprecated // It would be removed in 0.6.0
	public static List<Property> getProperties(AnnotatedType annotatedType) {
		return DEFAULT_PROPERTY_GENERATOR.generateObjectChildProperties(annotatedType);
	}

	@Deprecated // It would be removed in 0.6.0
	public static Optional<Property> getProperty(AnnotatedType annotatedType, String name) {
		return getProperties(annotatedType).stream()
			.filter(it -> name.equals(it.getName()))
			.findFirst();
	}

	@Deprecated // It would be removed in 0.6.0
	public static Map<String, Field> getFields(Class<?> clazz) {
		return getFieldsByName(Types.generateAnnotatedTypeWithoutAnnotation(clazz));
	}

	/**
	 * It is Deprecated. Use {@link TypeCache#getFieldsByName(Class)} instead.
	 */
	@Deprecated
	public static Map<String, Field> getFieldsByName(AnnotatedType annotatedType) {
		Class<?> clazz = Types.getActualType(annotatedType.getType());
		return TypeCache.getFieldsByName(clazz);
	}

	@Deprecated // It would be removed in 0.6.0
	public static Map<String, PropertyDescriptor> getPropertyDescriptors(Class<?> clazz) {
		return getPropertyDescriptorsByPropertyName(Types.generateAnnotatedTypeWithoutAnnotation(clazz));
	}

	/**
	 * It is Deprecated. Use {@link TypeCache#getPropertyDescriptorsByPropertyName(Class)} instead.
	 */
	@Deprecated
	public static Map<String, PropertyDescriptor> getPropertyDescriptorsByPropertyName(AnnotatedType annotatedType) {
		Class<?> clazz = Types.getActualType(annotatedType.getType());
		return TypeCache.getPropertyDescriptorsByPropertyName(clazz);
	}

	@Deprecated // It would be removed in 0.6.0
	public static Map<String, Property> getConstructorProperties(AnnotatedType annotatedType) {
		return getConstructorParameterPropertiesByParameterName(annotatedType);
	}

	/**
	 * It is Deprecated. Use {@link ConstructorParameterPropertyGenerator} instead.
	 */
	@Deprecated
	public static Map<String, Property> getConstructorParameterPropertiesByParameterName(AnnotatedType annotatedType) {
		Class<?> clazz = Types.getActualType(annotatedType.getType());

		Map<String, Property> constructorPropertiesByName = new HashMap<>();
		Map.Entry<Constructor<?>, String[]> parameterNamesByConstructor = getParameterNamesByConstructor(clazz);
		if (parameterNamesByConstructor == null) {
			return Collections.emptyMap();
		}

		Constructor<?> primaryConstructor = parameterNamesByConstructor.getKey();
		String[] parameterNames = parameterNamesByConstructor.getValue();
		AnnotatedType[] annotatedParameterTypes = primaryConstructor.getAnnotatedParameterTypes();

		Map<String, Field> fieldsByName = getFieldsByName(annotatedType);
		int parameterSize = parameterNames.length;
		for (int i = 0; i < parameterSize; i++) {
			AnnotatedType annotatedParameterType = annotatedParameterTypes[i];
			String parameterName = parameterNames[i];
			Field field = fieldsByName.get(parameterName);
			Property fieldProperty = field != null
				? new FieldProperty(
				Types.resolveWithTypeReferenceGenerics(annotatedType, field.getAnnotatedType()),
				field
			)
				: null;

			if (isGenericAnnotatedType(annotatedParameterType) && fieldProperty != null) {
				constructorPropertiesByName.put(
					parameterName,
					new ConstructorProperty(
						fieldProperty.getAnnotatedType(),
						primaryConstructor,
						parameterName,
						fieldProperty
					)
				);
			} else {
				constructorPropertiesByName.put(
					parameterName,
					new ConstructorProperty(
						annotatedParameterType,
						primaryConstructor,
						parameterName,
						fieldProperty
					)
				);
			}
		}
		return Collections.unmodifiableMap(constructorPropertiesByName);
	}

	/**
	 * It is Deprecated. Use {@link TypeCache#getParameterNamesByConstructor(Class)}} instead.
	 */
	@Deprecated
	@Nullable
	public static Entry<Constructor<?>, String[]> getParameterNamesByConstructor(Class<?> clazz) {
		return TypeCache.getParameterNamesByConstructor(clazz);
	}

	/**
	 * It is Deprecated. Use {@link TypeCache#clearCache()} instead.
	 */
	@Deprecated
	public static void clearCache() {
		TypeCache.clearCache();
	}

	private static boolean isGenericAnnotatedType(AnnotatedType annotatedType) {
		return annotatedType instanceof AnnotatedTypeVariable || annotatedType instanceof AnnotatedArrayType;
	}
}
