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

package com.navercorp.fixturemonkey.tests.kotlin17

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions
import com.navercorp.fixturemonkey.api.property.Property
import com.navercorp.fixturemonkey.api.property.PropertyUtils
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.property.KotlinPropertyGenerator
import org.assertj.core.api.BDDAssertions.catchThrowable
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class SealedTypePropertyNullabilityTest {
    @Test
    fun nonNullSealedInterfacePropertyIsNeverNull() {
        // given
        val monkeys = List(MONKEY_COUNT) { kotlinMonkey() }

        // when
        val actual = catchThrowable {
            monkeys.forEach { sut -> repeat(SAMPLE_COUNT) { sut.giveMeOne(SealedInterfaceHolder::class.java) } }
        }

        // then
        then(actual).isNull()
    }

    @Test
    fun nonNullSealedClassPropertyIsNeverNull() {
        // given
        val monkeys = List(MONKEY_COUNT) { kotlinMonkey() }

        // when
        val actual = catchThrowable {
            monkeys.forEach { sut -> repeat(SAMPLE_COUNT) { sut.giveMeOne(SealedClassHolder::class.java) } }
        }

        // then
        then(actual).isNull()
    }

    @Test
    fun sealedInterfaceCandidatesKeepNonNullOfDeclaredProperty() {
        // given
        val declaredProperty = valuePropertyOf(SealedInterfaceHolder::class.java)

        // when
        val actual = resolveCandidates(declaredProperty)

        // then
        then(actual.map { it.isNullable }).isNotEmpty().containsOnly(false)
    }

    @Test
    fun sealedClassCandidatesKeepNonNullOfDeclaredProperty() {
        // given
        val declaredProperty = valuePropertyOf(SealedClassHolder::class.java)

        // when
        val actual = resolveCandidates(declaredProperty)

        // then
        then(actual.map { it.isNullable }).isNotEmpty().containsOnly(false)
    }

    private fun resolveCandidates(declaredProperty: Property): List<Property> =
        FixtureMonkeyOptions.builder().build()
            .candidateConcretePropertyResolvers
            .filter { it.match(declaredProperty) }
            .flatMap { it.operator.resolve(declaredProperty) }

    private fun valuePropertyOf(holderType: Class<*>): Property = KotlinPropertyGenerator()
        .generateChildProperties(PropertyUtils.toProperty(holderType))
        .first { it.name == "value" }

    private fun kotlinMonkey(): FixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .build()

    sealed interface SealedValue

    data class FirstSealedValue(val number: Int) : SealedValue

    data class SecondSealedValue(val number: Int) : SealedValue

    sealed class SealedClassValue

    data class FirstSealedClassValue(val number: Int) : SealedClassValue()

    data class SecondSealedClassValue(val number: Int) : SealedClassValue()

    data class SealedInterfaceHolder(val value: SealedValue)

    data class SealedClassHolder(val value: SealedClassValue)

    companion object {
        private const val MONKEY_COUNT = 40
        private const val SAMPLE_COUNT = 30
    }
}
