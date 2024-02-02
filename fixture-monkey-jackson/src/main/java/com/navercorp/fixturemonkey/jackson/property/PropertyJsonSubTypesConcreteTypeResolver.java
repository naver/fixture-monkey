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

package com.navercorp.fixturemonkey.jackson.property;

import java.util.Collections;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

<<<<<<<< HEAD:fixture-monkey-jackson/src/main/java/com/navercorp/fixturemonkey/jackson/generator/PropertyJsonSubTypesObjectPropertyGenerator.java
import com.navercorp.fixturemonkey.api.generator.ObjectProperty;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.jackson.property.PropertyJsonSubTypesConcreteTypeResolver;

@API(since = "0.4.2", status = Status.MAINTAINED)
@Deprecated
/**
 * It is deprecated.
 * Use {@link PropertyJsonSubTypesConcreteTypeResolver} instaed.
 */
public final class PropertyJsonSubTypesObjectPropertyGenerator implements ObjectPropertyGenerator {
	public static final PropertyJsonSubTypesObjectPropertyGenerator INSTANCE =
		new PropertyJsonSubTypesObjectPropertyGenerator();
========
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.navercorp.fixturemonkey.api.property.CandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "1.1.0", status = Status.EXPERIMENTAL)
public final class PropertyJsonSubTypesConcreteTypeResolver implements CandidateConcretePropertyResolver {
	public static final CandidateConcretePropertyResolver INSTANCE = new PropertyJsonSubTypesConcreteTypeResolver();
>>>>>>>> a83036ca (Remove all deprecated in 1.0.x):fixture-monkey-jackson/src/main/java/com/navercorp/fixturemonkey/jackson/property/PropertyJsonSubTypesConcreteTypeResolver.java

	private static final PropertyJsonSubTypesConcreteTypeResolver DELEGATE =
		new PropertyJsonSubTypesConcreteTypeResolver();

	@Override
<<<<<<<< HEAD:fixture-monkey-jackson/src/main/java/com/navercorp/fixturemonkey/jackson/generator/PropertyJsonSubTypesObjectPropertyGenerator.java
	public ObjectProperty generate(ObjectPropertyGeneratorContext context) {
		Property property = context.getProperty();
		double nullInject = context.getNullInjectGenerator()
			.generate(context);
		PropertyGenerator propertyGenerator = context.getPropertyGenerator();

		Property actualConcreteProperty = DELEGATE.resolve(property).get(0);

		List<Property> childProperties =
			propertyGenerator.generateChildProperties(actualConcreteProperty);

		return new ObjectProperty(
			actualConcreteProperty,
			context.getPropertyNameResolver(),
			nullInject,
			context.getElementIndex(),
			Collections.singletonMap(actualConcreteProperty, childProperties)
		);
========
	public List<Property> resolve(Property property) {
		JsonSubTypes jsonSubTypes = getJacksonAnnotation(property, JsonSubTypes.class);
		if (jsonSubTypes == null) {
			throw new IllegalArgumentException("@JsonSubTypes is not found " + property.getType().getTypeName());
		}

		Class<?> type = getRandomJsonSubType(jsonSubTypes);
		AnnotatedType annotatedType = Types.generateAnnotatedTypeWithoutAnnotation(type);

		JsonTypeInfo jsonTypeInfo = getJacksonAnnotation(property, JsonTypeInfo.class);
		List<Annotation> annotations = new ArrayList<>(property.getAnnotations());
		annotations.add(jsonTypeInfo);

		Property actualProperty = new Property() {
			@Override
			public Type getType() {
				return type;
			}

			@Override
			public AnnotatedType getAnnotatedType() {
				return annotatedType;
			}

			@Nullable
			@Override
			public String getName() {
				return property.getName();
			}

			@Override
			public List<Annotation> getAnnotations() {
				return Collections.unmodifiableList(annotations);
			}

			@Nullable
			@Override
			public Object getValue(Object instance) {
				return property.getValue(instance);
			}
		};

		return Collections.singletonList(actualProperty);
>>>>>>>> a83036ca (Remove all deprecated in 1.0.x):fixture-monkey-jackson/src/main/java/com/navercorp/fixturemonkey/jackson/property/PropertyJsonSubTypesConcreteTypeResolver.java
	}
}
