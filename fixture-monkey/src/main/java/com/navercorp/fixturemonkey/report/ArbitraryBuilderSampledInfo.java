package com.navercorp.fixturemonkey.report;


public class ArbitraryBuilderSampledInfo implements ManipulatorsInfo {
	private final Integer sampledIndex;

	public ArbitraryBuilderSampledInfo(Integer sampledIndex) {
		this.sampledIndex = sampledIndex;
	}

	public Integer getSampledIndex() {
		return sampledIndex;
	}

	@Override
	public String toDebugLog() {
		throw new IllegalArgumentException("Should not be used for logging");
	}
}
