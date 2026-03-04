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

package com.navercorp.objectfarm.api.input;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;

class TypeInputParserRegistryTest {

	@Test
	void defaultsShouldContainAllParsers() {
		// given & when
		TypeInputParserRegistry registry = TypeInputParserRegistry.defaults();

		// then
		List<TypeInputParser> parsers = registry.getParsers();
		then(parsers).hasSize(3);
		then(parsers.get(0)).isInstanceOf(JavaTypeInputParser.class);
		then(parsers.get(1)).isInstanceOf(JsonSchemaInputParser.class);
		then(parsers.get(2)).isInstanceOf(TypeScriptInputParser.class);
	}

	@Test
	void parseShouldSelectJavaTypeParserForClass() {
		// given
		TypeInputParserRegistry registry = TypeInputParserRegistry.defaults();

		// when
		JvmType result = registry.parse(String.class);

		// then
		then(result).isInstanceOf(JavaType.class);
		then(result.getRawType()).isEqualTo(String.class);
	}

	@Test
	void parseShouldSelectJavaTypeParserForJvmType() {
		// given
		TypeInputParserRegistry registry = TypeInputParserRegistry.defaults();
		JvmType input = new JavaType(Integer.class);

		// when
		JvmType result = registry.parse(input);

		// then
		then(result).isSameAs(input);
	}

	@Test
	void parseShouldSelectJsonSchemaParserForJsonSchema() {
		// given
		TypeInputParserRegistry registry = TypeInputParserRegistry.defaults();
		String jsonSchema = "{ \"type\": \"object\", \"properties\": { \"name\": { \"type\": \"string\" } } }";

		// when
		JvmType result = registry.parse(jsonSchema);

		// then
		then(result).isInstanceOf(SyntheticJvmType.class);
		SyntheticJvmType syntheticType = (SyntheticJvmType)result;
		then(syntheticType.getMembers()).hasSize(1);
		then(syntheticType.getMembers().get(0).getName()).isEqualTo("name");
	}

	@Test
	void parseShouldSelectTypeScriptParserForTsSyntax() {
		// given
		TypeInputParserRegistry registry = TypeInputParserRegistry.defaults();
		String tsType = "{ name: string, age: number }";

		// when
		JvmType result = registry.parse(tsType);

		// then
		then(result).isInstanceOf(SyntheticJvmType.class);
		SyntheticJvmType syntheticType = (SyntheticJvmType)result;
		then(syntheticType.getMembers()).hasSize(2);
	}

	@Test
	void parseShouldThrowExceptionForUnsupportedInput() {
		// given
		TypeInputParserRegistry registry = TypeInputParserRegistry.defaults();

		// when & then
		thenThrownBy(() -> registry.parse(123))
			.isInstanceOf(TypeParseException.class)
			.hasMessageContaining("No parser found");
	}

	@Test
	void builderShouldCreateCustomRegistry() {
		// given & when
		TypeInputParserRegistry registry = TypeInputParserRegistry.builder()
			.register(new JavaTypeInputParser())
			.build();

		// then
		then(registry.getParsers()).hasSize(1);
		then(registry.parse(String.class).getRawType()).isEqualTo(String.class);
	}
}
