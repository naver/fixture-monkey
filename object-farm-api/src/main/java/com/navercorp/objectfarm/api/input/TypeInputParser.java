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

import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Interface for parsing various input formats into JvmType instances.
 * <p>
 * Implementations handle specific input formats:
 * <ul>
 *   <li>Java types (JvmType, Class&lt;?&gt;, Type, AnnotatedType)</li>
 *   <li>JSON Schema strings</li>
 *   <li>TypeScript-like syntax strings</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * TypeInputParser parser = new JavaTypeInputParser();
 * if (parser.supports(User.class)) {
 *     JvmType type = parser.parse(User.class, TypeParseContext.defaults());
 * }
 * }</pre>
 */
public interface TypeInputParser {
	/**
	 * Checks if this parser can handle the given input.
	 *
	 * @param input the input to check (JvmType, String, Class&lt;?&gt;, Type, etc.)
	 * @return true if this parser can handle the input
	 */
	boolean supports(Object input);

	/**
	 * Parses the input into a JvmType.
	 *
	 * @param input the input to parse
	 * @param context parsing context with configuration
	 * @return the parsed JvmType
	 * @throws TypeParseException if parsing fails
	 */
	JvmType parse(Object input, TypeParseContext context);
}
