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
import com.navercorp.fixturemonkey.LabMonkey
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.spec.JavaObject
import net.jqwik.api.Property
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenNoException

class PrimaryConstructorArbitraryIntrospectorTest {
    private val sut: LabMonkey = FixtureMonkey.labMonkeyBuilder()
        .plugin(KotlinPlugin())
        .build()

    @Property
    fun samplePrimaryConstructor() {
        thenNoException().isThrownBy { sut.giveMeOne<PrimaryConstructor>() }
    }

    @Property
    fun sampleNested() {
        thenNoException().isThrownBy { sut.giveMeOne<Nested>().nested.intValue }
    }

    @Property
    fun sampleDataClass() {
        thenNoException().isThrownBy { sut.giveMeOne<DataValue>() }
    }

    @Property
    fun sampleVarValue() {
        thenNoException().isThrownBy { sut.giveMeOne<VarValue>() }
    }

    @Property
    fun sampleNullableValue() {
        thenNoException().isThrownBy { sut.giveMeOne<NullableValue>() }
    }

    @Property
    fun sampleDefaultValue() {
        // when
        val actual = sut.giveMeOne<DefaultValue>().stringValue

        then(actual).isNotEqualTo("default_value")
    }

    @Property
    fun sampleSecondaryConstructor() {
        // when
        val actual = sut.giveMeOne<SecondaryConstructor>().stringValue

        then(actual).isNotEqualTo("default_value")
    }

    @Property
    fun sampleInterface() {
        // when
        val actual = sut.giveMeOne<InterfaceClass>()

        then(actual).isNull()
    }

    @Property
    fun sampleJavaObject() {
        val sut: LabMonkey = FixtureMonkey.labMonkeyBuilder()
            .plugin(KotlinPlugin())
            .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
            .build()
        // when
        val actual = sut.giveMeOne<JavaObject>()

        then(actual).isNotNull
    }
}
