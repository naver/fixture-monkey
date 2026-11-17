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
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenThrownBy
import org.junit.jupiter.api.Test

class StrictModeTest {
    @Test
    fun setMemberOfSealedInterfaceProperty() {
        // when
        val actual = SUT.giveMeBuilder<SealedInterfaceHolder>()
            .set("value.id", "x")
            .sampleList(TEST_COUNT)
            .map { it.value.id }
            .toSet()

        // then
        then(actual).containsExactly("x")
    }

    @Test
    fun setMemberOfSealedClassProperty() {
        // when
        val actual = SUT.giveMeBuilder<SealedClassHolder>()
            .set("value.id", "x")
            .sampleList(TEST_COUNT)
            .map { it.value.id }
            .toSet()

        // then
        then(actual).containsExactly("x")
    }

    @Test
    fun setMemberOfInterfaceProperty() {
        // when
        val actual = SUT.giveMeBuilder<InterfaceHolder>()
            .set("value.id", "x")
            .sampleList(TEST_COUNT)
            .map { it.value.id }
            .toSet()

        // then
        then(actual).containsExactly("x")
    }

    @Test
    fun setMemberOfSealedInterfaceListElement() {
        // when
        val actual = SUT.giveMeBuilder<SealedInterfaceListHolder>()
            .size("values", 1)
            .set("values[0].id", "x")
            .sampleList(TEST_COUNT)
            .map { it.values[0].id }
            .toSet()

        // then
        then(actual).containsExactly("x")
    }

    @Test
    fun setSubtypeFieldOfSealedInterfaceProperty() {
        // when
        val actual = SUT.giveMeBuilder<SingleSealedHolder>()
            .set("value.tags", listOf("a"))
            .sample()

        // then
        then((actual.value as OnlySealedValue).tags).containsExactly("a")
    }

    @Test
    fun setUnknownFieldOfSealedInterfacePropertyThrows() {
        thenThrownBy {
            SUT.giveMeBuilder<SealedInterfaceHolder>()
                .set("value.nonexistent", "x")
                .sample()
        }
            .isExactlyInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("No matching results for given NodeResolvers")
    }

    sealed interface SealedInterfaceValue {
        val id: String
    }

    data class FirstSealedInterfaceValue(override val id: String, val first: Int) : SealedInterfaceValue

    data class SecondSealedInterfaceValue(override val id: String, val second: Int) : SealedInterfaceValue

    sealed class SealedClassValue {
        abstract val id: String
    }

    data class FirstSealedClassValue(override val id: String, val first: Int) : SealedClassValue()

    data class SecondSealedClassValue(override val id: String, val second: Int) : SealedClassValue()

    interface InterfaceValue {
        val id: String
    }

    data class FirstInterfaceValue(override val id: String, val first: Int) : InterfaceValue

    data class SecondInterfaceValue(override val id: String, val second: Int) : InterfaceValue

    sealed interface SingleSealedValue

    data class OnlySealedValue(val tags: List<String>) : SingleSealedValue

    data class SealedInterfaceHolder(val value: SealedInterfaceValue)

    data class SealedClassHolder(val value: SealedClassValue)

    data class InterfaceHolder(val value: InterfaceValue)

    data class SingleSealedHolder(val value: SingleSealedValue)

    data class SealedInterfaceListHolder(val values: List<SealedInterfaceValue>)

    companion object {
        private val SUT = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(
                InterfacePlugin().interfaceImplements(
                    InterfaceValue::class.java,
                    listOf(FirstInterfaceValue::class.java, SecondInterfaceValue::class.java)
                )
            )
            .useExpressionStrictMode()
            .build()
    }
}
