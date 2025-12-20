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
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary
import com.navercorp.fixturemonkey.javax.validation.plugin.JavaxValidationPlugin
import com.navercorp.fixturemonkey.kotest.KotestBigDecimalCombinableArbitrary
import com.navercorp.fixturemonkey.kotest.KotestBigIntegerCombinableArbitrary
import com.navercorp.fixturemonkey.kotest.KotestCharacterCombinableArbitrary
import com.navercorp.fixturemonkey.kotest.KotestIntegerCombinableArbitrary
import com.navercorp.fixturemonkey.kotest.KotestByteCombinableArbitrary
import com.navercorp.fixturemonkey.kotest.KotestLongCombinableArbitrary
import com.navercorp.fixturemonkey.kotest.KotestPlugin
import com.navercorp.fixturemonkey.kotest.KotestShortCombinableArbitrary
import com.navercorp.fixturemonkey.kotest.KotestStringCombinableArbitrary
import com.navercorp.fixturemonkey.kotest.giveMeArb
import com.navercorp.fixturemonkey.kotest.setArb
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.setPostCondition
import com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT
import io.kotest.property.Arb
import io.kotest.property.arbitrary.single
import io.kotest.property.arbs.geo.zipcodes
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.BigInteger
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Digits
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.Negative
import javax.validation.constraints.NegativeOrZero
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Positive
import javax.validation.constraints.PositiveOrZero
import javax.validation.constraints.Size

