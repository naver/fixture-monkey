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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Parser for JSON Schema format strings.
 * <p>
 * Supports a subset of JSON Schema for type definition:
 * <ul>
 *   <li>Primitive types: string, integer, number, boolean, null</li>
 *   <li>Object types with properties</li>
 *   <li>Array types with items</li>
 * </ul>
 * <p>
 * Example schema:
 * <pre>{@code
 * {
 *   "type": "object",
 *   "properties": {
 *     "name": { "type": "string" },
 *     "age": { "type": "integer" },
 *     "tags": { "type": "array", "items": { "type": "string" } }
 *   }
 * }
 * }</pre>
 */
public final class JsonSchemaInputParser implements TypeInputParser {
	private static final String JSON_SCHEMA_MARKER = "\"type\"";

	@Override
	public boolean supports(Object input) {
		if (!(input instanceof String)) {
			return false;
		}
		String str = ((String)input).trim();
		return str.startsWith("{") && str.contains(JSON_SCHEMA_MARKER);
	}

	@Override
	public JvmType parse(Object input, TypeParseContext context) {
		String schemaJson = ((String)input).trim();

		try {
			Map<String, Object> schema = parseJsonObject(schemaJson);
			return convertToJvmType(schema, "Root", context);
		} catch (Exception e) {
			throw new TypeParseException("Failed to parse JSON Schema: " + e.getMessage(), e);
		}
	}

	private JvmType convertToJvmType(Map<String, Object> schema, String typeName, TypeParseContext context) {
		String type = (String)schema.get("type");

		if (type == null) {
			throw new TypeParseException("JSON Schema must have a 'type' property");
		}

		switch (type) {
			case "string":
				return new JavaType(String.class);
			case "integer":
				return new JavaType(Integer.class);
			case "number":
				return new JavaType(Double.class);
			case "boolean":
				return new JavaType(Boolean.class);
			case "null":
				return new JavaType(Void.class);
			case "array":
				return parseArrayType(schema, typeName, context);
			case "object":
				return parseObjectType(schema, typeName, context);
			default:
				throw new TypeParseException("Unsupported JSON Schema type: " + type);
		}
	}

	@SuppressWarnings("unchecked")
	private JvmType parseArrayType(Map<String, Object> schema, String typeName, TypeParseContext context) {
		Object items = schema.get("items");
		if (items == null) {
			// Array without items definition - use Object
			return new JavaType(List.class,
				java.util.Collections.singletonList(new JavaType(Object.class)),
				java.util.Collections.emptyList());
		}

		JvmType itemType;
		if (items instanceof Map) {
			itemType = convertToJvmType((Map<String, Object>)items, typeName + "Item", context);
		} else {
			itemType = new JavaType(Object.class);
		}

		return new JavaType(List.class,
			java.util.Collections.singletonList(itemType),
			java.util.Collections.emptyList());
	}

	@SuppressWarnings("unchecked")
	private JvmType parseObjectType(Map<String, Object> schema, String typeName, TypeParseContext context) {
		Object properties = schema.get("properties");

		if (properties == null || !(properties instanceof Map)) {
			// Object without properties - return as generic Object
			return new JavaType(Object.class);
		}

		Map<String, Object> propsMap = (Map<String, Object>)properties;
		SyntheticJvmType.Builder builder = SyntheticJvmType.builder(typeName);

		for (Map.Entry<String, Object> entry : propsMap.entrySet()) {
			String propName = entry.getKey();
			Object propSchema = entry.getValue();

			if (propSchema instanceof Map) {
				JvmType propType = convertToJvmType((Map<String, Object>)propSchema,
					typeName + capitalize(propName), context);
				builder.member(propName, propType);
			}
		}

		return builder.build();
	}

	private String capitalize(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}

	// Simple JSON parser
	@SuppressWarnings("unchecked")
	private Map<String, Object> parseJsonObject(String json) {
		JsonParser parser = new JsonParser(json);
		Object result = parser.parseValue();
		if (!(result instanceof Map)) {
			throw new TypeParseException("Expected JSON object at root level");
		}
		return (Map<String, Object>)result;
	}

	/**
	 * Simple recursive descent JSON parser.
	 */
	private static class JsonParser {
		private final String input;
		private int pos;

		JsonParser(String input) {
			this.input = input;
			this.pos = 0;
		}

