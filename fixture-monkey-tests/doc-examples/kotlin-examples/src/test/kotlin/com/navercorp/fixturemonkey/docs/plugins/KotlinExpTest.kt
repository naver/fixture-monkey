package com.navercorp.fixturemonkey.docs.plugins

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.into
import com.navercorp.fixturemonkey.kotlin.setExp
import com.navercorp.fixturemonkey.kotlin.sizeExp
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class KotlinExpTest {

	data class KotlinClass(
		val field: String,
		val list: List<String>,
		val nested: Nested
	) {
		data class Nested(
			val nestedField: String
		)
	}

	private val fixtureMonkey = FixtureMonkey.builder()
		.plugin(KotlinPlugin())
		.build()

	@Test
	fun setExpUsage() {
		// when
		val actual = fixtureMonkey.giveMeKotlinBuilder<KotlinClass>()
			.setExp(KotlinClass::field, "hello")
			.sample()

		// then
		then(actual.field).isEqualTo("hello")
	}

	@Test
	fun nestedExpUsage() {
		// when
		val actual = fixtureMonkey.giveMeKotlinBuilder<KotlinClass>()
			.setExp(KotlinClass::nested into KotlinClass.Nested::nestedField, "nested")
			.sample()

		// then
		then(actual.nested.nestedField).isEqualTo("nested")
	}

	@Test
	fun listExpUsage() {
		// when
		val actual = fixtureMonkey.giveMeKotlinBuilder<KotlinClass>()
			.sizeExp(KotlinClass::list, 3)
			.set("list[0]", "first")
			.sample()

		// then
		then(actual.list).hasSize(3)
		then(actual.list[0]).isEqualTo("first")
	}
}
