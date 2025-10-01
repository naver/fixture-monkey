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

import static com.navercorp.fixturemonkey.api.expression.JavaGetterMethodPropertySelector.javaGetter;

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

import com.navercorp.fixturemonkey.api.expression.JavaGetterMethodReference;
import com.navercorp.fixturemonkey.api.instantiator.Instantiator;
import com.navercorp.fixturemonkey.api.property.PropertySelector;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.customizer.InnerSpec;

@API(since = "1.1.0", status = Status.EXPERIMENTAL)
public interface JavaTypeArbitraryBuilder<T> extends ArbitraryBuilder<T> {
	@Override
	JavaTypeArbitraryBuilder<T> set(String expression, @Nullable Object value);

	@Override
	JavaTypeArbitraryBuilder<T> set(String expression, @Nullable Object value, int limit);

	@Override
	JavaTypeArbitraryBuilder<T> set(PropertySelector propertySelector, @Nullable Object value);

	@Override
	JavaTypeArbitraryBuilder<T> set(PropertySelector propertySelector, @Nullable Object value, int limit);

	@Override
	JavaTypeArbitraryBuilder<T> set(@Nullable Object value);

	@Override
	JavaTypeArbitraryBuilder<T> setInner(InnerSpec innerSpec);

	@Override
	JavaTypeArbitraryBuilder<T> setLazy(String expression, Supplier<?> supplier);

	@Override
	JavaTypeArbitraryBuilder<T> setLazy(String expression, Supplier<?> supplier, int limit);

	@Override
	JavaTypeArbitraryBuilder<T> setLazy(PropertySelector propertySelector, Supplier<?> supplier);

	@Override
	JavaTypeArbitraryBuilder<T> setLazy(PropertySelector propertySelector, Supplier<?> supplier, int limit);

	@Override
	JavaTypeArbitraryBuilder<T> setNull(String expression);

	@Override
	JavaTypeArbitraryBuilder<T> setNull(PropertySelector propertySelector);

	@Override
	JavaTypeArbitraryBuilder<T> setNotNull(String expression);

	@Override
	JavaTypeArbitraryBuilder<T> setNotNull(PropertySelector propertySelector);

	@Override
	<U> JavaTypeArbitraryBuilder<T> setPostCondition(String expression, Class<U> type, Predicate<U> predicate);

	@Override
	<U> JavaTypeArbitraryBuilder<T> setPostCondition(
		PropertySelector propertySelector,
		Class<U> type,
		Predicate<U> predicate
	);

	@Override
	<U> JavaTypeArbitraryBuilder<T> setPostCondition(
		String expression,
		Class<U> type,
		Predicate<U> predicate,
		int limit
	);

	@Override
	<U> JavaTypeArbitraryBuilder<T> setPostCondition(
		PropertySelector propertySelector,
		Class<U> type,
		Predicate<U> predicate,
		int limit
	);

	@Override
	JavaTypeArbitraryBuilder<T> setPostCondition(Predicate<T> predicate);

	@Override
	JavaTypeArbitraryBuilder<T> size(String expression, int size);

	@Override
	JavaTypeArbitraryBuilder<T> size(PropertySelector propertySelector, int size);

	@Override
	JavaTypeArbitraryBuilder<T> size(String expression, int minSize, int maxSize);

	@Override
	JavaTypeArbitraryBuilder<T> size(PropertySelector propertySelector, int minSize, int maxSize);

	@Override
	JavaTypeArbitraryBuilder<T> minSize(String expression, int minSize);

	@Override
	JavaTypeArbitraryBuilder<T> minSize(PropertySelector propertySelector, int minSize);

	@Override
	JavaTypeArbitraryBuilder<T> maxSize(String expression, int maxSize);

	@Override
	JavaTypeArbitraryBuilder<T> maxSize(PropertySelector propertySelector, int maxSize);

	@Override
	JavaTypeArbitraryBuilder<T> thenApply(BiConsumer<T, ArbitraryBuilder<T>> biConsumer);

	@Override
	JavaTypeArbitraryBuilder<T> acceptIf(Predicate<T> predicate, Consumer<ArbitraryBuilder<T>> consumer);

	@Override
	JavaTypeArbitraryBuilder<T> fixed();

	@Override
	<U> JavaTypeArbitraryBuilder<U> map(Function<T, U> mapper);

	@Override
	<U, R> JavaTypeArbitraryBuilder<R> zipWith(ArbitraryBuilder<U> other, BiFunction<T, U, R> combinator);

	@Override
	<U, V, R> JavaTypeArbitraryBuilder<R> zipWith(
		ArbitraryBuilder<U> other,
		ArbitraryBuilder<V> another,
		F3<T, U, V, R> combinator
	);

	@Override
	<U, V, W, R> JavaTypeArbitraryBuilder<R> zipWith(
		ArbitraryBuilder<U> other,
		ArbitraryBuilder<V> another,
		ArbitraryBuilder<W> theOther,
		F4<T, U, V, W, R> combinator
	);

	@Override
	<R> JavaTypeArbitraryBuilder<R> zipWith(
		List<ArbitraryBuilder<?>> others,
		Function<List<?>, R> combinator
	);

	@Override
	Arbitrary<T> build();

	@Override
	T sample();

	@Override
	List<T> sampleList(int size);

	@Override
	Stream<T> sampleStream();

