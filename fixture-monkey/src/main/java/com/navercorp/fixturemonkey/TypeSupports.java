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

package com.navercorp.fixturemonkey;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.temporal.Temporal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.platform.commons.util.ReflectionUtils;

public final class TypeSupports {
	private static final Map<Class<?>, Class<?>> WRAPPER_PRIMITIVE_CLASS_MAPPER;

	private TypeSupports() {
	}

	static {
		Map<Class<?>, Class<?>> map = new HashMap<>();
		map.put(Integer.class, int.class);
		map.put(Float.class, float.class);
		map.put(Double.class, double.class);
		map.put(Character.class, char.class);
		map.put(Long.class, long.class);
		map.put(Short.class, short.class);
		map.put(Boolean.class, boolean.class);
		map.put(Void.class, void.class);
		map.put(Byte.class, byte.class);
		WRAPPER_PRIMITIVE_CLASS_MAPPER = Collections.unmodifiableMap(map);
	}

	public static boolean isNumberType(Class<?> clazz) {
		return Number.class.isAssignableFrom(clazz);
	}

	public static boolean isFloatType(Class<?> clazz) {
		return clazz == BigDecimal.class
			|| clazz == Float.class
			|| clazz == float.class
			|| clazz == Double.class
			|| clazz == double.class;
	}

	public static boolean isDateType(Class<?> clazz) {
		return Date.class.isAssignableFrom(clazz)
			|| Temporal.class.isAssignableFrom(clazz);
	}

	public static <T, U> boolean isCompatibleType(Class<T> clazz1, Class<U> clazz2) {
		if (clazz1 == Object.class || clazz2 == Object.class) {
			return true;
		}

		if (!clazz1.isPrimitive() && !clazz2.isPrimitive()) {
			return clazz1 == clazz2;
		}

		Class<?> toPrimitiveClazz1 = WRAPPER_PRIMITIVE_CLASS_MAPPER.getOrDefault(clazz1, clazz1);
		Class<?> toPrimitiveClazz2 = WRAPPER_PRIMITIVE_CLASS_MAPPER.getOrDefault(clazz2, clazz2);
		return toPrimitiveClazz1 == toPrimitiveClazz2;
	}

	public static List<Field> extractFields(Class<?> clazz) {
		if (clazz == null) {
			return Collections.emptyList();
		}

		return ReflectionUtils.findFields(
			clazz,
			TypeSupports::availableField,
			ReflectionUtils.HierarchyTraversalMode.TOP_DOWN
		);
	}

	private static boolean availableField(Field field) {
		return !Modifier.isStatic(field.getModifiers());
	}
}
