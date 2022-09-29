package com.navercorp.fixturemonkey.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DebugInfoObserver implements Observer {
	public static DebugInfoObserver INSTANCE;
	private final Map<Integer, UserInputManipulatorListInfo> arbitraryBuilderToUserInputInfo = new HashMap<>();
	private final Map<Integer, OptimizedManipulatorsInfo> arbitraryBuilderToOptimizedInfo = new HashMap<>();
	private final Map<Integer, List<Integer>> arbitraryBuilderSampledPoint = new HashMap<>();
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public void update(Integer builder, DebugInfo debugInfo) {
		if (debugInfo instanceof UserInputManipulatorInfo) {
			UserInputManipulatorListInfo manipulatorListInfo = arbitraryBuilderToUserInputInfo.get(builder);
			if (manipulatorListInfo == null) {
				manipulatorListInfo = new UserInputManipulatorListInfo(new ArrayList<>());
				arbitraryBuilderToUserInputInfo.put(builder, manipulatorListInfo);
			}
			manipulatorListInfo.getManipulators().add((UserInputManipulatorInfo)debugInfo);
		} else if (debugInfo instanceof OptimizedManipulatorsInfo) {
			arbitraryBuilderToOptimizedInfo.put(builder, (OptimizedManipulatorsInfo)debugInfo);
		} else if (debugInfo instanceof ArbitraryBuilderSampledInfo) {
			List<Integer> sampledInfoList = arbitraryBuilderSampledPoint.get(builder);
			if (sampledInfoList == null) {
				sampledInfoList = new ArrayList<>();
			}
			sampledInfoList.add(((ArbitraryBuilderSampledInfo)debugInfo).getSampledIndex());
			arbitraryBuilderSampledPoint.put(builder, sampledInfoList);
		}
	}

	public void reportResult() {
		StringBuilder messageBuilder = new StringBuilder();
		for (Integer builder : arbitraryBuilderToOptimizedInfo.keySet()) {
			for (Integer sampleIndex : arbitraryBuilderSampledPoint.get(builder)) {
				messageBuilder.append(
					String.format("\n%s",
						ManipulatorReport.from(
							arbitraryBuilderToUserInputInfo.get(builder),
							arbitraryBuilderToOptimizedInfo.get(builder)
						)
					)
				);
			}
		}
		log.info(messageBuilder.toString());
	}
}
