package com.navercorp.fixturemonkey.generator;

import java.util.List;
import java.util.stream.Stream;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.Combinators.BuilderCombinator;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;

@SuppressWarnings({"rawtypes", "unchecked"})
final class StreamBuilder {
	public static final StreamBuilder INSTANCE = new StreamBuilder();

	<T> Arbitrary<T> build(List<ArbitraryNode> nodes) {
		BuilderCombinator<Stream.Builder> streamBuilderCombinator = Combinators.withBuilder(Stream::builder);
		if (nodes.isEmpty()) {
			return (Arbitrary<T>)streamBuilderCombinator.build(Stream.Builder::build);
		}

		for (ArbitraryNode<?> node : nodes) {
			streamBuilderCombinator = streamBuilderCombinator.use(node.getArbitrary()).in(Stream.Builder::add);
		}

		return (Arbitrary<T>)streamBuilderCombinator.build(Stream.Builder::build);
	}
}
