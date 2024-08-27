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

import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.fixturemonkey.api.random.Randoms;
import com.navercorp.fixturemonkey.junit.jupiter.annotation.Seed;

public final class FixtureMonkeySeedExtension implements AfterTestExecutionCallback {
	private static final ThreadLocal<Long> SEED_HOLDER = new ThreadLocal<>();

	private static final Logger LOGGER = LoggerFactory.getLogger(FixtureMonkeySeedExtension.class);
	/**
	 * Logs the seed used for the test if the test fails.
	 * This method is called after a test method has executed.
	 * If the test failed, it logs the seed used for the test.
	 **/
	@Override
	public void afterTestExecution(ExtensionContext context) throws Exception {
		Seed seed = context.getRequiredTestMethod().getAnnotation(Seed.class);
		setSeed(seed.value());
		if (context.getExecutionException().isPresent()) {
			logSeedIfTestFailed(context);
		}
	}

	/**
	 * Sets the seed for generating random numbers.
	 **/
	private void setSeed(long seed) {
		SEED_HOLDER.set(Randoms.create(String.valueOf(seed)).nextLong());
	}

	/**
	 * Logs the seed if the test failed.
	 * This method logs the seed value when a test method execution fails.
	 **/
	private void logSeedIfTestFailed(ExtensionContext context) {
		Method testMethod = context.getRequiredTestMethod();
		LOGGER.error(String.format("Test Method [%s] failed with seed: %d", testMethod.getName(), SEED_HOLDER.get()));
	}
}