	@Override
	JavaTypeArbitraryBuilder<T> copy();

	@Override
	JavaTypeArbitraryBuilder<T> validOnly(boolean validOnly);

	@Override
	JavaTypeArbitraryBuilder<T> instantiate(Instantiator instantiator);

	@Override
	JavaTypeArbitraryBuilder<T> instantiate(Class<?> type, Instantiator instantiator);

	@Override
	JavaTypeArbitraryBuilder<T> instantiate(TypeReference<?> type, Instantiator instantiator);

	default <R> JavaTypeArbitraryBuilder<T> setExpGetter(
		JavaGetterMethodReference<T, R> methodReference,
		@Nullable Object value,
		int limit
	) {
		return this.set(javaGetter(methodReference), value, limit);
	}

	default <R> JavaTypeArbitraryBuilder<T> setExpGetter(
		JavaGetterMethodReference<T, R> methodReference,
		@Nullable Object value
	) {
		return this.set(javaGetter(methodReference), value);
	}

	default <R> JavaTypeArbitraryBuilder<T> setExpGetter(
		PropertySelector propertySelector,
		@Nullable Object value,
		int limit
	) {
		return this.set(propertySelector, value, limit);
	}

	default <R> JavaTypeArbitraryBuilder<T> setExpGetter(
		PropertySelector propertySelector,
		@Nullable Object value
	) {
		return this.set(propertySelector, value);
	}

	default <R> JavaTypeArbitraryBuilder<T> setPostConditionExpGetter(
		JavaGetterMethodReference<T, R> methodReference,
		Class<R> type,
		Predicate<R> filter,
		int limit
	) {
		return this.setPostCondition(javaGetter(methodReference), type, filter, limit);
	}

	default <R> JavaTypeArbitraryBuilder<T> setPostConditionExpGetter(
		JavaGetterMethodReference<T, R> methodReference,
		Class<R> type,
		Predicate<R> filter
	) {
		return this.setPostCondition(javaGetter(methodReference), type, filter);
	}

	default <R> JavaTypeArbitraryBuilder<T> setPostConditionExpGetter(
		PropertySelector propertySelector,
		Class<R> type,
		Predicate<R> filter,
		int limit
	) {
		return this.setPostCondition(propertySelector, type, filter, limit);
	}

	default <R> JavaTypeArbitraryBuilder<T> setPostConditionExpGetter(
		PropertySelector propertySelector,
		Class<R> type,
		Predicate<R> filter
	) {
		return this.setPostCondition(propertySelector, type, filter);
	}

	default <R> JavaTypeArbitraryBuilder<T> sizeExpGetter(
		JavaGetterMethodReference<T, R> methodReference,
		int size
	) {
		return this.size(javaGetter(methodReference), size);
	}

	default <R> JavaTypeArbitraryBuilder<T> sizeExpGetter(
		JavaGetterMethodReference<T, R> methodReference,
		int minSize,
		int maxSize
	) {
		return this.size(javaGetter(methodReference), minSize, maxSize);
	}

	default <R> JavaTypeArbitraryBuilder<T> sizeExpGetter(
		PropertySelector propertySelector,
		int size
	) {
		return this.size(propertySelector, size);
	}

	default <R> JavaTypeArbitraryBuilder<T> sizeExpGetter(
		PropertySelector propertySelector,
		int minSize,
		int maxSize
	) {
		return this.size(propertySelector, minSize, maxSize);
	}

	default <R> JavaTypeArbitraryBuilder<T> minSizeExpGetter(
		JavaGetterMethodReference<T, R> methodReference,
		int minSize
	) {
		return this.minSize(javaGetter(methodReference), minSize);
	}

	default <R> JavaTypeArbitraryBuilder<T> minSizeExpGetter(
		PropertySelector propertySelector,
		int minSize
	) {
		return this.minSize(propertySelector, minSize);
	}

	default <R> JavaTypeArbitraryBuilder<T> maxSizeExpGetter(
		JavaGetterMethodReference<T, R> methodReference,
		int maxSize
	) {
		return this.maxSize(javaGetter(methodReference), maxSize);
	}

	default <R> JavaTypeArbitraryBuilder<T> maxSizeExpGetter(
		PropertySelector propertySelector,
		int maxSize
	) {
		return this.maxSize(propertySelector, maxSize);
	}

	default <R> JavaTypeArbitraryBuilder<T> setLazyExpGetter(
		JavaGetterMethodReference<T, R> methodReference,
		Supplier<?> supplier,
		int limit
	) {
		return this.setLazy(javaGetter(methodReference), supplier, limit);
	}

	default <R> JavaTypeArbitraryBuilder<T> setLazyExpGetter(
		JavaGetterMethodReference<T, R> methodReference,
		Supplier<?> supplier
	) {
		return this.setLazy(javaGetter(methodReference), supplier);
	}

	default <R> JavaTypeArbitraryBuilder<T> setLazyExpGetter(
		PropertySelector propertySelector,
		Supplier<?> supplier,
		int limit
	) {
		return this.setLazy(propertySelector, supplier, limit);
	}

	default <R> JavaTypeArbitraryBuilder<T> setLazyExpGetter(
		PropertySelector propertySelector,
		Supplier<?> supplier
	) {
		return this.setLazy(propertySelector, supplier);
	}
}
