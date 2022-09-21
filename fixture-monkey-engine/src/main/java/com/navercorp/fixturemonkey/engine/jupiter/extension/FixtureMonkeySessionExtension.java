package com.navercorp.fixturemonkey.engine.jupiter.extension;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jqwik.api.sessions.JqwikSession;

public final class FixtureMonkeySessionExtension implements TestInstancePostProcessor, BeforeEachCallback, BeforeAllCallback, AfterEachCallback, AfterAllCallback {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
		// testInstance.getClass()
		// 	.getMethod("setLogger", Logger.class)
		// 	.invoke(testInstance, logger);
	}

	@Override
	public void beforeEach(ExtensionContext context) {
	}

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
