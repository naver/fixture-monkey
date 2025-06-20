package com.navercorp.objectfarm.api.node.specs;

public class GenericObject {
	public interface GenericInterface<T> {
	}

	public static class GenericImplementation<T> implements GenericInterface<T> {
		T value;
	}

	public static class GenericArrayObject<T> {
		GenericImplementation<T>[] values;
	}

	public static class GenericStringArrayObject {
		GenericArrayObject<String> stringArray;
	}
}
