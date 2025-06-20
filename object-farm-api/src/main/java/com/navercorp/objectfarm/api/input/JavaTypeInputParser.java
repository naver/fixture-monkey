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

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;

import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.ObjectTypeReference;
import com.navercorp.objectfarm.api.type.Types;

/**
 * Parser for native Java type inputs.
 * <p>
 * This parser handles the following input types:
 * <ul>
 *   <li>{@link JvmType} - returned as-is</li>
 *   <li>{@link Class} - converted to JavaType</li>
 *   <li>{@link Type} - converted to JavaType (supports ParameterizedType, GenericArrayType, etc.)</li>
 *   <li>{@link AnnotatedType} - converted to JavaType preserving annotations</li>
 *   <li>{@link ObjectTypeReference} - converted to JavaType with generic type information</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * TypeInputParser parser = new JavaTypeInputParser();
 *
 * // From Class
 * JvmType type1 = parser.parse(String.class, context);
 *
 * // From JvmType (passes through)
 * JvmType type2 = parser.parse(existingJvmType, context);
 *
 * // From TypeReference (supports generics like List<String>)
 * JvmType type3 = parser.parse(new TypeReference<List<String>>() {}, context);
 * }</pre>
 */
public final class JavaTypeInputParser implements TypeInputParser {
	@Override
	public boolean supports(Object input) {
		return input instanceof JvmType
			|| input instanceof Class<?>
			|| input instanceof Type
			|| input instanceof AnnotatedType
			|| input instanceof ObjectTypeReference<?>;
	}

	@Override
	public JvmType parse(Object input, TypeParseContext context) {
		if (input instanceof JvmType) {
			return (JvmType)input;
		}

		if (input instanceof Class<?>) {
			return new JavaType((Class<?>)input);
		}

		if (input instanceof ObjectTypeReference<?>) {
			return new JavaType((ObjectTypeReference<?>)input);
		}

		if (input instanceof AnnotatedType) {
			AnnotatedType annotatedType = (AnnotatedType)input;
			return new JavaType(Types.toTypeReference(annotatedType));
		}

		if (input instanceof Type) {
			Type type = (Type)input;
			return new JavaType(Types.getActualType(type));
		}

		throw new TypeParseException("Unsupported Java type input: " + input.getClass().getName());
	}
}
