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
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.api.exception.FixedValueFilterMissException;

class LongCombinableArbitraryTest {
	@Test
	void combined() {
		// when
		Long actual = CombinableArbitrary.longs().combined();

		// then
		then(actual).isInstanceOf(Long.class);
	}

	@Test
	void withRange() {
		// given
		long min = 100L;
		long max = 200L;

		// when
		Long actual = CombinableArbitrary.longs().withRange(min, max).combined();

		// then
		then(actual).isBetween(min, max);
	}

	@Test
	void positive() {
		// when
		Long actual = CombinableArbitrary.longs().positive().combined();

		// then
		then(actual).isPositive();
	}

	@Test
	void negative() {
		// when
		Long actual = CombinableArbitrary.longs().negative().combined();

		// then
		then(actual).isNegative();
	}

	@Test
	void even() {
		// when
		boolean allEven = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.longs().even().combined())
			.allMatch(value -> value % 2 == 0);

		// then
		then(allEven).isTrue();
	}

	@Test
	void odd() {
		// when
		boolean allOdd = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.longs().odd().combined())
			.allMatch(value -> value % 2 != 0);

		// then
		then(allOdd).isTrue();
	}

	@Test
	void lastMethodWinsWithPositiveAndRange() {
		// given
		long min = 100L;
		long max = 200L;

		// positive().withRange(min, max) => withRange(min, max)
		Long actual = CombinableArbitrary.longs().positive().withRange(min, max).combined();

		// then
		then(actual).isBetween(min, max);
	}

	@Test
	void longUnique() {
		// filter().unique() => FixedValueFilterMissException
		thenThrownBy(() -> CombinableArbitrary.longs()
			.filter(x -> x == 123L)
			.unique()
			.combined())
			.isExactlyInstanceOf(FixedValueFilterMissException.class);
	}

	@Test
	void longMapping() {
		// when
		String actual = CombinableArbitrary.longs()
			.withRange(100L, 200L)
			.map(x -> "prefix" + x)
			.combined();

		// then
		then(actual).startsWith("prefix");
		then(actual).contains("1");
	}

	@Test
	void longFiltering() {
		// when
		Long actual = CombinableArbitrary.longs()
			.withRange(0L, 1000L)
			.filter(x -> x > 500L)
			.combined();

		// then
		then(actual).isGreaterThan(500L);
		then(actual).isLessThanOrEqualTo(1000L);
	}

	@Test
	void longFilteringWithMultipleConditions() {
		// when
		Long actual = CombinableArbitrary.longs()
			.withRange(0L, 100L)
			.filter(x -> x % 10 == 0)
			.combined();

		// then
		then(actual % 10).isEqualTo(0L);
		then(actual).isBetween(0L, 100L);
	}

	@Test
	void longInjectNull() {
		// when
		Long actual = CombinableArbitrary.longs()
			.withRange(1L, 100L)
			.injectNull(1.0)
			.combined();

		// then
		then(actual).isNull();
	}

	@Test
	void longInjectNullWithZeroProbability() {
		// when
		Long actual = CombinableArbitrary.longs()
			.withRange(1L, 100L)
			.injectNull(0.0)
			.combined();

		// then
		then(actual).isNotNull();
		then(actual).isBetween(1L, 100L);
	}

	@Test
	void longCombinationWithMultipleOperations() {
		// when
		String actual = CombinableArbitrary.longs()
			.withRange(1L, 10L)
			.filter(x -> x > 5L)
			.map(x -> "value:" + x)
			.combined();

		// then
		then(actual).startsWith("value:");
		String numberPart = actual.substring(6);
		long number = Long.parseLong(numberPart);
		then(number).isBetween(6L, 10L);
	}

	@Test
	void longUniqueWithDifferentValues() {
		// when
		Long actual = CombinableArbitrary.longs()
			.withRange(1L, 1000L)
			.unique()
			.combined();

		// then
		then(actual).isBetween(1L, 1000L);
	}

	@Test
	void fixed() {
		// when
		boolean actual = CombinableArbitrary.longs().fixed();

		// then
		then(actual).isFalse();
	}

	@Test
	void lastMethodWinsPositiveOverNegative() {
		// positive().negative() => negative()
		boolean allNegative = IntStream.range(0, 30)
			.mapToObj(i -> CombinableArbitrary.longs().positive().negative().combined())
			.allMatch(value -> value < 0);

		then(allNegative).isTrue();
	}

	@Test
	void lastMethodWinsEvenOverOdd() {
		// even().odd() => odd()
		boolean allOdd = IntStream.range(0, 30)
			.mapToObj(i -> CombinableArbitrary.longs().even().odd().combined())
			.allMatch(value -> value % 2 != 0);

		then(allOdd).isTrue();
	}

	@Test
	void nonZero() {
		// when
		boolean allNonZero = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.longs().nonZero().combined())
			.allMatch(value -> value != 0);

		// then
		then(allNonZero).isTrue();
	}

	@Test
	void multipleOf() {
		// when
		boolean allMultipleOfFive = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.longs().multipleOf(5L).combined())
			.allMatch(value -> value % 5 == 0);

		// then
		then(allMultipleOfFive).isTrue();
	}

	@Test
	void nonZeroCombined() {
		// when
		Long actual = CombinableArbitrary.longs().nonZero().combined();

		// then
		then(actual).isNotEqualTo(0L);
	}

	@Test
	void multipleOfCombined() {
		// when
		Long actual = CombinableArbitrary.longs().multipleOf(7L).combined();

		// then
		then(actual % 7L).isEqualTo(0L);
	}

	@Test
	void nonZeroWithRangeCombined() {
		// withRange(-5L, 5L).nonZero() => nonZero()
		Long actual = CombinableArbitrary.longs()
			.withRange(-5L, 5L)
			.nonZero()
			.combined();

		// then
		then(actual).isNotEqualTo(0L);
	}

	@Test
	void multipleOfWithPositiveAndRangeCombined() {
		// positive().withRange(1L, 50L).multipleOf(3L) => multipleOf(3L)
		Long actual = CombinableArbitrary.longs()
			.positive()
			.withRange(1L, 50L)
			.multipleOf(3L)
			.combined();

		// then
		then(actual % 3L).isEqualTo(0L);
	}

	@Test
	void nonZeroWithRangeExcludingZero() {
		// when - range already excludes 0, nonZero() is redundant but harmless
		Long actual = CombinableArbitrary.longs()
			.withRange(10L, 20L)
			.nonZero()
			.combined();

		// then
		then(actual).isNotEqualTo(0L);
		then(actual).isBetween(10L, 20L);
	}

	@Test
	void multipleOfWithComplexConstraints() {
		// when - multiple constraints: negative + range + multiple of 4
		Long actual = CombinableArbitrary.longs()
			.negative()
			.withRange(-100L, -1L)
			.multipleOf(4L)
			.combined();

		// then
		then(actual).isNegative();
		then(actual).isBetween(-100L, -1L);
		then(actual % 4L).isEqualTo(0L);
	}

}
