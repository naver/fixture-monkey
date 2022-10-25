package com.navercorp.fixturemonkey.test;

import java.beans.ConstructorProperties;

import lombok.Getter;

class FixtureMonkeyV04ConstructorPropertiesTestSpecs {
	@Getter
	public static class ConstructorSimpleObject {
		private final String value;

		private final boolean isNull;
		private final boolean lengthOverFive;
		private final String fixedValue = "test";

		@ConstructorProperties("value")
		public ConstructorSimpleObject(String value) {
			this.value = value;
			this.isNull = value == null;
			this.lengthOverFive = !isNull && value.length() > 5;
		}
	}

	@Getter
	public static class ConstructorComplexObject {
		private final ConstructorSimpleObject value;

		@ConstructorProperties("value")
		public ConstructorComplexObject(ConstructorSimpleObject value) {
			this.value = value;
		}
	}
}
