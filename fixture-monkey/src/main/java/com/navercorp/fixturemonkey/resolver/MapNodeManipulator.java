package com.navercorp.fixturemonkey.resolver;

import java.util.List;

public final class MapNodeManipulator implements NodeManipulator {
	private final ArbitraryTraverser traverser;
	private final List<NodeManipulator> keyManipulators;
	private final List<NodeManipulator> valueManipulators;

	public MapNodeManipulator(
		ArbitraryTraverser traverser,
		List<NodeManipulator> keyManipulators,
		List<NodeManipulator> valueManipulators
	) {
		this.traverser = traverser;
		this.keyManipulators = keyManipulators;
		this.valueManipulators = valueManipulators;
	}

	@Override
	public void manipulate(ArbitraryNode arbitraryNode) {
		AddMapEntryNodeManipulator addMapEntryManipulator = new AddMapEntryNodeManipulator(traverser);
		addMapEntryManipulator.manipulate(arbitraryNode);

		ArbitraryNode entryNode = arbitraryNode.getChildren().get(arbitraryNode.getChildren().size() - 1);
		for (NodeManipulator keyManipulator : keyManipulators) {
			keyManipulator.manipulate(entryNode.getChildren().get(0));
		}
		for (NodeManipulator valueManipulator : valueManipulators) {
			valueManipulator.manipulate(entryNode.getChildren().get(1));
		}
	}
}
