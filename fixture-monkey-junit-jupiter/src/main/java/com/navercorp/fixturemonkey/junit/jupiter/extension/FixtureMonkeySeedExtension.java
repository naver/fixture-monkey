package com.navercorp.fixturemonkey.junit.jupiter.extension;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Method;
import java.util.logging.Logger;

public class FixtureMonkeySeedExtension implements AfterTestExecutionCallback {

	private static final Logger logger = Logger.getLogger(FixtureMonkeySeedExtension.class.getName());

	private static final ThreadLocal<Long> seedHolder = new ThreadLocal<>();

	/**
	 * Store the seed used in the Fixture Monkey builder before test execution and output that seed if the test fails
	 * @param seed the seed used in the Fixture Monkey builder
	 */
	public static void setSeed(long seed) {
		seedHolder.set(seed);
	}

	@Override
	public void afterTestExecution(ExtensionContext context) throws Exception {
		Long seed = seedHolder.get();
		if (context.getExecutionException().isPresent()) {
			Method testMethod = context.getRequiredTestMethod();
			logger.info(() -> String.format("Test Method [%s] failed with seed: %d", testMethod.getName(), seed));
		}
	}
}
