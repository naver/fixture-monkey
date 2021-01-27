package com.navercorp.fixturemonkey.arbitrary;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

public class CompositeArbitraryGeneratorContext implements ArbitraryGeneratorContext {
	private final List<ArbitraryGeneratorContext> contexts;

	public CompositeArbitraryGeneratorContext(ArbitraryGeneratorContext... contexts) {
		this(Arrays.asList(contexts));
	}

	public CompositeArbitraryGeneratorContext(List<ArbitraryGeneratorContext> contexts) {
		this.contexts = contexts;
	}

	@Nullable
	@Override
	public <T> ArbitraryGenerator<T> get(Class<T> clazz) {
		return this.contexts.stream()
			.map(it -> it.get(clazz))
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
	}
}
