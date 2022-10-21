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

package com.navercorp.fixturemonkey.jackson.introspector;

import static com.navercorp.fixturemonkey.jackson.property.JacksonAnnotations.getJacksonAnnotation;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Builders;
import net.jqwik.api.Builders.BuilderCombinator;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.jackson.FixtureMonkeyJackson;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class JacksonArbitraryIntrospector implements ArbitraryIntrospector {
	public static final JacksonArbitraryIntrospector INSTANCE = new JacksonArbitraryIntrospector(
		FixtureMonkeyJackson.defaultObjectMapper()
	);

	private final ObjectMapper objectMapper;

	public JacksonArbitraryIntrospector(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Property property = context.getProperty();
		Class<?> actualType = Types.getActualType(property.getType());

		List<ArbitraryProperty> childrenProperties = context.getChildren();
		Map<String, Arbitrary<?>> childrenArbitraries = context.getChildrenArbitraryContexts()
			.getArbitrariesByResolvedName();

		BuilderCombinator<Map<String, Object>> builderCombinator = Builders.withBuilder(() -> initializeMap(property));

		for (ArbitraryProperty arbitraryProperty : childrenProperties) {
			String resolvePropertyName = arbitraryProperty.getObjectProperty().getResolvedPropertyName();
			Arbitrary<?> propertyArbitrary = childrenArbitraries.getOrDefault(
				resolvePropertyName,
				Arbitraries.just(null)
			);

			builderCombinator = builderCombinator.use(propertyArbitrary).in((map, value) -> {
				if (value != null) {
					Object jsonFormatted = arbitraryProperty.getObjectProperty()
						.getProperty()
						.getAnnotation(JsonFormat.class)
						.map(it -> format(value, it))
						.orElse(value);

					map.put(resolvePropertyName, jsonFormatted);
				}
				return map;
			});
		}

		return new ArbitraryIntrospectorResult(
			builderCombinator.build(
				map -> {
					if (property instanceof RootProperty) {
						return objectMapper.convertValue(map, actualType);
					}
					return map;
				}
			)
		);
	}

	private Map<String, Object> initializeMap(Property property) {
		Map<String, Object> defaultMap = new HashMap<>();

		Class<?> concreteClass = Types.getActualType(property.getType());
		JsonTypeInfo jsonTypeInfo = getJacksonAnnotation(property, JsonTypeInfo.class);

		if (jsonTypeInfo == null) {
			return defaultMap;
		}

		Id id = jsonTypeInfo.use();
		String jsonTypeInfoValue;
		switch (id) {
			case NAME:
				JsonTypeName jsonTypeName = getJacksonAnnotation(property, JsonTypeName.class);
				if (jsonTypeName != null) {
					jsonTypeInfoValue = jsonTypeName.value();
				} else {
					jsonTypeInfoValue = concreteClass.getSimpleName();
				}
				break;
			case CLASS:
				jsonTypeInfoValue = concreteClass.getName();
				break;
			default:
				throw new IllegalArgumentException("Unsupported JsonTypeInfo Id : " + id.name());
		}
		String jsonTypeInfoPropertyName =
			"".equals(jsonTypeInfo.property()) ? id.getDefaultPropertyName() : jsonTypeInfo.property();

		defaultMap.put(jsonTypeInfoPropertyName, jsonTypeInfoValue);
		return defaultMap;
	}

	private Object format(Object object, JsonFormat jsonFormat) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(jsonFormat.pattern())
			.withZone(ZoneId.systemDefault());

		if (object instanceof TemporalAccessor) {
			TemporalAccessor temporalAccessor = (TemporalAccessor)object;
			return dateTimeFormatter.format(temporalAccessor);
		} else if (object instanceof Date) {
			TemporalAccessor dateTemporalAccessor = ((Date)object).toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
			return dateTimeFormatter.format(dateTemporalAccessor);
		} else if (object instanceof Enum && jsonFormat.shape().isNumeric()) {
			return ((Enum<?>)object).ordinal();
		} else {
			return object;
		}
	}
}
