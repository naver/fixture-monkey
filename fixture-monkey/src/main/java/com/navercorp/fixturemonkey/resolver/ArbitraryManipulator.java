package com.navercorp.fixturemonkey.resolver;

import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ArbitraryManipulator {
	private final NodeResolver nodeResolver;
	private final NodeManipulator nodeManipulator;

	public ArbitraryManipulator(
		NodeResolver nodeResolver,
		NodeManipulator nodeManipulator
	) {
		this.nodeResolver = nodeResolver;
		this.nodeManipulator = nodeManipulator;
	}

	public void manipulate(ArbitraryTree tree) {
		List<ArbitraryNode> nodes = nodeResolver.resolve(tree);
		for (ArbitraryNode node : nodes) {
			nodeManipulator.manipulate(node);
		}
	}
}
