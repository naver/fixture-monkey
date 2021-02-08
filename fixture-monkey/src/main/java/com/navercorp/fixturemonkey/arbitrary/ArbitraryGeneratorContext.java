package com.navercorp.fixturemonkey.arbitrary;

public interface ArbitraryGeneratorContext {
	<T> ArbitraryGenerator<T> get(Class<T> clazz);
}
