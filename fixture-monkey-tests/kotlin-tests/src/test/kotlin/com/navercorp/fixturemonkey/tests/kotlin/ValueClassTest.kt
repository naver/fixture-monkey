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
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class ValueClassTest {
    @Test
    fun foo() {
        val foo: Foo = SUT.giveMeOne()

        then(foo)
        then(foo.bar).isNotNull
    }

    @Test
    fun valueClassProperty() {
        class ValueClassObject(val foo: Foo)

        val actual: ValueClassObject = SUT.giveMeOne()

        then(actual).isNotNull
        then(actual.foo).isNotNull
    }

    @Test
    fun valueClassPropertyFixed() {
        class ValueClassObject(val foo: Foo)

        val actual: ValueClassObject = SUT.giveMeBuilder<ValueClassObject>()
            .fixed()
            .sample()

        then(actual).isNotNull
        then(actual.foo).isNotNull
    }

    @Test
    fun privateConstructorValueClassProperty() {
        class ValueClassObject(val foo: FooWithPrivateConstructor)

        val actual: ValueClassObject = SUT.giveMeOne()

        then(actual).isNotNull
        then(actual.foo).isNotNull
    }

    @Test
    fun privateConstructorValueClassPropertyFixed() {
        class ValueClassObject(val foo: FooWithPrivateConstructor)

        val actual: ValueClassObject = SUT.giveMeBuilder<ValueClassObject>()
            .fixed()
            .sample()

        then(actual).isNotNull
        then(actual.foo).isNotNull
    }

    @JvmInline
    value class Foo(
        val bar: String,
    )

    @JvmInline
    value class FooWithPrivateConstructor private constructor(
        val bar: String
    )

    companion object {
        private val SUT = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()
    }
}
