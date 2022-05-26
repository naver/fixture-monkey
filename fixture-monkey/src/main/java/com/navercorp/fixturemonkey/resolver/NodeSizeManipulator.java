package com.navercorp.fixturemonkey.resolver;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class NodeSizeManipulator implements NodeManipulator {
	private final ArbitraryTraverser traverser;
	@Nullable
	private final Integer minSize;
	@Nullable
	private final Integer maxSize;

	public NodeSizeManipulator(
		ArbitraryTraverser traverser,
		@Nullable Integer minSize,
		@Nullable Integer maxSize
	) {
		this.traverser = traverser;
		this.minSize = minSize;
		this.maxSize = maxSize;
	}

	@Override
	public void manipulate(ArbitraryNode arbitraryNode) {
		ArbitraryProperty arbitraryProperty = arbitraryNode.getArbitraryProperty();
		if (arbitraryProperty.getContainerInfo() == null) {
			throw new IllegalArgumentException("Only container type supports NodeSizeManipulator.");
		}

		ArbitraryContainerInfo containerInfo = arbitraryProperty.getContainerInfo()
			.withElementMinSize(minSize)
			.withElementMaxSize(maxSize);

		ArbitraryNode traversedNode = traverser.traverse(arbitraryNode.getProperty(), containerInfo);
		ArbitraryProperty traversedNodeArbitraryProperty = traversedNode.getArbitraryProperty();
		arbitraryNode.setArbitraryProperty(
			arbitraryProperty
				.withChildProperties(traversedNodeArbitraryProperty.getChildProperties())
				.withContainerInfo(traversedNodeArbitraryProperty.getContainerInfo())
		);
		arbitraryNode.setArbitrary(traversedNode.getArbitrary());
		arbitraryNode.setChildren(traversedNode.getChildren());
	}
}
