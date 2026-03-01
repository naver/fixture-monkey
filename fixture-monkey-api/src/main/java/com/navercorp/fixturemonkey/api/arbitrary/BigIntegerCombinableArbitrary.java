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

import java.math.BigInteger;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "1.1.16", status = Status.EXPERIMENTAL)
public interface BigIntegerCombinableArbitrary extends CombinableArbitrary<BigInteger> {
	@Override
	BigInteger combined();

	@Override
	BigInteger rawValue();

	/**
	 * Generates a BigIntegerCombinableArbitrary which produces BigIntegers within the specified range.
	 *
	 * @param min the minimum value (inclusive)
	 * @param max the maximum value (inclusive)
	 * @return the BigIntegerCombinableArbitrary producing BigIntegers between {@code min} and {@code max}
	 */
	BigIntegerCombinableArbitrary withRange(BigInteger min, BigInteger max);

	/**
	 * Generates a BigIntegerCombinableArbitrary which produces only positive BigIntegers.
	 *
	 * @return the BigIntegerCombinableArbitrary producing positive BigIntegers
	 */
	BigIntegerCombinableArbitrary positive();

	/**
	 * Generates a BigIntegerCombinableArbitrary which produces only negative BigIntegers.
	 *
	 * @return the BigIntegerCombinableArbitrary producing negative BigIntegers
	 */
	BigIntegerCombinableArbitrary negative();

	/**
	 * Generates a BigIntegerCombinableArbitrary which produces only non-zero BigIntegers.
	 *
	 * @return the BigIntegerCombinableArbitrary producing non-zero BigIntegers
	 */
	BigIntegerCombinableArbitrary nonZero();

	/**
	 * Generates a BigIntegerCombinableArbitrary which produces BigIntegers as percentage values (0-100).
	 *
	 * @return the BigIntegerCombinableArbitrary producing percentage BigIntegers
	 */
	BigIntegerCombinableArbitrary percentage();

	/**
	 * Generates a BigIntegerCombinableArbitrary which produces BigIntegers as score values (0-100).
	 *
	 * @return the BigIntegerCombinableArbitrary producing score BigIntegers
	 */
	BigIntegerCombinableArbitrary score();

	/**
	 * Generates a BigIntegerCombinableArbitrary which produces BigIntegers as score values within the specified range.
	 *
	 * @param min the minimum score (inclusive)
	 * @param max the maximum score (inclusive)
	 * @return the BigIntegerCombinableArbitrary producing score BigIntegers
	 */
	BigIntegerCombinableArbitrary score(BigInteger min, BigInteger max);

	/**
	 * Generates a BigIntegerCombinableArbitrary which produces only even BigIntegers.
	 *
	 * @return the BigIntegerCombinableArbitrary producing even BigIntegers
	 */
	BigIntegerCombinableArbitrary even();

	/**
	 * Generates a BigIntegerCombinableArbitrary which produces only odd BigIntegers.
	 *
	 * @return the BigIntegerCombinableArbitrary producing odd BigIntegers
	 */
	BigIntegerCombinableArbitrary odd();

	/**
	 * Generates a BigIntegerCombinableArbitrary which produces BigIntegers that are multiples of the specified divisor.
	 *
	 * @param divisor the divisor for which generated BigIntegers should be multiples
	 * @return the BigIntegerCombinableArbitrary producing BigIntegers that are multiples of {@code divisor}
	 */
	BigIntegerCombinableArbitrary multipleOf(BigInteger divisor);

	/**
	 * Generates a BigIntegerCombinableArbitrary which produces only prime BigIntegers.
	 * <p><strong>Note:</strong> Prime generation may be computationally expensive for large values.
	 * Consider using with appropriate range constraints.
	 *
	 * @return the BigIntegerCombinableArbitrary producing prime BigIntegers
	 */
	BigIntegerCombinableArbitrary prime();

	@Override
	default BigIntegerCombinableArbitrary filter(Predicate<BigInteger> predicate) {
		return this.filter(DEFAULT_MAX_TRIES, predicate);
	}

	@Override
	default BigIntegerCombinableArbitrary filter(int tries, Predicate<BigInteger> predicate) {
		return new BigIntegerCombinableArbitraryDelegator(CombinableArbitrary.super.filter(tries, predicate));
	}

	@Override
	default BigIntegerCombinableArbitrary injectNull(double nullProbability) {
		return new BigIntegerCombinableArbitraryDelegator(CombinableArbitrary.super.injectNull(nullProbability));
	}

	@Override
	default BigIntegerCombinableArbitrary unique() {
		return new BigIntegerCombinableArbitraryDelegator(CombinableArbitrary.super.unique());
	}

	@Override
	void clear();

	@Override
	boolean fixed();
}
