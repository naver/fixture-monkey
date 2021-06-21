package com.navercorp.fixturemonkey.customizer;

import javax.annotation.Nullable;

import com.navercorp.fixturemonkey.generator.FieldArbitraries;

@FunctionalInterface
public interface ArbitraryCustomizer<T> {
	default void customizeFields(Class<T> type, FieldArbitraries fieldArbitraries) {
	}

	@Nullable
	T customizeFixture(@Nullable T object);
}
