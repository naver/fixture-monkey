package com.navercorp.fixturemonkey.tests.java;

import static org.assertj.core.api.BDDAssertions.then;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.random.Randoms;

class SeedInitializationTest {
	private static final Path ROOT_SEED_FILE = Paths.get(".fixture-monkey-seed");

	@BeforeAll
	static void setUpSeedFile() throws IOException {
		Files.write(ROOT_SEED_FILE, "123456789".getBytes());
	}

	@AfterAll
	static void cleanUpSeedFile() throws IOException {
		Files.deleteIfExists(ROOT_SEED_FILE);
	}

	@Test
	void readsSeedFromFile() {
		// given
		long fileSeed = 123456789L;

		// when
		FixtureMonkey.builder().useExperimental("fileSeed").build();

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
