package com.navercorp.fixturemonkey.report;


public class ArbitraryBuilderSampledInfo implements ManipulatorsInfo {
	private Integer userInputIndex;
	private final Integer optimizedIndex;

	public ArbitraryBuilderSampledInfo(Integer optimizedIndex) {
		this.optimizedIndex = optimizedIndex;
	}

	public ArbitraryBuilderSampledInfo setUserInputIndex(Integer userInputIndex) {
		this.userInputIndex = userInputIndex;
		return this;
	}

	public Integer getUserInputIndex() {
		return userInputIndex;
	}

	public Integer getOptimizedIndex() {
		return optimizedIndex;
	}

	@Override
	public String toDebugLog(Integer index) {
		throw new IllegalArgumentException("Should not be used for logging");
	}
}
