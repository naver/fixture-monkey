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

import java.lang.reflect.Field;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Builders;
import net.jqwik.api.Builders.BuilderCombinator;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.navercorp.fixturemonkey.api.property.FieldProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryType;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizers;
import com.navercorp.fixturemonkey.generator.AbstractArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.generator.FieldArbitraries;
import com.navercorp.fixturemonkey.jackson.FixtureMonkeyJackson;
import com.navercorp.fixturemonkey.jackson.property.JacksonPropertyNameResolver;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class JacksonArbitraryGenerator extends AbstractArbitraryGenerator {
	public static final JacksonArbitraryGenerator INSTANCE = new JacksonArbitraryGenerator();

	private final ObjectMapper objectMapper;
	private final ArbitraryCustomizers arbitraryCustomizers;

	private final PropertyNameResolver propertyNameResolver = new JacksonPropertyNameResolver();

	public JacksonArbitraryGenerator() {
		this(FixtureMonkeyJackson.defaultObjectMapper(), new ArbitraryCustomizers());
	}

	public JacksonArbitraryGenerator(ObjectMapper objectMapper) {
		this(objectMapper, new ArbitraryCustomizers());
	}

	private JacksonArbitraryGenerator(ObjectMapper objectMapper, ArbitraryCustomizers arbitraryCustomizers) {
		if (objectMapper.isEnabled(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)) {
			this.objectMapper = objectMapper.copy().configure(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS, false);
		} else {
			this.objectMapper = objectMapper;
		}
		this.arbitraryCustomizers = arbitraryCustomizers;
	}

	@Override
	protected <T> Arbitrary<T> generateObject(ArbitraryType type, List<ArbitraryNode> nodes) {
		FieldArbitraries fieldArbitraries = new FieldArbitraries(
			toArbitrariesByFieldName(nodes, ArbitraryNode::getPropertyName, this::formatValue)
		);

		this.arbitraryCustomizers.customizeFields(type.getType(), fieldArbitraries);

		BuilderCombinator<Map<String, Object>> builderCombinator = Builders.withBuilder(HashMap::new);
		for (Map.Entry<String, Arbitrary> entry : fieldArbitraries.entrySet()) {
			String fieldName = entry.getKey();
			Arbitrary<?> parameterArbitrary = entry.getValue();
			builderCombinator = builderCombinator.use(parameterArbitrary).in((map, value) -> {
				if (value != null) {
					map.put(fieldName, value);
				}
				return map;
			});
		}

		return (Arbitrary<T>)builderCombinator.build(
			map -> {
				Class clazz = type.getType();
				T fixture = (T)objectMapper.convertValue(map, clazz);

				return this.arbitraryCustomizers.customizeFixture(clazz, fixture);
			});
	}

	private <T> Arbitrary<T> formatValue(ArbitraryNode<T> node, Arbitrary<T> arbitrary) {
		ArbitraryType<T> nodeType = node.getType();
		JsonFormat jsonFormat = nodeType.getAnnotation(JsonFormat.class);
		if (jsonFormat != null) {
			return (Arbitrary<T>)arbitrary.map(it -> format(it, jsonFormat));
		}
		return arbitrary;
	}

	private Object format(Object object, JsonFormat jsonFormat) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(jsonFormat.pattern());
		if (object instanceof TemporalAccessor) {
			TemporalAccessor temporalAccessor = (TemporalAccessor)object;
			return dateTimeFormatter.format(temporalAccessor);
		} else if (object instanceof Date) {
			TemporalAccessor dateTemporalAccessor = ((Date)object).toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
			return dateTimeFormatter.format(dateTemporalAccessor);
		} else if (object instanceof Enum && jsonFormat.shape().isNumeric()) {
			return ((Enum)object).ordinal();
		} else {
			return object;
		}
	}

	@Override
	public ArbitraryGenerator withFixtureCustomizers(ArbitraryCustomizers arbitraryCustomizers) {
		if (this.arbitraryCustomizers == arbitraryCustomizers) {
			return this;
		}

		return new JacksonArbitraryGenerator(objectMapper, arbitraryCustomizers);
	}

	@Override
	public String resolveFieldName(Field field) {
		return this.propertyNameResolver.resolve(new FieldProperty(field));
	}

	@Override
	public String resolvePropertyName(Property property) {
		return propertyNameResolver.resolve(property);
	}
}

