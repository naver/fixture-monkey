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

package com.navercorp.fixturemonkey.tests.java.adapter;

import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.adapter.JavaNodeTreeAdapterPlugin;
import com.navercorp.fixturemonkey.api.random.Randoms;

class SeedInitializationAdapterTest {

	@Test
	void readsSeedFromFile() {
		// given
		long fileSeed = 123456789L;

		// when
		FixtureMonkey.builder()
			.useExperimental(options -> options.fileSeed())
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		// then
		then(Randoms.currentSeed()).isEqualTo(fileSeed);
	}

	@Test
	void builderSeedOverridesFileSeed() {
		// given
		long builderSeed = 987654321L;

		// when
		FixtureMonkey.builder()
			.seed(builderSeed)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		// then
		then(Randoms.currentSeed()).isEqualTo(builderSeed);
	}

}
