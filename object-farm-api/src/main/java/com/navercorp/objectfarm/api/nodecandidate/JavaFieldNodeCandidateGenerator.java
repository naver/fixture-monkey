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

package com.navercorp.objectfarm.api.nodecandidate;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.JvmTypes;
import com.navercorp.objectfarm.api.type.Reflections;

public final class JavaFieldNodeCandidateGenerator implements JvmNodeCandidateGenerator {

	@Override
	public List<JvmNodeCandidate> generateNextNodeCandidates(JvmType jvmType) {
		Class<?> rawType = jvmType.getRawType();
		if (rawType.isPrimitive() || rawType.isArray() || rawType.isEnum()) {
			return Collections.emptyList();
		}

		// Use java.beans.Introspector to discover getter/setter methods for each property.
		// Annotations on accessor methods are merged into the field's annotations
		// so that downstream consumers (e.g., Jackson's @JsonTypeInfo/@JsonSubTypes) can
		// find them regardless of whether they were declared on the field, getter, or setter.
		Map<String, PropertyDescriptor> descriptorsByName = getPropertyDescriptors(rawType);

		List<Field> fields = Reflections.findFields(rawType);
		return fields
			.stream()
			.map(field -> {
				List<Annotation> annotations = new ArrayList<>(Arrays.asList(field.getAnnotations()));
				Set<Class<? extends Annotation>> seen = annotations
					.stream()
					.map(Annotation::annotationType)
					.collect(Collectors.toSet());

				PropertyDescriptor descriptor = descriptorsByName.get(field.getName());
				if (descriptor != null) {
					mergeAccessorAnnotations(descriptor.getReadMethod(), annotations, seen);
					mergeAccessorAnnotations(descriptor.getWriteMethod(), annotations, seen);
				}

				JvmType javaType = JvmTypes.resolveJvmType(jvmType, field.getGenericType(), annotations);
				CreationMethod creationMethod = new FieldAccessCreationMethod(field);

				return JavaNodeCandidateFactory.INSTANCE.create(javaType, field.getName(), creationMethod);
			})
			.collect(Collectors.toList());
	}

	private static void mergeAccessorAnnotations(
		@Nullable Method accessor,
		List<Annotation> annotations,
		Set<Class<? extends Annotation>> seen
	) {
		if (accessor == null) {
			return;
		}
		for (Annotation ann : accessor.getAnnotations()) {
			if (seen.add(ann.annotationType())) {
				annotations.add(ann);
			}
		}
	}

	private static Map<String, PropertyDescriptor> getPropertyDescriptors(Class<?> clazz) {
		try {
			PropertyDescriptor[] descriptors = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
			Map<String, PropertyDescriptor> result = new HashMap<>(descriptors.length);
			for (PropertyDescriptor descriptor : descriptors) {
				if (!"class".equals(descriptor.getName())) {
					result.put(descriptor.getName(), descriptor);
				}
			}
			return result;
		} catch (IntrospectionException e) {
			return Collections.emptyMap();
		}
	}
}
