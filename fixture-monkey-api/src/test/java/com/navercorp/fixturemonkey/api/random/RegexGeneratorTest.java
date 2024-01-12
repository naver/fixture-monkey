package com.navercorp.fixturemonkey.api.random;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

public class RegexGeneratorTest {
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
