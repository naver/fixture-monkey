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

package com.navercorp.fixturemonkey.api.generator;

import java.beans.PropertyDescriptor;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.CompositeProperty;
import com.navercorp.fixturemonkey.api.property.FieldProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.property.PropertyDescriptorProperty;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class DefaultPropertyGenerator implements PropertyGenerator {
	public List<Property> generateProperties(AnnotatedType annotatedType) {
		Map<String, List<Property>> propertiesMap = new HashMap<>();

		Map<String, Property> constructorProperties =
			PropertyCache.getConstructorParameterPropertiesByParameterName(annotatedType);
		for (Entry<String, Property> entry : constructorProperties.entrySet()) {
			List<Property> properties = propertiesMap.computeIfAbsent(
				entry.getKey(), name -> new ArrayList<>()
			);
			properties.add(entry.getValue());
		}

		Map<String, Field> fieldMap = PropertyCache.getFieldsByName(annotatedType);
		for (Entry<String, Field> entry : fieldMap.entrySet()) {
			List<Property> properties = propertiesMap.computeIfAbsent(
				entry.getKey(), name -> new ArrayList<>()
			);
			properties.add(
				new FieldProperty(
					Types.resolveWithTypeReferenceGenerics(annotatedType, entry.getValue()),
					entry.getValue()
				)
			);
		}

		Map<String, PropertyDescriptor> propertyDescriptorMap =
			PropertyCache.getPropertyDescriptorsByPropertyName(annotatedType);
		for (Entry<String, PropertyDescriptor> entry : propertyDescriptorMap.entrySet()) {
			List<Property> properties = propertiesMap.computeIfAbsent(
				entry.getValue().getName(), name -> new ArrayList<>()
			);

			PropertyDescriptor propertyDescriptor = entry.getValue();
			if (propertyDescriptor.getReadMethod() != null && propertyDescriptor.getWriteMethod() != null) {
				properties.add(
					new PropertyDescriptorProperty(
						Types.resolveWithTypeReferenceGenerics(annotatedType, entry.getValue()),
						entry.getValue()
					)
				);
			}
		}

		List<Property> result = new ArrayList<>();
		for (List<Property> properties : propertiesMap.values()) {
			if (properties.isEmpty()) {
				continue;
			}

			if (properties.size() == 1) {
				result.add(properties.get(0));
			} else {
				result.add(new CompositeProperty(properties.get(0), properties.get(1)));
			}
		}

		return Collections.unmodifiableList(result);
	}
}
