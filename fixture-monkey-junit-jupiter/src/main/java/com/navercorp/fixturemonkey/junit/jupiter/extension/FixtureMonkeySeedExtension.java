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
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.fixturemonkey.api.random.Randoms;
import com.navercorp.fixturemonkey.api.random.SeedClaim;
import com.navercorp.fixturemonkey.junit.jupiter.annotation.Seed;

/**
 * This extension sets the seed for generating random numbers before a test method is executed.
 * It also logs the seed used for the test if the test fails.
 * It aims to make the test deterministic and reproducible.
 * <p>
 * If the test method has a {@link Seed} annotation, it uses the value of the annotation as the seed.
 * If the test method does not have a {@link Seed} annotation, it uses the hash code of the test method as the seed.
 * <p>
 * The {@link Seed} annotation has a higher priority than the option {@code seed} in FixtureMonkey.
 */
public final class FixtureMonkeySeedExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
	private static final Logger LOGGER = LoggerFactory.getLogger(FixtureMonkeySeedExtension.class);
	private static final Namespace NAMESPACE = Namespace.create(FixtureMonkeySeedExtension.class);
	private static final String CLAIM_KEY = "seedClaim";

	@Override
	public void beforeTestExecution(ExtensionContext context) {
		Seed seed = context.getRequiredTestMethod().getAnnotation(Seed.class);
		if (seed != null) {
			establishClaim(context, seed.value());
			return;
		}

		Method testMethod = context.getRequiredTestMethod();
		establishClaim(context, testMethod.hashCode());
	}

	/**
	 * Logs the seed used for the test if the test fails. The seed claim
	 * established in {@code beforeTestExecution} is released automatically
	 * by JUnit when this extension's {@link Store} is closed.
	 **/
	@Override
	public void afterTestExecution(ExtensionContext context) {
		if (context.getExecutionException().isPresent()) {
			logSeedIfTestFailed(context);
		}
	}

	/**
	 * Applies the seed and stores the resulting claim in the JUnit
	 * {@link Store} so it is released when the test scope ends, even
	 * if the test or other extensions throw.
	 **/
	private void establishClaim(ExtensionContext context, long seed) {
		SeedClaim claim = SeedClaim.establish(seed);
		context.getStore(NAMESPACE).put(CLAIM_KEY, (CloseableResource)claim::close);
	}

	/**
	 * Logs the seed if the test failed.
	 * This method logs the seed value when a test method execution fails.
	 **/
	private void logSeedIfTestFailed(ExtensionContext context) {
		Class<?> testClass = context.getRequiredTestClass();
		Method testMethod = context.getRequiredTestMethod();
		LOGGER.error(
			"Test Method [{}#{}] failed with seed: {}",
			testClass.getSimpleName(),
			testMethod.getName(),
			Randoms.currentSeed()
		);
	}
}
