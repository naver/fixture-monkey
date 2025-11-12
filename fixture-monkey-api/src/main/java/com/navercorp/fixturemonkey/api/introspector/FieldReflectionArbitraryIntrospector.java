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

import static com.navercorp.fixturemonkey.api.exception.Exceptions.throwAsUnchecked;
import static com.navercorp.fixturemonkey.api.property.DefaultPropertyGenerator.FIELD_METHOD_PROPERTY_GENERATOR;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitraryDelegator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorLoggingContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.type.Reflections;
import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class FieldReflectionArbitraryIntrospector implements ArbitraryIntrospector {
	public static final FieldReflectionArbitraryIntrospector INSTANCE = new FieldReflectionArbitraryIntrospector();
	private static final Logger LOGGER = LoggerFactory.getLogger(FieldReflectionArbitraryIntrospector.class);

	@SuppressWarnings("methodref.return")
	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Property property = context.getResolvedProperty();
		Class<?> type = Types.getActualType(property.getType());
		if (Modifier.isAbstract(type.getModifiers())) {
			return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
		}

		Map<ArbitraryProperty, CombinableArbitrary<?>> arbitrariesByArbitraryProperty =
			context.getCombinableArbitrariesByArbitraryProperty();

		CombinableArbitrary<?> generated = context.getGenerated();
		if (generated == CombinableArbitrary.NOT_GENERATED) {
			try {
				checkPrerequisite(type);
			} catch (Exception ex) {
				ArbitraryGeneratorLoggingContext loggingContext = context.getLoggingContext();
				if (loggingContext.isEnableLoggingFail()) {
					LOGGER.warn("Given type {} is failed to generate due to the exception. It may be null.", type, ex);
				}
				return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
			}
			generated = CombinableArbitrary.from(() -> Reflections.newInstance(type));
		}

		Map<String, Field> fieldsByPropertyName = TypeCache.getFieldsByName(type);
		return new ArbitraryIntrospectorResult(
			new CombinableArbitraryDelegator<>(
				CombinableArbitrary.objectBuilder()
					.properties(arbitrariesByArbitraryProperty)
					.build(combine(generated::combined, fieldsByPropertyName))
			)
		);
	}

	private void checkPrerequisite(Class<?> type) {
		try {
			TypeCache.getDeclaredConstructor(type);
		} catch (Exception ex) {
			throw throwAsUnchecked(ex);
		}
	}

	@SuppressWarnings("argument")
	private Function<Map<ArbitraryProperty, Object>, Object> combine(
		Supplier<Object> instance,
		Map<String, Field> fieldsByPropertyName
	) {
		return propertyValuesByArbitraryProperty -> {
			Object object = instance.get();
			propertyValuesByArbitraryProperty.forEach(
				(arbitraryProperty, value) -> {
					Property property = arbitraryProperty.getObjectProperty().getProperty();
					String originPropertyName = property.getName();
					Field field = fieldsByPropertyName.get(originPropertyName);

					if (field == null
						|| (Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers()))
						|| Modifier.isTransient(field.getModifiers())) {
						return;
					}

					String resolvePropertyName =
						arbitraryProperty.getObjectProperty().getResolvedPropertyName();

					try {
						if (value != null) {
							field.set(object, value);
						}
					} catch (IllegalAccessException | IllegalArgumentException ex) {
						LOGGER.warn("set field by reflection is failed. field: {} value: {}",
							resolvePropertyName,
							value,
							ex
						);
					}
				});

			return object;
		};
	}

	@Override
	public PropertyGenerator getRequiredPropertyGenerator(Property property) {
		return FIELD_METHOD_PROPERTY_GENERATOR;
	}
}
