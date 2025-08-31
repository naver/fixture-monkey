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
import com.navercorp.fixturemonkey.api.plugin.SimpleValueJqwikPlugin
import com.navercorp.fixturemonkey.javax.validation.plugin.JavaxValidationPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.validation.constraints.Max
import javax.validation.constraints.Min

class SimpleValueJqwikPluginTest {
    @RepeatedTest(TEST_COUNT)
    fun sampleInt() {
        val sut = FixtureMonkey.builder()
            .plugin(SimpleValueJqwikPlugin())
            .build()

        val actual: Int = sut.giveMeOne()

        then(actual).isBetween(-10000, 10000)
    }

    @RepeatedTest(TEST_COUNT)
    fun modifyNumberValue() {
        val sut = FixtureMonkey.builder()
            .plugin(
                SimpleValueJqwikPlugin()
                    .minNumberValue(-1)
                    .maxNumberValue(1)
            )
            .build()

        val actual: Int = sut.giveMeOne()

        then(actual).isBetween(-1, 1)
    }

    @RepeatedTest(TEST_COUNT)
    fun modifyStringLength() {
        val sut = FixtureMonkey.builder()
            .plugin(
                SimpleValueJqwikPlugin()
                    .minStringLength(2)
                    .maxStringLength(3)
            )
            .build()

        val actual: String = sut.giveMeOne()

        then(actual).hasSizeBetween(2, 3)
    }

    @RepeatedTest(TEST_COUNT)
    fun modifyCharacterPredicate() {
        val sut = FixtureMonkey.builder()
            .plugin(
                SimpleValueJqwikPlugin()
                    .characterPredicate { 50.toChar() <= it && it <= 100.toChar() }
            )
            .build()

        val actual: String = sut.giveMeOne()

        then(actual.chars()).allMatch { it in 50..100 }
    }

    @RepeatedTest(TEST_COUNT)
    fun modifyContainerSize() {
        val sut = FixtureMonkey.builder()
            .plugin(
                SimpleValueJqwikPlugin()
                    .minContainerSize(2)
                    .maxContainerSize(3)
            )
            .build()

        val actual: List<String> = sut.giveMeOne()

        then(actual).hasSizeBetween(2, 3)
    }

    @RepeatedTest(TEST_COUNT)
    fun modifyDate() {
        val sut = FixtureMonkey.builder()
            .plugin(
                SimpleValueJqwikPlugin()
                    .plusDaysFromToday(1L)
                    .minusDaysFromToday(1L)
            )
            .build()
        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS)

