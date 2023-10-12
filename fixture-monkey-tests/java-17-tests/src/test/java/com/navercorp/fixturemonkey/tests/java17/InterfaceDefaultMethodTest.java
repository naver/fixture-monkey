package com.navercorp.fixturemonkey.tests.java17;

import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.RepeatedTest;

import com.navercorp.fixturemonkey.FixtureMonkey;

class InterfaceDefaultMethodTest {
	private final static FixtureMonkey SUT = FixtureMonkey.create();

	@RepeatedTest(TEST_COUNT)
	void defaultMethod() {
		String actual = SUT.giveMeOne(DefaultMethodInterface.class).defaultMethod();

		then(actual).isEqualTo("test");
	}

	public interface DefaultMethodInterface {
		default String defaultMethod() {
			return "test";
		}
	}
}
