package com.navercorp.fixturemonkey.docs.plugins;

import static org.assertj.core.api.BDDAssertions.then;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.jackson.plugin.JacksonPlugin;
import lombok.Value;
import org.junit.jupiter.api.Test;

class JacksonAnnotationsTest {

	@Value
	public static class Product {
		long id;

		@JsonProperty("name")
		String productName;

		long price;

		@JsonIgnore
		List<String> options;

		Instant createdAt;
	}

	@Test
	void test() {
		// given
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.plugin(new JacksonPlugin())
			.build();

		// when
		Product actual = fixtureMonkey.giveMeBuilder(Product.class)
			.set("name", "book")
			.sample();

		// then
		then(actual.getProductName()).isEqualTo("book");
		then(actual.getOptions()).isNull();
	}
}
