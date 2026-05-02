package com.navercorp.fixturemonkey.docs.generating

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.instantiator.instantiateBy
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.util.Random

class InstantiateMethodsKotlinTest {

	private val fixtureMonkey = FixtureMonkey.builder()
		.plugin(KotlinPlugin())
		.defaultNotNull(true)
		.build()

	class SimpleProduct(
		val name: String,
		val price: Int
	)

	class Product {
		val id: Long
		val name: String
		val price: Long
		val options: List<String>

		constructor() {
			this.id = 0
			this.name = "defaultProduct"
			this.price = 0
			this.options = emptyList()
		}

		constructor(name: String, price: Long) {
			this.id = Random().nextLong()
			this.name = name
			this.price = price
			this.options = emptyList()
		}

		constructor(name: String, price: Long, options: List<String>) {
			this.id = Random().nextLong()
			this.name = name
			this.price = price
			this.options = options
		}

		companion object {
			fun create(name: String, price: Long): Product {
				return Product(name, price)
			}

			fun createRecommended(price: Long): Product {
				return Product("recommendedProduct", price)
			}
		}
	}

	class PartiallyInitializedObject(
		val name: String
	) {
		var count: Int = 0
		var items: List<String>? = null
	}

	@Test
	fun usingSimpleConstructor() {
		val product = fixtureMonkey.giveMeBuilder<SimpleProduct>()
			.instantiateBy {
				constructor<SimpleProduct>()
			}
			.sample()

		then(product).isNotNull
		then(product.name).isNotNull
	}

	@Test
	fun usingDefaultConstructor() {
		val product = fixtureMonkey.giveMeBuilder<Product>()
			.instantiateBy {
				constructor<Product>()
			}
			.sample()

		then(product.id).isEqualTo(0)
		then(product.name).isEqualTo("defaultProduct")
	}

	@Test
	fun selectingConstructorWithoutOptions() {
		val product = fixtureMonkey.giveMeBuilder<Product>()
			.instantiateBy {
				constructor<Product> {
					parameter<String>()
					parameter<Long>()
				}
			}
			.sample()

		then(product.options).isEmpty()
	}

	@Test
	fun selectingConstructorWithOptions() {
		val product = fixtureMonkey.giveMeBuilder<Product>()
			.instantiateBy {
				constructor<Product> {
					parameter<String>()
					parameter<Long>()
					parameter<List<String>>()
				}
			}
			.sample()

		then(product.options).isNotNull
	}

	@Test
	fun specifyingParameterValues() {
		val product = fixtureMonkey.giveMeBuilder<Product>()
			.instantiateBy {
				constructor<Product> {
					parameter<String>("productName")
					parameter<Long>()
				}
			}
			.set("productName", "specialProduct")
			.sample()

		then(product.name).isEqualTo("specialProduct")
	}

	@Test
	fun usingFactoryMethod() {
		val product = fixtureMonkey.giveMeBuilder<Product>()
			.instantiateBy {
				factory<Product>("create")
			}
			.sample()

		then(product).isNotNull
		then(product.options).isEmpty()
	}

	@Test
	fun selectingSpecificFactoryMethod() {
		val product = fixtureMonkey.giveMeBuilder<Product>()
			.instantiateBy {
				factory<Product>("createRecommended") {
					parameter<Long>()
				}
			}
			.sample()

		then(product.name).isEqualTo("recommendedProduct")
	}

	@Test
	fun specifyingFactoryMethodParameterValues() {
		val product = fixtureMonkey.giveMeBuilder<Product>()
			.instantiateBy {
				factory<Product>("create") {
					parameter<String>("productName")
					parameter<Long>("productPrice")
				}
			}
			.set("productName", "customProduct")
			.set("productPrice", 9900L)
			.sample()

		then(product.name).isEqualTo("customProduct")
		then(product.price).isEqualTo(9900L)
	}

	@Test
	fun propertySettingAfterConstructor() {
		val obj = fixtureMonkey.giveMeBuilder<PartiallyInitializedObject>()
			.instantiateBy {
				constructor<PartiallyInitializedObject> {
					parameter<String>()
					javaBeansProperty()
				}
			}
			.sample()

		then(obj.name).isNotNull
		then(obj.items).isNotNull
	}

	@Test
	fun preservingConstructorSetValues() {
		val specificName = "specificName"

		val obj = fixtureMonkey.giveMeBuilder<PartiallyInitializedObject>()
			.instantiateBy {
				constructor<PartiallyInitializedObject> {
					parameter<String>("name")
				}
			}
			.set("name", specificName)
			.sample()

		then(obj.name).isEqualTo(specificName)
	}
}
