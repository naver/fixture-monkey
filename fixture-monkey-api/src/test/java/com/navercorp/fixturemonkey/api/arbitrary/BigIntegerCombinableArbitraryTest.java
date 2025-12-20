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

import static org.assertj.core.api.BDDAssertions.then;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class BigIntegerCombinableArbitraryTest {
	@Test
	void combined() {
		// when
		BigInteger actual = CombinableArbitrary.bigIntegers().combined();

		// then
		then(actual).isInstanceOf(BigInteger.class);
	}

	@Test
	void withRange() {
		// given
		BigInteger min = BigInteger.valueOf(10);
		BigInteger max = BigInteger.valueOf(50);

		// when
		BigInteger actual = CombinableArbitrary.bigIntegers().withRange(min, max).combined();

		// then
		then(actual).isBetween(min, max);
	}

	@Test
	void positive() {
		// when
		BigInteger actual = CombinableArbitrary.bigIntegers().positive().combined();

		// then
		then(actual).isGreaterThan(BigInteger.ZERO);
	}

	@Test
	void negative() {
		// when
		BigInteger actual = CombinableArbitrary.bigIntegers().negative().combined();

		// then
		then(actual).isLessThan(BigInteger.ZERO);
	}

	@Test
	void nonZero() {
		// when
		BigInteger actual = CombinableArbitrary.bigIntegers().nonZero().combined();

		// then
		then(actual).isNotEqualTo(BigInteger.ZERO);
	}

	@Test
	void percentage() {
		// when
		BigInteger actual = CombinableArbitrary.bigIntegers().percentage().combined();

		// then
		then(actual).isBetween(BigInteger.ZERO, BigInteger.valueOf(100));
	}

	@Test
	void score() {
		// given
		BigInteger min = BigInteger.valueOf(80);
		BigInteger max = BigInteger.valueOf(100);

		// when
		BigInteger actual = CombinableArbitrary.bigIntegers().score(min, max).combined();

		// then
		then(actual).isBetween(min, max);
	}

	@Test
	void even() {
		// when
		BigInteger actual = CombinableArbitrary.bigIntegers().even().combined();

		// then
		then(actual.remainder(BigInteger.valueOf(2))).isEqualTo(BigInteger.ZERO);
	}

	@Test
	void odd() {
		// when
		BigInteger actual = CombinableArbitrary.bigIntegers().odd().combined();

		// then
		then(actual.remainder(BigInteger.valueOf(2))).isNotEqualTo(BigInteger.ZERO);
	}

	@Test
	void multipleOf() {
		// given
		BigInteger divisor = BigInteger.valueOf(5);

		// when
		BigInteger actual = CombinableArbitrary.bigIntegers().multipleOf(divisor).combined();

		// then
		then(actual.remainder(divisor)).isEqualTo(BigInteger.ZERO);
	}

	@Test
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	void prime() {
		// when
		BigInteger actual = CombinableArbitrary.bigIntegers()
			.withRange(BigInteger.valueOf(2), BigInteger.valueOf(100))
			.prime()
			.combined();

		// then
		then(isPrime(actual)).isTrue();
	}

	@Test
	void lastMethodWinsWithPositiveAndRange() {
		// given
		BigInteger min = BigInteger.valueOf(-50);
		BigInteger max = BigInteger.valueOf(-10);

		// when - positive().withRange() => withRange()
		BigInteger actual = CombinableArbitrary.bigIntegers().positive().withRange(min, max).combined();

		// then
		then(actual).isBetween(min, max);
	}

	@Test
	void bigIntegerMapping() {
		// when
		String actual = CombinableArbitrary.bigIntegers()
			.positive()
			.map(bigInt -> "bigint:" + bigInt)
			.combined();

		// then
		then(actual).startsWith("bigint:");
		String numberPart = actual.substring(7);
		BigInteger value = new BigInteger(numberPart);
		then(value).isGreaterThan(BigInteger.ZERO);
	}

	@Test
	void bigIntegerFiltering() {
		// when
		BigInteger actual = CombinableArbitrary.bigIntegers()
			.withRange(BigInteger.ZERO, BigInteger.valueOf(100))
			.filter(b -> b.compareTo(BigInteger.valueOf(50)) > 0)
			.combined();

		// then
		then(actual).isGreaterThan(BigInteger.valueOf(50));
		then(actual).isLessThanOrEqualTo(BigInteger.valueOf(100));
	}

	@Test
	void bigIntegerFilteringWithMultipleConditions() {
		// when
		BigInteger actual = CombinableArbitrary.bigIntegers()
			.positive()
			.filter(b -> b.remainder(BigInteger.valueOf(10)).equals(BigInteger.ZERO))  // multiples of 10
			.combined();

		// then
		then(actual).isGreaterThan(BigInteger.ZERO);
		then(actual.remainder(BigInteger.valueOf(10))).isEqualTo(BigInteger.ZERO);
	}

	@Test
	void bigIntegerInjectNull() {
		// when
		BigInteger actual = CombinableArbitrary.bigIntegers()
			.positive()
			.injectNull(1.0)  // 100% null
			.combined();

		// then
		then(actual).isNull();
	}

	@Test
	void bigIntegerInjectNullWithZeroProbability() {
		// when
		BigInteger actual = CombinableArbitrary.bigIntegers()
			.positive()
			.injectNull(0.0)  // 0% null
			.combined();

		// then
		then(actual).isNotNull();
		then(actual).isGreaterThan(BigInteger.ZERO);
	}

	@Test
	void bigIntegerCombinationWithMultipleOperations() {
		// when - combine multiple operations
		String actual = CombinableArbitrary.bigIntegers()
			.percentage()
			.filter(b -> b.compareTo(BigInteger.valueOf(50)) >= 0)  // >= 50
			.map(b -> "percentage:" + b)
			.combined();

		// then
		then(actual).startsWith("percentage:");
		String numberPart = actual.substring(11);
		BigInteger value = new BigInteger(numberPart);
		then(value).isBetween(BigInteger.valueOf(50), BigInteger.valueOf(100));
	}

	@Test
	void bigIntegerUniqueWithDifferentValues() {
		// when - unique() works with different values
		BigInteger actual = CombinableArbitrary.bigIntegers()
			.withRange(BigInteger.ZERO, BigInteger.valueOf(1000))  // wide range
			.unique()
			.combined();

		// then
		then(actual).isBetween(BigInteger.ZERO, BigInteger.valueOf(1000));
	}

	@Test
	void fixed() {
		// when
		boolean actual = CombinableArbitrary.bigIntegers().fixed();

		// then
		then(actual).isFalse();
	}

	@Test
	void lastMethodWinsRangeOverPositive() {
		// when - positive().withRange() => withRange()
		BigInteger actual = CombinableArbitrary.bigIntegers()
			.positive()
			.withRange(BigInteger.valueOf(-10), BigInteger.valueOf(-1))
			.combined();

		// then
		then(actual).isBetween(BigInteger.valueOf(-10), BigInteger.valueOf(-1));
	}

	@Test
	void lastMethodWinsOddOverEven() {
		// when - even().odd() => odd()
		BigInteger actual = CombinableArbitrary.bigIntegers().even().odd().combined();

		// then
		then(actual.remainder(BigInteger.valueOf(2))).isNotEqualTo(BigInteger.ZERO);
	}

	@Test
	void bigIntegerFilterWithMultipleOfFive() {
		// when - filter multiples of 5
		BigInteger actual = CombinableArbitrary.bigIntegers()
			.withRange(BigInteger.ZERO, BigInteger.valueOf(100))
			.filter(b -> b.remainder(BigInteger.valueOf(5)).equals(BigInteger.ZERO)).combined();

		// then
		then(actual.remainder(BigInteger.valueOf(5))).isEqualTo(BigInteger.ZERO);
		then(actual).isBetween(BigInteger.ZERO, BigInteger.valueOf(100));
	}

	@Test
	void nonZeroWithOdd() {
		// when - nonZero().odd() => odd()
		BigInteger actual = CombinableArbitrary.bigIntegers().nonZero().odd().combined();

		// then
		then(actual.remainder(BigInteger.valueOf(2))).isNotEqualTo(BigInteger.ZERO);
	}

	@Test
	void nonZeroWithEven() {
		// when - nonZero().even() => even()
		BigInteger actual = CombinableArbitrary.bigIntegers().nonZero().even().combined();

		// then
		then(actual.remainder(BigInteger.valueOf(2))).isEqualTo(BigInteger.ZERO);
	}

	@Test
	void nonZeroWithPositive() {
		// when
		BigInteger actual = CombinableArbitrary.bigIntegers().nonZero().positive().combined();

		// then
		then(actual).isGreaterThan(BigInteger.ZERO);
	}

	@Test
	void nonZeroWithNegative() {
		// when - nonZero().negative() => negative()
		BigInteger actual = CombinableArbitrary.bigIntegers().nonZero().negative().combined();

		// then
		then(actual).isLessThan(BigInteger.ZERO);
	}

	@Test
	void negativeWithNonZero() {
		// when - negative().nonZero() => nonZero()
		BigInteger actual = CombinableArbitrary.bigIntegers().negative().nonZero().combined();

		// then
		then(actual).isNotEqualTo(BigInteger.ZERO);
	}

	@Test
	void oddWithEvenCombination() {
		// when - odd().even() => even()
		BigInteger actual = CombinableArbitrary.bigIntegers().odd().even().combined();

		// then
		then(actual.remainder(BigInteger.valueOf(2))).isEqualTo(BigInteger.ZERO);
	}

	@Test
	void positiveWithNegativeCombination() {
		// when - positive().negative() => negative()
		BigInteger actual = CombinableArbitrary.bigIntegers().positive().negative().combined();

		// then
		then(actual).isLessThan(BigInteger.ZERO);
	}

	@Test
	void complexApiCombination() {
		// when - nonZero().positive().odd() => odd()
		BigInteger actual = CombinableArbitrary.bigIntegers().nonZero().positive().odd().combined();

		// then
		then(actual.remainder(BigInteger.valueOf(2))).isNotEqualTo(BigInteger.ZERO);
	}

	@Test
	void rangeOverridesOtherConstraints() {
		// when
		BigInteger min = BigInteger.valueOf(-50);
		BigInteger max = BigInteger.valueOf(-10);
		BigInteger actual = CombinableArbitrary.bigIntegers().nonZero().positive().odd().withRange(min, max).combined();

		// then
		then(actual).isBetween(min, max);
	}

	private boolean isPrime(BigInteger number) {
		if (number.compareTo(BigInteger.valueOf(2)) < 0) {
			return false;
		}
		if (number.equals(BigInteger.valueOf(2))) {
			return true;
		}
		if (number.remainder(BigInteger.valueOf(2)).equals(BigInteger.ZERO)) {
			return false;
		}

		BigInteger sqrt = sqrt(number);
		for (BigInteger index = BigInteger.valueOf(3);
			index.compareTo(sqrt) <= 0;
			index = index.add(BigInteger.valueOf(2))) {
			if (number.remainder(index).equals(BigInteger.ZERO)) {
				return false;
			}
		}
		return true;
	}

	private BigInteger sqrt(BigInteger number) {
		if (number.equals(BigInteger.ZERO)) {
			return BigInteger.ZERO;
		}
		BigInteger current = number;
		BigInteger next = number.add(BigInteger.ONE).divide(BigInteger.valueOf(2));
		while (next.compareTo(current) < 0) {
			current = next;
			next = current.add(number.divide(current)).divide(BigInteger.valueOf(2));
		}
		return current;
	}
}
