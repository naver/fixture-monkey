package com.navercorp.fixturemonkey.resolver;

import java.util.ArrayList;
import java.util.List;

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

		ArbitraryNode manipulatedNode = traverser.traverse(arbitraryNode.getProperty(), containerInfo);
		ArbitraryProperty traversedNodeArbitraryProperty = manipulatedNode.getArbitraryProperty();
		arbitraryNode.setArbitraryProperty(
			arbitraryProperty
				.withChildProperties(traversedNodeArbitraryProperty.getChildProperties())
				.withContainerInfo(traversedNodeArbitraryProperty.getContainerInfo())
		);
		arbitraryNode.setArbitrary(manipulatedNode.getArbitrary());
		arbitraryNode.setChildren(leftJoin(manipulatedNode, arbitraryNode).getChildren());
	}

	private ArbitraryNode leftJoin(
		ArbitraryNode leftNode,
		@Nullable ArbitraryNode rightNode
	) {
		if (rightNode == null) {
			return leftNode;
		}

		leftNode.setArbitrary(rightNode.getArbitrary());
		leftNode.setArbitraryProperty(
			leftNode.getArbitraryProperty()
				.withNullInject(rightNode.getArbitraryProperty().getNullInject())
		);

		List<ArbitraryNode> leftChildren = leftNode.getChildren();
		List<ArbitraryNode> rightChildren = rightNode.getChildren();
		int childrenSize = leftChildren.size();

		List<ArbitraryNode> result = new ArrayList<>();
		for (int i = 0; i < childrenSize; i++) {
			ArbitraryNode leftChild = leftChildren.get(i);
			ArbitraryNode rightChild = rightChildren.size() > i ? rightChildren.get(i) : null;

			result.add(leftJoin(leftChild, rightChild));
		}
		leftNode.setChildren(result);
		return leftNode;
	}
}
