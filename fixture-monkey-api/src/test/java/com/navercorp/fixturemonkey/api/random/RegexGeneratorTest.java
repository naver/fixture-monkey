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

import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class RegexGeneratorTest {
	private static final RegexGenerator SUT = new RegexGenerator();
	private static final int FLAG_CASE_INSENSITIVE = 2;

	@Test
	void regExpGenerationSuccess() {
		String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
		List<String> strings = SUT.generateAll(emailRegex, new int[] {}, null, null);

		Pattern pattern = Pattern.compile(emailRegex);
		then(strings).allMatch(it -> pattern.matcher(it).matches());
	}

	@Test
	void generateRegExpWithCaseInsensitiveFlag() {
		List<String> strings = SUT.generateAll("a", new int[] {FLAG_CASE_INSENSITIVE}, null, null);

		then("a").isIn(strings);
		then("A").isIn(strings);
	}

	@Test
	void generateRegExpWithMinMaxLength() {
		List<String> strings = SUT.generateAll("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", new int[] {}, 3, 7);

		then(strings).allMatch(it -> it.length() >= 3 && it.length() <= 7);
	}

	@Test
	void generateInvalidRegExpThrows() {
		thenThrownBy(
			() -> SUT.generateAll("^^a", new int[] {}, null, null)
		).isExactlyInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void generateIncalculableRegExpThrows() {
		thenThrownBy(
			() -> SUT.generateAll(
				"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
				new int[] {},
				20,
				null
			)
		).isExactlyInstanceOf(IllegalArgumentException.class);
	}
}
