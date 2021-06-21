package com.navercorp.fixturemonkey.arbitrary;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.ArbitraryBuilder;

public interface PreArbitraryManipulator<T> extends ArbitraryExpressionManipulator {
	Arbitrary<T> apply(Arbitrary<?> from);

	void accept(ArbitraryBuilder<T> arbitraryBuilder);

	PreArbitraryManipulator<T> copy();
}
