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
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators.F3;
import net.jqwik.api.Combinators.F4;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.OldArbitraryBuilderImpl;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.api.type.LazyAnnotatedType;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.customizer.InnerSpec;
import com.navercorp.fixturemonkey.expression.MonkeyExpressionFactory;
import com.navercorp.fixturemonkey.resolver.ApplyNodeCountManipulator;
import com.navercorp.fixturemonkey.resolver.ArbitraryManipulator;
import com.navercorp.fixturemonkey.resolver.ArbitraryResolver;
import com.navercorp.fixturemonkey.resolver.ArbitraryTraverser;
import com.navercorp.fixturemonkey.resolver.ManipulateOptions;
import com.navercorp.fixturemonkey.resolver.NodeFilterManipulator;
import com.navercorp.fixturemonkey.resolver.NodeNullityManipulator;
import com.navercorp.fixturemonkey.resolver.NodeResolver;
import com.navercorp.fixturemonkey.resolver.NodeSetArbitraryManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSetDecomposedValueManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSetLazyManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSizeManipulator;
import com.navercorp.fixturemonkey.resolver.RootNodeResolver;
import com.navercorp.fixturemonkey.validator.ArbitraryValidator;

// TODO: remove extends com.navercorp.fixturemonkey.ArbitraryBuilder<T> inheritance in 1.0.0
@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class DefaultArbitraryBuilder<T> extends OldArbitraryBuilderImpl<T> {
	private final ManipulateOptions manipulateOptions;
	private final RootProperty rootProperty;
	private final ArbitraryResolver resolver;
	private final ArbitraryTraverser traverser;
	private final ArbitraryValidator validator;
	private final MonkeyExpressionFactory monkeyExpressionFactory;
	private final List<ArbitraryManipulator> manipulators;
	private final Set<LazyArbitrary<?>> lazyArbitraries;
	private boolean validOnly = true;

	public DefaultArbitraryBuilder(
		ManipulateOptions manipulateOptions,
		RootProperty rootProperty,
		ArbitraryResolver resolver,
		ArbitraryTraverser traverser,
		ArbitraryValidator validator,
		List<ArbitraryManipulator> manipulators,
		Set<LazyArbitrary<?>> lazyArbitraries
	) {
		super();
		this.manipulateOptions = manipulateOptions;
		this.rootProperty = rootProperty;
		this.resolver = resolver;
		this.traverser = traverser;
		this.validator = validator;
		this.manipulators = manipulators;
		this.lazyArbitraries = lazyArbitraries;
		this.monkeyExpressionFactory = manipulateOptions.getDefaultMonkeyExpressionFactory();
	}

	@Override
	public ArbitraryBuilder<T> validOnly(boolean validOnly) {
		this.validOnly = validOnly;
		return this;
	}

	@Override
	public ArbitraryBuilder<T> set(
		String expression,
		@Nullable Object value,
		int limit
	) {
		NodeResolver nodeResolver = monkeyExpressionFactory.from(expression).toNodeResolver();

		if (value instanceof Arbitrary) {
			manipulators.add(
				new ArbitraryManipulator(
					nodeResolver,
					new ApplyNodeCountManipulator(
						new NodeSetArbitraryManipulator<>((Arbitrary<?>)value),
						limit
					)
				)
			);
		} else if (value == null) {
			this.setNull(expression);
		} else {
			manipulators.add(
				new ArbitraryManipulator(
					nodeResolver,
					new ApplyNodeCountManipulator(
						new NodeSetDecomposedValueManipulator<>(traverser, value),
						limit
					)
				)
			);
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
	public ArbitraryBuilder<T> setInner(String expression, Consumer<InnerSpec> specSpecifier) {
		NodeResolver nodeResolver = monkeyExpressionFactory.from(expression).toNodeResolver();
		InnerSpec innerSpec = new InnerSpec(traverser, nodeResolver);
		specSpecifier.accept(innerSpec);
		List<ArbitraryManipulator> mapManipulators = innerSpec.getArbitraryManipulators();
		manipulators.addAll(mapManipulators);
		return this;
	}

	@Override
	public ArbitraryBuilder<T> minSize(String expression, int min) {
		return this.size(expression, min, min + DEFAULT_ELEMENT_MAX_SIZE);
	}

	@Override
	public ArbitraryBuilder<T> maxSize(String expression, int max) {
		return this.size(expression, Math.max(0, max - DEFAULT_ELEMENT_MAX_SIZE), max);
	}

	@Override
	public ArbitraryBuilder<T> size(String expression, int size) {
		return this.size(expression, size, size);
	}

	@Override
	public ArbitraryBuilder<T> size(String expression, int min, int max) {
		if (min > max) {
			throw new IllegalArgumentException("should be min > max, min : " + min + " max : " + max);
		}

		NodeResolver nodeResolver = monkeyExpressionFactory.from(expression).toNodeResolver();

		this.manipulators.add(
			new ArbitraryManipulator(
				nodeResolver,
				new NodeSizeManipulator(
					traverser,
					min,
					max
				)
			)
		);
		return this;
	}

	@Override
	public ArbitraryBuilder<T> fixed() {
		this.manipulators.add(
			new ArbitraryManipulator(
				new RootNodeResolver(),
				new NodeSetDecomposedValueManipulator<>(traverser, this.sample())
			)
		);
		return this;
	}

	@Override
	public ArbitraryBuilder<T> apply(
		BiConsumer<T, ArbitraryBuilder<T>> biConsumer
	) {
		ArbitraryBuilder<T> copied = this.copy();

		LazyArbitrary<T> lazyArbitrary = LazyArbitrary.lazy(
			() -> {
				T sampled = copied.fixed().sample();
				biConsumer.accept(sampled, copied);
				return copied.sample();
			}
		);

		this.lazyArbitraries.add(lazyArbitrary);
		this.manipulators.add(
			new ArbitraryManipulator(
				new RootNodeResolver(),
				new NodeSetLazyManipulator<>(
					traverser,
					lazyArbitrary
				)
			)
		);
		return this;
	}

	@Override
	public ArbitraryBuilder<T> acceptIf(
		Predicate<T> predicate,
		Consumer<ArbitraryBuilder<T>> consumer
	) {
		return apply((it, builder) -> {
			if (predicate.test(it)) {
				consumer.accept(builder);
			}
		});
	}

	@Override
	public ArbitraryBuilder<T> setNull(String expression) {
		NodeResolver nodeResolver = monkeyExpressionFactory.from(expression).toNodeResolver();

		this.manipulators.add(new ArbitraryManipulator(
			nodeResolver,
			new NodeNullityManipulator(true)
		));
		return this;
	}

	@Override
	public ArbitraryBuilder<T> setNotNull(String expression) {
		NodeResolver nodeResolver = monkeyExpressionFactory.from(expression).toNodeResolver();

		this.manipulators.add(new ArbitraryManipulator(
			nodeResolver,
			new NodeNullityManipulator(false)
		));
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArbitraryBuilder<T> setPostCondition(Predicate<T> filter) {
		return this.setPostCondition(HEAD_NAME, (Class<T>)Types.getActualType(rootProperty.getType()), filter);
	}

	@Override
	public <U> ArbitraryBuilder<T> setPostCondition(
		String expression,
		Class<U> type,
		Predicate<U> filter
	) {
		return this.setPostCondition(expression, type, filter, MAX_MANIPULATION_COUNT);
	}

	@Override
	public <U> ArbitraryBuilder<T> setPostCondition(
		String expression,
		Class<U> type,
		Predicate<U> filter,
		int limit
	) {
		NodeResolver nodeResolver = monkeyExpressionFactory.from(expression).toNodeResolver();

		this.manipulators.add(
			new ArbitraryManipulator(
				nodeResolver,
				new ApplyNodeCountManipulator(
					new NodeFilterManipulator(type, filter),
					limit
				)
			)
		);
		return this;
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

	@SuppressWarnings("unchecked")
	@Override
	public Arbitrary<T> build() {
		return new ArbitraryValue<>(
			() -> {
				Arbitrary<T> arbitrary = (Arbitrary<T>)this.resolver.resolve(this.rootProperty, this.manipulators);
				lazyArbitraries.forEach(LazyArbitrary::clear);
				return arbitrary;
			},
			this.validator,
			this.validOnly
		);
	}

	@Override
	public T sample() {
		return this.build().sample();
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
			manipulateOptions,
			rootProperty,
			resolver,
			traverser,
			validator,
			new ArrayList<>(this.manipulators),
			new HashSet<>(this.lazyArbitraries)
		);
	}

	private <R> DefaultArbitraryBuilder<R> generateArbitraryBuilderLazily(LazyArbitrary<R> lazyArbitrary) {
		List<ArbitraryManipulator> manipulators = new ArrayList<>();
		manipulators.add(
			new ArbitraryManipulator(
				new RootNodeResolver(),
				new NodeSetLazyManipulator<>(traverser, lazyArbitrary)
			)
		);

		Set<LazyArbitrary<?>> lazyArbitraries = new HashSet<>();
		lazyArbitraries.add(lazyArbitrary);

		return new DefaultArbitraryBuilder<>(
			manipulateOptions,
			new RootProperty(new LazyAnnotatedType<>(lazyArbitrary::getValue)),
			resolver,
			traverser,
			validator,
			manipulators,
			lazyArbitraries
		);
	}
}
