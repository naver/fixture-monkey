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

import static com.navercorp.fixturemonkey.api.instantiator.InstantiatorUtils.resolveParameterTypes;
import static com.navercorp.fixturemonkey.api.instantiator.InstantiatorUtils.resolvedParameterNames;

import java.beans.ConstructorProperties;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.api.type.Types;

/**
 * Generates properties representing constructor parameters.
 * It might be a field as well.
 */
@API(since = "0.5.3", status = Status.MAINTAINED)
public final class ConstructorParameterPropertyGenerator implements PropertyGenerator {
	private final Predicate<Constructor<?>> constructorPredicate;
	private final Matcher matcher;

	public ConstructorParameterPropertyGenerator(
		Predicate<Constructor<?>> constructorPredicate,
		Matcher matcher
	) {
		this.constructorPredicate = constructorPredicate;
		this.matcher = matcher;
	}

	@Override
	public List<Property> generateChildProperties(Property property) {
		Class<?> clazz = Types.getActualType(property.getType());

		Constructor<?> declaredConstructor = TypeCache.getDeclaredConstructors(clazz).stream()
			.filter(constructorPredicate)
			.findFirst()
			.orElse(null);

		if (declaredConstructor == null) {
			return Collections.emptyList();
		}

		return generateParameterProperties(
			new ConstructorPropertyGeneratorContext(property, declaredConstructor)
		);
	}

	private static String[] getParameterNames(Constructor<?> constructor) {
		Parameter[] parameters = constructor.getParameters();
		boolean parameterEmpty = parameters.length == 0;
		if (parameterEmpty) {
			return new String[0];
		}

		ConstructorProperties constructorPropertiesAnnotation =
			constructor.getAnnotation(ConstructorProperties.class);

		if (constructorPropertiesAnnotation != null) {
			return constructorPropertiesAnnotation.value();
		} else {
			return Arrays.stream(parameters)
				.map(Parameter::getName)
				.toArray(String[]::new);
		}
	}

	public List<Property> generateParameterProperties(ConstructorPropertyGeneratorContext context) {
		Property property = context.getProperty();

		Class<?> type = Types.getActualType(property.getType());
		Constructor<?> constructor = context.getConstructor();

		Map<String, AnnotatedType> actualGenericTypesByTypeVariable =
			getGenericAnnotatedTypesByGenericTypeName(property);

		List<String> parameterNamesByConstructor = Arrays.asList(getParameterNames(constructor));
		List<String> inputParameterNames = context.getInputParameterNames();
		List<TypeReference<?>> typeReferencesByConstructor = Arrays.stream(constructor.getAnnotatedParameterTypes())
			.map(it -> actualGenericTypesByTypeVariable.getOrDefault(it.getType().getTypeName(), it))
			.map(ConstructorParameterPropertyGenerator::toTypeReference)
			.collect(Collectors.toList());

		List<String> resolvedParameterNames = resolvedParameterNames(parameterNamesByConstructor, inputParameterNames);
		List<TypeReference<?>> resolvedTypeReferences =
			resolveParameterTypes(typeReferencesByConstructor, context.getInputParameterTypes());

		Map<String, Field> fieldsByName = TypeCache.getFieldsByName(type);
		boolean anyReceiverParameter = Arrays.stream(constructor.getAnnotatedParameterTypes())
			.anyMatch(it -> constructor.getAnnotatedReceiverType() != null
				&& it.getType().equals(constructor.getAnnotatedReceiverType().getType()));
		int parameterSize = typeReferencesByConstructor.size();

		Map<String, Property> constructorPropertiesByName = new LinkedHashMap<>();
		if (anyReceiverParameter) {
			Parameter receiverParameter = constructor.getParameters()[0];
			constructorPropertiesByName.put(
				receiverParameter.getName(),
				new ConstructorProperty(
					new TypeNameProperty(receiverParameter.getAnnotatedType(), receiverParameter.getName(), null),
					constructor,
					null,
					null
				)
			);
		}
		int parameterStartIndex = anyReceiverParameter ? 1 : 0;
		for (int i = parameterStartIndex; i < parameterSize; i++) {
			int parameterNameIndex = anyReceiverParameter ? i - 1 : i;
			String parameterName = resolvedParameterNames.get(parameterNameIndex);
			Field field = fieldsByName.get(parameterName);
			Property fieldProperty = field != null
				? new FieldProperty(
				Types.resolveWithTypeReferenceGenerics(property.getAnnotatedType(), field.getAnnotatedType()),
				field
			)
				: null;

			AnnotatedType parameterAnnotatedType = resolvedTypeReferences.get(i).getAnnotatedType();
			if (isGenericAnnotatedType(parameterAnnotatedType) && fieldProperty != null) {
				constructorPropertiesByName.put(
					parameterName,
					new ConstructorProperty(
						fieldProperty,
						constructor,
						fieldProperty,
						null
					)
				);
			} else {
				constructorPropertiesByName.put(
					parameterName,
					new ConstructorProperty(
						new TypeNameProperty(parameterAnnotatedType, parameterName, null),
						constructor,
						fieldProperty,
						null
					)
				);
			}
		}
		return constructorPropertiesByName.values().stream()
			.filter(matcher::match)
			.collect(Collectors.toList());
	}

	private static TypeReference<Object> toTypeReference(AnnotatedType annotatedType) {
		return new TypeReference<Object>() {
			@Override
			public Type getType() {
				return annotatedType.getType();
			}

			@Override
			public AnnotatedType getAnnotatedType() {
				return annotatedType;
			}
		};
	}

	private static boolean isGenericAnnotatedType(AnnotatedType annotatedType) {
		return annotatedType instanceof AnnotatedTypeVariable || annotatedType instanceof AnnotatedArrayType;
	}

	private static Map<String, AnnotatedType> getGenericAnnotatedTypesByGenericTypeName(Property property) {
		Class<?> type = Types.getActualType(property.getType());

		List<AnnotatedType> genericsTypes = Types.getGenericsTypes(property.getAnnotatedType());
		List<TypeVariable<? extends Class<?>>> erasedTypeVariables = Arrays.asList(type.getTypeParameters());

		if (genericsTypes.size() != erasedTypeVariables.size()) {
			return Collections.emptyMap();
		}

		Map<String, AnnotatedType> actualGenericTypesByTypeName = new HashMap<>();
		for (int i = 0; i < genericsTypes.size(); i++) {
			AnnotatedType genericType = genericsTypes.get(i);
			TypeVariable<? extends Class<?>> erasedTypeVariable = erasedTypeVariables.get(i);
			actualGenericTypesByTypeName.put(erasedTypeVariable.getName(), genericType);
		}

		return actualGenericTypesByTypeName;
	}
}
