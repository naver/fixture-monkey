package com.navercorp.fixturemonkey.docs.getstarted;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.Value;
import org.junit.jupiter.api.Test;

class TipsTest {

	@Value
	public static class Product {
		long id;
		String name;
		long price;
		String category;
		int stock;
		List<String> options;
	}

	private static final FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.build();

	@Test
	void meaningfulTestData() {
		Product product = FIXTURE_MONKEY.giveMeBuilder(Product.class)
			.set("price", 1000L)
			.set("name", "Premium Product")
			.set("category", "ELECTRONICS")
			.set("stock", 50)
			.sample();

		then(product).isNotNull();
	}

	@Test
	void readableTests() {
		Product product = FIXTURE_MONKEY.giveMeBuilder(Product.class)
			.set("price", 2000L)
			.set("category", "PREMIUM")
			.sample();

		then(product).isNotNull();
	}

	@Test
	void handleCollections() {
		Product product = FIXTURE_MONKEY.giveMeBuilder(Product.class)
			.size("options", 3)
			.set("options[1]", "red")
			.sample();

		then(product.getOptions()).hasSize(3);
		then(product.getOptions().get(1)).isEqualTo("red");
	}

	@Test
	void reuseFixtureMonkey() {
		Product product1 = FIXTURE_MONKEY.giveMeBuilder(Product.class).sample();
		Product product2 = FIXTURE_MONKEY.giveMeBuilder(Product.class).sample();

		then(product1).isNotNull();
		then(product2).isNotNull();
	}

	private static final ArbitraryBuilder<Product> PREMIUM_PRODUCT_BUILDER =
		FIXTURE_MONKEY.giveMeBuilder(Product.class)
			.set("category", "PREMIUM")
			.set("price", 1000L);

	@Test
	void reuseArbitraryBuilder() {
		Product discountProduct = PREMIUM_PRODUCT_BUILDER
			.set("price", 2000L)
			.sample();

		Product shippingProduct = PREMIUM_PRODUCT_BUILDER
			.set("price", 5000L)
			.sample();

		then(discountProduct.getCategory()).isEqualTo("PREMIUM");
		then(shippingProduct.getCategory()).isEqualTo("PREMIUM");
	}

	@Test
	void startSimple() {
		Product product = FIXTURE_MONKEY.giveMeBuilder(Product.class)
			.set("name", "Test Product")
			.sample();

		then(product).isNotNull();
	}
}
