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
import com.navercorp.fixturemonkey.api.introspector.AnonymousArbitraryIntrospector
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator
import com.navercorp.fixturemonkey.api.type.Types
import com.navercorp.fixturemonkey.customizer.Values
import com.navercorp.fixturemonkey.javax.validation.plugin.JavaxValidationPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.generator.InterfaceKFunctionPropertyGenerator
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.setExpGetter
import com.navercorp.fixturemonkey.tests.TestEnvironment.*
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenNoException
import org.junit.jupiter.api.RepeatedTest
import java.lang.reflect.Modifier
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

        then(actual).isNotNull
        then(actual.string("str")).isNotNull
        then(actual.integer(1)).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleAnonymousSetMethod() {
        val actual = SUT.giveMeBuilder<InterfaceWithParams>()
            .set("string", Values.method { objects -> objects[0] as String + "$" })
            .sample()
            .string("test")

        then(actual).isEqualTo("test$")
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

    interface Interface {
        fun string(): String
        fun integer(): Int
    }

    interface InterfaceWithParams {
        fun string(str: String): String
        fun integer(int: Int): Int
    }

    interface ContainerInterface {
        fun list(): List<String>
        fun map(): Map<String, Int>
    }

    interface AnnotatedInterface {
        @NotEmpty
        fun string(): String
    }

    companion object {
        val SUT: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(JavaxValidationPlugin())
            .fallbackIntrospector(AnonymousArbitraryIntrospector.INSTANCE)
            .pushPropertyGenerator(
                MatcherOperator(
                    { p -> Modifier.isInterface(Types.getActualType(p.type).modifiers) },
                    InterfaceKFunctionPropertyGenerator()
                )
            )
            .build()
    }
}
