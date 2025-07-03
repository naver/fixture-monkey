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
		boolean allAlpha = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.chars().alphabetic().combined())
			.allMatch(Character::isLetter);

		// then
		then(allAlpha).isTrue();
	}

	@Test
	void numeric() {
		// when
		boolean allNumeric = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.chars().numeric().combined())
			.allMatch(c -> c >= '0' && c <= '9');

		// then
		then(allNumeric).isTrue();
	}

	@Test
	void alphaNumeric() {
		// when
		boolean allAlphaNumeric = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.chars().alphaNumeric().combined())
			.allMatch(Character::isLetterOrDigit);

		// then
		then(allAlphaNumeric).isTrue();
	}

	@Test
	void ascii() {
		// when
		boolean allAscii = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.chars().ascii().combined())
			.allMatch(c -> c >= '\u0020' && c <= '\u007E');

		// then
		then(allAscii).isTrue();
	}

	@Test
	void uppercase() {
		// when
		boolean allUppercase = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.chars().uppercase().combined())
			.allMatch(c -> c >= 'A' && c <= 'Z');

		// then
		then(allUppercase).isTrue();
	}

	@Test
	void lowercase() {
		// when
		boolean allLowercase = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.chars().lowercase().combined())
			.allMatch(c -> c >= 'a' && c <= 'z');

		// then
		then(allLowercase).isTrue();
	}

	@Test
	void korean() {
		// when
		boolean allKorean = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.chars().korean().combined())
			.allMatch(c -> c >= '\uAC00' && c <= '\uD7AF');

		// then
		then(allKorean).isTrue();
	}

	@Test
	void emoji() {
		// when
		boolean allEmoji = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.chars().emoji().combined())
			.allMatch(c -> c >= '\uD83D' && c <= '\uD83F');

		// then
		then(allEmoji).isTrue();
	}

	@Test
	void whitespace() {
		// when
		boolean allWhitespace = IntStream.range(0, 100)
			.mapToObj(i -> CombinableArbitrary.chars().whitespace().combined())
			.allMatch(c -> c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f');

		// then
		then(allWhitespace).isTrue();
	}

	@Test
	void lastMethodWinsWithAlphabeticAndRange() {
		// given
		char min = '0';
		char max = '9';

		// when - alphabetic()를 무시하고 withRange()가 우선되어야 함
		Character actual = CombinableArbitrary.chars().alphabetic().withRange(min, max).combined();

		// then
		then(actual).isBetween(min, max);
	}

	@Test
	void characterUnique() {
		// when & then - 고정값에 unique() 적용하면 예외 발생해야 함
		thenThrownBy(() -> CombinableArbitrary.chars()
			.filter(x -> x == 'A')  // 항상 같은 값만 생성
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
			.filter(Character::isUpperCase)  // 대문자만
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
		// when - 여러 연산을 조합
		String actual = CombinableArbitrary.chars()
			.uppercase()
			.filter(c -> c >= 'A' && c <= 'F')  // A-F 범위
			.map(c -> "hex:" + c)               // 문자열 변환
			.combined();

		// then
		then(actual).startsWith("hex:");
		char lastChar = actual.charAt(4);
		then(lastChar).isBetween('A', 'F');
	}

	@Test
	void characterUniqueWithDifferentValues() {
		// when - 서로 다른 값들에는 unique()가 정상 작동
		Character actual = CombinableArbitrary.chars()
			.alphabetic()  // 충분히 넓은 범위
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
		// when - ascii()를 무시하고 korean()이 우선되어야 함
		boolean allKorean = IntStream.range(0, 30)
			.mapToObj(i -> CombinableArbitrary.chars().ascii().korean().combined())
			.allMatch(c -> c >= '\uAC00' && c <= '\uD7AF');

		// then
		then(allKorean).isTrue();
	}

	@Test
	void lastMethodWinsRangeOverAlpha() {
		// when - alpha()를 무시하고 withRange()가 우선되어야 함
		boolean allInRange = IntStream.range(0, 30)
			.mapToObj(i -> CombinableArbitrary.chars().alphabetic().withRange('0', '9').combined())
			.allMatch(c -> c >= '0' && c <= '9');

		// then
		then(allInRange).isTrue();
	}

	@Test
	void characterFilterWithSpecificCondition() {
		// when - 대문자만 필터링
		boolean allUppercase = IntStream.range(0, 30)
			.mapToObj(i -> CombinableArbitrary.chars().alphabetic().filter(Character::isUpperCase).combined())
			.allMatch(Character::isUpperCase);

		// then
		then(allUppercase).isTrue();
	}
}
