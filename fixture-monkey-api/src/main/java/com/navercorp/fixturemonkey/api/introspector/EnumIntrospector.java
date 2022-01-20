package com.navercorp.fixturemonkey.api.introspector;

import net.jqwik.api.Arbitraries;

public class EnumIntrospector implements ArbitraryTypeIntrospector {
	public static final EnumIntrospector INSTANCE = new EnumIntrospector();

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryIntrospectorContext context) {
		Class<?> type = context.getType();
		if (!type.isEnum()) {
			return ArbitraryIntrospectorResult.EMPTY;
		}

		return new ArbitraryIntrospectorResult(Arbitraries.of((Class<Enum>)type));
	}
}
