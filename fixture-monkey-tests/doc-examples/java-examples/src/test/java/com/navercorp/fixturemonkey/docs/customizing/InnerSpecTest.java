package com.navercorp.fixturemonkey.docs.customizing;

import static org.assertj.core.api.BDDAssertions.then;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.customizer.InnerSpec;
import lombok.Value;
import org.junit.jupiter.api.Test;

class InnerSpecTest {

	@Value
	public static class Product {
		long id;
		String name;
		BigDecimal price;
		List<String> tags;
		Map<String, String> attributes;
	}

	private static final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@Test
	void simpleExample() {
		InnerSpec productSpec = new InnerSpec()
			.property("id", 1000L)
			.property("name", "Smartphone")
			.property("price", new BigDecimal("499.99"));

		Product product = fixtureMonkey.giveMeBuilder(Product.class)
			.setInner(productSpec)
			.sample();

		then(product.getId()).isEqualTo(1000L);
		then(product.getName()).isEqualTo("Smartphone");
	}

	@Test
	void propertyMethod() {
		InnerSpec innerSpec = new InnerSpec()
			.property("id", 1000);

		Product product = fixtureMonkey.giveMeBuilder(Product.class)
			.setInner(innerSpec)
			.sample();

		then(product.getId()).isEqualTo(1000);
	}

	@Test
	void sizeMethod() {
		InnerSpec innerSpec = new InnerSpec()
			.property("tags", tags -> tags.size(3));

		Product product = fixtureMonkey.giveMeBuilder(Product.class)
			.setInner(innerSpec)
			.sample();

		then(product.getTags()).hasSize(3);
	}

	@Test
	void mapEntry() {
		InnerSpec innerSpec = new InnerSpec()
			.property("attributes", attr -> attr
				.size(1)
				.entry("color", "red")
			);

		Product product = fixtureMonkey.giveMeBuilder(Product.class)
			.setInner(innerSpec)
			.sample();

		then(product.getAttributes()).containsEntry("color", "red");
	}
}
