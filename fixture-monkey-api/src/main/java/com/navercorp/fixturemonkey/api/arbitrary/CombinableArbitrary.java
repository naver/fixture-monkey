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

package com.navercorp.fixturemonkey.api.arbitrary;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.exception.FilterMissException;
import com.navercorp.fixturemonkey.api.jqwik.ArbitraryUtils;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary.LazyThreadSafetyMode;

/**
 * An arbitrary instance for combining arbitraries in order to generate an instance of specific class.
 *
 * @param <T> type to generate
 */
@API(since = "0.6.0", status = Status.EXPERIMENTAL)
public interface CombinableArbitrary<T> {
	CombinableArbitrary<?> NOT_GENERATED = CombinableArbitrary.from((Object)null);
	int DEFAULT_MAX_TRIES = 1_000;

	/**
	 * Generates a {@link FixedCombinableArbitrary} which returns always same value.
	 *
	 * @param object to be converted into {@link FixedCombinableArbitrary}.
	 * @return a {@link FixedCombinableArbitrary}
	 */
	static CombinableArbitrary<?> from(Object object) {
		return new FixedCombinableArbitrary<>(object);
	}

	/**
	 * @param arbitrary to be converted into {@link LazyCombinableArbitrary}.
	 * @return a {@link FixedCombinableArbitrary}
	 * @see #from(LazyArbitrary)
	 */
	static <U> CombinableArbitrary<U> from(Arbitrary<U> arbitrary) {
		return ArbitraryUtils.toCombinableArbitrary(arbitrary);
	}

	/**
	 * @param supplier to be converted into {@link LazyCombinableArbitrary}.
	 * @return a {@link LazyCombinableArbitrary}
	 * @see #from(LazyArbitrary)
	 */
	static <U> CombinableArbitrary<U> from(Supplier<U> supplier) {
		return from(LazyArbitrary.lazy(supplier, LazyThreadSafetyMode.SYNCHRONIZED));
	}

	/**
	 * Generates a {@link LazyCombinableArbitrary} which returns an arbitrary object.
	 *
	 * @param lazyArbitrary to be converted into {@link LazyCombinableArbitrary}.
	 * @return a {@link LazyCombinableArbitrary}
	 */
	static <U> CombinableArbitrary<U> from(LazyArbitrary<U> lazyArbitrary) {
		return new LazyCombinableArbitrary<>(lazyArbitrary);
	}

	/**
	 * Generates a builder for generating {@link ObjectCombinableArbitrary}.
	 *
	 * @return an {@link ObjectCombinableArbitrary} builder
	 */
	static ObjectCombineArbitraryBuilder objectBuilder() {
		return new ObjectCombineArbitraryBuilder();
	}

	/**
	 * Generates a builder for generating {@link ContainerCombinableArbitrary}.
	 *
	 * @return a {@link ContainerCombinableArbitrary} builder
	 */
	static ContainerCombineArbitraryBuilder containerBuilder() {
		return new ContainerCombineArbitraryBuilder();
	}

	/**
	 * Retrieves a combined object.
	 *
	 * @return a combined object
	 */
	T combined();

	/**
	 * Retrieves a raw object.
	 * For example, a map whose keys are property names and values are property values.
	 * Caller determines how the map is converted to an instance of class.
	 *
	 * @return an raw object
	 */
	Object rawValue();

	/**
	 * Applies a given {@code predicate} as a constraint.
	 * It would repeat generation {@link #DEFAULT_MAX_TRIES} times to satisfy the constraint.
	 * It would throw {@link FilterMissException} If repeated over {@link #DEFAULT_MAX_TRIES} times.
	 *
	 * @param predicate a constraint to satisfy
	 * @return A filtered {@link CombinableArbitrary}.
	 */
	default CombinableArbitrary<T> filter(Predicate<T> predicate) {
		return filter(DEFAULT_MAX_TRIES, predicate);
	}

	/**
	 * Applies a given {@code predicate} as a constraint.
	 * It would repeat generation {@code tries} times to satisfy the constraint.
	 * It would throw {@link FilterMissException} If repeated over {@code tries} times.
	 *
	 * @param predicate a constraint to satisfy
	 * @return A filtered {@link CombinableArbitrary}.
	 */
	default CombinableArbitrary<T> filter(int tries, Predicate<T> predicate) {
		return new FilteredCombinableArbitrary<>(
			tries,
			this,
			predicate
		);
	}

	/**
	 * Transforms a generated object into a new object.
	 *
	 * @param mapper a way of transforming
	 * @return A mapped {@link CombinableArbitrary}
	 */
	default <R> CombinableArbitrary<R> map(Function<T, R> mapper) {
		return new MappedCombinableArbitrary<>(
			this,
			mapper
		);
	}

	/**
	 * Makes it return {@code null} with a {@code nullProbability}% chance.
	 *
	 * @param nullProbability a probability to be {@code null}
	 * @return A {@link CombinableArbitrary} may return {@code null}
	 */
	default CombinableArbitrary<T> injectNull(double nullProbability) {
		return new NullInjectCombinableArbitrary<>(
			this,
			nullProbability
		);
	}

	/**
	 * Makes it return a unique value.
	 * The class of object generated by {@link CombinableArbitrary} should override {@code equals} and {@code hashCode}.
	 *
	 * @return A {@link CombinableArbitrary} returns a unique value
	 */
	default CombinableArbitrary<T> unique() {
		return new UniqueCombinableArbitrary<>(this, new ConcurrentHashMap<>());
	}

	/**
	 * Forces it to generate a new populated object.
	 */
	void clear();

	/**
	 * Checks if it is a fixed object.
	 * If true, {@link #clear()}} would make no change.
	 *
	 * @return fixed
	 */
	boolean fixed();
}
