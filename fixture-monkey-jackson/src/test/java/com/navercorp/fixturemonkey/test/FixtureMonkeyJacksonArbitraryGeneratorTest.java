/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.fixturemonkey.test;

import static org.assertj.core.api.BDDAssertions.then;

import net.jqwik.api.Property;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
import lombok.Value;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.expression.ExpressionGenerator;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.generator.BeanArbitraryGenerator;
import com.navercorp.fixturemonkey.jackson.generator.JacksonArbitraryGenerator;

class FixtureMonkeyJacksonArbitraryGeneratorTest {
	private final FixtureMonkey sut = FixtureMonkey.builder()
		.defaultGenerator(JacksonArbitraryGenerator.INSTANCE)
		.build();

	@Property
	void giveMeJsonPropertySet() {
		// when
		JsonPropertyClass actual = sut.giveMeBuilder(JsonPropertyClass.class)
			.set("jsonValue", "set")
			.sample();

		then(actual.value).isEqualTo("set");
	}

	@Property
	void giveMeJsonPropertySetWithExpressionGenerator() {
		// given
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

	@Property
	void giveMeJsonPropertyWithBeanArbitraryGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultGenerator(BeanArbitraryGenerator.INSTANCE)
			.build();

		// when
		JsonPropertyClass actual = sut.giveMeBuilder(JsonPropertyClass.class)
			.set("jsonValue", "set")
			.sample();

		then(actual.value).isNotEqualTo("set");
	}

	@Property
	void giveMeJsonNodeReturnsNull() {
		// when
		JsonNodeWrapperClass actual = sut.giveMeOne(JsonNodeWrapperClass.class);

		then(actual.getValue().isNull()).isTrue();
	}

	@Data
	public static class JsonPropertyClass {
		@JsonProperty("jsonValue")
		private String value;
	}

	@Value
	public static class JsonNodeWrapperClass {
		JsonNode value;
	}
}
