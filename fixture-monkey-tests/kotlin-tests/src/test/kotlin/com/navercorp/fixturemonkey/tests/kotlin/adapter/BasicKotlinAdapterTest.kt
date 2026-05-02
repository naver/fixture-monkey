/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.fixturemonkey.tests.kotlin.adapter

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinNodeTreeAdapterPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.tests.TestEnvironment.ADAPTER_TEST_COUNT
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test

/**
 * Basic Kotlin adapter tests - starting with the simplest cases.
 */
class BasicKotlinAdapterTest {
	data class SimpleObject(val name: String, val value: Int)

	data class NestedObject(val id: String, val inner: SimpleObject)

	data class ListContainer(val items: List<String>)

	companion object {
		private val SUT = FixtureMonkey.builder()
			.plugin(KotlinPlugin())
			.plugin(KotlinNodeTreeAdapterPlugin())
			.build()
	}

	// ==================== Simple Object Tests ====================

	@RepeatedTest(ADAPTER_TEST_COUNT)
	fun sampleSimpleObject() {
		// when
		val actual = SUT.giveMeOne<SimpleObject>()

		// then
		then(actual).isNotNull
		then(actual.name).isNotNull
	}

	@RepeatedTest(ADAPTER_TEST_COUNT)
	fun setSimpleObjectProperty() {
		// given
		val expected = "expectedName"

		// when
		val actual = SUT.giveMeKotlinBuilder<SimpleObject>()
			.set("name", expected)
			.sample()

		// then
		then(actual.name).isEqualTo(expected)
	}

	@RepeatedTest(ADAPTER_TEST_COUNT)
	fun setSimpleObjectPropertyWithPropertySelector() {
		// given
		val expected = "expectedName"

		// when
		val actual = SUT.giveMeKotlinBuilder<SimpleObject>()
			.set(SimpleObject::name, expected)
			.sample()

		// then
		then(actual.name).isEqualTo(expected)
	}

	@Test
	fun setEntireSimpleObject() {
		// given
		val expected = SimpleObject("test", 42)

		// when
		val actual = SUT.giveMeKotlinBuilder<SimpleObject>()
			.set(expected)
			.sample()

		// then
		then(actual).isEqualTo(expected)
	}

	// ==================== Nested Object Tests ====================

	@RepeatedTest(ADAPTER_TEST_COUNT)
	fun sampleNestedObject() {
		// when
		val actual = SUT.giveMeOne<NestedObject>()

		// then
		then(actual).isNotNull
		then(actual.inner).isNotNull
		then(actual.inner.name).isNotNull
	}

	@RepeatedTest(ADAPTER_TEST_COUNT)
	fun setNestedProperty() {
		// given
		val expected = "nestedName"

		// when
		val actual = SUT.giveMeKotlinBuilder<NestedObject>()
			.set("inner.name", expected)
			.sample()

		// then
		then(actual.inner.name).isEqualTo(expected)
	}

	// ==================== List Container Tests ====================

	@RepeatedTest(ADAPTER_TEST_COUNT)
	fun sampleListContainer() {
		// when
		val actual = SUT.giveMeOne<ListContainer>()

		// then
		then(actual).isNotNull
		then(actual.items).isNotNull
	}

	@RepeatedTest(ADAPTER_TEST_COUNT)
	fun setListSize() {
		// given
		val expectedSize = 3

		// when
		val actual = SUT.giveMeKotlinBuilder<ListContainer>()
			.size("items", expectedSize)
			.sample()

		// then
		then(actual.items).hasSize(expectedSize)
	}

	@RepeatedTest(ADAPTER_TEST_COUNT)
	fun setListElement() {
		// given
		val expected = "expectedElement"

		// when
		val actual = SUT.giveMeKotlinBuilder<ListContainer>()
			.size("items", 3)
			.set("items[0]", expected)
			.sample()

		// then
		then(actual.items[0]).isEqualTo(expected)
	}
}
