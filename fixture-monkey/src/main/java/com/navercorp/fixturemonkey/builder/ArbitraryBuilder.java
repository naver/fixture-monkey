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

import java.util.List;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression;
import com.navercorp.fixturemonkey.customizer.MapSpec;
import com.navercorp.fixturemonkey.resolver.ApplyNodeCountManipulator;
import com.navercorp.fixturemonkey.resolver.ArbitraryManipulator;
import com.navercorp.fixturemonkey.resolver.ArbitraryResolver;
import com.navercorp.fixturemonkey.resolver.ArbitraryTraverser;
import com.navercorp.fixturemonkey.resolver.ExpressionNodeResolver;
import com.navercorp.fixturemonkey.resolver.NodeAddMapEntryManipulator;
import com.navercorp.fixturemonkey.resolver.NodeNullityManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSetArbitraryManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSetDecomposedValueManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSetLazyManipulator;
import com.navercorp.fixturemonkey.resolver.NodeSizeManipulator;
import com.navercorp.fixturemonkey.resolver.RootNodeResolver;
import com.navercorp.fixturemonkey.validator.ArbitraryValidator;

// TODO: remove extends com.navercorp.fixturemonkey.ArbitraryBuilder<T> inheritance in 1.0.0
@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ArbitraryBuilder<T> extends com.navercorp.fixturemonkey.ArbitraryBuilder<T> {
	private final RootProperty rootProperty;
	private final List<ArbitraryManipulator> manipulators;
	private final ArbitraryResolver resolver;
	private final ArbitraryTraverser traverser;
	private final ArbitraryValidator validator;
	private boolean validOnly = true;

	public ArbitraryBuilder(
		RootProperty rootProperty,
		List<ArbitraryManipulator> manipulators,
		ArbitraryResolver resolver,
		ArbitraryTraverser traverser,
		ArbitraryValidator validator
	) {
		super();
		this.rootProperty = rootProperty;
		this.manipulators = manipulators;
		this.resolver = resolver;
		this.traverser = traverser;
		this.validator = validator;
	}

	@Override
	public ArbitraryBuilder<T> validOnly(boolean validOnly) {
		this.validOnly = validOnly;
		return this;
	}

	private ArbitraryBuilder<T> set(
		String expression,
		@Nullable Object value,
		int limit
	) {
		ExpressionNodeResolver nodeResolver = new ExpressionNodeResolver(ArbitraryExpression.from(expression));
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

	public void set(String mapName, List isSetKey, Object key, Object value) {
		ExpressionNodeResolver nodeResolver = new ExpressionNodeResolver(ArbitraryExpression.from(mapName, isSetKey));
		manipulators.add(
			new ArbitraryManipulator(
				nodeResolver,
				new NodeAddMapEntryManipulator(traverser, key, value)
			)
		);
	}

	@Override
	public ArbitraryBuilder<T> set(
		String expression,
		@Nullable Object value,
		long limit
	) {
		return this.set(expression, value, (int)limit);
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

		this.manipulators.add(
			new ArbitraryManipulator(
				new ExpressionNodeResolver(ArbitraryExpression.from(expression)),
				new NodeSizeManipulator(
					traverser,
					min,
					max
				)
			)
		);
		return this;
	}

	public ArbitraryBuilder<T> setMap(String mapName, Consumer<MapSpec> mapSpecSupplier) {
		MapSpec mapSpec = new MapSpec(mapName);
		mapSpecSupplier.accept(mapSpec);
		mapSpec.visit(this);
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
		BiConsumer<T, com.navercorp.fixturemonkey.ArbitraryBuilder<T>> biConsumer
	) {
		ArbitraryBuilder<T> copied = this.copy();

		this.manipulators.add(
			new ArbitraryManipulator(
				new RootNodeResolver(),
				new NodeSetLazyManipulator<>(
					traverser,
					() -> {
						T sampled = copied.fixed().sample();
						biConsumer.accept(sampled, copied);
						return copied.sample();
					}
				)
			)
		);
		return this;
	}

	@Override
	public ArbitraryBuilder<T> acceptIf(
		Predicate<T> predicate,
		Consumer<com.navercorp.fixturemonkey.ArbitraryBuilder<T>> consumer
	) {
		return apply((it, builder) -> {
			if (predicate.test(it)) {
				consumer.accept(builder);
			}
		});
	}

	@Override
	public ArbitraryBuilder<T> setNull(String expression) {
		this.manipulators.add(new ArbitraryManipulator(
			new ExpressionNodeResolver(ArbitraryExpression.from(expression)),
			new NodeNullityManipulator(true)
		));
		return this;
	}

	@Override
	public ArbitraryBuilder<T> setNotNull(String expression) {
		this.manipulators.add(new ArbitraryManipulator(
			new ExpressionNodeResolver(ArbitraryExpression.from(expression)),
			new NodeNullityManipulator(false)
		));
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Arbitrary<T> build() {
		return new ArbitraryValue<>(
			() -> (Arbitrary<T>)this.resolver.resolve(this.rootProperty, this.manipulators),
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

	public List<T> sampleList(int size) {
		return this.sampleStream().limit(size).collect(toList());
	}

	@Override
	public ArbitraryBuilder<T> copy() {
		return new ArbitraryBuilder<>(
			rootProperty,
			new ArrayList<>(manipulators),
			resolver,
			traverser,
			validator
		);
	}
}
