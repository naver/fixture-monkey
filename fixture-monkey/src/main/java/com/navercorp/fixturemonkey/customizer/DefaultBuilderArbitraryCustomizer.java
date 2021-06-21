package com.navercorp.fixturemonkey.customizer;

import javax.annotation.Nullable;

public class DefaultBuilderArbitraryCustomizer<T, B> extends DefaultArbitraryCustomizer<T>
	implements BuilderArbitraryCustomizer<T, B> {

	@Nullable
	@Override
	public B customizeBuilder(B builder) {
		return builder;
	}
}
