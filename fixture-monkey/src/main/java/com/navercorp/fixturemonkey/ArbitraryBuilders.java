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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.jqwik.api.Combinators.F3;
import net.jqwik.api.Combinators.F4;

public final class ArbitraryBuilders {
	public static <T, U, R> ArbitraryBuilder<R> zip(
		ArbitraryBuilder<T> a1,
		ArbitraryBuilder<U> a2,
		BiFunction<T, U, R> combinator
	) {
		return a1.zipWith(a2, combinator);
	}

	public static <T, U, V, R> ArbitraryBuilder<R> zip(
		ArbitraryBuilder<T> a1,
		ArbitraryBuilder<U> a2,
		ArbitraryBuilder<V> a3,
		F3<T, U, V, R> combinator
	) {
		return a1.zipWith(a2, a3, combinator);
	}

	public static <T, U, V, W, R> ArbitraryBuilder<R> zip(
		ArbitraryBuilder<T> a1,
		ArbitraryBuilder<U> a2,
		ArbitraryBuilder<V> a3,
		ArbitraryBuilder<W> a4,
		F4<T, U, V, W, R> combinator
	) {
		return a1.zipWith(a2, a3, a4, combinator);
	}

	public static <R> ArbitraryBuilder<R> zip(
		List<ArbitraryBuilder<?>> list,
		Function<List<?>, R> combinator
	) {
		if (list.size() < 2) {
			throw new IllegalArgumentException(
				"zip should be used in more than two ArbitraryBuilders, given size : " + list.size()
			);
		}
		ArbitraryBuilder<?> first = list.get(0);
		List<ArbitraryBuilder<?>> arbitraryBuilders = new ArrayList<>(list.subList(1, list.size()));
		return first.zipWith(arbitraryBuilders, combinator);
	}
}
