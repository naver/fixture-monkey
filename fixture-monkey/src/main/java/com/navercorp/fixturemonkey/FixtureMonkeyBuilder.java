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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators.F3;
import net.jqwik.api.Combinators.F4;

import com.navercorp.fixturemonkey.api.ObjectBuilder;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.constraint.JavaConstraintGenerator;
import com.navercorp.fixturemonkey.api.container.DecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.api.expression.TypedPropertySelector;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfoGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.NullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.instantiator.Instantiator;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.MatchArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.NullArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.matcher.TreeMatcher;
import com.navercorp.fixturemonkey.api.matcher.TreeMatcherOperator;
import com.navercorp.fixturemonkey.api.option.BuilderContextInitializer;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder;
import com.navercorp.fixturemonkey.api.plugin.Plugin;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.property.PropertySelector;
import com.navercorp.fixturemonkey.api.random.Randoms;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.api.validator.ArbitraryValidator;
import com.navercorp.fixturemonkey.builder.ArbitraryBuilderContext;
import com.navercorp.fixturemonkey.builder.ArbitraryBuilderContextProvider;
import com.navercorp.fixturemonkey.buildergroup.ArbitraryBuilderCandidate;
import com.navercorp.fixturemonkey.buildergroup.ArbitraryBuilderGroup;
import com.navercorp.fixturemonkey.customizer.InnerSpec;
import com.navercorp.fixturemonkey.customizer.MonkeyManipulatorFactory;
import com.navercorp.fixturemonkey.experimental.ExperimentalArbitraryBuilder;
import com.navercorp.fixturemonkey.expression.ArbitraryExpressionFactory;
import com.navercorp.fixturemonkey.expression.MonkeyExpression;
import com.navercorp.fixturemonkey.expression.MonkeyExpressionFactory;
import com.navercorp.fixturemonkey.resolver.ManipulatorOptimizer;
import com.navercorp.fixturemonkey.resolver.NoneManipulatorOptimizer;
import com.navercorp.fixturemonkey.tree.ApplyStrictModeResolver;
import com.navercorp.fixturemonkey.tree.NextNodePredicate;
import com.navercorp.fixturemonkey.tree.NodeResolver;

