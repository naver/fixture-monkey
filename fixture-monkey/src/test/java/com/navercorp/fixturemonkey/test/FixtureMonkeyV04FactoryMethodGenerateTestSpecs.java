package com.navercorp.fixturemonkey.test;

import lombok.Getter;

class FixtureMonkeyV04FactoryMethodGenerateTestSpecs {
	@Getter
	public static class FactoryMethodSpec {
		private final String type;
		private final String value;

		private FactoryMethodSpec(String type, String value) {
			this.type = type;
			this.value = value;
		}

		public static FactoryMethodSpec factory1(String value) {
			return new FactoryMethodSpec("type1", value);
		}

		public static FactoryMethodSpec factory2(String value) {
			return new FactoryMethodSpec("type2", value);
		}

		public static FactoryMethodSpec factory3(String value) {
			return new FactoryMethodSpec("type3", value);
		}
	}
}
