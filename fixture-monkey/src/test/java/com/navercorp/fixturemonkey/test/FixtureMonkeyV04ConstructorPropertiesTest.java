package com.navercorp.fixturemonkey.test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.LabMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04ConstructorPropertiesTestSpecs.ConstructorComplexObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04ConstructorPropertiesTestSpecs.ConstructorSimpleObject;

class FixtureMonkeyV04ConstructorPropertiesTest {
	private static final LabMonkey SUT = LabMonkey.labMonkeyBuilder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.build();

	@Property
	void sampleConstructorSimpleObject() {
		// when
		String actual = SUT.giveMeOne(ConstructorSimpleObject.class)
			.getFixedValue();

		then(actual).isEqualTo("test");
	}

	@Property
	void sampleConstructorComplexObject() {
		// when
		String actual = SUT.giveMeBuilder(ConstructorComplexObject.class)
			.setNotNull("value")
			.sample()
			.getValue()
			.getFixedValue();

		then(actual).isEqualTo("test");
	}

	@Property
	void setNotConstructorParameterNotAffected() {
		// when
		boolean actual = SUT.giveMeBuilder(ConstructorSimpleObject.class)
			.setNotNull("value")
			.set("isNull", true)
			.sample()
			.isNull();

		then(actual).isFalse();
	}

	@Property
	void setConstructorParameterAffects() {
		// when
		boolean actual = SUT.giveMeBuilder(ConstructorSimpleObject.class)
			.set("value", "length5")
			.sample()
			.isLengthOverFive();

		then(actual).isTrue();
	}

	@Property
	void fixed() {
		thenNoException()
			.isThrownBy(() -> SUT.giveMeBuilder(ConstructorComplexObject.class).fixed().sample());
	}
}
