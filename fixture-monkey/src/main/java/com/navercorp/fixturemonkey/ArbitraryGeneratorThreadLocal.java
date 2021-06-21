package com.navercorp.fixturemonkey;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

final class ArbitraryGeneratorThreadLocal {
	private static final ThreadLocal<Boolean> UNIQUE_SCOPE = new ThreadLocal<>();
	@SuppressWarnings("rawtypes")
	private static final ThreadLocal<Map<Class, Set>> USED_VALUES = new ThreadLocal<>();

	public static void setUniqueScope(boolean uniqueScope) {
		UNIQUE_SCOPE.set(uniqueScope);
	}

	public static boolean isUniqueScope() {
		return UNIQUE_SCOPE.get() == Boolean.TRUE;
	}

	public static void closeUniqueScope() {
		UNIQUE_SCOPE.remove();
		USED_VALUES.remove();
	}

	public static <T> Set<T> getUniqueValues(Class<T> type) {
		if (!isUniqueScope()) {
			return Collections.emptySet();
		}

		return Collections.unmodifiableSet(getTypeUniqueValues(type));
	}

	@SuppressWarnings("unchecked")
	public static <T> void addUniqueValue(@Nullable T value) {
		if (value == null) {
			return;
		}

		Set<T> uniqueValues = getTypeUniqueValues((Class<T>) value.getClass());
		uniqueValues.add(value);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static <T> Set<T> getTypeUniqueValues(Class<T> type) {
		Map<Class, Set> uniqueValues = USED_VALUES.get();
		if (uniqueValues == null) {
			uniqueValues = new HashMap<>();
			USED_VALUES.set(uniqueValues);
		}

		return uniqueValues.computeIfAbsent(type, k -> ConcurrentHashMap.newKeySet());
	}
}
