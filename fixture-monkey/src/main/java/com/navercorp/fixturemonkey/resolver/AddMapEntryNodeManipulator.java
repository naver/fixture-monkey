package com.navercorp.fixturemonkey.resolver;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;

public class AddMapEntryNodeManipulator implements NodeManipulator {
	private final ArbitraryTraverser traverser;

	public AddMapEntryNodeManipulator(ArbitraryTraverser traverser) {
		this.traverser = traverser;
	}

	@Override
	public void manipulate(ArbitraryNode arbitraryNode) {
		ArbitraryProperty arbitraryProperty = arbitraryNode.getArbitraryProperty();
		ArbitraryContainerInfo containerInfo = arbitraryProperty
			.getContainerInfo().withElementMinSize(1).withElementMaxSize(1);
		ArbitraryNode entryNode = traverser.traverse(arbitraryProperty.getProperty(), containerInfo)
			.getChildren().get(0);

		arbitraryProperty.getChildProperties().add(entryNode.getProperty());
		arbitraryNode.getChildren().add(entryNode);
	}
}
