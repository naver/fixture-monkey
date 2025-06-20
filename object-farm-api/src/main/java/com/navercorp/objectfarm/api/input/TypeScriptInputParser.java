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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Parser for TypeScript-like type syntax strings.
 * <p>
 * Supports simplified TypeScript type syntax:
 * <ul>
 *   <li>Primitive types: string, number, boolean, any, void, null, undefined</li>
 *   <li>Object types: {@code { name: string, age: number }}</li>
 *   <li>Array types: {@code string[]} or {@code Array<string>}</li>
 *   <li>Nested types: {@code { user: { name: string }, tags: string[] }}</li>
 * </ul>
 * <p>
 * Type mappings:
 * <ul>
 *   <li>string → String.class</li>
 *   <li>number → Double.class</li>
 *   <li>boolean → Boolean.class</li>
 *   <li>any → Object.class</li>
 *   <li>void/null/undefined → Void.class</li>
 * </ul>
 * <p>
 * Example:
 * <pre>{@code
 * { name: string, age: number, items: { id: number, label: string }[] }
 * }</pre>
 */
public final class TypeScriptInputParser implements TypeInputParser {
	private static final Pattern TS_OBJECT_PATTERN = Pattern.compile("^\\s*\\{\\s*\\w+\\s*:");

	private static final Map<String, Class<?>> TYPE_MAPPING = createTypeMapping();

	private static Map<String, Class<?>> createTypeMapping() {
		Map<String, Class<?>> map = new HashMap<>();
		map.put("string", String.class);
		map.put("number", Double.class);
		map.put("boolean", Boolean.class);
		map.put("any", Object.class);
		map.put("void", Void.class);
		map.put("null", Void.class);
		map.put("undefined", Void.class);
		map.put("object", Object.class);
		return Collections.unmodifiableMap(map);
	}

	@Override
	public boolean supports(Object input) {
		if (!(input instanceof String)) {
			return false;
		}
		String str = ((String)input).trim();
		return TS_OBJECT_PATTERN.matcher(str).find();
	}

	@Override
	public JvmType parse(Object input, TypeParseContext context) {
		String tsType = ((String)input).trim();

		try {
			TsParser parser = new TsParser(tsType);
			return parser.parseType("Root", context);
		} catch (Exception e) {
			throw new TypeParseException("Failed to parse TypeScript type: " + e.getMessage(), e);
		}
	}

	/**
	 * Simple recursive descent parser for TypeScript-like types.
	 */
	private static class TsParser {
		private final String input;
		private int pos;

		TsParser(String input) {
			this.input = input;
			this.pos = 0;
		}

		JvmType parseType(String typeName, TypeParseContext context) {
			skipWhitespace();

			if (pos >= input.length()) {
				throw new TypeParseException("Unexpected end of type definition");
			}

			char character = input.charAt(pos);

			// Object type: { ... }
			if (character == '{') {
				return parseObjectType(typeName, context);
			}

			// Primitive or named type
			String baseType = parseIdentifier();

			// Check for Array<T> syntax
			if (baseType.equals("Array")) {
				return parseGenericArray(typeName, context);
			}

			// Check for [] suffix (array)
			skipWhitespace();
			if (pos < input.length() && input.charAt(pos) == '[') {
				pos++; // skip [
				expect(']');
				JvmType elementType = resolvePrimitiveType(baseType, context);
				return new JavaType(List.class,
					Collections.singletonList(elementType),
					Collections.emptyList());
			}

			return resolvePrimitiveType(baseType, context);
		}

		private JvmType parseObjectType(String typeName, TypeParseContext context) {
			expect('{');
			skipWhitespace();

			if (pos < input.length() && input.charAt(pos) == '}') {
				pos++;
				return new JavaType(Object.class);
			}

			SyntheticJvmType.Builder builder = SyntheticJvmType.builder(typeName);
			List<String> memberNames = new ArrayList<>();

			while (true) {
				skipWhitespace();

				// Parse property name
				String propName = parseIdentifier();
				memberNames.add(propName);

				// Optional ?
				skipWhitespace();
				if (pos < input.length() && input.charAt(pos) == '?') {
					pos++;
				}

				// Expect :
				skipWhitespace();
				expect(':');

				// Parse property type
				JvmType propType = parseType(typeName + capitalize(propName), context);

				// Check for array suffix after nested type
				skipWhitespace();
				if (pos < input.length() && input.charAt(pos) == '[') {
					pos++; // skip [
					expect(']');
					propType = new JavaType(List.class,
						Collections.singletonList(propType),
						Collections.emptyList());
				}

				builder.member(propName, propType);

				skipWhitespace();
				if (pos >= input.length()) {
					throw new TypeParseException("Unexpected end of object type");
				}

				char character = input.charAt(pos);
				if (character == '}') {
					pos++;
					break;
				} else if (character == ',' || character == ';') {
					pos++;
				} else {
					throw new TypeParseException("Expected ',' or '}' at position " + pos + ", found: " + character);
				}
			}

			return builder.build();
		}

		private JvmType parseGenericArray(String typeName, TypeParseContext context) {
			skipWhitespace();
			expect('<');
			JvmType elementType = parseType(typeName + "Element", context);
			skipWhitespace();
			expect('>');

			return new JavaType(List.class,
				Collections.singletonList(elementType),
				Collections.emptyList());
		}

		private JvmType resolvePrimitiveType(String typeName, TypeParseContext context) {
			// Check type aliases first
			JvmType aliased = context.getTypeAlias(typeName);
			if (aliased != null) {
				return aliased;
			}

			// Check built-in types
			Class<?> primitiveClass = TYPE_MAPPING.get(typeName.toLowerCase());
			if (primitiveClass != null) {
				return new JavaType(primitiveClass);
			}

			// Try to load as class name
			try {
				Class<?> clazz = context.getClassLoader().loadClass(typeName);
				return new JavaType(clazz);
			} catch (ClassNotFoundException e) {
				// Unknown type - return as synthetic with no members
				return SyntheticJvmType.builder(typeName).build();
			}
		}

		private String parseIdentifier() {
			skipWhitespace();
			if (pos >= input.length()) {
				throw new TypeParseException("Expected identifier at end of input");
			}

			int start = pos;
			char character = input.charAt(pos);

			if (!Character.isJavaIdentifierStart(character)) {
				throw new TypeParseException("Expected identifier at position " + pos + ", found: " + character);
			}

			pos++;
			while (pos < input.length() && Character.isJavaIdentifierPart(input.charAt(pos))) {
				pos++;
			}

			return input.substring(start, pos);
		}

		private void expect(char character) {
			skipWhitespace();
			if (pos >= input.length() || input.charAt(pos) != character) {
				String found = pos >= input.length() ? "EOF" : String.valueOf(input.charAt(pos));
				throw new TypeParseException("Expected '" + character + "' at position " + pos + ", found: " + found);
			}
			pos++;
		}

		private void skipWhitespace() {
			while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
				pos++;
			}
		}

		private String capitalize(String str) {
			if (str == null || str.isEmpty()) {
				return str;
			}
			return Character.toUpperCase(str.charAt(0)) + str.substring(1);
		}
	}
}
