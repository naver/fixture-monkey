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

import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "1.1.16", status = Status.EXPERIMENTAL)
public interface FloatCombinableArbitrary extends CombinableArbitrary<Float> {
	@Override
	Float combined();

	@Override
	Float rawValue();

	/**
	 * Generates a FloatCombinableArbitrary which produces floats within the specified range.
	 *
	 * @param min the minimum value (inclusive)
	 * @param max the maximum value (inclusive)
	 * @return the FloatCombinableArbitrary producing floats between {@code min} and {@code max}
	 */
	FloatCombinableArbitrary withRange(float min, float max);

	/**
	 * Generates a FloatCombinableArbitrary which produces only positive floats.
	 *
	 * @return the FloatCombinableArbitrary producing positive floats
	 */
	FloatCombinableArbitrary positive();

	/**
	 * Generates a FloatCombinableArbitrary which produces only negative floats.
	 *
	 * @return the FloatCombinableArbitrary producing negative floats
	 */
	FloatCombinableArbitrary negative();

	/**
	 * Generates a FloatCombinableArbitrary which produces only non-zero floats.
	 * Useful for avoiding division by zero errors in mathematical operations.
	 *
	 * @return the FloatCombinableArbitrary producing non-zero floats
	 */
	FloatCombinableArbitrary nonZero();

	/**
	 * Generates a FloatCombinableArbitrary with specified precision (decimal places).
	 *
	 * @param scale the number of decimal places
	 * @return the FloatCombinableArbitrary producing floats with specified precision
	 */
	FloatCombinableArbitrary withPrecision(int scale);

	/**
	 * Generates a FloatCombinableArbitrary which produces only finite floats.
	 * Excludes NaN and Infinity values.
	 *
	 * @return the FloatCombinableArbitrary producing finite floats
	 */
	FloatCombinableArbitrary finite();

	/**
	 * Generates a FloatCombinableArbitrary which produces infinite floats.
	 *
	 * @return the FloatCombinableArbitrary producing infinite floats
	 */
	FloatCombinableArbitrary infinite();

	/**
	 * Generates a FloatCombinableArbitrary which produces normalized floats (0.0 to 1.0).
	 *
	 * @return the FloatCombinableArbitrary producing normalized floats
	 */
	FloatCombinableArbitrary normalized();

	/**
	 * Generates a FloatCombinableArbitrary which produces NaN values.
	 *
	 * @return the FloatCombinableArbitrary producing NaN floats
	 */
	FloatCombinableArbitrary nan();

	/**
	 * Generates a FloatCombinableArbitrary which produces values 0-100 for percentage representation.
	 *
	 * @return the FloatCombinableArbitrary producing percentage floats
	 */
	FloatCombinableArbitrary percentage();

	/**
	 * Generates a FloatCombinableArbitrary which produces values 0-100 for scoring systems.
	 *
	 * @return the FloatCombinableArbitrary producing score floats
	 */
	FloatCombinableArbitrary score();

	/**
	 * Injects a special value into generated values and edge cases.
	 * This value can be outside the constraints of the arbitrary.
	 *
	 * @param special the special value to inject
	 * @return the FloatCombinableArbitrary with injected special value
	 */
	FloatCombinableArbitrary withSpecialValue(float special);

	/**
	 * Injects a selection of standard special values:
	 * Float.NaN, Float.MIN_VALUE, Float.MIN_NORMAL, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY
	 *
	 * @return the FloatCombinableArbitrary with standard special values
	 */
	FloatCombinableArbitrary withStandardSpecialValues();

	@Override
	default FloatCombinableArbitrary filter(Predicate<Float> predicate) {
		return this.filter(DEFAULT_MAX_TRIES, predicate);
	}

	@Override
	default FloatCombinableArbitrary filter(int tries, Predicate<Float> predicate) {
		return new FloatCombinableArbitraryDelegator(CombinableArbitrary.super.filter(tries, predicate));
	}

	@Override
	default FloatCombinableArbitrary injectNull(double nullProbability) {
		return new FloatCombinableArbitraryDelegator(CombinableArbitrary.super.injectNull(nullProbability));
	}

	@Override
	default FloatCombinableArbitrary unique() {
		return new FloatCombinableArbitraryDelegator(CombinableArbitrary.super.unique());
	}

	@Override
	void clear();

	@Override
	boolean fixed();
}
