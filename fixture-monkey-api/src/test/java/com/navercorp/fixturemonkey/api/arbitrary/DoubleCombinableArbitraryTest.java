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
		boolean allPositive = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.doubles().positive().combined())
			.allMatch(d -> d > 0.0);

		// then
		then(allPositive).isTrue();
	}

	@Test
	void negative() {
		// when
		boolean allNegative = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.doubles().negative().combined())
			.allMatch(d -> d < 0.0);

		// then
		then(allNegative).isTrue();
	}

	@Test
	void nonZero() {
		// when
		boolean allNonZero = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.doubles().nonZero().combined())
			.allMatch(d -> d != 0.0);

		// then
		then(allNonZero).isTrue();
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
		boolean allFinite = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.doubles().finite().combined())
			.allMatch(Double::isFinite);

		// then
		then(allFinite).isTrue();
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
		boolean allNormalized = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.doubles().normalized().combined())
			.allMatch(d -> d >= 0.0 && d <= 1.0);

		// then
		then(allNormalized).isTrue();
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
		boolean allPercentage = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.doubles().percentage().combined())
			.allMatch(d -> d >= 0.0 && d <= 100.0);

		// then
		then(allPercentage).isTrue();
	}

	@Test
	void score() {
		// when
		boolean allScore = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.doubles().score().combined())
			.allMatch(d -> d >= 0.0 && d <= 100.0);

		// then
		then(allScore).isTrue();
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
		boolean allPositive = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.doubles().negative().positive().combined())
			.allMatch(d -> d > 0.0);

		// then
		then(allPositive).isTrue();
	}

	@Test
	void lastMethodWinsRangeOverPositive() {
		// when - positive().withRange(-10, -1) => withRange(-10, -1)
		boolean allInRange = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.doubles().positive().withRange(-10.0, -1.0).combined())
			.allMatch(d -> d >= -10.0 && d <= -1.0);

		// then
		then(allInRange).isTrue();
	}

	@Test
	void fixed() {
		// when
		boolean actual = CombinableArbitrary.doubles().fixed();

		// then
		then(actual).isFalse();
	}
}
