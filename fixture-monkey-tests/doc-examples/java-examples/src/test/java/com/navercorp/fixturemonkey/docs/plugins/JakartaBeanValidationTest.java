package com.navercorp.fixturemonkey.docs.plugins;

import static org.assertj.core.api.BDDAssertions.then;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import lombok.Value;
import org.junit.jupiter.api.Test;

class JakartaBeanValidationTest {

	@Value
	public static class Product {
		@Min(1)
		long id;

		@NotBlank
		String productName;

		@Max(100000)
		long price;

		@Size(min = 3)
		List<@NotBlank String> options;

		@Past
		Instant createdAt;
	}

	@Test
	void test() {
		// given
		FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.plugin(new JakartaValidationPlugin())
			.defaultNotNull(true)
			.build();

		// when
		Product actual = fixtureMonkey.giveMeOne(Product.class);

		// then
		then(actual.getId()).isGreaterThanOrEqualTo(1);
		then(actual.getProductName()).isNotBlank();
		then(actual.getPrice()).isLessThanOrEqualTo(100000);
		then(actual.getOptions().size()).isGreaterThanOrEqualTo(3);
		then(actual.getOptions()).allSatisfy(it -> then(it).isNotBlank());
		then(actual.getCreatedAt()).isBefore(Instant.now());
	}
}
