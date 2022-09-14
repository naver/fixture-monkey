package com.navercorp.fixturemonkey.test;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.LabMonkey;
import net.jqwik.api.Example;

import static org.assertj.core.api.BDDAssertions.then;

public class DebugMonkeyTest {
	private static final LabMonkey SUT = LabMonkey.labMonkey();

	@Example
	void sampleWithType() {
		// when
		ArbitraryBuilder<FixtureMonkeyV04TestSpecs.ComplexObject> builder = SUT.giveMeBuilder(FixtureMonkeyV04TestSpecs.ComplexObject.class);

		FixtureMonkeyV04TestSpecs.ComplexObject actual = builder
			.set("str", "test")
			.set("integer", 100)
			.sample();

		System.out.println("-------");
		FixtureMonkeyV04TestSpecs.ComplexObject actual2 = SUT.giveMeBuilder(FixtureMonkeyV04TestSpecs.ComplexObject.class)
			.set("str2", "test2")
			.sample();
		// then
		then(actual.getList()).isNotNull();
		then(actual.getMap()).isNotNull();
		then(actual.getMapEntry()).isNotNull();
	}

}
