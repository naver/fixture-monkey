package com.navercorp.fixturemonkey;

import java.util.List;
import java.util.stream.Collectors;

public final class UserInputManipulatorsInfo {
	private final String methodName;
	private final List<Object> args;

	public UserInputManipulatorsInfo(String methodName, List<Object> args) {
		this.methodName = methodName;
		this.args = args;
	}

	public String toDebugLog() {
		List<String> strArgs = args.stream().map(Object::toString).collect(Collectors.toList());
		if (methodName.equals("set")) {
			return ".set(" + String.join(", ", strArgs) + ")";
		} else if (methodName.equals("size")) {
			return ".size(" + String.join(", ", strArgs) + ")";
		}
		throw new IllegalArgumentException(
			"Wrong method used"
		);
	}
}
