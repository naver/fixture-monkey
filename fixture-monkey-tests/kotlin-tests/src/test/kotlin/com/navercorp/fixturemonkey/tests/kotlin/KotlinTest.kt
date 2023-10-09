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
import com.navercorp.fixturemonkey.jackson.plugin.JacksonPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenThrownBy
import org.junit.jupiter.api.Test

class KotlinTest {
    @Test
    fun throwIfFailed() {
        // given
        val sut = FixtureMonkey.builder()
            .throwIfFailed(true)
            .build()

        class Foo(val foo: String)

        // when, then
        thenThrownBy {
            sut.giveMeOne<Foo>()
        }.isExactlyInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("No-args constructor is not found.")
    }

    @Test
    fun notThrowIfFailed() {
        // given
        val sut = FixtureMonkey.builder()
            .throwIfFailed(false)
            .build()

        class Foo(val foo: String)

        // when
        val actual: Foo = sut.giveMeOne()

        then(actual).isNull()
    }

    @Test
    fun throwIfFailedWithJackson() {
        // given
        val sut = FixtureMonkey.builder()
            .plugin(JacksonPlugin())
            .throwIfFailed(true)
            .build()

        class Foo(val foo: String)

        // when, when
        thenThrownBy {
            sut.giveMeOne<Foo>()
        }.isExactlyInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Jackson fails to deserialize the generated value.")
    }

    @Test
    fun notThrowIfFailedWithJackson() {
        // given
        val sut = FixtureMonkey.builder()
            .plugin(JacksonPlugin())
            .throwIfFailed(false)
            .build()

        class Foo(val foo: String)

        // when
        val actual: Foo = sut.giveMeOne()

        // then
        then(actual).isNull()
    }

    companion object {
        private val SUT: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()
    }
}
