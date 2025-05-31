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

package com.navercorp.fixturemonkey.kotest

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary
import com.navercorp.fixturemonkey.api.arbitrary.JavaTimeArbitraryGeneratorSet
import com.navercorp.fixturemonkey.api.arbitrary.JavaTypeArbitraryGeneratorSet
import com.navercorp.fixturemonkey.api.arbitrary.IntegerCombinableArbitrary
import com.navercorp.fixturemonkey.api.constraint.JavaConstraintGenerator
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigDecimal
import io.kotest.property.arbitrary.bigInt
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.char
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.duration
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.instant
import io.kotest.property.arbitrary.localDate
import io.kotest.property.arbitrary.localDateTime
import io.kotest.property.arbitrary.localTime
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.offsetDateTime
import io.kotest.property.arbitrary.period
import io.kotest.property.arbitrary.short
import io.kotest.property.arbitrary.single
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.yearMonth
import io.kotest.property.arbitrary.zonedDateTime
import io.kotest.property.arbitrary.zoneId
import io.kotest.property.arbitrary.zoneOffset
import org.apiguardian.api.API
import org.apiguardian.api.API.Status
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.MonthDay
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.Period
import java.time.Year
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import kotlin.math.floor
import kotlin.math.pow
import kotlin.time.toJavaDuration

