package com.navercorp.fixturemonkey.test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.LabMonkey;
import com.navercorp.fixturemonkey.api.generator.FactoryPropertyGenerator;
import com.navercorp.fixturemonkey.api.introspector.FactoryMethodArbitraryIntrospector;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04FactoryMethodGenerateTestSpecs.FactoryMethodSpec;

class FixtureMonkeyV04FactoryMethodGenerateTest {
	private static final LabMonkey SUT = LabMonkey.labMonkeyBuilder()
		.pushAssignableTypePropertyGenerator(FactoryMethodSpec.class, new FactoryPropertyGenerator())
		.pushExactTypeArbitraryIntrospector(FactoryMethodSpec.class, FactoryMethodArbitraryIntrospector.INSTANCE)
		.build();

	@Property
	void sample() {
		// when
		FactoryMethodSpec actual = SUT.giveMeOne(FactoryMethodSpec.class);

		then(actual.getType()).isNotNull();
	}

	@Property
	void set() {
		String expected = "test";

		String actual = SUT.giveMeBuilder(FactoryMethodSpec.class)
			.set("arg0", expected)
			.sample()
			.getValue();

		then(actual).isEqualTo(expected);
	}

	@Property
	void setNotFactoryParameterNotAffected() {
		String expected = "type4";

		String actual = SUT.giveMeBuilder(FactoryMethodSpec.class)
			.set("arg1", expected)
			.sample()
			.getType();

		then(actual).isNotEqualTo(expected);
	}

	@Property
	void setNotFactoryParameterFixed() {
		thenNoException()
			.isThrownBy(() -> SUT.giveMeBuilder(FactoryMethodSpec.class)
				.fixed()
				.sample());
	}
}
