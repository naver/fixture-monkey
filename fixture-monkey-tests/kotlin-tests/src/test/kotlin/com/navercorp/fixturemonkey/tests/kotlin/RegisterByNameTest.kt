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

package com.navercorp.fixturemonkey.tests.kotlin

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeExperimentalBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.registerByName
import com.navercorp.fixturemonkey.kotlin.setExp
import com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test

class RegisterByNameTest {
	@Test
	fun buildersRegisteredByNameForSameTypeAreChosenAcrossSamples() {
		val actual = (0 until TEST_COUNT)
			.map { SUT.giveMeOne(Order::class.java).status }
			.toSet()

		then(actual).containsExactlyInAnyOrder(DELIVERED, CANCELLED)
	}

	@Test
	fun builderRegisteredByNameWithHigherWeightIsChosenMoreOften() {
		// given
		val sut = FixtureMonkey.builder()
			.plugin(KotlinPlugin())
			.seed(777L)
			.registerByName<Order>("delivered", weight = 1) {
				it.giveMeKotlinBuilder<Order>().setExp(Order::status, DELIVERED)
			}
			.registerByName<Order>("cancelled", weight = 9) {
				it.giveMeKotlinBuilder<Order>().setExp(Order::status, CANCELLED)
			}
			.build()

		// when
		val actual = (0 until 200)
			.map { sut.giveMeOne(Order::class.java).status }
			.groupingBy { it }
			.eachCount()

		// then
		then(actual).containsOnlyKeys(DELIVERED, CANCELLED)
		then(actual.getValue(CANCELLED)).isGreaterThan(actual.getValue(DELIVERED) * 3)
	}

	@RepeatedTest(TEST_COUNT)
	fun selectedNameIsAlwaysUsed() {
		val actual = SUT.giveMeExperimentalBuilder<Order>()
			.selectName("cancelled")
			.sample()

		then(actual.status).isEqualTo(CANCELLED)
	}

	@RepeatedTest(TEST_COUNT)
	fun selectNameThroughKotlinBuilder() {
		val actual = SUT.giveMeKotlinBuilder<Order>()
			.selectName("cancelled")
			.setExp(Order::memo, "memo")
			.sample()

		then(actual.status).isEqualTo(CANCELLED)
		then(actual.memo).isEqualTo("memo")
	}

	companion object {
		private const val DELIVERED = "DELIVERED"
		private const val CANCELLED = "CANCELLED"

		private val SUT = FixtureMonkey.builder()
			.plugin(KotlinPlugin())
			.defaultNotNull(true)
			.registerByName<Order>("delivered") { it.giveMeKotlinBuilder<Order>().setExp(Order::status, DELIVERED) }
			.registerByName<Order>("cancelled") { it.giveMeKotlinBuilder<Order>().setExp(Order::status, CANCELLED) }
			.build()
	}

	data class Order(val status: String, val memo: String)
}