class KotestInJunitTest {
    @RepeatedTest(TEST_COUNT)
    fun sampleStringWithSize() {
        class StringObject(@field:Size(min = 10, max = 20) val value: String)

        val actual = SUT.giveMeOne<StringObject>().value

        then(actual).hasSizeBetween(10, 20)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleStringWithNotEmpty() {
        class StringObject(@field:NotEmpty val value: String)

        val actual = SUT.giveMeOne<StringObject>().value

        then(actual).isNotEmpty()
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleShortWithNegative() {
        class ShortObject(@field:Negative val value: Short)

        val actual = SUT.giveMeOne<ShortObject>().value

        then(actual).isNegative()
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleShortWithNegativeOrZero() {
        class ShortObject(@field:NegativeOrZero val value: Short)

        val actual = SUT.giveMeOne<ShortObject>().value

        then(actual).isLessThanOrEqualTo(0)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleShortWithPositive() {
        class ShortObject(@field:Positive val value: Short)

        val actual = SUT.giveMeOne<ShortObject>().value

        then(actual).isPositive()
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleShortWithPositiveOrZero() {
        class ShortObject(@field:PositiveOrZero val value: Short)

        val actual = SUT.giveMeOne<ShortObject>().value

        then(actual).isGreaterThanOrEqualTo(0)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleShortWithMin() {
        class ShortObject(@field:Min(10) val value: Short)

        val actual = SUT.giveMeOne<ShortObject>().value

        then(actual).isGreaterThanOrEqualTo(10)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleShortWithDecimalMin() {
        class ShortObject(@field:DecimalMin("10") val value: Short)

        val actual = SUT.giveMeOne<ShortObject>().value

        then(actual).isGreaterThanOrEqualTo(10)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleShortWithMax() {
        class ShortObject(@field:Max(50) val value: Short)

        val actual = SUT.giveMeOne<ShortObject>().value

        then(actual).isLessThanOrEqualTo(50)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleShortWithDecimalMax() {
        class ShortObject(@field:DecimalMax("50") val value: Short)

        val actual = SUT.giveMeOne<ShortObject>().value

        then(actual).isLessThanOrEqualTo(50)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleShortWithDigits() {
        class ShortObject(@field:Digits(integer = 2, fraction = 0) val value: Short)

        val actual = SUT.giveMeOne<ShortObject>().value

        then(actual).matches { it in -99..99 }
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleByteWithNegative() {
        class ByteObject(@field:Negative val value: Byte)

        val actual = SUT.giveMeOne<ByteObject>().value

        then(actual).isNegative()
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleByteWithNegativeOrZero() {
        class ByteObject(@field:NegativeOrZero val value: Byte)

        val actual = SUT.giveMeOne<ByteObject>().value

        then(actual).isLessThanOrEqualTo(0)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleByteWithPositive() {
        class ByteObject(@field:Positive val value: Byte)

        val actual = SUT.giveMeOne<ByteObject>().value

        then(actual).isPositive()
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleByteWithPositiveOrZero() {
        class ByteObject(@field:PositiveOrZero val value: Byte)

        val actual = SUT.giveMeOne<ByteObject>().value

        then(actual).isGreaterThanOrEqualTo(0)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleByteWithMin() {
        class ByteObject(@field:Min(10) val value: Byte)

        val actual = SUT.giveMeOne<ByteObject>().value

        then(actual).isGreaterThanOrEqualTo(10)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleByteWithDecimalMin() {
        class ByteObject(@field:DecimalMin("10") val value: Byte)

        val actual = SUT.giveMeOne<ByteObject>().value

        then(actual).isGreaterThanOrEqualTo(10)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleByteWithMax() {
        class ByteObject(@field:Max(50) val value: Byte)

        val actual = SUT.giveMeOne<ByteObject>().value

        then(actual).isLessThanOrEqualTo(50)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleByteWithDecimalMax() {
        class ByteObject(@field:DecimalMax("50") val value: Byte)

        val actual = SUT.giveMeOne<ByteObject>().value

        then(actual).isLessThanOrEqualTo(50)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleByteWithDigits() {
        class ByteObject(@field:Digits(integer = 2, fraction = 0) val value: Byte)

        val actual = SUT.giveMeOne<ByteObject>().value

        then(actual).matches { it in -99..99 }
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleDoubleWithNegative() {
        class DoubleObject(@field:Negative val value: Double)

        val actual = SUT.giveMeOne<DoubleObject>().value

        then(actual).isNegative()
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleDoubleWithNegativeOrZero() {
        class DoubleObject(@field:NegativeOrZero val value: Double)

        val actual = SUT.giveMeOne<DoubleObject>().value

        then(actual).isLessThanOrEqualTo(0.0)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleDoubleWithPositive() {
        class DoubleObject(@field:Positive val value: Double)

        val actual = SUT.giveMeOne<DoubleObject>().value

        then(actual).isPositive()
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleDoubleWithPositiveOrZero() {
        class DoubleObject(@field:PositiveOrZero val value: Double)

        val actual = SUT.giveMeOne<DoubleObject>().value

        then(actual).isGreaterThanOrEqualTo(0.0)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleDoubleWithMin() {
        class DoubleObject(@field:Min(10) val value: Double)

        val actual = SUT.giveMeOne<DoubleObject>().value

        then(actual).isGreaterThanOrEqualTo(10.0)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleDoubleWithDecimalMin() {
        class DoubleObject(@field:DecimalMin("10") val value: Double)

        val actual = SUT.giveMeOne<DoubleObject>().value

        then(actual).isGreaterThanOrEqualTo(10.0)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleDoubleWithMax() {
        class DoubleObject(@field:Max(50) val value: Double)

        val actual = SUT.giveMeOne<DoubleObject>().value

        then(actual).isLessThanOrEqualTo(50.0)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleDoubleWithDecimalMax() {
        class DoubleObject(@field:DecimalMax("50") val value: Double)

        val actual = SUT.giveMeOne<DoubleObject>().value

        then(actual).isLessThanOrEqualTo(50.0)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleDoubleWithDigits() {
        class DoubleObject(@field:Digits(integer = 2, fraction = 0) val value: Double)

        val actual = SUT.giveMeOne<DoubleObject>().value

        then(actual).matches { it in -99.0..99.0 }
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleDoubleWithEqualMinMax() {
        class DoubleObject(
            @field:DecimalMin("10.0") @field:DecimalMax("10.0")
            val value: Double
        )

        val actual = SUT.giveMeOne<DoubleObject>().value
        then(actual).isEqualTo(10.0)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleDoubleWithMultipleConstraints() {
        class DoubleObject(
            @field:DecimalMax("11.5") @field:Max(10)
            val value: Double
        )

        val actual = SUT.giveMeOne<DoubleObject>().value
        then(actual).isLessThanOrEqualTo(10.0)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleDoubleWithPreciseExclusiveBounds() {
        class DoubleObject(
            @field:DecimalMin(value = "10.0", inclusive = false)
            @field:DecimalMax(value = "10.1", inclusive = false)
            val value: Double
        )

        val actual = SUT.giveMeOne<DoubleObject>().value

        then(actual)
            .isGreaterThan(10.0)
            .isLessThan(10.1)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleFloatWithNegative() {
        class FloatObject(@field:Negative val value: Float)

        val actual = SUT.giveMeOne<FloatObject>().value

        then(actual).isNegative()
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleFloatWithNegativeOrZero() {
        class FloatObject(@field:NegativeOrZero val value: Float)

        val actual = SUT.giveMeOne<FloatObject>().value

        then(actual).isLessThanOrEqualTo(0.0f)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleFloatWithPositive() {
        class FloatObject(@field:Positive val value: Float)

        val actual = SUT.giveMeOne<FloatObject>().value

        then(actual).isPositive()
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleFloatWithPositiveOrZero() {
        class FloatObject(@field:PositiveOrZero val value: Float)

        val actual = SUT.giveMeOne<FloatObject>().value

        then(actual).isGreaterThanOrEqualTo(0.0f)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleFloatWithMin() {
        class FloatObject(@field:Min(10) val value: Float)

        val actual = SUT.giveMeOne<FloatObject>().value

        then(actual).isGreaterThanOrEqualTo(10.0f)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleFloatWithDecimalMin() {
        class FloatObject(@field:DecimalMin("10") val value: Float)

        val actual = SUT.giveMeOne<FloatObject>().value

        then(actual).isGreaterThanOrEqualTo(10.0f)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleFloatWithMax() {
        class FloatObject(@field:Max(50) val value: Float)

        val actual = SUT.giveMeOne<FloatObject>().value

        then(actual).isLessThanOrEqualTo(50.0f)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleFloatWithDecimalMax() {
        class FloatObject(@field:DecimalMax("50") val value: Float)

        val actual = SUT.giveMeOne<FloatObject>().value

        then(actual).isLessThanOrEqualTo(50.0f)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleFloatWithDigits() {
        class FloatObject(@field:Digits(integer = 2, fraction = 0) val value: Float)

        val actual = SUT.giveMeOne<FloatObject>().value

        then(actual).matches { it in -99.0..99.0 }
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleFloatWithEqualMinMax() {
        class DoubleObject(
            @field:DecimalMin("10.0") @field:DecimalMax("10.0")
            val value: Double
        )

        val actual = SUT.giveMeOne<DoubleObject>().value
        then(actual).isEqualTo(10.0)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleFloatWithMultipleConstraints() {
        class DoubleObject(
            @field:DecimalMax("11.5") @field:Max(10)
            val value: Double
        )

        val actual = SUT.giveMeOne<DoubleObject>().value
        then(actual).isLessThanOrEqualTo(10.0)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleFloatWithPreciseExclusiveBounds() {
        class FloatObject(
            @field:DecimalMin(value = "10.0", inclusive = false)
            @field:DecimalMax(value = "10.1", inclusive = false)
            val value: Float
        )

        val actual = SUT.giveMeOne<FloatObject>().value

        then(actual)
            .isGreaterThan(10.0f)
            .isLessThan(10.1f)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleIntWithNegative() {
        class IntObject(@field:Negative val value: Int)

        val actual = SUT.giveMeOne<IntObject>().value

        then(actual).isNegative()
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleIntWithNegativeOrZero() {
        class IntObject(@field:NegativeOrZero val value: Int)

        val actual = SUT.giveMeOne<IntObject>().value

        then(actual).isLessThanOrEqualTo(0)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleIntWithPositive() {
        class IntObject(@field:Positive val value: Int)

        val actual = SUT.giveMeOne<IntObject>().value

        then(actual).isPositive()
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleIntWithPositiveOrZero() {
        class IntObject(@field:PositiveOrZero val value: Int)

        val actual = SUT.giveMeOne<IntObject>().value

        then(actual).isGreaterThanOrEqualTo(0)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleIntWithMin() {
        class IntObject(@field:Min(10) val value: Int)

        val actual = SUT.giveMeOne<IntObject>().value

        then(actual).isGreaterThanOrEqualTo(10)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleIntWithDecimalMin() {
        class IntObject(@field:DecimalMin("10") val value: Int)

        val actual = SUT.giveMeOne<IntObject>().value

        then(actual).isGreaterThanOrEqualTo(10)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleIntWithMax() {
        class IntObject(@field:Max(50) val value: Int)

        val actual = SUT.giveMeOne<IntObject>().value

        then(actual).isLessThanOrEqualTo(50)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleIntWithDecimalMax() {
        class IntObject(@field:DecimalMax("50") val value: Int)

        val actual = SUT.giveMeOne<IntObject>().value

        then(actual).isLessThanOrEqualTo(50)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleIntWithDigits() {
        class IntObject(@field:Digits(integer = 2, fraction = 0) val value: Int)

        val actual = SUT.giveMeOne<IntObject>().value

        then(actual).matches { it in -99..99 }
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleLongWithNegative() {
        class LongObject(@field:Negative val value: Long)

        val actual = SUT.giveMeOne<LongObject>().value

        then(actual).isNegative()
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleLongWithNegativeOrZero() {
        class LongObject(@field:NegativeOrZero val value: Long)

        val actual = SUT.giveMeOne<LongObject>().value

        then(actual).isLessThanOrEqualTo(0)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleLongWithPositive() {
        class LongObject(@field:Positive val value: Long)

        val actual = SUT.giveMeOne<LongObject>().value

        then(actual).isPositive()
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleLongWithPositiveOrZero() {
        class LongObject(@field:PositiveOrZero val value: Long)

        val actual = SUT.giveMeOne<LongObject>().value

        then(actual).isGreaterThanOrEqualTo(0)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleLongWithMin() {
        class LongObject(@field:Min(10) val value: Long)

        val actual = SUT.giveMeOne<LongObject>().value

        then(actual).isGreaterThanOrEqualTo(10)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleLongWithDecimalMin() {
        class LongObject(@field:DecimalMin("10") val value: Long)

        val actual = SUT.giveMeOne<LongObject>().value

        then(actual).isGreaterThanOrEqualTo(10)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleLongWithMax() {
        class LongObject(@field:Max(50) val value: Long)

        val actual = SUT.giveMeOne<LongObject>().value

        then(actual).isLessThanOrEqualTo(50)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleLongWithDecimalMax() {
        class LongObject(@field:DecimalMax("50") val value: Long)

        val actual = SUT.giveMeOne<LongObject>().value

        then(actual).isLessThanOrEqualTo(50)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleLongWithDigits() {
        class LongObject(@field:Digits(integer = 2, fraction = 0) val value: Long)

        val actual = SUT.giveMeOne<LongObject>().value

        then(actual).matches { it in -99L..99L }
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleBigIntegerWithNegative() {
        class BigIntegerObject(@field:Negative val value: BigInteger)

        val actual = SUT.giveMeOne<BigIntegerObject>().value

        then(actual).isNegative()
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleBigIntegerWithNegativeOrZero() {
        class BigIntegerObject(@field:NegativeOrZero val value: BigInteger)

        val actual = SUT.giveMeOne<BigIntegerObject>().value

        then(actual).isLessThanOrEqualTo(BigInteger.ZERO)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleBigIntegerWithPositive() {
        class BigIntegerObject(@field:Positive val value: BigInteger)

        val actual = SUT.giveMeOne<BigIntegerObject>().value

        then(actual).isPositive()
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleBigIntegerWithPositiveOrZero() {
        class BigIntegerObject(@field:PositiveOrZero val value: BigInteger)

        val actual = SUT.giveMeOne<BigIntegerObject>().value

        then(actual).isGreaterThanOrEqualTo(BigInteger.ZERO)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleBigIntegerWithMin() {
        class BigIntegerObject(@field:Min(10) val value: BigInteger)

        val actual = SUT.giveMeOne<BigIntegerObject>().value

        then(actual).isGreaterThanOrEqualTo(BigInteger.valueOf(10))
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleBigIntegerWithDecimalMin() {
        class BigIntegerObject(@field:DecimalMin("10") val value: BigInteger)

        val actual = SUT.giveMeOne<BigIntegerObject>().value

        then(actual).isGreaterThanOrEqualTo(BigInteger.valueOf(10))
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleBigIntegerWithMax() {
        class BigIntegerObject(@field:Max(50) val value: BigInteger)

        val actual = SUT.giveMeOne<BigIntegerObject>().value

        then(actual).isLessThanOrEqualTo(BigInteger.valueOf(50))
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleBigIntegerWithDecimalMax() {
        class BigIntegerObject(@field:DecimalMax("50") val value: BigInteger)

        val actual = SUT.giveMeOne<BigIntegerObject>().value

        then(actual).isLessThanOrEqualTo(BigInteger.valueOf(50))
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleBigIntegerWithDigits() {
        class BigIntegerObject(@field:Digits(integer = 2, fraction = 0) val value: BigInteger)

        val actual = SUT.giveMeOne<BigIntegerObject>().value

        then(actual).matches {
            it in BigInteger.valueOf(-99L)..BigInteger.valueOf(99L)
        }
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleBigDecimalWithNegative() {
        class BigDecimalObject(@field:Negative val value: BigDecimal)

        val actual = SUT.giveMeOne<BigDecimalObject>().value

        then(actual).isNegative()
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleBigDecimalWithNegativeOrZero() {
        class BigDecimalObject(@field:NegativeOrZero val value: BigDecimal)

        val actual = SUT.giveMeOne<BigDecimalObject>().value

        then(actual).isLessThanOrEqualTo(BigDecimal.ZERO)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleBigDecimalWithPositive() {
        class BigDecimalObject(@field:Positive val value: BigDecimal)

        val actual = SUT.giveMeOne<BigDecimalObject>().value

        then(actual).isPositive()
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleBigDecimalWithPositiveOrZero() {
        class BigDecimalObject(@field:PositiveOrZero val value: BigDecimal)

        val actual = SUT.giveMeOne<BigDecimalObject>().value

        then(actual).isGreaterThanOrEqualTo(BigDecimal.ZERO)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleBigDecimalWithMin() {
        class BigDecimalObject(@field:Min(10) val value: BigDecimal)

        val actual = SUT.giveMeOne<BigDecimalObject>().value

        then(actual).isGreaterThanOrEqualTo(BigDecimal.valueOf(10L))
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleBigDecimalWithDecimalMin() {
        class BigDecimalObject(@field:DecimalMin("10") val value: BigDecimal)

        val actual = SUT.giveMeOne<BigDecimalObject>().value

        then(actual).isGreaterThanOrEqualTo(BigDecimal.valueOf(10L))
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleBigDecimalWithMax() {
        class BigDecimalObject(@field:Max(50) val value: BigDecimal)

        val actual = SUT.giveMeOne<BigDecimalObject>().value

        then(actual).isLessThanOrEqualTo(BigDecimal.valueOf(50L))
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleBigDecimalWithDecimalMax() {
        class BigDecimalObject(@field:DecimalMax("50") val value: BigDecimal)

        val actual = SUT.giveMeOne<BigDecimalObject>().value

        then(actual).isLessThanOrEqualTo(BigDecimal.valueOf(50L))
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleBigDecimalWithDigits() {
        class BigDecimalObject(@field:Digits(integer = 2, fraction = 0) val value: BigDecimal)

        val actual = SUT.giveMeOne<BigDecimalObject>().value

        then(actual).matches {
            it in BigDecimal.valueOf(-99)..BigDecimal.valueOf(99)
        }
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleBigDecimalWithEqualMinMax() {
        class BigDecimalObject(
            @field:DecimalMin("10.00") @field:DecimalMax("10.00")
            val value: BigDecimal
        )

        val actual = SUT.giveMeOne<BigDecimalObject>().value
        then(actual).isEqualByComparingTo(BigDecimal("10.00"))
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleBigDecimalWithMultipleConstraints() {
        class BigDecimalObject(
            @field:DecimalMax("11.5") @field:Max(10)
            val value: BigDecimal
        )

        val actual = SUT.giveMeOne<BigDecimalObject>().value
        then(actual).isLessThanOrEqualTo(BigDecimal.valueOf(10))
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleBigDecimalWithPreciseExclusiveBounds() {
        class BigDecimalObject(
            @field:DecimalMin(value = "10.0", inclusive = false)
            @field:DecimalMax(value = "10.1", inclusive = false)
            val value: BigDecimal
        )

        val actual = SUT.giveMeOne<BigDecimalObject>().value

        then(actual)
            .isGreaterThan(BigDecimal("10.0"))
            .isLessThan(BigDecimal("10.1"))
    }

    @RepeatedTest(TEST_COUNT)
    fun setPostConditionExtension() {
        class StringObject(val string: String)

        val actual = SUT.giveMeKotlinBuilder<StringObject>()
            .setPostCondition<StringObject, String>("string") {
                it.length < 5
            }
            .sample()
            .string

        then(actual).hasSizeLessThan(5)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleMinZeroInteger() {
        class IntegerObject(@field:Min(0L) val value: Int)

        val actual = SUT.giveMeOne<IntegerObject>().value

        then(actual).isGreaterThanOrEqualTo(0)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleMinZeroDecimal() {
        class DecimalObject(@field:Min(0L) val value: Double)

        val actual = SUT.giveMeOne<DecimalObject>().value

        then(actual).isGreaterThanOrEqualTo(0.0)
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleSetArb() {
        class StringObject(val string: String)

        val actual = SUT.giveMeArb<StringObject> { setArb(StringObject::string, Arb.zipcodes()) }
            .single()
            .string

        then(actual).hasSize(5)
    }

    @Test
    fun integerCombinableArbitrary() {
        val actual = CombinableArbitrary.integers()

        then(actual).isInstanceOf(KotestIntegerCombinableArbitrary::class.java)
    }

    @RepeatedTest(TEST_COUNT)
    fun integerCombinableArbitraryPositive() {
        val actual = CombinableArbitrary.integers().positive().combined()

        then(actual).isPositive()
    }

    @RepeatedTest(TEST_COUNT)
    fun integerCombinableArbitraryNegative() {
        val actual = CombinableArbitrary.integers().negative().combined()

        then(actual).isNegative()
    }

    @RepeatedTest(TEST_COUNT)
    fun integerCombinableArbitraryEven() {
        val actual = CombinableArbitrary.integers().even().combined()

        then(actual).isEven()
    }

    @RepeatedTest(TEST_COUNT)
    fun integerCombinableArbitraryOdd() {
        val actual = CombinableArbitrary.integers().odd().combined()

        then(actual).isOdd()
    }

    @RepeatedTest(TEST_COUNT)
    fun integerCombinableArbitraryWithRange() {
        val actual = CombinableArbitrary.integers().withRange(10, 20).combined()

        then(actual).isBetween(10, 20)
    }

    @Test
    fun byteCombinableArbitrary() {
        val actual = CombinableArbitrary.bytes()

        then(actual).isInstanceOf(KotestByteCombinableArbitrary::class.java)
    }

    @RepeatedTest(TEST_COUNT)
    fun byteCombinableArbitraryPositive() {
        val actual = CombinableArbitrary.bytes().positive().combined()

        then(actual).isPositive()
    }

    @RepeatedTest(TEST_COUNT)
    fun byteCombinableArbitraryNegative() {
        val actual = CombinableArbitrary.bytes().negative().combined()

        then(actual).isNegative()
    }

    @RepeatedTest(TEST_COUNT)
    fun byteCombinableArbitraryEven() {
        val actual = CombinableArbitrary.bytes().even().combined()

        then(actual % 2).isEqualTo(0)
    }

    @RepeatedTest(TEST_COUNT)
    fun byteCombinableArbitraryOdd() {
        val actual = CombinableArbitrary.bytes().odd().combined()

        then(actual % 2 != 0).isTrue()
    }

    @RepeatedTest(TEST_COUNT)
    fun byteCombinableArbitraryAscii() {
        val actual = CombinableArbitrary.bytes().ascii().combined()

        then(actual).isBetween(0.toByte(), 127.toByte())
    }

    @RepeatedTest(TEST_COUNT)
    fun byteCombinableArbitraryWithRange() {
        val actual = CombinableArbitrary.bytes().withRange(10.toByte(), 20.toByte()).combined()

        then(actual).isBetween(10.toByte(), 20.toByte())
    }

    @Test
    fun longCombinableArbitrary() {
        val actual = CombinableArbitrary.longs()

        then(actual).isInstanceOf(KotestLongCombinableArbitrary::class.java)
    }

    @RepeatedTest(TEST_COUNT)
    fun longCombinableArbitraryPositive() {
        val actual = CombinableArbitrary.longs().positive().combined()

        then(actual).isPositive()
    }

    @RepeatedTest(TEST_COUNT)
    fun longCombinableArbitraryNegative() {
        val actual = CombinableArbitrary.longs().negative().combined()

        then(actual).isNegative()
    }

    @RepeatedTest(TEST_COUNT)
    fun longCombinableArbitraryEven() {
        val actual = CombinableArbitrary.longs().even().combined()

        then(actual % 2).isEqualTo(0)
    }

    @RepeatedTest(TEST_COUNT)
    fun longCombinableArbitraryOdd() {
        val actual = CombinableArbitrary.longs().odd().combined()

        then(actual % 2 != 0.toLong()).isTrue()
    }

    @RepeatedTest(TEST_COUNT)
    fun longCombinableArbitraryWithRange() {
        val actual = CombinableArbitrary.longs().withRange(10L, 20L).combined()

        then(actual).isBetween(10L, 20L)
    }

    @RepeatedTest(TEST_COUNT)
    fun longCombinableArbitraryNonZero() {
        val actual = CombinableArbitrary.longs().nonZero().combined()

        then(actual).isNotEqualTo(0L)
    }

    @RepeatedTest(TEST_COUNT)
    fun longCombinableArbitraryMultipleOf() {
        val actual = CombinableArbitrary.longs().multipleOf(7L).combined()

        then(actual % 7L).isEqualTo(0L)
    }

    @RepeatedTest(TEST_COUNT)
    fun longCombinableArbitraryNonZeroWithRange() {
        // withRange(-5L, 5L).nonZero() => nonZero()
        val actual = CombinableArbitrary.longs().withRange(-5L, 5L).nonZero().combined()

        then(actual).isNotEqualTo(0L)
    }

    @RepeatedTest(TEST_COUNT)
    fun longCombinableArbitraryMultipleOfWithPositiveAndRange() {
        // positive().withRange(1L, 50L).multipleOf(3L) => multipleOf(3L)
        val actual = CombinableArbitrary.longs()
            .positive()
            .withRange(1L, 50L)
            .multipleOf(3L)
            .combined()

        then(actual % 3L).isEqualTo(0L)
    }

    @RepeatedTest(TEST_COUNT)
    fun longCombinableArbitraryLastMethodWinsWithPositiveAndNegative() {
        // positive().negative() => negative()
        val actual = CombinableArbitrary.longs().positive().negative().combined()

        then(actual).isNegative()
    }

    @RepeatedTest(TEST_COUNT)
    fun longCombinableArbitraryLastMethodWinsWithEvenAndOdd() {
        // even().odd() => odd()
        val actual = CombinableArbitrary.longs().even().odd().combined()

        then(actual.toInt() % 2 != 0).isTrue()
    }

    @RepeatedTest(TEST_COUNT)
    fun longCombinableArbitraryLastMethodWinsWithNegativeAndRange() {
        // negative().withRange() => withRange()
        val actual = CombinableArbitrary.longs().negative().withRange(100L, 1000L).combined()

        then(actual).isBetween(100L, 1000L)
    }


    @Test
    fun shortCombinableArbitrary() {
        val actual = CombinableArbitrary.shorts()

        then(actual).isInstanceOf(KotestShortCombinableArbitrary::class.java)
    }

    @RepeatedTest(TEST_COUNT)
    fun shortCombinableArbitraryPositive() {
        val actual = CombinableArbitrary.shorts().positive().combined()

        then(actual).isPositive()
    }

    @RepeatedTest(TEST_COUNT)
    fun shortCombinableArbitraryNegative() {
        val actual = CombinableArbitrary.shorts().negative().combined()

        then(actual).isNegative()
    }

    @RepeatedTest(TEST_COUNT)
    fun shortCombinableArbitraryEven() {
        val actual = CombinableArbitrary.shorts().even().combined()

        then(actual % 2).isEqualTo(0)
    }

    @RepeatedTest(TEST_COUNT)
    fun shortCombinableArbitraryOdd() {
        val actual = CombinableArbitrary.shorts().odd().combined()
        then(actual % 2 != 0).isTrue()
    }

    @RepeatedTest(TEST_COUNT)
    fun shortCombinableArbitraryWithRange() {
        val actual = CombinableArbitrary.shorts().withRange(10.toShort(), 20.toShort()).combined()

        then(actual).isBetween(10.toShort(), 20.toShort())
    }

    @Test
    fun characterCombinableArbitrary() {
        val actual = CombinableArbitrary.chars()

        then(actual).isInstanceOf(KotestCharacterCombinableArbitrary::class.java)
    }

    @RepeatedTest(TEST_COUNT)
    fun characterCombinableArbitraryWithRange() {
        val actual = CombinableArbitrary.chars().withRange('A', 'Z').combined()

        then(actual).isBetween('A', 'Z')
    }

    @RepeatedTest(TEST_COUNT)
    fun characterCombinableArbitraryAlpha() {
        val actual = CombinableArbitrary.chars().alphabetic().combined()

        then(actual.isLetter()).isTrue()
    }

    @RepeatedTest(TEST_COUNT)
    fun characterCombinableArbitraryNumeric() {
        val actual = CombinableArbitrary.chars().numeric().combined()

        then(actual.isDigit()).isTrue()
    }

    @RepeatedTest(TEST_COUNT)
    fun characterCombinableArbitraryAlphaNumeric() {
        val actual = CombinableArbitrary.chars().alphaNumeric().combined()

        then(actual.isLetterOrDigit()).isTrue()
    }

    @RepeatedTest(TEST_COUNT)
    fun characterCombinableArbitraryAscii() {
        val actual = CombinableArbitrary.chars().ascii().combined()

        then(actual.code).isLessThanOrEqualTo(127)
    }

    @RepeatedTest(TEST_COUNT)
    fun characterCombinableArbitraryUppercase() {
        val actual = CombinableArbitrary.chars().uppercase().combined()

        then(actual.isUpperCase()).isTrue()
    }

    @RepeatedTest(TEST_COUNT)
    fun characterCombinableArbitraryLowercase() {
        val actual = CombinableArbitrary.chars().lowercase().combined()

        then(actual.isLowerCase()).isTrue()
    }

    @RepeatedTest(TEST_COUNT)
    fun characterCombinableArbitraryKorean() {
        val actual = CombinableArbitrary.chars().korean().combined()

        then(actual).isBetween('가', '힣')
    }

    @RepeatedTest(TEST_COUNT)
    fun characterCombinableArbitraryWhitespace() {
        val actual = CombinableArbitrary.chars().whitespace().combined()

        then(actual.isWhitespace()).isTrue()
    }

    @Test
    fun stringCombinableArbitrary() {
        val actual = CombinableArbitrary.strings()

        then(actual).isInstanceOf(KotestStringCombinableArbitrary::class.java)
    }

    @Test
    fun stringCombinableArbitraryAscii() {
        val actual = CombinableArbitrary.strings().ascii().combined()

        then(actual).satisfiesAnyOf(
            { then(it).isASCII() },
            { then(it).isNullOrEmpty() }
        )
    }

    @RepeatedTest(TEST_COUNT)
    fun byteCombinableArbitraryLastMethodWinsWithPositiveAndNegative() {
        // positive().negative() => negative()
        val actual = CombinableArbitrary.bytes().positive().negative().combined()

        then(actual).isNegative()
    }

    @RepeatedTest(TEST_COUNT)
    fun byteCombinableArbitraryLastMethodWinsWithEvenAndOdd() {
        // even().odd() => odd()
        val actual = CombinableArbitrary.bytes().even().odd().combined()

        then(actual % 2 != 0).isTrue()
    }

    @RepeatedTest(TEST_COUNT)
    fun byteCombinableArbitraryLastMethodWinsWithNegativeAndRange() {
        // negative().withRange() => withRange()
        val actual = CombinableArbitrary.bytes().negative().withRange(100.toByte(), 127.toByte()).combined()

        then(actual).isBetween(100.toByte(), 127.toByte())
    }

    @RepeatedTest(TEST_COUNT)
    fun byteCombinableArbitraryLastMethodWinsWithAsciiAndPositive() {
        // ascii().positive() => positive()
        val actual = CombinableArbitrary.bytes().ascii().positive().combined()

        then(actual).isPositive()
    }

    @RepeatedTest(TEST_COUNT)
    fun byteCombinableArbitraryLastMethodWinsWithPositiveAndAscii() {
        // positive().ascii() => ascii()
        val actual = CombinableArbitrary.bytes().positive().ascii().combined()

        then(actual).isBetween(0.toByte(), 127.toByte())
    }

    @RepeatedTest(TEST_COUNT)
    fun byteCombinableArbitraryLastMethodWinsWithAsciiAndEven() {
        // ascii().even() => even()
        val actual = CombinableArbitrary.bytes().ascii().even().combined()

        then(actual % 2).isEqualTo(0)
    }

    @RepeatedTest(TEST_COUNT)
    fun byteCombinableArbitraryLastMethodWinsWithEvenAndAscii() {
        // even().ascii() => ascii()
        val actual = CombinableArbitrary.bytes().even().ascii().combined()

        then(actual).isBetween(0.toByte(), 127.toByte())
    }

    @RepeatedTest(TEST_COUNT)
    fun byteCombinableArbitraryLastMethodWinsWithAsciiAndNegative() {
        // ascii().negative() => negative()
        val actual = CombinableArbitrary.bytes().ascii().negative().combined()

        then(actual).isNegative()
    }

    @RepeatedTest(TEST_COUNT)
    fun integerCombinableArbitraryLastMethodWinsWithPositiveAndNegative() {
        // positive().negative() => negative()
        val actual = CombinableArbitrary.integers().positive().negative().combined()

        then(actual).isNegative()
    }

    @RepeatedTest(TEST_COUNT)
    fun integerCombinableArbitraryLastMethodWinsWithEvenAndOdd() {
        // even().odd() => odd()
        val actual = CombinableArbitrary.integers().even().odd().combined()

        then(actual).isOdd()
    }

    @RepeatedTest(TEST_COUNT)
    fun integerCombinableArbitraryLastMethodWinsWithNegativeAndRange() {
        // negative().withRange() => withRange()
        val actual = CombinableArbitrary.integers().negative().withRange(100, 1000).combined()

        then(actual).isBetween(100, 1000)
    }

    @Test
    fun bigIntegerCombinableArbitrary() {
        val actual = CombinableArbitrary.bigIntegers()

        then(actual).isInstanceOf(KotestBigIntegerCombinableArbitrary::class.java)
    }

    @RepeatedTest(TEST_COUNT)
    fun bigIntegerCombinableArbitraryPositive() {
        val actual = CombinableArbitrary.bigIntegers().positive().combined()

        then(actual).isPositive()
    }

    @RepeatedTest(TEST_COUNT)
    fun bigIntegerCombinableArbitraryNegative() {
        val actual = CombinableArbitrary.bigIntegers().negative().combined()

        then(actual).isNegative()
    }

    @RepeatedTest(TEST_COUNT)
    fun bigIntegerCombinableArbitraryWithRange() {
        val actual = CombinableArbitrary.bigIntegers().withRange(
            BigInteger.valueOf(100), BigInteger.valueOf(1000)
        ).combined()

        then(actual).isBetween(BigInteger.valueOf(100), BigInteger.valueOf(1000))
    }

    @Test
    fun bigDecimalCombinableArbitrary() {
        val actual = CombinableArbitrary.bigDecimals()

        then(actual).isInstanceOf(KotestBigDecimalCombinableArbitrary::class.java)
    }

    @RepeatedTest(TEST_COUNT)
    fun bigDecimalCombinableArbitraryPositive() {
        val actual = CombinableArbitrary.bigDecimals().positive().combined()

        then(actual).isPositive()
    }

    @RepeatedTest(TEST_COUNT)
    fun bigDecimalCombinableArbitraryNegative() {
        val actual = CombinableArbitrary.bigDecimals().negative().combined()

        then(actual).isNegative()
    }

    @RepeatedTest(TEST_COUNT)
    fun bigDecimalCombinableArbitraryWithRange() {
        val actual = CombinableArbitrary.bigDecimals().withRange(
            BigDecimal("100.0"), BigDecimal("1000.0")
        ).combined()

        then(actual).isBetween(BigDecimal("100.0"), BigDecimal("1000.0"))
    }

    @RepeatedTest(TEST_COUNT)
    fun bigDecimalCombinableArbitraryWithPrecision() {
        val actual = CombinableArbitrary.bigDecimals().withPrecision(2).combined()

        then(actual.precision()).isLessThanOrEqualTo(2)
    }

    @RepeatedTest(TEST_COUNT)
    fun bigDecimalCombinableArbitraryLastMethodWins() {
        val actual = CombinableArbitrary.bigDecimals()
            .withRange(BigDecimal("10.0"), BigDecimal("20.0"))
            .negative()
            .combined()

        then(actual).isNegative()
    }

    companion object {
        private val SUT: FixtureMonkey = FixtureMonkey.builder()
            .plugin(JavaxValidationPlugin())
            .plugin(KotestPlugin())
            .plugin(KotlinPlugin())
            .build()
    }
}
