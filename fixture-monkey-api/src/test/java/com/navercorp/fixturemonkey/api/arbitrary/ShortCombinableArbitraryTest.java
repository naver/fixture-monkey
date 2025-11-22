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

import org.junit.jupiter.api.Test;

class ShortCombinableArbitraryTest {
	@Test
	void combined() {
		// when
		Short actual = CombinableArbitrary.shorts().combined();

		// then
		then(actual).isInstanceOf(Short.class);
	}

	@Test
	void withRange() {
		// given
		short min = 100;
		short max = 200;

		// when
		Short actual = CombinableArbitrary.shorts().withRange(min, max).combined();

		// then
		then(actual).isBetween(min, max);
	}

	@Test
	void positive() {
		// when
		Short actual = CombinableArbitrary.shorts().positive().combined();

		// then
		then(actual).isPositive();
	}

	@Test
	void negative() {
		// when
		Short actual = CombinableArbitrary.shorts().negative().combined();

		// then
		then(actual).isNegative();
	}

	@Test
	void even() {
		// when
		Short actual = CombinableArbitrary.shorts().even().combined();

		// then
		then(actual % 2).isEqualTo(0);
	}

	@Test
	void odd() {
		// when
		Short actual = CombinableArbitrary.shorts().odd().combined();

		// then
		then(actual % 2).isNotEqualTo(0);
	}

	@Test
	void nonZero() {
		// when
		Short actual = CombinableArbitrary.shorts().nonZero().combined();

		// then
		then(actual).isNotEqualTo((short)0);
	}

	@Test
	void multipleOf() {
		// given
		short multiplier = 7;

		// when
		Short actual = CombinableArbitrary.shorts().multipleOf(multiplier).combined();

		// then
		then(actual % multiplier).isEqualTo(0);
	}

	@Test
	void multipleOfRespectsRange() {
		// when
		Short actual = CombinableArbitrary.shorts().withRange((short)10, (short)14).multipleOf((short)6).combined();

		// then
		then(actual).isBetween((short)10, (short)14);
		then(actual % 6).isEqualTo(0);
	}

	@Test
	void percentage() {
		// when
		Short actual = CombinableArbitrary.shorts().percentage().combined();

		// then
		then(actual).isBetween((short)0, (short)100);
	}

	@Test
	void score() {
		// when
		Short actual = CombinableArbitrary.shorts().score().combined();

		// then
		then(actual).isBetween((short)0, (short)100);
	}

	@Test
	void year() {
		// when
		Short actual = CombinableArbitrary.shorts().year().combined();

		// then
		then(actual).isBetween((short)1900, (short)2100);
	}

	@Test
	void month() {
		// when
		Short actual = CombinableArbitrary.shorts().month().combined();

		// then
		then(actual).isBetween((short)1, (short)12);
	}

	@Test
	void day() {
		// when
		Short actual = CombinableArbitrary.shorts().day().combined();

		// then
		then(actual).isBetween((short)1, (short)31);
	}

	@Test
	void hour() {
		// when
		Short actual = CombinableArbitrary.shorts().hour().combined();

		// then
		then(actual).isBetween((short)0, (short)23);
	}

	@Test
	void minute() {
		// when
		Short actual = CombinableArbitrary.shorts().minute().combined();

		// then
		then(actual).isBetween((short)0, (short)59);
	}

	@Test
	void lastMethodWinsWithPositiveAndRange() {
		// given
		short min = -100;
		short max = -50;

		// when - positive().withRange(min, max) => withRange(min, max)
		Short actual = CombinableArbitrary.shorts().positive().withRange(min, max).combined();

		// then
		then(actual).isBetween(min, max);
	}

	@Test
	void shortMapping() {
		// when
		String actual = CombinableArbitrary.shorts()
			.positive()
			.map(x -> "short:" + x)
			.combined();

		// then
		then(actual).startsWith("short:");
		String numberPart = actual.substring(6);
		short value = Short.parseShort(numberPart);
		then(value).isGreaterThan((short)0);
	}

	@Test
	void shortFiltering() {
		// when
		Short actual = CombinableArbitrary.shorts()
			.withRange((short)0, (short)1000)
			.filter(s -> s > 500)
			.combined();

		// then
		then(actual).isGreaterThan((short)500);
		then(actual).isLessThanOrEqualTo((short)1000);
	}

