package com.navercorp.fixturemonkey.tests.concurrent;

import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.Map;

import org.junit.jupiter.api.RepeatedTest;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;

class ConcurrentTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.build();

	@RepeatedTest(TEST_COUNT)
	void test1() {
		JavaObject actual = SUT.giveMeOne(JavaObject.class);

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void test2() {
		JavaObject actual = SUT.giveMeOne(JavaObject.class);

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void test3() {
		JavaObject actual = SUT.giveMeOne(JavaObject.class);

		then(actual).isNotNull();
	}

	public record JavaObject(
		String value,
		Map<String, String> map
	) {

	}
}
