package com.navercorp.fixturemonkey.test;

import static org.assertj.core.api.BDDAssertions.then;

import net.jqwik.api.Property;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import com.navercorp.fixturemonkey.LabMonkey;
import com.navercorp.fixturemonkey.api.expression.ExpressionGenerator;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.jackson.property.JacksonPropertyNameResolver;

public class FixtureMonkeyV04ExpressionGeneratorTest {
	@Property
	void giveMeJsonPropertySetWithExpressionGenerator() {
		// given
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.defaultPropertyNameResolver(new JacksonPropertyNameResolver())
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

	@Data
	public static class JsonPropertyClass {
		@JsonProperty("jsonValue")
		private String value;
	}
}
