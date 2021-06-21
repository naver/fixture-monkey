package com.navercorp.fixturemonkey.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.Combinators.BuilderCombinator;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;

final class ListBuilder {
	public static ListBuilder INSTANCE = new ListBuilder();

	@SuppressWarnings({"rawtypes", "unchecked"})
	<T> Arbitrary<T> build(List<ArbitraryNode> nodes) {
		BuilderCombinator<CollectionBuilderFrame> listBuilderCombinator =
			Combinators.withBuilder(ListBuilderFrame::new);

		if (nodes.isEmpty()) {
			return (Arbitrary<T>)listBuilderCombinator.build(CollectionBuilderFrame::build);
		}

		for (ArbitraryNode<?> node : nodes) {
			listBuilderCombinator = listBuilderCombinator.use(node.getArbitrary()).in(
				CollectionBuilderFrame::add);
		}

		return (Arbitrary<T>)listBuilderCombinator.build(CollectionBuilderFrame::build);
	}

	private static class ListBuilderFrame extends CollectionBuilderFrame {
		public ListBuilderFrame() {
			super(new ArrayList<>());
		}

		@Override
		Collection<Object> build() {
			return new ArrayList<>(collection);
		}
	}
}
