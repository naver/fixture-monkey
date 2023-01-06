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

package com.navercorp.fixturemonkey.engine.jupiter.extension;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import net.jqwik.api.sessions.JqwikSession;

public final class FixtureMonkeySessionExtension implements BeforeAllCallback, AfterEachCallback, AfterAllCallback {

	@Override
	public void beforeAll(ExtensionContext context) {
		if (!JqwikSession.isActive()) {
			JqwikSession.start();
		}
	}

	@Override
	public void afterEach(ExtensionContext context) {
		if (JqwikSession.isActive()) {
			JqwikSession.finishTry();
		}
	}

	@Override
	public void afterAll(ExtensionContext context) {
		if (JqwikSession.isActive()) {
			JqwikSession.finish();
		}
	}
}
