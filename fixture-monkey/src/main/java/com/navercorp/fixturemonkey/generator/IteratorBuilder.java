package com.navercorp.fixturemonkey.generator;

import java.util.List;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;

final class IteratorBuilder {
	public static IteratorBuilder INSTANCE = new IteratorBuilder();

	@SuppressWarnings({"rawtypes", "unchecked"})
	<T> Arbitrary<T> build(List<ArbitraryNode> nodes) {
		return (Arbitrary<T>)ListBuilder.INSTANCE.build(nodes).map(it -> ((List)it).iterator());
	}
}
