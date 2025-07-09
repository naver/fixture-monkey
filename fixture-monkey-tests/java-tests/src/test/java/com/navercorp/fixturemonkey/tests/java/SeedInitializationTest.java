package com.navercorp.fixturemonkey.tests.java;

import static org.assertj.core.api.BDDAssertions.then;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.random.Randoms;

class SeedInitializationTest {

	@Test
	void readsSeedFromFile() throws IOException {
		// given
		String seedFile = ".fixture-monkey-seed";
		long fileSeed = 123456789L;
		Files.write(Paths.get(seedFile), String.valueOf(fileSeed).getBytes());
		try {
			// when
			FixtureMonkey.builder().build();
			// then
			then(Randoms.currentSeed()).isEqualTo(fileSeed);
		} finally {
			Files.deleteIfExists(Paths.get(seedFile));
		}
	}

	@Test
	void builderSeedOverridesFileSeed() throws IOException {
		// given
		String seedFile = ".fixture-monkey-seed";
		long fileSeed = 123456789L;
		long builderSeed = 987654321L;
		Files.write(Paths.get(seedFile), String.valueOf(fileSeed).getBytes());
		try {
			// when
			FixtureMonkey.builder().seed(builderSeed).build();
			// then
			then(Randoms.currentSeed()).isEqualTo(builderSeed);
		} finally {
			Files.deleteIfExists(Paths.get(seedFile));
		}
	}
}
