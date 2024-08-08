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
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.lang.reflect.Method;

import com.navercorp.fixturemonkey.api.random.Randoms;
import com.navercorp.fixturemonkey.junit.jupiter.annotation.Seed;

public final class FixtureMonkeySeedExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
	private static final ThreadLocal<Long> SEED_HOLDER = new ThreadLocal<>();

	private static final Logger logger = LoggerFactory.getLogger(FixtureMonkeySeedExtension.class);


	/**
	 * Sets the seed for Fixture Monkey before the test execution.
	 * If a method is annotated with @Seed, it uses the specified seed.
	 * Otherwise, it generates a new random seed.
	 *
	 * @param context the current extension context
	 */
	@Override
	public void beforeTestExecution(ExtensionContext context) throws Exception {
		Seed seedAnnotation = context.getRequiredTestMethod().getAnnotation(Seed.class);
		if (seedAnnotation != null) {
			long seed = seedAnnotation.value();
			setSeed(seed);
		} else {
			long seed = Randoms.currentSeed();
			setSeed(seed);
		}
	}

	/**
	 * Store the seed in the SEED_HOLDER before test execution.
	 * @param seed the seed to be stored
	 */
	private void setSeed(long seed) {
		SEED_HOLDER.set(seed);
	}

	/**
	 * Logs the seed used for the test if the test fails.
	 *
	 * @param context the current extension context
	 * @throws Exception if any error occurs during logging
	 */
	@Override
	public void afterTestExecution(ExtensionContext context) throws Exception {
		Long seed = SEED_HOLDER.get();
		if (context.getExecutionException().isPresent()) {
			Method testMethod = context.getRequiredTestMethod();
			if (seed != null) {
				logger.error(() -> String.format("Test Method [%s] failed with seed: %d", testMethod.getName(), seed));
			} else {
				logger.error(() -> String.format("Test Method [%s] failed, but no seed was set.", testMethod.getName()));
			}
		}
	}
}
