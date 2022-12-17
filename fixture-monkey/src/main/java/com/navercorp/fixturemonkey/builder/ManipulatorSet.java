package com.navercorp.fixturemonkey.builder;

import java.util.List;

import com.navercorp.fixturemonkey.MonkeyManipulatorFactory;
import com.navercorp.fixturemonkey.resolver.ArbitraryManipulator;
import com.navercorp.fixturemonkey.resolver.ContainerInfoManipulator;
import com.navercorp.fixturemonkey.resolver.NodeResolver;

public class ManipulatorSet {
	private final List<NodeResolverObjectHolder> nodeResolverObjectHolders;
	private final List<ContainerInfoManipulator> containerInfoManipulators;

	public ManipulatorSet(
		List<NodeResolverObjectHolder> nodeResolverObjectHolders,
		List<ContainerInfoManipulator> containerInfoManipulators
	) {
		this.nodeResolverObjectHolders = nodeResolverObjectHolders;
		this.containerInfoManipulators = containerInfoManipulators;
	}

	public List<NodeResolverObjectHolder> getNodeResolverObjectHolders() {
		return nodeResolverObjectHolders;
	}

	public List<ContainerInfoManipulator> getContainerInfoManipulators() {
		return containerInfoManipulators;
	}

	public static class NodeResolverObjectHolder {
		private final NodeResolver nodeResolver;
		private final Object value;

		public NodeResolverObjectHolder(NodeResolver nodeResolver, Object value) {
			this.nodeResolver = nodeResolver;
			this.value = value;
		}

		public ArbitraryManipulator convert(MonkeyManipulatorFactory monkeyManipulatorFactory) {
			return new ArbitraryManipulator(
				nodeResolver,
				monkeyManipulatorFactory.convertToNodeManipulator(this.value)
			);
		}
	}
}
