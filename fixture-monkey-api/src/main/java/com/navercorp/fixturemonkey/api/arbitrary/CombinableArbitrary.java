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

import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.exception.RetryableFilterMissException;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;

/**
 * An arbitrary instance for combining arbitraries in order to generate an instance of specific class.
 *
 * @param <T> type to generate
 */
@API(since = "0.6.0", status = Status.MAINTAINED)
public interface CombinableArbitrary<T> {
	CombinableArbitrary<?> NOT_GENERATED = CombinableArbitrary.from((Object)null);
	int DEFAULT_MAX_TRIES = 1_000;

	ServiceLoader<IntegerCombinableArbitrary> INTEGER_COMBINABLE_ARBITRARY_SERVICE_LOADER =
		ServiceLoader.load(IntegerCombinableArbitrary.class);
	ServiceLoader<StringCombinableArbitrary> STRING_COMBINABLE_ARBITRARY_SERVICE_LOADER =
		ServiceLoader.load(StringCombinableArbitrary.class);
	ServiceLoader<ByteCombinableArbitrary> BYTE_COMBINABLE_ARBITRARY_SERVICE_LOADER =
		ServiceLoader.load(ByteCombinableArbitrary.class);
	ServiceLoader<LongCombinableArbitrary> LONG_COMBINABLE_ARBITRARY_SERVICE_LOADER =
		ServiceLoader.load(LongCombinableArbitrary.class);
	ServiceLoader<ShortCombinableArbitrary> SHORT_COMBINABLE_ARBITRARY_SERVICE_LOADER =
		ServiceLoader.load(ShortCombinableArbitrary.class);
	ServiceLoader<CharacterCombinableArbitrary> CHARACTER_COMBINABLE_ARBITRARY_SERVICE_LOADER =
		ServiceLoader.load(CharacterCombinableArbitrary.class);
	ServiceLoader<FloatCombinableArbitrary> FLOAT_COMBINABLE_ARBITRARY_SERVICE_LOADER =
		ServiceLoader.load(FloatCombinableArbitrary.class);
	ServiceLoader<DoubleCombinableArbitrary> DOUBLE_COMBINABLE_ARBITRARY_SERVICE_LOADER =
		ServiceLoader.load(DoubleCombinableArbitrary.class);
	ServiceLoader<BigIntegerCombinableArbitrary> BIG_INTEGER_COMBINABLE_ARBITRARY_SERVICE_LOADER =
		ServiceLoader.load(BigIntegerCombinableArbitrary.class);
	ServiceLoader<BigDecimalCombinableArbitrary> BIG_DECIMAL_COMBINABLE_ARBITRARY_SERVICE_LOADER =
		ServiceLoader.load(BigDecimalCombinableArbitrary.class);

	/**
	 * Generates a {@link FixedCombinableArbitrary} which returns always same value.
	 *
	 * @param object to be converted into {@link FixedCombinableArbitrary}.
	 * @param <T>    type to converted as {@link CombinableArbitrary}
	 * @return a {@link FixedCombinableArbitrary}
	 */
	static <T> CombinableArbitrary<T> from(T object) {
		return new FixedCombinableArbitrary<>(object);
	}

