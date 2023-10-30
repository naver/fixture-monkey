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

package com.navercorp.fixturemonkey.api.experimental;

import static com.navercorp.fixturemonkey.api.experimental.InstantiatorUtils.resolveParameterTypes;
import static com.navercorp.fixturemonkey.api.experimental.InstantiatorUtils.resolvedParameterNames;
import static com.navercorp.fixturemonkey.api.type.Types.getDeclaredConstructor;
import static com.navercorp.fixturemonkey.api.type.Types.isAssignableTypes;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorArbitraryIntrospector.ConstructorWithParameterNames;
import com.navercorp.fixturemonkey.api.introspector.FactoryMethodArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FactoryMethodArbitraryIntrospector.FactoryMethodWithParameterNames;
import com.navercorp.fixturemonkey.api.introspector.FailoverIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.property.CompositePropertyGenerator;
import com.navercorp.fixturemonkey.api.property.ConstructorParameterPropertyGenerator;
import com.navercorp.fixturemonkey.api.property.ConstructorPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.property.FieldPropertyGenerator;
import com.navercorp.fixturemonkey.api.property.JavaBeansPropertyGenerator;
import com.navercorp.fixturemonkey.api.property.MethodParameterProperty;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.property.PropertyUtils;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.6.12", status = Status.EXPERIMENTAL)
public final class JavaInstantiatorProcessor implements InstantiatorProcessor {
	private static final ConstructorParameterPropertyGenerator JAVA_CONSTRUCTOR_PROPERTY_GENERATOR =
		new ConstructorParameterPropertyGenerator(
			it -> true,
			it -> true
		);
	private static final PropertyGenerator JAVA_FIELD_AND_BEANS_PROPERTY_GENERATOR = new CompositePropertyGenerator(
		Arrays.asList(
			new FieldPropertyGenerator(it -> true, it -> true),
			new JavaBeansPropertyGenerator(it -> it.getReadMethod() != null && it.getWriteMethod() != null, it -> true)
		)
	);

	@Override
	public InstantiatorProcessResult process(TypeReference<?> typeReference, Instantiator instantiator) {
		if (instantiator instanceof ConstructorInstantiator) {
			return processConstructor(typeReference, (ConstructorInstantiator<?>)instantiator);
		} else if (instantiator instanceof FactoryMethodInstantiator) {
			return processFactoryMethod(typeReference, (FactoryMethodInstantiator<?>)instantiator);
		} else if (instantiator instanceof PropertyInstantiator) {
			return processProperty(typeReference, (PropertyInstantiator<?>)instantiator);
		}
		throw new IllegalArgumentException("Given instantiator is not valid. instantiator: " + instantiator.getClass());
	}

	public InstantiatorProcessResult processConstructor(
		TypeReference<?> typeReference,
		ConstructorInstantiator<?> instantiator
	) {
		Class<?> type = Types.getActualType(typeReference.getType());
		List<TypeReference<?>> typeReferences = instantiator.getInputParameterTypes();

		Class<?>[] arguments = typeReferences.stream()
			.map(it -> Types.getActualType(it.getType()))
			.toArray(Class[]::new);

		Constructor<?> declaredConstructor = getDeclaredConstructor(type, arguments);
		Property property = PropertyUtils.toProperty(typeReference);

		List<Property> constructorParameterProperties = JAVA_CONSTRUCTOR_PROPERTY_GENERATOR.generateParameterProperties(
			new ConstructorPropertyGeneratorContext(
				property,
				declaredConstructor,
				instantiator.getInputParameterTypes(),
				instantiator.getInputParameterNames()
			)
		);

		List<String> parameterNames = constructorParameterProperties.stream()
			.map(Property::getName)
			.collect(toList());

		return new InstantiatorProcessResult(
			new ConstructorArbitraryIntrospector(
				new ConstructorWithParameterNames<>(declaredConstructor, parameterNames)
			),
			constructorParameterProperties
		);
	}

	private InstantiatorProcessResult processFactoryMethod(
		TypeReference<?> typeReference,
		FactoryMethodInstantiator<?> instantiator
	) {
		Class<?> type = Types.getActualType(typeReference.getType());
		List<TypeReference<?>> inputTypeReferences = instantiator.getInputParameterTypes();
		List<String> inputParameterNames = instantiator.getInputParameterNames();

		Class<?>[] inputParameterTypes = inputTypeReferences.stream()
			.map(it -> Types.getActualType(it.getType()))
			.toArray(Class[]::new);

		Method factoryMethod = Arrays.stream(type.getDeclaredMethods())
			.filter(it -> Modifier.isStatic(it.getModifiers()))
			.filter(it -> isAssignableTypes(inputParameterTypes, it.getParameterTypes()))
			.findAny()
			.orElse(null);

		if (factoryMethod == null) {
			throw new IllegalArgumentException(
				"Given type method is not exists. type: " + type + " inputParameterTypes: "
					+ Arrays.toString(inputParameterTypes)
			);
		}

		List<Property> properties = getMethodParameterProperties(
			factoryMethod,
			inputParameterNames,
			inputTypeReferences
		);
		List<String> parameterNames = properties.stream()
			.map(Property::getName)
			.collect(toList());

		return new InstantiatorProcessResult(
			new FactoryMethodArbitraryIntrospector(
				new FactoryMethodWithParameterNames(factoryMethod, parameterNames)
			),
			properties
		);
	}

	private static List<Property> getMethodParameterProperties(
		Method factoryMethod,
		List<String> inputParameterNames,
		List<TypeReference<?>> inputTypeReferences
	) {
		Parameter[] parameters = factoryMethod.getParameters();

		List<TypeReference<?>> methodParameterTypeReferences = Arrays.stream(parameters)
			.map(it -> Types.toTypeReference(it.getAnnotatedType()))
			.collect(toList());
		List<String> methodParameterNames = Arrays.stream(parameters).map(Parameter::getName).collect(toList());

		List<TypeReference<?>> resolvedParameterTypes = resolveParameterTypes(
			methodParameterTypeReferences,
			inputTypeReferences
		);
		List<String> resolvedParameterNames = resolvedParameterNames(
			methodParameterNames,
			inputParameterNames
		);

		List<Property> properties = new ArrayList<>();
		for (int i = 0; i < parameters.length; i++) {
			String resolvedParameterName = resolvedParameterNames.get(i);
			TypeReference<?> resolvedTypeReference = resolvedParameterTypes.get(i);
			properties.add(
				new MethodParameterProperty(
					resolvedTypeReference.getAnnotatedType(),
					resolvedParameterName,
					null
				)
			);
		}
		return properties;
	}

	public InstantiatorProcessResult processProperty(
		TypeReference<?> typeReference,
		PropertyInstantiator<?> instantiator
	) {
		Property property = PropertyUtils.toProperty(typeReference);

		List<Property> properties = JAVA_FIELD_AND_BEANS_PROPERTY_GENERATOR.generateChildProperties(property);

		return new InstantiatorProcessResult(
			new FailoverIntrospector(
				Arrays.asList(
					BeanArbitraryIntrospector.INSTANCE,
					FieldReflectionArbitraryIntrospector.INSTANCE
				)
			),
			properties
		);
	}
}
