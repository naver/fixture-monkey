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
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.setExp
import com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT
import net.jqwik.api.Arbitraries
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.RepeatedTest

class RegisterOuterTypeDirectivePrecedenceTest {
	data class Entity(val id: Long?, val name: String)

	data class Wrapper(val entity: Entity)

	@RepeatedTest(TEST_COUNT)
	fun registeredSetNullOverridesRegisteredFieldType() {
		val actual = SUT.giveMeOne(Entity::class.java)

		then(actual.id).isNull()
	}

	@RepeatedTest(TEST_COUNT)
	fun registeredSetNullOverridesRegisteredFieldTypeInNestedProperty() {
		val actual = SUT.giveMeOne(Wrapper::class.java)

		then(actual.entity.id).isNull()
	}

	@RepeatedTest(TEST_COUNT)
	fun registeredFieldTypeStillAppliesToOtherProperties() {
		val actual = SUT.giveMeOne(Long::class.javaObjectType)

		then(actual).isBetween(0L, Int.MAX_VALUE.toLong())
	}

	@RepeatedTest(TEST_COUNT)
	fun directSetOverridesRegisteredSetNull() {
		val actual = SUT.giveMeKotlinBuilder<Entity>()
			.setExp(Entity::id, 7L)
			.sample()

		then(actual.id).isEqualTo(7L)
	}

	companion object {
		private val SUT = FixtureMonkey.builder()
			.plugin(KotlinPlugin())
			.defaultNotNull(true)
			.register(Long::class.javaObjectType) {
				it.giveMeBuilder(Long::class.javaObjectType).set(Arbitraries.longs().between(0, Int.MAX_VALUE.toLong()))
			}
			.register(Entity::class.java) { it.giveMeBuilder(Entity::class.java).setNull("id") }
			.build()
	}
}
