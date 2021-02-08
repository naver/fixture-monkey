package com.navercorp.fixturemonkey.arbitrary;

public class NotSupportedTypeException extends RuntimeException {
	public NotSupportedTypeException(Class<?> clazz) {
		this(String.format(
			"The request type '%s' isn't supported to generate anonymous values.",
			clazz));
	}

	public NotSupportedTypeException(String message) {
		super(message);
	}
}
