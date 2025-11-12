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

import static com.navercorp.fixturemonkey.api.type.TypeCache.getParameterNames;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.property.ConstructorParameterPropertyGenerator;
import com.navercorp.fixturemonkey.api.property.ConstructorPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.type.Reflections;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.6.12", status = Status.MAINTAINED)
public final class ConstructorArbitraryIntrospector implements ArbitraryIntrospector {
	private final ConstructorWithParameterNames<?> constructorWithParamNames;

	public ConstructorArbitraryIntrospector(ConstructorWithParameterNames<?> constructorWithParamNames) {
		this.constructorWithParamNames = constructorWithParamNames;
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Property property = context.getResolvedProperty();
		Class<?> type = Types.getActualType(property.getType());

		if (Modifier.isAbstract(type.getModifiers())) {
			return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
		}

		List<@Nullable String> parameterNames = constructorWithParamNames.getParameterNames().isEmpty()
			? Arrays.asList(getParameterNames(constructorWithParamNames.getConstructor()))
			: constructorWithParamNames.getParameterNames();

		return new ArbitraryIntrospectorResult(
			CombinableArbitrary.objectBuilder()
				.properties(context.getCombinableArbitrariesByArbitraryProperty())
				.build(
					combine(
						constructorWithParamNames.getConstructor(),
						parameterNames
					)
				)
		);
	}

	@Override
	public PropertyGenerator getRequiredPropertyGenerator(Property property) {
		ConstructorParameterPropertyGenerator propertyGenerator = new ConstructorParameterPropertyGenerator(
			it -> it.equals(constructorWithParamNames.constructor),
			it -> true
		);

		if (constructorWithParamNames.getParameterNames().isEmpty()) {
			return propertyGenerator;
		}

		List<TypeReference<?>> parameterTypes =
			Arrays.stream(constructorWithParamNames.constructor.getGenericParameterTypes())
				.map(it -> Types.toTypeReference(Types.generateAnnotatedTypeWithoutAnnotation(it)))
				.collect(Collectors.toList());

		return p -> propertyGenerator.generateParameterProperties(
			new ConstructorPropertyGeneratorContext(
				p,
				constructorWithParamNames.constructor,
				parameterTypes,
				constructorWithParamNames.parameterNames
			)
		);
	}

	@SuppressWarnings({"argument", "return"})
	private static Function<Map<ArbitraryProperty, Object>, Object> combine(
		Constructor<?> constructor,
		List<@Nullable String> parameterNames
	) {
		int parameterSize = parameterNames.size();

		return propertyValuesByArbitraryProperty -> {
			Map<String, Object> valuesByResolvedName = new HashMap<>();

			propertyValuesByArbitraryProperty.forEach(
				(key, value) -> valuesByResolvedName.put(
					key.getObjectProperty().getResolvedPropertyName(),
					value
				)
			);

			List<Object> list = new ArrayList<>(parameterSize);
			for (String parameterName : parameterNames) {
				Object combined = valuesByResolvedName.getOrDefault(
					parameterName,
					null
				);
				list.add(combined);
			}
			return Reflections.newInstance(constructor, list.toArray());
		};
	}

	public static class ConstructorWithParameterNames<T> {
		private final Constructor<T> constructor;
		private final List<@Nullable String> parameterNames;

		public ConstructorWithParameterNames(Constructor<T> constructor, List<@Nullable String> parameterNames) {
			this.constructor = constructor;
			this.parameterNames = parameterNames;
		}

		public Constructor<T> getConstructor() {
			return constructor;
		}

		public List<@Nullable String> getParameterNames() {
			return parameterNames;
		}
	}
}
