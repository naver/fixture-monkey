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
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.set
import net.jqwik.api.Example
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenNoException
import org.junit.jupiter.api.assertAll
import java.util.Random
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class PrimaryConstructorArbitraryIntrospectorTest {
    private val sut: FixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .build()

    @Example
    fun samplePrimaryConstructor() {
        thenNoException().isThrownBy { sut.giveMeOne<PrimaryConstructor>() }
    }

    @Example
    fun sampleNested() {
        thenNoException().isThrownBy { sut.giveMeOne<Nested>().nested.intValue }
    }

    @Example
    fun sampleDataClass() {
        thenNoException().isThrownBy { sut.giveMeOne<DataValue>() }
    }

    @Example
    fun sampleVarValue() {
        thenNoException().isThrownBy { sut.giveMeOne<VarValue>() }
    }

    @Example
    fun sampleNullableValue() {
        thenNoException().isThrownBy { sut.giveMeOne<NullableValue>() }
    }

    @Example
    fun sampleDefaultValue() {
        // when
        val actual = sut.giveMeOne<DefaultValue>().stringValue

        then(actual).isNotEqualTo("default_value")
    }

    @Example
    fun sampleDuration() {
        // when
        val duration = sut.giveMeOne<Duration>()
        then(duration).isNotNull()
    }

    @Example
    fun sampleDurationValue() {
        // when
        val durationValue = sut.giveMeBuilder<DurationValue>()
            .sample()

        then(durationValue.duration).isNotNull()
    }

    @Example
    fun setDurationValue() {
        // given
        val duration = Random().nextLong().toDuration(DurationUnit.values().random())

        // when
        val one = sut.giveMeBuilder<DurationValue>()
            .set(DurationValue::duration, duration)
            .sample()

        // then
        then(one.duration).isEqualTo(duration)
    }

    @Example
    fun sampleGenericDuration() {
        // when
        val one = sut.giveMeOne<List<Duration>>()

        assertAll(
            { one.forEach { then(it).isNotNull() } },
            { then(one.size).isGreaterThanOrEqualTo(0) }
        )
    }

    @Example
    fun sampleSecondaryConstructor() {
        // when
        val actual = sut.giveMeOne<SecondaryConstructor>().stringValue

        then(actual).isNotEqualTo("default_value")
    }

    @Example
    fun sampleInterface() {
        // when
        val actual: InterfaceClass = sut.giveMeOne()

        then(actual).isNull()
    }
}
