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
		Byte actual = CombinableArbitrary.bytes().positive().combined();

		// then
		then(actual).isPositive();
	}

	@Test
	void negative() {
		// when
		Byte actual = CombinableArbitrary.bytes().negative().combined();

		// then
		then(actual).isNegative();
	}

	@Test
	void even() {
		// when
		Byte actual = CombinableArbitrary.bytes().even().combined();

		// then
		then(actual % 2).isEqualTo(0);
	}

	@Test
	void odd() {
		// when
		Byte actual = CombinableArbitrary.bytes().odd().combined();

		// then
		then(actual % 2).isNotEqualTo(0);
	}

	@Test
	void ascii() {
		// when
		Byte actual = CombinableArbitrary.bytes().ascii().combined();

		// then
		then(actual).isBetween((byte)0, (byte)127);
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
		Byte actual = CombinableArbitrary.bytes().positive().withRange((byte)-10, (byte)-1).combined();

		// then
		then(actual).isBetween((byte)-10, (byte)-1);
	}

	@Test
	void lastMethodWinsOddOverEven() {
		// when - even().odd() => odd()
		Byte actual = CombinableArbitrary.bytes().even().odd().combined();

		// then
		then(actual % 2).isNotEqualTo(0);
	}

	@Test
	void byteFilterWithMultipleOfFive() {
		// when - filter multiples of 5
		Byte actual = CombinableArbitrary.bytes()
				.withRange((byte)0, (byte)100)
				.filter(b -> b % 5 == 0).combined();

		// then
		then(actual % 5).isEqualTo(0);
	}

	@Test
	void asciiWithOdd() {
		// when - ascii().odd() => odd()
		Byte actual = CombinableArbitrary.bytes().ascii().odd().combined();

		// then
		then(actual % 2).isNotEqualTo(0);
	}

	@Test
	void asciiWithEven() {
		// when - ascii().even() => even()
		Byte actual = CombinableArbitrary.bytes().ascii().even().combined();

		// then
		then(actual % 2).isEqualTo(0);
	}

	@Test
	void asciiWithPositive() {
		// when
		Byte actual = CombinableArbitrary.bytes().ascii().positive().combined();

		// then
		then(actual).isBetween((byte)1, (byte)127);
	}

	@Test
	void asciiWithNegative() {
		// when - ascii().negative() => negative()
		Byte actual = CombinableArbitrary.bytes().ascii().negative().combined();

		// then
		then(actual).isNegative();
	}

	@Test
	void negativeWithAscii() {
		// when - negative().ascii() => ascii()
		Byte actual = CombinableArbitrary.bytes().negative().ascii().combined();

		// then
		then(actual).isBetween((byte)0, (byte)127);
	}

	@Test
	void oddWithEvenCombination() {
		// when - odd().even() => even()
		Byte actual = CombinableArbitrary.bytes().odd().even().combined();

		// then
		then(actual % 2).isEqualTo(0);
	}

	@Test
	void positiveWithNegativeCombination() {
		// when - positive().negative() => negative()
		Byte actual = CombinableArbitrary.bytes().positive().negative().combined();

		// then
		then(actual).isNegative();
	}

	@Test
	void complexApiCombination() {
		// when - ascii().positive().odd() => odd()
		Byte actual = CombinableArbitrary.bytes().ascii().positive().odd().combined();

		// then
		then(actual % 2).isNotEqualTo(0);
	}

	@Test
	void rangeOverridesOtherConstraints() {
		// when
		byte min = -50;
		byte max = -10;
		Byte actual = CombinableArbitrary.bytes().ascii().positive().odd().withRange(min, max).combined();

		// then
		then(actual).isBetween(min, max);
	}
}