		Object parseValue() {
			skipWhitespace();
			if (pos >= input.length()) {
				throw new TypeParseException("Unexpected end of JSON");
			}

			char character = input.charAt(pos);
			if (character == '{') {
				return parseObject();
			} else if (character == '[') {
				return parseArray();
			} else if (character == '"') {
				return parseString();
			} else if (character == 't' || character == 'f') {
				return parseBoolean();
			} else if (character == 'n') {
				return parseNull();
			} else if (character == '-' || Character.isDigit(character)) {
				return parseNumber();
			} else {
				throw new TypeParseException("Unexpected character at position " + pos + ": " + character);
			}
		}

		private Map<String, Object> parseObject() {
			Map<String, Object> result = new HashMap<>();
			expect('{');
			skipWhitespace();

			if (pos < input.length() && input.charAt(pos) == '}') {
				pos++;
				return result;
			}

			while (true) {
				skipWhitespace();
				String key = parseString();
				skipWhitespace();
				expect(':');
				Object value = parseValue();
				result.put(key, value);

				skipWhitespace();
				if (pos >= input.length()) {
					throw new TypeParseException("Unexpected end of JSON in object");
				}
				char character = input.charAt(pos);
				if (character == '}') {
					pos++;
					break;
				} else if (character == ',') {
					pos++;
				} else {
					throw new TypeParseException("Expected ',' or '}' at position " + pos);
				}
			}

			return result;
		}

		private List<Object> parseArray() {
			List<Object> result = new ArrayList<>();
			expect('[');
			skipWhitespace();

			if (pos < input.length() && input.charAt(pos) == ']') {
				pos++;
				return result;
			}

			while (true) {
				result.add(parseValue());
				skipWhitespace();

				if (pos >= input.length()) {
					throw new TypeParseException("Unexpected end of JSON in array");
				}
				char character = input.charAt(pos);
				if (character == ']') {
					pos++;
					break;
				} else if (character == ',') {
					pos++;
					skipWhitespace();
				} else {
					throw new TypeParseException("Expected ',' or ']' at position " + pos);
				}
			}

			return result;
		}

		private String parseString() {
			expect('"');
			StringBuilder sb = new StringBuilder();

			while (pos < input.length()) {
				char character = input.charAt(pos);
				if (character == '"') {
					pos++;
					return sb.toString();
				} else if (character == '\\') {
					pos++;
					if (pos >= input.length()) {
						throw new TypeParseException("Unexpected end of string escape");
					}
					char escaped = input.charAt(pos);
					switch (escaped) {
						case '"':
						case '\\':
						case '/':
							sb.append(escaped);
							break;
						case 'n':
							sb.append('\n');
							break;
						case 'r':
							sb.append('\r');
							break;
						case 't':
							sb.append('\t');
							break;
						case 'b':
							sb.append('\b');
							break;
						case 'f':
							sb.append('\f');
							break;
						default:
							sb.append(escaped);
					}
					pos++;
				} else {
					sb.append(character);
					pos++;
				}
			}

			throw new TypeParseException("Unterminated string");
		}

		private Number parseNumber() {
			int start = pos;
			if (input.charAt(pos) == '-') {
				pos++;
			}

			while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
				pos++;
			}

			boolean isDouble = false;
			if (pos < input.length() && input.charAt(pos) == '.') {
				isDouble = true;
				pos++;
				while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
					pos++;
				}
			}

			if (pos < input.length() && (input.charAt(pos) == 'e' || input.charAt(pos) == 'E')) {
				isDouble = true;
				pos++;
				if (pos < input.length() && (input.charAt(pos) == '+' || input.charAt(pos) == '-')) {
					pos++;
				}
				while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
					pos++;
				}
			}

			String numStr = input.substring(start, pos);
			if (isDouble) {
				return Double.parseDouble(numStr);
			} else {
				return Long.parseLong(numStr);
			}
		}

		private Boolean parseBoolean() {
			if (input.startsWith("true", pos)) {
				pos += 4;
				return true;
			} else if (input.startsWith("false", pos)) {
				pos += 5;
				return false;
			} else {
				throw new TypeParseException("Expected 'true' or 'false' at position " + pos);
			}
		}

		@Nullable
		private Object parseNull() {
			if (input.startsWith("null", pos)) {
				pos += 4;
				return null;
			} else {
				throw new TypeParseException("Expected 'null' at position " + pos);
			}
		}

		private void expect(char character) {
			skipWhitespace();
			if (pos >= input.length() || input.charAt(pos) != character) {
				throw new TypeParseException("Expected '" + character + "' at position " + pos);
			}
			pos++;
		}

		private void skipWhitespace() {
			while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
				pos++;
			}
		}
	}
}
