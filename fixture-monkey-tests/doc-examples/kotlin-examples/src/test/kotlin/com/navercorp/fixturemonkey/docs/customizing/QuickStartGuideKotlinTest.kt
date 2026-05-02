package com.navercorp.fixturemonkey.docs.customizing

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class QuickStartGuideKotlinTest {

	data class Product(
		val name: String?,
		val price: BigDecimal,
		val available: Boolean
	)

	data class Order(
		val products: List<Product>
	)

	data class Address(
		val street: String,
		val city: String
	)

	data class Customer(
		val name: String,
		val address: Address
	)

	private val fixtureMonkey = FixtureMonkey.builder()
		.plugin(KotlinPlugin())
		.defaultNotNull(true)
		.build()

	@Test
	fun keyMethods() {
		// given
		val product = fixtureMonkey.giveMeBuilder<Product>()
			.set("name", "Smartphone")
			.set("price", BigDecimal(499))
			.sample()

		val order = fixtureMonkey.giveMeBuilder<Order>()
			.size("products", 2)
			.set("products[0].name", "Laptop")
			.sample()

		// then
		then(product.name).isEqualTo("Smartphone")
		then(order.products).hasSize(2)
	}

	@Test
	fun settingPropertyValues() {
		val product = fixtureMonkey.giveMeBuilder<Product>()
			.set("name", "Smartphone")
			.set("price", BigDecimal("499.99"))
			.set("available", true)
			.sample()

		then(product.name).isEqualTo("Smartphone")
	}

	@Test
	fun settingNullValues() {
		val nullNameProduct = fixtureMonkey.giveMeBuilder<Product>()
			.setNull("name")
			.sample()

		then(nullNameProduct.name).isNull()
	}

	@Test
	fun workingWithCollections() {
		val orderWith2Products = fixtureMonkey.giveMeBuilder<Order>()
			.size("products", 2)
			.set("products[0].name", "Laptop")
			.sample()

		then(orderWith2Products.products).hasSize(2)
		then(orderWith2Products.products[0].name).isEqualTo("Laptop")
	}

	@Test
	fun customizingNestedObjects() {
		val customer = fixtureMonkey.giveMeBuilder<Customer>()
			.set("name", "John Doe")
			.set("address.street", "123 Main Street")
			.set("address.city", "New York")
			.sample()

		then(customer.name).isEqualTo("John Doe")
		then(customer.address.street).isEqualTo("123 Main Street")
	}

	@Test
	fun collectionSizeFirst() {
		val orderCorrect = fixtureMonkey.giveMeBuilder<Order>()
			.size("products", 1)
			.set("products[0].name", "Laptop")
			.sample()

		then(orderCorrect.products[0].name).isEqualTo("Laptop")
	}

	@Test
	fun ensureNotNull() {
		val nonNullProduct = fixtureMonkey.giveMeBuilder<Product>()
			.setNotNull("name")
			.setNotNull("price")
			.sample()

		then(nonNullProduct.name).isNotNull()
		then(nonNullProduct.price).isNotNull()
	}
}
