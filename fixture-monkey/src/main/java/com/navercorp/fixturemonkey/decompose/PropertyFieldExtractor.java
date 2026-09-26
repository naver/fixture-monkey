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

package com.navercorp.fixturemonkey.decompose;

import static com.navercorp.fixturemonkey.api.property.DefaultPropertyGenerator.FIELD_PROPERTY_GENERATOR;
import static com.navercorp.fixturemonkey.api.type.Types.generateAnnotatedTypeWithoutAnnotation;
import static com.navercorp.fixturemonkey.api.type.Types.isBoxedPrimitive;
import static com.navercorp.fixturemonkey.api.type.Types.isJavaType;
import static com.navercorp.fixturemonkey.api.type.Types.normalizeRawType;
import static com.navercorp.fixturemonkey.api.type.Types.toJvmType;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.property.FieldProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.TypeParameterProperty;
import com.navercorp.objectfarm.api.input.ExtractedField;
import com.navercorp.objectfarm.api.input.FieldExtractor;
import com.navercorp.objectfarm.api.input.InlinedValueResolver;

/**
 * Reads the fields of a value as the properties of the tree, named the way the tree names them, so a value
 * expands into the same paths during planning and during assembly.
 */
@API(since = "1.2.4", status = Status.EXPERIMENTAL)
public final class PropertyFieldExtractor implements FieldExtractor {
	private final Function<Property, @Nullable String> nameResolver;
	private final InlinedValueResolver inlinedValueResolver;

	/**
	 * Creates an extractor naming fields by {@code nameResolver}.
	 *
	 * @param nameResolver         the name of a property, or {@code null} for its declared name
	 * @param inlinedValueResolver reconstructs value types a JVM language inlined into the value's fields
	 */
	public PropertyFieldExtractor(
		@Nullable Function<Property, @Nullable String> nameResolver,
		InlinedValueResolver inlinedValueResolver
	) {
		this.nameResolver = nameResolver != null ? nameResolver : Property::getName;
		this.inlinedValueResolver = inlinedValueResolver;
	}

	@Override
	public Map<String, ExtractedField> extractFields(@Nullable Object value, String basePath) {
		if (value == null) {
			return new HashMap<>();
		}
		List<Property> childProperties = getChildProperties(value);
		if (childProperties == null) {
			return new HashMap<>();
		}

		Map<String, ExtractedField> result = new HashMap<>();
		for (Property childProperty : childProperties) {
			String name = nameResolver.apply(childProperty);
			if (name == null) {
				continue;
			}
			String childPath = basePath + "." + name;
			Class<?> declaredType = normalizeRawType(childProperty.getJvmType().getRawType());
			Object fieldValue = readPropertyValue(childProperty, value);
			ExtractedField extracted = new ExtractedField(fieldValue, declaredType);
			String memberName = childProperty.getName();
			result.put(
				childPath,
				memberName != null ? inlinedValueResolver.resolve(value, memberName, extracted) : extracted
			);
		}
		return result;
	}

	private @Nullable List<Property> getChildProperties(@Nullable Object value) {
		if (value == null) {
			return null;
		}
		Class<?> clazz = value.getClass();
		if (clazz.isPrimitive() || clazz == String.class || clazz.isEnum() || clazz.isArray()) {
			return null;
		}
		if (isBoxedPrimitive(clazz)) {
			return null;
		}
		if (
			value instanceof Collection
				|| value instanceof Map
				|| value instanceof Iterator
				|| value instanceof Stream
		) {
			return null;
		}
		if (isJavaType(clazz)) {
			return null;
		}
		AnnotatedType annotatedType = generateAnnotatedTypeWithoutAnnotation(clazz);
		Property parentProperty = new TypeParameterProperty(toJvmType(annotatedType, Collections.emptyList()));
		return FIELD_PROPERTY_GENERATOR.generateChildProperties(parentProperty);
	}

	private @Nullable Object readPropertyValue(Property property, @Nullable Object instance) {
		if (instance == null || !(property instanceof FieldProperty)) {
			return null;
		}
		Field field = ((FieldProperty)property).getField();
		try {
			field.setAccessible(true);
			return field.get(instance);
		} catch (IllegalAccessException ex) {
			return null;
		}
	}
}
