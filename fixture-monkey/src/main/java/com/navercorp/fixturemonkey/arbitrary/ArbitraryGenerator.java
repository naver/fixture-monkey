package com.navercorp.fixturemonkey.arbitrary;

import net.jqwik.api.Arbitrary;

public interface ArbitraryGenerator<T> {
	Arbitrary<T> generate(ArbitraryGeneratorContext context, ArbitraryBuilder<T> builder);
}
