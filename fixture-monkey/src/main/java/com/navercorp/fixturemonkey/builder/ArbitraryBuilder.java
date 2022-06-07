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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.api.type.Types.UnidentifiableType;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryExpression;
import com.navercorp.fixturemonkey.customizer.MapSpec;
import com.navercorp.fixturemonkey.resolver.ApplyNodeCountManipulator;
import com.navercorp.fixturemonkey.resolver.ArbitraryManipulator;
import com.navercorp.fixturemonkey.resolver.ArbitraryResolver;
import com.navercorp.fixturemonkey.resolver.ArbitraryTraverser;
import com.navercorp.fixturemonkey.resolver.CompositeNodeManipulator;
import com.navercorp.fixturemonkey.resolver.ExpressionNodeResolver;
import com.navercorp.fixturemonkey.resolver.NodeManipulator;
import com.navercorp.fixturemonkey.resolver.NodeFilterManipulator;
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

	public ArbitraryBuilder<T> setMap(String mapName, Consumer<MapSpec> mapSpecSupplier) {
		MapSpec mapSpec = new MapSpec(traverser);
		mapSpecSupplier.accept(mapSpec);

		List<NodeManipulator> mapManipulators = mapSpec.getManipulators();
		ExpressionNodeResolver nodeResolver = new ExpressionNodeResolver(ArbitraryExpression.from(mapName));

		this.manipulators.add(
			new ArbitraryManipulator(
				nodeResolver,
				new CompositeNodeManipulator(mapManipulators.toArray(new NodeManipulator[0]))
			)
		);
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
		long limit
	) {
		return this.setPostCondition(expression, type, filter, (int)limit);
	}

	public <U> ArbitraryBuilder<T> setPostCondition(
		String expression,
		Class<U> type,
		Predicate<U> filter,
		int limit
	) {
		ArbitraryExpression arbitraryExpression = ArbitraryExpression.from(expression);
		this.manipulators.add(
			new ArbitraryManipulator(
				new ExpressionNodeResolver(arbitraryExpression),
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
		U mapped = mapper.apply(this.sample());

		if (mapped == null) {
			RootProperty property = new RootProperty(generateAnnotatedTypeWithType(UnidentifiableType.class));
			return new ArbitraryBuilder<>(
				property,
				Collections.emptyList(), // could not apply any manipulators, since type is unidentifiable.
				this.resolver,
				this.traverser,
				this.validator
			);
		}

		RootProperty property = new RootProperty(generateAnnotatedTypeWithType(mapped.getClass()));
		List<ArbitraryManipulator> manipulators = new ArrayList<>();
		manipulators.add(
			new ArbitraryManipulator(
				new RootNodeResolver(),
				new NodeSetDecomposedValueManipulator<>(traverser, mapped)
			)
		);

		return new ArbitraryBuilder<>(
			property,
			manipulators,
			resolver,
			traverser,
			validator
		);
	}

	private <U> AnnotatedType generateAnnotatedTypeWithType(Class<U> type) {
		return new AnnotatedType() {
			@Override
			public Type getType() {
				return type;
			}

			@Override
			public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
				return null;
			}

			@Override
			public Annotation[] getAnnotations() {
				return new Annotation[0];
			}

			@Override
			public Annotation[] getDeclaredAnnotations() {
				return new Annotation[0];
			}
		};
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
