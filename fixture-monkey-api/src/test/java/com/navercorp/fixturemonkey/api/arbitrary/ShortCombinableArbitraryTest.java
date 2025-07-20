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
		boolean allPositive = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.shorts().positive().combined())
			.allMatch(s -> s > 0);

		// then
		then(allPositive).isTrue();
	}

	@Test
	void negative() {
		// when
		boolean allNegative = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.shorts().negative().combined())
			.allMatch(s -> s < 0);

		// then
		then(allNegative).isTrue();
	}

	@Test
	void even() {
		// when
		boolean allEven = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.shorts().even().combined())
			.allMatch(s -> s % 2 == 0);

		// then
		then(allEven).isTrue();
	}

	@Test
	void odd() {
		// when
		boolean allOdd = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.shorts().odd().combined())
			.allMatch(s -> s % 2 != 0);

		// then
		then(allOdd).isTrue();
	}

	@Test
	void nonZero() {
		// when
		boolean allNonZero = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.shorts().nonZero().combined())
			.allMatch(s -> s != 0);

		// then
		then(allNonZero).isTrue();
	}

	@Test
	void multipleOf() {
		// given
		short multiplier = 7;

		// when
		boolean allMultiplesOfSeven = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.shorts().multipleOf(multiplier).combined())
			.allMatch(s -> s % multiplier == 0);

		// then
		then(allMultiplesOfSeven).isTrue();
	}

	@Test
	void percentage() {
		// when
		boolean allPercentage = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.shorts().percentage().combined())
			.allMatch(s -> s >= 0 && s <= 100);

		// then
		then(allPercentage).isTrue();
	}

	@Test
	void score() {
		// when
		boolean allScore = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.shorts().score().combined())
			.allMatch(s -> s >= 0 && s <= 100);

		// then
		then(allScore).isTrue();
	}

	@Test
	void year() {
		// when
		boolean allYear = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.shorts().year().combined())
			.allMatch(s -> s >= 1900 && s <= 2100);

		// then
		then(allYear).isTrue();
	}

	@Test
	void month() {
		// when
		boolean allMonth = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.shorts().month().combined())
			.allMatch(s -> s >= 1 && s <= 12);

		// then
		then(allMonth).isTrue();
	}

	@Test
	void day() {
		// when
		boolean allDay = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.shorts().day().combined())
			.allMatch(s -> s >= 1 && s <= 31);

		// then
		then(allDay).isTrue();
	}

	@Test
	void hour() {
		// when
		boolean allHour = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.shorts().hour().combined())
			.allMatch(s -> s >= 0 && s <= 23);

		// then
		then(allHour).isTrue();
	}

	@Test
	void minute() {
		// when
		boolean allMinute = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.shorts().minute().combined())
			.allMatch(s -> s >= 0 && s <= 59);

		// then
		then(allMinute).isTrue();
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
	void addUniqueAtFixedShort() {
		// when & then - unique().combined()  => FixedValueFilterMissException
		thenThrownBy(() -> CombinableArbitrary.shorts()
			.filter(x -> x == (short)42)
			.unique()
			.combined())
			.isExactlyInstanceOf(FixedValueFilterMissException.class);
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
		boolean allNegative = IntStream.range(0, 30)
			.mapToObj(i -> CombinableArbitrary.shorts().positive().negative().combined())
			.allMatch(s -> s < 0);

		// then
		then(allNegative).isTrue();
	}

	@Test
	void lastMethodWinsEvenOverRange() {
		// when -.withRange((min, max).even() => .even()
		boolean allEven = IntStream.range(0, 30)
			.mapToObj(i -> CombinableArbitrary.shorts().withRange((short)1, (short)1000).even().combined())
			.allMatch(s -> s % 2 == 0);

		// then
		then(allEven).isTrue();
	}

	@Test
	void shortFilterWithHundredMultiple() {
		// when
		boolean allMultipleOfHundred = IntStream.range(0, 30)
			.mapToObj(i -> CombinableArbitrary.shorts().withRange((short)0, (short)1000)
				.filter(s -> s % 100 == 0).combined())
			.allMatch(s -> s % 100 == 0);

		// then
		then(allMultipleOfHundred).isTrue();
	}

	@Test
	void lastMethodWinsEvenOverOdd() {
		// even().odd() => odd()
		boolean allOdd = IntStream.range(0, 30)
			.mapToObj(i -> CombinableArbitrary.shorts().even().odd().combined())
			.allMatch(s -> s % 2 != 0);

		then(allOdd).isTrue();
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
		boolean allPositive = IntStream.range(0, 30)
			.mapToObj(i -> CombinableArbitrary.shorts()
				.withRange((short)-1000, (short)-100)
				.positive()
				.combined())
			.allMatch(s -> s > 0);

		then(allPositive).isTrue();
	}

	@Test
	void nonZeroWithMultipleOf() {
		// when
		boolean allNonZeroMultiplesOfFive = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.shorts().nonZero().multipleOf((short)5).combined())
			.allMatch(s -> s != 0 && s % 5 == 0);

		// then
		then(allNonZeroMultiplesOfFive).isTrue();
	}

	@Test
	void lastMethodWinsYearOverPercentage() {
		// when - percentage().year() => year()
		boolean allYear = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.shorts().percentage().year().combined())
			.allMatch(s -> s >= 1900 && s <= 2100);

		// then
		then(allYear).isTrue();
	}

	@Test
	void lastMethodWinsMultipleOfOverEven() {
		// when - even().multipleOf(3) => multipleOf(3)
		boolean allMultiplesOfThree = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.shorts().even().multipleOf((short)3).combined())
			.allMatch(s -> s % 3 == 0);

		// then
		then(allMultiplesOfThree).isTrue();
	}
}
