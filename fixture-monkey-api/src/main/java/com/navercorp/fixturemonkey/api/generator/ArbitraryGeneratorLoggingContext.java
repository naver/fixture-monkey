package com.navercorp.fixturemonkey.api.generator;


public final class ArbitraryGeneratorLoggingContext {
	private final boolean enableLoggingFail;

	public ArbitraryGeneratorLoggingContext(boolean enableLoggingFail) {
		this.enableLoggingFail = enableLoggingFail;
	}

	public boolean isEnableLoggingFail() {
		return enableLoggingFail;
	}
}
