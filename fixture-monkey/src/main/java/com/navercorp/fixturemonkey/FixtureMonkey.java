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

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.ObjectBuilder;
import com.navercorp.fixturemonkey.api.context.MonkeyContext;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
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
		List<MatcherOperator<Function<FixtureMonkey, ? extends ArbitraryBuilder<?>>>> registeredArbitraryBuilders,
		MonkeyManipulatorFactory monkeyManipulatorFactory,
		MonkeyExpressionFactory monkeyExpressionFactory
	) {
		this.fixtureMonkeyOptions = fixtureMonkeyOptions;
		this.manipulatorOptimizer = manipulatorOptimizer;
		this.monkeyContext = MonkeyContext.builder(fixtureMonkeyOptions).build();
		this.monkeyManipulatorFactory = monkeyManipulatorFactory;
		this.monkeyExpressionFactory = monkeyExpressionFactory;
		initializeRegisteredArbitraryBuilders(registeredArbitraryBuilders);
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

	@API(since = "1.1.12", status = Status.EXPERIMENTAL)
	@SuppressWarnings("unchecked")
	public <T> ArbitraryBuilder<T> throwMeBuilder(T... reified) {
		if (reified.length == 1) {
			return this.giveMeBuilder(reified[0]);
		} else if (reified.length > 0) {
			throw new IllegalArgumentException("reified should be empty");
		}

		return this.giveMeBuilder(getClassOf(reified));
	}

	public <T> ArbitraryBuilder<T> giveMeBuilder(TypeReference<T> type) {
		TreeRootProperty rootProperty = new RootProperty(new TypeParameterProperty(type.getAnnotatedType()));

		ArbitraryBuilderContext builderContext = monkeyContext.getRegisteredArbitraryBuilders().stream()
			.filter(it -> it.match(rootProperty))
			.map(MatcherOperator::getOperator)
			.findAny()
			.map(ArbitraryBuilderContextProvider.class::cast)
			.map(ArbitraryBuilderContextProvider::getContext)
			.orElse(ArbitraryBuilderContext.newBuilderContext(monkeyContext));

		return new DefaultArbitraryBuilder<>(
			rootProperty,
			new ArbitraryResolver(
				manipulatorOptimizer,
				monkeyManipulatorFactory,
				monkeyContext
			),
			monkeyManipulatorFactory,
			monkeyExpressionFactory,
			builderContext.copy(),
			monkeyContext,
			fixtureMonkeyOptions.getInstantiatorProcessor()
		);
	}

	public <T> ArbitraryBuilder<T> giveMeBuilder(T value) {
		ArbitraryBuilderContext context = ArbitraryBuilderContext.newBuilderContext(monkeyContext);

		ArbitraryManipulator arbitraryManipulator =
			monkeyManipulatorFactory.newArbitraryManipulator(monkeyExpressionFactory.from("$").toNodeResolver(), value);
		context.addManipulator(arbitraryManipulator);

		return new DefaultArbitraryBuilder<>(
			new RootProperty(new TypeParameterProperty(new LazyAnnotatedType<>(() -> value))),
			new ArbitraryResolver(
				manipulatorOptimizer,
				monkeyManipulatorFactory,
				monkeyContext
			),
			monkeyManipulatorFactory,
			monkeyExpressionFactory,
			context,
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

	@API(since = "1.1.12", status = Status.EXPERIMENTAL)
	@SuppressWarnings("unchecked")
	public <T> ArbitraryBuilder<T> throwMeJavaBuilder(T... reified) {
		if (reified.length == 1) {
			return this.giveMeBuilder(reified[0]);
		} else if (reified.length > 0) {
			throw new IllegalArgumentException("reified should be empty");
		}

		return this.giveMeJavaBuilder(getClassOf(reified));
	}

	public <T> JavaTypeArbitraryBuilder<T> giveMeJavaBuilder(TypeReference<T> type) {
		return new JavaTypeDefaultTypeArbitraryBuilder<>(this.giveMeBuilder(type));
	}

	public <T> Stream<T> giveMe(Class<T> type) {
		return Stream.generate(() -> this.giveMeBuilder(type).sample());
	}

	@API(since = "1.1.12", status = Status.EXPERIMENTAL)
	@SuppressWarnings("unchecked")
	public <T> Stream<T> throwMe(T... reified) {
		if (reified.length > 0) {
			throw new IllegalArgumentException("reified should be empty");
		}

		return this.giveMe(getClassOf(reified));
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

	@SuppressWarnings("unchecked")
	public <T> T throwMeOne(T... reified) {
		if (reified.length > 0) {
			throw new IllegalArgumentException("reified should be empty");
		}

		return this.giveMeOne(getClassOf(reified));
	}

	public <T> T giveMeOne(TypeReference<T> typeReference) {
		return this.giveMe(typeReference, 1).get(0);
	}

	public <T> Arbitrary<T> giveMeArbitrary(Class<T> type) {
		return this.giveMeBuilder(type).build();
	}

	@API(since = "1.1.12", status = Status.EXPERIMENTAL)
	@SuppressWarnings("unchecked")
	public <T> Arbitrary<T> throwMeArbitrary(T... reified) {
		if (reified.length > 0) {
			throw new IllegalArgumentException("reified should be empty");
		}

		return this.giveMeArbitrary(getClassOf(reified));
	}

	public <T> Arbitrary<T> giveMeArbitrary(TypeReference<T> typeReference) {
		return this.giveMeBuilder(typeReference).build();
	}

	private void initializeRegisteredArbitraryBuilders(
		List<MatcherOperator<Function<FixtureMonkey, ? extends ArbitraryBuilder<?>>>> registeredArbitraryBuilders
	) {
		List<? extends MatcherOperator<? extends ObjectBuilder<?>>> generatedRegisteredArbitraryBuilder =
			registeredArbitraryBuilders.stream()
				.map(it -> new MatcherOperator<>(it.getMatcher(), (ObjectBuilder<?>)(it.getOperator().apply(this))))
				.collect(toList());

		for (int i = generatedRegisteredArbitraryBuilder.size() - 1; i >= 0; i--) {
			monkeyContext.getRegisteredArbitraryBuilders().add(generatedRegisteredArbitraryBuilder.get(i));
		}
	}

	/**
	 * Implemented in Mockito since 4.9.0.
	 * <a href="https://github.com/mockito/mockito/pull/2779#issuecomment-1312693742">...</a>
	 * <p>
	 * Using reified array type to infer the type of the array.
	 */
	@SuppressWarnings("unchecked")
	private static <T> Class<T> getClassOf(T[] array) {
		return (Class<T>)array.getClass().getComponentType();
	}
}