@API(since = "0.6.11", status = Status.MAINTAINED)
class KotestJavaArbitraryGeneratorSet(
    private val constraintGenerator: JavaConstraintGenerator,
) : JavaTypeArbitraryGeneratorSet {
    override fun strings(context: ArbitraryGeneratorContext): CombinableArbitrary<String> {
        val stringConstraint = constraintGenerator.generateStringConstraint(context)

        return CombinableArbitrary.from {
            if (stringConstraint != null) {
                val minSize = stringConstraint.minSize?.toInt() ?: 0
                val maxSize = stringConstraint.maxSize?.toInt() ?: 100
                Arb.string(minSize = minSize, maxSize = maxSize).single()
            } else {
                Arb.string().single()
            }
        }
    }

    override fun characters(context: ArbitraryGeneratorContext): CombinableArbitrary<Char> =
        CombinableArbitrary.from { Arb.char().single() }

    override fun shorts(context: ArbitraryGeneratorContext): CombinableArbitrary<Short> {
        val integerConstraint = constraintGenerator.generateIntegerConstraint(context)

        return CombinableArbitrary.from {
            if (integerConstraint != null) {
                val min = integerConstraint.min?.toShort() ?: Short.MIN_VALUE
                val max = integerConstraint.max?.toShort() ?: Short.MAX_VALUE

                Arb.short(min = min, max = max).single()
            } else {
                Arb.short().single()
            }
        }
    }

    override fun bytes(context: ArbitraryGeneratorContext): CombinableArbitrary<Byte> {
        val integerConstraint = constraintGenerator.generateIntegerConstraint(context)

        return CombinableArbitrary.from {
            if (integerConstraint != null) {
                val min = integerConstraint.min?.toByte() ?: Byte.MIN_VALUE
                val max = integerConstraint.max?.toByte() ?: Byte.MAX_VALUE

                Arb.byte(min = min, max = max).single()
            } else {
                Arb.byte().single()
            }
        }
    }

    override fun doubles(context: ArbitraryGeneratorContext): CombinableArbitrary<Double> {
        val decimalConstraint = constraintGenerator.generateDecimalConstraint(context)

        return CombinableArbitrary.from {
            if (decimalConstraint != null) {
                val scale = decimalConstraint.scale
                var min = decimalConstraint.min?.toDouble() ?: -Double.MAX_VALUE
                var max = decimalConstraint.max?.toDouble() ?: Double.MAX_VALUE

                if (min == max &&
                    (decimalConstraint.minInclusive ?: true) &&
                    (decimalConstraint.maxInclusive ?: true)
                ) {
                    Arb.constant(min)
                } else {
                    if (decimalConstraint.minInclusive == false) {
                        min += Double.MIN_VALUE
                    }
                    if (decimalConstraint.maxInclusive ?: true) {
                        if (max != Double.MAX_VALUE) {
                            max += Double.MIN_VALUE
                        }
                    }

                    Arb.double(min = min, max = max)
                }.map { if (scale != null) it.ofScale(scale) else it }
                    .single()
            } else {
                Arb.double().single()
            }
        }
    }

    override fun floats(context: ArbitraryGeneratorContext): CombinableArbitrary<Float> {
        val decimalConstraint = constraintGenerator.generateDecimalConstraint(context)

        return CombinableArbitrary.from {
            if (decimalConstraint != null) {
                val scale = decimalConstraint.scale
                var min = decimalConstraint.min?.toFloat() ?: -Float.MAX_VALUE
                var max = decimalConstraint.max?.toFloat() ?: Float.MAX_VALUE

                if (min == max &&
                    (decimalConstraint.minInclusive ?: true) &&
                    (decimalConstraint.maxInclusive ?: true)
                ) {
                    Arb.constant(min)
                } else {
                    if (decimalConstraint.minInclusive == false) {
                        min += Float.MIN_VALUE
                    }
                    if (decimalConstraint.maxInclusive ?: true) {
                        if (max != Float.MAX_VALUE) {
                            max += Float.MIN_VALUE
                        }
                    }

                    Arb.float(min = min, max = max)
                }.map { if (scale != null) it.ofScale(scale) else it }
                    .single()
            } else {
                Arb.float().single()
            }
        }
    }

    override fun integers(context: ArbitraryGeneratorContext): IntegerCombinableArbitrary {
        val integerConstraint = constraintGenerator.generateIntegerConstraint(context)
        val combinableArbitrary = KotestIntegerCombinableArbitrary()

        return if (integerConstraint != null) {
            val min = integerConstraint.min?.toInt() ?: Int.MIN_VALUE
            val max = integerConstraint.max?.toInt() ?: Int.MAX_VALUE

            combinableArbitrary.withRange(min, max)
        } else {
            combinableArbitrary
        }
    }

    override fun longs(context: ArbitraryGeneratorContext): CombinableArbitrary<Long> {
        val integerConstraint = constraintGenerator.generateIntegerConstraint(context)

        return CombinableArbitrary.from {
            if (integerConstraint != null) {
                val min = integerConstraint.min?.toLong() ?: Long.MIN_VALUE
                val max = integerConstraint.max?.toLong() ?: Long.MAX_VALUE

                Arb.long(min = min, max = max).single()
            } else {
                Arb.long().single()
            }
        }
    }

    override fun bigIntegers(context: ArbitraryGeneratorContext): CombinableArbitrary<BigInteger> {
        val integerConstraint = constraintGenerator.generateIntegerConstraint(context)

        return CombinableArbitrary.from {
            if (integerConstraint != null) {
                val min = integerConstraint.min?.toInt() ?: Int.MIN_VALUE
                val max = integerConstraint.max?.toInt() ?: Int.MAX_VALUE

                Arb.bigInt(min..max).single()
            } else {
                Arb.bigInt(maxNumBits = 21).single()
            }
        }
    }

    override fun bigDecimals(context: ArbitraryGeneratorContext): CombinableArbitrary<BigDecimal> {
        val decimalConstraint = constraintGenerator.generateDecimalConstraint(context)

        return CombinableArbitrary.from {
            if (decimalConstraint != null) {
                val scale = decimalConstraint.scale
                var min = decimalConstraint.min ?: BigDecimal.valueOf(-Double.MAX_VALUE)
                var max = decimalConstraint.max ?: BigDecimal.valueOf(Double.MAX_VALUE)

                if (min.compareTo(max) == 0 &&
                    (decimalConstraint.minInclusive ?: true) &&
                    (decimalConstraint.maxInclusive ?: true)
                ) {
                    Arb.constant(min)
                } else {
                    if (decimalConstraint.minInclusive == false) {
                        min = min.add(BigDecimal.valueOf(Double.MIN_VALUE))
                    }
                    if (decimalConstraint.maxInclusive ?: true) {
                        if (max != BigDecimal.valueOf(Double.MAX_VALUE)) {
                            max = max.add(BigDecimal.valueOf(Double.MIN_VALUE))
                        }
                    }

                    Arb.bigDecimal(min = min, max = max)
                }.map { if (scale != null) it.setScale(scale, RoundingMode.DOWN) else it }
                    .single()
            } else {
                Arb.bigDecimal().single()
            }
        }
    }
}

