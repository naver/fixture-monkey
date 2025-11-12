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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.constraint.JavaConstraintGenerator;
import com.navercorp.fixturemonkey.api.container.DecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.NullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.MatchArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.NullArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.matcher.PriorityMatcherOperator;
import com.navercorp.fixturemonkey.api.matcher.TreeMatcher;
import com.navercorp.fixturemonkey.api.matcher.TreeMatcherOperator;
import com.navercorp.fixturemonkey.api.option.BuilderContextInitializer;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder;
import com.navercorp.fixturemonkey.api.plugin.Plugin;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.random.Randoms;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.api.validator.ArbitraryValidator;
import com.navercorp.fixturemonkey.buildergroup.ArbitraryBuilderCandidate;
import com.navercorp.fixturemonkey.buildergroup.ArbitraryBuilderGroup;
import com.navercorp.fixturemonkey.customizer.MonkeyManipulatorFactory;
import com.navercorp.fixturemonkey.experimental.ExperimentalFixtureMonkeyOptions;
import com.navercorp.fixturemonkey.expression.ArbitraryExpressionFactory;
import com.navercorp.fixturemonkey.expression.MonkeyExpressionFactory;
import com.navercorp.fixturemonkey.expression.StrictModeMonkeyExpressionFactory;
import com.navercorp.fixturemonkey.resolver.ManipulatorOptimizer;
import com.navercorp.fixturemonkey.resolver.NoneManipulatorOptimizer;
import com.navercorp.fixturemonkey.seed.SeedFileLoader;

@SuppressWarnings("unused")
@API(since = "0.4.0", status = Status.MAINTAINED)
public final class FixtureMonkeyBuilder {
	private static final int DEFAULT_PRIORITY = Integer.MAX_VALUE;

