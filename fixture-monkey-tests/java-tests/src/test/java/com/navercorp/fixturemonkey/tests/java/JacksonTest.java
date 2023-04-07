package com.navercorp.fixturemonkey.tests.java;

import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.thenNoException;

import org.junit.jupiter.api.RepeatedTest;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.jackson.plugin.JacksonPlugin;
import com.navercorp.fixturemonkey.tests.java.JacksonSpecs.JsonTypeInfoIdClassSpec;
import com.navercorp.fixturemonkey.tests.java.JacksonSpecs.JsonTypeInfoIdNameSpec;
import com.navercorp.fixturemonkey.tests.java.JacksonSpecs.JsonTypeInfoListSpec;
import com.navercorp.fixturemonkey.tests.java.JacksonSpecs.TypeWithAnnotationsListSpec;
import com.navercorp.fixturemonkey.tests.java.JacksonSpecs.TypeWithAnnotationsSpec;

@SuppressWarnings("rawtypes")
class JacksonTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.plugin(new JacksonPlugin())
		.defaultNotNull(true)
		.build();

	@RepeatedTest(TEST_COUNT)
	void jsonTypeInfo() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoIdNameSpec.class));
	}

	@RepeatedTest(TEST_COUNT)
	void jsonTypeListInfo() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoListSpec.class));
	}

	@RepeatedTest(TEST_COUNT)
	void jsonTypeInfoWithIdClass() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonTypeInfoIdClassSpec.class));
	}

	@RepeatedTest(TEST_COUNT)
	void jsonTypeWithoutAnnotations() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(TypeWithAnnotationsSpec.class));
	}

	@RepeatedTest(TEST_COUNT)
	void jsonTypeListWithoutAnnotations() {
		thenNoException().isThrownBy(() -> SUT.giveMeBuilder(TypeWithAnnotationsListSpec.class));
	}
}
