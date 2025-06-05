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
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT
import com.navercorp.fixturemonkey.tests.kotlin.InterfaceTest.SealedObjectClass.ConcreteSealedObjectClass
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.util.LinkedList

class InterfaceTest {
    @RepeatedTest(TEST_COUNT)
    fun anonymousArbitraryIntrospector() {
        // given
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(InterfacePlugin())
            .build()

        // when
        val actual = sut.giveMeOne<Interface>().string()

        // then
        then(actual).isNotNull()
    }

    @RepeatedTest(TEST_COUNT)
    fun notUseAnonymousArbitraryIntrospector() {
        // given
        val sut = FixtureMonkey.builder()
            .plugin(InterfacePlugin().useAnonymousArbitraryIntrospector(false))
            .build()

        // when
        val actual: Interface = sut.giveMeOne()

        // then
        then(actual).isNull()
    }

    @RepeatedTest(TEST_COUNT)
    fun sealedInterfaceApplySet() {
        val actual = SUT.giveMeBuilder<SealedInterface>()
            .thenApply { _, builder -> builder.set("stringObject.string", "expected") }
            .sample()

        then((actual as SealedInterfaceImplementation).stringObject.string).isEqualTo("expected")
    }

    @RepeatedTest(TEST_COUNT)
    fun interfaceImplementsExtendsInterface() {
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(
                InterfacePlugin()
                    .interfaceImplements(Collection::class.java, listOf(Set::class.java))
            )
            .build()

        val actual: Collection<String> = sut.giveMeOne()

        then(actual).isInstanceOf(HashSet::class.java)
    }

    @Test
    fun interfaceImplementsAssignableTypeGeneratesConcreteTypeNotThrows() {
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(
                InterfacePlugin()
                    .interfaceImplements(
                        AssignableTypeMatcher(Collection::class.java),
                        listOf(LinkedList::class.java)
                    )

            )
            .build()

        val actual: ArrayList<String> = sut.giveMeOne()

        then(actual).isInstanceOf(ArrayList::class.java)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleSealedClass() {
        val actual = SUT.giveMeOne<SealedClass>()

        then(actual).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun fixedSealedClass() {
        val actual = SUT.giveMeBuilder<SealedClass>()
            .fixed()
            .sample()

        then(actual).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleImplementedSealedClass() {
        val actual = SUT.giveMeOne<ImplementedSealedClass>()

        then(actual).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun fixedImplementedSealedClass() {
        val actual = SUT.giveMeBuilder<ImplementedSealedClass>()
            .fixed()
            .sample()

        then(actual).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun sealedObject() {
        val actual: SealedObjectClass = SUT.giveMeOne()

        then(actual).isInstanceOf(ConcreteSealedObjectClass::class.java)
    }

    @RepeatedTest(TEST_COUNT)
    fun sealedObjectThenApply() {
        val actual = SUT.giveMeBuilder<SealedObjectClass>()
            .thenApply { obj, builder -> }
            .sample()

        then(actual).isInstanceOf(ConcreteSealedObjectClass::class.java)
    }

    interface Interface {
        fun string(): String
    }

    sealed interface SealedInterface

    class SealedInterfaceImplementation(val stringObject: StringObject) : SealedInterface

    class StringObject(val string: String)

    sealed class SealedClass

    class ImplementedSealedClass(
        val string: String,
        val integer: Int,
        val float: Float,
        val long: Long,
        val double: Double,
        val byte: Byte,
        val char: Char,
        val short: Short,
        val boolean: Boolean,
        val enum: Enum,
    ) : SealedClass()

    enum class Enum { ONE, TWO, THREE }

    sealed class SealedObjectClass {
        object ConcreteSealedObjectClass : SealedObjectClass()
    }

    companion object {
        private val SUT: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()
    }
} 