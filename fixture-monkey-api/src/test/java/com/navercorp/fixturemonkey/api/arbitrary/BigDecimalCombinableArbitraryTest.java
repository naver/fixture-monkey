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

import java.math.BigDecimal;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class BigDecimalCombinableArbitraryTest {
	@Test
	void combined() {
		// when
		BigDecimal actual = CombinableArbitrary.bigDecimals().combined();

		// then
		then(actual).isInstanceOf(BigDecimal.class);
	}

	@Test
	void withRange() {
		// given
		BigDecimal min = BigDecimal.valueOf(10.5);
		BigDecimal max = BigDecimal.valueOf(50.5);

		// when
		BigDecimal actual = CombinableArbitrary.bigDecimals().withRange(min, max).combined();

		// then
		then(actual).isBetween(min, max);
	}

	@Test
	void positive() {
		// when
		boolean allPositive = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigDecimals().positive().combined())
			.allMatch(b -> b.compareTo(BigDecimal.ZERO) > 0);

		// then
		then(allPositive).isTrue();
	}

	@Test
	void negative() {
		// when
		boolean allNegative = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigDecimals().negative().combined())
			.allMatch(b -> b.compareTo(BigDecimal.ZERO) < 0);

		// then
		then(allNegative).isTrue();
	}

	@Test
	void nonZero() {
		// when
		boolean allNonZero = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigDecimals().nonZero().combined())
			.allMatch(b -> b.compareTo(BigDecimal.ZERO) != 0);

		// then
		then(allNonZero).isTrue();
	}

	@Test
	void percentage() {
		// when
		boolean allInPercentageRange = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigDecimals().percentage().combined())
			.allMatch(b -> b.compareTo(BigDecimal.ZERO) >= 0 && b.compareTo(BigDecimal.valueOf(100)) <= 0);

		// then
		then(allInPercentageRange).isTrue();
	}

	@Test
	void score() {
		// given
		BigDecimal min = BigDecimal.valueOf(80.5);
		BigDecimal max = BigDecimal.valueOf(100.0);

		// when
		boolean allInScoreRange = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigDecimals().score(min, max).combined())
			.allMatch(b -> b.compareTo(min) >= 0 && b.compareTo(max) <= 0);

		// then
		then(allInScoreRange).isTrue();
	}

	@Test
	void withPrecision() {
		// given
		int precision = 3;

		// when
		boolean allWithCorrectPrecision = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigDecimals().withPrecision(precision).combined())
			.allMatch(b -> b.precision() <= precision);

		// then
		then(allWithCorrectPrecision).isTrue();
	}

	@Test
	void withScale() {
		// given
		int scale = 2;

		// when
		boolean allWithCorrectScale = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigDecimals().withScale(scale).combined())
			.allMatch(b -> b.scale() == scale);

		// then
		then(allWithCorrectScale).isTrue();
	}

	@Test
	void normalized() {
		// when
		boolean allNormalized = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigDecimals().normalized().combined())
			.allMatch(b -> b.equals(b.stripTrailingZeros()));

		// then
		then(allNormalized).isTrue();
	}

	@Test
	void lastMethodWinsWithPositiveAndRange() {
		// given
		BigDecimal min = BigDecimal.valueOf(-50.5);
		BigDecimal max = BigDecimal.valueOf(-10.5);

		// when - positive().withRange() => withRange()
		BigDecimal actual = CombinableArbitrary.bigDecimals().positive().withRange(min, max).combined();

		// then
		then(actual).isBetween(min, max);
	}

	@Test
	void bigDecimalMapping() {
		// when
		String actual = CombinableArbitrary.bigDecimals()
			.positive()
			.map(x -> "bigdecimal:" + x)
			.combined();

		// then
		then(actual).startsWith("bigdecimal:");
		String numberPart = actual.substring(11);
		BigDecimal value = new BigDecimal(numberPart);
		then(value).isGreaterThan(BigDecimal.ZERO);
	}

	@Test
	void bigDecimalFiltering() {
		// when
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.withRange(BigDecimal.ZERO, BigDecimal.valueOf(100))
			.filter(b -> b.compareTo(BigDecimal.valueOf(50)) > 0)
			.combined();

		// then
		then(actual).isGreaterThan(BigDecimal.valueOf(50));
		then(actual).isLessThanOrEqualTo(BigDecimal.valueOf(100));
	}

	@Test
	void bigDecimalFilteringWithMultipleConditions() {
		// when
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.positive()
			.filter(b -> b.remainder(BigDecimal.valueOf(0.5)).compareTo(BigDecimal.ZERO) == 0)  // multiples of 0.5
			.combined();

		// then
		then(actual).isGreaterThan(BigDecimal.ZERO);
		then(actual.remainder(BigDecimal.valueOf(0.5))).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	void bigDecimalInjectNull() {
		// when
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.positive()
			.injectNull(1.0)  // 100% null
			.combined();

		// then
		then(actual).isNull();
	}

	@Test
	void bigDecimalInjectNullWithZeroProbability() {
		// when
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.positive()
			.injectNull(0.0)  // 0% null
			.combined();

		// then
		then(actual).isNotNull();
		then(actual).isGreaterThan(BigDecimal.ZERO);
	}

	@Test
	void bigDecimalCombinationWithMultipleOperations() {
		// when - combine multiple operations
		String actual = CombinableArbitrary.bigDecimals()
			.percentage()
			.filter(b -> b.compareTo(BigDecimal.valueOf(50)) >= 0)  // >= 50
			.map(b -> "percentage:" + b)
			.combined();

		// then
		then(actual).startsWith("percentage:");
		String numberPart = actual.substring(11);
		BigDecimal value = new BigDecimal(numberPart);
		then(value).isBetween(BigDecimal.valueOf(50), BigDecimal.valueOf(100));
	}

	@Test
	void bigDecimalUniqueWithDifferentValues() {
		// when - unique() works with different values
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.withRange(BigDecimal.ZERO, BigDecimal.valueOf(1000))  // wide range
			.unique()
			.combined();

		// then
		then(actual).isBetween(BigDecimal.ZERO, BigDecimal.valueOf(1000));
	}

	@Test
	void fixed() {
		// when
		boolean actual = CombinableArbitrary.bigDecimals().fixed();

		// then
		then(actual).isFalse();
	}

	@Test
	void lastMethodWinsRangeOverPositive() {
		// when - positive().withRange() => withRange()
		boolean allInRange = IntStream.range(0, 30)
			.mapToObj(i -> CombinableArbitrary.bigDecimals().positive().withRange(BigDecimal.valueOf(-10.5), BigDecimal.valueOf(-1.5)).combined())
			.allMatch(b -> b.compareTo(BigDecimal.valueOf(-10.5)) >= 0 && b.compareTo(BigDecimal.valueOf(-1.5)) <= 0);

		// then
		then(allInRange).isTrue();
	}

	@Test
	void lastMethodWinsPrecisionOverScale() {
		// when - withPrecision().withScale() => withScale()
		boolean allWithCorrectScale = IntStream.range(0, 30)
			.mapToObj(i -> CombinableArbitrary.bigDecimals().withPrecision(5).withScale(2).combined())
			.allMatch(b -> b.scale() == 2);

		// then
		then(allWithCorrectScale).isTrue();
	}

	@Test
	void bigDecimalFilterWithPositiveAndScale() {
		// when - filter positive with specific scale
		boolean allPositiveWithScale = IntStream.range(0, 30)
			.mapToObj(i -> CombinableArbitrary.bigDecimals()
				.positive()
				.withScale(2)
				.filter(b -> b.compareTo(BigDecimal.valueOf(10)) >= 0).combined())
			.allMatch(b -> b.compareTo(BigDecimal.ZERO) > 0 && b.scale() == 2 && b.compareTo(BigDecimal.valueOf(10)) >= 0);

		// then
		then(allPositiveWithScale).isTrue();
	}

	@Test
	void nonZeroWithPrecision() {
		// when - nonZero().withPrecision() => withPrecision()
		boolean allWithCorrectPrecision = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigDecimals().nonZero().withPrecision(3).combined())
			.allMatch(b -> b.precision() <= 3);

		// then
		then(allWithCorrectPrecision).isTrue();
	}

	@Test
	void nonZeroWithScale() {
		// when - nonZero().withScale() => withScale()
		boolean allWithCorrectScale = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigDecimals().nonZero().withScale(2).combined())
			.allMatch(b -> b.scale() == 2);

		// then
		then(allWithCorrectScale).isTrue();
	}

	@Test
	void nonZeroWithPositive() {
		// when
		boolean allNonZeroAndPositive = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigDecimals().nonZero().positive().combined())
			.allMatch(b -> b.compareTo(BigDecimal.ZERO) > 0);

		// then
		then(allNonZeroAndPositive).isTrue();
	}

	@Test
	void nonZeroWithNegative() {
		// when - nonZero().negative() => negative()
		boolean allNegative = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigDecimals().nonZero().negative().combined())
			.allMatch(b -> b.compareTo(BigDecimal.ZERO) < 0);

		// then
		then(allNegative).isTrue();
	}

	@Test
	void negativeWithNonZero() {
		// when - negative().nonZero() => nonZero()
		boolean allNonZero = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigDecimals().negative().nonZero().combined())
			.allMatch(b -> b.compareTo(BigDecimal.ZERO) != 0);

		// then
		then(allNonZero).isTrue();
	}

	@Test
	void positiveWithNegativeCombination() {
		// when - positive().negative() => negative()
		boolean allNegative = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigDecimals().positive().negative().combined())
			.allMatch(b -> b.compareTo(BigDecimal.ZERO) < 0);

		// then
		then(allNegative).isTrue();
	}

	@Test
	void complexApiCombination() {
		// when - nonZero().positive().withScale() => withScale()
		boolean allWithCorrectScale = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigDecimals().nonZero().positive().withScale(2).combined())
			.allMatch(b -> b.scale() == 2);

		// then
		then(allWithCorrectScale).isTrue();
	}

	@Test
	void rangeOverridesOtherConstraints() {
		// when
		BigDecimal min = BigDecimal.valueOf(-50.5);
		BigDecimal max = BigDecimal.valueOf(-10.5);
		boolean allInRange = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigDecimals().nonZero().positive().withScale(3).withRange(min, max).combined())
			.allMatch(b -> b.compareTo(min) >= 0 && b.compareTo(max) <= 0);

		// then
		then(allInRange).isTrue();
	}

	@Test
	void normalizedWithScale() {
		// when - normalized().withScale() => withScale()
		boolean allWithCorrectScale = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigDecimals().normalized().withScale(2).combined())
			.allMatch(b -> b.scale() == 2);

		// then
		then(allWithCorrectScale).isTrue();
	}

	@Test
	void scaleWithNormalized() {
		// when - withScale().normalized() => normalized()
		boolean allNormalized = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigDecimals().withScale(2).normalized().combined())
			.allMatch(b -> b.equals(b.stripTrailingZeros()));

		// then
		then(allNormalized).isTrue();
	}

	@Test
	void precisionWithNormalized() {
		// when - withPrecision().normalized() => normalized()
		boolean allNormalized = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigDecimals().withPrecision(5).normalized().combined())
			.allMatch(b -> b.equals(b.stripTrailingZeros()));

		// then
		then(allNormalized).isTrue();
	}

	@Test
	void normalizedWithPrecision() {
		// when - normalized().withPrecision() => withPrecision()
		boolean allWithCorrectPrecision = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bigDecimals().normalized().withPrecision(3).combined())
			.allMatch(b -> b.precision() <= 3);

		// then
		then(allWithCorrectPrecision).isTrue();
	}
}
