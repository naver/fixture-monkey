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

package com.navercorp.fixturemonkey.api.property;

import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.fixturemonkey.api.type.Types;

/**
 * Generates properties representing constructor parameters.
 * It might be a field as well.
 */
@API(since = "0.5.3", status = Status.MAINTAINED)
public final class ConstructorParameterPropertyGenerator implements PropertyGenerator {
	private final Matcher matcher;

	public ConstructorParameterPropertyGenerator(Matcher matcher) {
		this.matcher = matcher;
	}

	@Override
	public List<Property> generateChildProperties(Property property) {
		Class<?> clazz = Types.getActualType(property.getType());

		Map<String, Property> constructorPropertiesByName = new HashMap<>();
		Map.Entry<Constructor<?>, String[]> parameterNamesByConstructor =
			TypeCache.getParameterNamesByConstructor(clazz);
		if (parameterNamesByConstructor == null) {
			return Collections.emptyList();
		}

		Constructor<?> primaryConstructor = parameterNamesByConstructor.getKey();
		String[] parameterNames = parameterNamesByConstructor.getValue();
		AnnotatedType[] annotatedParameterTypes = primaryConstructor.getAnnotatedParameterTypes();

		Map<String, Field> fieldsByName = TypeCache.getFieldsByName(clazz);
		int parameterSize = parameterNames.length;
		for (int i = 0; i < parameterSize; i++) {
			AnnotatedType annotatedParameterType = annotatedParameterTypes[i];
			String parameterName = parameterNames[i];
			Field field = fieldsByName.get(parameterName);
			Property fieldProperty = field != null
				? new FieldProperty(
				Types.resolveWithTypeReferenceGenerics(property.getAnnotatedType(), field.getAnnotatedType()),
				field
			)
				: null;

			if (isGenericAnnotatedType(annotatedParameterType) && fieldProperty != null) {
				constructorPropertiesByName.put(
					parameterName,
					new ConstructorProperty(
						fieldProperty.getAnnotatedType(),
						primaryConstructor,
						parameterName,
						fieldProperty
					)
				);
			} else {
				constructorPropertiesByName.put(
					parameterName,
					new ConstructorProperty(
						annotatedParameterType,
						primaryConstructor,
						parameterName,
						fieldProperty
					)
				);
			}
		}
		return Collections.unmodifiableMap(constructorPropertiesByName).values().stream()
			.filter(matcher::match)
			.collect(Collectors.toList());
	}

	private static boolean isGenericAnnotatedType(AnnotatedType annotatedType) {
		return annotatedType instanceof AnnotatedTypeVariable || annotatedType instanceof AnnotatedArrayType;
	}
}
