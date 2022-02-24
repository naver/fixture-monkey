package com.navercorp.fixturemonkey.api.type;

import static org.assertj.core.api.BDDAssertions.then;

import java.beans.PropertyDescriptor;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
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
		AnnotatedType type = typeReference.getAnnotatedType();
		AnnotatedType generics = Types.getGenericsTypes(type).get(0);

		// when
		Class<?> actual = Types.getActualType(generics.getType());

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
		AnnotatedType type = typeReference.getAnnotatedType();

		// when
		List<AnnotatedType> actual = Types.getGenericsTypes(type);

		then(actual).isEmpty();
	}

	@Test
	void getGenericsTypes() {
		// given
		TypeReference<GenericSample<String>> typeReference = new TypeReference<GenericSample<String>>() {
		};
		AnnotatedType type = typeReference.getAnnotatedType();

		// when
		List<AnnotatedType> actual = Types.getGenericsTypes(type);

		then(actual).hasSize(1);

		AnnotatedType genericsType = actual.get(0);
		then(genericsType.getType()).isEqualTo(String.class);
	}

	@Test
	void getGenericsTypesComplex() {
		// given
		TypeReference<GenericSample<GenericSample2<String>>> typeReference =
			new TypeReference<GenericSample<GenericSample2<String>>>() {
			};
		AnnotatedType type = typeReference.getAnnotatedType();

		// when
		List<AnnotatedType> actual = Types.getGenericsTypes(type);

		then(actual).hasSize(1);

		AnnotatedType genericsType = actual.get(0);
		then(genericsType).isInstanceOf(AnnotatedParameterizedType.class);
		then(genericsType.getType()).isInstanceOf(ParameterizedType.class);

		AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType)genericsType;
		then(Types.getActualType(parameterizedType.getType())).isEqualTo(GenericSample2.class);

		List<AnnotatedType> nestedGenericsTypes = Types.getGenericsTypes(genericsType);
		then(nestedGenericsTypes).hasSize(1);
		then(nestedGenericsTypes.get(0).getType()).isEqualTo(String.class);
	}

	@Test
	void getGenericsTypesBiGenericSample() {
		// given
		TypeReference<BiGenericSample<Integer, String>> typeReference =
			new TypeReference<BiGenericSample<Integer, String>>() {
			};
		AnnotatedType type = typeReference.getAnnotatedType();

		// when
		List<AnnotatedType> actual = Types.getGenericsTypes(type);

		then(actual).hasSize(2);
		then(actual.get(0).getType()).isEqualTo(Integer.class);
		then(actual.get(1).getType()).isEqualTo(String.class);
	}

	@Test
	void getGenericsTypesWithWildCard() {
		// given
		TypeReference<BiGenericSample<Integer, ?>> typeReference =
			new TypeReference<BiGenericSample<Integer, ?>>() {
			};
		AnnotatedType type = typeReference.getAnnotatedType();

		// when
		List<AnnotatedType> actual = Types.getGenericsTypes(type);

		then(actual).hasSize(2);
		then(actual.get(0).getType()).isEqualTo(Integer.class);

		AnnotatedType secondType = actual.get(1);
		then(secondType).isInstanceOf(AnnotatedWildcardType.class);
		then(secondType.getType()).isInstanceOf(WildcardType.class);
		then(Types.getActualType(secondType.getType())).isEqualTo(Object.class);
	}

	@Test
	void getGenericsTypesBiGenericSampleComplex() {
		// given
		TypeReference<BiGenericSample<GenericSample<Integer>, BiGenericSample<Integer, String>>> typeReference =
			new TypeReference<BiGenericSample<GenericSample<Integer>, BiGenericSample<Integer, String>>>() {
			};
		AnnotatedType type = typeReference.getAnnotatedType();

		// when
		List<AnnotatedType> actual = Types.getGenericsTypes(type);

		then(actual).hasSize(2);

		AnnotatedType firstGenericsType = actual.get(0);
		then(firstGenericsType).isInstanceOf(AnnotatedParameterizedType.class);
		then(firstGenericsType.getType()).isInstanceOf(ParameterizedType.class);

		AnnotatedParameterizedType firstParameterizedType = (AnnotatedParameterizedType)firstGenericsType;
		then(Types.getActualType(firstParameterizedType.getType())).isEqualTo(GenericSample.class);

		List<AnnotatedType> firstNestedGenericsTypes = Types.getGenericsTypes(firstParameterizedType);
		then(firstNestedGenericsTypes).hasSize(1);
		then(firstNestedGenericsTypes.get(0).getType()).isEqualTo(Integer.class);

		AnnotatedType secondGenericsType = actual.get(1);
		then(secondGenericsType).isInstanceOf(AnnotatedParameterizedType.class);
		then(secondGenericsType.getType()).isInstanceOf(ParameterizedType.class);

		AnnotatedParameterizedType secondParameterizedType = (AnnotatedParameterizedType)secondGenericsType;
		then(Types.getActualType(secondParameterizedType.getType())).isEqualTo(BiGenericSample.class);

		List<AnnotatedType> secondNestedGenericsTypes = Types.getGenericsTypes(secondParameterizedType);
		then(secondNestedGenericsTypes).hasSize(2);
		then(secondNestedGenericsTypes.get(0).getType()).isEqualTo(Integer.class);
		then(secondNestedGenericsTypes.get(1).getType()).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceGenerics() {
		// given
		TypeReference<GenericSample<String>> typeReference = new TypeReference<GenericSample<String>>() {
		};

		Map<String, Field> fields = PropertyCache.getFields(GenericSample.class);
		Field field = fields.get("name");

		// when
		AnnotatedType actual = Types.resolveWithTypeReferenceGenerics(typeReference.getAnnotatedType(), field);

		then(actual.getType()).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceNestedGenerics() {
		// given
		TypeReference<GenericSample<String>> typeReference = new TypeReference<GenericSample<String>>() {
		};

		Map<String, Field> fields = PropertyCache.getFields(GenericSample.class);
		Field field = fields.get("sample2");

		// when
		AnnotatedType actual = Types.resolveWithTypeReferenceGenerics(typeReference.getAnnotatedType(), field);

		then(actual).isInstanceOf(AnnotatedParameterizedType.class);
		then(actual.getType()).isInstanceOf(ParameterizedType.class);

		AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType)actual;
		then(((ParameterizedType)parameterizedType.getType()).getRawType()).isEqualTo(GenericSample2.class);
		then(parameterizedType.getAnnotatedActualTypeArguments()).hasSize(1);
		then(parameterizedType.getAnnotatedActualTypeArguments()[0].getType()).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceListGenerics() {
		// given
		TypeReference<GenericSample<String>> typeReference = new TypeReference<GenericSample<String>>() {
		};

		Map<String, Field> fields = PropertyCache.getFields(GenericSample.class);
		Field field = fields.get("list");

		// when
		AnnotatedType actual = Types.resolveWithTypeReferenceGenerics(typeReference.getAnnotatedType(), field);

		then(actual).isInstanceOf(AnnotatedParameterizedType.class);
		then(actual.getType()).isInstanceOf(ParameterizedType.class);

		AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType)actual;
		then(((ParameterizedType)parameterizedType.getType()).getRawType()).isEqualTo(List.class);
		then(parameterizedType.getAnnotatedActualTypeArguments()).hasSize(1);
		then(parameterizedType.getAnnotatedActualTypeArguments()[0].getType()).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsRefied() {
		// given
		TypeReference<GenericSample<String>> typeReference = new TypeReference<GenericSample<String>>() {
		};

		Map<String, Field> fields = PropertyCache.getFields(GenericSample.class);
		Field field = fields.get("samples");

		// when
		AnnotatedType actual = Types.resolveWithTypeReferenceGenerics(typeReference.getAnnotatedType(), field);

		then(actual).isInstanceOf(AnnotatedParameterizedType.class);
		then(actual.getType()).isInstanceOf(ParameterizedType.class);

		AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType)actual;
		then(((ParameterizedType)parameterizedType.getType()).getRawType()).isEqualTo(List.class);
		then(parameterizedType.getAnnotatedActualTypeArguments()).hasSize(1);
		then(parameterizedType.getAnnotatedActualTypeArguments()[0].getType()).isEqualTo(Sample.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsSimple() {
		// given
		TypeReference<Sample> typeReference = new TypeReference<Sample>() {
		};

		Map<String, Field> fields = PropertyCache.getFields(Sample.class);
		Field field = fields.get("name");

		// when
		AnnotatedType actual = Types.resolveWithTypeReferenceGenerics(typeReference.getAnnotatedType(), field);

		then(actual.getType()).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsWildCard() {
		// given
		TypeReference<GenericSample<?>> typeReference = new TypeReference<GenericSample<?>>() {
		};

		Map<String, Field> fields = PropertyCache.getFields(GenericSample.class);
		Field field = fields.get("name");

		// when
		AnnotatedType actual = Types.resolveWithTypeReferenceGenerics(typeReference.getAnnotatedType(), field);

		then(actual).isInstanceOf(AnnotatedWildcardType.class);
		then(actual.getType()).isInstanceOf(WildcardType.class);
		then(((AnnotatedWildcardType)actual).getAnnotatedUpperBounds()[0].getType()).isEqualTo(Object.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsNoGenerics() {
		// given
		TypeReference<GenericSample> typeReference = new TypeReference<GenericSample>() {
		};

		Map<String, Field> fields = PropertyCache.getFields(GenericSample.class);
		Field field = fields.get("name");

		// when
		AnnotatedType actual = Types.resolveWithTypeReferenceGenerics(typeReference.getAnnotatedType(), field);

		then(actual.getType()).isInstanceOf(Object.class);
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
		AnnotatedType actual = Types.resolveWithTypeReferenceGenerics(typeReference.getAnnotatedType(), field);

		then(actual.getType()).isEqualTo(Integer.class);
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
		AnnotatedType actual = Types.resolveWithTypeReferenceGenerics(typeReference.getAnnotatedType(), field);

		then(actual.getType()).isEqualTo(String.class);
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
		AnnotatedType actual = Types.resolveWithTypeReferenceGenerics(typeReference.getAnnotatedType(), field);

		then(actual).isInstanceOf(AnnotatedParameterizedType.class);
		then(actual.getType()).isInstanceOf(ParameterizedType.class);

		AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType)actual;
		then(((ParameterizedType)parameterizedType.getType()).getRawType()).isEqualTo(BiGenericSample2.class);
		then(parameterizedType.getAnnotatedActualTypeArguments()).hasSize(2);
		then(parameterizedType.getAnnotatedActualTypeArguments()[0].getType()).isEqualTo(Integer.class);
		then(parameterizedType.getAnnotatedActualTypeArguments()[1].getType()).isEqualTo(String.class);
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
		AnnotatedType actual = Types.resolveWithTypeReferenceGenerics(typeReference.getAnnotatedType(), field);

		then(actual).isInstanceOf(AnnotatedParameterizedType.class);
		then(actual.getType()).isInstanceOf(ParameterizedType.class);

		AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType)actual;
		then(((ParameterizedType)parameterizedType.getType()).getRawType()).isEqualTo(BiGenericSample2.class);
		then(parameterizedType.getAnnotatedActualTypeArguments()).hasSize(2);

		AnnotatedType firstGenerics = parameterizedType.getAnnotatedActualTypeArguments()[0];
		then(firstGenerics).isInstanceOf(AnnotatedParameterizedType.class);
		then(((ParameterizedType)firstGenerics.getType()).getRawType()).isEqualTo(GenericSample.class);
		then(((AnnotatedParameterizedType)firstGenerics).getAnnotatedActualTypeArguments()).hasSize(1);
		then(((AnnotatedParameterizedType)firstGenerics).getAnnotatedActualTypeArguments()[0].getType())
			.isEqualTo(Integer.class);

		AnnotatedType secondGenerics = parameterizedType.getAnnotatedActualTypeArguments()[1];
		then(secondGenerics).isInstanceOf(AnnotatedParameterizedType.class);
		then(((ParameterizedType)secondGenerics.getType()).getRawType()).isEqualTo(BiGenericSample.class);
		then(((AnnotatedParameterizedType)secondGenerics).getAnnotatedActualTypeArguments()).hasSize(2);
		then(((AnnotatedParameterizedType)secondGenerics).getAnnotatedActualTypeArguments()[0].getType())
			.isEqualTo(Integer.class);
		then(((AnnotatedParameterizedType)secondGenerics).getAnnotatedActualTypeArguments()[1].getType())
			.isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsPropertyDescriptor() throws NoSuchMethodException {
		// given
		TypeReference<GenericSample<String>> typeReference = new TypeReference<GenericSample<String>>() {
		};

		Map<Method, PropertyDescriptor> propertyDescriptors =
			PropertyCache.getPropertyDescriptors(GenericSample.class);
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(
			GenericSample.class.getDeclaredMethod("getName")
		);

		// when
		AnnotatedType actual = Types.resolveWithTypeReferenceGenerics(
			typeReference.getAnnotatedType(), propertyDescriptor);

		then(actual.getType()).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceNestedGenericsPropertyDescriptor() throws NoSuchMethodException {
		// given
		TypeReference<GenericSample<String>> typeReference = new TypeReference<GenericSample<String>>() {
		};

		Map<Method, PropertyDescriptor> propertyDescriptors =
			PropertyCache.getPropertyDescriptors(GenericSample.class);
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(
			GenericSample.class.getDeclaredMethod("getSample2")
		);

		// when
		AnnotatedType actual = Types.resolveWithTypeReferenceGenerics(
			typeReference.getAnnotatedType(), propertyDescriptor);

		then(actual).isInstanceOf(AnnotatedParameterizedType.class);
		then(actual.getType()).isInstanceOf(ParameterizedType.class);

		AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType)actual;
		then(((ParameterizedType)parameterizedType.getType()).getRawType()).isEqualTo(GenericSample2.class);
		then(parameterizedType.getAnnotatedActualTypeArguments()).hasSize(1);
		then(parameterizedType.getAnnotatedActualTypeArguments()[0].getType()).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceListGenericsPropertyDescriptor() throws NoSuchMethodException {
		// given
		TypeReference<GenericSample<String>> typeReference = new TypeReference<GenericSample<String>>() {
		};

		Map<Method, PropertyDescriptor> propertyDescriptors =
			PropertyCache.getPropertyDescriptors(GenericSample.class);
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(
			GenericSample.class.getDeclaredMethod("getList")
		);

		// when
		AnnotatedType actual = Types.resolveWithTypeReferenceGenerics(
			typeReference.getAnnotatedType(), propertyDescriptor);

		then(actual).isInstanceOf(AnnotatedParameterizedType.class);
		then(actual.getType()).isInstanceOf(ParameterizedType.class);

		AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType)actual;
		then(((ParameterizedType)parameterizedType.getType()).getRawType()).isEqualTo(List.class);
		then(parameterizedType.getAnnotatedActualTypeArguments()).hasSize(1);
		then(parameterizedType.getAnnotatedActualTypeArguments()[0].getType()).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsRefiedPropertyDescriptor() throws NoSuchMethodException {
		// given
		TypeReference<GenericSample<String>> typeReference = new TypeReference<GenericSample<String>>() {
		};

		Map<Method, PropertyDescriptor> propertyDescriptors =
			PropertyCache.getPropertyDescriptors(GenericSample.class);
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(
			GenericSample.class.getDeclaredMethod("getSamples")
		);

		// when
		AnnotatedType actual = Types.resolveWithTypeReferenceGenerics(
			typeReference.getAnnotatedType(), propertyDescriptor);

		then(actual).isInstanceOf(AnnotatedParameterizedType.class);
		then(actual.getType()).isInstanceOf(ParameterizedType.class);

		AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType)actual;
		then(((ParameterizedType)parameterizedType.getType()).getRawType()).isEqualTo(List.class);
		then(parameterizedType.getAnnotatedActualTypeArguments()).hasSize(1);
		then(parameterizedType.getAnnotatedActualTypeArguments()[0].getType()).isEqualTo(Sample.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsSimplePropertyDescriptor() throws NoSuchMethodException {
		// given
		TypeReference<Sample> typeReference = new TypeReference<Sample>() {
		};

		Map<Method, PropertyDescriptor> propertyDescriptors =
			PropertyCache.getPropertyDescriptors(Sample.class);
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(
			Sample.class.getDeclaredMethod("getName")
		);

		// when
		AnnotatedType actual = Types.resolveWithTypeReferenceGenerics(
			typeReference.getAnnotatedType(), propertyDescriptor);

		then(actual.getType()).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsWildCardPropertyDescriptor() throws NoSuchMethodException {
		// given
		TypeReference<GenericSample<?>> typeReference = new TypeReference<GenericSample<?>>() {
		};

		Map<Method, PropertyDescriptor> propertyDescriptors =
			PropertyCache.getPropertyDescriptors(GenericSample.class);
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(
			GenericSample.class.getDeclaredMethod("getName")
		);

		// when
		AnnotatedType actual = Types.resolveWithTypeReferenceGenerics(
			typeReference.getAnnotatedType(), propertyDescriptor);

		then(actual).isInstanceOf(AnnotatedWildcardType.class);
		then(actual.getType()).isInstanceOf(WildcardType.class);
		then(((AnnotatedWildcardType)actual).getAnnotatedUpperBounds()[0].getType()).isEqualTo(Object.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsNoGenericsPropertyDescriptor() throws NoSuchMethodException {
		// given
		TypeReference<GenericSample> typeReference = new TypeReference<GenericSample>() {
		};

		Map<Method, PropertyDescriptor> propertyDescriptors =
			PropertyCache.getPropertyDescriptors(GenericSample.class);
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(
			GenericSample.class.getDeclaredMethod("getName")
		);

		// when
		AnnotatedType actual = Types.resolveWithTypeReferenceGenerics(
			typeReference.getAnnotatedType(), propertyDescriptor);

		then(actual.getType()).isEqualTo(Object.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsBiGenericsFirstPropertyDescriptor() throws NoSuchMethodException {
		// given
		TypeReference<BiGenericSample<Integer, String>> typeReference =
			new TypeReference<BiGenericSample<Integer, String>>() {
			};

		Map<Method, PropertyDescriptor> propertyDescriptors =
			PropertyCache.getPropertyDescriptors(BiGenericSample.class);
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(
			BiGenericSample.class.getDeclaredMethod("getName")
		);

		// when
		AnnotatedType actual = Types.resolveWithTypeReferenceGenerics(
			typeReference.getAnnotatedType(), propertyDescriptor);

		then(actual.getType()).isEqualTo(Integer.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsBiGenericsSecondPropertyDescriptor() throws NoSuchMethodException {
		// given
		TypeReference<BiGenericSample<Integer, String>> typeReference =
			new TypeReference<BiGenericSample<Integer, String>>() {
			};

		Map<Method, PropertyDescriptor> propertyDescriptors =
			PropertyCache.getPropertyDescriptors(BiGenericSample.class);
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(
			BiGenericSample.class.getDeclaredMethod("getAddress")
		);

		// when
		AnnotatedType actual = Types.resolveWithTypeReferenceGenerics(
			typeReference.getAnnotatedType(), propertyDescriptor);

		then(actual.getType()).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsBiGenericsNestedPropertyDescriptor() throws NoSuchMethodException {
		// given
		TypeReference<BiGenericSample<Integer, String>> typeReference =
			new TypeReference<BiGenericSample<Integer, String>>() {
			};

		Map<Method, PropertyDescriptor> propertyDescriptors =
			PropertyCache.getPropertyDescriptors(BiGenericSample.class);
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(
			BiGenericSample.class.getDeclaredMethod("getSample2")
		);

		// when
		AnnotatedType actual = Types.resolveWithTypeReferenceGenerics(
			typeReference.getAnnotatedType(), propertyDescriptor);

		then(actual).isInstanceOf(AnnotatedParameterizedType.class);
		then(actual.getType()).isInstanceOf(ParameterizedType.class);

		AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType)actual;
		then(((ParameterizedType)parameterizedType.getType()).getRawType()).isEqualTo(BiGenericSample2.class);
		then(parameterizedType.getAnnotatedActualTypeArguments()).hasSize(2);
		then(parameterizedType.getAnnotatedActualTypeArguments()[0].getType()).isEqualTo(Integer.class);
		then(parameterizedType.getAnnotatedActualTypeArguments()[1].getType()).isEqualTo(String.class);
	}

	@Test
	void resolveWithTypeReferenceGenericsBiGenericsComplexPropertyDescriptor() throws NoSuchMethodException {
		// given
		TypeReference<BiGenericSample<GenericSample<Integer>, BiGenericSample<Integer, String>>> typeReference =
			new TypeReference<BiGenericSample<GenericSample<Integer>, BiGenericSample<Integer, String>>>() {
			};

		Map<Method, PropertyDescriptor> propertyDescriptors =
			PropertyCache.getPropertyDescriptors(BiGenericSample.class);
		PropertyDescriptor propertyDescriptor = propertyDescriptors.get(
			BiGenericSample.class.getDeclaredMethod("getSample2")
		);

		// when
		AnnotatedType actual = Types.resolveWithTypeReferenceGenerics(
			typeReference.getAnnotatedType(), propertyDescriptor);

		then(actual).isInstanceOf(AnnotatedParameterizedType.class);
		then(actual.getType()).isInstanceOf(ParameterizedType.class);

		AnnotatedParameterizedType parameterizedType = (AnnotatedParameterizedType)actual;
		then(((ParameterizedType)parameterizedType.getType()).getRawType()).isEqualTo(BiGenericSample2.class);
		then(parameterizedType.getAnnotatedActualTypeArguments()).hasSize(2);

		AnnotatedType firstGenerics = parameterizedType.getAnnotatedActualTypeArguments()[0];
		then(firstGenerics).isInstanceOf(AnnotatedParameterizedType.class);
		then(((ParameterizedType)firstGenerics.getType()).getRawType()).isEqualTo(GenericSample.class);
		then(((AnnotatedParameterizedType)firstGenerics).getAnnotatedActualTypeArguments()).hasSize(1);
		then(((AnnotatedParameterizedType)firstGenerics).getAnnotatedActualTypeArguments()[0].getType())
			.isEqualTo(Integer.class);

		AnnotatedType secondGenerics = parameterizedType.getAnnotatedActualTypeArguments()[1];
		then(secondGenerics).isInstanceOf(AnnotatedParameterizedType.class);
		then(((ParameterizedType)secondGenerics.getType()).getRawType()).isEqualTo(BiGenericSample.class);
		then(((AnnotatedParameterizedType)secondGenerics).getAnnotatedActualTypeArguments()).hasSize(2);
		then(((AnnotatedParameterizedType)secondGenerics).getAnnotatedActualTypeArguments()[0].getType())
			.isEqualTo(Integer.class);
		then(((AnnotatedParameterizedType)secondGenerics).getAnnotatedActualTypeArguments()[1].getType())
			.isEqualTo(String.class);
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
