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
public interface DoubleCombinableArbitrary extends CombinableArbitrary<Double> {
	@Override
	Double combined();

	@Override
	Double rawValue();

	/**
	 * Generates a DoubleCombinableArbitrary which produces doubles within the specified range.
	 *
	 * @param min the minimum value (inclusive)
	 * @param max the maximum value (inclusive)
	 * @return the DoubleCombinableArbitrary producing doubles between {@code min} and {@code max}
	 */
	DoubleCombinableArbitrary withRange(double min, double max);

	/**
	 * Generates a DoubleCombinableArbitrary which produces only positive doubles.
	 *
	 * @return the DoubleCombinableArbitrary producing positive doubles
	 */
	DoubleCombinableArbitrary positive();

	/**
	 * Generates a DoubleCombinableArbitrary which produces only negative doubles.
	 *
	 * @return the DoubleCombinableArbitrary producing negative doubles
	 */
	DoubleCombinableArbitrary negative();

	/**
	 * Generates a DoubleCombinableArbitrary which produces only non-zero doubles.
	 * Useful for avoiding division by zero errors in mathematical operations.
	 *
	 * @return the DoubleCombinableArbitrary producing non-zero doubles
	 */
	DoubleCombinableArbitrary nonZero();

	/**
	 * Generates a DoubleCombinableArbitrary with specified precision (decimal places).
	 *
	 * @param scale the number of decimal places
	 * @return the DoubleCombinableArbitrary producing doubles with specified precision
	 */
	DoubleCombinableArbitrary withPrecision(int scale);

	/**
	 * Generates a DoubleCombinableArbitrary which produces only finite doubles.
	 * Excludes NaN and Infinity values.
	 *
	 * @return the DoubleCombinableArbitrary producing finite doubles
	 */
	DoubleCombinableArbitrary finite();

	/**
	 * Generates a DoubleCombinableArbitrary which produces infinite doubles.
	 *
	 * @return the DoubleCombinableArbitrary producing infinite doubles
	 */
	DoubleCombinableArbitrary infinite();

	/**
	 * Generates a DoubleCombinableArbitrary which produces normalized doubles (0.0 to 1.0).
	 *
	 * @return the DoubleCombinableArbitrary producing normalized doubles
	 */
	DoubleCombinableArbitrary normalized();

	/**
	 * Generates a DoubleCombinableArbitrary which produces NaN values.
	 *
	 * @return the DoubleCombinableArbitrary producing NaN doubles
	 */
	DoubleCombinableArbitrary nan();

	/**
	 * Generates a DoubleCombinableArbitrary which produces values 0-100 for percentage representation.
	 *
	 * @return the DoubleCombinableArbitrary producing percentage doubles
	 */
	DoubleCombinableArbitrary percentage();

	/**
	 * Generates a DoubleCombinableArbitrary which produces values 0-100 for scoring systems.
	 *
	 * @return the DoubleCombinableArbitrary producing score doubles
	 */
	DoubleCombinableArbitrary score();

	/**
	 * Injects a special value into generated values and edge cases.
	 * This value can be outside the constraints of the arbitrary.
	 *
	 * @param special the special value to inject
	 * @return the DoubleCombinableArbitrary with injected special value
	 */
	DoubleCombinableArbitrary withSpecialValue(double special);

	/**
	 * Injects a selection of standard special values:
	 * Double.NaN, Double.MIN_VALUE, Double.MIN_NORMAL, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY
	 *
	 * @return the DoubleCombinableArbitrary with standard special values
	 */
	DoubleCombinableArbitrary withStandardSpecialValues();

	@Override
	default DoubleCombinableArbitrary filter(Predicate<Double> predicate) {
		return this.filter(DEFAULT_MAX_TRIES, predicate);
	}

	@Override
	default DoubleCombinableArbitrary filter(int tries, Predicate<Double> predicate) {
		return new DoubleCombinableArbitraryDelegator(CombinableArbitrary.super.filter(tries, predicate));
	}

	@Override
	default DoubleCombinableArbitrary injectNull(double nullProbability) {
		return new DoubleCombinableArbitraryDelegator(CombinableArbitrary.super.injectNull(nullProbability));
	}

	@Override
	default DoubleCombinableArbitrary unique() {
		return new DoubleCombinableArbitraryDelegator(CombinableArbitrary.super.unique());
	}

	@Override
	void clear();

	@Override
	boolean fixed();
}
