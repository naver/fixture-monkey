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

package com.navercorp.fixturemonkey.builder;

import static com.navercorp.fixturemonkey.Constants.DEFAULT_ELEMENT_MAX_SIZE;
import static com.navercorp.fixturemonkey.Constants.HEAD_NAME;
import static com.navercorp.fixturemonkey.Constants.MAX_MANIPULATION_COUNT;
import static com.navercorp.fixturemonkey.customizer.Values.NOT_NULL;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators.F3;
import net.jqwik.api.Combinators.F4;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.context.MonkeyContext;
import com.navercorp.fixturemonkey.api.experimental.TypedPropertySelector;
import com.navercorp.fixturemonkey.api.expression.ExpressionGenerator;
import com.navercorp.fixturemonkey.api.instantiator.Instantiator;
import com.navercorp.fixturemonkey.api.instantiator.InstantiatorProcessResult;
import com.navercorp.fixturemonkey.api.instantiator.InstantiatorProcessor;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.property.PropertySelector;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.api.type.LazyAnnotatedType;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.customizer.ArbitraryManipulator;
import com.navercorp.fixturemonkey.customizer.ContainerInfoManipulator;
import com.navercorp.fixturemonkey.customizer.InnerSpec;
import com.navercorp.fixturemonkey.customizer.ManipulatorSet;
import com.navercorp.fixturemonkey.customizer.MonkeyManipulatorFactory;
import com.navercorp.fixturemonkey.experimental.ExperimentalArbitraryBuilder;
import com.navercorp.fixturemonkey.resolver.ArbitraryResolver;
import com.navercorp.fixturemonkey.tree.ArbitraryTraverser;

@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
@API(since = "0.4.0", status = Status.MAINTAINED)
public final class DefaultArbitraryBuilder<T> implements ArbitraryBuilder<T>, ExperimentalArbitraryBuilder<T> {
	private final FixtureMonkeyOptions fixtureMonkeyOptions;
	private final RootProperty rootProperty;
	private final ArbitraryResolver resolver;
	private final ArbitraryTraverser traverser;
	private final MonkeyManipulatorFactory monkeyManipulatorFactory;
	private final ArbitraryBuilderContext context;
	private final List<MatcherOperator<? extends ArbitraryBuilder<?>>> registeredArbitraryBuilders;
	private final MonkeyContext monkeyContext;
	private final InstantiatorProcessor instantiatorProcessor;

	public DefaultArbitraryBuilder(
		FixtureMonkeyOptions fixtureMonkeyOptions,
		RootProperty rootProperty,
		ArbitraryResolver resolver,
		ArbitraryTraverser traverser,
		MonkeyManipulatorFactory monkeyManipulatorFactory,
		ArbitraryBuilderContext context,
		List<MatcherOperator<? extends ArbitraryBuilder<?>>> registeredArbitraryBuilders,
		MonkeyContext monkeyContext,
		InstantiatorProcessor instantiatorProcessor
	) {
		this.fixtureMonkeyOptions = fixtureMonkeyOptions;
		this.rootProperty = rootProperty;
		this.resolver = resolver;
		this.traverser = traverser;
		this.context = context;
		this.monkeyManipulatorFactory = monkeyManipulatorFactory;
		this.registeredArbitraryBuilders = registeredArbitraryBuilders;
		this.monkeyContext = monkeyContext;
		this.instantiatorProcessor = instantiatorProcessor;
	}

	@Override
	public ArbitraryBuilder<T> validOnly(boolean validOnly) {
		this.context.setValidOnly(validOnly);
		return this;
	}

	@Override
	public ArbitraryBuilder<T> set(
		String expression,
		@Nullable Object value,
		int limit
	) {
		if (value instanceof InnerSpec) {
			this.setInner((InnerSpec)value);
		} else {
			ArbitraryManipulator arbitraryManipulator =
				monkeyManipulatorFactory.newArbitraryManipulator(expression, value, limit);
			this.context.addManipulator(arbitraryManipulator);
		}
		return this;
	}

	@Override
	public ArbitraryBuilder<T> set(
		String expression,
		@Nullable Object value
	) {
		return this.set(expression, value, MAX_MANIPULATION_COUNT);
	}

	@Override
	public ArbitraryBuilder<T> set(@Nullable Object value) {
		return this.set(HEAD_NAME, value);
	}

	@Override
	public ArbitraryBuilder<T> set(PropertySelector propertySelector, @Nullable Object value, int limit) {
		return this.set(resolveExpression(toExpressionGenerator(propertySelector)), value, limit);
	}

	@Override
	public ArbitraryBuilder<T> set(PropertySelector propertySelector, @Nullable Object value) {
		return this.set(resolveExpression(toExpressionGenerator(propertySelector)), value);
	}

