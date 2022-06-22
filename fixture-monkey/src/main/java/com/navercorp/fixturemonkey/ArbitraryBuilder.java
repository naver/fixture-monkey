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

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators.F3;
import net.jqwik.api.Combinators.F4;

import com.navercorp.fixturemonkey.api.expression.ExpressionGenerator;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizer;
import com.navercorp.fixturemonkey.customizer.ExpressionSpec;
import com.navercorp.fixturemonkey.customizer.InnerSpec;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public interface ArbitraryBuilder<T> {
	ArbitraryBuilder<T> spec(ExpressionSpec expressionSpec);

	ArbitraryBuilder<T> specAny(ExpressionSpec... specs);

	ArbitraryBuilder<T> set(String expression, @Nullable Object value);

	ArbitraryBuilder<T> set(String expression, @Nullable Object value, int limit);

	ArbitraryBuilder<T> set(ExpressionGenerator expressionGenerator, @Nullable Object value);

	ArbitraryBuilder<T> set(ExpressionGenerator expressionGenerator, @Nullable Object value, int limit);

	ArbitraryBuilder<T> set(@Nullable Object value);

	ArbitraryBuilder<T> setInner(String expression, Consumer<InnerSpec> specSupplier);

	ArbitraryBuilder<T> setLazy(String expression, Supplier<?> supplier);

	ArbitraryBuilder<T> setNull(String expression);

	ArbitraryBuilder<T> setNull(ExpressionGenerator expressionGenerator);

	ArbitraryBuilder<T> setNotNull(String expression);

	ArbitraryBuilder<T> setNotNull(ExpressionGenerator expressionGenerator);

	<U> ArbitraryBuilder<T> setPostCondition(String expression, Class<U> clazz, Predicate<U> filter);

	<U> ArbitraryBuilder<T> setPostCondition(
		String expression,
		Class<U> clazz,
		Predicate<U> filter,
		int limit
	);

	<U> ArbitraryBuilder<T> setPostCondition(
		ExpressionGenerator expressionGenerator,
		Class<U> clazz,
		Predicate<U> filter,
		int limit
	);

	<U> ArbitraryBuilder<T> setPostCondition(
		ExpressionGenerator expressionGenerator,
		Class<U> clazz,
		Predicate<U> filter
	);

	ArbitraryBuilder<T> setPostCondition(Predicate<T> filter);

	ArbitraryBuilder<T> size(String expression, int size);

	ArbitraryBuilder<T> size(ExpressionGenerator expressionGenerator, int size);

	ArbitraryBuilder<T> size(String expression, int min, int max);

	ArbitraryBuilder<T> size(ExpressionGenerator expression, int min, int max);

	ArbitraryBuilder<T> minSize(String expression, int min);

	ArbitraryBuilder<T> minSize(ExpressionGenerator expressionGenerator, int min);

	ArbitraryBuilder<T> maxSize(String expression, int max);

	ArbitraryBuilder<T> maxSize(ExpressionGenerator expressionGenerator, int max);

	ArbitraryBuilder<T> apply(BiConsumer<T, ArbitraryBuilder<T>> biConsumer);

	ArbitraryBuilder<T> acceptIf(Predicate<T> predicate, Consumer<ArbitraryBuilder<T>> consumer);

	ArbitraryBuilder<T> fixed();

	<U> ArbitraryBuilder<U> map(Function<T, U> mapper);

	<U, R> ArbitraryBuilder<R> zipWith(ArbitraryBuilder<U> other, BiFunction<T, U, R> combinator);

	<U, V, R> ArbitraryBuilder<R> zipWith(
		ArbitraryBuilder<U> other,
		ArbitraryBuilder<V> another,
		F3<T, U, V, R> combinator
	);

	<U, V, W, R> ArbitraryBuilder<R> zipWith(
		ArbitraryBuilder<U> other,
		ArbitraryBuilder<V> another,
		ArbitraryBuilder<W> theOther,
		F4<T, U, V, W, R> combinator
	);

	<R> ArbitraryBuilder<R> zipWith(
		List<ArbitraryBuilder<?>> other,
		Function<List<?>, R> combinator
	);

	ArbitraryBuilder<T> customize(Class<T> type, ArbitraryCustomizer<T> customizer);

	Arbitrary<T> build();

	T sample();

	List<T> sampleList(int size);

	Stream<T> sampleStream();

	ArbitraryBuilder<T> copy();

	ArbitraryBuilder<T> validOnly(boolean validOnly);
}
