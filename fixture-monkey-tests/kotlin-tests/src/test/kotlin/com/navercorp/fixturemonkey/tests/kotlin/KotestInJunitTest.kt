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
import com.navercorp.fixturemonkey.javax.validation.plugin.JavaxValidationPlugin
import com.navercorp.fixturemonkey.kotest.KotestPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.setPostCondition
import com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.RepeatedTest
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

        then(actual).matches { it in 10..99 || it in -99..-10 }
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

        then(actual).matches { it in 10..99 || it in -99..-10 }
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

        then(actual).matches { it in 10.0..99.0 || it in -100.0..-10.0 }
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

        then(actual).matches { it in 10.0..99.0 || it in -100.0..-10.0 }
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

        then(actual).matches { it in 10..99 || it in -99..-10 }
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

        then(actual).matches { it in 10L..99L || it in -99L..-10L }
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
            it in BigInteger.valueOf(10L)..BigInteger.valueOf(99L) ||
                it in BigInteger.valueOf(-99L)..BigInteger.valueOf(-10L)
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
            it in BigDecimal.valueOf(10L)..BigDecimal.valueOf(99) ||
                it in BigDecimal.valueOf(-100)..BigDecimal.valueOf(-10)
        }
    }

    @RepeatedTest(TEST_COUNT)
    fun setPostConditionExtension() {
        class StringObject(val string: String)

        val actual = SUT.giveMeBuilder<StringObject>()
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

    companion object {
        private val SUT: FixtureMonkey = FixtureMonkey.builder()
            .plugin(JavaxValidationPlugin())
            .plugin(KotestPlugin())
            .plugin(KotlinPlugin())
            .build()
    }
}
