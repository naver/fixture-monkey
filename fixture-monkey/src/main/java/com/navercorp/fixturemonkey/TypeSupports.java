package com.navercorp.fixturemonkey;

import java.math.BigDecimal;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TypeSupports {
	private TypeSupports() {
	}

	private static Map<Class<?>, Class<?>> WRAPPER_PRIMITIVE_CLASS_MAPPER;

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

	public static <T, U> boolean isSameType(Class<T> clazz1, Class<U> clazz2) {
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
}
