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
import java.math.RoundingMode;

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
		Float actual = CombinableArbitrary.floats().positive().combined();

		// then
		then(actual).isPositive();
	}

	@Test
	void negative() {
		// when
		Float actual = CombinableArbitrary.floats().negative().combined();

		// then
		then(actual).isNegative();
	}

	@Test
	void nonZero() {
		// when
		Float actual = CombinableArbitrary.floats().nonZero().combined();

		// then
		then(actual).isNotEqualTo(0.0f);
	}

	@Test
	void withPrecision() {
		// given
		int scale = 2;

		// when
		Float actual = CombinableArbitrary.floats().withPrecision(scale).combined();

		// then — re-applying setScale(scale) is a no-op when the value already
		// has the requested precision (idempotent round-trip works at any magnitude)
		float rescaled = BigDecimal.valueOf(actual).setScale(scale, RoundingMode.HALF_UP).floatValue();
		then(actual).isEqualTo(rescaled);
	}

	@Test
	void finite() {
		// when
		Float actual = CombinableArbitrary.floats().finite().combined();

		// then
		then(Float.isFinite(actual)).isTrue();
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
		Float actual = CombinableArbitrary.floats().normalized().combined();

		// then
		then(actual).isBetween(0.0f, 1.0f);
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
		Float actual = CombinableArbitrary.floats().percentage().combined();

		// then
		then(actual).isBetween(0.0f, 100.0f);
	}

	@Test
	void score() {
		// when
		Float actual = CombinableArbitrary.floats().score().combined();

		// then
		then(actual).isBetween(0.0f, 100.0f);
	}

	@Test
	void withSpecialValue() {
		// given
		float specialValue = 999.9f;

		// when
		float actual = CombinableArbitrary.floats()
			.withRange(1.0f, 10.0f)
			.withSpecialValue(specialValue)
			.combined();

		// then — result is either the special value or within the configured range
		then(Float.compare(actual, specialValue) == 0 || (actual >= 1.0f && actual <= 10.0f)).isTrue();
	}

	@Test
	void withStandardSpecialValues() {
		// when
		float actual = CombinableArbitrary.floats()
			.withRange(1.0f, 10.0f)
			.withStandardSpecialValues()
			.combined();

		// then — result is one of the standard specials (NaN, +/-infinity,
		// MIN_VALUE, MIN_NORMAL) or within the configured range
		boolean isStandardSpecial = Float.isNaN(actual)
			|| Float.isInfinite(actual)
			|| Float.compare(actual, Float.MIN_VALUE) == 0
			|| Float.compare(actual, Float.MIN_NORMAL) == 0;
		then(isStandardSpecial || (actual >= 1.0f && actual <= 10.0f)).isTrue();
	}

	@Test
	void lastMethodWinsPositiveOverNegative() {
		// when - negative().positive() => positive()
		Float actual = CombinableArbitrary.floats().negative().positive().combined();

		// then
		then(actual).isPositive();
	}

	@Test
	void lastMethodWinsRangeOverPositive() {
		// when - positive().withRange(-10, -1) => withRange(-10, -1)
		Float actual = CombinableArbitrary.floats().positive().withRange(-10.0f, -1.0f).combined();

		// then
		then(actual).isBetween(-10.0f, -1.0f);
	}

	@Test
	void fixed() {
		// when
		boolean actual = CombinableArbitrary.floats().fixed();

		// then
		then(actual).isFalse();
	}
}
