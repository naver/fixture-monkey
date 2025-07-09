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

class ByteCombinableArbitraryTest {
	@Test
	void combined() {
		// when
		Byte actual = CombinableArbitrary.bytes().combined();

		// then
		then(actual).isInstanceOf(Byte.class);
	}

	@Test
	void withRange() {
		// given
		byte min = 10;
		byte max = 50;

		// when
		Byte actual = CombinableArbitrary.bytes().withRange(min, max).combined();

		// then
		then(actual).isBetween(min, max);
	}

	@Test
	void positive() {
		// when
		boolean allPositive = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bytes().positive().combined())
			.allMatch(b -> b > 0);

		// then
		then(allPositive).isTrue();
	}

	@Test
	void negative() {
		// when
		boolean allNegative = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bytes().negative().combined())
			.allMatch(b -> b < 0);

		// then
		then(allNegative).isTrue();
	}

	@Test
	void even() {
		// when
		boolean allEven = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bytes().even().combined())
			.allMatch(b -> b % 2 == 0);

		// then
		then(allEven).isTrue();
	}

	@Test
	void odd() {
		// when
		boolean allOdd = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bytes().odd().combined())
			.allMatch(b -> b % 2 != 0);

		// then
		then(allOdd).isTrue();
	}

	@Test
	void ascii() {
		// when
		boolean allAscii = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bytes().ascii().combined())
			.allMatch(b -> b >= 0 && b <= 127);

		// then
		then(allAscii).isTrue();
	}

	@Test
	void lastMethodWinsWithPositiveAndRange() {
		// given
		byte min = -50;
		byte max = -10;

		// when - positive().withRange() => withRange()
		Byte actual = CombinableArbitrary.bytes().positive().withRange(min, max).combined();

		// then
		then(actual).isBetween(min, max);
	}

	@Test
	void byteMapping() {
		// when
		String actual = CombinableArbitrary.bytes()
			.positive()
			.map(x -> "byte:" + x)
			.combined();

		// then
		then(actual).startsWith("byte:");
		String numberPart = actual.substring(5);
		byte value = Byte.parseByte(numberPart);
		then(value).isGreaterThan((byte)0);
	}

	@Test
	void byteFiltering() {
		// when
		Byte actual = CombinableArbitrary.bytes()
			.withRange((byte)0, (byte)100)
			.filter(b -> b > 50)
			.combined();

		// then
		then(actual).isGreaterThan((byte)50);
		then(actual).isLessThanOrEqualTo((byte)100);
	}

	@Test
	void byteFilteringWithMultipleConditions() {
		// when
		Byte actual = CombinableArbitrary.bytes()
			.positive()
			.filter(b -> b % 10 == 0)  // multiples of 10
			.combined();

		// then
		then(actual).isGreaterThan((byte)0);
		then(actual % 10).isEqualTo(0);
	}

	@Test
	void byteInjectNull() {
		// when
		Byte actual = CombinableArbitrary.bytes()
			.positive()
			.injectNull(1.0)  // 100% null
			.combined();

		// then
		then(actual).isNull();
	}

	@Test
	void byteInjectNullWithZeroProbability() {
		// when
		Byte actual = CombinableArbitrary.bytes()
			.positive()
			.injectNull(0.0)  // 0% null
			.combined();

		// then
		then(actual).isNotNull();
		then(actual).isGreaterThan((byte)0);
	}

	@Test
	void byteCombinationWithMultipleOperations() {
		// when - combine multiple operations
		String actual = CombinableArbitrary.bytes()
			.ascii()
			.filter(b -> b >= 65 && b <= 90)  // A-Z ASCII 범위
			.map(b -> "ascii:" + (char)b.byteValue())  // convert to char
			.combined();

		// then
		then(actual).startsWith("ascii:");
		char lastChar = actual.charAt(6);
		then(lastChar).isBetween('A', 'Z');
	}

	@Test
	void byteUniqueWithDifferentValues() {
		// when - unique() works with different values
		Byte actual = CombinableArbitrary.bytes()
			.ascii()  // wide range
			.unique()
			.combined();

		// then
		then(actual).isBetween((byte)0, (byte)127);
	}

	@Test
	void fixed() {
		// when
		boolean actual = CombinableArbitrary.bytes().fixed();

		// then
		then(actual).isFalse();
	}

	@Test
	void lastMethodWinsRangeOverPositive() {
		// when - positive().withRange() => withRange()
		boolean allInRange = IntStream.range(0, 30)
			.mapToObj(i -> CombinableArbitrary.bytes().positive().withRange((byte)-10, (byte)-1).combined())
			.allMatch(b -> b >= -10 && b <= -1);

		// then
		then(allInRange).isTrue();
	}

	@Test
	void lastMethodWinsOddOverEven() {
		// when - even().odd() => odd()
		boolean allOdd = IntStream.range(0, 30)
			.mapToObj(i -> CombinableArbitrary.bytes().even().odd().combined())
			.allMatch(b -> b % 2 != 0);

		// then
		then(allOdd).isTrue();
	}

	@Test
	void byteFilterWithMultipleOfFive() {
		// when - filter multiples of 5
		boolean allMultipleOfFive = IntStream.range(0, 30)
			.mapToObj(i -> CombinableArbitrary.bytes()
				.withRange((byte)0, (byte)100)
				.filter(b -> b % 5 == 0).combined())
			.allMatch(b -> b % 5 == 0);

		// then
		then(allMultipleOfFive).isTrue();
	}

	@Test
	void asciiWithOdd() {
		// when - ascii().odd() => odd()
		boolean allOdd = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bytes().ascii().odd().combined())
			.allMatch(b -> b % 2 != 0);

		// then
		then(allOdd).isTrue();
	}

	@Test
	void asciiWithEven() {
		// when - ascii().even() => even()
		boolean allEven = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bytes().ascii().even().combined())
			.allMatch(b -> b % 2 == 0);

		// then
		then(allEven).isTrue();
	}

	@Test
	void asciiWithPositive() {
		// when
		boolean allAsciiAndPositive = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bytes().ascii().positive().combined())
			.allMatch(b -> b >= 1 && b <= 127);

		// then
		then(allAsciiAndPositive).isTrue();
	}

	@Test
	void asciiWithNegative() {
		// when - ascii().negative() => negative()
		boolean allNegative = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bytes().ascii().negative().combined())
			.allMatch(b -> b < 0);

		// then
		then(allNegative).isTrue();
	}

	@Test
	void negativeWithAscii() {
		// when - negative().ascii() => ascii()
		boolean allAscii = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bytes().negative().ascii().combined())
			.allMatch(b -> b >= 0 && b <= 127);

		// then
		then(allAscii).isTrue();
	}

	@Test
	void oddWithEvenCombination() {
		// when - odd().even() => even()
		boolean allEven = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bytes().odd().even().combined())
			.allMatch(b -> b % 2 == 0);

		// then
		then(allEven).isTrue();
	}

	@Test
	void positiveWithNegativeCombination() {
		// when - positive().negative() => negative()
		boolean allNegative = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bytes().positive().negative().combined())
			.allMatch(b -> b < 0);

		// then
		then(allNegative).isTrue();
	}

	@Test
	void complexApiCombination() {
		// when - ascii().positive().odd() => odd()
		boolean allOdd = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bytes().ascii().positive().odd().combined())
			.allMatch(b -> b % 2 != 0);

		// then
		then(allOdd).isTrue();
	}

	@Test
	void rangeOverridesOtherConstraints() {
		// when
		byte min = -50;
		byte max = -10;
		boolean allInRange = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.bytes().ascii().positive().odd().withRange(min, max).combined())
			.allMatch(b -> b >= min && b <= max);

		// then
		then(allInRange).isTrue();
	}
}
