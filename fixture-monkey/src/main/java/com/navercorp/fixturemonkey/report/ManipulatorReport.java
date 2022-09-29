package com.navercorp.fixturemonkey.report;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ManipulatorReport {
	private static final String border = String.join("", Collections.nCopies(80, "#"));

	public static String from(UserInputManipulatorListInfo userInputManipulatorListInfo, OptimizedManipulatorsInfo optimizedManipulatorsInfo) {
		return buildReport(userInputManipulatorListInfo, optimizedManipulatorsInfo);
	}

	public static String buildReport(UserInputManipulatorListInfo userInputManipulatorListInfo, OptimizedManipulatorsInfo optimizedManipulatorsInfo) {
		StringBuilder reportBuilder = new StringBuilder();

		appendBorder(reportBuilder);
		appendHeader(reportBuilder);
		appendUserInputManipulatorsInfoReport(reportBuilder, userInputManipulatorListInfo);
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

	private static void appendUserInputManipulatorsInfoReport(StringBuilder reportBuilder, UserInputManipulatorListInfo userInputManipulatorListInfo) {
		reportBuilder.append(String.format("User Input\n"));

		reportBuilder.append(String.join("\n%s", userInputManipulatorListInfo.toDebugLog()));
	}

	private static void appendOptimizedManipulatorsInfoReport(StringBuilder reportBuilder, OptimizedManipulatorsInfo optimizedManipulatorsInfo) {
		reportBuilder.append(String.format("\n\nApplied Operations\n"));

		reportBuilder.append(String.join("\n%s", optimizedManipulatorsInfo.toDebugLog()));
	}
}
