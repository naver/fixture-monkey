package com.navercorp.fixturemonkey.docs.plugins

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.setExp
import com.navercorp.fixturemonkey.kotlin.into
import com.navercorp.fixturemonkey.kotlin.sizeExp
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class KotlinPluginFeaturesTest {

	data class Product(
		val id: Long,
		val name: String,
		val price: Long,
		val options: List<String>
	)

	data class Order(
		val product: Product
	)

	private val fixtureMonkey = FixtureMonkey.builder()
		.plugin(KotlinPlugin())
		.build()

	@Test
	fun quickExample() {
		// given
		val product: Product = fixtureMonkey.giveMeOne()

		// then
		then(product).isNotNull
	}

	@Test
	fun customizeWithTypeSafeReferences() {
		// when
		val custom = fixtureMonkey.giveMeKotlinBuilder<Product>()
			.setExp(Product::name, "Fixture Monkey")
			.setExp(Product::price, 29_900L)
			.sizeExp(Product::options, 3)
			.sample()

		// then
		then(custom.name).isEqualTo("Fixture Monkey")
		then(custom.price).isEqualTo(29_900L)
		then(custom.options).hasSize(3)
	}

	@Test
	fun nestedPropertyReference() {
		// when
		val order = fixtureMonkey.giveMeKotlinBuilder<Order>()
			.setExp(Order::product into Product::name, "Fixture Monkey")
			.sample()

		// then
		then(order.product.name).isEqualTo("Fixture Monkey")
	}
}
