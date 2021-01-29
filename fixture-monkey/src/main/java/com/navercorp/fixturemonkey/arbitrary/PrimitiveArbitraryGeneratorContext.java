package com.navercorp.fixturemonkey.arbitrary;

import java.lang.invoke.MethodType;

import javax.annotation.Nullable;

public class PrimitiveArbitraryGeneratorContext implements ArbitraryGeneratorContext {
	private final ArbitraryGeneratorContext innerWrappedGeneratorContext;

	public PrimitiveArbitraryGeneratorContext(
		ArbitraryGeneratorContext innerWrappedGeneratorContext
	) {
		this.innerWrappedGeneratorContext = innerWrappedGeneratorContext;
	}

	@Nullable
	@Override
	public <T> ArbitraryGenerator<T> get(Class<T> clazz) {
		return this.innerWrappedGeneratorContext.get(wrap(clazz));
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> wrap(Class<T> clazz) {
		return (Class<T>) MethodType.methodType(clazz).wrap().returnType();
	}
}
