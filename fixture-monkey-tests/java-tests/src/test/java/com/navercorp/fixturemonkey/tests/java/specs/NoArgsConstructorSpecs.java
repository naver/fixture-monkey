package com.navercorp.fixturemonkey.tests.java.specs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class NoArgsConstructorSpecs {
	@NoArgsConstructor
	@Getter
	@Setter
	public static class JavaTypeObject {
		private String string;
		private int primitiveInteger;
		private float primitiveFloat;
		private long primitiveLong;
		private double primitiveDouble;
		private byte primitiveByte;
		private char primitiveCharacter;
		private short primitiveShort;
		private boolean primitiveBoolean;
		private Integer wrapperInteger;
		private Float wrapperFloat;
		private Long wrapperLong;
		private Double wrapperDouble;
		private Byte wrapperByte;
		private Character wrapperCharacter;
		private Short wrapperShort;
		private Boolean wrapperBoolean;
		private Enum enumValue;
	}

	@NoArgsConstructor
	@Getter
	@Setter
	public static class StringObject {
		private String value;
	}

	@NoArgsConstructor
	@Getter
	@Setter
	public static class NestedObject {
		private StringObject object;
	}
}