	/**
	 * @param supplier to be converted into {@link LazyCombinableArbitrary}.
	 * @return a {@link LazyCombinableArbitrary}
	 * @see #from(LazyArbitrary)
	 */
	static <U> CombinableArbitrary<U> from(Supplier<U> supplier) {
		return from(LazyArbitrary.lazy(supplier));
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
	 * It would throw {@link RetryableFilterMissException} If repeated over {@link #DEFAULT_MAX_TRIES} times.
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
	 * It would throw {@link RetryableFilterMissException} If repeated over {@code tries} times.
	 *
	 * @param predicate a constraint to satisfy
	 * @return A filtered {@link CombinableArbitrary}.
	 */
	default CombinableArbitrary<T> filter(int tries, Predicate<T> predicate) {
		if (this instanceof FilteredCombinableArbitrary) {
			return new FilteredCombinableArbitrary<>(
				tries,
				(FilteredCombinableArbitrary<T>)this,
				predicate
			);
		}
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

	/**
	 * Generates a {@link IntegerCombinableArbitrary} which returns a randomly generated Integer.
	 * You can customize the generated Integer by using {@link IntegerCombinableArbitrary}.
	 *
	 * @return a {@link CombinableArbitrary} returns a randomly generated Integer
	 */
	@API(since = "1.1.12", status = Status.EXPERIMENTAL)
	static IntegerCombinableArbitrary integers() {
		return INTEGER_COMBINABLE_ARBITRARY_SERVICE_LOADER.iterator().next();
	}

	/**
	 * Generates a {@link ByteCombinableArbitrary} which returns a randomly generated Byte.
	 * You can customize the generated Byte by using {@link ByteCombinableArbitrary}.
	 *
	 * @return a {@link CombinableArbitrary} returns a randomly generated Byte
	 */
	@API(since = "1.1.15", status = Status.EXPERIMENTAL)
	static ByteCombinableArbitrary bytes() {
		return BYTE_COMBINABLE_ARBITRARY_SERVICE_LOADER.iterator().next();
	}

	/**
	 * Generates a {@link StringCombinableArbitrary} which returns a randomly generated String.
	 * You can customize the generated String by using {@link StringCombinableArbitrary}.
	 *
	 * @return a {@link CombinableArbitrary} returns a randomly generated String
	 */
	@API(since = "1.1.12", status = Status.EXPERIMENTAL)
	static StringCombinableArbitrary strings() {
		return STRING_COMBINABLE_ARBITRARY_SERVICE_LOADER.iterator().next();
	}

	/**
	 * Generates a {@link LongCombinableArbitrary} which returns a randomly generated Long.
	 * You can customize the generated Long by using {@link LongCombinableArbitrary}.
	 *
	 * @return a {@link CombinableArbitrary} returns a randomly generated Long
	 */
	@API(since = "1.1.16", status = Status.EXPERIMENTAL)
	static LongCombinableArbitrary longs() {
		return LONG_COMBINABLE_ARBITRARY_SERVICE_LOADER.iterator().next();
	}

	/**
	 * Generates a {@link ShortCombinableArbitrary} which returns a randomly generated Short.
	 * You can customize the generated Short by using {@link ShortCombinableArbitrary}.
	 *
	 * @return a {@link CombinableArbitrary} returns a randomly generated Short
	 */
	@API(since = "1.1.16", status = Status.EXPERIMENTAL)
	static ShortCombinableArbitrary shorts() {
		return SHORT_COMBINABLE_ARBITRARY_SERVICE_LOADER.iterator().next();
	}

	/**
	 * Generates a {@link CharacterCombinableArbitrary} which returns a randomly generated Character.
	 * You can customize the generated Character by using {@link CharacterCombinableArbitrary}.
	 *
	 * @return a {@link CombinableArbitrary} returns a randomly generated Character
	 */
	@API(since = "1.1.16", status = Status.EXPERIMENTAL)
	static CharacterCombinableArbitrary chars() {
		return CHARACTER_COMBINABLE_ARBITRARY_SERVICE_LOADER.iterator().next();
	}

	/**
	 * Generates a {@link FloatCombinableArbitrary} which returns a randomly generated Float.
	 * You can customize the generated Float by using {@link FloatCombinableArbitrary}.
	 *
	 * @return a {@link CombinableArbitrary} returns a randomly generated Float
	 */
	@API(since = "1.1.16", status = Status.EXPERIMENTAL)
	static FloatCombinableArbitrary floats() {
		return FLOAT_COMBINABLE_ARBITRARY_SERVICE_LOADER.iterator().next();
	}

	/**
	 * Generates a {@link DoubleCombinableArbitrary} which returns a randomly generated Double.
	 * You can customize the generated Double by using {@link DoubleCombinableArbitrary}.
	 *
	 * @return a {@link CombinableArbitrary} returns a randomly generated Double
	 */
	@API(since = "1.1.16", status = Status.EXPERIMENTAL)
	static DoubleCombinableArbitrary doubles() {
		return DOUBLE_COMBINABLE_ARBITRARY_SERVICE_LOADER.iterator().next();
	}

	/**
	 * Generates a {@link BigIntegerCombinableArbitrary} which returns a randomly generated BigInteger.
	 * You can customize the generated BigInteger by using {@link BigIntegerCombinableArbitrary}.
	 *
	 * @return a {@link CombinableArbitrary} returns a randomly generated BigInteger
	 */
	@API(since = "1.1.16", status = Status.EXPERIMENTAL)
	static BigIntegerCombinableArbitrary bigIntegers() {
		return BIG_INTEGER_COMBINABLE_ARBITRARY_SERVICE_LOADER.iterator().next();
	}

	/**
	 * Generates a {@link BigDecimalCombinableArbitrary} which returns a randomly generated BigDecimal.
	 * You can customize the generated BigDecimal by using {@link BigDecimalCombinableArbitrary}.
	 *
	 * @return a {@link CombinableArbitrary} returns a randomly generated BigDecimal
	 */
	@API(since = "1.1.16", status = Status.EXPERIMENTAL)
	static BigDecimalCombinableArbitrary bigDecimals() {
		return BIG_DECIMAL_COMBINABLE_ARBITRARY_SERVICE_LOADER.iterator().next();
	}

}
