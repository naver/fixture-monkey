package com.navercorp.fixturemonkey.report;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ManipulatorReport {
	private static final String border = String.join("", Collections.nCopies(80, "#"));

	public static String from(List<UserInputManipulatorsInfo> userInputManipulatoInfo, OptimizedManipulatorsInfo optimizedManipulatorsInfo) {
		return buildReport(userInputManipulatoInfo, optimizedManipulatorsInfo);
	}

	public static String buildReport(List<UserInputManipulatorsInfo> userInputManipulatoInfo, OptimizedManipulatorsInfo optimizedManipulatorsInfo) {
		StringBuilder reportBuilder = new StringBuilder();

		appendBorder(reportBuilder);
		appendHeader(reportBuilder);
		appendUserInputManipulatorsInfoReport(reportBuilder, userInputManipulatoInfo);
		appendOptimizedManipulatorsInfoReport(reportBuilder, optimizedManipulatorsInfo);
		appendBorder(reportBuilder);

		return reportBuilder.toString();
	}

	private static void appendBorder(StringBuilder reportBuilder) {
		reportBuilder.append(String.format("\n%s\n", border));
	}

	private static void appendHeader(StringBuilder reportBuilder) {
		reportBuilder.append(String.format("ArbitraryBuilder was built with the following operations\n"));
	}

	private static void appendUserInputManipulatorsInfoReport(StringBuilder reportBuilder, List<UserInputManipulatorsInfo> userInputManipulatoInfo) {
		reportBuilder.append(String.format("User Input\n"));

		AtomicInteger index = new AtomicInteger();
		reportBuilder.append(String.join("\n",
			userInputManipulatoInfo
				.stream()
				.map(it -> String.format("[%s] %s", index.getAndIncrement(), it.toDebugLog()))
				.collect(
					Collectors.toList()))
		);
	}

	private static void appendOptimizedManipulatorsInfoReport(StringBuilder reportBuilder, OptimizedManipulatorsInfo optimizedManipulatorsInfo) {
		reportBuilder.append(String.format("\n\nApplied Operations\n"));

		AtomicInteger index = new AtomicInteger();
		reportBuilder.append(String.join("\n%s", optimizedManipulatorsInfo.toDebugLog()));
	}
}
