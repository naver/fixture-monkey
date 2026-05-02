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

import com.navercorp.objectfarm.api.type.JvmType;

class JsonSchemaInputParserTest {

	private final JsonSchemaInputParser parser = new JsonSchemaInputParser();
	private final TypeParseContext context = TypeParseContext.defaults();

	@Test
	void supportsShouldReturnTrueForJsonSchemaString() {
		// given
		String jsonSchema = "{ \"type\": \"object\" }";

		// when & then
		then(parser.supports(jsonSchema)).isTrue();
	}

	@Test
	void supportsShouldReturnFalseForNonJsonSchema() {
		// given & when & then
		then(parser.supports("not a json")).isFalse();
		then(parser.supports(123)).isFalse();
		then(parser.supports(null)).isFalse();
	}

	@Test
	void parseShouldHandlePrimitiveTypes() {
		// given & when & then
		then(parser.parse("{ \"type\": \"string\" }", context).getRawType()).isEqualTo(String.class);
		then(parser.parse("{ \"type\": \"integer\" }", context).getRawType()).isEqualTo(Integer.class);
		then(parser.parse("{ \"type\": \"number\" }", context).getRawType()).isEqualTo(Double.class);
		then(parser.parse("{ \"type\": \"boolean\" }", context).getRawType()).isEqualTo(Boolean.class);
	}

	@Test
	void parseShouldHandleObjectWithProperties() {
		// given
		String schema = "{ \"type\": \"object\", \"properties\": { "
			+ "\"name\": { \"type\": \"string\" }, "
			+ "\"age\": { \"type\": \"integer\" } "
			+ "} }";

		// when
		JvmType result = parser.parse(schema, context);

		// then
		then(result).isInstanceOf(SyntheticJvmType.class);
		SyntheticJvmType syntheticType = (SyntheticJvmType)result;
		then(syntheticType.getTypeName()).isEqualTo("Root");
		then(syntheticType.getMembers()).hasSize(2);

		List<SyntheticMember> members = syntheticType.getMembers();
		then(members).extracting(SyntheticMember::getName).containsExactlyInAnyOrder("name", "age");
	}

	@Test
	void parseShouldHandleArrayType() {
		// given
		String schema = "{ \"type\": \"array\", \"items\": { \"type\": \"string\" } }";

		// when
		JvmType result = parser.parse(schema, context);

		// then
		then(result.getRawType()).isEqualTo(List.class);
		then(result.getTypeVariables()).hasSize(1);
		then(result.getTypeVariables().get(0).getRawType()).isEqualTo(String.class);
	}

	@Test
	void parseShouldHandleNestedObjects() {
		// given
		String schema = "{ \"type\": \"object\", \"properties\": { "
			+ "\"address\": { \"type\": \"object\", \"properties\": { "
			+ "\"city\": { \"type\": \"string\" } "
			+ "} } } }";

		// when
		JvmType result = parser.parse(schema, context);

		// then
		then(result).isInstanceOf(SyntheticJvmType.class);
		SyntheticJvmType rootType = (SyntheticJvmType)result;
		then(rootType.getMembers()).hasSize(1);

		SyntheticMember addressMember = rootType.getMembers().get(0);
		then(addressMember.getName()).isEqualTo("address");
		then(addressMember.getType()).isInstanceOf(SyntheticJvmType.class);

		SyntheticJvmType addressType = (SyntheticJvmType)addressMember.getType();
		then(addressType.getMembers()).hasSize(1);
		then(addressType.getMembers().get(0).getName()).isEqualTo("city");
	}

	@Test
	void parseShouldThrowExceptionForInvalidJson() {
		// given
		String invalidJson = "{ \"type\": }";

		// when & then
		thenThrownBy(() -> parser.parse(invalidJson, context))
			.isInstanceOf(TypeParseException.class);
	}

	@Test
	void parseShouldThrowExceptionForMissingType() {
		// given
		String noType = "{ \"properties\": {} }";

		// when & then
		thenThrownBy(() -> parser.parse(noType, context))
			.isInstanceOf(TypeParseException.class)
			.hasMessageContaining("type");
	}
}
