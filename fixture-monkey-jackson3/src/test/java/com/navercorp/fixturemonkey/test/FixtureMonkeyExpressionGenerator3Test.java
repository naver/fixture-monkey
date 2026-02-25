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

import lombok.Value;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.expression.ExpressionGenerator;
import com.navercorp.fixturemonkey.api.property.DefaultPropertyGenerator;
import com.navercorp.fixturemonkey.api.property.PropertyGenerator;
import com.navercorp.fixturemonkey.api.property.RootProperty;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.jackson3.plugin.Jackson3Plugin;

public class FixtureMonkeyExpressionGenerator3Test {
	private static final PropertyGenerator DEFAULT_PROPERTY_GENERATOR = new DefaultPropertyGenerator();

	@SuppressWarnings("OptionalGetWithoutIsPresent")
	@Property
	void setJsonPropertyWithExpressionGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new Jackson3Plugin())
			.build();
		TypeReference<JsonPropertyClass> typeReference = new TypeReference<>() {
		};
		ExpressionGenerator expressionGenerator = resolver -> {
			com.navercorp.fixturemonkey.api.property.Property property =
				DEFAULT_PROPERTY_GENERATOR.generateChildProperties(new RootProperty(typeReference.getAnnotatedType()))
					.stream()
					.filter(it -> "value".equals(it.getName()))
					.findFirst()
					.get();
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