	@Override
	public ArbitraryBuilder<T> setLazy(String expression, Supplier<?> supplier, int limit) {
		ArbitraryManipulator arbitraryManipulator =
			monkeyManipulatorFactory.newArbitraryManipulator(expression, supplier, limit);
		this.context.addManipulator(arbitraryManipulator);
		return this;
	}

	@Override
	public ArbitraryBuilder<T> setLazy(String expression, Supplier<?> supplier) {
		return this.setLazy(expression, supplier, MAX_MANIPULATION_COUNT);
	}

	@Override
	public ArbitraryBuilder<T> setLazy(PropertySelector propertySelector, Supplier<?> supplier, int limit) {
		return this.setLazy(resolveExpression(toExpressionGenerator(propertySelector)), supplier, limit);
	}

	@Override
	public ArbitraryBuilder<T> setLazy(PropertySelector propertySelector, Supplier<?> supplier) {
		return this.setLazy(
			resolveExpression(toExpressionGenerator(propertySelector)), supplier, MAX_MANIPULATION_COUNT
		);
	}

	@Override
	public ArbitraryBuilder<T> setInner(InnerSpec innerSpec) {
		ManipulatorSet manipulatorSet = innerSpec.getManipulatorSet(monkeyManipulatorFactory);
		List<ArbitraryManipulator> arbitraryManipulators = manipulatorSet.getArbitraryManipulators();
		List<ContainerInfoManipulator> containerInfoManipulators = manipulatorSet.getContainerInfoManipulators();

		this.context.addManipulators(arbitraryManipulators);
		this.context.addContainerInfoManipulators(containerInfoManipulators);
		return this;
	}

	@Override
	public ArbitraryBuilder<T> minSize(String expression, int minSize) {
		return this.size(expression, minSize, minSize + DEFAULT_ELEMENT_MAX_SIZE);
	}

	@Override
	public ArbitraryBuilder<T> minSize(PropertySelector propertySelector, int minSize) {
		return this.size(
			resolveExpression(toExpressionGenerator(propertySelector)),
			minSize,
			minSize + DEFAULT_ELEMENT_MAX_SIZE
		);
	}

	@Override
	public ArbitraryBuilder<T> maxSize(String expression, int maxSize) {
		return this.size(expression, Math.max(0, maxSize - DEFAULT_ELEMENT_MAX_SIZE), maxSize);
	}

	@Override
	public ArbitraryBuilder<T> maxSize(PropertySelector propertySelector, int maxSize) {
		return this.size(
			resolveExpression(toExpressionGenerator(propertySelector)),
			Math.max(0, maxSize - DEFAULT_ELEMENT_MAX_SIZE),
			maxSize
		);
	}

	@Override
	public ArbitraryBuilder<T> size(String expression, int size) {
		return this.size(expression, size, size);
	}

	@Override
	public ArbitraryBuilder<T> size(PropertySelector propertySelector, int size) {
		return this.size(resolveExpression(toExpressionGenerator(propertySelector)), size, size);
	}

	@Override
	public ArbitraryBuilder<T> size(String expression, int minSize, int maxSize) {
		if (minSize > maxSize) {
			throw new IllegalArgumentException("should be min > max, min : " + minSize + " max : " + maxSize);
		}

		ContainerInfoManipulator containerInfoManipulator =
			monkeyManipulatorFactory.newContainerInfoManipulator(expression, minSize, maxSize);

		this.context.addContainerInfoManipulator(containerInfoManipulator);
		return this;
	}

	@Override
	public ArbitraryBuilder<T> size(PropertySelector propertySelector, int minSize, int maxSize) {
		return this.size(resolveExpression(toExpressionGenerator(propertySelector)), minSize, maxSize);
	}

	@Override
	public ArbitraryBuilder<T> fixed() {
		this.context.getContainerInfoManipulators().forEach(ContainerInfoManipulator::fixed);

		this.context.markFixed();
		return this;
	}

	@Override
	public ArbitraryBuilder<T> thenApply(BiConsumer<T, ArbitraryBuilder<T>> biConsumer) {
		this.context.getContainerInfoManipulators().forEach(ContainerInfoManipulator::fixed);

		ArbitraryBuilder<T> appliedBuilder = this.copy();

		LazyArbitrary<T> lazyArbitrary = LazyArbitrary.lazy(
			() -> {
				ArbitraryBuilder<T> lazyBuilder = appliedBuilder.copy();
				T sampled = lazyBuilder.fixed().sample();
				biConsumer.accept(sampled, lazyBuilder);
				return lazyBuilder.sample();
			}
		);

		ArbitraryManipulator arbitraryManipulator =
			monkeyManipulatorFactory.newArbitraryManipulator("$", lazyArbitrary);
		this.context.addManipulator(arbitraryManipulator);
		return this;
	}

