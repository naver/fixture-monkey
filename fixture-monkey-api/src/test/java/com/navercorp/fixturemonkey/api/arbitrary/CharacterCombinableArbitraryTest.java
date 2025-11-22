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

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.api.exception.FixedValueFilterMissException;

class CharacterCombinableArbitraryTest {
	@Test
	void combined() {
		// when
		Character actual = CombinableArbitrary.chars().combined();

		// then
		then(actual).isInstanceOf(Character.class);
	}

	@Test
	void withRange() {
		// given
		char min = 'A';
		char max = 'Z';

		// when
		Character actual = CombinableArbitrary.chars().withRange(min, max).combined();

		// then
		then(actual).isBetween(min, max);
	}

	@Test
	void alphabetic() {
		// when
		Character actual = CombinableArbitrary.chars().alphabetic().combined();

		// then
		then(Character.isLetter(actual)).isTrue();
	}

	@Test
	void numeric() {
		// when
		Character actual = CombinableArbitrary.chars().numeric().combined();

		// then
		then(actual).isBetween('0', '9');
	}

	@Test
	void alphaNumeric() {
		// when
		Character actual = CombinableArbitrary.chars().alphaNumeric().combined();

		// then
		then(Character.isLetterOrDigit(actual)).isTrue();
	}

	@Test
	void ascii() {
		// when
		Character actual = CombinableArbitrary.chars().ascii().combined();

		// then
		then(actual).isBetween('\u0020', '\u007E');
	}

	@Test
	void uppercase() {
		// when
		Character actual = CombinableArbitrary.chars().uppercase().combined();

		// then
		then(actual).isBetween('A', 'Z');
	}

	@Test
	void lowercase() {
		// when
		Character actual = CombinableArbitrary.chars().lowercase().combined();

		// then
		then(actual).isBetween('a', 'z');
	}

	@Test
	void korean() {
		// when
		Character actual = CombinableArbitrary.chars().korean().combined();

		// then
		then(actual).isBetween('\uAC00', '\uD7AF');
	}

	@Test
	void emoji() {
		// when
		Character actual = CombinableArbitrary.chars().emoji().combined();

		// then
		then(actual).isBetween('\uD83D', '\uD83F');
	}

	@Test
	void whitespace() {
		// when
		Character actual = CombinableArbitrary.chars().whitespace().combined();

		// then
		then(Character.isWhitespace(actual)).isTrue();
	}

	@Test
	void lastMethodWinsWithAlphabeticAndRange() {
		// given
		char min = '0';
		char max = '9';

		// when - withRange() should take precedence over alphabetic()
		Character actual = CombinableArbitrary.chars().alphabetic().withRange(min, max).combined();

		// then
		then(actual).isBetween(min, max);
	}

	@Test
	void characterUnique() {
		// when & then - applying unique() to fixed value should throw exception
		thenThrownBy(() -> CombinableArbitrary.chars()
			.filter(x -> x == 'A')  // always generates same value
			.unique()
			.combined())
			.isExactlyInstanceOf(FixedValueFilterMissException.class);
	}

	@Test
	void characterMapping() {
		// when
		String actual = CombinableArbitrary.chars()
			.uppercase()
			.map(x -> "char:" + x)
			.combined();

		// then
		then(actual).startsWith("char:");
		then(actual).hasSize(6); // "char:" + 1 character
		char lastChar = actual.charAt(5);
		then(lastChar).isBetween('A', 'Z');
	}

	@Test
	void characterFiltering() {
		// when
		Character actual = CombinableArbitrary.chars()
			.withRange('A', 'Z')
			.filter(c -> c > 'M')
			.combined();

		// then
		then(actual).isGreaterThan('M');
		then(actual).isLessThanOrEqualTo('Z');
	}

	@Test
	void characterFilteringWithMultipleConditions() {
		// when
		Character actual = CombinableArbitrary.chars()
			.alphabetic()
			.filter(Character::isUpperCase)  // uppercase only
			.combined();

		// then
		then(Character.isUpperCase(actual)).isTrue();
		then(Character.isLetter(actual)).isTrue();
	}

	@Test
	void characterInjectNull() {
		// when
		Character actual = CombinableArbitrary.chars()
			.alphabetic()
			.injectNull(1.0)  // 100% null
			.combined();

		// then
		then(actual).isNull();
	}

	@Test
	void characterInjectNullWithZeroProbability() {
		// when
		Character actual = CombinableArbitrary.chars()
			.alphabetic()
			.injectNull(0.0)  // 0% null
			.combined();

		// then
		then(actual).isNotNull();
		then(Character.isLetter(actual)).isTrue();
	}

	@Test
	void characterCombinationWithMultipleOperations() {
		// when - combining multiple operations
		String actual = CombinableArbitrary.chars()
			.uppercase()
			.filter(c -> c >= 'A' && c <= 'F')  // A-F range
			.map(c -> "hex:" + c)               // string conversion
			.combined();

		// then
		then(actual).startsWith("hex:");
		char lastChar = actual.charAt(4);
		then(lastChar).isBetween('A', 'F');
	}

	@Test
	void characterUniqueWithDifferentValues() {
		// when - unique() works properly with different values
		Character actual = CombinableArbitrary.chars()
			.alphabetic()  // sufficiently wide range
			.unique()
			.combined();

		// then
		then(Character.isLetter(actual)).isTrue();
	}

	@Test
	void fixed() {
		// when
		boolean actual = CombinableArbitrary.chars().fixed();

		// then
		then(actual).isFalse();
	}

	@Test
	void lastMethodWinsKoreanOverAscii() {
		// when - korean() should take precedence over ascii()
		Character actual = CombinableArbitrary.chars().ascii().korean().combined();

		// then
		then(actual).isBetween('\uAC00', '\uD7AF');
	}

	@Test
	void lastMethodWinsRangeOverAlpha() {
		// when - withRange() should take precedence over alpha()
		Character actual = CombinableArbitrary.chars().alphabetic().withRange('0', '9').combined();

		// then
		then(actual).isBetween('0', '9');
	}

	@Test
	void characterFilterWithSpecificCondition() {
		// when - filtering uppercase only
		Character actual = CombinableArbitrary.chars().alphabetic().filter(Character::isUpperCase).combined();

		// then
		then(Character.isUpperCase(actual)).isTrue();
	}
}
