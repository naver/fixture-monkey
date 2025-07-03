package com.navercorp.fixturemonkey.tests.java;

import static org.assertj.core.api.BDDAssertions.then;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.random.Randoms;

class SeedInitializationTest {
	private static final String SEED_FILE = ".fixture-monkey-seed";
	private static final long FILE_SEED = 123456789L;
	private static final long BUILDER_SEED = 987654321L;

	@BeforeEach
	void setUp() throws IOException {
		Files.write(Paths.get(SEED_FILE), String.valueOf(FILE_SEED).getBytes());
	}

	@AfterEach
	void tearDown() throws IOException {
		Files.deleteIfExists(Paths.get(SEED_FILE));
	}

	@Test
	void readsSeedFromFile() {
		// given
		// seed file is created in setUp()

		// when
		FixtureMonkey.builder().build();

		// then
		then(Randoms.currentSeed()).isEqualTo(FILE_SEED);

		// TODO: Change to use .giveMeOne() method in future tests
	}

	@Test
	void builderSeedOverridesFileSeed() {
		// given
		// seed file is created in setUp()

		// when
		FixtureMonkey.builder().seed(BUILDER_SEED).build();

		// then
		then(Randoms.currentSeed()).isEqualTo(BUILDER_SEED);

		// TODO: Change to use .giveMeOne() method in future tests
	}
}
