package com.navercorp.fixturemonkey.generator;

import java.util.List;
import java.util.Optional;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;

@SuppressWarnings({"rawtypes", "unchecked"})
final class OptionalBuilder {
	public static final OptionalBuilder INSTANCE = new OptionalBuilder();

	<T> Arbitrary<T> build(List<ArbitraryNode> nodes) {
		if (nodes.isEmpty()) {
			return (Arbitrary<T>)Arbitraries.just(Optional.empty());
		}

		if (nodes.size() > 1) {
			throw new IllegalArgumentException("Optional can not have more than one value.");
		}

		return (Arbitrary<T>)nodes.get(0).getArbitrary().optional();
	}
}
