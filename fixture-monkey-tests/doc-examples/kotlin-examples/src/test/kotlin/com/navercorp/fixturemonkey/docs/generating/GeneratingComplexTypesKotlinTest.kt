package com.navercorp.fixturemonkey.docs.generating

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class GeneratingComplexTypesKotlinTest {

	private val fixtureMonkey = FixtureMonkey.builder()
		.plugin(KotlinPlugin())
		.build()

	class Generic<T>(val foo: T)

	class GenericImpl(val foo: Generic<String>)

	class TwoGenericObject<T, U>(val foo: T, val bar: U)

	class SelfReference(val foo: String, val bar: SelfReference?)

	class SelfReferenceList(val foo: String, val bar: List<SelfReferenceList>?)

	sealed class SealedClass

	object ObjectSealedClass : SealedClass()

	class SealedClassImpl(val foo: String) : SealedClass()

	@JvmInline
	value class ValueClass(val foo: String)

	interface KotlinInterface {
		val foo: String
		val bar: Int
	}

	@Test
	fun generateGenericInt() {
		// when
		val genericInt: Generic<Int> = fixtureMonkey.giveMeOne()

		// then
		then(genericInt).isNotNull
	}

	@Test
	fun generateGenericImpl() {
		// when
		val genericImpl: GenericImpl = fixtureMonkey.giveMeOne()

		// then
		then(genericImpl).isNotNull
	}

	@Test
	fun generateTwoGenericObject() {
		// when
		val twoParam: TwoGenericObject<String, Int> = fixtureMonkey.giveMeOne()

		// then
		then(twoParam).isNotNull
	}

	@Test
	fun generateSelfReference() {
		// when
		val selfRef: SelfReference = fixtureMonkey.giveMeOne()

		// then
		then(selfRef).isNotNull
	}

	@Test
	fun generateSelfReferenceWithContainerInfo() {
		// given
		val customFixture = FixtureMonkey.builder()
			.plugin(KotlinPlugin())
			.defaultArbitraryContainerInfoGenerator { ArbitraryContainerInfo(2, 2) }
			.build()

		// when
		val refList: SelfReferenceList = customFixture.giveMeOne()

		// then
		then(refList).isNotNull
	}

	@Test
	fun generateSealedClass() {
		// when
		val sealedClass: SealedClass = fixtureMonkey.giveMeOne()

		// then
		then(sealedClass).isNotNull
	}

	@Test
	fun generateValueClass() {
		// when
		val valueClass: ValueClass = fixtureMonkey.giveMeOne()

		// then
		then(valueClass).isNotNull
	}

	@Test
	fun generateInterface() {
		// given
		val customFixture = FixtureMonkey.builder()
			.plugin(KotlinPlugin())
			.plugin(InterfacePlugin())
			.build()

		// when
		val instance: KotlinInterface = customFixture.giveMeOne()

		// then
		then(instance).isNotNull
		then(instance.foo).isNotNull
	}
}
