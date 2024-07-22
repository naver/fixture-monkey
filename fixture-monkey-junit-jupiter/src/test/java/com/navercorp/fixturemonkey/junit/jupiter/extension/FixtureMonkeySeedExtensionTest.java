package com.navercorp.fixturemonkey.junit.jupiter.extension;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.navercorp.fixturemonkey.FixtureMonkey;

@ExtendWith(FixtureMonkeySeedExtension.class)
class FixtureMonkeySeedExtensionTest {

	@BeforeAll
	static void beforeAll() {
		long seed = System.currentTimeMillis();
		FixtureMonkeySeedExtension.setSeed(seed);
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.seed(seed)
			.build();
	}
	@Test
	void getSeedWhenTestFail() throws Exception {
		throw new Exception("Intentional failure for testing seed logging");
	}
}
