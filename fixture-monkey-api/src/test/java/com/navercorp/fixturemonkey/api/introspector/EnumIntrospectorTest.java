package com.navercorp.fixturemonkey.api.introspector;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Collections;

import org.junit.jupiter.api.Test;

class EnumIntrospectorTest {
	@Test
	void introspect() {
		// given
		ArbitraryIntrospectorContext context = new ArbitraryIntrospectorContext(
			Seasons.class,
			"season",
			null,
			Collections.emptyList()
		);

		// when
		ArbitraryIntrospectorResult actual = EnumIntrospector.INSTANCE.introspect(context);

		then(actual.getValue()).isNotNull();
		then(actual.getValue().sample()).isExactlyInstanceOf(Seasons.class);
	}

	enum Seasons {
		SPRING,
		SUMMER,
		FALL,
		WINTER
	}
}
