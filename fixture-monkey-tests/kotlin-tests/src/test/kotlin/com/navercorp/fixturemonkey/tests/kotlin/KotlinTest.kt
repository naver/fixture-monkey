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
import com.navercorp.fixturemonkey.api.experimental.JavaGetterMethodPropertySelector.javaGetter
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeExperimentalBuilder
import com.navercorp.fixturemonkey.kotlin.instantiator.instantiateBy
import com.navercorp.fixturemonkey.tests.kotlin.JavaConstructorTestSpecs.JavaTypeObject
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenThrownBy
import org.junit.jupiter.api.Test

class KotlinTest {
    @Test
    fun kotlinObjectUseJavaGetterThrows() {
        class KotlinObject(val value: String)

        thenThrownBy {
            SUT.giveMeBuilder<KotlinObject>()
                .set(javaGetter(KotlinObject::value), "test")
                .sample()
                .value
        }.cause
            .isExactlyInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Kotlin type could not resolve property name.")
    }

    @Test
    fun javaGetter() {
        val expected = "test"

        val actual = SUT.giveMeExperimentalBuilder<JavaTypeObject>()
            .instantiateBy {
                constructor {
                    parameter<String>("string")
                    parameter<Int>()
                }
            }
            .set(javaGetter(JavaTypeObject::getString), expected)
            .sample()
            .string

        then(actual).isEqualTo(expected)
    }

    companion object {
        private val SUT: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()
    }
}
