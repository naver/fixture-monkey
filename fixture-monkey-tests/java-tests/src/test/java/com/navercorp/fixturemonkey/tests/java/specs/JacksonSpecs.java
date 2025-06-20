package com.navercorp.fixturemonkey.tests.java.specs;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;

@SuppressWarnings("unused")
public class JacksonSpecs {

	@Value
	public static class JsonTypeInfoIdName {

		@JsonTypeInfo(use = Id.NAME)
		@JsonSubTypes(
			{
				@JsonSubTypes.Type(value = TypeA.class, name = "TypeA"),
				@JsonSubTypes.Type(value = TypeB.class, name = "typeB"),
			}
		)
		Type type;
	}

	@Value
	public static class JsonTypeInfoIdClass {

		@JsonTypeInfo(use = Id.CLASS)
		@JsonSubTypes(
			{
				@JsonSubTypes.Type(
					value = TypeA.class,
					name = "com.navercorp.fixturemonkey.tests.java.JacksonSpecs$TypeA"
				),
				@JsonSubTypes.Type(
					value = TypeB.class,
					name = "com.navercorp.fixturemonkey.tests.java.JacksonSpecs$TypeB"
				),
			}
		)
		Type type;
	}

	@Value
	public static class JsonTypeInfoList {

		@JsonTypeInfo(use = Id.NAME)
		@JsonSubTypes(
			{
				@JsonSubTypes.Type(value = TypeA.class, name = "TypeA"),
				@JsonSubTypes.Type(value = TypeB.class, name = "typeB"),
			}
		)
		List<Type> types;
	}

	@Value
	public static class JsonTypeInfoListIncludeWrapperObject {

		@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
		@JsonSubTypes(
			{
				@JsonSubTypes.Type(value = TypeA.class, name = "TypeA"),
				@JsonSubTypes.Type(value = TypeB.class, name = "typeB"),
			}
		)
		List<Type> types;
	}

	@Getter
	public static class JsonTypeInfoListInSetter {

		@JsonTypeInfo(use = Id.NAME)
		List<Type> types;

		@JsonSubTypes(
			{
				@JsonSubTypes.Type(value = TypeA.class, name = "TypeA"),
				@JsonSubTypes.Type(value = TypeB.class, name = "typeB"),
			}
		)
		public void setTypes(List<Type> types) {
			this.types = types;
		}
	}

	@Getter
	public static class JsonTypeInfoListInSetterIncludeWrapperObject {

		@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
		List<Type> types;

		@JsonSubTypes(
			{
				@JsonSubTypes.Type(value = TypeA.class, name = "TypeA"),
				@JsonSubTypes.Type(value = TypeB.class, name = "typeB"),
			}
		)
		public void setTypes(List<Type> types) {
			this.types = types;
		}
	}

	@Value
	public static class TypeWithAnnotationsValue {

		TypeWithAnnotations type;
	}

	@Value
	public static class TypeWithAnnotationsList {

		List<TypeWithAnnotations> types;
	}

	public interface Type {
	}

	@Value
	public static class TypeA implements Type {

		String value;
	}

	@Value
	@JsonTypeName("typeB")
	public static class TypeB implements Type {

		int value;
	}

	@JsonTypeInfo(use = Id.NAME)
	@JsonSubTypes(
		{
			@JsonSubTypes.Type(value = TypeAWithAnnotations.class, name = "TypeAWithAnnotations"),
			@JsonSubTypes.Type(value = TypeBWithAnnotations.class, name = "TypeBWithAnnotations"),
		}
	)
	public interface TypeWithAnnotations {
	}

	@Value
	public static class TypeAWithAnnotations implements TypeWithAnnotations {

		String value;
	}

	@Value
	public static class TypeBWithAnnotations implements TypeWithAnnotations {

		int value;
	}

	@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
	@JsonSubTypes(
		{
			@JsonSubTypes.Type(
				value = TypeAWithAnnotationsIncludeWrapperObject.class,
				name = "TypeAWithAnnotationsIncludeWrapperObject"
			),
			@JsonSubTypes.Type(
				value = TypeBWithAnnotationsIncludeWrapperObject.class,
				name = "TypeBWithAnnotationsIncludeWrapperObject"
			),
		}
	)
	public interface TypeWithAnnotationsIncludeWrapperObject {
	}

