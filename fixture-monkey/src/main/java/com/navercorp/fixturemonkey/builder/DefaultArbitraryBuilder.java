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
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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
import com.navercorp.fixturemonkey.api.customizer.FixtureCustomizer;
import com.navercorp.fixturemonkey.api.expression.ExpressionGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.api.random.Randoms;
import com.navercorp.fixturemonkey.api.type.LazyAnnotatedType;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.arbitrary.BuilderManipulator;
import com.navercorp.fixturemonkey.arbitrary.ContainerSizeManipulator;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizer;
import com.navercorp.fixturemonkey.customizer.ExpressionSpec;
import com.navercorp.fixturemonkey.customizer.InnerSpec;
import com.navercorp.fixturemonkey.expression.MonkeyExpressionFactory;
import com.navercorp.fixturemonkey.resolver.ApplyNodeCountManipulator;
import com.navercorp.fixturemonkey.resolver.ArbitraryBuilderContext;
import com.navercorp.fixturemonkey.resolver.ArbitraryManipulator;
import com.navercorp.fixturemonkey.resolver.ArbitraryResolver;
import com.navercorp.fixturemonkey.resolver.ArbitraryTraverser;
import com.navercorp.fixturemonkey.resolver.BuilderManipulatorAdapter;
import com.navercorp.fixturemonkey.resolver.ContainerInfoManipulator;
import com.navercorp.fixturemonkey.resolver.IdentityNodeResolver;
import com.navercorp.fixturemonkey.resolver.ManipulateOptions;
import com.navercorp.fixturemonkey.resolver.NodeFilterManipulator;
import com.navercorp.fixturemonkey.resolver.NodeNullityManipulator;
import com.navercorp.fixturemonkey.resolver.NodeResolver;
import com.navercorp.fixturemonkey.resolver.NodeSetDecomposedValueManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSetLazyManipulator;
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
	private final ArbitraryBuilderContext context;
	private final BuilderManipulatorAdapter builderManipulatorAdapter;

	public DefaultArbitraryBuilder(
		ManipulateOptions manipulateOptions,
		RootProperty rootProperty,
		ArbitraryResolver resolver,
		ArbitraryTraverser traverser,
		ArbitraryValidator validator,
		ArbitraryBuilderContext context
	) {
		super();
		this.manipulateOptions = manipulateOptions;
		this.rootProperty = rootProperty;
		this.resolver = resolver;
		this.traverser = traverser;
		this.validator = validator;
		this.context = context;
		this.monkeyExpressionFactory = manipulateOptions.getDefaultMonkeyExpressionFactory();
		this.builderManipulatorAdapter = new BuilderManipulatorAdapter(traverser, manipulateOptions);
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
		NodeResolver nodeResolver = monkeyExpressionFactory.from(expression).toNodeResolver();

		if (value instanceof Arbitrary) {
			this.setLazy(expression, () -> ((Arbitrary<?>)value).sample(), limit);
		} else if (value instanceof DefaultArbitraryBuilder) {
			this.setLazy(expression, () -> ((DefaultArbitraryBuilder<?>)value).sample());
		} else if (value instanceof ExpressionSpec) {
			this.spec((ExpressionSpec)value);
		} else if (value == null) {
			this.setNull(expression);
		} else {
			this.context.addManipulator(
				new ArbitraryManipulator(
					nodeResolver,
					new ApplyNodeCountManipulator(
						new NodeSetDecomposedValueManipulator<>(traverser, manipulateOptions, value, false),
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
	public ArbitraryBuilder<T> set(ExpressionGenerator expressionGenerator, @Nullable Object value, int limit) {
		return this.set(resolveExpression(expressionGenerator), value, limit);
	}

	@Override
	public ArbitraryBuilder<T> set(ExpressionGenerator expressionGenerator, @Nullable Object value) {
		return this.set(resolveExpression(expressionGenerator), value);
	}

	@Override
	public ArbitraryBuilder<T> setLazy(String expression, Supplier<?> supplier, int limit) {
		NodeResolver nodeResolver = monkeyExpressionFactory.from(expression).toNodeResolver();
		LazyArbitrary<?> lazyArbitrary = LazyArbitrary.lazy(supplier);
		this.context.addManipulator(
			new ArbitraryManipulator(
				nodeResolver,
				new ApplyNodeCountManipulator(
					new NodeSetLazyManipulator<>(
						traverser,
						manipulateOptions,
						lazyArbitrary,
						false
					),
					limit
				)
			)
		);
		return this;
	}

	@Override
	public ArbitraryBuilder<T> setLazy(String expression, Supplier<?> supplier) {
		return this.setLazy(expression, supplier, MAX_MANIPULATION_COUNT);
	}

	@Override
	public ArbitraryBuilder<T> setLazy(ExpressionGenerator expressionGenerator, Supplier<?> supplier, int limit) {
		return this.setLazy(resolveExpression(expressionGenerator), supplier, limit);
	}

	@Override
	public ArbitraryBuilder<T> setLazy(ExpressionGenerator expressionGenerator, Supplier<?> supplier) {
		return this.setLazy(resolveExpression(expressionGenerator), supplier, MAX_MANIPULATION_COUNT);
	}

	@Override
	public ArbitraryBuilder<T> spec(ExpressionSpec expressionSpec) {
		List<BuilderManipulator> builderManipulators = expressionSpec.getBuilderManipulators();

		this.context.addContainerInfoManipulators(
			builderManipulators.stream()
				.filter(ContainerSizeManipulator.class::isInstance)
				.map(builderManipulatorAdapter::convertToContainerInfoManipulator)
				.collect(Collectors.toList())
		);
		this.context.addManipulators(
			builderManipulators.stream()
				.filter(it -> !(it instanceof ContainerSizeManipulator))
				.map(builderManipulatorAdapter::convertToArbitraryManipulator)
				.collect(toList())
		);
		return this;
	}

	@Override
	public ArbitraryBuilder<T> specAny(ExpressionSpec... specs) {
		if (specs == null || specs.length == 0) {
			return this;
		}

		ExpressionSpec spec = Arrays.asList(specs).get(Randoms.nextInt(specs.length));
		return this.spec(spec);
	}

	@Override
	public ArbitraryBuilder<T> setInner(String expression, Consumer<InnerSpec> specSpecifier) {
		NodeResolver nodeResolver = monkeyExpressionFactory.from(expression).toNodeResolver();
		InnerSpec innerSpec = new InnerSpec(traverser, manipulateOptions, nodeResolver);
		specSpecifier.accept(innerSpec);
		this.context.addManipulators(innerSpec.getArbitraryManipulators());
		this.context.addContainerInfoManipulators(innerSpec.getContainerInfoManipulators());
		return this;
	}

	@Override
	public ArbitraryBuilder<T> setInner(ExpressionGenerator expressionGenerator, Consumer<InnerSpec> specSpecifier) {
		return this.setInner(resolveExpression(expressionGenerator), specSpecifier);
	}

	@Override
	public ArbitraryBuilder<T> minSize(String expression, int min) {
		return this.size(expression, min, min + DEFAULT_ELEMENT_MAX_SIZE);
	}

	@Override
	public ArbitraryBuilder<T> minSize(ExpressionGenerator expressionGenerator, int min) {
		return this.size(resolveExpression(expressionGenerator), min, min + DEFAULT_ELEMENT_MAX_SIZE);
	}

	@Override
	public ArbitraryBuilder<T> maxSize(String expression, int max) {
		return this.size(expression, Math.max(0, max - DEFAULT_ELEMENT_MAX_SIZE), max);
	}

	@Override
	public ArbitraryBuilder<T> maxSize(ExpressionGenerator expressionGenerator, int max) {
		return this.size(resolveExpression(expressionGenerator), Math.max(0, max - DEFAULT_ELEMENT_MAX_SIZE), max);
	}

	@Override
	public ArbitraryBuilder<T> size(String expression, int size) {
		return this.size(expression, size, size);
	}

	@Override
	public ArbitraryBuilder<T> size(ExpressionGenerator expressionGenerator, int size) {
		return this.size(resolveExpression(expressionGenerator), size, size);
	}

	@Override
	public ArbitraryBuilder<T> size(String expression, int min, int max) {
		if (min > max) {
			throw new IllegalArgumentException("should be min > max, min : " + min + " max : " + max);
		}

		NodeResolver nodeResolver = monkeyExpressionFactory.from(expression).toNodeResolver();

		this.context.addContainerInfoManipulator(
			nodeResolver,
			new ArbitraryContainerInfo(min, max, true)
		);
		return this;
	}

	@Override
	public ArbitraryBuilder<T> size(ExpressionGenerator expressionGenerator, int min, int max) {
		return this.size(resolveExpression(expressionGenerator), min, max);
	}

	@Override
	public ArbitraryBuilder<T> fixed() {
		this.context.getContainerInfoManipulators().forEach(ContainerInfoManipulator::fixed);

		this.context.addManipulator(
			new ArbitraryManipulator(
				IdentityNodeResolver.INSTANCE,
				new NodeSetDecomposedValueManipulator<>(traverser, manipulateOptions, this.sample(), false)
			)
		);
		return this;
	}

	@Override
	public ArbitraryBuilder<T> apply(
		BiConsumer<T, ArbitraryBuilder<T>> biConsumer
	) {
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

		this.context.addManipulator(
			new ArbitraryManipulator(
				IdentityNodeResolver.INSTANCE,
				new NodeSetLazyManipulator<>(
					traverser,
					manipulateOptions,
					lazyArbitrary,
					true
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

		this.context.addManipulator(new ArbitraryManipulator(
			nodeResolver,
			new NodeNullityManipulator(true)
		));
		return this;
	}

	@Override
	public ArbitraryBuilder<T> setNull(ExpressionGenerator expressionGenerator) {
		return this.setNull(resolveExpression(expressionGenerator));
	}

	@Override
	public ArbitraryBuilder<T> setNotNull(String expression) {
		NodeResolver nodeResolver = monkeyExpressionFactory.from(expression).toNodeResolver();

		this.context.addManipulator(
			new ArbitraryManipulator(
				nodeResolver,
				new NodeNullityManipulator(false)
			)
		);
		return this;
	}

	@Override
	public ArbitraryBuilder<T> setNotNull(ExpressionGenerator expressionGenerator) {
		return this.setNotNull(resolveExpression(expressionGenerator));
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

		this.context.addManipulator(
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
	public <U> ArbitraryBuilder<T> setPostCondition(
		ExpressionGenerator expressionGenerator,
		Class<U> clazz,
		Predicate<U> filter,
		int limit
	) {
		return this.setPostCondition(resolveExpression(expressionGenerator), clazz, filter, limit);
	}

	@Override
	public <U> ArbitraryBuilder<T> setPostCondition(
		ExpressionGenerator expressionGenerator,
		Class<U> clazz,
		Predicate<U> filter
	) {
		return this.setPostCondition(resolveExpression(expressionGenerator), clazz, filter);
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
	public ArbitraryBuilder<T> customize(Class<T> type, ArbitraryCustomizer<T> customizer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ArbitraryBuilder<T> customize(MatcherOperator<FixtureCustomizer<T>> fixtureCustomizer) {
		this.context.addCustomizer(fixtureCustomizer);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Arbitrary<T> build() {
		List<ArbitraryManipulator> buildManipulators = new ArrayList<>(this.context.getManipulators());

		return new ArbitraryValue<>(
			() -> {
				Arbitrary<T> arbitrary = (Arbitrary<T>)this.resolver.resolve(
					this.rootProperty,
					buildManipulators,
					context.getCustomizers(),
					context.getContainerInfoManipulators()
				);
				return arbitrary;
			},
			this.validator,
			context.isValidOnly()
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
			context.copy()
		);
	}

	private String resolveExpression(ExpressionGenerator expressionGenerator) {
		return expressionGenerator.generate(property -> {
			PropertyNameResolver propertyNameResolver = manipulateOptions.getPropertyNameResolver(property);
			return propertyNameResolver.resolve(property);
		});
	}

	private <R> DefaultArbitraryBuilder<R> generateArbitraryBuilderLazily(LazyArbitrary<R> lazyArbitrary) {
		ArbitraryBuilderContext context = new ArbitraryBuilderContext();
		context.addManipulator(
			new ArbitraryManipulator(
				IdentityNodeResolver.INSTANCE,
				new NodeSetLazyManipulator<>(traverser, manipulateOptions, lazyArbitrary, true)
			)
		);

		return new DefaultArbitraryBuilder<>(
			manipulateOptions,
			new RootProperty(new LazyAnnotatedType<>(lazyArbitrary::getValue)),
			resolver,
			traverser,
			validator,
			context
		);
	}
}
