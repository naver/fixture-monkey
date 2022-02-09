package com.navercorp.fixturemonkey.api.type;

import static org.assertj.core.api.BDDAssertions.then;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

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

	@Test
	void getGenericsTypesEmpty() {
		// given
		TypeReference<Sample> typeReference = new TypeReference<Sample>() {
		};
		Type type = typeReference.getType();

		// when
		List<Type> actual = Types.getGenericsTypes(type);

		then(actual).isEmpty();
	}

	@Test
	void getGenericsTypes() {
		// given
		TypeReference<GenericSample<String>> typeReference = new TypeReference<GenericSample<String>>() {
		};
		Type type = typeReference.getType();

		// when
		List<Type> actual = Types.getGenericsTypes(type);

		then(actual).hasSize(1);

		Type genericsType = actual.get(0);
		then(genericsType).isEqualTo(String.class);
	}

	@Test
	void getGenericsTypesComplex() {
		// given
		TypeReference<GenericSample<GenericSample2<String>>> typeReference =
			new TypeReference<GenericSample<GenericSample2<String>>>() {
			};
		Type type = typeReference.getType();

		// when
		List<Type> actual = Types.getGenericsTypes(type);

		then(actual).hasSize(1);

		Type genericsType = actual.get(0);
		then(genericsType).isInstanceOf(ParameterizedType.class);

		ParameterizedType parameterizedType = (ParameterizedType)genericsType;
		then(Types.getActualType(parameterizedType)).isEqualTo(GenericSample2.class);

		List<Type> nestedGenericsTypes = Types.getGenericsTypes(genericsType);
		then(nestedGenericsTypes).hasSize(1);
		then(nestedGenericsTypes.get(0)).isEqualTo(String.class);
	}

	@Test
	void getGenericsTypesBiGenericSample() {
		// given
		TypeReference<BiGenericSample<Integer, String>> typeReference =
			new TypeReference<BiGenericSample<Integer, String>>() {
			};
		Type type = typeReference.getType();

		// when
		List<Type> actual = Types.getGenericsTypes(type);

		then(actual).hasSize(2);
		then(actual.get(0)).isEqualTo(Integer.class);
		then(actual.get(1)).isEqualTo(String.class);
	}

	@Test
	void getGenericsTypesBiGenericSampleComplex() {
		// given
		TypeReference<BiGenericSample<GenericSample<Integer>, BiGenericSample<Integer, String>>> typeReference =
			new TypeReference<BiGenericSample<GenericSample<Integer>, BiGenericSample<Integer, String>>>() {
			};
		Type type = typeReference.getType();

		// when
		List<Type> actual = Types.getGenericsTypes(type);

		then(actual).hasSize(2);

		Type firstGenericsType = actual.get(0);
		then(firstGenericsType).isInstanceOf(ParameterizedType.class);

		ParameterizedType firstParameterizedType = (ParameterizedType)firstGenericsType;
		then(Types.getActualType(firstParameterizedType)).isEqualTo(GenericSample.class);

		List<Type> firstNestedGenericsTypes = Types.getGenericsTypes(firstParameterizedType);
		then(firstNestedGenericsTypes).hasSize(1);
		then(firstNestedGenericsTypes.get(0)).isEqualTo(Integer.class);

		Type secondGenericsType = actual.get(1);
		then(secondGenericsType).isInstanceOf(ParameterizedType.class);

		ParameterizedType secondParameterizedType = (ParameterizedType)secondGenericsType;
		then(Types.getActualType(secondParameterizedType)).isEqualTo(BiGenericSample.class);

		List<Type> secondNestedGenericsTypes = Types.getGenericsTypes(secondParameterizedType);
		then(secondNestedGenericsTypes).hasSize(2);
		then(secondNestedGenericsTypes.get(0)).isEqualTo(Integer.class);
		then(secondNestedGenericsTypes.get(1)).isEqualTo(String.class);
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

	static class BiGenericSample<T, R> {
		private BiGenericSample2<T, R> sample2;
		private T name;
		private R address;
		private Sample test;
	}

	static class BiGenericSample2<T, R> {
		private T name;
		private R address;
	}
}