@SuppressWarnings("unused")
@API(since = "0.4.0", status = Status.MAINTAINED)
public final class FixtureMonkeyBuilder {
	private final FixtureMonkeyOptionsBuilder fixtureMonkeyOptionsBuilder = FixtureMonkeyOptions.builder();
	private final List<MatcherOperator<Function<FixtureMonkey, ? extends ArbitraryBuilder<?>>>>
		registeredArbitraryBuilders = new ArrayList<>();
	private ManipulatorOptimizer manipulatorOptimizer = new NoneManipulatorOptimizer();
	private MonkeyExpressionFactory monkeyExpressionFactory = new ArbitraryExpressionFactory();
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
							} catch (IllegalAccessException | InvocationTargetException ex) {
								ex.printStackTrace();
								throw new RuntimeException(ex);
							}
						};
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
		this.monkeyExpressionFactory = expression -> {
			MonkeyExpression monkeyExpression = new ArbitraryExpressionFactory().from(expression);

			return new MonkeyExpression() {
				@Override
				public NodeResolver toNodeResolver() {
					return new ApplyStrictModeResolver(monkeyExpression.toNodeResolver());
				}

				@Override
				public List<NextNodePredicate> toNextNodePredicate() { // TODO:
					return monkeyExpression.toNextNodePredicate();
				}
			};
		};
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
		this.seed = seed;
		return this;
	}

	public FixtureMonkey build() {
		FixtureMonkeyOptions fixtureMonkeyOptions = fixtureMonkeyOptionsBuilder.build();
		MonkeyManipulatorFactory monkeyManipulatorFactory = new MonkeyManipulatorFactory(
			new AtomicInteger(),
			fixtureMonkeyOptions.getDecomposedContainerValueFactory(),
			fixtureMonkeyOptions.getContainerPropertyGenerators()
		);

		Randoms.setSeed(seed);

		RegisterFixtureMonkey registerFixtureMonkey = new RegisterFixtureMonkey(
			fixtureMonkeyOptions,
			manipulatorOptimizer,
			monkeyManipulatorFactory,
			monkeyExpressionFactory
		);

		List<MatcherOperator<? extends ObjectBuilder<?>>> generatedRegisteredArbitraryBuilder = new ArrayList<>();
		for (int i = registeredArbitraryBuilders.size() - 1; i >= 0; i--) {
			MatcherOperator<Function<FixtureMonkey, ? extends ArbitraryBuilder<?>>> it =
				registeredArbitraryBuilders.get(i);

			generatedRegisteredArbitraryBuilder.add(
				new MatcherOperator<>(
					it.getMatcher(),
					(ObjectBuilder<?>)(it.getOperator().apply(registerFixtureMonkey))
				)
			);
		}

		return new FixtureMonkey(
			fixtureMonkeyOptions,
			manipulatorOptimizer,
			generatedRegisteredArbitraryBuilder,
			monkeyManipulatorFactory,
			monkeyExpressionFactory
		);
	}

	/**
	 * It is to generate the leaf node of registered {@link ArbitraryBuilder}.
	 */
	private static final class RegisterFixtureMonkey extends FixtureMonkey {
		public RegisterFixtureMonkey(
			FixtureMonkeyOptions fixtureMonkeyOptions,
			ManipulatorOptimizer manipulatorOptimizer,
			MonkeyManipulatorFactory monkeyManipulatorFactory,
			MonkeyExpressionFactory monkeyExpressionFactory
		) {
			super(fixtureMonkeyOptions, manipulatorOptimizer, Collections.emptyList(), monkeyManipulatorFactory,
				monkeyExpressionFactory);
		}

		@Override
		public <T> ArbitraryBuilder<T> giveMeBuilder(Class<T> type) {
			return new RegisteredArbitraryBuilder<>(this, super.giveMeBuilder(type));
		}

		@Override
		public <T> ArbitraryBuilder<T> giveMeBuilder(TypeReference<T> type) {
			return new RegisteredArbitraryBuilder<>(this, super.giveMeBuilder(type));
		}

		@Override
		public <T> ArbitraryBuilder<T> giveMeBuilder(T value) {
			return new RegisteredArbitraryBuilder<>(this, super.giveMeBuilder(value));
		}

		@Override
		public <T> ExperimentalArbitraryBuilder<T> giveMeExperimentalBuilder(Class<T> type) {
			return new RegisteredArbitraryBuilder<>(this, super.giveMeExperimentalBuilder(type));
		}

		@Override
		public <T> ExperimentalArbitraryBuilder<T> giveMeExperimentalBuilder(TypeReference<T> type) {
			return new RegisteredArbitraryBuilder<>(this, super.giveMeExperimentalBuilder(type));
		}

		private static class RegisteredArbitraryBuilder<T> implements
			ArbitraryBuilder<T>,
			ExperimentalArbitraryBuilder<T>,
			ObjectBuilder<T>,
			ArbitraryBuilderContextProvider {
			private final RegisterFixtureMonkey fixtureMonkey;
			private final ArbitraryBuilder<T> delegate;

			public RegisteredArbitraryBuilder(RegisterFixtureMonkey fixtureMonkey, ArbitraryBuilder<T> delegate) {
				this.fixtureMonkey = fixtureMonkey;
				this.delegate = delegate;
			}

			@Override
			public ArbitraryBuilder<T> set(String expression, @Nullable Object value) {
				return delegate.set(expression, value);
			}

			@Override
			public ArbitraryBuilder<T> set(String expression, @Nullable Object value, int limit) {
				return delegate.set(expression, value, limit);
			}

			@Override
			public ArbitraryBuilder<T> set(PropertySelector propertySelector, @Nullable Object value) {
				return delegate.set(propertySelector, value);
			}

			@Override
			public ArbitraryBuilder<T> set(PropertySelector propertySelector, @Nullable Object value, int limit) {
				return delegate.set(propertySelector, value, limit);
			}

			@Override
			public ArbitraryBuilder<T> set(@Nullable Object value) {
				return delegate.set(value);
			}

			@Override
			public ArbitraryBuilder<T> setInner(InnerSpec innerSpec) {
				return delegate.setInner(innerSpec);
			}

			@Override
			public ArbitraryBuilder<T> setLazy(String expression, Supplier<?> supplier) {
				return delegate.setLazy(expression, supplier);
			}

			@Override
			public ArbitraryBuilder<T> setLazy(String expression, Supplier<?> supplier, int limit) {
				return delegate.setLazy(expression, supplier, limit);
			}

			@Override
			public ArbitraryBuilder<T> setLazy(PropertySelector propertySelector, Supplier<?> supplier) {
				return delegate.setLazy(propertySelector, supplier);
			}

			@Override
			public ArbitraryBuilder<T> setLazy(PropertySelector propertySelector, Supplier<?> supplier, int limit) {
				return delegate.setLazy(propertySelector, supplier, limit);
			}

			@Override
			public ArbitraryBuilder<T> setNull(String expression) {
				return delegate.setNull(expression);
			}

			@Override
			public ArbitraryBuilder<T> setNull(PropertySelector propertySelector) {
				return delegate.setNull(propertySelector);
			}

			@Override
			public ArbitraryBuilder<T> setNotNull(String expression) {
				return delegate.setNotNull(expression);
			}

			@Override
			public ArbitraryBuilder<T> setNotNull(PropertySelector propertySelector) {
				return delegate.setNotNull(propertySelector);
			}

			@Override
			public <U> ArbitraryBuilder<T> setPostCondition(String expression, Class<U> type, Predicate<U> predicate) {
				return delegate.setPostCondition(expression, type, predicate);
			}

			@Override
			public <U> ArbitraryBuilder<T> setPostCondition(PropertySelector propertySelector, Class<U> type,
				Predicate<U> predicate) {
				return delegate.setPostCondition(propertySelector, type, predicate);
			}

			@Override
			public <U> ArbitraryBuilder<T> setPostCondition(String expression, Class<U> type, Predicate<U> predicate,
				int limit) {
				return delegate.setPostCondition(expression, type, predicate, limit);
			}

			@Override
			public <U> ArbitraryBuilder<T> setPostCondition(PropertySelector propertySelector, Class<U> type,
				Predicate<U> predicate, int limit) {
				return delegate.setPostCondition(propertySelector, type, predicate, limit);
			}

			@Override
			public ArbitraryBuilder<T> setPostCondition(Predicate<T> predicate) {
				return delegate.setPostCondition(predicate);
			}

			@Override
			public ArbitraryBuilder<T> size(String expression, int size) {
				return delegate.size(expression, size);
			}

			@Override
			public ArbitraryBuilder<T> size(PropertySelector propertySelector, int size) {
				return delegate.size(propertySelector, size);
			}

			@Override
			public ArbitraryBuilder<T> size(String expression, int minSize, int maxSize) {
				return delegate.size(expression, minSize, maxSize);
			}

			@Override
			public ArbitraryBuilder<T> size(PropertySelector propertySelector, int minSize, int maxSize) {
				return delegate.size(propertySelector, minSize, maxSize);
			}

			@Override
			public ArbitraryBuilder<T> minSize(String expression, int minSize) {
				return delegate.minSize(expression, minSize);
			}

			@Override
			public ArbitraryBuilder<T> minSize(PropertySelector propertySelector, int minSize) {
				return delegate.minSize(propertySelector, minSize);
			}

			@Override
			public ArbitraryBuilder<T> maxSize(String expression, int maxSize) {
				return delegate.maxSize(expression, maxSize);
			}

			@Override
			public ArbitraryBuilder<T> maxSize(PropertySelector propertySelector, int maxSize) {
				return delegate.maxSize(propertySelector, maxSize);
			}

			@Override
			public ArbitraryBuilder<T> thenApply(BiConsumer<T, ArbitraryBuilder<T>> biConsumer) {
				return fixtureMonkey.giveMeBuilder(delegate.thenApply(biConsumer).sample());
			}

			@Override
			public ArbitraryBuilder<T> acceptIf(Predicate<T> predicate, Consumer<ArbitraryBuilder<T>> consumer) {
				return fixtureMonkey.giveMeBuilder(delegate.acceptIf(predicate, consumer).sample());
			}

			@Override
			public ArbitraryBuilder<T> fixed() {
				return delegate.fixed();
			}

			@Override
			public <U> ArbitraryBuilder<U> map(Function<T, U> mapper) {
				return fixtureMonkey.giveMeBuilder(delegate.map(mapper).sample());
			}

			@Override
			public <U, R> ArbitraryBuilder<R> zipWith(ArbitraryBuilder<U> other, BiFunction<T, U, R> combinator) {
				return fixtureMonkey.giveMeBuilder(delegate.zipWith(other, combinator).sample());
			}

			@Override
			public <U, V, R> ArbitraryBuilder<R> zipWith(ArbitraryBuilder<U> other, ArbitraryBuilder<V> another,
				F3<T, U, V, R> combinator) {
				return fixtureMonkey.giveMeBuilder(delegate.zipWith(other, another, combinator).sample());
			}

			@Override
			public <U, V, W, R> ArbitraryBuilder<R> zipWith(ArbitraryBuilder<U> other, ArbitraryBuilder<V> another,
				ArbitraryBuilder<W> theOther, F4<T, U, V, W, R> combinator) {
				return fixtureMonkey.giveMeBuilder(delegate.zipWith(other, another, theOther, combinator).sample());
			}

			@Override
			public <R> ArbitraryBuilder<R> zipWith(List<ArbitraryBuilder<?>> others, Function<List<?>, R> combinator) {
				return fixtureMonkey.giveMeBuilder(delegate.zipWith(others, combinator).sample());
			}

			@Override
			public Arbitrary<T> build() {
				return delegate.build();
			}

			@Override
			public T sample() {
				return delegate.sample();
			}

			@Override
			public List<T> sampleList(int size) {
				return delegate.sampleList(size);
			}

			@Override
			public Stream<T> sampleStream() {
				return delegate.sampleStream();
			}

			@Override
			public ArbitraryBuilder<T> copy() {
				return delegate.copy();
			}

			@Override
			public ArbitraryBuilder<T> validOnly(boolean validOnly) {
				return delegate.validOnly(validOnly);
			}

			@Override
			public ArbitraryBuilder<T> instantiate(Instantiator instantiator) {
				return delegate.instantiate(instantiator);
			}

			@Override
			public ArbitraryBuilder<T> instantiate(Class<?> type, Instantiator instantiator) {
				return delegate.instantiate(type, instantiator);
			}

			@Override
			public ArbitraryBuilder<T> instantiate(TypeReference<?> type, Instantiator instantiator) {
				return delegate.instantiate(type, instantiator);
			}

			@Override
			public <U> ArbitraryBuilder<T> customizeProperty(
				TypedPropertySelector<U> propertySelector,
				Function<CombinableArbitrary<? extends U>, CombinableArbitrary<? extends U>>
					combinableArbitraryCustomizer) {
				return delegate.customizeProperty(propertySelector, combinableArbitraryCustomizer);
			}

			@Override
			public ArbitraryBuilderContext getActiveContext() {
				return ((ArbitraryBuilderContextProvider)delegate).getActiveContext();
			}
		}
	}
}
