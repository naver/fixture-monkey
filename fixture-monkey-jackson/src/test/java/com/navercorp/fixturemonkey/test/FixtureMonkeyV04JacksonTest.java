package com.navercorp.fixturemonkey.test;

import static org.assertj.core.api.BDDAssertions.thenNoException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import net.jqwik.api.Property;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Value;

import com.navercorp.fixturemonkey.LabMonkey;
import com.navercorp.fixturemonkey.jackson.plugin.JacksonPlugin;

class FixtureMonkeyV04JacksonTest {
	private static final LabMonkey SUT = LabMonkey.labMonkeyBuilder()
		.plugin(new JacksonPlugin())
		.build();

	@Property
	void jsonFormat() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonFormatSpec.class));
	}

	@Property
	void jsonTypeInfo() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoIdNameSpec.class));
	}

	@Property
	void jsonTypeListInfo() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoListSpec.class));
	}

	@Property
	void jsonTypeInfoWithIdClass() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoIdClassSpec.class));
	}

	@Property
	void jsonTypeWithoutAnnotations() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(TypeWithAnnotationsSpec.class));
	}

	@Property
	void jsonTypeListWithoutAnnotations() {
		thenNoException().isThrownBy(() -> SUT.giveMeBuilder(TypeWithAnnotationsListSpec.class));
	}

	@Value
	public static class JsonFormatSpec {
		@JsonFormat(shape = Shape.NUMBER)
		JsonEnum jsonEnum;

		@JsonFormat(pattern = "yyyy MM dd")
		Date date;

		@JsonFormat(pattern = "yyyy MM dd")
		LocalDate localDate;

		@JsonFormat(pattern = "HH:mm:ss")
		LocalTime localTime;

		@JsonFormat(pattern = "yyyy MM dd HH:mm:ss")
		LocalDateTime localDateTime;

		@JsonFormat(pattern = "yyyy MM dd HH:mm:ssZ")
		Instant instant;

		@JsonFormat(pattern = "yyyy MM dd HH:mm:ssZ")
		ZonedDateTime zonedDateTime;

		@JsonFormat(pattern = "yyyy MM dd HH:mm:ssZ")
		OffsetDateTime offsetDateTime;
	}

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
		@JsonSubTypes({
			@JsonSubTypes.Type(
				value = TypeA.class,
				name = "com.navercorp.fixturemonkey.test.FixtureMonkeyV04Test$TypeA"
			),
			@JsonSubTypes.Type(
				value = TypeB.class,
				name = "com.navercorp.fixturemonkey.test.FixtureMonkeyV04Test$TypeB"
			)
		})
		Type type;
	}

	@Value
	public static class TypeWithAnnotationsSpec {
		TypeWithAnnotations type;
	}

	@Value
	public static class TypeWithAnnotationsListSpec {
		List<TypeWithAnnotations> types;
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

	public enum JsonEnum {
		ONE, TWO, THREE
	}
}
