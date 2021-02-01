package com.navercorp.fixturemonkey.arbitrary;

public final class ArbitraryBuilder<T> {
	private final Class<T> targetClass;

	public ArbitraryBuilder(Class<T> targetClass) {
		this.targetClass = targetClass;
	}

	public Class<T> getTargetClass() {
		return this.targetClass;
	}
}
