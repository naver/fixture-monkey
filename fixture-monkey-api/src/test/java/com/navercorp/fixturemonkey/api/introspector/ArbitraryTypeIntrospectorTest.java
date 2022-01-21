package com.navercorp.fixturemonkey.api.introspector;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Collections;

import org.junit.jupiter.api.Test;

class ArbitraryTypeIntrospectorTest {
	@Test
	void introspectEnumType() {
		// given
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			Season.class,
			"season",
			null,
			Collections.emptyList()
		);

		// when
		ArbitraryIntrospectorResult actual = ArbitraryTypeIntrospector.INTROSPECTORS.introspect(context);

		// then
		then(actual.getValue().sample()).isExactlyInstanceOf(Season.class);
	}

	enum Season {
		SPRING, SUMMER, FALL, WINTER
	}
}
