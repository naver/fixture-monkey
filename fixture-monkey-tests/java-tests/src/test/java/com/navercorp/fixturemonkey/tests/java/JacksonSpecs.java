package com.navercorp.fixturemonkey.tests.java;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

@SuppressWarnings("unused")
public class JacksonSpecs {
	@Value
	public static class JsonTypeInfoIdName {
		@JsonTypeInfo(use = Id.NAME)
		@JsonSubTypes({
			@JsonSubTypes.Type(value = TypeA.class, name = "TypeA"),
			@JsonSubTypes.Type(value = TypeB.class, name = "typeB")
		})
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
				)
			}
		)
		Type type;
	}

	@Value
	public static class JsonTypeInfoList {
		@JsonTypeInfo(use = Id.NAME)
		@JsonSubTypes({
			@JsonSubTypes.Type(value = TypeA.class, name = "TypeA"),
			@JsonSubTypes.Type(value = TypeB.class, name = "typeB")
		})
		List<Type> types;
	}

	@Value
	public static class JsonTypeInfoListIncludeWrapperObject {
		@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
		@JsonSubTypes({
			@JsonSubTypes.Type(value = TypeA.class, name = "TypeA"),
			@JsonSubTypes.Type(value = TypeB.class, name = "typeB")
		})
		List<Type> types;
	}

	@Getter
	public static class JsonTypeInfoListInSetter {
		@JsonTypeInfo(use = Id.NAME)
		List<Type> types;

		@JsonSubTypes({
			@JsonSubTypes.Type(value = TypeA.class, name = "TypeA"),
			@JsonSubTypes.Type(value = TypeB.class, name = "typeB")
		})
		public void setTypes(List<Type> types) {
			this.types = types;
		}
	}

	@Getter
	public static class JsonTypeInfoListInSetterIncludeWrapperObject {
		@JsonTypeInfo(use = Id.NAME, include = As.WRAPPER_OBJECT)
		List<Type> types;

		@JsonSubTypes({
			@JsonSubTypes.Type(value = TypeA.class, name = "TypeA"),
			@JsonSubTypes.Type(value = TypeB.class, name = "typeB")
		})
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
	@JsonSubTypes({
		@JsonSubTypes.Type(value = TypeAWithAnnotations.class, name = "TypeAWithAnnotations"),
		@JsonSubTypes.Type(value = TypeBWithAnnotations.class, name = "TypeBWithAnnotations")
	})
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
	@JsonSubTypes({
		@JsonSubTypes.Type(
			value = TypeAWithAnnotationsIncludeWrapperObject.class,
			name = "TypeAWithAnnotationsIncludeWrapperObject"
			),
		@JsonSubTypes.Type(
			value = TypeBWithAnnotationsIncludeWrapperObject.class,
			name = "TypeBWithAnnotationsIncludeWrapperObject"
			)
	})
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
}
