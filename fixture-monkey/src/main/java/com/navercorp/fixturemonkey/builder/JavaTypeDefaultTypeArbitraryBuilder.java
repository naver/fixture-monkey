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

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators.F3;
import net.jqwik.api.Combinators.F4;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.JavaTypeArbitraryBuilder;
import com.navercorp.fixturemonkey.api.ObjectBuilder;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.expression.TypedPropertySelector;
import com.navercorp.fixturemonkey.api.instantiator.Instantiator;
import com.navercorp.fixturemonkey.api.property.PropertySelector;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.customizer.InnerSpec;

@API(since = "1.1.0", status = Status.EXPERIMENTAL)
public final class JavaTypeDefaultTypeArbitraryBuilder<T>
	implements JavaTypeArbitraryBuilder<T>, ArbitraryBuilderContextProvider, ObjectBuilder<T> {
	private final ArbitraryBuilder<T> delegate;

	public JavaTypeDefaultTypeArbitraryBuilder(ArbitraryBuilder<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> set(String expression, @Nullable Object value) {
		delegate.set(expression, value);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> set(String expression, @Nullable Object value, int limit) {
		delegate.set(expression, value, limit);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> set(PropertySelector propertySelector, @Nullable Object value) {
		delegate.set(propertySelector, value);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> set(PropertySelector propertySelector, @Nullable Object value, int limit) {
		delegate.set(propertySelector, value, limit);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> set(@Nullable Object value) {
		delegate.set(value);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> setInner(InnerSpec innerSpec) {
		delegate.setInner(innerSpec);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> setLazy(String expression, Supplier<?> supplier) {
		delegate.setLazy(expression, supplier);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> setLazy(String expression, Supplier<?> supplier, int limit) {
		delegate.setLazy(expression, supplier, limit);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> setLazy(PropertySelector propertySelector, Supplier<?> supplier) {
		delegate.setLazy(propertySelector, supplier);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> setLazy(PropertySelector propertySelector, Supplier<?> supplier, int limit) {
		delegate.setLazy(propertySelector, supplier, limit);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> setNull(String expression) {
		delegate.setNull(expression);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> setNull(PropertySelector propertySelector) {
		delegate.setNull(propertySelector);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> setNotNull(String expression) {
		delegate.setNotNull(expression);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> setNotNull(PropertySelector propertySelector) {
		delegate.setNotNull(propertySelector);
		return this;
	}

	@Override
	public <U> JavaTypeArbitraryBuilder<T> setPostCondition(String expression, Class<U> type, Predicate<U> predicate) {
		delegate.setPostCondition(expression, type, predicate);
		return this;
	}

	@Override
	public <U> JavaTypeArbitraryBuilder<T> setPostCondition(
		PropertySelector propertySelector,
		Class<U> type,
		Predicate<U> predicate
	) {
		delegate.setPostCondition(propertySelector, type, predicate);
		return this;
	}

	@Override
	public <U> JavaTypeArbitraryBuilder<T> setPostCondition(
		String expression,
		Class<U> type,
		Predicate<U> predicate,
		int limit
	) {
		delegate.setPostCondition(expression, type, predicate, limit);
		return this;
	}

	@Override
	public <U> JavaTypeArbitraryBuilder<T> setPostCondition(
		PropertySelector propertySelector,
		Class<U> type,
		Predicate<U> predicate,
		int limit
	) {
		delegate.setPostCondition(propertySelector, type, predicate, limit);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> setPostCondition(Predicate<T> predicate) {
		delegate.setPostCondition(predicate);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> size(String expression, int size) {
		delegate.size(expression, size);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> size(PropertySelector propertySelector, int size) {
		delegate.size(propertySelector, size);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> size(String expression, int minSize, int maxSize) {
		delegate.size(expression, minSize, maxSize);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> size(PropertySelector propertySelector, int minSize, int maxSize) {
		delegate.size(propertySelector, minSize, maxSize);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> minSize(String expression, int minSize) {
		delegate.minSize(expression, minSize);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> minSize(PropertySelector propertySelector, int minSize) {
		delegate.minSize(propertySelector, minSize);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> maxSize(String expression, int maxSize) {
		delegate.maxSize(expression, maxSize);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> maxSize(PropertySelector propertySelector, int maxSize) {
		delegate.maxSize(propertySelector, maxSize);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> thenApply(BiConsumer<T, ArbitraryBuilder<T>> biConsumer) {
		delegate.thenApply(biConsumer);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> acceptIf(Predicate<T> predicate, Consumer<ArbitraryBuilder<T>> consumer) {
		delegate.acceptIf(predicate, consumer);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> fixed() {
		delegate.fixed();
		return this;
	}

	@Override
	public <U> JavaTypeArbitraryBuilder<U> map(Function<T, U> mapper) {
		return new JavaTypeDefaultTypeArbitraryBuilder<>(delegate.map(mapper));
	}

	@Override
	public <U, R> JavaTypeArbitraryBuilder<R> zipWith(ArbitraryBuilder<U> other, BiFunction<T, U, R> combinator) {
		return new JavaTypeDefaultTypeArbitraryBuilder<>(
			delegate.zipWith(other, combinator)
		);
	}

	@Override
	public <U, V, R> JavaTypeArbitraryBuilder<R> zipWith(
		ArbitraryBuilder<U> other,
		ArbitraryBuilder<V> another,
		F3<T, U, V, R> combinator
	) {
		return new JavaTypeDefaultTypeArbitraryBuilder<>(
			delegate.zipWith(other, another, combinator)
		);
	}

	@Override
	public <U, V, W, R> JavaTypeArbitraryBuilder<R> zipWith(
		ArbitraryBuilder<U> other,
		ArbitraryBuilder<V> another,
		ArbitraryBuilder<W> theOther,
		F4<T, U, V, W, R> combinator
	) {
		return new JavaTypeDefaultTypeArbitraryBuilder<>(
			delegate.zipWith(other, another, theOther, combinator)
		);
	}

	@Override
	public <R> JavaTypeArbitraryBuilder<R> zipWith(List<ArbitraryBuilder<?>> others, Function<List<?>, R> combinator) {
		return new JavaTypeDefaultTypeArbitraryBuilder<>(
			delegate.zipWith(others, combinator)
		);
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
	public JavaTypeArbitraryBuilder<T> copy() {
		return new JavaTypeDefaultTypeArbitraryBuilder<>(delegate.copy());
	}

	@Override
	public JavaTypeArbitraryBuilder<T> validOnly(boolean validOnly) {
		delegate.validOnly(validOnly);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> instantiate(Instantiator instantiator) {
		delegate.instantiate(instantiator);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> instantiate(Class<?> type, Instantiator instantiator) {
		delegate.instantiate(type, instantiator);
		return this;
	}

	@Override
	public JavaTypeArbitraryBuilder<T> instantiate(TypeReference<?> type, Instantiator instantiator) {
		delegate.instantiate(type, instantiator);
		return this;
	}

	@Override
	public <U> ArbitraryBuilder<T> customizeProperty(
		TypedPropertySelector<U> propertySelector,
		Function<CombinableArbitrary<? extends U>, CombinableArbitrary<? extends U>> combinableArbitraryCustomizer
	) {
		return delegate.customizeProperty(propertySelector, combinableArbitraryCustomizer);
	}

	@Override
	public ArbitraryBuilderContext getActiveContext() {
		return ((ArbitraryBuilderContextProvider)delegate).getActiveContext();
	}
}
