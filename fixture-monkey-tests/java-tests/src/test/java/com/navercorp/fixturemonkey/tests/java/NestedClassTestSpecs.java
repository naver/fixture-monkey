package com.navercorp.fixturemonkey.tests.java;

import lombok.Value;

class NestedClassTestSpecs {
	@SuppressWarnings("InnerClassMayBeStatic")
	@Value
	public class Inner {
		String value;
	}
}
