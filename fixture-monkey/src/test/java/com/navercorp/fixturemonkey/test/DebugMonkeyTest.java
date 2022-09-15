package com.navercorp.fixturemonkey.test;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.LabMonkey;
import com.navercorp.fixturemonkey.LabMonkeyBuilder;
import net.jqwik.api.Example;

import static org.assertj.core.api.BDDAssertions.then;

public class DebugMonkeyTest {

	@Example
	void sample() {
		// when
		LabMonkey SUT = LabMonkey.labMonkeyBuilder()
			.register(String.class, monkey -> monkey.giveMeBuilder("test"))
			.build();

		FixtureMonkeyV04TestSpecs.ComplexObject complexObject = SUT.giveMeBuilder(FixtureMonkeyV04TestSpecs.ComplexObject.class)
			.set("str", "test")
			.size("strList", 6)
			.sample();

//		System.out.println("-------");
//		FixtureMonkeyV04TestSpecs.ComplexObject actual2 = SUT.giveMeBuilder(FixtureMonkeyV04TestSpecs.ComplexObject.class)
//			.set("str2", "test2")
//			.sample();
//		// then
//		then(actual.getList()).isNotNull();
//		then(actual.getMap()).isNotNull();
//		then(actual.getMapEntry()).isNotNull();
	}

}
