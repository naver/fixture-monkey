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

package com.navercorp.fixturemonkey.tests.java17.adapter;

import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.RepeatedTest;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.adapter.JavaNodeTreeAdapterPlugin;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;

class InterfaceDefaultMethodAdapterTest {

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.plugin(new InterfacePlugin())
		.plugin(new JavaNodeTreeAdapterPlugin())
		.build();

	@RepeatedTest(TEST_COUNT)
	void defaultMethod() {
		String actual = SUT.giveMeOne(DefaultMethodInterface.class).defaultMethod();

		then(actual).isEqualTo("test");
	}

	public interface DefaultMethodInterface {
		default String defaultMethod() {
			return "test";
		}
	}
}
