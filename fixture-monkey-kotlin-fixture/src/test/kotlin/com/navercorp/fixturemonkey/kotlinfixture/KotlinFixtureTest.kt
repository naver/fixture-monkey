package com.navercorp.fixturemonkey.kotlinfixture

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.RepeatedTest
import java.math.BigDecimal
import java.math.BigInteger
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
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Date

class KotlinFixtureTest {
    @RepeatedTest(TEST_COUNT)
    fun sampleJavaType() {
        val actual: JavaTypeObject = SUT.giveMeOne()

        then(actual).isNotNull
        then(actual.string).isNotNull
        then(actual.char).isNotNull
        then(actual.short).isNotNull
        then(actual.byte).isNotNull
        then(actual.double).isNotNull
        then(actual.float).isNotNull
        then(actual.int).isNotNull
        then(actual.long).isNotNull
        then(actual.bigInteger).isNotNull
        then(actual.bigDecimal).isNotNull
    }

    @RepeatedTest(TEST_COUNT)
    fun sampleJavaTime() {
        val actual: JavaTimeObject = SUT.giveMeOne()

        then(actual).isNotNull
        then(actual.calendar).isNotNull
        then(actual.date).isNotNull
        then(actual.instant).isNotNull
        then(actual.localDate).isNotNull
        then(actual.localDateTime).isNotNull
        then(actual.localTime).isNotNull
        then(actual.zonedDateTime).isNotNull
        then(actual.monthDay).isNotNull
        then(actual.offsetDateTime).isNotNull
        then(actual.offsetTime).isNotNull
        then(actual.period).isNotNull
        then(actual.duration).isNotNull
        then(actual.year).isNotNull
        then(actual.yearMonth).isNotNull
        then(actual.zoneOffset).isNotNull
    }

    data class JavaTypeObject(
        val string: String,
        val char: Char,
        val short: Short,
        val byte: Byte,
        val double: Double,
        val float: Float,
        val int: Int,
        val long: Long,
        val bigInteger: BigInteger,
        val bigDecimal: BigDecimal,
    )

    data class JavaTimeObject(
        val calendar: Calendar,
        val date: Date,
        val instant: Instant,
        val localDate: LocalDate,
        val localDateTime: LocalDateTime,
        val localTime: LocalTime,
        val zonedDateTime: ZonedDateTime,
        val monthDay: MonthDay,
        val offsetDateTime: OffsetDateTime,
        val offsetTime: OffsetTime,
        val period: Period,
        val duration: Duration,
        val year: Year,
        val yearMonth: YearMonth,
        val zoneOffset: ZoneOffset,
    )

    companion object {
        const val TEST_COUNT = 1000

        val SUT: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinFixturePlugin())
            .build()
    }
}
