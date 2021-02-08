package com.navercorp.fixturemonkey.arbitrary;

import net.jqwik.api.Arbitrary;

public class EmptyArbitraryGenerator<T> implements ArbitraryGenerator<T> {
	private static final ArbitraryGenerator<?> instance = new EmptyArbitraryGenerator<>();

	@SuppressWarnings("unchecked")
	public static <U> ArbitraryGenerator<U> getInstance() {
		return (ArbitraryGenerator<U>)instance;
	}

	@Override
	public Arbitrary<T> generate(ArbitraryGeneratorContext context, ArbitraryBuilder<T> builder) {
		throw new NotSupportedTypeException(builder.getTargetClass());
	}
}
