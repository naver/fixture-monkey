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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.junit.jupiter.annotation.GiveMe;
import com.navercorp.fixturemonkey.junit.jupiter.extension.support.ParameterContextAwareFixtureMonkey;

public class FixtureMonkeyParameterExtension implements ParameterResolver {

	protected final FixtureMonkey fixtureMonkey;

	public FixtureMonkeyParameterExtension() {
		this.fixtureMonkey = FixtureMonkey.create();
	}

	public FixtureMonkeyParameterExtension(FixtureMonkey fixtureMonkey) {
		this.fixtureMonkey = fixtureMonkey;
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
		throws ParameterResolutionException {
		return parameterContext.isAnnotated(GiveMe.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
		throws ParameterResolutionException {
		return getParameterContextAwareFixtureMonkey(parameterContext).giveMe();
	}

	protected ParameterContextAwareFixtureMonkey getParameterContextAwareFixtureMonkey(
		ParameterContext parameterContext) {
		return ParameterContextAwareFixtureMonkey.of(parameterContext, fixtureMonkey);
	}
}
