package com.navercorp.fixturemonkey.tests.java.specs;

import lombok.Value;

public class NestedClassSpecs {
	@SuppressWarnings("InnerClassMayBeStatic")
	@Value
	public class Inner {
		String value;
	}
}
