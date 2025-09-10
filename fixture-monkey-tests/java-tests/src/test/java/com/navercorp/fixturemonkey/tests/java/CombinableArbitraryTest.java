package com.navercorp.fixturemonkey.tests.java;

import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.arbitrary.StringCombinableArbitrary;
import com.navercorp.fixturemonkey.api.jqwik.JqwikByteCombinableArbitrary;
import com.navercorp.fixturemonkey.api.jqwik.JqwikIntegerCombinableArbitrary;
import com.navercorp.fixturemonkey.api.jqwik.JqwikLongCombinableArbitrary;
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
	void longCombinableArbitraryIsJqwik() {
		CombinableArbitrary<Long> actual = CombinableArbitrary.longs();

		then(actual).isInstanceOf(JqwikLongCombinableArbitrary.class);
	}

	@RepeatedTest(TEST_COUNT)
	void longCombinableArbitraryInjectNull() {
		Long actual = CombinableArbitrary.longs().injectNull(1).combined();

		then(actual).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void longCombinableArbitraryFilter() {
		Long actual = CombinableArbitrary.longs().filter(it -> it > 10000L).combined();

		then(actual).isGreaterThan(10000L);
	}

	@RepeatedTest(TEST_COUNT)
	void longCombinableArbitraryPositive() {
		Long actual = CombinableArbitrary.longs().positive().combined();

		then(actual).isPositive();
	}

	@RepeatedTest(TEST_COUNT)
	void longCombinableArbitraryNegative() {
		Long actual = CombinableArbitrary.longs().negative().combined();

		then(actual).isNegative();
	}

	@RepeatedTest(TEST_COUNT)
	void longCombinableArbitraryWithRange() {
		Long actual = CombinableArbitrary.longs().withRange(10L, 100L).combined();

		then(actual).isBetween(10L, 100L);
	}

	@RepeatedTest(TEST_COUNT)
	void longCombinableArbitraryWithRangeAndFilter() {
		Long actual = CombinableArbitrary.longs().withRange(10L, 100L).filter(it -> 75L <= it).combined();

		then(actual).isBetween(75L, 100L);
	}

	@RepeatedTest(TEST_COUNT)
	void longCombinableArbitraryEven() {
		Long actual = CombinableArbitrary.longs().even().combined();

		then(actual % 2).isEqualTo(0);
	}

	@RepeatedTest(TEST_COUNT)
	void longCombinableArbitraryOdd() {
		Long actual = CombinableArbitrary.longs().odd().combined();

		then(actual % 2 != 0).isTrue();
	}

	@RepeatedTest(TEST_COUNT)
	void longCombinableArbitraryLastOperationWinsWithPositiveAndNegative() {
		Long actual = CombinableArbitrary.longs().positive().negative().combined();

		then(actual).isNegative();
	}

	@RepeatedTest(TEST_COUNT)
	void longCombinableArbitraryLastOperationWinsWithEvenAndOdd() {
		Long actual = CombinableArbitrary.longs().even().odd().combined();

		then(actual % 2 != 0).isTrue();
	}

	@RepeatedTest(TEST_COUNT)
	void longCombinableArbitraryLastOperationWinsWithNegativeAndRange() {
		Long actual = CombinableArbitrary.longs().negative().withRange(100L, 1000L).combined();

		then(actual).isBetween(100L, 1000L);
	}

	@Test
	void byteCombinableArbitraryIsJqwik() {
		CombinableArbitrary<Byte> actual = CombinableArbitrary.bytes();

		then(actual).isInstanceOf(JqwikByteCombinableArbitrary.class);
	}

	@RepeatedTest(TEST_COUNT)
	void byteCombinableArbitraryInjectNull() {
		Byte actual = CombinableArbitrary.bytes().injectNull(1).combined();

		then(actual).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void byteCombinableArbitraryFilter() {
		Byte actual = CombinableArbitrary.bytes().filter(it -> it > 50).combined();

		then(actual).isGreaterThan((byte)50);
	}

	@RepeatedTest(TEST_COUNT)
	void byteCombinableArbitraryPositive() {
		Byte actual = CombinableArbitrary.bytes().positive().combined();

		then(actual).isPositive();
	}

	@RepeatedTest(TEST_COUNT)
	void byteCombinableArbitraryNegative() {
		Byte actual = CombinableArbitrary.bytes().negative().combined();

		then(actual).isNegative();
	}

	@RepeatedTest(TEST_COUNT)
	void byteCombinableArbitraryWithRange() {
		Byte actual = CombinableArbitrary.bytes().withRange((byte)10, (byte)100).combined();

		then(actual).isBetween((byte)10, (byte)100);
	}

	@RepeatedTest(TEST_COUNT)
	void byteCombinableArbitraryWithRangeAndFilter() {
		Byte actual = CombinableArbitrary.bytes().withRange((byte)10, (byte)100).filter(it -> 75 <= it).combined();

		then(actual).isBetween((byte)75, (byte)100);
	}

	@RepeatedTest(TEST_COUNT)
	void byteCombinableArbitraryEven() {
		Byte actual = CombinableArbitrary.bytes().even().combined();

		then(actual % 2).isEqualTo(0);
	}

	@RepeatedTest(TEST_COUNT)
	void byteCombinableArbitraryOdd() {
		Byte actual = CombinableArbitrary.bytes().odd().combined();

		then(actual % 2 != 0).isTrue();
	}

	@RepeatedTest(TEST_COUNT)
	void byteCombinableArbitraryAscii() {
		Byte actual = CombinableArbitrary.bytes().ascii().combined();

		then(actual).isBetween((byte)0, (byte)127);
	}

	@RepeatedTest(TEST_COUNT)
	void byteCombinableArbitraryLastOperationWinsWithPositiveAndNegative() {
		// positive().negative() => negative()
		Byte actual = CombinableArbitrary.bytes().positive().negative().combined();

		then(actual).isNegative();
	}

	@RepeatedTest(TEST_COUNT)
	void byteCombinableArbitraryLastOperationWinsWithEvenAndOdd() {
		// even().odd() => odd()
		Byte actual = CombinableArbitrary.bytes().even().odd().combined();

		then(actual % 2 != 0).isTrue();
	}

	@RepeatedTest(TEST_COUNT)
	void byteCombinableArbitraryLastOperationWinsWithNegativeAndRange() {
		// negative().withRange() => withRange()
		Byte actual = CombinableArbitrary.bytes().negative().withRange((byte)100, (byte)127).combined();

		then(actual).isBetween((byte)100, (byte)127);
	}

	@RepeatedTest(TEST_COUNT)
	void byteCombinableArbitraryLastOperationWinsWithAsciiAndPositive() {
		// ascii().positive() => positive()
		Byte actual = CombinableArbitrary.bytes().ascii().positive().combined();

		then(actual).isPositive();
	}

	@RepeatedTest(TEST_COUNT)
	void byteCombinableArbitraryLastOperationWinsWithPositiveAndAscii() {
		// positive().ascii() => ascii()
		Byte actual = CombinableArbitrary.bytes().positive().ascii().combined();

		then(actual).isBetween((byte)0, (byte)127);
	}

	@RepeatedTest(TEST_COUNT)
	void byteCombinableArbitraryLastOperationWinsWithAsciiAndEven() {
		// ascii().even() => even()
		Byte actual = CombinableArbitrary.bytes().ascii().even().combined();

		then(actual % 2).isEqualTo(0);
	}

	@RepeatedTest(TEST_COUNT)
	void byteCombinableArbitraryLastOperationWinsWithEvenAndAscii() {
		// even().ascii() => ascii()
		Byte actual = CombinableArbitrary.bytes().even().ascii().combined();

		then(actual).isBetween((byte)0, (byte)127);
	}

	@RepeatedTest(TEST_COUNT)
	void byteCombinableArbitraryLastOperationWinsWithAsciiAndNegative() {
		// ascii().negative() => negative()
		Byte actual = CombinableArbitrary.bytes().ascii().negative().combined();

		then(actual).isNegative();
	}

	// More comprehensive conflict API tests for Byte can be found in ByteCombinableArbitraryTest
	// which demonstrates extensive last-method-wins behavior patterns

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

		then(actual).satisfiesAnyOf(
			it -> then(it).isAlphabetic(),
			it -> then(it).isNullOrEmpty()
		);
	}

	@Test
	void stringCombinableArbitraryLatterWins() {
		String actual = CombinableArbitrary.strings().korean().alphabetic().combined();

		then(actual).satisfiesAnyOf(
			it -> then(it).isAlphabetic(),
			it -> then(it).isNullOrEmpty()
		);
	}
}
