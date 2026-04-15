package com.navercorp.fixturemonkey.docs.plugins

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.introspector.KotlinAndJavaCompositeArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.introspector.PrimaryConstructorArbitraryIntrospector
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class IntrospectorsKotlinTest {

	data class Product(
		val id: Long,
		val name: String,
		val price: Long,
		val options: List<String>
	)

	@Test
	fun primaryConstructorIntrospector() {
		// given
		val fixtureMonkey = FixtureMonkey.builder()
			.plugin(KotlinPlugin())
			.objectIntrospector(PrimaryConstructorArbitraryIntrospector.INSTANCE)
			.build()

		// when
		val product: Product = fixtureMonkey.giveMeOne()

		// then
		then(product).isNotNull
	}

	@Test
	fun kotlinAndJavaCompositeIntrospector() {
		// given
		val fixtureMonkey = FixtureMonkey.builder()
			.plugin(KotlinPlugin())
			.objectIntrospector(KotlinAndJavaCompositeArbitraryIntrospector())
			.build()

		// when
		val product: Product = fixtureMonkey.giveMeOne()

		// then
		then(product).isNotNull
	}
}
