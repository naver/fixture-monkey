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

package com.navercorp.fixturemonkey.jackson.generator;

import static com.navercorp.fixturemonkey.jackson.property.JacksonAnnotations.getJacksonAnnotation;

import java.util.Collections;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.fasterxml.jackson.annotation.JsonSubTypes;

import com.navercorp.fixturemonkey.api.generator.ObjectProperty;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.property.ElementProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.jackson.property.ElementJsonSubTypesConcreteTypeResolver;

/**
 * It is deprecated.
 * Use {@link ElementJsonSubTypesConcreteTypeResolver} instaed.
 */
@API(since = "0.4.2", status = Status.MAINTAINED)
@Deprecated
public final class ElementJsonSubTypesObjectPropertyGenerator implements ObjectPropertyGenerator {
	public static final ElementJsonSubTypesObjectPropertyGenerator INSTANCE =
		new ElementJsonSubTypesObjectPropertyGenerator();

	private static final ElementJsonSubTypesConcreteTypeResolver DELEGATE =
		new ElementJsonSubTypesConcreteTypeResolver();

	@Override
	public ObjectProperty generate(ObjectPropertyGeneratorContext context) {
		Property property = context.getProperty();
		double nullInject = context.getNullInjectGenerator()
			.generate(context);
		PropertyGenerator propertyGenerator = context.getPropertyGenerator();
		Property containerProperty = ((ElementProperty)property).getContainerProperty();

		JsonSubTypes jsonSubTypes = getJacksonAnnotation(containerProperty, JsonSubTypes.class);
		if (jsonSubTypes == null) {
			throw new IllegalArgumentException("@JsonSubTypes is not found " + property.getType().getTypeName());
		}

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
	}
}
