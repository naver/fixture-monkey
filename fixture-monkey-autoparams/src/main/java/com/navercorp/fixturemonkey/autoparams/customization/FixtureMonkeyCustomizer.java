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

package com.navercorp.fixturemonkey.autoparams.customization;

import autoparams.customization.CompositeCustomizer;

import com.navercorp.fixturemonkey.FixtureMonkey;

public final class FixtureMonkeyCustomizer extends CompositeCustomizer {
	private static FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.create();

	// call setUp @BeforeAll Lifecycle for customized FixtureMonkey object.
	public static void setUp(FixtureMonkey fixtureMonkey) {
		FIXTURE_MONKEY = fixtureMonkey;
	}

	public FixtureMonkeyCustomizer() {
		this(FIXTURE_MONKEY);
	}

	public FixtureMonkeyCustomizer(FixtureMonkey fixtureMonkey) {
		super(
			new FixtureMonkeyValueCustomizer(fixtureMonkey),
			new FixtureMonkeyArbitraryCustomizer(fixtureMonkey),
			new FixtureMonkeyArbitraryBuilderCustomizer(fixtureMonkey),
			new FixtureMonkeyItselfCustomizer(fixtureMonkey)
		);
	}
}
