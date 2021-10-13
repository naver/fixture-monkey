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
