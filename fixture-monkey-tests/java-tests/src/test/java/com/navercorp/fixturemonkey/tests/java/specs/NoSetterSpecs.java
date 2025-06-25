package com.navercorp.fixturemonkey.tests.java.specs;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class NoSetterSpecs {
	@NoArgsConstructor
	@Getter
	public static class StringObject {
		private String value;
	}
}
