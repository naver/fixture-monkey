package com.navercorp.fixturemonkey.arbitrary;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

public class PrimitiveArbitraryGeneratorContext implements ArbitraryGeneratorContext {
	private final Map<Class<?>, Arbitrary<?>> map = new HashMap<>();

	public PrimitiveArbitraryGeneratorContext() {
		map.put(byte.class, Arbitraries.bytes());
		map.put(short.class, Arbitraries.shorts());
		map.put(int.class, Arbitraries.integers());
		map.put(long.class, Arbitraries.longs());
		map.put(float.class, Arbitraries.floats());
		map.put(double.class, Arbitraries.doubles());
		map.put(char.class, Arbitraries.chars());
		map.put(boolean.class, Arbitraries.of(true, false));
	}

	@SuppressWarnings("unchecked")
	@Nullable
	@Override
	public <T> ArbitraryGenerator<T> get(Class<T> clazz) {
		return (context, builder) -> (Arbitrary<T>)map.get(clazz);
	}
}
