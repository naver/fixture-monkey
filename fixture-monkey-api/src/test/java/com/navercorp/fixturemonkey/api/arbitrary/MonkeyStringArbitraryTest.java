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

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.arbitraries.StringArbitrary;
import net.jqwik.api.constraints.Size;

class MonkeyStringArbitraryTest {
	StringArbitrary koreanStringArbitrary = new MonkeyStringArbitrary().korean();

	@Test
	void koreanShouldGenerateOnlyKoreanCharacters() {
		StringArbitrary arbitrary = koreanStringArbitrary.ofMinLength(1).ofMaxLength(10);

		String sample = arbitrary.sample();

		then(sample.chars().allMatch(ch -> ch >= '가' && ch <= '힣')).isTrue();
	}

	@Property(tries = 1)
	void koreanShouldAlwaysGenerateStringsWithinKoreanCharacterRange(
		@ForAll @Size(min = 1, max = 50) String ignored
	) {
		String sample = koreanStringArbitrary.sample();

		then(sample.chars().allMatch(ch -> ch >= '가' && ch <= '힣')).isTrue();
	}

	@Test
	void koreanShouldRespectMinAndMaxLength() {
		int minLength = 5;
		int maxLength = 10;
		StringArbitrary arbitrary = koreanStringArbitrary.ofMinLength(minLength).ofMaxLength(maxLength);

		String sample = arbitrary.sample();

		then(sample.length() >= minLength && sample.length() <= maxLength).isTrue();
	}

	@Test
	void koreanShouldNotGenerateNonKoreanCharacters() {
		String sample = koreanStringArbitrary.sample();

		then(sample.chars().anyMatch(ch -> ch < '가' || ch > '힣')).isFalse();
	}

	@RepeatedTest(100)
	void koreanShouldGenerateDifferentStrings() {
		StringArbitrary arbitrary = koreanStringArbitrary.ofMinLength(5).ofMaxLength(10);

		String firstSample = arbitrary.sample();
		String secondSample = arbitrary.sample();

		then(firstSample).isNotEqualTo(secondSample);
	}
}
