package com.navercorp.fixturemonkey.tests.java;

import lombok.Builder;
import lombok.Value;

@SuppressWarnings("unused")
class ImmutableGenericTypeSpecs {

	public interface GenericInterface<T> {
	}

	@Value
	@Builder
	public static class GenericImplementationObject<T> implements GenericInterface<T> {
		T value;
	}

	public interface TwoGenericInterface<T, U> {
	}

	@Value
	@Builder
	public static class TwoGenericImplementationObject<T, U> implements TwoGenericInterface<T, U> {
		T tValue;

		U uValue;
	}

	@Value
	@Builder
	public static class GenericObject<T> {
		T value;
	}

	public interface Interface {
	}
}
