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

class FloatCombinableArbitraryTest {

	@Test
	void combined() {
		// when
		Float actual = CombinableArbitrary.floats().combined();

		// then
		then(actual).isInstanceOf(Float.class);
	}

	@Test
	void withRange() {
		// given
		float min = 1.0f;
		float max = 10.0f;

		// when
		Float actual = CombinableArbitrary.floats().withRange(min, max).combined();

		// then
		then(actual).isBetween(min, max);
	}

	@Test
	void positive() {
		// when
		boolean allPositive = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.floats().positive().combined())
			.allMatch(f -> f > 0.0f);

		// then
		then(allPositive).isTrue();
	}

	@Test
	void negative() {
		// when
		boolean allNegative = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.floats().negative().combined())
			.allMatch(f -> f < 0.0f);

		// then
		then(allNegative).isTrue();
	}

	@Test
	void nonZero() {
		// when
		boolean allNonZero = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.floats().nonZero().combined())
			.allMatch(f -> f != 0.0f);

		// then
		then(allNonZero).isTrue();
	}

	@Test
	void withPrecision() {
		// given
		int scale = 2;

		// when
		Float actual = CombinableArbitrary.floats().withPrecision(scale).combined();

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
			.mapToObj(i -> CombinableArbitrary.floats().finite().combined())
			.allMatch(Float::isFinite);

		// then
		then(allFinite).isTrue();
	}

	@Test
	void infinite() {
		// when
		Float actual = CombinableArbitrary.floats().infinite().combined();

		// then
		then(Float.isInfinite(actual)).isTrue();
	}

	@Test
	void normalized() {
		// when
		boolean allNormalized = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.floats().normalized().combined())
			.allMatch(f -> f >= 0.0f && f <= 1.0f);

		// then
		then(allNormalized).isTrue();
	}

	@Test
	void nan() {
		// when
		Float actual = CombinableArbitrary.floats().nan().combined();

		// then
		then(Float.isNaN(actual)).isTrue();
	}

	@Test
	void percentage() {
		// when
		boolean allPercentage = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.floats().percentage().combined())
			.allMatch(f -> f >= 0.0f && f <= 100.0f);

		// then
		then(allPercentage).isTrue();
	}

	@Test
	void score() {
		// when
		boolean allScore = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.floats().score().combined())
			.allMatch(f -> f >= 0.0f && f <= 100.0f);

		// then
		then(allScore).isTrue();
	}

	@Test
	void withSpecialValue() {
		// given
		float specialValue = 999.9f;

		// when - try multiple times to verify standard special values injection
		boolean specialValueFound = IntStream.range(0, 1000)
			.mapToObj(
				i -> CombinableArbitrary.floats().withRange(1.0f, 10.0f).withSpecialValue(specialValue).combined())
			.anyMatch(f -> Float.compare(f, specialValue) == 0);

		// then
		then(specialValueFound).isTrue();
	}

	@Test
	void withStandardSpecialValues() {
		// when - try multiple times to verify standard special values injection
		boolean hasNaN = IntStream.range(0, 1000)
			.mapToObj(i -> CombinableArbitrary.floats().withRange(1.0f, 10.0f).withStandardSpecialValues().combined())
			.anyMatch(f -> Float.isNaN((Float)f));

		boolean hasInfinity = IntStream.range(0, 1000)
			.mapToObj(i -> CombinableArbitrary.floats().withRange(1.0f, 10.0f).withStandardSpecialValues().combined())
			.anyMatch(f -> Float.isInfinite((Float)f));

		// then
		then(hasNaN || hasInfinity).isTrue(); // 최소한 하나의 특별값은 생성되어야 함
	}

	@Test
	void lastMethodWinsPositiveOverNegative() {
		// when - negative().positive() => positive()
		boolean allPositive = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.floats().negative().positive().combined())
			.allMatch(f -> f > 0.0f);

		// then
		then(allPositive).isTrue();
	}

	@Test
	void lastMethodWinsRangeOverPositive() {
		// when - positive().withRange(-10, -1) => withRange(-10, -1)
		boolean allInRange = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.floats().positive().withRange(-10.0f, -1.0f).combined())
			.allMatch(f -> f >= -10.0f && f <= -1.0f);

		// then
		then(allInRange).isTrue();
	}

	@Test
	void fixed() {
		// when
		boolean actual = CombinableArbitrary.floats().fixed();

		// then
		then(actual).isFalse();
	}
}
