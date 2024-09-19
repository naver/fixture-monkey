package com.navercorp.fixturemonkey.api.arbitrary;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.arbitraries.StringArbitrary;
import net.jqwik.api.constraints.Size;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MonkeyStringArbitraryTest {
	StringArbitrary koreanStringArbitrary = new MonkeyStringArbitrary().korean();

	@Test
	void koreanShouldGenerateOnlyKoreanCharacters() {
		StringArbitrary arbitrary = koreanStringArbitrary.ofMinLength(1).ofMaxLength(10);

		String sample = arbitrary.sample();

		assertTrue(sample.chars().allMatch(ch -> ch >= '가' && ch <= '힣'));
	}

	@Property(tries = 100)
	void koreanShouldAlwaysGenerateStringsWithinKoreanCharacterRange(
		@ForAll @Size(min = 1, max = 50) String ignored
	) {
		String sample = koreanStringArbitrary.sample();

		assertTrue(sample.chars().allMatch(ch -> ch >= '가' && ch <= '힣'));
	}

	@Test
	void koreanShouldRespectMinAndMaxLength() {
		int minLength = 5;
		int maxLength = 10;
		StringArbitrary arbitrary = koreanStringArbitrary.ofMinLength(minLength).ofMaxLength(maxLength);

		String sample = arbitrary.sample();

		assertTrue(sample.length() >= minLength && sample.length() <= maxLength);
	}

	@Test
	void koreanShouldNotGenerateNonKoreanCharacters() {
		String sample = koreanStringArbitrary.sample();

		assertFalse(sample.chars().anyMatch(ch -> ch < '가' || ch > '힣'));
	}

	@RepeatedTest(100)
	void koreanShouldGenerateDifferentStrings() {
		StringArbitrary arbitrary = koreanStringArbitrary.ofMinLength(5).ofMaxLength(10);

		String firstSample = arbitrary.sample();
		String secondSample = arbitrary.sample();

		assertNotEquals(firstSample, secondSample, "Generated strings should not all be identical");
	}
}
