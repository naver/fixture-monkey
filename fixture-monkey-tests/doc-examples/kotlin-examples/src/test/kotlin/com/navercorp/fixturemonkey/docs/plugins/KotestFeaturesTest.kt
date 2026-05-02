package com.navercorp.fixturemonkey.docs.plugins

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotest.KotestPlugin
import com.navercorp.fixturemonkey.kotest.giveMeArb
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldNotBe
import io.kotest.property.checkAll

class KotestFeaturesTest : StringSpec({

	val fixtureMonkey = FixtureMonkey.builder()
		.plugin(KotlinPlugin())
		.plugin(KotestPlugin())
		.build()

	"generate random product" {
		data class Product(
			val id: Long,
			val name: String,
			val price: Long
		)

		val product: Product = fixtureMonkey.giveMeOne()

		product shouldNotBe null
	}

	"property based testing with checkAll" {
		data class Product(
			val id: Long,
			val name: String,
			val price: Long
		)

		checkAll(fixtureMonkey.giveMeArb<Product>()) { product ->
			product shouldNotBe null
		}
	}
})
