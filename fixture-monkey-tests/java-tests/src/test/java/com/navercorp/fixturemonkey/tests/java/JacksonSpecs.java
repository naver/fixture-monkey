package com.navercorp.fixturemonkey.tests.java;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Value;

public class JacksonSpecs {
	@Value
	public static class JsonTypeInfoIdNameSpec {
		@JsonTypeInfo(use = Id.NAME)
		@JsonSubTypes({
			@JsonSubTypes.Type(value = TypeA.class, name = "TypeA"),
			@JsonSubTypes.Type(value = TypeB.class, name = "typeB")
		})
		Type type;
	}

	@Value
	public static class JsonTypeInfoIdClassSpec {
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
	public static class JsonTypeInfoListSpec {
		@JsonTypeInfo(use = Id.NAME)
		@JsonSubTypes({
			@JsonSubTypes.Type(value = TypeA.class, name = "TypeA"),
			@JsonSubTypes.Type(value = TypeB.class, name = "typeB")
		})
		List<Type> types;
	}

	@Value
	public static class TypeWithAnnotationsSpec {
		TypeWithAnnotations type;
	}

	@Value
	public static class TypeWithAnnotationsListSpec {
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
}
