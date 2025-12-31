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

package com.navercorp.fixturemonkey.junit.jupiter.extension.support;

import org.junit.jupiter.api.extension.ParameterContext;

import com.navercorp.fixturemonkey.FixtureMonkey;

final class DefaultParameterContextAwareFixtureMonkey implements ParameterContextAwareFixtureMonkey {
	private final ParameterContext parameterContext;
	private final FixtureMonkey fixtureMonkey;

	public DefaultParameterContextAwareFixtureMonkey(
		ParameterContext parameterContext,
		FixtureMonkey fixtureMonkey
	) {
		this.parameterContext = parameterContext;
		this.fixtureMonkey = fixtureMonkey;
	}

	@SuppressWarnings("return")
	@Override
	public Object giveMe() {
		Class<?> type = parameterContext.getParameter().getType();

		return fixtureMonkey.giveMeOne(type);
	}
}
