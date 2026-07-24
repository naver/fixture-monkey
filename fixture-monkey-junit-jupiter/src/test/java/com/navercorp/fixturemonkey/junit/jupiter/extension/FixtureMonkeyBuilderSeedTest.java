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
		then(actual).isEqualTo("㼌✠섨ꝓ仛禦催ᘓ蓊類౺阹瞻");
	}

	@Test
	void builderSeedOneSecondCallProducesCanonicalSeedOneSecondString() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder().seed(1L).build();

		// when
		sut.giveMeOne(String.class);
		String actual = sut.giveMeOne(String.class);

		// then — must match FixtureMonkeySeedExtensionTest.latterValue
		then(actual).isEqualTo("婵얎⽒竻·俌欕悳잸횑ٻ킐結㗗蜵ꓣ몒둡塸聩");
	}

	@Test
	void builderSeedOneIntegerMatchesAnnotationSeedOneInteger() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder().seed(1L).build();

		// when
		Integer actual = sut.giveMeOne(Integer.class);

		// then — must match FixtureMonkeySeedExtensionTest.integerReturnsSame
		then(actual).isEqualTo(86904);
	}

	@Test
	void builderSeedOneLongMatchesAnnotationSeedOneLong() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder().seed(1L).build();

		// when
		Long actual = sut.giveMeOne(Long.class);

		// then — must match FixtureMonkeySeedExtensionTest.longReturnsSame
		then(actual).isEqualTo(-1555898L);
	}
}
