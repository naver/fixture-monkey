package com.navercorp.fixturemonkey.arbitrary;

import java.lang.reflect.Field;

public interface NullableArbitraryEvaluator {
	default boolean isNullable(Field field) {
		return true;
	}
}
