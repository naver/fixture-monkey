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

class TypeScriptInputParserTest {

	private final TypeScriptInputParser parser = new TypeScriptInputParser();
	private final TypeParseContext context = TypeParseContext.defaults();

	@Test
	void supportsShouldReturnTrueForTsSyntax() {
		// given
		String tsType = "{ name: string }";

		// when & then
		then(parser.supports(tsType)).isTrue();
	}

	@Test
	void supportsShouldReturnFalseForNonTsSyntax() {
		// given & when & then
		then(parser.supports("just a string")).isFalse();
		then(parser.supports("{ \"type\": \"object\" }")).isFalse();  // JSON Schema
		then(parser.supports(123)).isFalse();
	}

	@Test
	void parseShouldHandleSimpleObject() {
		// given
		String tsType = "{ name: string, age: number }";

		// when
		JvmType result = parser.parse(tsType, context);

		// then
		then(result).isInstanceOf(SyntheticJvmType.class);
		SyntheticJvmType syntheticType = (SyntheticJvmType)result;
		then(syntheticType.getMembers()).hasSize(2);
		then(syntheticType.getMembers()).extracting(SyntheticMember::getName)
			.containsExactly("name", "age");
	}

	@Test
	void parseShouldMapPrimitiveTypesCorrectly() {
		// given
		String tsType = "{ str: string, num: number, bool: boolean, any: any }";

		// when
		JvmType result = parser.parse(tsType, context);

		// then
		SyntheticJvmType syntheticType = (SyntheticJvmType)result;
		List<SyntheticMember> members = syntheticType.getMembers();

		then(members.get(0).getType().getRawType()).isEqualTo(String.class);
		then(members.get(1).getType().getRawType()).isEqualTo(Double.class);
		then(members.get(2).getType().getRawType()).isEqualTo(Boolean.class);
		then(members.get(3).getType().getRawType()).isEqualTo(Object.class);
	}

	@Test
	void parseShouldHandleArraySuffix() {
		// given
		String tsType = "{ tags: string[] }";

		// when
		JvmType result = parser.parse(tsType, context);

		// then
		SyntheticJvmType syntheticType = (SyntheticJvmType)result;
		JvmType tagsType = syntheticType.getMembers().get(0).getType();
		then(tagsType.getRawType()).isEqualTo(List.class);
		then(tagsType.getTypeVariables().get(0).getRawType()).isEqualTo(String.class);
	}

	@Test
	void parseShouldHandleGenericArraySyntax() {
		// given
		String tsType = "{ items: Array<number> }";

		// when
		JvmType result = parser.parse(tsType, context);

		// then
		SyntheticJvmType syntheticType = (SyntheticJvmType)result;
		JvmType itemsType = syntheticType.getMembers().get(0).getType();
		then(itemsType.getRawType()).isEqualTo(List.class);
		then(itemsType.getTypeVariables().get(0).getRawType()).isEqualTo(Double.class);
	}

	@Test
	void parseShouldHandleNestedObjects() {
		// given
		String tsType = "{ user: { name: string, email: string } }";

		// when
		JvmType result = parser.parse(tsType, context);

		// then
		SyntheticJvmType rootType = (SyntheticJvmType)result;
		then(rootType.getMembers()).hasSize(1);

		JvmType userType = rootType.getMembers().get(0).getType();
		then(userType).isInstanceOf(SyntheticJvmType.class);

		SyntheticJvmType syntheticUserType = (SyntheticJvmType)userType;
		then(syntheticUserType.getMembers()).hasSize(2);
		then(syntheticUserType.getMembers()).extracting(SyntheticMember::getName)
			.containsExactly("name", "email");
	}

	@Test
	void parseShouldHandleNestedObjectArray() {
		// given
		String tsType = "{ items: { id: number, label: string }[] }";

		// when
		JvmType result = parser.parse(tsType, context);

		// then
		SyntheticJvmType rootType = (SyntheticJvmType)result;
		JvmType itemsType = rootType.getMembers().get(0).getType();

		then(itemsType.getRawType()).isEqualTo(List.class);
		JvmType elementType = itemsType.getTypeVariables().get(0);
		then(elementType).isInstanceOf(SyntheticJvmType.class);

		SyntheticJvmType itemType = (SyntheticJvmType)elementType;
		then(itemType.getMembers()).hasSize(2);
	}

	@Test
	void parseShouldHandleOptionalProperties() {
		// given
		String tsType = "{ name: string, nickname?: string }";

		// when
		JvmType result = parser.parse(tsType, context);

		// then
		SyntheticJvmType syntheticType = (SyntheticJvmType)result;
		then(syntheticType.getMembers()).hasSize(2);
	}

	@Test
	void parseShouldHandleSemicolonSeparator() {
		// given
		String tsType = "{ name: string; age: number }";

		// when
		JvmType result = parser.parse(tsType, context);

		// then
		SyntheticJvmType syntheticType = (SyntheticJvmType)result;
		then(syntheticType.getMembers()).hasSize(2);
	}

	@Test
	void parseShouldThrowExceptionForInvalidSyntax() {
		// given
		String invalidType = "{ name: }";

		// when & then
		thenThrownBy(() -> parser.parse(invalidType, context))
			.isInstanceOf(TypeParseException.class);
	}
}
