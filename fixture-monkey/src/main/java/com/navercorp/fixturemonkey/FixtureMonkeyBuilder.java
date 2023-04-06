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

package com.navercorp.fixturemonkey;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.context.MonkeyContext;
import com.navercorp.fixturemonkey.api.customizer.FixtureCustomizer;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.InterfaceObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.NullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.NullObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.PropertyGenerator;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.CompositeArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.JavaArbitraryResolver;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeArbitraryResolver;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.introspector.JavaTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.ExactTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.option.GenerateOptionsBuilder;
import com.navercorp.fixturemonkey.api.plugin.Plugin;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.api.validator.ArbitraryValidator;
import com.navercorp.fixturemonkey.expression.MonkeyExpressionFactory;
import com.navercorp.fixturemonkey.resolver.ArbitraryTraverser;
import com.navercorp.fixturemonkey.resolver.DecomposableContainerValue;
import com.navercorp.fixturemonkey.resolver.DecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.resolver.ManipulateOptions;
import com.navercorp.fixturemonkey.resolver.ManipulateOptionsBuilder;
import com.navercorp.fixturemonkey.resolver.ManipulatorOptimizer;
import com.navercorp.fixturemonkey.resolver.NoneManipulatorOptimizer;

@API(since = "0.4.0", status = Status.MAINTAINED)
public class FixtureMonkeyBuilder {
	private final GenerateOptionsBuilder generateOptionsBuilder = GenerateOptions.builder();
	private final ManipulateOptionsBuilder manipulateOptionsBuilder = ManipulateOptions.builder();
	private ManipulatorOptimizer manipulatorOptimizer = new NoneManipulatorOptimizer();
	private DecomposedContainerValueFactory defaultDecomposedContainerValueFactory = (obj) -> {
		throw new IllegalArgumentException("given type is not supported container : " + obj.getClass().getTypeName());
	};
	private final Map<Class<?>, DecomposedContainerValueFactory> decomposableContainerFactoryMap = new HashMap<>();

