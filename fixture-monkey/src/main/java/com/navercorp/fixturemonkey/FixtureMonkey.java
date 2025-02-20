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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.context.MonkeyContext;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.matcher.NamedMatcher;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.api.random.Randoms;
import com.navercorp.fixturemonkey.api.type.LazyAnnotatedType;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.customizer.ArbitraryManipulator;
import com.navercorp.fixturemonkey.customizer.MonkeyManipulatorFactory;
import com.navercorp.fixturemonkey.customizer.PriorityMatcherOperator;
import com.navercorp.fixturemonkey.experimental.ExperimentalArbitraryBuilder;
import com.navercorp.fixturemonkey.resolver.ArbitraryBuilderContext;
import com.navercorp.fixturemonkey.resolver.ArbitraryResolver;
import com.navercorp.fixturemonkey.resolver.DefaultArbitraryBuilder;
import com.navercorp.fixturemonkey.resolver.ManipulatorOptimizer;
import com.navercorp.fixturemonkey.tree.ArbitraryTraverser;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class FixtureMonkey {
	private final FixtureMonkeyOptions fixtureMonkeyOptions;
	private final ArbitraryTraverser traverser;
	private final ManipulatorOptimizer manipulatorOptimizer;
	private final MonkeyContext monkeyContext;
	private final List<PriorityMatcherOperator<? extends ArbitraryBuilder<?>>> registeredArbitraryBuilders
		= new ArrayList<>();
	private final MonkeyManipulatorFactory monkeyManipulatorFactory;

	public FixtureMonkey(
		FixtureMonkeyOptions fixtureMonkeyOptions,
		ArbitraryTraverser traverser,
		ManipulatorOptimizer manipulatorOptimizer,
		MonkeyContext monkeyContext,
		List<PriorityMatcherOperator<Function<FixtureMonkey,
			? extends ArbitraryBuilder<?>>>> registeredArbitraryBuildersWithPriority,
		MonkeyManipulatorFactory monkeyManipulatorFactory,
		Map<String, PriorityMatcherOperator<Function<FixtureMonkey,
			? extends ArbitraryBuilder<?>>>> registeredPriorityMatchersByName
	) {
		this.fixtureMonkeyOptions = fixtureMonkeyOptions;
		this.traverser = traverser;
		this.manipulatorOptimizer = manipulatorOptimizer;
		this.monkeyContext = monkeyContext;
		this.monkeyManipulatorFactory = monkeyManipulatorFactory;
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
		RootProperty rootProperty = new RootProperty(type.getAnnotatedType());

		List<PriorityMatcherOperator<? extends ArbitraryBuilder<?>>> priorityOperators = registeredArbitraryBuilders
			.stream()
			.filter(it -> it.match(rootProperty))
			.sorted(Comparator.comparingInt(PriorityMatcherOperator::getPriority))
			.collect(toList());

		List<PriorityMatcherOperator<? extends ArbitraryBuilder<?>>> highestPriorityOperators
			= getHighestPriorityOperators(priorityOperators);

		if (highestPriorityOperators.size() > 1) {
			Collections.shuffle(highestPriorityOperators, Randoms.current());
		}

		ArbitraryBuilderContext builderContext = highestPriorityOperators.stream()
			.map(MatcherOperator::getOperator)
			.findAny()
			.map(DefaultArbitraryBuilder.class::cast)
			.map(DefaultArbitraryBuilder::getContext)
			.orElse(new ArbitraryBuilderContext());

		return new DefaultArbitraryBuilder<>(
			fixtureMonkeyOptions,
			rootProperty,
			new ArbitraryResolver(
				traverser,
				manipulatorOptimizer,
				monkeyManipulatorFactory,
				fixtureMonkeyOptions,
				monkeyContext,
				registeredArbitraryBuilders
			),
			traverser,
			monkeyManipulatorFactory,
			builderContext.copy(),
			registeredArbitraryBuilders,
			monkeyContext,
			manipulatorOptimizer,
			fixtureMonkeyOptions.getInstantiatorProcessor()
		);
	}

	private List<PriorityMatcherOperator<? extends ArbitraryBuilder<?>>> getHighestPriorityOperators(
		List<PriorityMatcherOperator<? extends ArbitraryBuilder<?>>> priorityOperators
	) {
		if (priorityOperators.isEmpty()) {
			return priorityOperators;
		}

		int highestPriority = priorityOperators.get(0).getPriority();

		return priorityOperators.stream()
			.filter(it -> it.getPriority() == highestPriority)
			.collect(toList());
	}

	public <T> ArbitraryBuilder<T> giveMeBuilder(T value) {
		ArbitraryBuilderContext context = new ArbitraryBuilderContext();

		ArbitraryManipulator arbitraryManipulator =
			monkeyManipulatorFactory.newArbitraryManipulator("$", value);
		context.addManipulator(arbitraryManipulator);

		return new DefaultArbitraryBuilder<>(
			fixtureMonkeyOptions,
			new RootProperty(new LazyAnnotatedType<>(() -> value)),
			new ArbitraryResolver(
				traverser,
				manipulatorOptimizer,
				monkeyManipulatorFactory,
				fixtureMonkeyOptions,
				monkeyContext,
				registeredArbitraryBuilders
			),
			traverser,
			monkeyManipulatorFactory,
			context,
			registeredArbitraryBuilders,
			monkeyContext,
			manipulatorOptimizer,
			fixtureMonkeyOptions.getInstantiatorProcessor()
		);
	}

	public <T> ExperimentalArbitraryBuilder<T> giveMeExperimentalBuilder(Class<T> type) {
		return (ExperimentalArbitraryBuilder<T>)giveMeBuilder(type);
	}

	public <T> ExperimentalArbitraryBuilder<T> giveMeExperimentalBuilder(TypeReference<T> type) {
		return (ExperimentalArbitraryBuilder<T>)giveMeBuilder(type);
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

	public <T> T giveMeOne(Class<T> type) {
		return this.giveMe(type, 1).get(0);
	}

	public <T> T giveMeOne(TypeReference<T> typeReference) {
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
		List<? extends PriorityMatcherOperator<? extends ArbitraryBuilder<?>>> generatedRegisteredArbitraryBuilder =
			registeredArbitraryBuildersWithPriority.stream()
				.map(it -> new PriorityMatcherOperator<>(
					it.getMatcher(), it.getOperator().apply(this), it.getPriority())
				)
				.collect(toList());

		for (int i = generatedRegisteredArbitraryBuilder.size() - 1; i >= 0; i--) {
			this.registeredArbitraryBuilders.add(generatedRegisteredArbitraryBuilder.get(i));
		}
	}

	private void initializeNamedArbitraryBuilderMap(
		Map<String, PriorityMatcherOperator<Function<FixtureMonkey,
			? extends ArbitraryBuilder<?>>>> mapsByRegisteredName
	) {
		mapsByRegisteredName.forEach((registeredName, matcherOperator) -> {
			registeredArbitraryBuilders.add(
				new PriorityMatcherOperator<>(
					new NamedMatcher(matcherOperator.getMatcher(), registeredName),
					matcherOperator.getOperator().apply(this),
					matcherOperator.getPriority()
				)
			);
		});
	}
}
