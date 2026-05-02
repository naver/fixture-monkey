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
import java.util.List;

import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Registry for {@link TypeInputParser} implementations.
 * <p>
 * Maintains a list of parsers and provides automatic parser selection based on input type.
 * Parsers are checked in registration order.
 * <p>
 * Example usage:
 * <pre>{@code
 * // Create with default parsers
 * TypeInputParserRegistry registry = TypeInputParserRegistry.defaults();
 *
 * // Parse any supported input
 * JvmType type = registry.parse(User.class, TypeParseContext.defaults());
 *
 * // Create with custom parsers
 * TypeInputParserRegistry customRegistry = TypeInputParserRegistry.builder()
 *     .register(new JavaTypeInputParser())
 *     .register(new JsonSchemaInputParser())
 *     .build();
 * }</pre>
 */
public final class TypeInputParserRegistry {
	private final List<TypeInputParser> parsers;

	private TypeInputParserRegistry(List<TypeInputParser> parsers) {
		this.parsers = new ArrayList<>(parsers);
	}

	/**
	 * Creates a registry with default parsers.
	 * <p>
	 * Default parsers (checked in order):
	 * <ul>
	 *   <li>{@link JavaTypeInputParser} - JvmType, Class, Type, AnnotatedType</li>
	 *   <li>{@link JsonSchemaInputParser} - JSON Schema format</li>
	 *   <li>{@link TypeScriptInputParser} - TypeScript-like syntax</li>
	 * </ul>
	 *
	 * @return the default registry
	 */
	public static TypeInputParserRegistry defaults() {
		return builder()
			.register(new JavaTypeInputParser())
			.register(new JsonSchemaInputParser())
			.register(new TypeScriptInputParser())
			.build();
	}

	/**
	 * Creates a new builder for TypeInputParserRegistry.
	 *
	 * @return a new builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Parses the input using the first matching parser.
	 *
	 * @param input the input to parse
	 * @param context the parsing context
	 * @return the parsed JvmType
	 * @throws TypeParseException if no parser supports the input
	 */
	public JvmType parse(Object input, TypeParseContext context) {
		for (TypeInputParser parser : parsers) {
			if (parser.supports(input)) {
				return parser.parse(input, context);
			}
		}
		throw new TypeParseException("No parser found for input type: " + input.getClass().getName());
	}

	/**
	 * Parses the input using default context.
	 *
	 * @param input the input to parse
	 * @return the parsed JvmType
	 * @throws TypeParseException if no parser supports the input
	 */
	public JvmType parse(Object input) {
		return parse(input, TypeParseContext.defaults());
	}

	/**
	 * Returns all registered parsers in registration order.
	 *
	 * @return list of parsers
	 */
	public List<TypeInputParser> getParsers() {
		return new ArrayList<>(parsers);
	}

	/**
	 * Builder for creating TypeInputParserRegistry instances.
	 */
	public static final class Builder {
		private final List<TypeInputParser> parsers = new ArrayList<>();

		private Builder() {
		}

		/**
		 * Registers a parser.
		 *
		 * @param parser the parser to register
		 * @return this builder
		 */
		public Builder register(TypeInputParser parser) {
			this.parsers.add(parser);
			return this;
		}

		/**
		 * Builds the TypeInputParserRegistry.
		 *
		 * @return the built registry
		 */
		public TypeInputParserRegistry build() {
			return new TypeInputParserRegistry(parsers);
		}
	}
}