	@Override
	public ArbitraryBuilder<T> acceptIf(
		Predicate<T> predicate,
		Consumer<ArbitraryBuilder<T>> consumer
	) {
		return thenApply((it, builder) -> {
			if (predicate.test(it)) {
				consumer.accept(builder);
			}
		});
	}

	@Override
	public ArbitraryBuilder<T> setNull(String expression) {
		ArbitraryManipulator arbitraryManipulator =
			monkeyManipulatorFactory.newArbitraryManipulator(expression, null);
		this.context.addManipulator(arbitraryManipulator);
		return this;
	}

	@Override
	public ArbitraryBuilder<T> setNull(PropertySelector propertySelector) {
		return this.setNull(resolveExpression(toExpressionGenerator(propertySelector)));
	}

	@Override
	public ArbitraryBuilder<T> setNotNull(String expression) {
		ArbitraryManipulator arbitraryManipulator =
			monkeyManipulatorFactory.newArbitraryManipulator(expression, NOT_NULL);

		this.context.addManipulator(arbitraryManipulator);
		return this;
	}

	@Override
	public ArbitraryBuilder<T> setNotNull(PropertySelector propertySelector) {
		return this.setNotNull(resolveExpression(toExpressionGenerator(propertySelector)));
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArbitraryBuilder<T> setPostCondition(Predicate<T> predicate) {
		return this.setPostCondition(HEAD_NAME, (Class<T>)Types.getActualType(rootProperty.getType()), predicate);
	}

	@Override
	public <U> ArbitraryBuilder<T> setPostCondition(
		String expression,
		Class<U> type,
		Predicate<U> predicate
	) {
		return this.setPostCondition(expression, type, predicate, MAX_MANIPULATION_COUNT);
	}

	@Override
	public <U> ArbitraryBuilder<T> setPostCondition(
		String expression,
		Class<U> type,
		Predicate<U> predicate,
		int limit
	) {
		ArbitraryManipulator arbitraryManipulator =
			monkeyManipulatorFactory.newArbitraryManipulator(expression, type, predicate, limit);

		this.context.addManipulator(arbitraryManipulator);
		return this;
	}

	@Override
	public <U> ArbitraryBuilder<T> setPostCondition(
		PropertySelector propertySelector,
		Class<U> type,
		Predicate<U> predicate,
		int limit
	) {
		return this.setPostCondition(
			resolveExpression(toExpressionGenerator(propertySelector)),
			type,
			predicate,
			limit
		);
	}

	@Override
	public <U> ArbitraryBuilder<T> setPostCondition(
		PropertySelector propertySelector,
		Class<U> type,
		Predicate<U> predicate
	) {
		return this.setPostCondition(resolveExpression(toExpressionGenerator(propertySelector)), type, predicate);
	}

	@Override
	public <U> ArbitraryBuilder<U> map(Function<T, U> mapper) {
		LazyArbitrary<U> lazyArbitrary = LazyArbitrary.lazy(() -> mapper.apply(this.sample()));
		return generateArbitraryBuilderLazily(lazyArbitrary);
	}

	@Override
	public <U, R> ArbitraryBuilder<R> zipWith(ArbitraryBuilder<U> other, BiFunction<T, U, R> combinator) {
		LazyArbitrary<R> lazyArbitrary = LazyArbitrary.lazy(() -> combinator.apply(this.sample(), other.sample()));
		return generateArbitraryBuilderLazily(lazyArbitrary);
	}

	@Override
	public <U, V, R> ArbitraryBuilder<R> zipWith(
		ArbitraryBuilder<U> other,
		ArbitraryBuilder<V> another,
		F3<T, U, V, R> combinator
	) {
		LazyArbitrary<R> lazyArbitrary = LazyArbitrary.lazy(
			() -> combinator.apply(this.sample(), other.sample(), another.sample())
		);
		return generateArbitraryBuilderLazily(lazyArbitrary);
	}

	@Override
	public <U, V, W, R> ArbitraryBuilder<R> zipWith(
		ArbitraryBuilder<U> other,
		ArbitraryBuilder<V> another,
		ArbitraryBuilder<W> theOther,
		F4<T, U, V, W, R> combinator
	) {
		LazyArbitrary<R> lazyArbitrary = LazyArbitrary.lazy(
			() -> combinator.apply(this.sample(), other.sample(), another.sample(), theOther.sample())
		);
		return generateArbitraryBuilderLazily(lazyArbitrary);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public <R> ArbitraryBuilder<R> zipWith(
		List<ArbitraryBuilder<?>> others,
		Function<List<?>, R> combinator
	) {
		LazyArbitrary<R> lazyArbitrary = LazyArbitrary.lazy(
			() -> {
				List combinedList = new ArrayList<>();
				combinedList.add(this.sample());
				for (ArbitraryBuilder<?> other : others) {
					combinedList.add(other.sample());
				}
				return combinator.apply(combinedList);
			}
		);
		return generateArbitraryBuilderLazily(lazyArbitrary);
	}

	@Override
	public ExperimentalArbitraryBuilder<T> instantiate(Instantiator instantiator) {
		return instantiate(
			new TypeReference<T>() {
				@Override
				public Type getType() {
					return rootProperty.getType();
				}

				@Override
				public AnnotatedType getAnnotatedType() {
					return rootProperty.getAnnotatedType();
				}
			},
			instantiator
		);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public ExperimentalArbitraryBuilder<T> instantiate(Class<?> type, Instantiator instantiator) {
		return instantiate(
			new TypeReference(type) {
			},
			instantiator
		);
	}

	@Override
	public ExperimentalArbitraryBuilder<T> instantiate(
		TypeReference<?> typeReference,
		Instantiator instantiator
	) {
		InstantiatorProcessResult result = instantiatorProcessor.process(typeReference, instantiator);

		Class<?> type = Types.getActualType(typeReference.getType());
		context.putArbitraryIntrospector(type, result.getIntrospector());
		context.putPropertyConfigurer(type, result.getProperties());
		return this;
	}

	@Override
	public <U> ArbitraryBuilder<T> customizeProperty(
		TypedPropertySelector<U> propertySelector,
		Function<CombinableArbitrary<? extends U>, CombinableArbitrary<? extends U>> combinableArbitraryCustomizer
	) {
		String expression = resolveExpression(toExpressionGenerator(propertySelector));
		context.addManipulator(
			monkeyManipulatorFactory.newArbitraryManipulator(expression, combinableArbitraryCustomizer)
		);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Arbitrary<T> build() {
		ArbitraryBuilderContext buildContext = context.copy();

		return Arbitraries.ofSuppliers(() -> (T)resolveArbitrary(buildContext).combined());
	}

	@SuppressWarnings("unchecked")
	@Override
	public T sample() {
		return (T)resolveArbitrary(context).combined();
	}

	@Override
	public Stream<T> sampleStream() {
		return this.build().sampleStream();
	}

	@Override
	public List<T> sampleList(int size) {
		return this.sampleStream().limit(size).collect(toList());
	}

	@Override
	public ArbitraryBuilder<T> copy() {
		return new DefaultArbitraryBuilder<>(
			fixtureMonkeyOptions,
			rootProperty,
			resolver,
			traverser,
			monkeyManipulatorFactory,
			context.copy(),
			registeredArbitraryBuilders,
			monkeyContext,
			instantiatorProcessor
		);
	}

	public ArbitraryBuilderContext getContext() {
		return this.context;
	}

	private CombinableArbitrary<?> resolveArbitrary(ArbitraryBuilderContext context) {
		if (context.isFixed()) {
			if (context.getFixedCombinableArbitrary() == null || context.fixedExpired()) {
				Object fixed = resolver.resolve(
						rootProperty,
						context
					)
					.combined();
				context.addManipulator(monkeyManipulatorFactory.newArbitraryManipulator("$", fixed));
				context.renewFixed(CombinableArbitrary.from(fixed));
			}
			return context.getFixedCombinableArbitrary();
		}

		return resolver.resolve(
			rootProperty,
			context
		);
	}

	private String resolveExpression(ExpressionGenerator expressionGenerator) {
		return expressionGenerator.generate(property -> {
			PropertyNameResolver propertyNameResolver = fixtureMonkeyOptions.getPropertyNameResolver(property);
			return propertyNameResolver.resolve(property);
		});
	}

	private <R> DefaultArbitraryBuilder<R> generateArbitraryBuilderLazily(LazyArbitrary<R> lazyArbitrary) {
		ArbitraryBuilderContext context = new ArbitraryBuilderContext();
		ArbitraryManipulator arbitraryManipulator =
			monkeyManipulatorFactory.newArbitraryManipulator("$", lazyArbitrary);
		context.addManipulator(arbitraryManipulator);

		return new DefaultArbitraryBuilder<>(
			fixtureMonkeyOptions,
			new RootProperty(new LazyAnnotatedType<>(lazyArbitrary::getValue)),
			resolver,
			traverser,
			monkeyManipulatorFactory,
			context,
			registeredArbitraryBuilders,
			monkeyContext,
			instantiatorProcessor
		);
	}

	private ExpressionGenerator toExpressionGenerator(PropertySelector propertySelector) {
		if (propertySelector instanceof ExpressionGenerator) {
			return (ExpressionGenerator)propertySelector;
		}
		throw new UnsupportedOperationException(
			"Given propertySelector is not supported. type of propertySelector: " + propertySelector.getClass()
		);
	}
}