class KotestJavaTimeArbitraryGeneratorSet(
    private val constraintGenerator: JavaConstraintGenerator,
) : JavaTimeArbitraryGeneratorSet {
    override fun calendars(context: ArbitraryGeneratorContext): CombinableArbitrary<Calendar> {
        val dateTimeConstraint = constraintGenerator.generateDateTimeConstraint(context)

        return CombinableArbitrary.from {
            if (dateTimeConstraint != null) {
                val minValue = dateTimeConstraint.min ?: DEFAULT_LOCAL_DATE_TIME_MIN
                val maxValue = dateTimeConstraint.max ?: DEFAULT_LOCAL_DATE_TIME_MAX
                Arb.zonedDateTime(minValue = minValue, maxValue = maxValue)
            } else {
                Arb.zonedDateTime()
            }
                .map { zonedDateTime -> GregorianCalendar.from(zonedDateTime) }.single()
        }
    }

    override fun dates(context: ArbitraryGeneratorContext): CombinableArbitrary<Date> {
        val dateTimeConstraint = constraintGenerator.generateDateTimeConstraint(context)

        return CombinableArbitrary.from {
            if (dateTimeConstraint != null) {
                val minValue = dateTimeConstraint.min?.toInstant(DEFAULT_ZONE_OFFSET) ?: Instant.MIN
                val maxValue = dateTimeConstraint.max?.toInstant(DEFAULT_ZONE_OFFSET) ?: Instant.MAX
                Arb.instant(minValue = minValue, maxValue = maxValue)
            } else {
                Arb.instant(
                    minValue = Instant.now().minus(Duration.ofDays(365)),
                    maxValue = Instant.now().plus(Duration.ofDays(365)),
                )
            }
                .map { instant -> Date.from(instant) }.single()
        }
    }

    override fun instants(context: ArbitraryGeneratorContext): CombinableArbitrary<Instant> {
        val dateTimeConstraint = constraintGenerator.generateDateTimeConstraint(context)

        return CombinableArbitrary.from {
            if (dateTimeConstraint != null) {
                val minValue = dateTimeConstraint.min?.toInstant(DEFAULT_ZONE_OFFSET) ?: Instant.MIN
                val maxValue = dateTimeConstraint.max?.toInstant(DEFAULT_ZONE_OFFSET) ?: Instant.MAX
                Arb.instant(minValue = minValue, maxValue = maxValue)
            } else {
                Arb.instant()
            }.single()
        }
    }

    override fun localDates(context: ArbitraryGeneratorContext): CombinableArbitrary<LocalDate> {
        val dateTimeConstraint = constraintGenerator.generateDateTimeConstraint(context)

        return CombinableArbitrary.from {
            if (dateTimeConstraint != null) {
                val minValue = dateTimeConstraint.min?.toLocalDate() ?: DEFAULT_LOCAL_DATE_MIN
                val maxValue = dateTimeConstraint.max?.toLocalDate() ?: DEFAULT_LOCAL_DATE_MAX
                Arb.localDate(minDate = minValue, maxDate = maxValue)
            } else {
                Arb.localDate()
            }.single()
        }
    }

    override fun localDateTimes(context: ArbitraryGeneratorContext): CombinableArbitrary<LocalDateTime> {
        val dateTimeConstraint = constraintGenerator.generateDateTimeConstraint(context)

        return CombinableArbitrary.from {
            if (dateTimeConstraint != null) {
                val minValue = dateTimeConstraint.min ?: DEFAULT_LOCAL_DATE_TIME_MIN
                val maxValue = dateTimeConstraint.max ?: DEFAULT_LOCAL_DATE_TIME_MAX
                Arb.localDateTime(minLocalDateTime = minValue, maxLocalDateTime = maxValue)
            } else {
                Arb.localDateTime()
            }.single()
        }
    }

    override fun localTimes(context: ArbitraryGeneratorContext): CombinableArbitrary<LocalTime> {
        val dateTimeConstraint = constraintGenerator.generateDateTimeConstraint(context)

        return CombinableArbitrary.from {
            if (dateTimeConstraint != null) {
                val minValue = dateTimeConstraint.min?.toLocalTime() ?: DEFAULT_LOCAL_TIME_MIN
                val maxValue = dateTimeConstraint.max?.toLocalTime() ?: DEFAULT_LOCAL_TIME_MAX
                Arb.localTime().filter { it in minValue..maxValue }
            } else {
                Arb.localTime()
            }.single()
        }
    }

    override fun zonedDateTimes(context: ArbitraryGeneratorContext): CombinableArbitrary<ZonedDateTime> {
        val dateTimeConstraint = constraintGenerator.generateDateTimeConstraint(context)

        return CombinableArbitrary.from {
            if (dateTimeConstraint != null) {
                val minValue = dateTimeConstraint.min ?: DEFAULT_LOCAL_DATE_TIME_MIN
                val maxValue = dateTimeConstraint.max ?: DEFAULT_LOCAL_DATE_TIME_MAX
                Arb.zonedDateTime(minValue = minValue, maxValue = maxValue)
            } else {
                Arb.zonedDateTime()
            }.single()
        }
    }

    override fun monthDays(context: ArbitraryGeneratorContext): CombinableArbitrary<MonthDay> {
        val dateTimeConstraint = constraintGenerator.generateDateTimeConstraint(context)

        return CombinableArbitrary.from {
            if (dateTimeConstraint != null) {
                val minValue = dateTimeConstraint.min ?: DEFAULT_LOCAL_DATE_TIME_MIN
                val maxValue = dateTimeConstraint.max ?: DEFAULT_LOCAL_DATE_TIME_MAX
                Arb.zonedDateTime(minValue = minValue, maxValue = maxValue)
            } else {
                Arb.zonedDateTime()
            }.map { zonedDateTime -> MonthDay.from(zonedDateTime) }.single()
        }
    }

    override fun offsetDateTimes(context: ArbitraryGeneratorContext): CombinableArbitrary<OffsetDateTime> {
        val dateTimeConstraint = constraintGenerator.generateDateTimeConstraint(context)

        return CombinableArbitrary.from {
            if (dateTimeConstraint != null) {
                val minValue = dateTimeConstraint.min ?: DEFAULT_LOCAL_DATE_TIME_MIN
                val maxValue = dateTimeConstraint.max ?: DEFAULT_LOCAL_DATE_TIME_MAX
                Arb.offsetDateTime(minValue = minValue, maxValue = maxValue)
            } else {
                Arb.offsetDateTime()
            }.single()
        }
    }

    override fun offsetTimes(context: ArbitraryGeneratorContext): CombinableArbitrary<OffsetTime> {
        val dateTimeConstraint = constraintGenerator.generateDateTimeConstraint(context)

        return CombinableArbitrary.from {
            if (dateTimeConstraint != null) {
                val minValue = dateTimeConstraint.min ?: DEFAULT_LOCAL_DATE_TIME_MIN
                val maxValue = dateTimeConstraint.max ?: DEFAULT_LOCAL_DATE_TIME_MAX
                Arb.offsetDateTime(minValue = minValue, maxValue = maxValue)
            } else {
                Arb.offsetDateTime()
            }.map { offsetDateTime -> offsetDateTime.toOffsetTime() }.single()
        }
    }

    override fun periods(context: ArbitraryGeneratorContext): CombinableArbitrary<Period> =
        CombinableArbitrary.from { Arb.period().single() }

    override fun durations(context: ArbitraryGeneratorContext): CombinableArbitrary<Duration> =
        CombinableArbitrary.from { Arb.duration().map { it.toJavaDuration() }.single() }

    override fun years(context: ArbitraryGeneratorContext): CombinableArbitrary<Year> {
        val dateTimeConstraint = constraintGenerator.generateDateTimeConstraint(context)

        return CombinableArbitrary.from {
            if (dateTimeConstraint != null) {
                val minValue = YearMonth.from(dateTimeConstraint.min?.plusMonths(1)?.plusMonths(1))
                    ?: YearMonth.of(1970, 1)
                val maxValue = YearMonth.from(dateTimeConstraint.min?.minusMonths(1)?.plusMonths(1))
                    ?: YearMonth.of(2030, 12)
                Arb.yearMonth(minYearMonth = minValue, maxYearMonth = maxValue)
            } else {
                Arb.yearMonth()
            }.map { yearMonth -> Year.of(yearMonth.year) }.single()
        }
    }

    override fun yearMonths(context: ArbitraryGeneratorContext): CombinableArbitrary<YearMonth> {
        val dateTimeConstraint = constraintGenerator.generateDateTimeConstraint(context)

        return CombinableArbitrary.from {
            if (dateTimeConstraint != null) {
                val minValue = YearMonth.from(dateTimeConstraint.min?.plusMonths(1)?.plusMonths(1))
                    ?: YearMonth.of(1970, 1)
                val maxValue = YearMonth.from(dateTimeConstraint.min?.minusMonths(1)?.plusMonths(1))
                    ?: YearMonth.of(2030, 12)
                Arb.yearMonth(minYearMonth = minValue, maxYearMonth = maxValue)
            } else {
                Arb.yearMonth()
            }.single()
        }
    }

    override fun zoneOffsets(context: ArbitraryGeneratorContext): CombinableArbitrary<ZoneOffset> =
        CombinableArbitrary.from { Arb.zoneOffset().single() }

    override fun zoneIds(context: ArbitraryGeneratorContext?): CombinableArbitrary<ZoneId> =
        CombinableArbitrary.from { Arb.zoneId().single() }

    companion object {
        val DEFAULT_ZONE_OFFSET: ZoneOffset = OffsetTime.now().offset
        val DEFAULT_LOCAL_DATE_TIME_MIN: LocalDateTime = LocalDateTime.of(1970, 1, 1, 0, 0)
        val DEFAULT_LOCAL_DATE_MIN: LocalDate = LocalDate.of(1970, 1, 1)
        val DEFAULT_LOCAL_TIME_MIN: LocalTime = LocalTime.of(0, 0)
        val DEFAULT_LOCAL_DATE_TIME_MAX: LocalDateTime = LocalDateTime.of(2030, 12, 31, 23, 59)
        val DEFAULT_LOCAL_DATE_MAX: LocalDate = LocalDate.of(2030, 12, 31)
        val DEFAULT_LOCAL_TIME_MAX: LocalTime = LocalTime.of(23, 59)
    }
}

private fun Double.ofScale(scale: Int) = 10.0.pow(scale).let {
    floor(this * it) / it
}

private fun Float.ofScale(scale: Int) = 10.0f.pow(scale).let {
    floor(this * it) / it
}
