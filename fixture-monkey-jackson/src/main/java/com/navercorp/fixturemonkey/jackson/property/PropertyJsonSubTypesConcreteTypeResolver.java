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

import static com.navercorp.fixturemonkey.jackson.property.JacksonAnnotations.getJacksonAnnotation;
import static com.navercorp.fixturemonkey.jackson.property.JacksonAnnotations.getRandomJsonSubType;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.navercorp.fixturemonkey.api.property.CandidateConcretePropertyResolver;
import com.navercorp.fixturemonkey.api.property.ConcreteTypeProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "1.1.0", status = Status.EXPERIMENTAL)
public final class PropertyJsonSubTypesConcreteTypeResolver implements CandidateConcretePropertyResolver {
	public static final CandidateConcretePropertyResolver INSTANCE = new PropertyJsonSubTypesConcreteTypeResolver();

	@SuppressWarnings("argument")
	@Override
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

		Property actualProperty = new ConcreteTypeProperty(annotatedType, property, annotations);
		return Collections.singletonList(actualProperty);
	}
}
