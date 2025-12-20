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

import java.math.BigDecimal;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "1.1.16", status = Status.EXPERIMENTAL)
public interface BigDecimalCombinableArbitrary extends CombinableArbitrary<BigDecimal> {
	@Override
	BigDecimal combined();

	@Override
	BigDecimal rawValue();

	/**
	 * Generates a BigDecimalCombinableArbitrary which produces BigDecimals within the specified range.
	 *
	 * @param min the minimum value (inclusive)
	 * @param max the maximum value (inclusive)
	 * @return the BigDecimalCombinableArbitrary producing BigDecimals between {@code min} and {@code max}
	 */
	BigDecimalCombinableArbitrary withRange(BigDecimal min, BigDecimal max);

	/**
	 * Generates a BigDecimalCombinableArbitrary which produces only positive BigDecimals.
	 *
	 * @return the BigDecimalCombinableArbitrary producing positive BigDecimals
	 */
	BigDecimalCombinableArbitrary positive();

	/**
	 * Generates a BigDecimalCombinableArbitrary which produces only negative BigDecimals.
	 *
	 * @return the BigDecimalCombinableArbitrary producing negative BigDecimals
	 */
	BigDecimalCombinableArbitrary negative();

	/**
	 * Generates a BigDecimalCombinableArbitrary which produces only non-zero BigDecimals.
	 *
	 * @return the BigDecimalCombinableArbitrary producing non-zero BigDecimals
	 */
	BigDecimalCombinableArbitrary nonZero();

	/**
	 * Generates a BigDecimalCombinableArbitrary which produces BigDecimals as percentage values (0.0-100.0).
	 *
	 * @return the BigDecimalCombinableArbitrary producing percentage BigDecimals
	 */
	BigDecimalCombinableArbitrary percentage();

	/**
	 * Generates a BigDecimalCombinableArbitrary which produces BigDecimals as score values (0-100).
	 *
	 * @return the BigDecimalCombinableArbitrary producing score BigDecimals
	 */
	BigDecimalCombinableArbitrary score();

	/**
	 * Generates a BigDecimalCombinableArbitrary which produces BigDecimals as score values within the specified range.
	 *
	 * @param min the minimum score (inclusive)
	 * @param max the maximum score (inclusive)
	 * @return the BigDecimalCombinableArbitrary producing score BigDecimals
	 */
	BigDecimalCombinableArbitrary score(BigDecimal min, BigDecimal max);

	/**
	 * Generates a BigDecimalCombinableArbitrary which produces BigDecimals with the specified precision.
	 *
	 * @param precision the number of significant digits
	 * @return the BigDecimalCombinableArbitrary producing BigDecimals with {@code precision} significant digits
	 */
	BigDecimalCombinableArbitrary withPrecision(int precision);

	/**
	 * Generates a BigDecimalCombinableArbitrary which produces BigDecimals with the specified scale.
	 *
	 * @param scale the number of digits to the right of the decimal point
	 * @return the BigDecimalCombinableArbitrary producing BigDecimals with {@code scale} decimal places
	 */
	BigDecimalCombinableArbitrary withScale(int scale);

	/**
	 * Generates a BigDecimalCombinableArbitrary which produces normalized BigDecimals (0.0 to 1.0).
	 *
	 * @return the BigDecimalCombinableArbitrary producing normalized BigDecimals
	 */
	BigDecimalCombinableArbitrary normalized();

	/**
	 * Generates a BigDecimalCombinableArbitrary which produces BigDecimals with trailing zeros removed.
	 *
	 * @return the BigDecimalCombinableArbitrary producing BigDecimals without trailing zeros
	 */
	BigDecimalCombinableArbitrary stripTrailingZeros();

	@Override
	default BigDecimalCombinableArbitrary filter(Predicate<BigDecimal> predicate) {
		return this.filter(DEFAULT_MAX_TRIES, predicate);
	}

	@Override
	default BigDecimalCombinableArbitrary filter(int tries, Predicate<BigDecimal> predicate) {
		return new BigDecimalCombinableArbitraryDelegator(CombinableArbitrary.super.filter(tries, predicate));
	}

	@Override
	default BigDecimalCombinableArbitrary injectNull(double nullProbability) {
		return new BigDecimalCombinableArbitraryDelegator(CombinableArbitrary.super.injectNull(nullProbability));
	}

	@Override
	default BigDecimalCombinableArbitrary unique() {
		return new BigDecimalCombinableArbitraryDelegator(CombinableArbitrary.super.unique());
	}

	@Override
	void clear();

	@Override
	boolean fixed();
}
