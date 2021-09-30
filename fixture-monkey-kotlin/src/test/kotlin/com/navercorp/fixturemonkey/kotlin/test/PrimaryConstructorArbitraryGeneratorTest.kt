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

package com.navercorp.fixturemonkey.kotlin.test

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KFixtureMonkey
import net.jqwik.api.Property
import org.assertj.core.api.BDDAssertions.then

class PrimaryConstructorArbitraryGeneratorTest {
    private val sut: FixtureMonkey = KFixtureMonkey.create()

    @Property
    fun giveMeClassWithPrimaryConstructor() {
        // when
        val actual = this.sut.giveMe(ClassWithPrimaryConstructor::class.java, 10)

        then(actual).hasSize(10)
    }

    @Property
    fun giveMeClassWithNestedOne() {
        // when
        val actual = this.sut.giveMe(ClassWithNestedOne::class.java, 10)

        then(actual).hasSize(10)
    }

    @Property
    fun giveMeDataClass() {
        // when
        val actual = this.sut.giveMe(DataClass::class.java, 10)

        then(actual).hasSize(10)
    }

    @Property
    fun giveMeClassWithVarValue() {
        // when
        val actual = this.sut.giveMe(ClassWithVarValue::class.java, 10)

        then(actual).hasSize(10)
    }

    @Property
    fun giveMeClassWithNullable() {
        // when
        val actual = this.sut.giveMe(ClassWithNullable::class.java, 10)

        then(actual).hasSize(10)
    }

    @Property
    fun giveMeClassWithDefaultValue() {
        // when
        val actual = this.sut.giveMe(ClassWithDefaultValue::class.java, 10)

        // then
        then(actual).hasSize(10).allSatisfy {
            with(it) {
                then(stringValue).isNotEqualTo("default_value")
            }
        }
    }

    @Property
    fun giveMeClassWithSecondaryConstructor() {
        // when
        val actual = this.sut.giveMe(ClassWithSecondaryConstructor::class.java, 10)

        then(actual).hasSize(10).allSatisfy {
            with(it) {
                then(stringValue).isNotEqualTo("default_value")
            }
        }
    }

    @Property
    fun giveMeInterfaceClass() {
        // when
        val actual = this.sut.giveMeOne(InterfaceClass::class.java)

        then(actual).isNull()
    }

    class ClassWithPrimaryConstructor(
        val intValue: Int,
        val stringValue: String
    )

    class NestedClass(
        val intValue: Int
    )

    class ClassWithNestedOne(
        val nestedClass: NestedClass
    )

    data class DataClass(
        val intValue: Int,
        val stringValue: String
    )

    class ClassWithVarValue(
        var intValue: Int,
        var stringValue: String
    )

    class ClassWithNullable(
        val intValue: Int,
        val stringValue: String?
    )

    class ClassWithDefaultValue(
        val intValue: Int,
        val stringValue: String = "default_value"
    )

    class ClassWithSecondaryConstructor(
        val intValue: Int,
        val stringValue: String
    ) {
        constructor(another: String) : this(0, "default_value")
    }

    interface InterfaceClass {
        fun test()
    }
}
