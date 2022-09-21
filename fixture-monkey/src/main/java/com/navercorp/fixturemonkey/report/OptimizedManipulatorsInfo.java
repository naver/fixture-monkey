package com.navercorp.fixturemonkey.report;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.navercorp.fixturemonkey.resolver.ArbitraryManipulator;

public class OptimizedManipulatorsInfo implements ManipulatorsInfo {
	private final List<ArbitraryManipulator> manipulators;

	public OptimizedManipulatorsInfo(List<ArbitraryManipulator> manipulators) {
		this.manipulators = manipulators;
	}

	@Override
	public String toDebugLog() {
		AtomicInteger index = new AtomicInteger();
		return String.join("\n",
			manipulators
				.stream()
				.map(it -> String.format("[%s] %s", index.getAndIncrement(), it.toString()))
				.collect(Collectors.toList())
		);
	}
}
