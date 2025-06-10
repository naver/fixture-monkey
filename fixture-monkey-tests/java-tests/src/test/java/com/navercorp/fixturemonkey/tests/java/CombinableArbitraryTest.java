package com.navercorp.fixturemonkey.tests.java;

import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.arbitrary.StringCombinableArbitrary;
import com.navercorp.fixturemonkey.api.jqwik.JqwikIntegerCombinableArbitrary;
import com.navercorp.fixturemonkey.api.jqwik.JqwikStringCombinableArbitrary;

class CombinableArbitraryTest {
	@Test
	void integerCombinableArbitraryIsJqwik() {
		CombinableArbitrary<Integer> actual = CombinableArbitrary.integers();

		then(actual).isInstanceOf(JqwikIntegerCombinableArbitrary.class);
	}

	@RepeatedTest(TEST_COUNT)
	void integerCombinableArbitraryInjectNull() {
		Integer actual = CombinableArbitrary.integers().injectNull(1).combined();

		then(actual).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void integerCombinableArbitraryFilter() {
		Integer actual = CombinableArbitrary.integers().filter(it -> it > 10000).combined();

		then(actual).isGreaterThan(10000);
	}

	@RepeatedTest(TEST_COUNT)
	void integerCombinableArbitraryPositive() {
		Integer actual = CombinableArbitrary.integers().positive().combined();

		then(actual).isPositive();
	}

	@RepeatedTest(TEST_COUNT)
	void integerCombinableArbitraryNegative() {
		Integer actual = CombinableArbitrary.integers().negative().combined();

		then(actual).isNegative();
	}

	@RepeatedTest(TEST_COUNT)
	void integerCombinableArbitraryWithRange() {
		Integer actual = CombinableArbitrary.integers().withRange(10, 100).combined();

		then(actual).isBetween(10, 100);
	}

	@RepeatedTest(TEST_COUNT)
	void integerCombinableArbitraryWithRangeAndFilter() {
		Integer actual = CombinableArbitrary.integers().withRange(10, 100).filter(it -> 75 <= it).combined();

		then(actual).isBetween(75, 100);
	}

	@RepeatedTest(TEST_COUNT)
	void integerCombinableArbitraryEven() {
		Integer actual = CombinableArbitrary.integers().even().combined();

		then(actual).isEven();
	}

	@RepeatedTest(TEST_COUNT)
	void integerCombinableArbitraryOdd() {
		Integer actual = CombinableArbitrary.integers().odd().combined();

		then(actual).isOdd();
	}

	@RepeatedTest(TEST_COUNT)
	void integerCombinableArbitraryLastOperationWinsWithPositiveAndNegative() {
		Integer actual = CombinableArbitrary.integers().positive().negative().combined();

		then(actual).isNegative();
	}

	@RepeatedTest(TEST_COUNT)
	void integerCombinableArbitraryLastOperationWinsWithEvenAndOdd() {
		Integer actual = CombinableArbitrary.integers().even().odd().combined();

		then(actual).isOdd();
	}

	@RepeatedTest(TEST_COUNT)
	void integerCombinableArbitraryLastOperationWinsWithNegativeAndRange() {
		Integer actual = CombinableArbitrary.integers().negative().withRange(100, 1000).combined();

		then(actual).isBetween(100, 1000);
	}

	@Test
	void stringCombinableArbitraryIsJqwik() {
		StringCombinableArbitrary actual = CombinableArbitrary.strings();

		then(actual).isInstanceOf(JqwikStringCombinableArbitrary.class);
	}

	@Test
	void stringCombinableArbitraryInjectNull() {
		String actual = CombinableArbitrary.strings().injectNull(1).combined();

		then(actual).isNull();
	}

	@Test
	void stringCombinableArbitraryFilter() {
		String actual = CombinableArbitrary.strings().filter(it -> it.length() > 5).combined();

		then(actual).hasSizeGreaterThan(5);
	}

	@Test
	void stringCombinableArbitraryFilterCharacter() {
		String actual = CombinableArbitrary.strings().filterCharacter(it -> 'a' <= it && it <= 'z').combined();

		then(actual.chars()).allMatch(it -> 'a' <= it && it <= 'z');
	}

	@Test
	void stringCombinableArbitraryFilterCharacterAndFilter() {
		String actual = CombinableArbitrary.strings()
			.filterCharacter(it -> 'a' <= it && it <= 'z')
			.filter(it -> it.length() < 5)
			.combined();

		then(actual.chars()).allMatch(it -> 'a' <= it && it <= 'z');
		then(actual).hasSizeLessThan(5);
	}

	@Test
	void stringCombinableArbitraryNumeric() {
		String actual = CombinableArbitrary.strings().numeric().combined();

		then(actual).matches("[0-9]*");
	}

	@Test
	void stringCombinableArbitraryMap() {
		String actual = CombinableArbitrary.strings().map(it -> "prefix" + it).combined();

		then(actual).startsWith("prefix");
	}

	@Test
	void stringCombinableArbitraryKorean() {
		String actual = CombinableArbitrary.strings().korean().combined();

		then(actual.chars()).allMatch(it -> '가' <= it && it <= '힣');
	}

	@Test
	void stringCombinableArbitraryAlphabet() {
		String actual = CombinableArbitrary.strings().alphabetic().combined();

		then(actual).isAlphabetic();
	}

	@Test
	void stringCombinableArbitraryLatterWins() {
		String actual = CombinableArbitrary.strings().korean().alphabetic().combined();

		then(actual).isAlphabetic();
	}
}
