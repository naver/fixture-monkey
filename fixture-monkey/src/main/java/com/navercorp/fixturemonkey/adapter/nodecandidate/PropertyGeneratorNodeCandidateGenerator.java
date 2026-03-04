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

package com.navercorp.fixturemonkey.adapter.nodecandidate;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.adapter.property.JvmNodePropertyFactory;
import com.navercorp.fixturemonkey.api.property.CompositeProperty;
import com.navercorp.fixturemonkey.api.property.ConstructorProperty;
import com.navercorp.fixturemonkey.api.property.FieldProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyDescriptorProperty;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.objectfarm.api.nodecandidate.ConstructorParamCreationMethod;
import com.navercorp.objectfarm.api.nodecandidate.CreationMethod;
import com.navercorp.objectfarm.api.nodecandidate.FieldAccessCreationMethod;
import com.navercorp.objectfarm.api.nodecandidate.JavaNodeCandidateFactory;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidateGenerator;
import com.navercorp.objectfarm.api.nodecandidate.MethodInvocationCreationMethod;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Bridges {@link PropertyGenerator} to {@link JvmNodeCandidateGenerator}.
 * <p>
 * This adapter allows fixture-monkey's introspector-specific {@link PropertyGenerator}
 * to be used within the object-farm node tree building infrastructure.
 *
 * @since 1.1.0
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
public final class PropertyGeneratorNodeCandidateGenerator implements JvmNodeCandidateGenerator {
	private final PropertyGenerator propertyGenerator;

	/**
	 * Creates a new generator that wraps the given {@link PropertyGenerator}.
	 *
	 * @param propertyGenerator the property generator to wrap
	 */
	public PropertyGeneratorNodeCandidateGenerator(PropertyGenerator propertyGenerator) {
		this.propertyGenerator = propertyGenerator;
	}

	@Override
	public List<JvmNodeCandidate> generateNextNodeCandidates(JvmType jvmType) {
		Class<?> rawType = jvmType.getRawType();
		if (rawType.isPrimitive() || rawType.isArray() || rawType.isEnum()) {
			return Collections.emptyList();
		}

		Property property = JvmNodePropertyFactory.fromType(jvmType);
		AnnotatedType parentAnnotatedType = property.getAnnotatedType();

		List<Property> childProperties = propertyGenerator.generateChildProperties(property);
		if (childProperties == null || childProperties.isEmpty()) {
			return Collections.emptyList();
		}

		List<JvmNodeCandidate> result = new ArrayList<>(childProperties.size());
		for (int i = 0; i < childProperties.size(); i++) {
			result.add(toNodeCandidate(parentAnnotatedType, childProperties.get(i), i));
		}
		return result;
	}

	private JvmNodeCandidate toNodeCandidate(AnnotatedType parentAnnotatedType, Property property, int index) {
		// Resolve type variables in the child property's type against the parent's type
		AnnotatedType resolvedType = Types.resolveWithTypeReferenceGenerics(
			parentAnnotatedType,
			property.getAnnotatedType()
		);
		JvmType jvmType = Types.toJvmType(resolvedType, property.getAnnotations());
		String name = property.getName();
		if (name == null) {
			name = "";
		}
		CreationMethod creationMethod = toCreationMethod(property, index);

		return JavaNodeCandidateFactory.INSTANCE.create(jvmType, name, creationMethod);
	}

	private @Nullable CreationMethod toCreationMethod(Property property, int index) {
		if (property instanceof FieldProperty) {
			Field field = ((FieldProperty)property).getField();
			return new FieldAccessCreationMethod(field);
		}

		if (property instanceof ConstructorProperty) {
			ConstructorProperty constructorProperty = (ConstructorProperty)property;
			return new ConstructorParamCreationMethod(constructorProperty.getConstructor(), index);
		}

		if (property instanceof PropertyDescriptorProperty) {
			PropertyDescriptorProperty descriptorProperty = (PropertyDescriptorProperty)property;
			Method writeMethod = descriptorProperty.getPropertyDescriptor().getWriteMethod();
			if (writeMethod != null) {
				return new MethodInvocationCreationMethod(writeMethod);
			}
		}

		// Handle CompositeProperty by trying primary and secondary properties
		if (property instanceof CompositeProperty) {
			CompositeProperty compositeProperty = (CompositeProperty)property;
			CreationMethod primaryMethod = toCreationMethod(compositeProperty.getPrimaryProperty(), index);
			if (primaryMethod != null) {
				return primaryMethod;
			}
			return toCreationMethod(compositeProperty.getSecondaryProperty(), index);
		}

		return null;
	}
}
