package com.navercorp.fixturemonkey.customizer;

import javax.annotation.Nullable;

public class DefaultArbitraryCustomizer<T> implements ArbitraryCustomizer<T> {
	@Nullable
	@Override
	public T customizeFixture(@Nullable T object) {
		return object;
	}
}
