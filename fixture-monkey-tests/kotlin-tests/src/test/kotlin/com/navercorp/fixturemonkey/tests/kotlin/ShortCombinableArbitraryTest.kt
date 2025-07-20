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

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary
import io.kotest.matchers.nulls.shouldBeNull
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class ShortCombinableArbitraryTest {
    @Test
    fun combined() {
        // when
        val actual = CombinableArbitrary.shorts().combined()

        // then
        then(actual).isInstanceOf(Short::class.javaObjectType)
    }

    @Test
    fun withRange() {
        // given
        val min: Short = 100
        val max: Short = 200

        // when
        val actual = CombinableArbitrary.shorts().withRange(min, max).combined()

        // then
        then(actual).isBetween(min, max)
    }

    @Test
    fun positive() {
        // when
        val allPositive = (0 until 100)
            .map { CombinableArbitrary.shorts().positive().combined() }
            .all { it > 0 }

        // then
        then(allPositive).isTrue()
    }

    @Test
    fun negative() {
        // when
        val allNegative = (0 until 100)
            .map { CombinableArbitrary.shorts().negative().combined() }
            .all { it < 0 }

        // then
        then(allNegative).isTrue()
    }

    @Test
    fun even() {
        // when
        val allEven = (0 until 100)
            .map { CombinableArbitrary.shorts().even().combined() }
            .all { it % 2 == 0 }

        // then
        then(allEven).isTrue()
    }

    @Test
    fun odd() {
        // when
        val allOdd = (0 until 100)
            .map { CombinableArbitrary.shorts().odd().combined() }
            .all { it % 2 != 0 }

        // then
        then(allOdd).isTrue()
    }

    @Test
    fun nonZero() {
        // when
        val allNonZero = (0 until 100)
            .map { CombinableArbitrary.shorts().nonZero().combined() }
            .all { it != 0.toShort() }

        // then
        then(allNonZero).isTrue()
    }

    @Test
    fun multipleOf() {
        // given
        val multiplier: Short = 7

        // when
        val allMultiplesOfSeven = (0 until 100)
            .map { CombinableArbitrary.shorts().multipleOf(multiplier).combined() }
            .all { it % multiplier == 0 }

        // then
        then(allMultiplesOfSeven).isTrue()
    }

    @Test
    fun percentage() {
        // when
        val allPercentage = (0 until 100)
            .map { CombinableArbitrary.shorts().percentage().combined() }
            .all { it in 0..100 }

        // then
        then(allPercentage).isTrue()
    }

    @Test
    fun score() {
        // when
        val allScore = (0 until 100)
            .map { CombinableArbitrary.shorts().score().combined() }
            .all { it in 0..100 }

        // then
        then(allScore).isTrue()
    }

    @Test
    fun year() {
        // when
        val allYear = (0 until 100)
            .map { CombinableArbitrary.shorts().year().combined() }
            .all { it in 1900..2100 }

        // then
        then(allYear).isTrue()
    }

    @Test
    fun month() {
        // when
        val allMonth = (0 until 100)
            .map { CombinableArbitrary.shorts().month().combined() }
            .all { it in 1..12 }

        // then
        then(allMonth).isTrue()
    }

    @Test
    fun day() {
        // when
        val allDay = (0 until 100)
            .map { CombinableArbitrary.shorts().day().combined() }
            .all { it in 1..31 }

        // then
        then(allDay).isTrue()
    }

    @Test
    fun hour() {
        // when
        val allHour = (0 until 100)
            .map { CombinableArbitrary.shorts().hour().combined() }
            .all { it in 0..23 }

        // then
        then(allHour).isTrue()
    }

    @Test
    fun minute() {
        // when
        val allMinute = (0 until 100)
            .map { CombinableArbitrary.shorts().minute().combined() }
            .all { it in 0..59 }

        // then
        then(allMinute).isTrue()
    }

    @Test
    fun shortMapping() {
        // when
        val actual = CombinableArbitrary.shorts()
            .positive()
            .map { "short:$it" }
            .combined()

        // then
        then(actual).startsWith("short:")
        val numberPart = actual.substring(6)
        val value = numberPart.toShort()
        then(value).isGreaterThan(0.toShort())
    }

    @Test
    fun shortFiltering() {
        // when
        val actual = CombinableArbitrary.shorts()
            .withRange(0.toShort(), 1000.toShort())
            .filter { it > 500 }
            .combined()
        // then
        then(actual).isGreaterThan(500.toShort())
        then(actual).isLessThanOrEqualTo(1000.toShort())
    }

    @Test
    fun shortInjectNull() {
        // when
        val actual = CombinableArbitrary.shorts()
            .positive()
            .injectNull(1.0)
            .combined()

        // then
        actual.shouldBeNull()
    }

    @Test
    fun shortInjectNullWithZeroProbability() {
        // when
        val actual = CombinableArbitrary.shorts()
            .positive()
            .injectNull(0.0)
            .combined()

        // then
        then(actual).isNotNull()
        then(actual!!).isGreaterThan(0.toShort())
    }

    @Test
    fun fixed() {
        // when
        val actual = CombinableArbitrary.shorts().fixed()

        // then
        then(actual).isFalse()
    }

    @Test
    fun nonZeroWithMultipleOf() {
        // when
        val allNonZeroMultiplesOfFive = (0 until 100)
            .map { CombinableArbitrary.shorts().nonZero().multipleOf(5).combined() }
            .all { it != 0.toShort() && it % 5 == 0 }

        // then
        then(allNonZeroMultiplesOfFive).isTrue()
    }

    @Test
    fun lastMethodWinsYearOverPercentage() {
        // when - percentage().year() => year()
        val allYear = (0 until 100)
            .map { CombinableArbitrary.shorts().percentage().year().combined() }
            .all { it in 1900..2100 }

        // then
        then(allYear).isTrue()
    }

    @Test
    fun lastMethodWinsMultipleOfOverEven() {
        // when - even().multipleOf(3) => multipleOf(3)
        val allMultiplesOfThree = (0 until 100)
            .map { CombinableArbitrary.shorts().even().multipleOf(3).combined() }
            .all { it % 3 == 0 }

        // then
        then(allMultiplesOfThree).isTrue()
    }
}
