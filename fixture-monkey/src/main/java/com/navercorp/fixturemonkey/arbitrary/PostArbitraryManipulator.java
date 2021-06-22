package com.navercorp.fixturemonkey.arbitrary;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.ArbitraryBuilder;

public interface PostArbitraryManipulator<T> extends ArbitraryExpressionManipulator {
	Arbitrary<T> apply(Arbitrary<?> from);

	boolean isMappableTo(ArbitraryNode<T> node);

	default void accept(ArbitraryBuilder<T> arbitraryBuilder) {
		arbitraryBuilder.addPostArbitraryManipulator(this);
	}

	PostArbitraryManipulator<T> copy();
}
