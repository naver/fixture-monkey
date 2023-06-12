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

import static com.navercorp.fixturemonkey.api.property.PropertyCache.getParameterNamesByConstructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ObjectCombinableArbitrary;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Reflections;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.2", status = Status.MAINTAINED)
public final class ConstructorPropertiesArbitraryIntrospector implements ArbitraryIntrospector {
	public static final ConstructorPropertiesArbitraryIntrospector INSTANCE =
		new ConstructorPropertiesArbitraryIntrospector();

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Property property = context.getResolvedProperty();
		Class<?> type = Types.getActualType(property.getType());
		if (Modifier.isAbstract(type.getModifiers())) {
			return ArbitraryIntrospectorResult.EMPTY;
		}

		Entry<Constructor<?>, String[]> parameterNamesByConstructor = getParameterNamesByConstructor(type);
		if (parameterNamesByConstructor == null) {
			throw new IllegalArgumentException(
				"Primary Constructor does not exist. type " + type.getSimpleName());
		}

		Constructor<?> primaryConstructor = parameterNamesByConstructor.getKey();
		String[] parameterNames = parameterNamesByConstructor.getValue();
		int parameterSize = parameterNames.length;

		return new ArbitraryIntrospectorResult(
			new ObjectCombinableArbitrary(
				context.getCombinableArbitrariesByArbitraryProperty(),
				propertyValuesByArbitraryProperty -> {
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
				}
			)
		);
	}
}
