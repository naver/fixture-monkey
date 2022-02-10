package com.navercorp.fixturemonkey.api.type;

import static org.assertj.core.api.BDDAssertions.then;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.api.property.PropertyCache;

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
	void getActualTypeForGenericsWildCard() {
		// given
		TypeReference<GenericSample<?>> typeReference = new TypeReference<GenericSample<?>>() {
		};
		Type type = typeReference.getType();
		Type generics = Types.getGenericsTypes(type).get(0);

		// when
		Class<?> actual = Types.getActualType(generics);

		then(actual).isEqualTo(Object.class);
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
	void getGenericsTypesWithWildCard() {
		// given
		TypeReference<BiGenericSample<Integer, ?>> typeReference =
			new TypeReference<BiGenericSample<Integer, ?>>() {
			};
		Type type = typeReference.getType();

		// when
		List<Type> actual = Types.getGenericsTypes(type);

		then(actual).hasSize(2);
		then(actual.get(0)).isEqualTo(Integer.class);

		Type secondType = actual.get(1);
		then(secondType).isInstanceOf(WildcardType.class);
		then(Types.getActualType(secondType)).isEqualTo(Object.class);
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

	@Test
	void resolveWithTypeReferenceGenerics() {
		// given
		TypeReference<GenericSample<String>> typeReference = new TypeReference<GenericSample<String>>() {
		};

		Map<String, Field> fields = PropertyCache.getFields(GenericSample.class);
		Field field = fields.get("name");

		// when
		Type actual = Types.resolveWithTypeReferenceGenerics(typeReference, field);

		then(actual).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceNestedGenerics() {
		// given
		TypeReference<GenericSample<String>> typeReference = new TypeReference<GenericSample<String>>() {
		};

		Map<String, Field> fields = PropertyCache.getFields(GenericSample.class);
		Field field = fields.get("sample2");

		// when
		Type actual = Types.resolveWithTypeReferenceGenerics(typeReference, field);

		then(actual).isInstanceOf(ParameterizedType.class);

		ParameterizedType parameterizedType = (ParameterizedType)actual;
		then(parameterizedType.getRawType()).isEqualTo(GenericSample2.class);
		then(parameterizedType.getActualTypeArguments()).hasSize(1);
		then(parameterizedType.getActualTypeArguments()[0]).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceListGenerics() {
		// given
		TypeReference<GenericSample<String>> typeReference = new TypeReference<GenericSample<String>>() {
		};

		Map<String, Field> fields = PropertyCache.getFields(GenericSample.class);
		Field field = fields.get("list");

		// when
		Type actual = Types.resolveWithTypeReferenceGenerics(typeReference, field);

		then(actual).isInstanceOf(ParameterizedType.class);

		ParameterizedType parameterizedType = (ParameterizedType)actual;
		then(parameterizedType.getRawType()).isEqualTo(List.class);
		then(parameterizedType.getActualTypeArguments()).hasSize(1);
		then(parameterizedType.getActualTypeArguments()[0]).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsRefied() {
		// given
		TypeReference<GenericSample<String>> typeReference = new TypeReference<GenericSample<String>>() {
		};

		Map<String, Field> fields = PropertyCache.getFields(GenericSample.class);
		Field field = fields.get("samples");

		// when
		Type actual = Types.resolveWithTypeReferenceGenerics(typeReference, field);

		then(actual).isInstanceOf(ParameterizedType.class);

		ParameterizedType parameterizedType = (ParameterizedType)actual;
		then(parameterizedType.getRawType()).isEqualTo(List.class);
		then(parameterizedType.getActualTypeArguments()).hasSize(1);
		then(parameterizedType.getActualTypeArguments()[0]).isEqualTo(Sample.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsSimple() {
		// given
		TypeReference<Sample> typeReference = new TypeReference<Sample>() {
		};

		Map<String, Field> fields = PropertyCache.getFields(Sample.class);
		Field field = fields.get("name");

		// when
		Type actual = Types.resolveWithTypeReferenceGenerics(typeReference, field);

		then(actual).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsWildCard() {
		// given
		TypeReference<GenericSample<?>> typeReference = new TypeReference<GenericSample<?>>() {
		};

		Map<String, Field> fields = PropertyCache.getFields(GenericSample.class);
		Field field = fields.get("name");

		// when
		Type actual = Types.resolveWithTypeReferenceGenerics(typeReference, field);

		then(actual).isInstanceOf(WildcardType.class);
		then(((WildcardType)actual).getUpperBounds()[0]).isEqualTo(Object.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsNoGenerics() {
		// given
		TypeReference<GenericSample> typeReference = new TypeReference<GenericSample>() {
		};

		Map<String, Field> fields = PropertyCache.getFields(GenericSample.class);
		Field field = fields.get("name");

		// when
		Type actual = Types.resolveWithTypeReferenceGenerics(typeReference, field);

		then(actual).isEqualTo(Object.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsBiGenericsFirst() {
		// given
		TypeReference<BiGenericSample<Integer, String>> typeReference =
			new TypeReference<BiGenericSample<Integer, String>>() {
			};

		Map<String, Field> fields = PropertyCache.getFields(BiGenericSample.class);
		Field field = fields.get("name");

		// when
		Type actual = Types.resolveWithTypeReferenceGenerics(typeReference, field);

		then(actual).isEqualTo(Integer.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsBiGenericsSecond() {
		// given
		TypeReference<BiGenericSample<Integer, String>> typeReference =
			new TypeReference<BiGenericSample<Integer, String>>() {
			};

		Map<String, Field> fields = PropertyCache.getFields(BiGenericSample.class);
		Field field = fields.get("address");

		// when
		Type actual = Types.resolveWithTypeReferenceGenerics(typeReference, field);

		then(actual).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsBiGenericsNested() {
		// given
		TypeReference<BiGenericSample<Integer, String>> typeReference =
			new TypeReference<BiGenericSample<Integer, String>>() {
			};

		Map<String, Field> fields = PropertyCache.getFields(BiGenericSample.class);
		Field field = fields.get("sample2");

		// when
		Type actual = Types.resolveWithTypeReferenceGenerics(typeReference, field);

		then(actual).isInstanceOf(ParameterizedType.class);

		ParameterizedType parameterizedType = (ParameterizedType)actual;
		then(parameterizedType.getRawType()).isEqualTo(BiGenericSample2.class);
		then(parameterizedType.getActualTypeArguments()).hasSize(2);
		then(parameterizedType.getActualTypeArguments()[0]).isEqualTo(Integer.class);
		then(parameterizedType.getActualTypeArguments()[1]).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsBiGenericsComplex() {
		// given
		TypeReference<BiGenericSample<GenericSample<Integer>, BiGenericSample<Integer, String>>> typeReference =
			new TypeReference<BiGenericSample<GenericSample<Integer>, BiGenericSample<Integer, String>>>() {
			};

		Map<String, Field> fields = PropertyCache.getFields(BiGenericSample.class);
		Field field = fields.get("sample2");

		// when
		Type actual = Types.resolveWithTypeReferenceGenerics(typeReference, field);

		then(actual).isInstanceOf(ParameterizedType.class);

		ParameterizedType parameterizedType = (ParameterizedType)actual;
		then(parameterizedType.getRawType()).isEqualTo(BiGenericSample2.class);
		then(parameterizedType.getActualTypeArguments()).hasSize(2);

		Type firstGenerics = parameterizedType.getActualTypeArguments()[0];
		then(firstGenerics).isInstanceOf(ParameterizedType.class);
		then(((ParameterizedType)firstGenerics).getRawType()).isEqualTo(GenericSample.class);
		then(((ParameterizedType)firstGenerics).getActualTypeArguments()).hasSize(1);
		then(((ParameterizedType)firstGenerics).getActualTypeArguments()[0]).isEqualTo(Integer.class);

		Type secondGenerics = parameterizedType.getActualTypeArguments()[1];
		then(secondGenerics).isInstanceOf(ParameterizedType.class);
		then(((ParameterizedType)secondGenerics).getRawType()).isEqualTo(BiGenericSample.class);
		then(((ParameterizedType)secondGenerics).getActualTypeArguments()).hasSize(2);
		then(((ParameterizedType)secondGenerics).getActualTypeArguments()[0]).isEqualTo(Integer.class);
		then(((ParameterizedType)secondGenerics).getActualTypeArguments()[1]).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsPropertyDescriptor() throws NoSuchMethodException {
		// given
		TypeReference<GenericSample<String>> typeReference = new TypeReference<GenericSample<String>>() {
		};

		Map<Method, PropertyDescriptor> propertyDescriptors =
			PropertyCache.getReadPropertyDescriptors(GenericSample.class);
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(
			GenericSample.class.getDeclaredMethod("getName")
		);

		// when
		Type actual = Types.resolveWithTypeReferenceGenerics(typeReference, propertyDescriptor);

		then(actual).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceNestedGenericsPropertyDescriptor() throws NoSuchMethodException {
		// given
		TypeReference<GenericSample<String>> typeReference = new TypeReference<GenericSample<String>>() {
		};

		Map<Method, PropertyDescriptor> propertyDescriptors =
			PropertyCache.getReadPropertyDescriptors(GenericSample.class);
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(
			GenericSample.class.getDeclaredMethod("getSample2")
		);

		// when
		Type actual = Types.resolveWithTypeReferenceGenerics(typeReference, propertyDescriptor);

		then(actual).isInstanceOf(ParameterizedType.class);

		ParameterizedType parameterizedType = (ParameterizedType)actual;
		then(parameterizedType.getRawType()).isEqualTo(GenericSample2.class);
		then(parameterizedType.getActualTypeArguments()).hasSize(1);
		then(parameterizedType.getActualTypeArguments()[0]).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceListGenericsPropertyDescriptor() throws NoSuchMethodException {
		// given
		TypeReference<GenericSample<String>> typeReference = new TypeReference<GenericSample<String>>() {
		};

		Map<Method, PropertyDescriptor> propertyDescriptors =
			PropertyCache.getReadPropertyDescriptors(GenericSample.class);
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(
			GenericSample.class.getDeclaredMethod("getList")
		);

		// when
		Type actual = Types.resolveWithTypeReferenceGenerics(typeReference, propertyDescriptor);

		then(actual).isInstanceOf(ParameterizedType.class);

		ParameterizedType parameterizedType = (ParameterizedType)actual;
		then(parameterizedType.getRawType()).isEqualTo(List.class);
		then(parameterizedType.getActualTypeArguments()).hasSize(1);
		then(parameterizedType.getActualTypeArguments()[0]).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsRefiedPropertyDescriptor() throws NoSuchMethodException {
		// given
		TypeReference<GenericSample<String>> typeReference = new TypeReference<GenericSample<String>>() {
		};

		Map<Method, PropertyDescriptor> propertyDescriptors =
			PropertyCache.getReadPropertyDescriptors(GenericSample.class);
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(
			GenericSample.class.getDeclaredMethod("getSamples")
		);

		// when
		Type actual = Types.resolveWithTypeReferenceGenerics(typeReference, propertyDescriptor);

		then(actual).isInstanceOf(ParameterizedType.class);

		ParameterizedType parameterizedType = (ParameterizedType)actual;
		then(parameterizedType.getRawType()).isEqualTo(List.class);
		then(parameterizedType.getActualTypeArguments()).hasSize(1);
		then(parameterizedType.getActualTypeArguments()[0]).isEqualTo(Sample.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsSimplePropertyDescriptor() throws NoSuchMethodException {
		// given
		TypeReference<Sample> typeReference = new TypeReference<Sample>() {
		};

		Map<Method, PropertyDescriptor> propertyDescriptors =
			PropertyCache.getReadPropertyDescriptors(Sample.class);
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(
			Sample.class.getDeclaredMethod("getName")
		);

		// when
		Type actual = Types.resolveWithTypeReferenceGenerics(typeReference, propertyDescriptor);

		then(actual).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsWildCardPropertyDescriptor() throws NoSuchMethodException {
		// given
		TypeReference<GenericSample<?>> typeReference = new TypeReference<GenericSample<?>>() {
		};

		Map<Method, PropertyDescriptor> propertyDescriptors =
			PropertyCache.getReadPropertyDescriptors(GenericSample.class);
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(
			GenericSample.class.getDeclaredMethod("getName")
		);

		// when
		Type actual = Types.resolveWithTypeReferenceGenerics(typeReference, propertyDescriptor);

		then(actual).isInstanceOf(WildcardType.class);
		then(((WildcardType)actual).getUpperBounds()[0]).isEqualTo(Object.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsNoGenericsPropertyDescriptor() throws NoSuchMethodException {
		// given
		TypeReference<GenericSample> typeReference = new TypeReference<GenericSample>() {
		};

		Map<Method, PropertyDescriptor> propertyDescriptors =
			PropertyCache.getReadPropertyDescriptors(GenericSample.class);
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(
			GenericSample.class.getDeclaredMethod("getName")
		);

		// when
		Type actual = Types.resolveWithTypeReferenceGenerics(typeReference, propertyDescriptor);

		then(actual).isEqualTo(Object.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsBiGenericsFirstPropertyDescriptor() throws NoSuchMethodException {
		// given
		TypeReference<BiGenericSample<Integer, String>> typeReference =
			new TypeReference<BiGenericSample<Integer, String>>() {
			};

		Map<Method, PropertyDescriptor> propertyDescriptors =
			PropertyCache.getReadPropertyDescriptors(BiGenericSample.class);
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(
			BiGenericSample.class.getDeclaredMethod("getName")
		);

		// when
		Type actual = Types.resolveWithTypeReferenceGenerics(typeReference, propertyDescriptor);

		then(actual).isEqualTo(Integer.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsBiGenericsSecondPropertyDescriptor() throws NoSuchMethodException {
		// given
		TypeReference<BiGenericSample<Integer, String>> typeReference =
			new TypeReference<BiGenericSample<Integer, String>>() {
			};

		Map<Method, PropertyDescriptor> propertyDescriptors =
			PropertyCache.getReadPropertyDescriptors(BiGenericSample.class);
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(
			BiGenericSample.class.getDeclaredMethod("getAddress")
		);

		// when
		Type actual = Types.resolveWithTypeReferenceGenerics(typeReference, propertyDescriptor);

		then(actual).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsBiGenericsNestedPropertyDescriptor() throws NoSuchMethodException {
		// given
		TypeReference<BiGenericSample<Integer, String>> typeReference =
			new TypeReference<BiGenericSample<Integer, String>>() {
			};

		Map<Method, PropertyDescriptor> propertyDescriptors =
			PropertyCache.getReadPropertyDescriptors(BiGenericSample.class);
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(
			BiGenericSample.class.getDeclaredMethod("getSample2")
		);

		// when
		Type actual = Types.resolveWithTypeReferenceGenerics(typeReference, propertyDescriptor);

		then(actual).isInstanceOf(ParameterizedType.class);

		ParameterizedType parameterizedType = (ParameterizedType)actual;
		then(parameterizedType.getRawType()).isEqualTo(BiGenericSample2.class);
		then(parameterizedType.getActualTypeArguments()).hasSize(2);
		then(parameterizedType.getActualTypeArguments()[0]).isEqualTo(Integer.class);
		then(parameterizedType.getActualTypeArguments()[1]).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsBiGenericsComplexPropertyDescriptor() throws NoSuchMethodException {
		// given
		TypeReference<BiGenericSample<GenericSample<Integer>, BiGenericSample<Integer, String>>> typeReference =
			new TypeReference<BiGenericSample<GenericSample<Integer>, BiGenericSample<Integer, String>>>() {
			};

		Map<Method, PropertyDescriptor> propertyDescriptors =
			PropertyCache.getReadPropertyDescriptors(BiGenericSample.class);
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(
			BiGenericSample.class.getDeclaredMethod("getSample2")
		);

		// when
		Type actual = Types.resolveWithTypeReferenceGenerics(typeReference, propertyDescriptor);

		then(actual).isInstanceOf(ParameterizedType.class);

		ParameterizedType parameterizedType = (ParameterizedType)actual;
		then(parameterizedType.getRawType()).isEqualTo(BiGenericSample2.class);
		then(parameterizedType.getActualTypeArguments()).hasSize(2);

		Type firstGenerics = parameterizedType.getActualTypeArguments()[0];
		then(firstGenerics).isInstanceOf(ParameterizedType.class);
		then(((ParameterizedType)firstGenerics).getRawType()).isEqualTo(GenericSample.class);
		then(((ParameterizedType)firstGenerics).getActualTypeArguments()).hasSize(1);
		then(((ParameterizedType)firstGenerics).getActualTypeArguments()[0]).isEqualTo(Integer.class);

		Type secondGenerics = parameterizedType.getActualTypeArguments()[1];
		then(secondGenerics).isInstanceOf(ParameterizedType.class);
		then(((ParameterizedType)secondGenerics).getRawType()).isEqualTo(BiGenericSample.class);
		then(((ParameterizedType)secondGenerics).getActualTypeArguments()).hasSize(2);
		then(((ParameterizedType)secondGenerics).getActualTypeArguments()[0]).isEqualTo(Integer.class);
		then(((ParameterizedType)secondGenerics).getActualTypeArguments()[1]).isEqualTo(String.class);
	}

	static class Sample {
		private String name;

		public String getName() {
			return this.name;
		}
	}

	static class GenericSample<T> {
		private GenericSample2<T> sample2;
		private T name;
		private Sample test;
		private List<T> list;
		private List<Sample> samples;

		public GenericSample2<T> getSample2() {
			return this.sample2;
		}

		public T getName() {
			return this.name;
		}

		public Sample getTest() {
			return this.test;
		}

		public List<T> getList() {
			return this.list;
		}

		public List<Sample> getSamples() {
			return this.samples;
		}
	}

	static class GenericSample2<T> {
		private T name;

		public T getName() {
			return this.name;
		}
	}

	static class BiGenericSample<T, R> {
		private BiGenericSample2<T, R> sample2;
		private T name;
		private R address;
		private Sample test;

		public BiGenericSample2<T, R> getSample2() {
			return this.sample2;
		}

		public T getName() {
			return this.name;
		}

		public R getAddress() {
			return this.address;
		}

		public Sample getTest() {
			return this.test;
		}
	}

	static class BiGenericSample2<T, R> {
		private T name;
		private R address;

		public T getName() {
			return this.name;
		}

		public R getAddress() {
			return this.address;
		}
	}
}
