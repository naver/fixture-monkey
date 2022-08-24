package com.navercorp.fixturemonkey.test;

import java.util.List;
import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

public class ExpressionGeneratorTestSpecs {
	@Getter
	@Setter
	@EqualsAndHashCode
	public static class StringList {
		private List<String> values;
	}

	@Getter
	@Setter
	@EqualsAndHashCode
	public static class StringValue {
		private String value;
	}

	@Getter
	@Setter
	@EqualsAndHashCode
	public static class MapKeyIntegerValueInteger {
		private Map<Integer, Integer> values;
	}
}
