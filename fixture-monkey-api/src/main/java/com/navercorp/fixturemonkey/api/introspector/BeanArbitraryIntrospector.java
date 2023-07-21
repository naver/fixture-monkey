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

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Reflections;
import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class BeanArbitraryIntrospector implements ArbitraryIntrospector {
	public static final BeanArbitraryIntrospector INSTANCE = new BeanArbitraryIntrospector();
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Property property = context.getResolvedProperty();
		Class<?> type = Types.getActualType(property.getType());
		if (Modifier.isAbstract(type.getModifiers())) {
			return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
		}

		Map<ArbitraryProperty, CombinableArbitrary> arbitrariesByArbitraryProperty =
			context.getCombinableArbitrariesByArbitraryProperty();

		return new ArbitraryIntrospectorResult(
			CombinableArbitrary.objectBuilder()
				.properties(arbitrariesByArbitraryProperty)
				.build(combine(type))
		);
	}

	private Function<Map<ArbitraryProperty, Object>, Object> combine(Class<?> type) {
		Map<String, PropertyDescriptor> propertyDescriptors =
			TypeCache.getPropertyDescriptorsByPropertyName(type);

		return propertyValuesByArbitraryProperty -> {
			Object instance = Reflections.newInstance(type);
			propertyValuesByArbitraryProperty.forEach(
				(arbitraryProperty, value) -> {
					String originPropertyName = arbitraryProperty.getObjectProperty()
						.getProperty()
						.getName();
					PropertyDescriptor propertyDescriptor = propertyDescriptors.get(originPropertyName);
					Method writeMethod = propertyDescriptor.getWriteMethod();
					try {
						if (value != null) {
							writeMethod.invoke(instance, value);
						}
					} catch (IllegalAccessException | InvocationTargetException ex) {
						log.warn("set bean property is failed. name: {} value: {}",
							writeMethod.getName(),
							value,
							ex);
					}

				}
			);
			return instance;
		};
	}
}
