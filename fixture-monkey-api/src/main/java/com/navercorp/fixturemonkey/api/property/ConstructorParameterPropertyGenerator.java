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

import static com.navercorp.fixturemonkey.api.instantiator.InstantiatorUtils.resolvedParameterNames;

import java.beans.ConstructorProperties;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.JvmTypes;

/**
 * Generates properties representing constructor parameters.
 * It might be a field as well.
 */
@API(since = "0.5.3", status = Status.EXPERIMENTAL)
public final class ConstructorParameterPropertyGenerator implements PropertyGenerator {
	private final Function<Property, List<Constructor<?>>> constructorsSupplier;
	private final Predicate<Constructor<?>> constructorPredicate;
	private final Matcher matcher;

	public ConstructorParameterPropertyGenerator(
		Predicate<Constructor<?>> constructorPredicate,
		Matcher matcher
	) {
		this.constructorsSupplier = p -> TypeCache.getDeclaredConstructors(p.getJvmType().getRawType());
		this.constructorPredicate = constructorPredicate;
		this.matcher = matcher;
	}

	public ConstructorParameterPropertyGenerator(
		Function<Property, List<Constructor<?>>> constructorsSupplier,
		Predicate<Constructor<?>> constructorPredicate,
		Matcher matcher
	) {
		this.constructorsSupplier = constructorsSupplier;
		this.constructorPredicate = constructorPredicate;
		this.matcher = matcher;
	}

	@Override
	public List<Property> generateChildProperties(Property property) {
		Constructor<?> declaredConstructor = constructorsSupplier.apply(property).stream()
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

	@SuppressWarnings("dereference.of.nullable")
	public List<Property> generateParameterProperties(ConstructorPropertyGeneratorContext context) {
		Property property = context.getProperty();

		JvmType parentJvmType = property.getJvmType();
		Class<?> type = parentJvmType.getRawType();
		Constructor<?> constructor = context.getConstructor();

		List<String> parameterNamesByConstructor = Arrays.asList(getParameterNames(constructor));
		List<@Nullable String> inputParameterNames = context.getInputParameterNames();
		Parameter[] parameters = constructor.getParameters();
		List<JvmType> resolvedParameterJvmTypes = Arrays.stream(parameters)
			.map(p -> JvmTypes.resolveJvmType(
				parentJvmType,
				p.getParameterizedType(),
				Arrays.asList(p.getAnnotations())
			))
			.collect(Collectors.toList());

		List<String> resolvedParameterNames = resolvedParameterNames(parameterNamesByConstructor, inputParameterNames);
		List<JvmType> resolvedTypes = resolveParameterJvmTypes(resolvedParameterJvmTypes, context.getInputParameterTypes());

		Map<String, Field> fieldsByName = TypeCache.getFieldsByName(type);
		boolean anyReceiverParameter = constructor.getAnnotatedReceiverType() != null
			&& Arrays.stream(constructor.getAnnotatedParameterTypes())
				.anyMatch(it -> it.getType().equals(constructor.getAnnotatedReceiverType().getType()));
		int parameterSize = parameters.length;

		Map<String, Property> constructorPropertiesByName = new LinkedHashMap<>();
		if (anyReceiverParameter) {
			Parameter receiverParameter = parameters[0];
			JvmType receiverJvmType = JvmTypes.resolveJvmType(
				parentJvmType,
				receiverParameter.getParameterizedType(),
				Arrays.asList(receiverParameter.getAnnotations())
			);
			constructorPropertiesByName.put(
				receiverParameter.getName(),
				new ConstructorProperty(
					new TypeNameProperty(receiverJvmType, receiverParameter.getName(), null),
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
				JvmTypes.resolveJvmType(
					parentJvmType,
					field.getGenericType(),
					Arrays.asList(field.getAnnotations())
				),
				field,
				null
			)
				: null;

			JvmType parameterJvmType = resolvedTypes.get(i);
			if (isUnresolvedGenericType(parameters[i].getParameterizedType()) && fieldProperty != null) {
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
						new TypeNameProperty(parameterJvmType, parameterName, null),
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

	private static List<JvmType> resolveParameterJvmTypes(
		List<JvmType> defaults,
		List<TypeReference<?>> inputParameterTypes
	) {
		List<JvmType> resolved = new java.util.ArrayList<>();
		for (int i = 0; i < defaults.size(); i++) {
			if (inputParameterTypes.size() > i) {
				resolved.add(Types.toJvmType(
					inputParameterTypes.get(i).getAnnotatedType(),
					Collections.emptyList()
				));
			} else {
				resolved.add(defaults.get(i));
			}
		}
		return resolved;
	}

	private static boolean isUnresolvedGenericType(Type type) {
		return type instanceof TypeVariable || type instanceof GenericArrayType;
	}
}
