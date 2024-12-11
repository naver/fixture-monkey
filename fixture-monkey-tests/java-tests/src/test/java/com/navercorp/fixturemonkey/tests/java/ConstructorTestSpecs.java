package com.navercorp.fixturemonkey.tests.java;

import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import com.navercorp.fixturemonkey.tests.java.ImmutableJavaTestSpecs.Enum;

public class ConstructorTestSpecs {
	@Value
	public static class JavaTypeObject {
		String string;
		int primitiveInteger;
		float primitiveFloat;
		long primitiveLong;
		double primitiveDouble;
		byte primitiveByte;
		char primitiveCharacter;
		short primitiveShort;
		boolean primitiveBoolean;
		Integer wrapperInteger;
		Float wrapperFloat;
		Long wrapperLong;
		Double wrapperDouble;
		Byte wrapperByte;
		Character wrapperCharacter;
		Short wrapperShort;
		Boolean wrapperBoolean;
		Enum enumValue;

		public JavaTypeObject(
			int primitiveInteger,
			float primitiveFloat,
			long primitiveLong,
			double primitiveDouble,
			byte primitiveByte,
			char primitiveCharacter,
			short primitiveShort,
			boolean primitiveBoolean
		) {
			this.string = "first";
			this.primitiveInteger = primitiveInteger;
			this.primitiveFloat = primitiveFloat;
			this.primitiveLong = primitiveLong;
			this.primitiveDouble = primitiveDouble;
			this.primitiveByte = primitiveByte;
			this.primitiveCharacter = primitiveCharacter;
			this.primitiveShort = primitiveShort;
			this.primitiveBoolean = primitiveBoolean;
			this.wrapperInteger = null;
			this.wrapperFloat = null;
			this.wrapperLong = null;
			this.wrapperDouble = null;
			this.wrapperByte = null;
			this.wrapperCharacter = null;
			this.wrapperShort = null;
			this.wrapperBoolean = null;
			this.enumValue = null;
		}

		public JavaTypeObject() {
			this.string = "second";
			this.primitiveInteger = 1;
			this.primitiveFloat = -1;
			this.primitiveLong = 1;
			this.primitiveDouble = 1.0;
			this.primitiveByte = 1;
			this.primitiveCharacter = 1;
			this.primitiveShort = 2;
			this.primitiveBoolean = false;
			this.wrapperInteger = null;
			this.wrapperFloat = null;
			this.wrapperLong = null;
			this.wrapperDouble = null;
			this.wrapperByte = null;
			this.wrapperCharacter = null;
			this.wrapperShort = null;
			this.wrapperBoolean = null;
			this.enumValue = null;
		}

		public JavaTypeObject(String str) {
			this.string = str;
			this.primitiveInteger = 1;
			this.primitiveFloat = -1;
			this.primitiveLong = 1;
			this.primitiveDouble = 1.0;
			this.primitiveByte = 1;
			this.primitiveCharacter = 1;
			this.primitiveShort = 2;
			this.primitiveBoolean = false;
			this.wrapperInteger = null;
			this.wrapperFloat = null;
			this.wrapperLong = null;
			this.wrapperDouble = null;
			this.wrapperByte = null;
			this.wrapperCharacter = null;
			this.wrapperShort = null;
			this.wrapperBoolean = null;
			this.enumValue = null;
		}

		public static JavaTypeObject from(String str) {
			return new JavaTypeObject("factory");
		}
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@Value
	public static class ContainerObject {
		int[] primitiveArray;
		String[] array;
		JavaTypeObject[] complexArray;
		List<String> list;
		List<JavaTypeObject> complexList;
		Set<String> set;
		Set<JavaTypeObject> complexSet;
		Map<String, Integer> map;
		Map<String, JavaTypeObject> complexMap;
		Map.Entry<String, Integer> mapEntry;
		Map.Entry<String, JavaTypeObject> complexMapEntry;
		Optional<String> optional;
		OptionalInt optionalInt;
		OptionalLong optionalLong;
		OptionalDouble optionalDouble;

		public ContainerObject(
			int[] primitiveArray,
			String[] array,
			JavaTypeObject[] complexArray,
			List<String> list,
			List<JavaTypeObject> complexList,
			Set<String> set,
			Set<JavaTypeObject> complexSet,
			Map<String, Integer> map,
			Map<String, JavaTypeObject> complexMap,
			Entry<String, Integer> mapEntry,
			Entry<String, JavaTypeObject> complexMapEntry,
			Optional<String> optional,
			OptionalInt optionalInt,
			OptionalLong optionalLong,
			OptionalDouble optionalDouble
		) {
			this.primitiveArray = primitiveArray;
			this.array = array;
			this.complexArray = complexArray;
			this.list = list;
			this.complexList = complexList;
			this.set = set;
			this.complexSet = complexSet;
			this.map = map;
			this.complexMap = complexMap;
			this.mapEntry = mapEntry;
			this.complexMapEntry = complexMapEntry;
			this.optional = optional;
			this.optionalInt = optionalInt;
			this.optionalLong = optionalLong;
			this.optionalDouble = optionalDouble;
		}

		public ContainerObject(
			List<String> list,
			List<JavaTypeObject> complexList,
			Set<String> set,
			Set<JavaTypeObject> complexSet,
			Map<String, Integer> map,
			Map<String, JavaTypeObject> complexMap,
			Entry<String, Integer> mapEntry,
			Entry<String, JavaTypeObject> complexMapEntry,
			Optional<String> optional,
			OptionalInt optionalInt,
			OptionalLong optionalLong,
			OptionalDouble optionalDouble
		) {
			this.primitiveArray = new int[] {1};
			this.array = new String[] {"test"};
			this.complexArray = new JavaTypeObject[0];
			this.list = list;
			this.complexList = complexList;
			this.set = set;
			this.complexSet = complexSet;
			this.map = map;
			this.complexMap = complexMap;
			this.mapEntry = mapEntry;
			this.complexMapEntry = complexMapEntry;
			this.optional = optional;
			this.optionalInt = optionalInt;
			this.optionalLong = optionalLong;
			this.optionalDouble = optionalDouble;
		}
	}

	@Value
	public static class GenericObject<T> {
		T value;

		public GenericObject(T value) {
			this.value = value;
		}
	}

	@Value
	public static class TwoGenericObject<T, U> {
		T tValue;
		U uValue;

		public TwoGenericObject(T tValue, U uValue) {
			this.tValue = tValue;
			this.uValue = uValue;
		}
	}

	@Value
	public static class SimpleContainerObject {
		List<JavaTypeObject> list;

		public SimpleContainerObject(List<JavaTypeObject> list) {
			this.list = list;
		}
	}

	public static class FieldAndConstructorParameterMismatchObject {
		private final String value;

		@ConstructorProperties("integer")
		public FieldAndConstructorParameterMismatchObject(int integer) {
			this.value = String.valueOf(integer);
		}

		public String getValue() {
			return value;
		}
	}

	@Getter
	@AllArgsConstructor
	public class JavaxValidationObject {
		@Max(100)
		@Min(100)
		private int value;
	}
}
