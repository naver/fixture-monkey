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

import org.junit.jupiter.api.Test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.set
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

    @Test
    fun samplePrimaryConstructor() {
        thenNoException().isThrownBy { sut.giveMeOne<PrimaryConstructor>() }
    }

    @Test
    fun sampleNested() {
        thenNoException().isThrownBy { sut.giveMeOne<Nested>().nested.intValue }
    }

    @Test
    fun sampleDataClass() {
        thenNoException().isThrownBy { sut.giveMeOne<DataValue>() }
    }

    @Test
    fun sampleVarValue() {
        thenNoException().isThrownBy { sut.giveMeOne<VarValue>() }
    }

    @Test
    fun sampleNullableValue() {
        thenNoException().isThrownBy { sut.giveMeOne<NullableValue>() }
    }

    @Test
    fun sampleDefaultValue() {
        // when
        val actual = sut.giveMeOne<DefaultValue>().stringValue

        then(actual).isNotEqualTo("default_value")
    }

    @Test
    fun sampleDefaultValueWhenOptionalParameterIsSkipped() {
        // when
        val actual = sut.giveMeKotlinBuilder<DefaultValue>()
            .set(DefaultValue::stringValue, null)
            .sample()

        then(actual.stringValue).isEqualTo("default_value")
    }

    @Test
    fun sampleNullableDefaultValue() {
        // when
        val actual = sut.giveMeKotlinBuilder<NullableDefaultValue>()
            .set(NullableDefaultValue::stringValue, null)
            .sample()

        then(actual.stringValue).isNull()
    }

    @Test
    fun sampleDuration() {
        // when
        val duration = sut.giveMeOne<Duration>()
        then(duration).isNotNull()
    }

    @Test
    fun sampleDurationValue() {
        // when
        val durationValue = sut.giveMeBuilder<DurationValue>()
            .sample()

        then(durationValue.duration).isNotNull()
    }

    @Test
    fun setDurationValue() {
        // given
        val duration = Random().nextLong().toDuration(DurationUnit.values().random())

        // when
        val one = sut.giveMeKotlinBuilder<DurationValue>()
            .set(DurationValue::duration, duration)
            .sample()

        // then
        then(one.duration).isEqualTo(duration)
    }

    @Test
    fun sampleGenericDuration() {
        // when
        val one = sut.giveMeOne<List<Duration>>()

        assertAll(
            { one.forEach { then(it).isNotNull() } },
            { then(one.size).isGreaterThanOrEqualTo(0) }
        )
    }

    @Test
    fun sampleSecondaryConstructor() {
        // when
        val actual = sut.giveMeOne<SecondaryConstructor>().stringValue

        then(actual).isNotEqualTo("default_value")
    }

    @Test
    fun sampleInterface() {
        // when
        val actual: InterfaceClass = sut.giveMeOne()

        then(actual).isNull()
    }
}
