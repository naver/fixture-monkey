package com.navercorp.fixturemonkey.api.introspector;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitraries;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
final class BooleanTypeIntrospector implements ArbitraryTypeIntrospector {
	static final BooleanTypeIntrospector INSTANCE = new BooleanTypeIntrospector();

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryIntrospectorContext context) {
		Class<?> type = context.getType();
		if (type != boolean.class && type != Boolean.class) {
			return ArbitraryIntrospectorResult.EMPTY;
		}

		return new ArbitraryIntrospectorResult(Arbitraries.of(true, false));
	}
}
