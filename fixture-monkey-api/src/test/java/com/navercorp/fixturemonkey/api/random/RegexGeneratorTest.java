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

package com.navercorp.fixturemonkey.api.random;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class RegexGeneratorTest {
	private static final RegexGenerator SUT = new RegexGenerator();
	private static final int FLAG_CASE_INSENSITIVE = 2;

	@Test
	void regExpGenerationSuccess() {
		String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
		String result = SUT.generate(emailRegex, new int[] {}, it -> true);

		Pattern pattern = Pattern.compile(emailRegex);
		then(pattern.matcher(result).matches()).isTrue();
	}

	@Test
	void generateRegExpWithCaseInsensitiveFlag() {
		String result = SUT.generate("a", new int[] {FLAG_CASE_INSENSITIVE}, it -> true);

		then(result).isIn("a", "A");
	}

	@Test
	void generateRegExpWithPredicate() {
		String result = SUT.generate(
			"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
			new int[] {},
			it -> it.length() >= 3 && it.length() <= 50
		);

		then(result.length()).isBetween(3, 50);
	}

	@Test
	void generateInvalidRegExpThrows() {
		thenThrownBy(
			() -> SUT.generate(
				"^^a",
				new int[] {},
				it -> true
			)
		).isExactlyInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void generateIncalculableRegExpThrows() {
		thenThrownBy(
			() -> SUT.generate(
				"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
				new int[] {},
				it -> it.length() <= 3
			)
		).isExactlyInstanceOf(IllegalArgumentException.class);
	}
}
