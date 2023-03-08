package com.navercorp.fixturemonkey.test;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.test.FixtureMonkeyConstructorPropertiesTestSpecs.ConstructorComplexObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyConstructorPropertiesTestSpecs.ConstructorSimpleObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyConstructorPropertiesTestSpecs.GenericValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyConstructorPropertiesTestSpecs.NoParameterConstructor;
import com.navercorp.fixturemonkey.test.FixtureMonkeyConstructorPropertiesTestSpecs.PrivateConstructor;

class FixtureMonkeyConstructorPropertiesTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
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

	@Property
	void noParameterConstructor() {
		String actual = SUT.giveMeOne(NoParameterConstructor.class)
			.getValue();

		then(actual).isEqualTo("fixed");
	}

	@Property
	void sampleGeneric() {
		String actual = SUT.giveMeBuilder(new TypeReference<GenericValue<String>>() {
			})
			.setNotNull("value")
			.sample()
			.getValue();

		then(actual).isNotNull();
	}

	@Property
	void sampleFixedGeneric() {
		String actual = SUT.giveMeBuilder(new TypeReference<GenericValue<String>>() {
			})
			.setNotNull("value")
			.fixed()
			.sample()
			.getValue();

		then(actual).isNotNull();
	}

	@Property
	void sampleWithPrivateConstructor() {
		String actual = SUT.giveMeBuilder(PrivateConstructor.class)
			.set("value", "changed")
			.sample()
			.getValue();

		then(actual).isEqualTo("fixed");
	}
}
