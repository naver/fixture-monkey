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

package com.navercorp.fixturemonkey.api.instantiator;

import static com.navercorp.fixturemonkey.api.instantiator.InstantiatorUtils.resolveParameterTypes;
import static com.navercorp.fixturemonkey.api.instantiator.InstantiatorUtils.resolvedParameterNames;
import static com.navercorp.fixturemonkey.api.type.Types.isAssignableTypes;
import static java.util.stream.Collectors.toList;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.CompositeArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorArbitraryIntrospector.ConstructorWithParameterNames;
import com.navercorp.fixturemonkey.api.introspector.FactoryMethodArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FactoryMethodArbitraryIntrospector.FactoryMethodWithParameterNames;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.property.ConstructorParameterPropertyGenerator;
import com.navercorp.fixturemonkey.api.property.ConstructorPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.property.FieldPropertyGenerator;
import com.navercorp.fixturemonkey.api.property.JavaBeansPropertyGenerator;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyUtils;
import com.navercorp.fixturemonkey.api.property.TypeNameProperty;
import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.6.12", status = Status.MAINTAINED)
public final class JavaInstantiatorProcessor implements InstantiatorProcessor {
	private static final ConstructorParameterPropertyGenerator JAVA_CONSTRUCTOR_PROPERTY_GENERATOR =
		new ConstructorParameterPropertyGenerator(
			it -> true,
			it -> true
		);

	@Override
	public InstantiatorProcessResult process(TypeReference<?> typeReference, Instantiator instantiator) {
		if (instantiator instanceof ConstructorInstantiator) {
			return processConstructor(typeReference, (ConstructorInstantiator<?>)instantiator);
		} else if (instantiator instanceof FactoryMethodInstantiator) {
			return processFactoryMethod(typeReference, (FactoryMethodInstantiator<?>)instantiator);
		} else if (instantiator instanceof JavaFieldPropertyInstantiator) {
			return processField(typeReference, (JavaFieldPropertyInstantiator<?>)instantiator);
		} else if (instantiator instanceof JavaBeansPropertyInstantiator) {
			return processJavaBeansProperty(typeReference, (JavaBeansPropertyInstantiator<?>)instantiator);
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

		Constructor<?> declaredConstructor;
		try {
			declaredConstructor = TypeCache.getDeclaredConstructor(type, arguments);
		} catch (IllegalArgumentException ex) {
			declaredConstructor = TypeCache.getDeclaredConstructors(type).get(0);
		}

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

		ConstructorArbitraryIntrospector constructorArbitraryIntrospector = new ConstructorArbitraryIntrospector(
			new ConstructorWithParameterNames<>(declaredConstructor, parameterNames)
		);

		PropertyInstantiator<?> propertyInstantiator = instantiator.getPropertyInstantiator();
		if (propertyInstantiator != null) {
			return processPropertyInstantiator(
				typeReference,
				propertyInstantiator,
				constructorArbitraryIntrospector,
				constructorParameterProperties
			);
		}

		return new InstantiatorProcessResult(
			constructorArbitraryIntrospector,
			constructorParameterProperties
		);
	}

	private InstantiatorProcessResult processFactoryMethod(
		TypeReference<?> typeReference,
		FactoryMethodInstantiator<?> instantiator
	) {
		Class<?> type = Types.getActualType(typeReference.getType());
		String factoryMethodName = instantiator.getFactoryMethodName();
		List<TypeReference<?>> inputTypeReferences = instantiator.getInputParameterTypes();
		List<String> inputParameterNames = instantiator.getInputParameterNames();

		Class<?>[] inputParameterTypes = inputTypeReferences.stream()
			.map(it -> Types.getActualType(it.getType()))
			.toArray(Class[]::new);

		Method factoryMethod = Arrays.stream(type.getDeclaredMethods())
			.filter(it -> it.getName().equals(factoryMethodName))
			.filter(it -> Modifier.isStatic(it.getModifiers()))
			.filter(
				it -> inputParameterTypes.length == 0 || isAssignableTypes(inputParameterTypes, it.getParameterTypes())
			)
			.findAny()
			.orElse(null);

		if (factoryMethod == null) {
			throw new IllegalArgumentException(
				"Given type method is not exists."
					+ " name: " + factoryMethodName
					+ " type: " + type
					+ " inputParameterTypes: " + Arrays.toString(inputParameterTypes)
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

		FactoryMethodArbitraryIntrospector factoryMethodArbitraryIntrospector = new FactoryMethodArbitraryIntrospector(
			new FactoryMethodWithParameterNames(factoryMethod, parameterNames)
		);

		PropertyInstantiator<?> propertyInstantiator = instantiator.getPropertyInstantiator();
		if (propertyInstantiator != null) {
			return processPropertyInstantiator(
				typeReference,
				propertyInstantiator,
				factoryMethodArbitraryIntrospector,
				properties
			);
		}

		return new InstantiatorProcessResult(
			factoryMethodArbitraryIntrospector,
			properties
		);
	}

	private InstantiatorProcessResult processPropertyInstantiator(
		TypeReference<?> typeReference,
		PropertyInstantiator<?> propertyInstantiator,
		ArbitraryIntrospector formerArbitraryIntrospector,
		List<Property> formerProperties
	) {
		InstantiatorProcessResult propertyInstantiatorProcessResult =
			this.process(typeReference, propertyInstantiator);

		List<Property> resolvedProperties = new ArrayList<>(formerProperties);
		resolvedProperties.addAll(propertyInstantiatorProcessResult.getProperties());

		return new InstantiatorProcessResult(
			new CompositeArbitraryIntrospector(
				Arrays.asList(
					formerArbitraryIntrospector,
					propertyInstantiatorProcessResult.getIntrospector()
				)
			),
			resolvedProperties
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
				new TypeNameProperty(
					resolvedTypeReference.getAnnotatedType(),
					resolvedParameterName,
					null
				)
			);
		}
		return properties;
	}

	public InstantiatorProcessResult processField(
		TypeReference<?> typeReference,
		JavaFieldPropertyInstantiator<?> instantiator
	) {
		Property property = PropertyUtils.toProperty(typeReference);

		Predicate<Field> fieldPredicate = instantiator.getFieldPredicate();
		List<Property> properties =
			new FieldPropertyGenerator(fieldPredicate, it -> true).generateChildProperties(property);

		return new InstantiatorProcessResult(
			FieldReflectionArbitraryIntrospector.INSTANCE,
			properties
		);
	}

	public InstantiatorProcessResult processJavaBeansProperty(
		TypeReference<?> typeReference,
		JavaBeansPropertyInstantiator<?> instantiator
	) {
		Property property = PropertyUtils.toProperty(typeReference);

		Predicate<PropertyDescriptor> propertyDescriptorPredicate = instantiator.getPropertyDescriptorPredicate();
		List<Property> properties = new JavaBeansPropertyGenerator(propertyDescriptorPredicate, it -> true)
			.generateChildProperties(property);

		return new InstantiatorProcessResult(
			FieldReflectionArbitraryIntrospector.INSTANCE,
			properties
		);
	}
}
