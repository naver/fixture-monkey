package com.navercorp.fixturemonkey.docs.getstarted;

import static org.assertj.core.api.BDDAssertions.then;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.Value;
import org.junit.jupiter.api.Test;

class BasicCreationTest {

	@Value
	public static class Product {
		long id;
		String productName;
		long price;
		List<String> options;
		Instant createdAt;
		ProductType productType;
		Map<Integer, String> merchantInfo;
	}

	public enum ProductType {
		ELECTRONICS,
		CLOTHING,
		FOOD
	}

	@Test
	void test() {
		// given
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.build();

		// when
		Product actual = fixtureMonkey.giveMeOne(Product.class);

		// then
		then(actual).isNotNull();
	}
}
