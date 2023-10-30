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

package com.navercorp.fixturemonkey.experimental;

import static com.navercorp.fixturemonkey.api.type.Types.getDeclaredConstructor;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Constructor;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.introspector.ConstructorArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorArbitraryIntrospector.ConstructorWithParameterNames;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.ConstructorPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyUtils;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.resolver.ArbitraryBuilderContext;

@API(since = "0.6.12", status = Status.EXPERIMENTAL)
public final class InstantiatorProcessor {
	private final FixtureMonkeyOptions fixtureMonkeyOptions;
	private final ArbitraryBuilderContext context;

	public InstantiatorProcessor(FixtureMonkeyOptions fixtureMonkeyOptions, ArbitraryBuilderContext context) {
		this.fixtureMonkeyOptions = fixtureMonkeyOptions;
		this.context = context;
	}

	public void process(TypeReference<?> typeReference, Instantiator instantiator) {
		if (instantiator instanceof ConstructorInstantiator) {
			initializeByConstructor(typeReference, (ConstructorInstantiator<?>)instantiator);
		}
	}

	private void initializeByConstructor(TypeReference<?> typeReference, ConstructorInstantiator<?> instantiator) {
		Class<?> type = Types.getActualType(typeReference.getType());
		List<TypeReference<?>> typeReferences = instantiator.getTypes();

		Class<?>[] arguments = typeReferences.stream()
			.map(it -> Types.getActualType(it.getType()))
			.toArray(Class[]::new);

		Constructor<?> declaredConstructor = getDeclaredConstructor(type, arguments);
		Property property = PropertyUtils.toProperty(typeReference);

		List<Property> constructorParameterProperties = fixtureMonkeyOptions.getConstructorPropertyGenerator()
			.generateParameterProperties(
				new ConstructorPropertyGeneratorContext(
					property,
					declaredConstructor,
					instantiator.getTypes(),
					instantiator.getParameterNames()
				)
			);

		List<String> parameterNames = constructorParameterProperties.stream()
			.map(Property::getName)
			.collect(toList());

		context.putArbitraryIntrospector(
			type,
			new ConstructorArbitraryIntrospector(
				new ConstructorWithParameterNames<>(declaredConstructor, parameterNames)
			)
		);

		context.putPropertyConfigurer(
			type,
			constructorParameterProperties
		);
	}
}
