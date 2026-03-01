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

package com.navercorp.fixturemonkey.jackson3.introspector;

import static com.navercorp.fixturemonkey.jackson3.property.Jackson3Annotations.getJacksonAnnotation;

import java.lang.reflect.Type;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;

import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.type.TypeFactory;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.property.CompositeProperty;
import com.navercorp.fixturemonkey.api.property.ConstructorProperty;
import com.navercorp.fixturemonkey.api.property.FieldProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyDescriptorProperty;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.jackson3.FixtureMonkeyJackson3;
import com.navercorp.fixturemonkey.jackson3.type.Jackson3TypeReference;

@API(since = "1.1.0", status = Status.EXPERIMENTAL)
public final class Jackson3ObjectArbitraryIntrospector implements ArbitraryIntrospector {
	public static final Jackson3ObjectArbitraryIntrospector INSTANCE = new Jackson3ObjectArbitraryIntrospector(
		FixtureMonkeyJackson3.defaultJsonMapper()
	);

	private final ObjectMapper objectMapper;

	public Jackson3ObjectArbitraryIntrospector(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Property property = context.getResolvedProperty();
		TypeFactory typeFactory = TypeFactory.createDefaultInstance();
		JavaType type = typeFactory.constructType(new Jackson3TypeReference<>() {
			@Override
			public Type getType() {
				return property.getType();
			}
		});

		return new ArbitraryIntrospectorResult(
			new Jackson3CombinableArbitrary<>(
				CombinableArbitrary.objectBuilder()
					.properties(context.getCombinableArbitrariesByArbitraryProperty())
					.build(combineAsJson(property)),
				map -> objectMapper.convertValue(map, type)
			)
		);
	}

	@SuppressWarnings("unchecked")
	private Function<Map<ArbitraryProperty, Object>, Object> combineAsJson(Property property) {
		return propertyValuesByArbitraryProperty -> {
			Map<String, Object> map = initializeMap(property);

			propertyValuesByArbitraryProperty.entrySet()
				.stream()
				.filter(it -> isJacksonSerializableProperty(it.getKey().getObjectProperty().getProperty()))
				.forEach(
					entry -> {
						ArbitraryProperty arbitraryProperty = entry.getKey();
						Object value = entry.getValue();

						String resolvePropertyName = arbitraryProperty.getObjectProperty()
							.getResolvedPropertyName();

						if (value != null) {
							Object jsonFormatted = arbitraryProperty.getObjectProperty()
								.getProperty()
								.getAnnotation(JsonFormat.class)
								.map(it -> format(value, it))
								.orElse(value);

							JsonTypeInfo jsonTypeInfo = getJacksonAnnotation(property,
								JsonTypeInfo.class);
							if (jsonTypeInfo == null) {
								map.put(resolvePropertyName, jsonFormatted);
							} else {
								if (jsonTypeInfo.include() == As.WRAPPER_OBJECT) {
									String typeIdentifier = getJsonTypeInfoIdentifier(
										jsonTypeInfo,
										property
									);

									Map<String, Object> typeJson =
										(Map<String, Object>)map.getOrDefault(typeIdentifier, new HashMap<>());
									typeJson.put(resolvePropertyName, jsonFormatted);
									map.put(typeIdentifier, typeJson);
								}
							}
						}
					}
				);
			return map;
		};
	}

	private Map<String, Object> initializeMap(Property property) {
		Map<String, Object> defaultMap = new HashMap<>();

		JsonTypeInfo jsonTypeInfo = getJacksonAnnotation(property, JsonTypeInfo.class);
		if (jsonTypeInfo == null || jsonTypeInfo.include() == As.WRAPPER_OBJECT) {
			return defaultMap;
		}

		String jsonTypeInfoValue = getJsonTypeInfoIdentifier(jsonTypeInfo, property);
		String jsonTypeInfoPropertyName = getJsonTypeInfoPropertyName(jsonTypeInfo);

		defaultMap.put(jsonTypeInfoPropertyName, jsonTypeInfoValue);
		return defaultMap;
	}

	private String getJsonTypeInfoPropertyName(JsonTypeInfo jsonTypeInfo) {
		return "".equals(jsonTypeInfo.property())
			? jsonTypeInfo.use().getDefaultPropertyName()
			: jsonTypeInfo.property();
	}

	private String getJsonTypeInfoIdentifier(
		JsonTypeInfo jsonTypeInfo,
		Property property
	) {
		JsonTypeName jsonTypeName = getJacksonAnnotation(property, JsonTypeName.class);
		Class<?> type = Types.getActualType(property.getType());

		Id id = jsonTypeInfo.use();
		String jsonTypeInfoValue;
		switch (id) {
			case NAME:
				if (jsonTypeName != null) {
					jsonTypeInfoValue = jsonTypeName.value();
				} else {
					jsonTypeInfoValue = type.getSimpleName();
				}
				break;
			case CLASS:
				jsonTypeInfoValue = type.getName();
				break;
			default:
				throw new IllegalArgumentException("Unsupported JsonTypeInfo Id : " + id.name());
		}
		return jsonTypeInfoValue;
	}

	private Object format(Object object, JsonFormat jsonFormat) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(jsonFormat.pattern())
			.withZone(ZoneId.systemDefault());

		if (object instanceof TemporalAccessor temporalAccessor) {
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

	private boolean isJacksonSerializableProperty(Property property) {
		if (property instanceof CompositeProperty compositeProperty) {
			return isJacksonSerializableProperty(compositeProperty.getPrimaryProperty())
				|| isJacksonSerializableProperty(compositeProperty.getSecondaryProperty());
		}

		return property instanceof FieldProperty
			|| property instanceof PropertyDescriptorProperty
			|| property instanceof ConstructorProperty;
	}
}
