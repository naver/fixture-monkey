package com.navercorp.fixturemonkey.tests.java;

import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.arbitrary.StringCombinableArbitrary;
import com.navercorp.fixturemonkey.api.jqwik.JqwikByteCombinableArbitrary;
import com.navercorp.fixturemonkey.api.jqwik.JqwikCharacterCombinableArbitrary;
import com.navercorp.fixturemonkey.api.jqwik.JqwikIntegerCombinableArbitrary;
import com.navercorp.fixturemonkey.api.jqwik.JqwikLongCombinableArbitrary;
import com.navercorp.fixturemonkey.api.jqwik.JqwikShortCombinableArbitrary;
import com.navercorp.fixturemonkey.api.jqwik.JqwikStringCombinableArbitrary;

class CombinableArbitraryTest {
	@Test
	void integerCombinableArbitraryIsJqwik() {
		CombinableArbitrary<Integer> actual = CombinableArbitrary.integers();

		then(actual).isInstanceOf(JqwikIntegerCombinableArbitrary.class);
	}

	@Test
	void integerCombinableArbitraryInjectNull() {
		Integer actual = CombinableArbitrary.integers().injectNull(1).combined();

		then(actual).isNull();
	}

	@Test
	void integerCombinableArbitraryFilter() {
		Integer actual = CombinableArbitrary.integers().filter(it -> it > 10000).combined();

		then(actual).isGreaterThan(10000);
	}

	@Test
	void integerCombinableArbitraryPositive() {
		Integer actual = CombinableArbitrary.integers().positive().combined();

		then(actual).isPositive();
	}

	@Test
	void integerCombinableArbitraryNegative() {
		Integer actual = CombinableArbitrary.integers().negative().combined();

		then(actual).isNegative();
	}

	@Test
	void integerCombinableArbitraryWithRange() {
		Integer actual = CombinableArbitrary.integers().withRange(10, 100).combined();

		then(actual).isBetween(10, 100);
	}

	@Test
	void integerCombinableArbitraryWithRangeAndFilter() {
		Integer actual = CombinableArbitrary.integers().withRange(10, 100).filter(it -> 75 <= it).combined();

		then(actual).isBetween(75, 100);
	}

	@Test
	void integerCombinableArbitraryEven() {
		Integer actual = CombinableArbitrary.integers().even().combined();

		then(actual).isEven();
	}

	@Test
	void integerCombinableArbitraryOdd() {
		Integer actual = CombinableArbitrary.integers().odd().combined();

		then(actual).isOdd();
	}

	@Test
	void integerCombinableArbitraryLastOperationWinsWithPositiveAndNegative() {
		Integer actual = CombinableArbitrary.integers().positive().negative().combined();

		then(actual).isNegative();
	}

	@Test
	void integerCombinableArbitraryLastOperationWinsWithEvenAndOdd() {
		Integer actual = CombinableArbitrary.integers().even().odd().combined();

		then(actual).isOdd();
	}

	@Test
	void integerCombinableArbitraryLastOperationWinsWithNegativeAndRange() {
		Integer actual = CombinableArbitrary.integers().negative().withRange(100, 1000).combined();

		then(actual).isBetween(100, 1000);
	}

	@Test
	void longCombinableArbitraryIsJqwik() {
		CombinableArbitrary<Long> actual = CombinableArbitrary.longs();

		then(actual).isInstanceOf(JqwikLongCombinableArbitrary.class);
	}

	@Test
	void longCombinableArbitraryInjectNull() {
		Long actual = CombinableArbitrary.longs().injectNull(1).combined();

		then(actual).isNull();
	}

	@Test
	void longCombinableArbitraryFilter() {
		Long actual = CombinableArbitrary.longs().filter(it -> it > 10000L).combined();

		then(actual).isGreaterThan(10000L);
	}

	@Test
	void longCombinableArbitraryPositive() {
		Long actual = CombinableArbitrary.longs().positive().combined();

		then(actual).isPositive();
	}

	@Test
	void longCombinableArbitraryNegative() {
		Long actual = CombinableArbitrary.longs().negative().combined();

		then(actual).isNegative();
	}

	@Test
	void longCombinableArbitraryWithRange() {
		Long actual = CombinableArbitrary.longs().withRange(10L, 100L).combined();

		then(actual).isBetween(10L, 100L);
	}

	@Test
	void longCombinableArbitraryWithRangeAndFilter() {
		Long actual = CombinableArbitrary.longs().withRange(10L, 100L).filter(it -> 75L <= it).combined();

		then(actual).isBetween(75L, 100L);
	}

	@Test
	void longCombinableArbitraryEven() {
		Long actual = CombinableArbitrary.longs().even().combined();

		then(actual % 2).isEqualTo(0);
	}

	@Test
	void longCombinableArbitraryOdd() {
		Long actual = CombinableArbitrary.longs().odd().combined();

		then(actual % 2 != 0).isTrue();
	}

	@Test
	void longCombinableArbitraryLastOperationWinsWithPositiveAndNegative() {
		Long actual = CombinableArbitrary.longs().positive().negative().combined();

		then(actual).isNegative();
	}

