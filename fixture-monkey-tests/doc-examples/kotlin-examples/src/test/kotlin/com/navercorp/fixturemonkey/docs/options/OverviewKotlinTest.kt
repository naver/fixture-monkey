package com.navercorp.fixturemonkey.docs.options

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo
import com.navercorp.fixturemonkey.api.jqwik.JavaTypeArbitraryGenerator
import com.navercorp.fixturemonkey.api.jqwik.JqwikPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import net.jqwik.api.Arbitraries
import net.jqwik.api.arbitraries.StringArbitrary
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class OverviewKotlinTest {

	data class Product(
		val productName: String,
		val price: Long,
		val category: String,
		val items: List<String>
	)

	@Test
	fun testDefaultNotNullOption() {
		// given
		val fixtureMonkey = FixtureMonkey.builder()
			.plugin(KotlinPlugin())
			.defaultNotNull(true)
			.build()

		// when
		val product: Product = fixtureMonkey.giveMeOne()

		// then
		then(product.productName).isNotNull
		then(product.price).isNotNull
		then(product.category).isNotNull
	}

	@Test
	fun testJavaTypeArbitraryGeneratorOption() {
		// given
		val fixtureMonkey = FixtureMonkey.builder()
			.plugin(
				JqwikPlugin()
					.javaTypeArbitraryGenerator(object : JavaTypeArbitraryGenerator {
						override fun strings(): StringArbitrary {
							return Arbitraries.strings().alpha().ofLength(10)
						}
					})
			)
			.build()

		// when
		val generatedString: String = fixtureMonkey.giveMeOne()

		// then
		then(generatedString).hasSize(10)
		then(generatedString).matches("[a-zA-Z]+")
	}

	@Test
	fun testRegisterOption() {
		// given
		val fixtureMonkey = FixtureMonkey.builder()
			.plugin(KotlinPlugin())
			.register(Product::class.java) { builder ->
				builder.giveMeBuilder(Product::class.java)
					.set("price", Arbitraries.longs().greaterOrEqual(1))
					.set("category", "Electronics")
			}
			.build()

		// when
		val product: Product = fixtureMonkey.giveMeOne()

		// then
		then(product.price).isPositive
		then(product.category).isEqualTo("Electronics")
	}

	@Test
	fun testContainerSizeOption() {
		// given
		val fixtureMonkey = FixtureMonkey.builder()
			.plugin(KotlinPlugin())
			.defaultArbitraryContainerInfoGenerator { ArbitraryContainerInfo(3, 3) }
			.build()

		// when
		val stringList: List<String> = fixtureMonkey.giveMeOne()

		// then
		then(stringList).hasSize(3)
	}
}
