package com.navercorp.fixturemonkey.api.type;

import static org.assertj.core.api.BDDAssertions.then;

import java.lang.reflect.Type;

import org.junit.jupiter.api.Test;

class TypesTest {

	@Test
	void getActualType() {
		// given
		TypeReference<Sample> typeReference = new TypeReference<Sample>() {
		};
		Type type = typeReference.getType();

		// when
		Class<?> actual = Types.getActualType(type);

		then(actual).isEqualTo(Sample.class);
	}

	@Test
	void getActualTypeForGenerics() {
		// given
		TypeReference<GenericSample<String>> typeReference = new TypeReference<GenericSample<String>>() {
		};
		Type type = typeReference.getType();

		// when
		Class<?> actual = Types.getActualType(type);

		then(actual).isEqualTo(GenericSample.class);
	}

	@Test
	void getActualTypeForWildCardGenerics() {
		// given
		TypeReference<GenericSample<?>> typeReference = new TypeReference<GenericSample<?>>() {
		};
		Type type = typeReference.getType();

		// when
		Class<?> actual = Types.getActualType(type);

		then(actual).isEqualTo(GenericSample.class);
	}

	@Test
	void getActualTypeForExceptGenerics() {
		// given
		TypeReference<GenericSample> typeReference = new TypeReference<GenericSample>() {
		};
		Type type = typeReference.getType();

		// when
		Class<?> actual = Types.getActualType(type);

		then(actual).isEqualTo(GenericSample.class);
	}

	static class Sample {
		private String name;
	}

	static class GenericSample<T> {
		private GenericSample2<T> sample2;
		private T name;
		private Sample test;
	}

	static class GenericSample2<T> {
		private T name;
	}
}
