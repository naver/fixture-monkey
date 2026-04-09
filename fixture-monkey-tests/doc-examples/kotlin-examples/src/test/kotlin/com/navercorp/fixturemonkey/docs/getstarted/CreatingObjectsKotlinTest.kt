package com.navercorp.fixturemonkey.docs.getstarted

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.setExp
import com.navercorp.fixturemonkey.kotlin.sizeExp
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.time.Instant

class CreatingObjectsKotlinTest {

	data class Product(
		val id: Long,
		val productName: String,
		val price: Long,
		val options: List<String>,
		val createdAt: Instant,
		val productType: ProductType,
		val merchantInfo: Map<Int, String>
	)

	enum class ProductType {
		ELECTRONICS,
		CLOTHING,
		FOOD
	}

	@Test
	fun giveMeOneUsage() {
		val fixtureMonkey = FixtureMonkey.builder()
			.plugin(KotlinPlugin())
			.build()

		val product: Product = fixtureMonkey.giveMeOne()

		then(product).isNotNull
	}

	@Test
	fun test() {
		// given
		val fixtureMonkey = FixtureMonkey.builder()
			.plugin(KotlinPlugin())
			.build()

		// when
		val actual: Product = fixtureMonkey.giveMeOne()

		// then
		then(actual).isNotNull
	}

	@Test
	fun customizeTest() {
		// given
		val fixtureMonkey = FixtureMonkey.builder()
			.plugin(KotlinPlugin())
			.build()

		// when
		val actual = fixtureMonkey.giveMeKotlinBuilder<Product>()
			.setExp(Product::id, 1000L)
			.sizeExp(Product::options, 3)
			.set("options[1]", "red")
			.sample()

		// then
		then(actual.id).isEqualTo(1000L)
		then(actual.options).hasSize(3)
		then(actual.options[1]).isEqualTo("red")
	}
}
