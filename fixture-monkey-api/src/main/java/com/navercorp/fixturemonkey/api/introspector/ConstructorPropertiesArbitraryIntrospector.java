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

package com.navercorp.fixturemonkey.api.introspector;

import java.beans.ConstructorProperties;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorLoggingContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.property.ConstructorParameterPropertyGenerator;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.type.Reflections;
import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.2", status = Status.MAINTAINED)
public final class ConstructorPropertiesArbitraryIntrospector implements ArbitraryIntrospector {
	public static final ConstructorPropertiesArbitraryIntrospector INSTANCE =
		new ConstructorPropertiesArbitraryIntrospector();
	public static final PropertyGenerator PROPERTY_GENERATOR = new ConstructorParameterPropertyGenerator(
		it -> it.getAnnotation(ConstructorProperties.class) != null
			|| Arrays.stream(it.getParameters()).anyMatch(Parameter::isNamePresent)
			|| it.getParameters().length == 0,
		it -> true
	);

	private static final Logger LOGGER = LoggerFactory.getLogger(ConstructorPropertiesArbitraryIntrospector.class);

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Property property = context.getResolvedProperty();
		Class<?> type = Types.getActualType(property.getType());
		if (Modifier.isAbstract(type.getModifiers())) {
			return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
		}

		Entry<Constructor<?>, String[]> parameterNamesByConstructor = TypeCache.getParameterNamesByConstructor(type);
		if (parameterNamesByConstructor == null) {
			ArbitraryGeneratorLoggingContext loggingContext = context.getLoggingContext();
			if (loggingContext.isEnableLoggingFail()) {
				LOGGER.warn(
					"Given type {} is failed to generate due to the exception. It may be null.",
					type,
					new IllegalArgumentException("Primary Constructor does not exist. type " + type.getSimpleName())
				);
			}
			return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
		}

		Constructor<?> primaryConstructor = parameterNamesByConstructor.getKey();
		String[] parameterNames = parameterNamesByConstructor.getValue();

		return new ArbitraryIntrospectorResult(
			CombinableArbitrary.objectBuilder()
				.properties(context.getCombinableArbitrariesByArbitraryProperty())
				.build(combine(primaryConstructor, parameterNames))
		);
	}

	@SuppressWarnings({"argument", "return"})
	private static Function<Map<ArbitraryProperty, Object>, Object> combine(
		Constructor<?> primaryConstructor,
		String[] parameterNames
	) {
		int parameterSize = parameterNames.length;

		return propertyValuesByArbitraryProperty -> {
			Map<String, Object> valuesByResolvedName = new HashMap<>();

			propertyValuesByArbitraryProperty.forEach(
				(key, value) -> valuesByResolvedName.put(
					key.getObjectProperty().getResolvedPropertyName(),
					value)
			);

			List<Object> list = new ArrayList<>(parameterSize);

			for (String parameterName : parameterNames) {
				Object combined = valuesByResolvedName.getOrDefault(
					parameterName,
					null
				);

				list.add(combined);
			}
			return Reflections.newInstance(primaryConstructor, list.toArray());
		};
	}

	@Override
	public PropertyGenerator getRequiredPropertyGenerator(Property property) {
		return PROPERTY_GENERATOR;
	}
}
