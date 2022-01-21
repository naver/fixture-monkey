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

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class PropertyCache {
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertyCache.class);

	private static final Map<Class<?>, Map<Method, PropertyDescriptor>> PROPERTY_DESCRIPTORS =
		new ConcurrentHashMap<>();
	private static final Map<Class<?>, Map<String, Field>> FIELDS = new ConcurrentHashMap<>();
	private static final Map<Class<?>, Map<String, Optional<Property>>> PROPERTIES = new ConcurrentHashMap<>();

	public static Optional<Property> getReadProperty(Class<?> clazz, String name) {
		Map<String, Optional<Property>> propertyMap = PROPERTIES.computeIfAbsent(
			clazz, type -> new ConcurrentHashMap<>());

		Optional<Property> property = propertyMap.get(name);
		if (property != null) {
			return property;
		}

		PropertyDescriptor propertyDescriptor = getReadPropertyDescriptors(clazz).values().stream()
			.filter(it -> it.getName().equals(name))
			.findFirst()
			.orElse(null);
		Field field = getFields(clazz).get(name);

		if (propertyDescriptor == null && field == null) {
			property = Optional.empty();
			propertyMap.put(name, property);
			return property;
		}

		if (propertyDescriptor == null) {
			property = Optional.of(new FieldProperty(field));
			propertyMap.put(name, property);
			return property;
		}

		if (field == null) {
			property = Optional.of(new PropertyDescriptorProperty(propertyDescriptor));
			propertyMap.put(name, property);
			return property;
		}

		property = Optional.of(
			new CompositeProperty(
				new PropertyDescriptorProperty(propertyDescriptor),
				new FieldProperty(field)
			)
		);
		propertyMap.put(name, property);
		return property;
	}

	public static Optional<Property> getReadProperty(Class<?> clazz, Method method) {
		Map<Method, PropertyDescriptor> propertyDescriptorMap = getReadPropertyDescriptors(clazz);
		PropertyDescriptor propertyDescriptor = propertyDescriptorMap.get(method);
		if (propertyDescriptor == null) {
			return Optional.empty();
		}

		String propertyName = propertyDescriptor.getName();

		Map<String, Optional<Property>> propertyMap = PROPERTIES.computeIfAbsent(
			clazz, type -> new ConcurrentHashMap<>());

		Optional<Property> property = propertyMap.get(propertyName);
		if (property != null) {
			return property;
		}

		Field field = getFields(clazz).get(propertyName);

		if (field == null) {
			property = Optional.of(new PropertyDescriptorProperty(propertyDescriptor));
			propertyMap.put(propertyName, property);
			return property;
		}

		property = Optional.of(
			new CompositeProperty(
				new PropertyDescriptorProperty(propertyDescriptor),
				new FieldProperty(field)
			)
		);
		propertyMap.put(propertyName, property);
		return property;
	}

	public static Map<String, Field> getFields(Class<?> clazz) {
		return FIELDS.computeIfAbsent(clazz, type -> {
			Map<String, Field> result = new ConcurrentHashMap<>();
			List<Field> fields = ReflectionUtils.findFields(
				clazz, field -> true, HierarchyTraversalMode.TOP_DOWN);
			for (Field field : fields) {
				result.put(field.getName(), field);
			}
			return result;
		});
	}

	public static Map<Method, PropertyDescriptor> getReadPropertyDescriptors(Class<?> clazz) {
		return PROPERTY_DESCRIPTORS.computeIfAbsent(clazz, type -> {
			Map<Method, PropertyDescriptor> result = new ConcurrentHashMap<>();
			try {
				PropertyDescriptor[] descriptors = Introspector.getBeanInfo(type)
					.getPropertyDescriptors();
				for (PropertyDescriptor descriptor : descriptors) {
					Method readMethod = descriptor.getReadMethod(); // can not be null
					result.put(readMethod, descriptor);
				}
			} catch (IntrospectionException ex) {
				LOGGER.warn("Introspect bean property is failed. type: " + clazz, ex);
			}
			return result;
		});
	}
}
