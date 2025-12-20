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
		BigDecimal actual = CombinableArbitrary.bigDecimals().positive().combined();

		// then
		then(actual).isGreaterThan(BigDecimal.ZERO);
	}

	@Test
	void negative() {
		// when
		BigDecimal actual = CombinableArbitrary.bigDecimals().negative().combined();

		// then
		then(actual).isLessThan(BigDecimal.ZERO);
	}

	@Test
	void nonZero() {
		// when
		BigDecimal actual = CombinableArbitrary.bigDecimals().nonZero().combined();

		// then
		then(actual).isNotEqualTo(BigDecimal.ZERO);
	}

	@Test
	void percentage() {
		// when
		BigDecimal actual = CombinableArbitrary.bigDecimals().percentage().combined();

		// then
		then(actual).isBetween(BigDecimal.ZERO, BigDecimal.valueOf(100));
	}

	@Test
	void score() {
		// given
		BigDecimal min = BigDecimal.valueOf(80.5);
		BigDecimal max = BigDecimal.valueOf(100.0);

		// when
		BigDecimal actual = CombinableArbitrary.bigDecimals().score(min, max).combined();

		// then
		then(actual).isBetween(min, max);
	}

	@Test
	void withPrecision() {
		// given
		int precision = 3;

		// when
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.withPrecision(precision)
			.combined();

		// then
		then(actual.precision()).isLessThanOrEqualTo(precision);
	}

	@Test
	void withScale() {
		// given
		int scale = 2;

		// when
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.withScale(scale)
			.combined();

		// then
		then(actual.scale()).isEqualTo(scale);
	}

	@Test
	void normalized() {
		// when
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.normalized()
			.combined();

		// then
		then(actual).isEqualTo(actual.stripTrailingZeros());
	}

	@Test
	void lastMethodWinsWithPositiveAndRange() {
		// given
		BigDecimal min = BigDecimal.valueOf(-50.5);
		BigDecimal max = BigDecimal.valueOf(-10.5);

		// when - positive().withRange() => withRange()
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.positive()
			.withRange(min, max)
			.combined();

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
			.filter(b -> b.remainder(BigDecimal.valueOf(0.5))
				.compareTo(BigDecimal.ZERO) == 0)
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
			.injectNull(1.0)
			.combined();

		// then
		then(actual).isNull();
	}

	@Test
	void bigDecimalInjectNullWithZeroProbability() {
		// when
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.positive()
			.injectNull(0.0)
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
			.filter(b -> b.compareTo(BigDecimal.valueOf(50)) >= 0)
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
			.withRange(BigDecimal.ZERO, BigDecimal.valueOf(1000))
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
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.positive()
			.withRange(BigDecimal.valueOf(-10.5), BigDecimal.valueOf(-1.5))
			.combined();

		// then
		then(actual).isBetween(BigDecimal.valueOf(-10.5), BigDecimal.valueOf(-1.5));
	}

	@Test
	void lastMethodWinsPrecisionOverScale() {
		// when - withPrecision().withScale() => withScale()
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.withPrecision(5)
			.withScale(2)
			.combined();

		// then
		then(actual.scale()).isEqualTo(2);
	}

	@Test
	void bigDecimalFilterWithPositiveAndScale() {
		// when - filter positive with specific scale
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.positive()
			.withScale(2)
			.filter(b -> b.compareTo(BigDecimal.valueOf(10)) >= 0)
			.combined();

		// then
		then(actual).isGreaterThan(BigDecimal.ZERO);
		then(actual.scale()).isEqualTo(2);
		then(actual).isGreaterThanOrEqualTo(BigDecimal.valueOf(10));
	}

	@Test
	void nonZeroWithPrecision() {
		// when - nonZero().withPrecision() => withPrecision()
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.nonZero()
			.withPrecision(3)
			.combined();

		// then
		then(actual.precision()).isLessThanOrEqualTo(3);
	}

	@Test
	void nonZeroWithScale() {
		// when - nonZero().withScale() => withScale()
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.nonZero()
			.withScale(2)
			.combined();

		// then
		then(actual.scale()).isEqualTo(2);
	}

	@Test
	void nonZeroWithPositive() {
		// when
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.nonZero()
			.positive()
			.combined();

		// then
		then(actual).isGreaterThan(BigDecimal.ZERO);
	}

	@Test
	void nonZeroWithNegative() {
		// when - nonZero().negative() => negative()
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.nonZero()
			.negative()
			.combined();

		// then
		then(actual).isLessThan(BigDecimal.ZERO);
	}

	@Test
	void negativeWithNonZero() {
		// when - negative().nonZero() => nonZero()
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.negative()
			.nonZero()
			.combined();

		// then
		then(actual).isNotEqualTo(BigDecimal.ZERO);
	}

	@Test
	void positiveWithNegativeCombination() {
		// when - positive().negative() => negative()
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.positive()
			.negative()
			.combined();

		// then
		then(actual).isLessThan(BigDecimal.ZERO);
	}

	@Test
	void complexApiCombination() {
		// when - nonZero().positive().withScale() => withScale()
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.nonZero()
			.positive()
			.withScale(2)
			.combined();

		// then
		then(actual.scale()).isEqualTo(2);
	}

	@Test
	void rangeOverridesOtherConstraints() {
		// when
		BigDecimal min = BigDecimal.valueOf(-50.5);
		BigDecimal max = BigDecimal.valueOf(-10.5);
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.nonZero()
			.positive()
			.withScale(3)
			.withRange(min, max)
			.combined();

		// then
		then(actual).isBetween(min, max);
	}

	@Test
	void normalizedWithScale() {
		// when - normalized().withScale() => withScale()
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.normalized()
			.withScale(2)
			.combined();

		// then
		then(actual.scale()).isEqualTo(2);
	}

	@Test
	void scaleWithNormalized() {
		// when - withScale().normalized() => normalized()
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.withScale(2)
			.normalized()
			.combined();

		// then
		then(actual).isEqualTo(actual.stripTrailingZeros());
	}

	@Test
	void precisionWithNormalized() {
		// when - withPrecision().normalized() => normalized()
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.withPrecision(5)
			.normalized()
			.combined();

		// then
		then(actual).isEqualTo(actual.stripTrailingZeros());
	}

	@Test
	void normalizedWithPrecision() {
		// when - normalized().withPrecision() => withPrecision()
		BigDecimal actual = CombinableArbitrary.bigDecimals()
			.normalized()
			.withPrecision(3)
			.combined();

		// then
		then(actual.precision()).isLessThanOrEqualTo(3);
	}
}
