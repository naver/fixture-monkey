package com.navercorp.fixturemonkey.api.introspector;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitraries;

class CompositeArbitraryTypeIntrospectorTest {
	@Test
	void introspect() {
		// given
		CompositeArbitraryTypeIntrospector sut = new CompositeArbitraryTypeIntrospector(
			Arrays.asList(
				(context) -> ArbitraryIntrospectorResult.EMPTY,
				(context) -> new ArbitraryIntrospectorResult(null),
				(context) -> new ArbitraryIntrospectorResult(Arbitraries.strings()),
				(context) -> new ArbitraryIntrospectorResult(Arbitraries.integers())
			)
		);

		// when
		ArbitraryIntrospectorResult actual = sut.introspect(null);

		then(actual).isNotEqualTo(ArbitraryIntrospectorResult.EMPTY);
		then(actual.getValue()).isNotNull();
		then(actual.getValue().sample()).isExactlyInstanceOf(String.class);
	}
}
