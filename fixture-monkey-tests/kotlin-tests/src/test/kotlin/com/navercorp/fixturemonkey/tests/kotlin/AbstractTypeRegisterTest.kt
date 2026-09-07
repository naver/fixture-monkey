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
import com.navercorp.fixturemonkey.FixtureMonkeyBuilder
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMe
import com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource

class AbstractTypeRegisterTest {
    @ParameterizedTest(name = "{0}")
    @EnumSource(AbstractKind::class)
    fun registeredBuilderBeneathAbstractPropertyIsUsed(kind: AbstractKind) {
        val sut = kind.builder()
            .register(Label::class.java) { it.giveMeBuilder(Label(PINNED)) }
            .build()

        val actual = kind.sampledValues(sut).map { it.label.text }.toSet()

        then(actual).containsExactly(PINNED)
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(AbstractKind::class)
    fun registeredBuilderForAbstractTypeIsUsedForPropertyOfThatType(kind: AbstractKind) {
        val sut = kind.builder()
            .register(kind.abstractType) { it.giveMeBuilder(kind.pinned) }
            .build()

        val actual = kind.sampledValues(sut).toSet()

        then(actual).containsExactly(kind.pinned)
    }

    @ParameterizedTest(name = "{0}, {1}")
    @MethodSource("kindsAndOverloads")
    fun registerOverloadForAbstractTypeIsUsedForPropertyOfThatType(kind: AbstractKind, overload: RegisterOverload) {
        val sut = overload.register(kind.builder(), kind.abstractType, kind.pinned).build()

        val actual = kind.sampledValues(sut).toSet()

        then(actual).containsExactly(kind.pinned)
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(AbstractKind::class)
    fun abstractPropertyVariesItsImplementationAcrossSamples(kind: AbstractKind) {
        val sut = kind.builder().build()

        val actual = kind.sampledValues(sut).map { it::class }.toSet()

        then(actual).hasSize(2)
    }

    @Test
    fun registeredBuilderBeneathAbstractContainerElementIsUsed() {
        val sut = kotlinBuilder()
            .register(Label::class.java) { it.giveMeBuilder(Label(PINNED)) }
            .build()

        val actual = sut.giveMe<SealedInterfaceListHolder>(TEST_COUNT)
            .flatMap { it.values }
            .map { it.label.text }
            .toSet()

        then(actual).containsExactly(PINNED)
    }

    @Test
    fun registeredBuilderTwoLevelsBeneathAbstractPropertyIsUsed() {
        val sut = kotlinBuilder()
            .register(Label::class.java) { it.giveMeBuilder(Label(PINNED)) }
            .build()

        val actual = sut.giveMe<NestedSealedInterfaceHolder>(TEST_COUNT).map { it.holder.value.label.text }.toSet()

        then(actual).containsExactly(PINNED)
    }

    @Test
    fun registeredBuilderIsUsedForConcretePropertyType() {
        val sut = kotlinBuilder()
            .register(Label::class.java) { it.giveMeBuilder(Label(PINNED)) }
            .build()

        val actual = sut.giveMe<ConcreteHolder>(TEST_COUNT).map { it.value.label.text }.toSet()

        then(actual).containsExactly(PINNED)
    }

    @Test
    fun registeredBuilderIsUsedForConcreteRootType() {
        val sut = kotlinBuilder()
            .register(Label::class.java) { it.giveMeBuilder(Label(PINNED)) }
            .build()

        val actual = sut.giveMe<FirstSealedInterfaceValue>(TEST_COUNT).map { it.label.text }.toSet()

        then(actual).containsExactly(PINNED)
    }

    enum class AbstractKind {
        SEALED_INTERFACE {
            override val abstractType = SealedInterfaceValue::class.java
            override val pinned = FirstSealedInterfaceValue(Label(PINNED))
            override fun builder() = kotlinBuilder()
            override fun sampledValues(sut: FixtureMonkey) =
                sut.giveMe<SealedInterfaceHolder>(TEST_COUNT).map { it.value }
        },
        SEALED_CLASS {
            override val abstractType = SealedClassValue::class.java
            override val pinned = FirstSealedClassValue(Label(PINNED))
            override fun builder() = kotlinBuilder()
            override fun sampledValues(sut: FixtureMonkey) =
                sut.giveMe<SealedClassHolder>(TEST_COUNT).map { it.value }
        },
        INTERFACE {
            override val abstractType = InterfaceValue::class.java
            override val pinned = FirstInterfaceValue(Label(PINNED))
            override fun builder() = kotlinBuilder().plugin(
                InterfacePlugin().interfaceImplements(
                    InterfaceValue::class.java,
                    listOf(FirstInterfaceValue::class.java, SecondInterfaceValue::class.java)
                )
            )
            override fun sampledValues(sut: FixtureMonkey) =
                sut.giveMe<InterfaceHolder>(TEST_COUNT).map { it.value }
        },
        ABSTRACT_CLASS {
            override val abstractType = AbstractClassValue::class.java
            override val pinned = FirstAbstractClassValue(Label(PINNED))
            override fun builder() = kotlinBuilder().plugin(
                InterfacePlugin().abstractClassExtends(
                    AbstractClassValue::class.java,
                    listOf(FirstAbstractClassValue::class.java, SecondAbstractClassValue::class.java)
                )
            )
            override fun sampledValues(sut: FixtureMonkey) =
                sut.giveMe<AbstractClassHolder>(TEST_COUNT).map { it.value }
        };

        abstract val abstractType: Class<*>
        abstract val pinned: Labeled
        abstract fun builder(): FixtureMonkeyBuilder
        abstract fun sampledValues(sut: FixtureMonkey): List<Labeled>
    }

    enum class RegisterOverload {
        REGISTER_EXACT_TYPE {
            override fun register(builder: FixtureMonkeyBuilder, type: Class<*>, pinned: Any) =
                builder.registerExactType(type) { it.giveMeBuilder(pinned) }
        },
        REGISTER_ASSIGNABLE_TYPE {
            override fun register(builder: FixtureMonkeyBuilder, type: Class<*>, pinned: Any) =
                builder.registerAssignableType(type) { it.giveMeBuilder(pinned) }
        },
        REGISTER_WITH_PRIORITY {
            override fun register(builder: FixtureMonkeyBuilder, type: Class<*>, pinned: Any) =
                builder.register(type, { it.giveMeBuilder(pinned) }, 0)
        };

        abstract fun register(builder: FixtureMonkeyBuilder, type: Class<*>, pinned: Any): FixtureMonkeyBuilder
    }

    companion object {
        private const val PINNED = "PINNED"

        private fun kotlinBuilder(): FixtureMonkeyBuilder = FixtureMonkey.builder().plugin(KotlinPlugin())

        @JvmStatic
        fun kindsAndOverloads(): List<Arguments> = AbstractKind.values().flatMap { kind ->
            RegisterOverload.values().map { overload -> Arguments.of(kind, overload) }
        }
    }

    interface Labeled {
        val label: Label
    }

    data class Label(val text: String)

    sealed interface SealedInterfaceValue : Labeled

    data class FirstSealedInterfaceValue(override val label: Label) : SealedInterfaceValue

    data class SecondSealedInterfaceValue(override val label: Label) : SealedInterfaceValue

    sealed class SealedClassValue : Labeled

    data class FirstSealedClassValue(override val label: Label) : SealedClassValue()

    data class SecondSealedClassValue(override val label: Label) : SealedClassValue()

    interface InterfaceValue : Labeled

    data class FirstInterfaceValue(override val label: Label) : InterfaceValue

    data class SecondInterfaceValue(override val label: Label) : InterfaceValue

    abstract class AbstractClassValue : Labeled

    data class FirstAbstractClassValue(override val label: Label) : AbstractClassValue()

    data class SecondAbstractClassValue(override val label: Label) : AbstractClassValue()

    data class SealedInterfaceHolder(val value: SealedInterfaceValue)

    data class SealedClassHolder(val value: SealedClassValue)

    data class InterfaceHolder(val value: InterfaceValue)

    data class AbstractClassHolder(val value: AbstractClassValue)

    data class SealedInterfaceListHolder(val values: List<SealedInterfaceValue>)

    data class NestedSealedInterfaceHolder(val holder: SealedInterfaceHolder)

    data class ConcreteHolder(val value: FirstSealedInterfaceValue)
}
