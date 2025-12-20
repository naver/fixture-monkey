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

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class DoubleCombinableArbitraryTest {
	@Test
	void combined() {
		// when
		Double actual = CombinableArbitrary.doubles().combined();

		// then
		then(actual).isInstanceOf(Double.class);
	}

	@Test
	void withRange() {
		// given
		double min = 1.0;
		double max = 10.0;

		// when
		Double actual = CombinableArbitrary.doubles().withRange(min, max).combined();

		// then
		then(actual).isBetween(min, max);
	}

	@Test
	void positive() {
		// when
		Double actual = CombinableArbitrary.doubles().positive().combined();

		// then
		then(actual).isPositive();
	}

	@Test
	void negative() {
		// when
		Double actual = CombinableArbitrary.doubles().negative().combined();

		// then
		then(actual).isNegative();
	}

	@Test
	void nonZero() {
		// when
		Double actual = CombinableArbitrary.doubles().nonZero().combined();

		// then
		then(actual).isNotEqualTo(0.0);
	}

	@Test
	void withPrecision() {
		// given
		int scale = 2;

		// when
		Double actual = CombinableArbitrary.doubles().withPrecision(scale).combined();

		// then
		String actualStr = actual.toString();
		int decimalIndex = actualStr.indexOf('.');
		if (decimalIndex != -1) {
			int actualScale = actualStr.length() - decimalIndex - 1;
			then(actualScale).isLessThanOrEqualTo(scale);
		}
	}

	@Test
	void finite() {
		// when
		Double actual = CombinableArbitrary.doubles().finite().combined();

		// then
		then(Double.isFinite(actual)).isTrue();
	}

	@Test
	void infinite() {
		// when
		Double actual = CombinableArbitrary.doubles().infinite().combined();

		// then
		then(Double.isInfinite(actual)).isTrue();
	}

	@Test
	void normalized() {
		// when
		Double actual = CombinableArbitrary.doubles().normalized().combined();

		// then
		then(actual).isBetween(0.0, 1.0);
	}

	@Test
	void nan() {
		// when
		Double actual = CombinableArbitrary.doubles().nan().combined();

		// then
		then(Double.isNaN(actual)).isTrue();
	}

	@Test
	void percentage() {
		// when
		Double actual = CombinableArbitrary.doubles().percentage().combined();

		// then
		then(actual).isBetween(0.0, 100.0);
	}

	@Test
	void score() {
		// when
		Double actual = CombinableArbitrary.doubles().score().combined();

		// then
		then(actual).isBetween(0.0, 100.0);
	}

	@Test
	void withSpecialValue() {
		// given
		double specialValue = 999.9;

		// when - try multiple times to verify special value injection
		boolean specialValueFound = IntStream.range(0, 1000)
			.mapToObj(i -> CombinableArbitrary.doubles().withRange(1.0, 10.0).withSpecialValue(specialValue).combined())
			.anyMatch(d -> Double.compare(d, specialValue) == 0);

		// then
		then(specialValueFound).isTrue();
	}

	@Test
	void withStandardSpecialValues() {
		// when - try multiple times to verify standard special values injection
		boolean hasNaN = IntStream.range(0, 1000)
			.mapToObj(i -> CombinableArbitrary.doubles().withRange(1.0, 10.0).withStandardSpecialValues().combined())
			.anyMatch(d -> Double.isNaN(d));

		boolean hasInfinity = IntStream.range(0, 1000)
			.mapToObj(i -> CombinableArbitrary.doubles().withRange(1.0, 10.0).withStandardSpecialValues().combined())
			.anyMatch(d -> Double.isInfinite(d));

		// then
		then(hasNaN || hasInfinity).isTrue(); // at least one special value should be generated
	}

	@Test
	void lastMethodWinsPositiveOverNegative() {
		// when - negative().positive() => positive()
		Double actual = CombinableArbitrary.doubles().negative().positive().combined();

		// then
		then(actual).isPositive();
	}

	@Test
	void lastMethodWinsRangeOverPositive() {
		// when - positive().withRange(-10, -1) => withRange(-10, -1)
		Double actual = CombinableArbitrary.doubles().positive().withRange(-10.0, -1.0).combined();

		// then
		then(actual).isBetween(-10.0, -1.0);
	}

	@Test
	void fixed() {
		// when
		boolean actual = CombinableArbitrary.doubles().fixed();

		// then
		then(actual).isFalse();
	}
}
