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
import com.navercorp.fixturemonkey.javax.validation.plugin.JavaxValidationPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.setExpGetter
import com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.RepeatedTest
import javax.validation.constraints.NotEmpty

class AnonymousInstanceTest {
    @RepeatedTest(TEST_COUNT)
    fun sampleAnonymous() {
        val actual = SUT.giveMeOne<Interface>()

        then(actual.string()).isNotNull
        then(actual.integer()).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleAnonymousSetValue() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<Interface>()
            .setExpGetter(Interface::string, expected)
            .sample()
            .string()

        then(actual).isEqualTo(expected)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleAnonymousWithParam() {
        val actual = SUT.giveMeOne<InterfaceWithParams>()

        then(actual).isNull()
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleAnonymousContainer() {
        val actual = SUT.giveMeOne<ContainerInterface>()

        then(actual.list()).isNotNull
        then(actual.map()).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleAnonymousContainerSetList() {
        val actual: List<String> = SUT.giveMeBuilder<ContainerInterface>()
            .size("list", 3)
            .set("list[0]", "test")
            .sample()
            .list()

        then(actual).hasSize(3)
        then(actual[0]).isEqualTo("test")
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleAnonymousAnnotatedInterface() {
        val actual = SUT.giveMeOne<AnnotatedInterface>().string()

        then(actual).isNotEmpty
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleGetterInterface() {
        val actual = SUT.giveMeOne<GetterInterface>().getString()

        then(actual).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun setGetterInterface() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<GetterInterface>()
            .set("string", expected)
            .sample()
            .getString()

        then(actual).isEqualTo(expected)
    }

    @RepeatedTest(TEST_COUNT)
    fun samplePropertyInterface() {
        val actual = SUT.giveMeOne<PropertyInterface>()

        then(actual.string).isNotNull
        then(actual.int).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun setPropertyInterface() {
        val expected = "expected"

        val actual = SUT.giveMeBuilder<PropertyInterface>()
            .set("string", expected)
            .sample()
            .string

        then(actual).isEqualTo(expected)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleExtendedInterface() {
        val actual = SUT.giveMeOne<ExtendsInterface>()

        then(actual.value()).isNotNull
        then(actual.string()).isNotNull
        then(actual.integer()).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun setExtendedInterface() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<ExtendsInterface>()
            .set("value", expected)
            .sample()
            .value()

        then(actual).isEqualTo(expected)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleExtendsInterfaceWithOverrideMethod() {
        val actual = SUT.giveMeOne<ExtendsInterfaceWithOverrideMethod>().string()

        then(actual).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun setExtendsInterfaceWithOverrideMethod() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<ExtendsInterfaceWithOverrideMethod>()
            .set("string", expected)
            .sample()
            .string()

        then(actual).isEqualTo(expected)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleExtendsTwoInterface() {
        val actual = SUT.giveMeOne<ExtendsTwoInterface>().string()

        then(actual).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleNestedExtendsInterface() {
        val actual = SUT.giveMeOne<NestedExtendsInterface>().string()

        then(actual).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun setNestedExtendsInterface() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<NestedExtendsInterface>()
            .set("string", expected)
            .sample()
            .string()

        then(actual).isEqualTo(expected)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleExtendsPropertyInterface() {
        val actual = SUT.giveMeOne<ExtendsPropertyInterface>().string

        then(actual).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun setExtendsPropertyInterface() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<ExtendsPropertyInterface>()
            .set("string", expected)
            .sample()
            .string

        then(actual).isEqualTo(expected)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleNestedExtendsPropertyInterface() {
        val actual = SUT.giveMeOne<NestedExtendsPropertyInterface>().string

        then(actual).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun setNestedExtendsPropertyInterface() {
        val expected = "test"

        val actual = SUT.giveMeBuilder<NestedExtendsPropertyInterface>()
            .set("string", expected)
            .sample()
            .string

        then(actual).isEqualTo(expected)
    }

    interface Interface {
        fun string(): String
        fun integer(): Int
    }

    interface InterfaceWithParams {
        fun string(str: String): String?
        fun integer(int: Int): Int?
    }

    interface ContainerInterface {
        fun list(): List<String>
        fun map(): Map<String, Int>
    }

    interface AnnotatedInterface {
        @NotEmpty
        fun string(): String
    }

    interface GetterInterface {
        fun getString(): String
    }

    interface PropertyInterface {
        val string: String

        val int: Int
    }

    interface ExtendsInterface : Interface {
        fun value(): String
    }

    interface ExtendsInterfaceWithOverrideMethod : Interface {
        override fun string(): String
    }

    interface ExtendsTwoInterface : Interface, ContainerInterface

    interface NestedExtendsInterface : ExtendsInterfaceWithOverrideMethod, Interface

    interface ExtendsPropertyInterface : PropertyInterface {
        override val string: String
    }

    interface NestedExtendsPropertyInterface : ExtendsPropertyInterface, PropertyInterface

    companion object {
        val SUT: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(JavaxValidationPlugin())
            .build()
    }
}
