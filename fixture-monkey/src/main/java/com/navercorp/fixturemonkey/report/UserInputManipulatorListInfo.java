package com.navercorp.fixturemonkey.report;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class UserInputManipulatorListInfo implements ManipulatorsInfo {
	private final List<UserInputManipulatorInfo> manipulators;

	public UserInputManipulatorListInfo(List<UserInputManipulatorInfo> manipulators) {
		this.manipulators = manipulators;
	}

	public List<UserInputManipulatorInfo> getManipulators() {
		return manipulators;
	}

	@Override
	public String toDebugLog(Integer idx) {
		AtomicInteger index = new AtomicInteger();
		return String.join("\n",
			manipulators
				.subList(0, idx)
				.stream()
				.map(it -> String.format("[%s] %s", index.getAndIncrement(), it.toDebugLog(0)))
				.collect(Collectors.toList()));
	}
}
