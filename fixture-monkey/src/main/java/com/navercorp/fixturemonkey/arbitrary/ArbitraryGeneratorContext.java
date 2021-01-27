package com.navercorp.fixturemonkey.arbitrary;

import javax.annotation.Nullable;

public interface ArbitraryGeneratorContext {
	@Nullable
	<T> ArbitraryGenerator<T> get(Class<T> clazz);
}
