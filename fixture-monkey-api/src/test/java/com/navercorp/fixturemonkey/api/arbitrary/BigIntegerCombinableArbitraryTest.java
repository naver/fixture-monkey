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
import java.util.stream.IntStream;
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
		boolean allPositive = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigIntegers().positive().combined())
			.allMatch(b -> b.compareTo(BigInteger.ZERO) > 0);

		// then
		then(allPositive).isTrue();
	}

	@Test
	void negative() {
		// when
		boolean allNegative = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigIntegers().negative().combined())
			.allMatch(b -> b.compareTo(BigInteger.ZERO) < 0);

		// then
		then(allNegative).isTrue();
	}

	@Test
	void nonZero() {
		// when
		boolean allNonZero = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigIntegers().nonZero().combined())
			.noneMatch(b -> b.equals(BigInteger.ZERO));

		// then
		then(allNonZero).isTrue();
	}

	@Test
	void percentage() {
		// when
		boolean allInPercentageRange = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigIntegers().percentage().combined())
			.allMatch(b -> b.compareTo(BigInteger.ZERO) >= 0 && b.compareTo(BigInteger.valueOf(100)) <= 0);

		// then
		then(allInPercentageRange).isTrue();
	}

	@Test
	void score() {
		// given
		BigInteger min = BigInteger.valueOf(80);
		BigInteger max = BigInteger.valueOf(100);

		// when
		boolean allInScoreRange = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigIntegers().score(min, max).combined())
			.allMatch(b -> b.compareTo(min) >= 0 && b.compareTo(max) <= 0);

		// then
		then(allInScoreRange).isTrue();
	}

	@Test
	void even() {
		// when
		boolean allEven = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigIntegers().even().combined())
			.allMatch(b -> b.remainder(BigInteger.valueOf(2)).equals(BigInteger.ZERO));

		// then
		then(allEven).isTrue();
	}

	@Test
	void odd() {
		// when
		boolean allOdd = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigIntegers().odd().combined())
			.noneMatch(b -> b.remainder(BigInteger.valueOf(2)).equals(BigInteger.ZERO));

		// then
		then(allOdd).isTrue();
	}

	@Test
	void multipleOf() {
		// given
		BigInteger divisor = BigInteger.valueOf(5);

		// when
		boolean allMultipleOfFive = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigIntegers().multipleOf(divisor).combined())
			.allMatch(b -> b.remainder(divisor).equals(BigInteger.ZERO));

		// then
		then(allMultipleOfFive).isTrue();
	}

	@Test
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	void prime() {
		// when
		boolean allPrime = IntStream.range(0, 30)
			.mapToObj(i -> CombinableArbitrary.bigIntegers()
				.withRange(BigInteger.valueOf(2), BigInteger.valueOf(100))
				.prime()
				.combined())
			.allMatch(this::isPrime);

		// then
		then(allPrime).isTrue();
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
		boolean allInRange = IntStream.range(0, 30)
			.mapToObj(i -> CombinableArbitrary.bigIntegers()
				.positive()
				.withRange(BigInteger.valueOf(-10), BigInteger.valueOf(-1))
				.combined())
			.allMatch(b -> b.compareTo(BigInteger.valueOf(-10)) >= 0 && b.compareTo(BigInteger.valueOf(-1)) <= 0);

		// then
		then(allInRange).isTrue();
	}

	@Test
	void lastMethodWinsOddOverEven() {
		// when - even().odd() => odd()
		boolean allOdd = IntStream.range(0, 30)
			.mapToObj(i -> CombinableArbitrary.bigIntegers().even().odd().combined())
			.noneMatch(b -> b.remainder(BigInteger.valueOf(2)).equals(BigInteger.ZERO));

		// then
		then(allOdd).isTrue();
	}

	@Test
	void bigIntegerFilterWithMultipleOfFive() {
		// when - filter multiples of 5
		boolean allMultipleOfFive = IntStream.range(0, 30)
			.mapToObj(i -> CombinableArbitrary.bigIntegers()
				.withRange(BigInteger.ZERO, BigInteger.valueOf(100))
				.filter(b -> b.remainder(BigInteger.valueOf(5)).equals(BigInteger.ZERO)).combined())
			.allMatch(b -> b.remainder(BigInteger.valueOf(5)).equals(BigInteger.ZERO));

		// then
		then(allMultipleOfFive).isTrue();
	}

	@Test
	void nonZeroWithOdd() {
		// when - nonZero().odd() => odd()
		boolean allOdd = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigIntegers().nonZero().odd().combined())
			.noneMatch(b -> b.remainder(BigInteger.valueOf(2)).equals(BigInteger.ZERO));

		// then
		then(allOdd).isTrue();
	}

	@Test
	void nonZeroWithEven() {
		// when - nonZero().even() => even()
		boolean allEven = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigIntegers().nonZero().even().combined())
			.allMatch(b -> b.remainder(BigInteger.valueOf(2)).equals(BigInteger.ZERO));

		// then
		then(allEven).isTrue();
	}

	@Test
	void nonZeroWithPositive() {
		// when
		boolean allNonZeroAndPositive = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigIntegers().nonZero().positive().combined())
			.allMatch(b -> b.compareTo(BigInteger.ZERO) > 0);

		// then
		then(allNonZeroAndPositive).isTrue();
	}

	@Test
	void nonZeroWithNegative() {
		// when - nonZero().negative() => negative()
		boolean allNegative = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigIntegers().nonZero().negative().combined())
			.allMatch(b -> b.compareTo(BigInteger.ZERO) < 0);

		// then
		then(allNegative).isTrue();
	}

	@Test
	void negativeWithNonZero() {
		// when - negative().nonZero() => nonZero()
		boolean allNonZero = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigIntegers().negative().nonZero().combined())
			.noneMatch(b -> b.equals(BigInteger.ZERO));

		// then
		then(allNonZero).isTrue();
	}

	@Test
	void oddWithEvenCombination() {
		// when - odd().even() => even()
		boolean allEven = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigIntegers().odd().even().combined())
			.allMatch(b -> b.remainder(BigInteger.valueOf(2)).equals(BigInteger.ZERO));

		// then
		then(allEven).isTrue();
	}

	@Test
	void positiveWithNegativeCombination() {
		// when - positive().negative() => negative()
		boolean allNegative = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigIntegers().positive().negative().combined())
			.allMatch(b -> b.compareTo(BigInteger.ZERO) < 0);

		// then
		then(allNegative).isTrue();
	}

	@Test
	void complexApiCombination() {
		// when - nonZero().positive().odd() => odd()
		boolean allOdd = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigIntegers().nonZero().positive().odd().combined())
			.noneMatch(b -> b.remainder(BigInteger.valueOf(2)).equals(BigInteger.ZERO));

		// then
		then(allOdd).isTrue();
	}

	@Test
	void rangeOverridesOtherConstraints() {
		// when
		BigInteger min = BigInteger.valueOf(-50);
		BigInteger max = BigInteger.valueOf(-10);
		boolean allInRange = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigIntegers().nonZero().positive().odd().withRange(min, max).combined())
			.allMatch(b -> b.compareTo(min) >= 0 && b.compareTo(max) <= 0);

		// then
		then(allInRange).isTrue();
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
