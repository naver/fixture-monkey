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
package com.navercorp.fixturemonkey.junit.jupiter.extension;

import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;

/**
 * Verifies that {@code FixtureMonkeyBuilder.seed(long)} produces the same canonical values
 * as the {@code @Seed} annotation in {@link FixtureMonkeySeedExtensionTest}.
 *
 * <p>This class intentionally does <strong>not</strong> register {@link FixtureMonkeySeedExtension},
 * so the seed configured on the builder is the only source of randomness control.
 */
class FixtureMonkeyBuilderSeedTest {

	@Test
	void builderSeedOneProducesCanonicalSeedOneFirstString() {
		// given — a FixtureMonkey configured with seed=1 via FixtureMonkeyBuilder
		FixtureMonkey sut = FixtureMonkey.builder().seed(1L).build();

		// when
		String actual = sut.giveMeOne(String.class);

		// then — must match the canonical first String of seed-1 verified by
		// FixtureMonkeySeedExtensionTest.seedReturnsSame / arbitraryBuilderSampleReturnsSame
		then(actual).isEqualTo("\u03b4\u914e\u2dc4\u8358\u8b0e\u941c\u3e90");
	}

	@Test
	void builderSeedOneSecondCallProducesCanonicalSeedOneSecondString() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder().seed(1L).build();

		// when
		sut.giveMeOne(String.class);
		String actual = sut.giveMeOne(String.class);

		// then — must match FixtureMonkeySeedExtensionTest.latterValue
		then(actual).isEqualTo(
			"\u5f93\u5eac\u6874\u0212\ua91e\u5d9a\u4da5\u2c3e\u431c\ubba2\u2c84\ud12c\ucc3c\u2c18"
			+ "\u04a3\u4084\u4365\u8741");
	}

	@Test
	void builderSeedOneIntegerMatchesAnnotationSeedOneInteger() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder().seed(1L).build();

		// when
		Integer actual = sut.giveMeOne(Integer.class);

		// then — must match FixtureMonkeySeedExtensionTest.integerReturnsSame
		then(actual).isEqualTo(3588295);
	}

	@Test
	void builderSeedOneLongMatchesAnnotationSeedOneLong() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder().seed(1L).build();

		// when
		Long actual = sut.giveMeOne(Long.class);

		// then — must match FixtureMonkeySeedExtensionTest.longReturnsSame
		then(actual).isEqualTo(-1740023L);
	}
}
