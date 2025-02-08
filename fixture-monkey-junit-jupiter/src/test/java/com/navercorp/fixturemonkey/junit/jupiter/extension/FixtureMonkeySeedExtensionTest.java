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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.junit.jupiter.annotation.Seed;

@ExtendWith(FixtureMonkeySeedExtension.class)
class FixtureMonkeySeedExtensionTest {
	private static final FixtureMonkey SUT = FixtureMonkey.create();

	@Seed(1)
	@RepeatedTest(100)
	void seedReturnsSame() {
		String expected = "✠섨ꝓ仛禦催ᘓ蓊類౺阹瞻塢飖獾ࠒ⒐፨";

		String actual = SUT.giveMeOne(String.class);

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void latterValue() {
		String expected = "欕悳잸";
		SUT.giveMeOne(String.class);

		String actual = SUT.giveMeOne(String.class);

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void containerReturnsSame() {
		List<String> expected = Collections.singletonList("仛禦催ᘓ蓊類౺阹瞻塢飖獾ࠒ⒐፨婵얎⽒竻·俌欕悳잸횑ٻ킐結");

		List<String> actual = SUT.giveMeOne(new TypeReference<List<String>>() {
		});

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void containerMattersOrder() {
		Set<String> expected = new HashSet<>(Collections.singletonList("仛禦催ᘓ蓊類౺阹瞻塢飖獾ࠒ⒐፨婵얎⽒竻·俌欕悳잸횑ٻ킐結"));

		Set<String> actual = SUT.giveMeOne(new TypeReference<Set<String>>() {
		});

		then(actual).isEqualTo(expected);
	}

	@Seed(1)
	@RepeatedTest(100)
	void multipleContainerReturnsDiff() {
		Set<String> firstSet = SUT.giveMeOne(new TypeReference<Set<String>>() {
		});

		List<String> secondList = SUT.giveMeOne(new TypeReference<List<String>>() {
		});

		then(firstSet).isNotEqualTo(secondList);
	}

	@Seed(1)
	@RepeatedTest(100)
	void multipleFixtureMonkeyInstancesReturnsAsOneInstance() {
		List<String> expected = Arrays.asList(
			"✠섨ꝓ仛禦催ᘓ蓊類౺阹瞻塢飖獾ࠒ⒐፨",
			"欕悳잸"
		);
		FixtureMonkey firstFixtureMonkey = FixtureMonkey.create();
		FixtureMonkey secondFixtureMonkey = FixtureMonkey.create();

		List<String> actual = Arrays.asList(
			firstFixtureMonkey.giveMeOne(String.class),
			secondFixtureMonkey.giveMeOne(String.class)
		);

		then(actual).isEqualTo(expected);
	}
}
