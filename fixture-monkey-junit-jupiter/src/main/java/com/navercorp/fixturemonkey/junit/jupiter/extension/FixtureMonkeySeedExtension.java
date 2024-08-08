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

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

import com.navercorp.fixturemonkey.api.random.Randoms;
import com.navercorp.fixturemonkey.junit.jupiter.annotation.ShowMeLog;

public final class FixtureMonkeySeedExtension implements AfterTestExecutionCallback {
	private static final ThreadLocal<Long> SEED_HOLDER = new ThreadLocal<>();

	private static final Logger logger = LoggerFactory.getLogger(FixtureMonkeySeedExtension.class);

	/**
	 * Logs the seed used for the test if the test fails.
	 * If the @ShowMeLog annotation is present on the test method,
	 * it shows log of the current seed if the test fails.
	 **/
	@Override
	public void afterTestExecution(ExtensionContext context) throws Exception {
		ShowMeLog showMeLog = context.getRequiredTestMethod().getAnnotation(ShowMeLog.class);
		if (showMeLog != null) {
			SEED_HOLDER.set(Randoms.currentSeed());
			Long seed = SEED_HOLDER.get();
			if (context.getExecutionException().isPresent()) {
				logSeedIfTestFailed(context, seed);
			}
		}
	}

	/**
	 * Logs the seed if the test failed.
	 *	This method is called when a test method execution fails.
	 **/
	private void logSeedIfTestFailed(ExtensionContext context, long seed) {
		Method testMethod = context.getRequiredTestMethod();
		logger.error(String.format("Test Method [%s] failed with seed: %d", testMethod.getName(), seed));
	}
}
