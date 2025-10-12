package com.navercorp.fixturemonkey.api.random;

import static org.assertj.core.api.BDDAssertions.then;

import net.jqwik.api.Property;

class DeterministicRandomTest {

	@Property
	void basicTest() {
		DeterministicRandom deterministicRandom = new DeterministicRandom(12345L);
		then(deterministicRandom).isNotNull();
	}
}
