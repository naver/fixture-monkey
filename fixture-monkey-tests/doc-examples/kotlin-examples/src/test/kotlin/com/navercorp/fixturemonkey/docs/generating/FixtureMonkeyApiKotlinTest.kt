package com.navercorp.fixturemonkey.docs.generating

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeArbitrary
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMe
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class FixtureMonkeyApiKotlinTest {

	data class Product(
		val id: Long,
		val name: String,
		val price: Double
	)

	private val fixtureMonkey = FixtureMonkey.builder()
		.plugin(KotlinPlugin())
		.build()

	@Test
	fun giveMeOne() {
		val product: Product = fixtureMonkey.giveMeOne()

		val strList: List<String> = fixtureMonkey.giveMeOne()

		then(product).isNotNull
		then(strList).isNotNull
	}

	@Test
	fun giveMe() {
		val productSequence: Sequence<Product> = fixtureMonkey.giveMe()
		val strListSequence: Sequence<List<String>> = fixtureMonkey.giveMe()

		val productList: List<Product> = fixtureMonkey.giveMe<Product>(3).toList()
		val strListList: List<List<String>> = fixtureMonkey.giveMe<List<String>>(3).toList()

		then(productSequence).isNotNull
		then(strListSequence).isNotNull
		then(productList).hasSize(3)
		then(strListList).hasSize(3)
	}

	@Test
	fun giveMeBuilder() {
		val productBuilder = fixtureMonkey.giveMeBuilder<Product>()

		val strListBuilder = fixtureMonkey.giveMeBuilder<List<String>>()

		then(productBuilder.sample()).isNotNull
		then(strListBuilder.sample()).isNotNull
	}

	@Test
	fun giveMeBuilderWithInstance() {
		val product = Product(1L, "Book", 9.99)

		val productBuilder = fixtureMonkey.giveMeBuilder(product)

		then(productBuilder.sample()).isNotNull
	}

	@Test
	fun obtainInstances() {
		val productBuilder = fixtureMonkey.giveMeBuilder<Product>()

		val product = productBuilder.sample()
		val productList = productBuilder.sampleList(3)
		val productStream = productBuilder.sampleStream()

		then(product).isNotNull
		then(productList).hasSize(3)
		then(productStream).isNotNull
	}

	@Test
	fun build() {
		val productBuilder = fixtureMonkey.giveMeBuilder<Product>()

		val productArbitrary = productBuilder.build()

		then(productArbitrary).isNotNull
	}

	@Test
	fun giveMeArbitrary() {
		val productArbitrary = fixtureMonkey.giveMeArbitrary<Product>()

		val strListArbitrary = fixtureMonkey.giveMeArbitrary<List<String>>()

		then(productArbitrary).isNotNull
		then(strListArbitrary).isNotNull
	}
}
