package com.navercorp.fixturemonkey.generator;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.Combinators.BuilderCombinator;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;

final class SetBuilder {
	public static SetBuilder INSTANCE = new SetBuilder();

	@SuppressWarnings({"rawtypes", "unchecked"})
	<T> Arbitrary<T> build(List<ArbitraryNode> nodes) {
		BuilderCombinator<CollectionBuilderFrame> setBuilderCombinator = Combinators.withBuilder(SetBuilderFrame::new);
		if (nodes.isEmpty()) {
			return (Arbitrary<T>)setBuilderCombinator.build(CollectionBuilderFrame::build);
		}

		for (ArbitraryNode<?> node : nodes) {
			setBuilderCombinator = setBuilderCombinator.use(node.getArbitrary().unique())
				.in(CollectionBuilderFrame::add);
		}

		return (Arbitrary<T>)setBuilderCombinator.build(CollectionBuilderFrame::build);
	}

	private static class SetBuilderFrame extends CollectionBuilderFrame {
		public SetBuilderFrame() {
			super(new HashSet<>());
		}

		@Override
		Collection<Object> build() {
			return new HashSet<>(collection);
		}
	}
}
