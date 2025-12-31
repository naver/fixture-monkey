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

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.NonNull;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.ObjectBuilder;
import com.navercorp.fixturemonkey.api.context.MonkeyContext;
import com.navercorp.fixturemonkey.api.matcher.NamedMatcher;
import com.navercorp.fixturemonkey.api.matcher.PriorityMatcherOperator;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.api.property.TreeRootProperty;
import com.navercorp.fixturemonkey.api.property.TypeParameterProperty;
import com.navercorp.fixturemonkey.api.type.LazyAnnotatedType;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.builder.ArbitraryBuilderContext;
import com.navercorp.fixturemonkey.builder.ArbitraryBuilderContextProvider;
import com.navercorp.fixturemonkey.builder.DefaultArbitraryBuilder;
import com.navercorp.fixturemonkey.builder.JavaTypeDefaultTypeArbitraryBuilder;
import com.navercorp.fixturemonkey.customizer.ArbitraryManipulator;
import com.navercorp.fixturemonkey.customizer.MonkeyManipulatorFactory;
import com.navercorp.fixturemonkey.experimental.ExperimentalArbitraryBuilder;
import com.navercorp.fixturemonkey.expression.MonkeyExpressionFactory;
import com.navercorp.fixturemonkey.resolver.ArbitraryResolver;
import com.navercorp.fixturemonkey.resolver.ManipulatorOptimizer;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class FixtureMonkey {
	private final FixtureMonkeyOptions fixtureMonkeyOptions;
	private final ManipulatorOptimizer manipulatorOptimizer;
	private final MonkeyContext monkeyContext;
	private final MonkeyManipulatorFactory monkeyManipulatorFactory;
	private final MonkeyExpressionFactory monkeyExpressionFactory;

	public FixtureMonkey(
		FixtureMonkeyOptions fixtureMonkeyOptions,
		ManipulatorOptimizer manipulatorOptimizer,
		List<PriorityMatcherOperator<Function<FixtureMonkey,
			? extends ArbitraryBuilder<?>>>> registeredArbitraryBuildersWithPriority,
		MonkeyManipulatorFactory monkeyManipulatorFactory,
		MonkeyExpressionFactory monkeyExpressionFactory,
		Map<String, PriorityMatcherOperator<Function<FixtureMonkey,
			? extends ArbitraryBuilder<?>>>> registeredPriorityMatchersByName
	) {
		this.fixtureMonkeyOptions = fixtureMonkeyOptions;
		this.manipulatorOptimizer = manipulatorOptimizer;
		this.monkeyContext = MonkeyContext.builder(fixtureMonkeyOptions).build();
		this.monkeyManipulatorFactory = monkeyManipulatorFactory;
		this.monkeyExpressionFactory = monkeyExpressionFactory;
		initializeRegisteredArbitraryBuilders(registeredArbitraryBuildersWithPriority);
		initializeNamedArbitraryBuilderMap(registeredPriorityMatchersByName);
	}

	public static FixtureMonkeyBuilder builder() {
		return new FixtureMonkeyBuilder();
	}

	public static FixtureMonkey create() {
		return builder().build();
	}

	public <T> ArbitraryBuilder<T> giveMeBuilder(Class<T> type) {
		TypeReference<T> typeReference = new TypeReference<T>(type) {
		};
		return giveMeBuilder(typeReference);
	}

	public <T> ArbitraryBuilder<T> giveMeBuilder(TypeReference<T> type) {
		TreeRootProperty rootProperty = new RootProperty(new TypeParameterProperty(type.getAnnotatedType()));

		List<PriorityMatcherOperator<ArbitraryBuilderContext>> standByContexts =
			monkeyContext.getRegisteredArbitraryBuilders().stream()
				.filter(it -> it.match(rootProperty))
				.map(it ->
					new PriorityMatcherOperator<>(
						it.getMatcher(),
						((ArbitraryBuilderContextProvider)it.getOperator()).getActiveContext(),
						it.getPriority()
					)
				)
				.collect(toList());

		ArbitraryBuilderContext newActiveBuilderContext =
			ArbitraryBuilderContext.newBuilderContext(monkeyContext);

		return new DefaultArbitraryBuilder<>(
			rootProperty,
			new ArbitraryResolver(
				manipulatorOptimizer,
				monkeyManipulatorFactory,
				monkeyContext
			),
			monkeyManipulatorFactory,
			monkeyExpressionFactory,
			newActiveBuilderContext,
			standByContexts,
			monkeyContext,
			fixtureMonkeyOptions.getInstantiatorProcessor()
		);
	}

	public <T> ArbitraryBuilder<T> giveMeBuilder(T value) {
		ArbitraryBuilderContext newActiveBuilderContext = ArbitraryBuilderContext.newBuilderContext(monkeyContext);

		ArbitraryManipulator arbitraryManipulator =
			monkeyManipulatorFactory.newArbitraryManipulator(monkeyExpressionFactory.from("$").toNodeResolver(), value);
		newActiveBuilderContext.addManipulator(arbitraryManipulator);

		return new DefaultArbitraryBuilder<>(
			new RootProperty(new TypeParameterProperty(new LazyAnnotatedType<>(() -> value))),
			new ArbitraryResolver(
				manipulatorOptimizer,
				monkeyManipulatorFactory,
				monkeyContext
			),
			monkeyManipulatorFactory,
			monkeyExpressionFactory,
			newActiveBuilderContext,
			Collections.emptyList(),
			monkeyContext,
			fixtureMonkeyOptions.getInstantiatorProcessor()
		);
	}

	public <T> ExperimentalArbitraryBuilder<T> giveMeExperimentalBuilder(Class<T> type) {
		return (ExperimentalArbitraryBuilder<T>)giveMeBuilder(type);
	}

	public <T> ExperimentalArbitraryBuilder<T> giveMeExperimentalBuilder(TypeReference<T> type) {
		return (ExperimentalArbitraryBuilder<T>)giveMeBuilder(type);
	}

	public <T> JavaTypeArbitraryBuilder<T> giveMeJavaBuilder(T value) {
		return new JavaTypeDefaultTypeArbitraryBuilder<>(this.giveMeBuilder(value));
	}

	public <T> JavaTypeArbitraryBuilder<T> giveMeJavaBuilder(Class<T> type) {
		return new JavaTypeDefaultTypeArbitraryBuilder<>(this.giveMeBuilder(type));
	}

	public <T> JavaTypeArbitraryBuilder<T> giveMeJavaBuilder(TypeReference<T> type) {
		return new JavaTypeDefaultTypeArbitraryBuilder<>(this.giveMeBuilder(type));
	}

	public <T> Stream<T> giveMe(Class<T> type) {
		return Stream.generate(() -> this.giveMeBuilder(type).sample());
	}

	public <T> Stream<T> giveMe(TypeReference<T> typeReference) {
		return Stream.generate(() -> this.giveMeBuilder(typeReference).sample());
	}

	public <T> List<T> giveMe(Class<T> type, int size) {
		return this.giveMe(type).limit(size).collect(toList());
	}

	public <T> List<T> giveMe(TypeReference<T> typeReference, int size) {
		return this.giveMe(typeReference).limit(size).collect(toList());
	}

	@SuppressWarnings("return")
	public <T> @NonNull T giveMeOne(Class<T> type) {
		return this.giveMe(type, 1).get(0);
	}

	@SuppressWarnings("return")
	public <T> @NonNull T giveMeOne(TypeReference<T> typeReference) {
		return this.giveMe(typeReference, 1).get(0);
	}

	public <T> Arbitrary<T> giveMeArbitrary(Class<T> type) {
		return this.giveMeBuilder(type).build();
	}

	public <T> Arbitrary<T> giveMeArbitrary(TypeReference<T> typeReference) {
		return this.giveMeBuilder(typeReference).build();
	}

	private void initializeRegisteredArbitraryBuilders(
		List<PriorityMatcherOperator<Function<FixtureMonkey,
			? extends ArbitraryBuilder<?>>>> registeredArbitraryBuildersWithPriority
	) {
		List<? extends PriorityMatcherOperator<? extends ObjectBuilder<?>>> generatedRegisteredArbitraryBuilder =
			registeredArbitraryBuildersWithPriority.stream()
				.map(it -> new PriorityMatcherOperator<>(
					it.getMatcher(), (ObjectBuilder<?>)it.getOperator().apply(this), it.getPriority())
				)
				.collect(toList());

		for (int i = generatedRegisteredArbitraryBuilder.size() - 1; i >= 0; i--) {
			monkeyContext.getRegisteredArbitraryBuilders().add(generatedRegisteredArbitraryBuilder.get(i));
		}
	}

	private void initializeNamedArbitraryBuilderMap(
		Map<String, PriorityMatcherOperator<Function<FixtureMonkey,
			? extends ArbitraryBuilder<?>>>> mapsByRegisteredName
	) {
		mapsByRegisteredName.forEach((registeredName, matcherOperator) -> {
			monkeyContext.getRegisteredArbitraryBuilders().add(
				new PriorityMatcherOperator<>(
					new NamedMatcher(matcherOperator.getMatcher(), registeredName),
					(ObjectBuilder<?>)matcherOperator.getOperator().apply(this),
					matcherOperator.getPriority()
				)
			);
		});
	}
}
