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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.container.DecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.api.context.MonkeyContext;
import com.navercorp.fixturemonkey.api.context.MonkeyContextBuilder;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.InterfaceObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.NullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.NullObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
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
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder;
import com.navercorp.fixturemonkey.api.plugin.Plugin;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.api.validator.ArbitraryValidator;
import com.navercorp.fixturemonkey.buildergroup.ArbitraryBuilderCandidate;
import com.navercorp.fixturemonkey.buildergroup.ArbitraryBuilderGroup;
import com.navercorp.fixturemonkey.expression.ArbitraryExpressionFactory;
import com.navercorp.fixturemonkey.expression.MonkeyExpressionFactory;
import com.navercorp.fixturemonkey.resolver.ManipulatorOptimizer;
import com.navercorp.fixturemonkey.resolver.NoneManipulatorOptimizer;
import com.navercorp.fixturemonkey.tree.ApplyStrictModeResolver;
import com.navercorp.fixturemonkey.tree.ArbitraryTraverser;

@SuppressWarnings("unused")
@API(since = "0.4.0", status = Status.MAINTAINED)
public class FixtureMonkeyBuilder {
	private final FixtureMonkeyOptionsBuilder fixtureMonkeyOptionsBuilder = FixtureMonkeyOptions.builder();
	private final List<MatcherOperator<Function<FixtureMonkey, ? extends ArbitraryBuilder<?>>>>
		registeredArbitraryBuilders = new ArrayList<>();
	private ManipulatorOptimizer manipulatorOptimizer = new NoneManipulatorOptimizer();
	private MonkeyExpressionFactory monkeyExpressionFactory = new ArbitraryExpressionFactory();
	private final MonkeyContextBuilder monkeyContextBuilder = MonkeyContext.builder();

