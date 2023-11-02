package com.navercorp.fixturemonkey.util;

public abstract class StringUtils {
	public static boolean isBlank(String str) {
		return str == null || str.trim().isEmpty();
	}

	public static boolean isNotBlank(String str) {
		return !isBlank(str);
	}
}