	@Test
	void shortFilteringWithMultipleConditions() {
		// when
		Short actual = CombinableArbitrary.shorts()
			.positive()
			.filter(s -> s % 100 == 0)
			.combined();

		// then
		then(actual).isGreaterThan((short)0);
		then(actual % 100).isEqualTo(0);
	}

	@Test
	void shortInjectNull() {
		// when
		Short actual = CombinableArbitrary.shorts()
			.positive()
			.injectNull(1.0)
			.combined();

		// then
		then(actual).isNull();
	}

	@Test
	void shortInjectNullWithZeroProbability() {
		// when
		Short actual = CombinableArbitrary.shorts()
			.positive()
			.injectNull(0.0)
			.combined();

		// then
		then(actual).isNotNull();
		then(actual).isGreaterThan((short)0);
	}

	@Test
	void filterMultiplesOfTenAndMapToStringWithPrefix() {
		// when
		String actual = CombinableArbitrary.shorts()
			.withRange((short)1, (short)100)
			.filter(s -> s % 10 == 0)
			.map(s -> "value:" + s)
			.combined();

		// then
		then(actual).startsWith("value:");
		String numberPart = actual.substring(6);
		short value = Short.parseShort(numberPart);
		then(value % 10).isEqualTo(0);
		then(value).isBetween((short)1, (short)100);
	}

	@Test
	void shortUniqueWithDifferentValues() {
		// when
		Short actual = CombinableArbitrary.shorts()
			.withRange((short)1, (short)1000)
			.unique()
			.combined();

		// then
		then(actual).isBetween((short)1, (short)1000);
	}

	@Test
	void fixed() {
		// when
		boolean actual = CombinableArbitrary.shorts().fixed();

		// then
		then(actual).isFalse();
	}

	@Test
	void lastMethodWinsNegativeOverPositive() {
		// when - positive().negative() => negative()
		Short actual = CombinableArbitrary.shorts().positive().negative().combined();

		// then
		then(actual).isNegative();
	}

	@Test
	void lastMethodWinsEvenOverRange() {
		// when -.withRange((min, max).even() => .even()
		Short actual = CombinableArbitrary.shorts().withRange((short)1, (short)1000).even().combined();

		// then
		then(actual % 2).isEqualTo(0);
	}

	@Test
	void shortFilterWithHundredMultiple() {
		// when
		Short actual = CombinableArbitrary.shorts().withRange((short)0, (short)1000)
				.filter(s -> s % 100 == 0).combined();

		// then
		then(actual % 100).isEqualTo(0);
	}

	@Test
	void lastMethodWinsEvenOverOdd() {
		// even().odd() => odd()
		Short actual = CombinableArbitrary.shorts().even().odd().combined();

		then(actual % 2).isNotEqualTo(0);
	}

	@Test
	void lastMethodWinsRangeOverNegative() {
		// negative().withRange() => withRange()
		Short actual = CombinableArbitrary.shorts()
			.negative()
			.withRange((short)100, (short)1000)
			.combined();

		then(actual).isBetween((short)100, (short)1000);
	}

	@Test
	void lastMethodWinsPositiveOverRange() {
		// withRange().positive() => positive()
		Short actual = CombinableArbitrary.shorts()
				.withRange((short)-1000, (short)-100)
				.positive()
				.combined();

		then(actual).isPositive();
	}

	@Test
	void nonZeroWithMultipleOf() {
		// when - nonZero().multipleOf((short)5) => multipleOf((short)5)
		Short actual = CombinableArbitrary.shorts().nonZero().multipleOf((short)5).combined();

		// then
		then(actual % 5).isEqualTo(0);
	}

	@Test
	void lastMethodWinsYearOverPercentage() {
		// when - percentage().year() => year()
		Short actual = CombinableArbitrary.shorts().percentage().year().combined();

		// then
		then(actual).isBetween((short)1900, (short)2100);
	}

	@Test
	void lastMethodWinsMultipleOfOverEven() {
		// when - even().multipleOf(3) => multipleOf(3)
		Short actual = CombinableArbitrary.shorts().even().multipleOf((short)3).combined();

		// then
		then(actual % 3).isEqualTo(0);
	}
}
