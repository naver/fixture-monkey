package com.navercorp.fixturemonkey.arbitrary;

import java.util.HashMap;
import java.util.Map;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

public class PrimitiveWrappedArbitraryGeneratorContext implements ArbitraryGeneratorContext {
	private final Map<Class<?>, Arbitrary<?>> map = new HashMap<>();

	public PrimitiveWrappedArbitraryGeneratorContext() {
		map.put(Byte.class, Arbitraries.bytes());
		map.put(Short.class, Arbitraries.shorts());
		map.put(Integer.class, Arbitraries.integers());
		map.put(Long.class, Arbitraries.longs());
		map.put(Float.class, Arbitraries.floats());
		map.put(Double.class, Arbitraries.doubles());
		map.put(Character.class, Arbitraries.chars());
		map.put(Boolean.class, Arbitraries.of(true, false));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ArbitraryGenerator<T> get(Class<T> clazz) {
		Arbitrary<T> result = (Arbitrary<T>)map.get(clazz);

		return result == null
			? EmptyArbitraryGenerator.getInstance()
			: ((context, builder) -> result);
	}
}