	public FixtureMonkeyBuilder pushPropertyGenerator(MatcherOperator<PropertyGenerator> propertyGenerator) {
		generateOptionsBuilder.insertFirstPropertyGenerator(propertyGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushAssignableTypePropertyGenerator(
		Class<?> type,
		PropertyGenerator propertyGenerator
	) {
		generateOptionsBuilder.insertFirstPropertyGenerator(type, propertyGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushExactTypePropertyGenerator(Class<?> type, PropertyGenerator propertyGenerator) {
		generateOptionsBuilder.insertFirstPropertyGenerator(
			MatcherOperator.assignableTypeMatchOperator(type, propertyGenerator)
		);
		return this;
	}

	public FixtureMonkeyBuilder manipulatorOptimizer(ManipulatorOptimizer manipulatorOptimizer) {
		this.manipulatorOptimizer = manipulatorOptimizer;
		return this;
	}

	@Deprecated
	public FixtureMonkeyBuilder monkeyExpressionFactory(MonkeyExpressionFactory monkeyExpressionFactory) {
		manipulateOptionsBuilder.monkeyExpressionFactory(monkeyExpressionFactory);
		return this;
	}

	public FixtureMonkeyBuilder defaultObjectPropertyGenerator(
		ObjectPropertyGenerator objectPropertyGenerator
	) {
		generateOptionsBuilder.defaultObjectPropertyGenerator(objectPropertyGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushAssignableTypeObjectPropertyGenerator(
		Class<?> type,
		ObjectPropertyGenerator objectPropertyGenerator
	) {
		generateOptionsBuilder.insertFirstArbitraryObjectPropertyGenerator(type, objectPropertyGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushExactTypeObjectPropertyGenerator(
		Class<?> type,
		ObjectPropertyGenerator objectPropertyGenerator
	) {
		generateOptionsBuilder.insertFirstArbitraryObjectPropertyGenerator(
			MatcherOperator.exactTypeMatchOperator(type, objectPropertyGenerator)
		);
		return this;
	}

	public FixtureMonkeyBuilder pushObjectPropertyGenerator(
		MatcherOperator<ObjectPropertyGenerator> objectPropertyGenerator
	) {
		generateOptionsBuilder.insertFirstArbitraryObjectPropertyGenerator(objectPropertyGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushAssignableTypeContainerPropertyGenerator(
		Class<?> type,
		ContainerPropertyGenerator containerPropertyGenerator
	) {
		generateOptionsBuilder.insertFirstArbitraryContainerPropertyGenerator(type, containerPropertyGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushExactTypeContainerPropertyGenerator(
		Class<?> type,
		ContainerPropertyGenerator containerPropertyGenerator
	) {
		generateOptionsBuilder.insertFirstArbitraryContainerPropertyGenerator(
			MatcherOperator.exactTypeMatchOperator(type, containerPropertyGenerator)
		);
		return this;
	}

	public FixtureMonkeyBuilder pushContainerPropertyGenerator(
		MatcherOperator<ContainerPropertyGenerator> containerPropertyGenerator
	) {
		generateOptionsBuilder.insertFirstArbitraryContainerPropertyGenerator(containerPropertyGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushAssignableTypePropertyNameResolver(
		Class<?> type,
		PropertyNameResolver propertyNameResolver
	) {
		generateOptionsBuilder.insertFirstPropertyNameResolver(type, propertyNameResolver);
		return this;
	}

	public FixtureMonkeyBuilder pushExactTypePropertyNameResolver(
		Class<?> type,
		PropertyNameResolver propertyNameResolver
	) {
		generateOptionsBuilder.insertFirstPropertyNameResolver(
			MatcherOperator.exactTypeMatchOperator(type, propertyNameResolver)
		);
		return this;
	}

	public FixtureMonkeyBuilder pushPropertyNameResolver(
		MatcherOperator<PropertyNameResolver> propertyNameResolver
	) {
		generateOptionsBuilder.insertFirstPropertyNameResolver(propertyNameResolver);
		return this;
	}

	public FixtureMonkeyBuilder defaultPropertyNameResolver(PropertyNameResolver propertyNameResolver) {
		generateOptionsBuilder.defaultPropertyNameResolver(propertyNameResolver);
		return this;
	}

	public FixtureMonkeyBuilder pushExactTypeNullInjectGenerator(
		Class<?> type,
		NullInjectGenerator nullInjectGenerator
	) {
		generateOptionsBuilder.insertFirstNullInjectGenerators(
			MatcherOperator.exactTypeMatchOperator(type, nullInjectGenerator)
		);
		return this;
	}

	public FixtureMonkeyBuilder pushAssignableTypeNullInjectGenerator(
		Class<?> type,
		NullInjectGenerator nullInjectGenerator
	) {
		generateOptionsBuilder.insertFirstNullInjectGenerators(type, nullInjectGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushNullInjectGenerator(MatcherOperator<NullInjectGenerator> nullInjectGenerator) {
		generateOptionsBuilder.insertFirstNullInjectGenerators(nullInjectGenerator);
		return this;
	}

	public FixtureMonkeyBuilder defaultNullInjectGenerator(NullInjectGenerator nullInjectGenerator) {
		generateOptionsBuilder.defaultNullInjectGenerator(nullInjectGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushArbitraryContainerInfoGenerator(
		MatcherOperator<ArbitraryContainerInfoGenerator> arbitraryContainerInfoGenerator
	) {
		generateOptionsBuilder.insertFirstArbitraryContainerInfoGenerator(arbitraryContainerInfoGenerator);
		return this;
	}

	public FixtureMonkeyBuilder defaultArbitraryContainerInfoGenerator(
		ArbitraryContainerInfoGenerator defaultArbitraryContainerInfoGenerator) {
		generateOptionsBuilder.defaultArbitraryContainerInfoGenerator(defaultArbitraryContainerInfoGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushAssignableTypeArbitraryIntrospector(
		Class<?> type,
		ArbitraryIntrospector arbitraryIntrospector
	) {
		generateOptionsBuilder.insertFirstArbitraryIntrospector(type, arbitraryIntrospector);
		return this;
	}

	public FixtureMonkeyBuilder pushExactTypeArbitraryIntrospector(
		Class<?> type,
		ArbitraryIntrospector arbitraryIntrospector
	) {
		generateOptionsBuilder.insertFirstArbitraryIntrospector(
			MatcherOperator.exactTypeMatchOperator(type, arbitraryIntrospector)
		);
		return this;
	}

	public FixtureMonkeyBuilder pushArbitraryIntrospector(
		MatcherOperator<ArbitraryIntrospector> arbitraryIntrospector
	) {
		generateOptionsBuilder.insertFirstArbitraryIntrospector(arbitraryIntrospector);
		return this;
	}

	public FixtureMonkeyBuilder objectIntrospector(ArbitraryIntrospector objectIntrospector) {
		this.generateOptionsBuilder.objectIntrospector(it -> objectIntrospector);
		return this;
	}

	public FixtureMonkeyBuilder javaTypeArbitraryGenerator(
		JavaTypeArbitraryGenerator javaTypeArbitraryGenerator
	) {
		generateOptionsBuilder.javaTypeArbitraryGenerator(javaTypeArbitraryGenerator);
		return this;
	}

	public FixtureMonkeyBuilder javaArbitraryResolver(JavaArbitraryResolver javaArbitraryResolver) {
		generateOptionsBuilder.javaArbitraryResolver(javaArbitraryResolver);
		return this;
	}

	public FixtureMonkeyBuilder javaTimeTypeArbitraryGenerator(
		JavaTimeTypeArbitraryGenerator javaTimeTypeArbitraryGenerator
	) {
		generateOptionsBuilder.javaTimeTypeArbitraryGenerator(javaTimeTypeArbitraryGenerator);
		return this;
	}

	public FixtureMonkeyBuilder javaTimeArbitraryResolver(JavaTimeArbitraryResolver javaTimeArbitraryResolver) {
		generateOptionsBuilder.javaTimeArbitraryResolver(javaTimeArbitraryResolver);
		return this;
	}

	public FixtureMonkeyBuilder arbitraryValidator(ArbitraryValidator arbitraryValidator) {
		this.generateOptionsBuilder.defaultArbitraryValidator(arbitraryValidator);
		return this;
	}

	public FixtureMonkeyBuilder pushExceptGenerateType(Matcher matcher) {
		generateOptionsBuilder.insertFirstArbitraryObjectPropertyGenerator(
			new MatcherOperator<>(
				matcher,
				NullObjectPropertyGenerator.INSTANCE
			)
		);
		return this;
	}

	public FixtureMonkeyBuilder addExceptGenerateClass(Class<?> type) {
		return pushExceptGenerateType(new AssignableTypeMatcher(type));
	}

	public FixtureMonkeyBuilder addExceptGenerateClasses(Class<?>... types) {
		for (Class<?> type : types) {
			addExceptGenerateClass(type);
		}
		return this;
	}

	public FixtureMonkeyBuilder addExceptGeneratePackage(String exceptGeneratePackage) {
		return pushExceptGenerateType(
			property -> Types.primitiveToWrapper(Types.getActualType(property.getType()))
				.getPackage()
				.getName()
				.startsWith(exceptGeneratePackage)
		);
	}

	public FixtureMonkeyBuilder addExceptGeneratePackages(String... exceptGeneratePackages) {
		for (String exceptGeneratePackage : exceptGeneratePackages) {
			addExceptGeneratePackage(exceptGeneratePackage);
		}
		return this;
	}

	public FixtureMonkeyBuilder register(
		Class<?> type,
		Function<FixtureMonkey, ? extends ArbitraryBuilder<?>> registeredArbitraryBuilder
	) {
		manipulateOptionsBuilder.register(
			MatcherOperator.assignableTypeMatchOperator(type, registeredArbitraryBuilder)
		);
		return this;
	}

	public FixtureMonkeyBuilder registerExactType(
		Class<?> type,
		Function<FixtureMonkey, ? extends ArbitraryBuilder<?>> registeredArbitraryBuilder
	) {
		manipulateOptionsBuilder.register(MatcherOperator.exactTypeMatchOperator(type, registeredArbitraryBuilder));
		return this;
	}

	public FixtureMonkeyBuilder registerAssignableType(
		Class<?> type,
		Function<FixtureMonkey, ? extends ArbitraryBuilder<?>> registeredArbitraryBuilder
	) {
		manipulateOptionsBuilder.register(
			MatcherOperator.assignableTypeMatchOperator(type, registeredArbitraryBuilder)
		);
		return this;
	}

	public FixtureMonkeyBuilder register(
		MatcherOperator<Function<FixtureMonkey, ? extends ArbitraryBuilder<?>>> registeredArbitraryBuilder
	) {
		manipulateOptionsBuilder.register(registeredArbitraryBuilder);
		return this;
	}

	public FixtureMonkeyBuilder registerGroup(Class<?>... arbitraryBuilderGroups) {
		for (Class<?> arbitraryBuilderGroup : arbitraryBuilderGroups) {
			Method[] methods = arbitraryBuilderGroup.getMethods();
			for (Method method : methods) {
				int paramCount = method.getParameterCount();
				Class<?> returnType = method.getReturnType();
				if (paramCount != 1 || !ArbitraryBuilder.class.isAssignableFrom(returnType)) {
					continue;
				}
				try {
					Class<?> actualType = Types.getActualType(
						Types.getGenericsTypes(method.getAnnotatedReturnType()).get(0)
					);
					Object noArgsInstance = arbitraryBuilderGroup.getDeclaredConstructor().newInstance();
					Function<FixtureMonkey, ? extends ArbitraryBuilder<?>> registerArbitraryBuilder =
						(fixtureMonkey) -> {
							try {
								return (ArbitraryBuilder<?>)method.invoke(noArgsInstance, fixtureMonkey);
							} catch (IllegalAccessException | InvocationTargetException e) {
								e.printStackTrace();
								throw new RuntimeException(e);
							}
						};
					this.register(actualType, registerArbitraryBuilder);
				} catch (InvocationTargetException
						| InstantiationException
						| IllegalAccessException
						| NoSuchMethodException e) {
					// ignored
				}
			}
		}
		return this;
	}

	@SuppressWarnings("rawtypes")
	public FixtureMonkeyBuilder pushFixtureCustomizer(MatcherOperator<FixtureCustomizer> arbitraryCustomizer) {
		generateOptionsBuilder.insertFirstFixtureCustomizer(arbitraryCustomizer);
		return this;
	}

	public <T> FixtureMonkeyBuilder pushAssignableTypeFixtureCustomizer(
		Class<T> type,
		FixtureCustomizer<? extends T> fixtureCustomizer
	) {
		generateOptionsBuilder.insertFirstFixtureCustomizer(type, fixtureCustomizer);
		return this;
	}

	public <T> FixtureMonkeyBuilder pushExactTypeFixtureCustomizer(
		Class<T> type,
		FixtureCustomizer<T> fixtureCustomizer
	) {
		generateOptionsBuilder.insertFirstFixtureCustomizer(
			MatcherOperator.exactTypeMatchOperator(type, fixtureCustomizer)
		);
		return this;
	}

	public FixtureMonkeyBuilder plugin(Plugin plugin) {
		generateOptionsBuilder.plugin(plugin);
		return this;
	}

	public FixtureMonkeyBuilder defaultDecomposedContainerValueFactory(
		DecomposedContainerValueFactory defaultDecomposedContainerValueFactory
	) {
		this.defaultDecomposedContainerValueFactory = defaultDecomposedContainerValueFactory;
		return this;
	}

	public FixtureMonkeyBuilder pushContainerIntrospector(ArbitraryIntrospector containerIntrospector) {
		this.generateOptionsBuilder.containerIntrospector(it ->
			new CompositeArbitraryIntrospector(
				Arrays.asList(
					containerIntrospector,
					it
				)
			)
		);
		return this;
	}

	public FixtureMonkeyBuilder addContainerType(
		Class<?> type,
		ContainerPropertyGenerator containerObjectPropertyGenerator,
		ArbitraryIntrospector containerArbitraryIntrospector,
		DecomposedContainerValueFactory decomposedContainerValueFactory
	) {
		this.pushAssignableTypeContainerPropertyGenerator(type, containerObjectPropertyGenerator);
		this.pushContainerIntrospector(containerArbitraryIntrospector);
		decomposableContainerFactoryMap.put(type, decomposedContainerValueFactory);
		return this;
	}

	public FixtureMonkeyBuilder defaultPropertyGenerator(PropertyGenerator propertyGenerator) {
		this.generateOptionsBuilder.defaultPropertyGenerator(propertyGenerator);
		return this;
	}

	public FixtureMonkeyBuilder defaultNotNull(boolean defaultNotNull) {
		this.generateOptionsBuilder.defaultNotNull(defaultNotNull);
		return this;
	}

	public FixtureMonkeyBuilder nullableContainer(boolean nullableContainer) {
		this.generateOptionsBuilder.nullableContainer(nullableContainer);
		return this;
	}

	public FixtureMonkeyBuilder nullableElement(boolean nullableElement) {
		this.generateOptionsBuilder.nullableElement(nullableElement);
		return this;
	}

	public <T> FixtureMonkeyBuilder interfaceImplements(
		Matcher matcher,
		List<Class<? extends T>> implementations
	) {
		this.pushObjectPropertyGenerator(
			new MatcherOperator<>(
				matcher,
				new InterfaceObjectPropertyGenerator(implementations)
			)
		);
		return this;
	}

	public <T> FixtureMonkeyBuilder interfaceImplements(
		Class<T> interfaceClass,
		List<Class<? extends T>> implementations
	) {
		if (!Modifier.isAbstract(interfaceClass.getModifiers())) {
			throw new IllegalArgumentException(
				"interfaceImplements option first parameter should be interface or abstract class. "
					+ interfaceClass.getTypeName()
			);
		}

		return this.interfaceImplements(new ExactTypeMatcher(interfaceClass), implementations);
	}

	public FixtureMonkey build() {
		manipulateOptionsBuilder.additionalDecomposedContainerValueFactory(
			obj -> {
				Class<?> actualType = obj.getClass();
				for (
					Entry<Class<?>, DecomposedContainerValueFactory> entry :
					this.decomposableContainerFactoryMap.entrySet()
				) {
					Class<?> type = entry.getKey();
					DecomposableContainerValue decomposedValue = entry.getValue().from(obj);

					if (actualType.isAssignableFrom(type)) {
						return decomposedValue;
					}
				}
				return this.defaultDecomposedContainerValueFactory.from(obj);
			}
		);

		GenerateOptions generateOptions = generateOptionsBuilder.build();
		ArbitraryTraverser traverser = new ArbitraryTraverser(generateOptions);

		return new FixtureMonkey(
			generateOptions,
			manipulateOptionsBuilder,
			traverser,
			manipulatorOptimizer,
			generateOptions.getDefaultArbitraryValidator(),
			MonkeyContext.builder().build()
		);
	}

	public FixtureMonkeyBuilder useExpressionStrictMode() {
		this.manipulateOptionsBuilder.expressionStrictMode(true);
		return this;
	}
}