        val actual: Instant = sut.giveMeOne()

        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS)
        then(actual).isBetween(yesterday, tomorrow)
    }

    @RepeatedTest(TEST_COUNT)
    fun withValidationPlugin() {
        // given
        class JavaxValidationAnnotationValue(
            @field:Min(20) @field:Max(30) val annotatedInt: Int,
            val notAnnotatedInt: Int
        )

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(JavaxValidationPlugin())
            .plugin(SimpleValueJqwikPlugin())
            .build()

        // when
        val actual: JavaxValidationAnnotationValue = sut.giveMeOne()

        then(actual.annotatedInt).isBetween(20, 30)
        then(actual.notAnnotatedInt).isBetween(-10000, 10000)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleSetObject() {
        class SetObject(val integers: Set<Int>)

        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(SimpleValueJqwikPlugin())
            .build()

        val setObject = sut.giveMeKotlinBuilder<SetObject>()
            .size("integers", 3)
            .sample()


        then(setObject).isNotNull
        then(setObject.integers).hasSize(3)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleByte() {
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(SimpleValueJqwikPlugin())
            .build()

        val actual: Byte = sut.giveMeOne()

        then(actual).isBetween(Byte.MIN_VALUE, Byte.MAX_VALUE)
    }

    @RepeatedTest(TEST_COUNT)
    fun modifyNumberValueByte() {
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(
                SimpleValueJqwikPlugin()
                    .minNumberValue(-1)
                    .maxNumberValue(1)
            )
            .build()

        val actual: Byte = sut.giveMeOne()

        then(actual).isBetween(-1, 1)
    }

    @RepeatedTest(TEST_COUNT)
    fun modifyOutOfRangeNumberValueByte() {
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(
                SimpleValueJqwikPlugin()
                    .minNumberValue(-129)
                    .maxNumberValue(129)
            )
            .build()

        val actual: Byte = sut.giveMeOne()

        then(actual).isBetween(Byte.MIN_VALUE, Byte.MAX_VALUE)
    }

    @RepeatedTest(TEST_COUNT)
    fun setPositiveMinNumberReturnsPositive() {
        val sut = FixtureMonkey.builder()
            .plugin(SimpleValueJqwikPlugin().minNumberValue(0))
            .build()

        val actual: Long = sut.giveMeOne()

        then(actual).isGreaterThanOrEqualTo(0);
    }

    @RepeatedTest(TEST_COUNT)
    fun setNegativeMaxNumberReturnsNegative() {
        val sut = FixtureMonkey.builder()
            .plugin(SimpleValueJqwikPlugin().maxNumberValue(-0))
            .build()

        val actual: Long = sut.giveMeOne()

        then(actual).isLessThanOrEqualTo(0);
    }

    @RepeatedTest(TEST_COUNT)
    fun modifyPositiveDecimalNumberRange() {
        val sut = FixtureMonkey.builder()
            .plugin(
                SimpleValueJqwikPlugin()
                    .minNumberValue(1)
                    .maxNumberValue(10)
            )
            .build()

        val actual: Double = sut.giveMeOne()

        then(actual).isBetween(1.0, 10.0)
    }

    @RepeatedTest(TEST_COUNT)
    fun modifyNegativeDecimalNumberRange() {
        val sut = FixtureMonkey.builder()
            .plugin(
                SimpleValueJqwikPlugin()
                    .minNumberValue(-10)
                    .maxNumberValue(-1)
            )
            .build()

        val actual: Double = sut.giveMeOne()

        then(actual).isBetween(-10.0, -1.0)
    }

    @RepeatedTest(TEST_COUNT)
    fun modifyMixedDecimalNumberRange() {
        val sut = FixtureMonkey.builder()
            .plugin(
                SimpleValueJqwikPlugin()
                    .minNumberValue(-5)
                    .maxNumberValue(5)
            )
            .build()

        val actual: Double = sut.giveMeOne()

        then(actual).isBetween(-5.0, 5.0)
    }

    @Test
    fun simpleValueJqwikPluginOnlyGreaterThan() {
        // given
        val expected = 10001L
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(
                SimpleValueJqwikPlugin()
                    .minNumberValue(expected)
            )
            .build()

        // when
        val actual: Long = sut.giveMeOne()

        // then
        then(actual).isGreaterThanOrEqualTo(expected)
    }

    @Test
    fun simpleValueJqwikPluginMinStringLengthGreaterThanDefault() {
        // given
        val expected = 10L
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(
                SimpleValueJqwikPlugin()
                    .minStringLength(expected)
            )
            .build()

        // when
        val actual: String = sut.giveMeOne()

        // then
        then(actual.length).isGreaterThanOrEqualTo(expected.toInt())
    }

    @Test
    fun simpleValueJqwikPluginMinContainerSizeGreaterThanDefault() {
        // given
        val expected = 5
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(
                SimpleValueJqwikPlugin()
                    .minContainerSize(expected)
            )
            .build()

        // when
        val actual: List<String> = sut.giveMeOne<List<String>>()

        // then
        then(actual.size).isGreaterThanOrEqualTo(expected)
    }

    @Test
    fun simpleValueJqwikPluginMinusDaysFromTodayGreaterThanDefault() {
        // given
        val expected = 400L
        val sut = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .plugin(
                SimpleValueJqwikPlugin()
                    .minusDaysFromToday(expected)
            )
            .build()

        // when
        val actual: LocalDateTime = sut.giveMeOne()

        // then
        val expectedMinDate = LocalDateTime.now().minusDays(expected)
        val expectedMaxDate = LocalDateTime.now().plusDays(expected + 30L)
        then(actual).isBetween(expectedMinDate, expectedMaxDate)
    }
}