	@Value
	public static class TypeAWithAnnotationsIncludeWrapperObject implements TypeWithAnnotationsIncludeWrapperObject {

		String value;
	}

	@Value
	public static class TypeBWithAnnotationsIncludeWrapperObject implements TypeWithAnnotationsIncludeWrapperObject {

		int value;
	}

	@Value
	public static class TypeWithAnnotationsIncludeWrapperObjectList {

		List<TypeWithAnnotationsIncludeWrapperObject> types;
	}

	@AllArgsConstructor
	public static class ConstructorObject {

		private final String value1;
		private final int value2;
	}

	@Value
	public static class JsonPropertyOuter {

		JsonPropertyInner inner;
	}

	@Value
	public static class JsonPropertyWithOuter {

		@JsonProperty("outer")
		JsonPropertyInner inner;
	}

	@Value
	public static class JsonPropertyInner {

		@JsonProperty("renamed")
		String originalField;
	}

	@Value
	public static class JsonPropertyContainerOuter {

		@JsonProperty("body")
		List<String> contents;
	}

	@Value
	public static class JsonPropertyMultipleFields {

		@JsonProperty("name")
		String originalName;

		@JsonProperty("age")
		int originalAge;

		@JsonProperty("active")
		boolean originalActive;
	}

	@Value
	public static class JsonPropertyWithNormalField {

		@JsonProperty("renamed")
		String originalField;

		String normalField;
	}

	@AllArgsConstructor
	@Getter
	public static class JsonPropertyConstructor {

		@JsonProperty("id")
		private final String identifier;

		@JsonProperty("val")
		private final int number;
	}

	@Value
	public static class JsonTypeInfoFieldOuter {

		String name;

		@JsonTypeInfo(use = Id.NAME)
		@JsonSubTypes(
			{
				@JsonSubTypes.Type(value = TypeA.class, name = "TypeA"),
				@JsonSubTypes.Type(value = TypeB.class, name = "typeB"),
			}
		)
		Type type;

		@JsonTypeInfo(use = Id.NAME)
		@JsonSubTypes(
			{
				@JsonSubTypes.Type(value = TypeA.class, name = "TypeA"),
				@JsonSubTypes.Type(value = TypeB.class, name = "typeB"),
			}
		)
		List<Type> types;
	}

	@Value
	public static class JsonFormatObject {

		@JsonFormat(shape = JsonFormat.Shape.NUMBER)
		JsonEnum enumValue;

		@JsonFormat(pattern = "yyyy-MM-dd")
		LocalDate date;
	}

	public enum JsonEnum {
		ONE,
		TWO,
		THREE,
	}

	@Value
	public static class JsonNodeWrapper {

		JsonNode value;
	}

	@Value
	@Builder
	@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
	@AllArgsConstructor
	public static class DirectOrderPayMethod {

		long directOrderPayMethodNo;

		boolean usePointAll;

		DirectOrderPayMethodType firstPayMethod;

		DirectOrderPayMethodType secondPayMethod;

		public boolean isFirstPayMethodChargePoint() {
			return firstPayMethod.getPayMethodType().equals("CHARGE_POINT");
		}

		@Value
		public static class DirectOrderPayMethodType {

			String payMethodType;
		}
	}

	@Value
	public static class PayPreApproval {

		DirectOrderPayMethod payMethod;

		String description;
	}

	/**
	 * Simulates Lombok @Value WITHOUT @ConstructorProperties
	 * (i.e., lombok.anyConstructor.addConstructorProperties=false and no -parameters flag)
	 */
	@Getter
	public static class NoConstructorPropertiesPayMethod {

		private final String payMethodType;
		private final int amount;

		public NoConstructorPropertiesPayMethod(String payMethodType, int amount) {
			this.payMethodType = payMethodType;
			this.amount = amount;
		}
	}

	@Getter
	public static class NoConstructorPropertiesWrapper {

		private final NoConstructorPropertiesPayMethod payMethod;
		private final String description;

		public NoConstructorPropertiesWrapper(NoConstructorPropertiesPayMethod payMethod, String description) {
			this.payMethod = payMethod;
			this.description = description;
		}
	}
}
