package com.navercorp.fixturemonkey.docs.plugins;

import static org.assertj.core.api.BDDAssertions.then;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.datafaker.plugin.DataFakerPlugin;
import lombok.Value;
import org.junit.jupiter.api.Test;

class DataFakerFeaturesTest {

	@Value
	public static class User {
		String firstName;
		String lastName;
		String fullName;
	}

	@Value
	public static class Address {
		String streetAddress;
		String city;
		String country;
	}

	@Test
	void setup() {
		// given
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.plugin(new DataFakerPlugin())
			.build();

		// when
		User user = fixtureMonkey.giveMeOne(User.class);

		// then
		then(user).isNotNull();
	}
}