	@Test
	void longCombinableArbitraryLastOperationWinsWithEvenAndOdd() {
		Long actual = CombinableArbitrary.longs().even().odd().combined();

		then(actual % 2 != 0).isTrue();
	}

	@Test
	void longCombinableArbitraryLastOperationWinsWithNegativeAndRange() {
		Long actual = CombinableArbitrary.longs().negative().withRange(100L, 1000L).combined();

		then(actual).isBetween(100L, 1000L);
	}

	@Test
	void byteCombinableArbitraryIsJqwik() {
		CombinableArbitrary<Byte> actual = CombinableArbitrary.bytes();

		then(actual).isInstanceOf(JqwikByteCombinableArbitrary.class);
	}

	@Test
	void byteCombinableArbitraryInjectNull() {
		Byte actual = CombinableArbitrary.bytes().injectNull(1).combined();

		then(actual).isNull();
	}

	@Test
	void byteCombinableArbitraryFilter() {
		Byte actual = CombinableArbitrary.bytes().filter(it -> it > 50).combined();

		then(actual).isGreaterThan((byte)50);
	}

	@Test
	void byteCombinableArbitraryPositive() {
		Byte actual = CombinableArbitrary.bytes().positive().combined();

		then(actual).isPositive();
	}

	@Test
	void byteCombinableArbitraryNegative() {
		Byte actual = CombinableArbitrary.bytes().negative().combined();

		then(actual).isNegative();
	}

	@Test
	void byteCombinableArbitraryWithRange() {
		Byte actual = CombinableArbitrary.bytes().withRange((byte)10, (byte)100).combined();

		then(actual).isBetween((byte)10, (byte)100);
	}

	@Test
	void byteCombinableArbitraryWithRangeAndFilter() {
		Byte actual = CombinableArbitrary.bytes().withRange((byte)10, (byte)100).filter(it -> 75 <= it).combined();

		then(actual).isBetween((byte)75, (byte)100);
	}

	@Test
	void byteCombinableArbitraryEven() {
		Byte actual = CombinableArbitrary.bytes().even().combined();

		then(actual % 2).isEqualTo(0);
	}

	@Test
	void byteCombinableArbitraryOdd() {
		Byte actual = CombinableArbitrary.bytes().odd().combined();

		then(actual % 2 != 0).isTrue();
	}

	@Test
	void byteCombinableArbitraryAscii() {
		Byte actual = CombinableArbitrary.bytes().ascii().combined();

		then(actual).isBetween((byte)0, (byte)127);
	}

	@Test
	void byteCombinableArbitraryLastOperationWinsWithPositiveAndNegative() {
		// positive().negative() => negative()
		Byte actual = CombinableArbitrary.bytes().positive().negative().combined();

		then(actual).isNegative();
	}

	@Test
	void byteCombinableArbitraryLastOperationWinsWithEvenAndOdd() {
		// even().odd() => odd()
		Byte actual = CombinableArbitrary.bytes().even().odd().combined();

		then(actual % 2 != 0).isTrue();
	}

	@Test
	void byteCombinableArbitraryLastOperationWinsWithNegativeAndRange() {
		// negative().withRange() => withRange()
		Byte actual = CombinableArbitrary.bytes().negative().withRange((byte)100, (byte)127).combined();

		then(actual).isBetween((byte)100, (byte)127);
	}

	@Test
	void byteCombinableArbitraryLastOperationWinsWithAsciiAndPositive() {
		// ascii().positive() => positive()
		Byte actual = CombinableArbitrary.bytes().ascii().positive().combined();

		then(actual).isPositive();
	}

	@Test
	void byteCombinableArbitraryLastOperationWinsWithPositiveAndAscii() {
		// positive().ascii() => ascii()
		Byte actual = CombinableArbitrary.bytes().positive().ascii().combined();

		then(actual).isBetween((byte)0, (byte)127);
	}

	@Test
	void byteCombinableArbitraryLastOperationWinsWithAsciiAndEven() {
		// ascii().even() => even()
		Byte actual = CombinableArbitrary.bytes().ascii().even().combined();

		then(actual % 2).isEqualTo(0);
	}

	@Test
	void byteCombinableArbitraryLastOperationWinsWithEvenAndAscii() {
		// even().ascii() => ascii()
		Byte actual = CombinableArbitrary.bytes().even().ascii().combined();

		then(actual).isBetween((byte)0, (byte)127);
	}

	@Test
	void byteCombinableArbitraryLastOperationWinsWithAsciiAndNegative() {
		// ascii().negative() => negative()
		Byte actual = CombinableArbitrary.bytes().ascii().negative().combined();

		then(actual).isNegative();
	}

	// More comprehensive conflict API tests for Byte can be found in ByteCombinableArbitraryTest
	// which demonstrates extensive last-method-wins behavior patterns

	@Test
	void characterCombinableArbitraryIsJqwik() {
		CombinableArbitrary<Character> actual = CombinableArbitrary.chars();

		then(actual).isInstanceOf(JqwikCharacterCombinableArbitrary.class);
	}

