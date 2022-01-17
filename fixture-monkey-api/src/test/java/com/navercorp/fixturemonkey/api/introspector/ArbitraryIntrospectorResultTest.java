package com.navercorp.fixturemonkey.api.introspector;

import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.Test;

class ArbitraryIntrospectorResultTest {
	@Test
	void equalsEmptyWithNull() {
		ArbitraryIntrospectorResult sut = new ArbitraryIntrospectorResult(null);
		then(ArbitraryIntrospectorResult.EMPTY.equals(sut)).isTrue();
	}
}