	private final FixtureMonkeyOptionsBuilder fixtureMonkeyOptionsBuilder = FixtureMonkeyOptions.builder();
	private boolean expressionStrictMode = false;
	private PropertyNameResolver defaultPropertyNameResolver;
	private final List<MatcherOperator<PropertyNameResolver>> propertyNameResolvers = new ArrayList<>();
	private final List<PriorityMatcherOperator<Function<FixtureMonkey, ? extends ArbitraryBuilder<?>>>>
		registeredArbitraryBuildersWithPriority = new ArrayList<>();
	private final Map<String, PriorityMatcherOperator<Function<FixtureMonkey, ? extends ArbitraryBuilder<?>>>>
		registeredPriorityMatchersByName = new HashMap<>();
	private ManipulatorOptimizer manipulatorOptimizer = new NoneManipulatorOptimizer();
	private MonkeyExpressionFactory monkeyExpressionFactory = new ArbitraryExpressionFactory();
	private boolean experimentalFileSeedEnabled = false;
	private long seed = System.nanoTime();

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
		this.propertyNameResolvers.add(MatcherOperator.assignableTypeMatchOperator(type, propertyNameResolver));
		return this;
	}

	public FixtureMonkeyBuilder pushExactTypePropertyNameResolver(
		Class<?> type,
		PropertyNameResolver propertyNameResolver
	) {
		this.propertyNameResolvers.add(MatcherOperator.exactTypeMatchOperator(type, propertyNameResolver));
		return this;
	}

	public FixtureMonkeyBuilder pushPropertyNameResolver(
		MatcherOperator<PropertyNameResolver> propertyNameResolver
	) {
		this.propertyNameResolvers.add(propertyNameResolver);
		return this;
	}

	public FixtureMonkeyBuilder defaultPropertyNameResolver(PropertyNameResolver propertyNameResolver) {
		this.defaultPropertyNameResolver = propertyNameResolver;
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

	public FixtureMonkeyBuilder arbitraryValidator(ArbitraryValidator arbitraryValidator) {
		this.fixtureMonkeyOptionsBuilder.defaultArbitraryValidator(arbitraryValidator);
		return this;
	}

	public FixtureMonkeyBuilder pushExceptGenerateType(Matcher matcher) {
		fixtureMonkeyOptionsBuilder.insertFirstArbitraryIntrospector(
			new MatcherOperator<>(
				matcher,
				NullArbitraryIntrospector.INSTANCE
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

	@SuppressWarnings("dereference.of.nullable")
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

	/**
	 * Registers an ArbitraryBuilder with the DEFAULT priority (Integer.MAX_VALUE).
	 *
	 * @param registeredArbitraryBuilder the MatcherOperator containing the matcher
	 * and the ArbitraryBuilder to be registered
	 * @return the current instance of FixtureMonkeyBuilder for method chaining
	 */
	public FixtureMonkeyBuilder register(
		Class<?> type,
		Function<FixtureMonkey, ? extends ArbitraryBuilder<?>> registeredArbitraryBuilder
	) {
		return this.register(type, registeredArbitraryBuilder, DEFAULT_PRIORITY);
	}

	/**
	 * Registers an ArbitraryBuilder with a specified priority.
	 *
	 * @param registeredArbitraryBuilder the MatcherOperator containing the matcher
	 * and the ArbitraryBuilder to be registered
	 * @param priority the priority of the ArbitraryBuilder; higher values indicate lower priority
	 * @return the current instance of FixtureMonkeyBuilder for method chaining
	 * @throws IllegalArgumentException if the priority is less than 0
	 *
	 * If multiple ArbitraryBuilders have the same priority, one of them will be selected randomly.
	 */
	public FixtureMonkeyBuilder register(
		Class<?> type,
		Function<FixtureMonkey, ? extends ArbitraryBuilder<?>> registeredArbitraryBuilder,
		int priority
	) {
		return this.register(MatcherOperator.assignableTypeMatchOperator(type, registeredArbitraryBuilder), priority);
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
		return this.register(registeredArbitraryBuilder, DEFAULT_PRIORITY);
	}

	public FixtureMonkeyBuilder register(
		MatcherOperator<Function<FixtureMonkey, ? extends ArbitraryBuilder<?>>> registeredArbitraryBuilder,
		int priority
	) {
		this.registeredArbitraryBuildersWithPriority.add(
			new PriorityMatcherOperator<>(
				registeredArbitraryBuilder.getMatcher(), registeredArbitraryBuilder.getOperator(), priority
			)
		);
		return this;
	}

	@SuppressWarnings("return")
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
							} catch (IllegalAccessException | InvocationTargetException ex) {
								ex.printStackTrace();
								throw new RuntimeException(ex);
							}
						};

					// TODO: Support Order annotation
					// if (arbitraryBuilderGroup.isAnnotationPresent(Order.class)) {
					// 	Order order = arbitraryBuilderGroup.getAnnotation(Order.class);
					// 	this.register(actualType, registerArbitraryBuilder, order.value());
					// 	continue;
					// }
					this.register(actualType, registerArbitraryBuilder);
				} catch (Exception ex) {
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
					candidate.getArbitraryBuilderRegisterer(),
					DEFAULT_PRIORITY
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

	public FixtureMonkeyBuilder addDecomposedContainerValueFactory(
		Class<?> type,
		DecomposedContainerValueFactory additionalDecomposedContainerValueFactory
	) {
		fixtureMonkeyOptionsBuilder.addDecomposedContainerValueFactory(type, additionalDecomposedContainerValueFactory);
		return this;
	}

	public FixtureMonkeyBuilder pushContainerIntrospector(ArbitraryIntrospector containerIntrospector) {
		this.fixtureMonkeyOptionsBuilder.containerIntrospector(it ->
			new MatchArbitraryIntrospector(
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

	public FixtureMonkeyBuilder enableLoggingFail(boolean enableLoggingFail) {
		this.fixtureMonkeyOptionsBuilder.enableLoggingFail(enableLoggingFail);
		return this;
	}

	public FixtureMonkeyBuilder useExpressionStrictMode() {
		this.expressionStrictMode = true;
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

	public FixtureMonkeyBuilder javaConstraintGenerator(JavaConstraintGenerator javaConstraintGenerator) {
		fixtureMonkeyOptionsBuilder.javaConstraintGenerator(javaConstraintGenerator);
		return this;
	}

	public FixtureMonkeyBuilder pushJavaConstraintGeneratorCustomizer(
		UnaryOperator<JavaConstraintGenerator> javaConstraintGeneratorCustomizer
	) {
		fixtureMonkeyOptionsBuilder.insertFirstJavaConstraintGeneratorCustomizer(javaConstraintGeneratorCustomizer);
		return this;
	}

	public FixtureMonkeyBuilder pushCustomizeValidOnly(TreeMatcher matcher, boolean validOnly) {
		fixtureMonkeyOptionsBuilder.insertFirstBuilderContextInitializer(
			new TreeMatcherOperator<>(
				matcher,
				BuilderContextInitializer.validOnly(validOnly)
			)
		);
		return this;
	}

	public FixtureMonkeyBuilder useExperimental(
		UnaryOperator<ExperimentalFixtureMonkeyOptions> experimentalOptionsConfigurator
	) {
		ExperimentalFixtureMonkeyOptions options = experimentalOptionsConfigurator.apply(
			new ExperimentalFixtureMonkeyOptions()
		);

		if (options.isFileSeedEnabled()) {
			this.experimentalFileSeedEnabled = true;
			Long fileSeed = new SeedFileLoader().loadSeedFromFile();
			this.seed = fileSeed != null ? fileSeed : System.nanoTime();
		}
		return this;
	}

	/**
	 * sets the seed for generating random numbers.
	 * <p>
	 * If you use the {@code fixture-monkey-junit-jupiter} module,
	 * the seed value can be overridden by the {@code Seed} annotation.
	 *
	 * @param seed seed value for generating random numbers.
	 * @return FixtureMonkeyBuilder
	 */
	public FixtureMonkeyBuilder seed(long seed) {
		this.experimentalFileSeedEnabled = false;
		this.seed = seed;
		return this;
	}

	public FixtureMonkey build() {
		if (defaultPropertyNameResolver != null) {
			fixtureMonkeyOptionsBuilder.defaultPropertyNameResolver(defaultPropertyNameResolver);
		}

		for (MatcherOperator<PropertyNameResolver> propertyNameResolver : propertyNameResolvers) {
			fixtureMonkeyOptionsBuilder.insertFirstPropertyNameResolver(propertyNameResolver);
		}

		FixtureMonkeyOptions fixtureMonkeyOptions = fixtureMonkeyOptionsBuilder.build();
		MonkeyManipulatorFactory monkeyManipulatorFactory = new MonkeyManipulatorFactory(
			new AtomicInteger(),
			fixtureMonkeyOptions.getDecomposedContainerValueFactory(),
			fixtureMonkeyOptions.getContainerPropertyGenerators()
		);

		MonkeyExpressionFactory monkeyExpressionFactory = newExpressionFactory(fixtureMonkeyOptions);

		Randoms.setSeed(seed);
		return new FixtureMonkey(
			fixtureMonkeyOptions,
			manipulatorOptimizer,
			registeredArbitraryBuildersWithPriority,
			monkeyManipulatorFactory,
			monkeyExpressionFactory,
			registeredPriorityMatchersByName
		);
	}

	private MonkeyExpressionFactory newExpressionFactory(FixtureMonkeyOptions fixtureMonkeyOptions) {
		if (!expressionStrictMode) {
			return this.monkeyExpressionFactory;
		}

		PropertyNameResolver compositePropertyNameResolver = newCompositePropertyNameResolver(fixtureMonkeyOptions);

		return new StrictModeMonkeyExpressionFactory(
			new ArbitraryExpressionFactory(),
			compositePropertyNameResolver
		);
	}

	private PropertyNameResolver newCompositePropertyNameResolver(
		FixtureMonkeyOptions fixtureMonkeyOptions
	) {
		return property -> fixtureMonkeyOptions.getPropertyNameResolvers().stream()
			.filter(it -> it.getMatcher().match(property))
			.findFirst()
			.map(it -> it.getOperator().resolve(property))
			.orElseGet(() -> fixtureMonkeyOptions.getDefaultPropertyNameResolver().resolve(property));
	}
}