	public FixtureMonkeyBuilder pushPropertyGenerator(MatcherOperator<PropertyGenerator> propertyGenerator) {
		fixtureMonkeyOptionsBuilder.insertFirstPropertyGenerator(propertyGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushAssignableTypePropertyGenerator(
		Class<?> type,
		PropertyGenerator propertyGenerator
	) {
		fixtureMonkeyOptionsBuilder.insertFirstPropertyGenerator(type, propertyGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushExactTypePropertyGenerator(Class<?> type, PropertyGenerator propertyGenerator) {
		fixtureMonkeyOptionsBuilder.insertFirstPropertyGenerator(
			MatcherOperator.assignableTypeMatchOperator(type, propertyGenerator)
		);
		return this;
	}

	public FixtureMonkeyBuilder manipulatorOptimizer(ManipulatorOptimizer manipulatorOptimizer) {
		this.manipulatorOptimizer = manipulatorOptimizer;
		return this;
	}

	public FixtureMonkeyBuilder defaultObjectPropertyGenerator(
		ObjectPropertyGenerator objectPropertyGenerator
	) {
		fixtureMonkeyOptionsBuilder.defaultObjectPropertyGenerator(objectPropertyGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushAssignableTypeObjectPropertyGenerator(
		Class<?> type,
		ObjectPropertyGenerator objectPropertyGenerator
	) {
		fixtureMonkeyOptionsBuilder.insertFirstArbitraryObjectPropertyGenerator(type, objectPropertyGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushExactTypeObjectPropertyGenerator(
		Class<?> type,
		ObjectPropertyGenerator objectPropertyGenerator
	) {
		fixtureMonkeyOptionsBuilder.insertFirstArbitraryObjectPropertyGenerator(
			MatcherOperator.exactTypeMatchOperator(type, objectPropertyGenerator)
		);
		return this;
	}

	public FixtureMonkeyBuilder pushObjectPropertyGenerator(
		MatcherOperator<ObjectPropertyGenerator> objectPropertyGenerator
	) {
		fixtureMonkeyOptionsBuilder.insertFirstArbitraryObjectPropertyGenerator(objectPropertyGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushAssignableTypeContainerPropertyGenerator(
		Class<?> type,
		ContainerPropertyGenerator containerPropertyGenerator
	) {
		fixtureMonkeyOptionsBuilder.insertFirstArbitraryContainerPropertyGenerator(type, containerPropertyGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushExactTypeContainerPropertyGenerator(
		Class<?> type,
		ContainerPropertyGenerator containerPropertyGenerator
	) {
		fixtureMonkeyOptionsBuilder.insertFirstArbitraryContainerPropertyGenerator(
			MatcherOperator.exactTypeMatchOperator(type, containerPropertyGenerator)
		);
		return this;
	}

	public FixtureMonkeyBuilder pushContainerPropertyGenerator(
		MatcherOperator<ContainerPropertyGenerator> containerPropertyGenerator
	) {
		fixtureMonkeyOptionsBuilder.insertFirstArbitraryContainerPropertyGenerator(containerPropertyGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushAssignableTypePropertyNameResolver(
		Class<?> type,
		PropertyNameResolver propertyNameResolver
	) {
		fixtureMonkeyOptionsBuilder.insertFirstPropertyNameResolver(type, propertyNameResolver);
		return this;
	}

	public FixtureMonkeyBuilder pushExactTypePropertyNameResolver(
		Class<?> type,
		PropertyNameResolver propertyNameResolver
	) {
		fixtureMonkeyOptionsBuilder.insertFirstPropertyNameResolver(
			MatcherOperator.exactTypeMatchOperator(type, propertyNameResolver)
		);
		return this;
	}

	public FixtureMonkeyBuilder pushPropertyNameResolver(
		MatcherOperator<PropertyNameResolver> propertyNameResolver
	) {
		fixtureMonkeyOptionsBuilder.insertFirstPropertyNameResolver(propertyNameResolver);
		return this;
	}

	public FixtureMonkeyBuilder defaultPropertyNameResolver(PropertyNameResolver propertyNameResolver) {
		fixtureMonkeyOptionsBuilder.defaultPropertyNameResolver(propertyNameResolver);
		return this;
	}

	public FixtureMonkeyBuilder pushExactTypeNullInjectGenerator(
		Class<?> type,
		NullInjectGenerator nullInjectGenerator
	) {
		fixtureMonkeyOptionsBuilder.insertFirstNullInjectGenerators(
			MatcherOperator.exactTypeMatchOperator(type, nullInjectGenerator)
		);
		return this;
	}

	public FixtureMonkeyBuilder pushAssignableTypeNullInjectGenerator(
		Class<?> type,
		NullInjectGenerator nullInjectGenerator
	) {
		fixtureMonkeyOptionsBuilder.insertFirstNullInjectGenerators(type, nullInjectGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushNullInjectGenerator(MatcherOperator<NullInjectGenerator> nullInjectGenerator) {
		fixtureMonkeyOptionsBuilder.insertFirstNullInjectGenerators(nullInjectGenerator);
		return this;
	}

	public FixtureMonkeyBuilder defaultNullInjectGenerator(NullInjectGenerator nullInjectGenerator) {
		fixtureMonkeyOptionsBuilder.defaultNullInjectGenerator(nullInjectGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushArbitraryContainerInfoGenerator(
		MatcherOperator<ArbitraryContainerInfoGenerator> arbitraryContainerInfoGenerator
	) {
		fixtureMonkeyOptionsBuilder.insertFirstArbitraryContainerInfoGenerator(arbitraryContainerInfoGenerator);
		return this;
	}

	public FixtureMonkeyBuilder defaultArbitraryContainerInfoGenerator(
		ArbitraryContainerInfoGenerator defaultArbitraryContainerInfoGenerator) {
		fixtureMonkeyOptionsBuilder.defaultArbitraryContainerInfoGenerator(defaultArbitraryContainerInfoGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushAssignableTypeArbitraryIntrospector(
		Class<?> type,
		ArbitraryIntrospector arbitraryIntrospector
	) {
		fixtureMonkeyOptionsBuilder.insertFirstArbitraryIntrospector(type, arbitraryIntrospector);
		return this;
	}

	public FixtureMonkeyBuilder pushExactTypeArbitraryIntrospector(
		Class<?> type,
		ArbitraryIntrospector arbitraryIntrospector
	) {
		fixtureMonkeyOptionsBuilder.insertFirstArbitraryIntrospector(
			MatcherOperator.exactTypeMatchOperator(type, arbitraryIntrospector)
		);
		return this;
	}

	public FixtureMonkeyBuilder pushArbitraryIntrospector(
		MatcherOperator<ArbitraryIntrospector> arbitraryIntrospector
	) {
		fixtureMonkeyOptionsBuilder.insertFirstArbitraryIntrospector(arbitraryIntrospector);
		return this;
	}

	public FixtureMonkeyBuilder objectIntrospector(ArbitraryIntrospector objectIntrospector) {
		this.fixtureMonkeyOptionsBuilder.objectIntrospector(it -> objectIntrospector);
		return this;
	}

	public FixtureMonkeyBuilder javaTypeArbitraryGenerator(
		JavaTypeArbitraryGenerator javaTypeArbitraryGenerator
	) {
		fixtureMonkeyOptionsBuilder.javaTypeArbitraryGenerator(javaTypeArbitraryGenerator);
		return this;
	}

	public FixtureMonkeyBuilder javaArbitraryResolver(JavaArbitraryResolver javaArbitraryResolver) {
		fixtureMonkeyOptionsBuilder.javaArbitraryResolver(javaArbitraryResolver);
		return this;
	}

	public FixtureMonkeyBuilder javaTimeTypeArbitraryGenerator(
		JavaTimeTypeArbitraryGenerator javaTimeTypeArbitraryGenerator
	) {
		fixtureMonkeyOptionsBuilder.javaTimeTypeArbitraryGenerator(javaTimeTypeArbitraryGenerator);
		return this;
	}

	public FixtureMonkeyBuilder javaTimeArbitraryResolver(JavaTimeArbitraryResolver javaTimeArbitraryResolver) {
		fixtureMonkeyOptionsBuilder.javaTimeArbitraryResolver(javaTimeArbitraryResolver);
		return this;
	}

	public FixtureMonkeyBuilder arbitraryValidator(ArbitraryValidator arbitraryValidator) {
		this.fixtureMonkeyOptionsBuilder.defaultArbitraryValidator(arbitraryValidator);
		return this;
	}

	public FixtureMonkeyBuilder pushExceptGenerateType(Matcher matcher) {
		fixtureMonkeyOptionsBuilder.insertFirstArbitraryObjectPropertyGenerator(
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
		return this.register(MatcherOperator.assignableTypeMatchOperator(type, registeredArbitraryBuilder));
	}

	public FixtureMonkeyBuilder registerExactType(
		Class<?> type,
		Function<FixtureMonkey, ? extends ArbitraryBuilder<?>> registeredArbitraryBuilder
	) {
		return this.register(MatcherOperator.exactTypeMatchOperator(type, registeredArbitraryBuilder));
	}

	public FixtureMonkeyBuilder registerAssignableType(
		Class<?> type,
		Function<FixtureMonkey, ? extends ArbitraryBuilder<?>> registeredArbitraryBuilder
	) {
		return this.register(MatcherOperator.assignableTypeMatchOperator(type, registeredArbitraryBuilder));
	}

	public FixtureMonkeyBuilder register(
		MatcherOperator<Function<FixtureMonkey, ? extends ArbitraryBuilder<?>>> registeredArbitraryBuilder
	) {
		this.registeredArbitraryBuilders.add(registeredArbitraryBuilder);
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

	public FixtureMonkeyBuilder registerGroup(ArbitraryBuilderGroup... arbitraryBuilderGroups) {
		for (ArbitraryBuilderGroup arbitraryBuilderGroup : arbitraryBuilderGroups) {
			List<ArbitraryBuilderCandidate<?>> candidates = arbitraryBuilderGroup.generateCandidateList()
				.getCandidates();

			for (ArbitraryBuilderCandidate<?> candidate : candidates) {
				this.register(
					candidate.getClassType(),
					candidate.getArbitraryBuilderRegisterer()
				);
			}
		}
		return this;
	}

	public FixtureMonkeyBuilder plugin(Plugin plugin) {
		fixtureMonkeyOptionsBuilder.plugin(plugin);
		return this;
	}

	public FixtureMonkeyBuilder defaultDecomposedContainerValueFactory(
		DecomposedContainerValueFactory defaultDecomposedContainerValueFactory
	) {
		fixtureMonkeyOptionsBuilder.decomposedContainerValueFactory(defaultDecomposedContainerValueFactory);
		return this;
	}

	public FixtureMonkeyBuilder pushContainerIntrospector(ArbitraryIntrospector containerIntrospector) {
		this.fixtureMonkeyOptionsBuilder.containerIntrospector(it ->
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
		this.fixtureMonkeyOptionsBuilder.addDecomposedContainerValueFactory(type, decomposedContainerValueFactory);
		return this;
	}

	public FixtureMonkeyBuilder defaultPropertyGenerator(PropertyGenerator propertyGenerator) {
		this.fixtureMonkeyOptionsBuilder.defaultPropertyGenerator(propertyGenerator);
		return this;
	}

	public FixtureMonkeyBuilder defaultNotNull(boolean defaultNotNull) {
		this.fixtureMonkeyOptionsBuilder.defaultNotNull(defaultNotNull);
		return this;
	}

	public FixtureMonkeyBuilder nullableContainer(boolean nullableContainer) {
		this.fixtureMonkeyOptionsBuilder.nullableContainer(nullableContainer);
		return this;
	}

	public FixtureMonkeyBuilder nullableElement(boolean nullableElement) {
		this.fixtureMonkeyOptionsBuilder.nullableElement(nullableElement);
		return this;
	}

	public <T> FixtureMonkeyBuilder interfaceImplements(
		Matcher matcher,
		List<Class<? extends T>> implementations
	) {
		this.pushObjectPropertyGenerator(
			new MatcherOperator<>(
				matcher,
				new InterfaceObjectPropertyGenerator<>(implementations)
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

	public FixtureMonkeyBuilder useExpressionStrictMode() {
		this.monkeyExpressionFactory = expression ->
			() -> new ApplyStrictModeResolver(new ArbitraryExpressionFactory().from(expression).toNodeResolver());
		return this;
	}

	public FixtureMonkeyBuilder defaultArbitraryGenerator(
		UnaryOperator<ArbitraryGenerator> arbitraryGeneratorUnaryOperator
	) {
		this.fixtureMonkeyOptionsBuilder.defaultArbitraryGenerator(arbitraryGeneratorUnaryOperator);
		return this;
	}

	public FixtureMonkeyBuilder generateMaxTries(int generateMaxTries) {
		fixtureMonkeyOptionsBuilder.generateMaxTries(generateMaxTries);
		return this;
	}

	public FixtureMonkeyBuilder generateUniqueMaxTries(int generateUniqueMaxTries) {
		fixtureMonkeyOptionsBuilder.generateUniqueMaxTries(generateUniqueMaxTries);
		return this;
	}

	public FixtureMonkey build() {
		FixtureMonkeyOptions fixtureMonkeyOptions = fixtureMonkeyOptionsBuilder.build();
		ArbitraryTraverser traverser = new ArbitraryTraverser(fixtureMonkeyOptions);

		MonkeyContext monkeyContext = monkeyContextBuilder.build();
		return new FixtureMonkey(
			fixtureMonkeyOptions,
			traverser,
			manipulatorOptimizer,
			fixtureMonkeyOptions.getDefaultArbitraryValidator(),
			monkeyContext,
			registeredArbitraryBuilders,
			monkeyExpressionFactory
		);
	}
}
