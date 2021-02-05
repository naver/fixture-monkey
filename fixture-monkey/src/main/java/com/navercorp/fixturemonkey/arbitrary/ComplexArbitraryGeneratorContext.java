package com.navercorp.fixturemonkey.arbitrary;

import javax.annotation.Nullable;

public class ComplexArbitraryGeneratorContext implements ArbitraryGeneratorContext {
	@Nullable
	@Override
	public <T> ArbitraryGenerator<T> get(Class<T> clazz) {
		return new ComplexArbitraryGenerator<>();
	}
}