	@Test
	void characterCombinableArbitraryInjectNull() {
		Character actual = CombinableArbitrary.chars().injectNull(1).combined();

		then(actual).isNull();
	}

	@Test
	void characterCombinableArbitraryFilter() {
		Character actual = CombinableArbitrary.chars().filter(it -> it > 'A').combined();

		then(actual).isGreaterThan('A');
	}

	@Test
	void characterCombinableArbitraryWithRange() {
		Character actual = CombinableArbitrary.chars().withRange('A', 'Z').combined();

		then(actual).isBetween('A', 'Z');
	}

	@Test
	void characterCombinableArbitraryAlpha() {
		Character actual = CombinableArbitrary.chars().alphabetic().combined();

		then(Character.isLetter(actual)).isTrue();
	}

	@Test
	void characterCombinableArbitraryNumeric() {
		Character actual = CombinableArbitrary.chars().numeric().combined();

		then(Character.isDigit(actual)).isTrue();
	}

	@Test
	void characterCombinableArbitraryAlphaNumeric() {
		Character actual = CombinableArbitrary.chars().alphaNumeric().combined();

		then(Character.isLetterOrDigit(actual)).isTrue();
	}

	@Test
	void characterCombinableArbitraryAscii() {
		Character actual = CombinableArbitrary.chars().ascii().combined();

		then((int)actual).isLessThanOrEqualTo(127);
	}

	@Test
	void characterCombinableArbitraryUppercase() {
		Character actual = CombinableArbitrary.chars().uppercase().combined();

		then(Character.isUpperCase(actual)).isTrue();
	}

	@Test
	void characterCombinableArbitraryLowercase() {
		Character actual = CombinableArbitrary.chars().lowercase().combined();

		then(Character.isLowerCase(actual)).isTrue();
	}

	@Test
	void characterCombinableArbitraryKorean() {
		Character actual = CombinableArbitrary.chars().korean().combined();

		then(actual).isBetween('가', '힣');
	}

	@Test
	void characterCombinableArbitraryWhitespace() {
		Character actual = CombinableArbitrary.chars().whitespace().combined();

		then(Character.isWhitespace(actual)).isTrue();
	}

	@Test
	void characterCombinableArbitraryLastOperationWinsWithAlphaAndNumeric() {
		Character actual = CombinableArbitrary.chars().alphabetic().numeric().combined();

		then(Character.isDigit(actual)).isTrue();
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

	@Test
	void shortCombinableArbitraryIsJqwik() {
		CombinableArbitrary<Short> actual = CombinableArbitrary.shorts();

		then(actual).isInstanceOf(JqwikShortCombinableArbitrary.class);
	}

	@Test
	void shortCombinableArbitraryInjectNull() {
		Short actual = CombinableArbitrary.shorts().injectNull(1).combined();

		then(actual).isNull();
	}

	@Test
	void shortCombinableArbitraryFilter() {
		Short actual = CombinableArbitrary.shorts().filter(it -> it > 100).combined();

		then(actual).isGreaterThan((short) 100);
	}

	@Test
	void shortCombinableArbitraryPositive() {
		Short actual = CombinableArbitrary.shorts().positive().combined();

		then(actual).isPositive();
	}

	@Test
	void shortCombinableArbitraryNegative() {
		Short actual = CombinableArbitrary.shorts().negative().combined();

		then(actual).isNegative();
	}

	@Test
	void shortCombinableArbitraryWithRange() {
		Short actual = CombinableArbitrary.shorts().withRange((short) 10, (short) 100).combined();

		then(actual).isBetween((short) 10, (short) 100);
	}

	@Test
	void shortCombinableArbitraryWithRangeAndFilter() {
		Short actual = CombinableArbitrary.shorts()
			.withRange((short) 10, (short) 100)
			.filter(it -> 75 <= it)
			.combined();

		then(actual).isBetween((short) 75, (short) 100);
	}

	@Test
	void shortCombinableArbitraryEven() {
		Short actual = CombinableArbitrary.shorts().even().combined();

		then(actual % 2).isEqualTo(0);
	}

	@Test
	void shortCombinableArbitraryOdd() {
		Short actual = CombinableArbitrary.shorts().odd().combined();

		then(actual % 2 != 0).isTrue();
	}

	@Test
	void shortCombinableArbitraryLastOperationWinsWithPositiveAndNegative() {
		Short actual = CombinableArbitrary.shorts().positive().negative().combined();

		then(actual).isNegative();
	}

	@Test
	void shortCombinableArbitraryLastOperationWinsWithEvenAndOdd() {
		Short actual = CombinableArbitrary.shorts().even().odd().combined();

		then(actual % 2 != 0).isTrue();
	}

	@Test
	void shortCombinableArbitraryLastOperationWinsWithNegativeAndRange() {
		Short actual = CombinableArbitrary.shorts().negative().withRange((short) 100, (short) 1000).combined();

		then(actual).isBetween((short) 100, (short) 1000);
	}

}
