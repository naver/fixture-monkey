package com.navercorp.fixturemonkey.tests.java;

import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.random.Randoms;

class SeedInitializationTest {

	@Test
	void readsSeedFromFile() {
		// given
		long fileSeed = 123456789L;

		// when
		FixtureMonkey.builder().useExperimental(options -> options.fileSeed()).build();

		// then
		then(Randoms.currentSeed()).isEqualTo(fileSeed);
	}

	@Test
	void builderSeedOverridesFileSeed() {
		// given
		long builderSeed = 987654321L;

		// when
		FixtureMonkey.builder().seed(builderSeed).build();

		// then
		then(Randoms.currentSeed()).isEqualTo(builderSeed);
	}

}
