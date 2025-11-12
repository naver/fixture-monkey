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

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
public final class BeanArbitraryIntrospector implements ArbitraryIntrospector {
	public static final BeanArbitraryIntrospector INSTANCE = new BeanArbitraryIntrospector();

	private static final Logger LOGGER = LoggerFactory.getLogger(BeanArbitraryIntrospector.class);

	@Override
	@SuppressWarnings({"argument", "methodref.return", ""})
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

		Map<String, PropertyDescriptor> propertyDescriptorsByPropertyName =
			TypeCache.getPropertyDescriptorsByPropertyName(type);
		return new ArbitraryIntrospectorResult(
			new CombinableArbitraryDelegator<>(
				CombinableArbitrary.objectBuilder()
					.properties(arbitrariesByArbitraryProperty)
					.build(combine(generated::combined, propertyDescriptorsByPropertyName))
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

	@SuppressWarnings({"dereference.of.nullable", "argument"})
	private Function<Map<ArbitraryProperty, Object>, Object> combine(
		Supplier<Object> instance,
		Map<String, PropertyDescriptor> propertyDescriptorsByPropertyName
	) {
		return propertyValuesByArbitraryProperty -> {
			Object object = instance.get();
			propertyValuesByArbitraryProperty.forEach(
				(arbitraryProperty, value) -> {
					Property property = arbitraryProperty.getObjectProperty().getProperty();

					String originPropertyName = property.getName();
					PropertyDescriptor propertyDescriptor = propertyDescriptorsByPropertyName.get(originPropertyName);
					Method writeMethod = propertyDescriptor.getWriteMethod();
					try {
						if (value != null) {
							writeMethod.invoke(object, value);
						}
					} catch (IllegalAccessException | InvocationTargetException ex) {
						LOGGER.warn("set bean property is failed. name: {} value: {}",
							writeMethod.getName(),
							value,
							ex);
					} catch (NullPointerException ex) {
						LOGGER.warn("The '{}' property '{}' may not have a setter",
							object.getClass().getName(),
							originPropertyName,
							ex);
					}
				}
			);
			return object;
		};
	}

	@Override
	public PropertyGenerator getRequiredPropertyGenerator(Property property) {
		return FIELD_METHOD_PROPERTY_GENERATOR;
	}
}
