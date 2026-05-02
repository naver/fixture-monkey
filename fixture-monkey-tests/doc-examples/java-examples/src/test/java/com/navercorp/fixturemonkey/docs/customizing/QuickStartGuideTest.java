package com.navercorp.fixturemonkey.docs.customizing;

import static org.assertj.core.api.BDDAssertions.then;

import java.math.BigDecimal;
import java.util.List;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.Value;
import org.junit.jupiter.api.Test;

class QuickStartGuideTest {

	@Value
	public static class Product {
		String name;
		BigDecimal price;
		boolean available;
	}

	@Value
	public static class Order {
		List<Product> products;
	}

	@Value
	public static class Address {
		String street;
		String city;
	}

	@Value
	public static class Customer {
		String name;
		Address address;
	}

	private static final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@Test
	void keyMethods() {
		// given
		Product product = fixtureMonkey.giveMeBuilder(Product.class)
			.set("name", "Smartphone")
			.set("price", new BigDecimal(499))
			.sample();

		Order order = fixtureMonkey.giveMeBuilder(Order.class)
			.size("products", 2)
			.set("products[0].name", "Laptop")
			.sample();

		// then
		then(product.getName()).isEqualTo("Smartphone");
		then(order.getProducts()).hasSize(2);
	}

	@Test
	void settingPropertyValues() {
		Product product = fixtureMonkey.giveMeBuilder(Product.class)
			.set("name", "Smartphone")
			.set("price", new BigDecimal("499.99"))
			.set("available", true)
			.sample();

		then(product.getName()).isEqualTo("Smartphone");
	}

	@Test
	void settingNullValues() {
		Product nullNameProduct = fixtureMonkey.giveMeBuilder(Product.class)
			.setNull("name")
			.sample();

		then(nullNameProduct.getName()).isNull();
	}

	@Test
	void workingWithCollections() {
		Order orderWith2Products = fixtureMonkey.giveMeBuilder(Order.class)
			.size("products", 2)
			.set("products[0].name", "Laptop")
			.sample();

		then(orderWith2Products.getProducts()).hasSize(2);
		then(orderWith2Products.getProducts().get(0).getName()).isEqualTo("Laptop");
	}

	@Test
	void customizingNestedObjects() {
		Customer customer = fixtureMonkey.giveMeBuilder(Customer.class)
			.set("name", "John Doe")
			.set("address.street", "123 Main Street")
			.set("address.city", "New York")
			.sample();

		then(customer.getName()).isEqualTo("John Doe");
		then(customer.getAddress().getStreet()).isEqualTo("123 Main Street");
	}

	@Test
	void collectionSizeFirst() {
		Order orderCorrect = fixtureMonkey.giveMeBuilder(Order.class)
			.size("products", 1)
			.set("products[0].name", "Laptop")
			.sample();

		then(orderCorrect.getProducts().get(0).getName()).isEqualTo("Laptop");
	}

	@Test
	void ensureNotNull() {
		Product nonNullProduct = fixtureMonkey.giveMeBuilder(Product.class)
			.setNotNull("name")
			.setNotNull("price")
			.sample();

		then(nonNullProduct.getName()).isNotNull();
		then(nonNullProduct.getPrice()).isNotNull();
	}
}
