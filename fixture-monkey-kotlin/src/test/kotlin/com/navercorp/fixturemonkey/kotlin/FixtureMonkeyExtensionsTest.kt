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

package com.navercorp.fixturemonkey.kotlin

import com.navercorp.fixturemonkey.ArbitraryOption
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.generator.FieldArbitraries
import com.navercorp.fixturemonkey.kotlin.customizer.KArbitraryCustomizer
import net.jqwik.api.Arbitraries
import net.jqwik.api.Property
import org.assertj.core.api.BDDAssertions.then
import kotlin.reflect.KClass

class FixtureMonkeyExtensionsTest {
    private val sut: FixtureMonkey = KFixtureMonkeyBuilder().build()

    @Property
    fun giveMeList() {
        val actual = sut.giveMe(TestClass::class, 10)

        then(actual).hasSize(10).allSatisfy {
            with(it) {
                then(intValue).isBetween(Int.MIN_VALUE, Int.MAX_VALUE)
                then(stringValue).isNotNull()
            }
        }
    }

    @Property
    fun giveMeListWithCustomizer() {
        val actual = sut.giveMe(
            TestClass::class,
            10,
            object : KArbitraryCustomizer<TestClass> {
                override fun customizeFields(type: KClass<TestClass>, fieldArbitraries: FieldArbitraries) {
                    fieldArbitraries.apply {
                        replaceArbitrary("intValue", Arbitraries.just(-1))
                    }
                }

                override fun customizeFixture(target: TestClass?): TestClass? {
                    return target?.copy(stringValue = "test_value")
                }
            }
        )

        then(actual).hasSize(10).allSatisfy {
            with(it) {
                then(intValue).isEqualTo(-1)
                then(stringValue).isEqualTo("test_value")
            }
        }
    }

    @Property
    fun giveMeOne() {
        val actual = sut.giveMeOne(TestClass::class)

        with(actual) {
            then(intValue).isBetween(Int.MIN_VALUE, Int.MAX_VALUE)
            then(stringValue).isNotNull()
        }
    }

    @Property
    fun giveMeOneWithCustomizer() {
        val actual = sut.giveMeOne(
            TestClass::class,
            object : KArbitraryCustomizer<TestClass> {
                override fun customizeFields(type: KClass<TestClass>, fieldArbitraries: FieldArbitraries) {
                    fieldArbitraries.apply {
                        replaceArbitrary("intValue", Arbitraries.just(-1))
                    }
                }

                override fun customizeFixture(target: TestClass?): TestClass? {
                    return target?.copy(stringValue = "test_value")
                }
            }
        )

        with(actual) {
            then(intValue).isEqualTo(-1)
            then(stringValue).isEqualTo("test_value")
        }
    }

    @Property
    fun giveMeArbitary() {
        val actual = sut.giveMeArbitrary(TestClass::class)

        then(actual).isNotNull
    }

    @Property
    fun giveMeArbitraryBuilder() {
        val actual = sut.giveMeArbitraryBuilder(TestClass::class)

        then(actual).isNotNull
    }

    @Property
    fun giveMeArbitraryBuilderWithOptions() {
        val actual = sut.giveMeArbitraryBuilder(TestClass::class, ArbitraryOption.builder().build())

        then(actual).isNotNull
    }

    @Property
    fun giveMeArbitraryBuilderWithValue() {
        val value = TestClass(1, "test")
        val actual = sut.giveMeArbitraryBuilder(value)

        then(actual).isNotNull
    }

    data class TestClass(
        val intValue: Int,
        val stringValue: String,
    )
}
