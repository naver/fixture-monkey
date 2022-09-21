package com.navercorp.fixturemonkey.test;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.LabMonkey;
import com.navercorp.fixturemonkey.LabMonkeyBuilder;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Example;

import static org.assertj.core.api.BDDAssertions.then;

public class DebugInfoObserverTest {

	@Example
	void sample() {
		// when
//		LabMonkey SUT = LabMonkey.create();
		LabMonkey SUT = LabMonkey.labMonkeyBuilder()
			//.register(String.class, monkey -> monkey.giveMeBuilder(String.class).set("$", "test").setLazy("$", ()->Arbitraries.strings()))
			.build();

		FixtureMonkeyV04TestSpecs.SimpleObject simpleObject = SUT.giveMeBuilder(FixtureMonkeyV04TestSpecs.SimpleObject.class)
			.set("str", "test")
			.size("strList", 6)
//			.apply((it, builder) -> builder.set("str", String.valueOf(it.getStrList().get(0))))
//			.acceptIf(
//				it -> true,
//				it -> it.setNotNull("str")
//			)
			.sample();
		then(simpleObject);
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
