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

package com.navercorp.fixturemonkey.api.expression;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.type.TypeCache;

public final class JavaGetterPropertyFieldNameResolver {
	private static final String GET_PREFIX = "get";
	private static final String IS_PREFIX = "is";

	@Nullable
	public String resolveFieldName(Class<?> targetClass, String methodName) {

		if (isValidField(targetClass, methodName)) {
			// class could be using property-style getters (e.g. java record)
			return methodName;
		} else if (hasPrefix(GET_PREFIX, methodName)) {
			return stripPrefixPropertyName(targetClass, methodName, GET_PREFIX.length());
		} else if (hasPrefix(IS_PREFIX, methodName)) {
			return stripPrefixPropertyName(targetClass, methodName, IS_PREFIX.length());
		}

		return null;
	}

	private static String stripPrefixPropertyName(Class<?> targetClass, String methodName, int prefixLength) {
		char[] ch = methodName.toCharArray();
		ch[prefixLength] = Character.toLowerCase(ch[prefixLength]);
		String fieldName = new String(ch, prefixLength, ch.length - prefixLength);
		return isValidField(targetClass, fieldName) ? fieldName : null;
	}

	private static boolean hasPrefix(String prefix, String methodName) {
		return methodName.startsWith(prefix) && methodName.length() > prefix.length();
	}

	private static boolean isValidField(Class<?> type, String fieldName) {
		return TypeCache.getFieldsByName(type).containsKey(fieldName);
	}
}
