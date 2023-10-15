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

import static java.util.stream.Collectors.toList;

import java.beans.ConstructorProperties;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.fixturemonkey.api.container.ConcurrentLruCache;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class TypeCache {
	private static final Logger LOGGER = LoggerFactory.getLogger(TypeCache.class);
	private static final Map<Field, AnnotatedType> FIELD_ANNOTATED_TYPE_MAP = new ConcurrentHashMap<>(2048);
	private static final Map<PropertyDescriptor, AnnotatedType> PROPERTY_DESCRIPTOR_ANNOTATED_TYPE_MAP =
		new ConcurrentHashMap<>(2048);
	private static final Map<Class<?>, Map<String, PropertyDescriptor>> PROPERTY_DESCRIPTORS =
		new ConcurrentLruCache<>(2048);
	private static final Map<Class<?>, Map<String, Field>> FIELDS = new ConcurrentLruCache<>(2048);
	private static final Map<Class<?>, Map.Entry<Constructor<?>, String[]>> PARAMETER_NAMES_BY_PRIMARY_CONSTRUCTOR =
		new ConcurrentLruCache<>(2048);
	private static final Map<Class<?>, List<Constructor<?>>> CONSTRUCTORS = new ConcurrentLruCache<>(2048);

	public static AnnotatedType getAnnotatedType(Field field) {
		return FIELD_ANNOTATED_TYPE_MAP.computeIfAbsent(field, Field::getAnnotatedType);
	}

	public static AnnotatedType getAnnotatedType(PropertyDescriptor propertyDescriptor) {
		return PROPERTY_DESCRIPTOR_ANNOTATED_TYPE_MAP.computeIfAbsent(
			propertyDescriptor,
			it -> it.getReadMethod().getAnnotatedReturnType()
		);
	}

	public static Map<String, Field> getFieldsByName(Class<?> clazz) {
		return FIELDS.computeIfAbsent(clazz, type -> {
			Map<String, Field> result = new ConcurrentHashMap<>();
			try {
				List<Field> fields = Reflections.findFields(clazz)
					.stream()
					.filter(it -> !Modifier.isStatic(it.getModifiers()))
					.collect(toList());
				for (Field field : fields) {
					field.setAccessible(true);
					result.put(field.getName(), field);
				}
			} catch (Exception e) {
				LOGGER.warn("Failed to create fields in type {}.", clazz.getName());
			}
			return result;
		});
	}

	public static Map<String, PropertyDescriptor> getPropertyDescriptorsByPropertyName(Class<?> clazz) {
		return PROPERTY_DESCRIPTORS.computeIfAbsent(clazz, type -> {
			Map<String, PropertyDescriptor> result = new ConcurrentHashMap<>();
			try {
				PropertyDescriptor[] descriptors = Introspector.getBeanInfo(type)
					.getPropertyDescriptors();
				for (PropertyDescriptor descriptor : descriptors) {
					if (descriptor.getName().equals("class")) {
						continue;
					}
					result.put(descriptor.getName(), descriptor);
				}
			} catch (IntrospectionException ex) {
				LOGGER.warn("Introspect bean property is failed. type: " + clazz, ex);
			}
			return result;
		});
	}

	public static List<Constructor<?>> getDeclaredConstructors(Class<?> type) {
		return CONSTRUCTORS.computeIfAbsent(type, clazz -> Arrays.asList(clazz.getDeclaredConstructors()));
	}

	@Nullable
	public static Entry<Constructor<?>, String[]> getParameterNamesByConstructor(Class<?> clazz) {
		return PARAMETER_NAMES_BY_PRIMARY_CONSTRUCTOR.computeIfAbsent(clazz,
			type -> {
				List<Constructor<?>> possibilities = new ArrayList<>();

				Constructor<?>[] constructors = clazz.getDeclaredConstructors();

				for (Constructor<?> constructor : constructors) {
					Parameter[] parameters = constructor.getParameters();
					boolean namePresent = Arrays.stream(parameters).anyMatch(Parameter::isNamePresent);
					boolean parameterEmpty = parameters.length == 0;
					if (namePresent || parameterEmpty) {
						possibilities.add(constructor);
					} else {
						ConstructorProperties constructorPropertiesAnnotation =
							constructor.getAnnotation(ConstructorProperties.class);

						if (constructorPropertiesAnnotation != null) {
							possibilities.add(constructor);
						}
					}
				}

				boolean constructorPropertiesPresent = possibilities.stream()
					.anyMatch(it -> it.getAnnotation(ConstructorProperties.class) != null);

				Constructor<?> primaryConstructor;
				if (constructorPropertiesPresent) {
					primaryConstructor = possibilities.stream()
						.filter(it -> it.getAnnotation(ConstructorProperties.class) != null)
						.findFirst()
						.orElseThrow(() -> new IllegalArgumentException(
							"Constructor should have @ConstructorProperties" + clazz.getSimpleName())
						);
				} else {
					primaryConstructor = possibilities.stream()
						.findFirst()
						.orElse(null);
				}

				if (primaryConstructor == null) {
					return null;
				}

				String[] parameterNames = getParameterNames(primaryConstructor);
				AnnotatedType[] annotatedParameterTypes = primaryConstructor.getAnnotatedParameterTypes();

				if (parameterNames.length != annotatedParameterTypes.length) {
					throw new IllegalArgumentException(
						"@ConstructorProperties values size should same as constructor parameter size"
					);
				}
				return new SimpleEntry<>(primaryConstructor, parameterNames);
			});
	}

	public static void clearCache() {
		PARAMETER_NAMES_BY_PRIMARY_CONSTRUCTOR.clear();
		PROPERTY_DESCRIPTORS.clear();
		FIELDS.clear();
	}

	private static String[] getParameterNames(Constructor<?> constructor) {
		Parameter[] parameters = constructor.getParameters();
		boolean namePresent = Arrays.stream(parameters).anyMatch(Parameter::isNamePresent);
		boolean parameterEmpty = parameters.length == 0;

		if (parameterEmpty) {
			return new String[0];
		}

		if (namePresent) {
			return Arrays.stream(parameters)
				.map(Parameter::getName)
				.toArray(String[]::new);
		} else {
			ConstructorProperties constructorPropertiesAnnotation =
				constructor.getAnnotation(ConstructorProperties.class);
			return constructorPropertiesAnnotation.value();
		}
	}
}
