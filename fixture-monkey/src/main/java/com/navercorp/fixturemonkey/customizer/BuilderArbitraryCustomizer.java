package com.navercorp.fixturemonkey.customizer;

import javax.annotation.Nullable;

import com.navercorp.fixturemonkey.generator.BuilderFieldArbitraries;

@FunctionalInterface
public interface BuilderArbitraryCustomizer<T, B> extends ArbitraryCustomizer<T> {
	default void customizeBuilderFields(BuilderFieldArbitraries<B> builderFieldArbitraries) {
	}

	@Nullable
	B customizeBuilder(B builder);

	@Nullable
	@Override
	default T customizeFixture(@Nullable T object) {
		return object;
	}
}
