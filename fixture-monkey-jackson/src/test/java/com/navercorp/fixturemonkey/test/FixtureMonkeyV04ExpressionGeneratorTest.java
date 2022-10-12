package com.navercorp.fixturemonkey.test;

import static org.assertj.core.api.BDDAssertions.then;

import net.jqwik.api.Property;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;

import com.navercorp.fixturemonkey.LabMonkey;
import com.navercorp.fixturemonkey.api.expression.ExpressionGenerator;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.jackson.plugin.JacksonPlugin;

public class FixtureMonkeyV04ExpressionGeneratorTest {
	@SuppressWarnings("OptionalGetWithoutIsPresent")
	@Property
	void setJsonPropertyWithExpressionGenerator() {
		// given
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.plugin(new JacksonPlugin())
			.build();
		TypeReference<JsonPropertyClass> typeReference = new TypeReference<JsonPropertyClass>() {
		};
		ExpressionGenerator expressionGenerator = resolver -> {
			com.navercorp.fixturemonkey.api.property.Property property =
				PropertyCache.getProperty(typeReference.getAnnotatedType(), "value").get();
			return resolver.resolve(property);
		};

		// when
		JsonPropertyClass actual = sut.giveMeBuilder(JsonPropertyClass.class)
			.set(expressionGenerator, "set")
			.sample();

		then(actual.value).isEqualTo("set");
	}

	@Value
	public static class JsonPropertyClass {
		@JsonProperty("jsonValue")
		String value;
	}
}
