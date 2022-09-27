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
import java.util.function.Consumer
import kotlin.reflect.KClass

class FixtureMonkeyExtensionsTest {
    private val sut: FixtureMonkey = KFixtureMonkey.create()

    @Property
    fun giveMe() {
        // when
        val actual = sut.giveMe<IntegerStringWrapperClass>().take(10).toList()

        then(actual).hasSize(10).allSatisfy(
            Consumer {
                with(it) {
                    then(intValue).isBetween(Int.MIN_VALUE, Int.MAX_VALUE)
                    then(stringValue).isNotNull()
                }
            }
        )
    }

    @Property
    fun giveMeWithCustomizer() {
        // when
        val actual = sut.giveMe(
            object : KArbitraryCustomizer<IntegerStringWrapperClass> {
                override fun customizeFields(
                    type: KClass<IntegerStringWrapperClass>,
                    fieldArbitraries: FieldArbitraries,
                ) {
                    fieldArbitraries.apply {
                        replaceArbitrary("intValue", Arbitraries.just(-1))
                    }
                }

                override fun customizeFixture(target: IntegerStringWrapperClass?): IntegerStringWrapperClass? {
                    return target?.copy(stringValue = "test_value")
                }
            }
        ).take(10).toList()

        then(actual).hasSize(10).allSatisfy(
            Consumer {
                with(it) {
                    then(intValue).isEqualTo(-1)
                    then(stringValue).isEqualTo("test_value")
                }
            }
        )
    }

    @Property
    fun giveMeWithCustomizerUsingTrailingLambda() {
        // when
        val actual = sut.giveMe<IntegerStringWrapperClass> {
            it?.copy(stringValue = "test_value")
        }.take(10).toList()

        then(actual).hasSize(10).allSatisfy(
            Consumer {
                with(it) {
                    then(stringValue).isEqualTo("test_value")
                }
            }
        )
    }

    @Property
    fun giveMeList() {
        // when
        val actual = sut.giveMe<IntegerStringWrapperClass>(10)

        then(actual).hasSize(10).allSatisfy(
            Consumer {
                with(it) {
                    then(intValue).isBetween(Int.MIN_VALUE, Int.MAX_VALUE)
                    then(stringValue).isNotNull()
                }
            }
        )
    }

    @Property
    fun giveMeListWithCustomizer() {
        // when
        val actual = sut.giveMe(
            10,
            object : KArbitraryCustomizer<IntegerStringWrapperClass> {
                override fun customizeFields(
                    type: KClass<IntegerStringWrapperClass>,
                    fieldArbitraries: FieldArbitraries,
                ) {
                    fieldArbitraries.apply {
                        replaceArbitrary("intValue", Arbitraries.just(-1))
                    }
                }

                override fun customizeFixture(target: IntegerStringWrapperClass?): IntegerStringWrapperClass? {
                    return target?.copy(stringValue = "test_value")
                }
            }
        )

        then(actual).hasSize(10).allSatisfy(
            Consumer {
                with(it) {
                    then(intValue).isEqualTo(-1)
                    then(stringValue).isEqualTo("test_value")
                }
            }
        )
    }

    @Property
    fun giveMeListWithCustomizerUsingTrailingLambda() {
        // when
        val actual = sut.giveMe<IntegerStringWrapperClass>(10) {
            it?.copy(stringValue = "test_value")
        }

        then(actual).hasSize(10).allSatisfy(
            Consumer {
                with(it) {
                    then(stringValue).isEqualTo("test_value")
                }
            }
        )
    }

    @Property
    fun giveMeOne() {
        // when
        val actual = sut.giveMeOne<IntegerStringWrapperClass>()

        with(actual) {
            then(intValue).isBetween(Int.MIN_VALUE, Int.MAX_VALUE)
            then(stringValue).isNotNull
        }
    }

    @Property
    fun giveMeOneWithCustomizer() {
        // when
        val actual = sut.giveMeOne(
            object : KArbitraryCustomizer<IntegerStringWrapperClass> {
                override fun customizeFields(
                    type: KClass<IntegerStringWrapperClass>,
                    fieldArbitraries: FieldArbitraries,
                ) {
                    fieldArbitraries.apply {
                        replaceArbitrary("intValue", Arbitraries.just(-1))
                    }
                }

                override fun customizeFixture(target: IntegerStringWrapperClass?): IntegerStringWrapperClass? {
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
    fun giveMeOneWithCustomizerUsingTrailingLambda() {
        // when
        val actual = sut.giveMeOne<IntegerStringWrapperClass> {
            it?.copy(stringValue = "test_value")
        }

        with(actual) {
            then(stringValue).isEqualTo("test_value")
        }
    }

    @Property
    fun giveMeArbitrary() {
        // when
        val actual = sut.giveMeArbitrary<IntegerStringWrapperClass>()

        then(actual).isNotNull
    }

    @Property
    fun giveMeBuilder() {
        // when
        val actual = sut.giveMeBuilder<IntegerStringWrapperClass>()

        then(actual).isNotNull
    }

    @Property
    fun giveMeBuilderWithOptions() {
        // when
        val actual = sut.giveMeBuilder<IntegerStringWrapperClass>(ArbitraryOption.builder().build())

        then(actual).isNotNull
    }

    @Property
    fun giveMeBuilderWithValue() {
        // when
        val value = IntegerStringWrapperClass(1, "test")
        val actual = sut.giveMeBuilder(value)

        then(actual).isNotNull
    }

    @Property
    fun giveMeBuilderSetWithProperty() {
        val actual = sut.giveMeBuilder<IntegerStringWrapperClass>()
            .set(IntegerStringWrapperClass::intValue, 1)
            .sample()

        then(actual.intValue).isEqualTo(1)
    }

    @Property
    fun giveMeOneListType() {
        val actual = sut.giveMeOne<List<String>>()

        then(actual).isNotNull
    }

    @Property
    fun giveMeBuilderListType() {
        val actual = sut.giveMeBuilder<List<String>>().sample()

        then(actual).isNotNull
    }

    data class IntegerStringWrapperClass(
        val intValue: Int,
        val stringValue: String,
    )
}
