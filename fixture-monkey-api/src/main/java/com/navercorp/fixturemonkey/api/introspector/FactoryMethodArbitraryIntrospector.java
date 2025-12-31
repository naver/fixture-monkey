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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.6.12", status = Status.MAINTAINED)
public final class FactoryMethodArbitraryIntrospector implements ArbitraryIntrospector {
	private final FactoryMethodWithParameterNames factoryMethodWithParameterNames;

	public FactoryMethodArbitraryIntrospector(FactoryMethodWithParameterNames factoryMethodWithParameterNames) {
		this.factoryMethodWithParameterNames = factoryMethodWithParameterNames;
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Property property = context.getResolvedProperty();
		Class<?> type = Types.getActualType(property.getType());
		if (Modifier.isAbstract(type.getModifiers())) {
			return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
		}

		return new ArbitraryIntrospectorResult(
			CombinableArbitrary.objectBuilder()
				.properties(context.getCombinableArbitrariesByArbitraryProperty())
				.build(
					combine(
						factoryMethodWithParameterNames.getFactoryMethod(),
						factoryMethodWithParameterNames.getParameterNames()
					)
				)
		);
	}

	@SuppressWarnings({"argument", "return"})
	private static Function<Map<ArbitraryProperty, Object>, Object> combine(
		Method factoryMethod,
		List<String> parameterNames
	) {
		int parameterSize = parameterNames.size();

		return propertyValuesByArbitraryProperty -> {
			Map<String, Object> valuesByPropertyName = new HashMap<>();

			propertyValuesByArbitraryProperty.forEach(
				(key, value) -> valuesByPropertyName.put(
					key.getObjectProperty().getProperty().getName(),
					value
				)
			);

			List<Object> list = new ArrayList<>(parameterSize);
			for (String parameterName : parameterNames) {
				Object combined = valuesByPropertyName.getOrDefault(
					parameterName,
					null
				);
				list.add(combined);
			}
			try {
				return factoryMethod.invoke(null, list.toArray());
			} catch (IllegalAccessException | InvocationTargetException ex) {
				throw new RuntimeException(ex);
			}
		};
	}

	public static class FactoryMethodWithParameterNames {
		private final Method factoryMethod;
		private final List<String> parameterNames;

		public FactoryMethodWithParameterNames(Method factoryMethod, List<String> parameterNames) {
			this.factoryMethod = factoryMethod;
			this.parameterNames = parameterNames;
		}

		public Method getFactoryMethod() {
			return factoryMethod;
		}

		public List<String> getParameterNames() {
			return parameterNames;
		}
	}
}
