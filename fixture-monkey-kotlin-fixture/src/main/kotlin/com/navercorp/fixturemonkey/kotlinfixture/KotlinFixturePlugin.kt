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

package com.navercorp.fixturemonkey.kotlinfixture

import com.appmattus.kotlinfixture.Fixture
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary
import com.navercorp.fixturemonkey.api.arbitrary.JavaTimeArbitraryGeneratorSet
import com.navercorp.fixturemonkey.api.arbitrary.JavaTypeArbitraryGeneratorSet
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder
import com.navercorp.fixturemonkey.api.plugin.Plugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import org.apiguardian.api.API
import org.apiguardian.api.API.Status.EXPERIMENTAL
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

@API(since = "0.6.3", status = EXPERIMENTAL)
class KotlinFixturePlugin(
    val fixture: Fixture = Fixture(),
) : Plugin {
    override fun accept(optionsBuilder: FixtureMonkeyOptionsBuilder) {
        optionsBuilder
            .plugin(KotlinPlugin())
            .javaArbitraryGeneratorSet(object : JavaTypeArbitraryGeneratorSet {
                override fun strings(context: ArbitraryGeneratorContext): CombinableArbitrary<String> =
                    CombinableArbitrary.from { fixture() }

                override fun characters(context: ArbitraryGeneratorContext): CombinableArbitrary<Char> =
                    CombinableArbitrary.from { fixture() }

                override fun shorts(context: ArbitraryGeneratorContext): CombinableArbitrary<Short> =
                    CombinableArbitrary.from { fixture() }

                override fun bytes(context: ArbitraryGeneratorContext): CombinableArbitrary<Byte> =
                    CombinableArbitrary.from { fixture() }

                override fun doubles(context: ArbitraryGeneratorContext): CombinableArbitrary<Double> =
                    CombinableArbitrary.from { fixture() }

                override fun floats(context: ArbitraryGeneratorContext): CombinableArbitrary<Float> =
                    CombinableArbitrary.from { fixture() }

                override fun integers(context: ArbitraryGeneratorContext): CombinableArbitrary<Int> =
                    CombinableArbitrary.from { fixture() }

                override fun longs(context: ArbitraryGeneratorContext): CombinableArbitrary<Long> =
                    CombinableArbitrary.from { fixture() }

                override fun bigIntegers(context: ArbitraryGeneratorContext): CombinableArbitrary<BigInteger> =
                    CombinableArbitrary.from { fixture() }

                override fun bigDecimals(context: ArbitraryGeneratorContext): CombinableArbitrary<BigDecimal> =
                    CombinableArbitrary.from { fixture() }
            })
            .javaTimeArbitraryGeneratorSet(object : JavaTimeArbitraryGeneratorSet {
                override fun calendars(context: ArbitraryGeneratorContext): CombinableArbitrary<Calendar> =
                    CombinableArbitrary.from { fixture() }

                override fun dates(context: ArbitraryGeneratorContext): CombinableArbitrary<Date> =
                    CombinableArbitrary.from { fixture() }

                override fun instants(context: ArbitraryGeneratorContext): CombinableArbitrary<Instant> =
                    CombinableArbitrary.from { fixture() }

                override fun localDates(context: ArbitraryGeneratorContext): CombinableArbitrary<LocalDate> =
                    CombinableArbitrary.from { fixture() }

                override fun localDateTimes(context: ArbitraryGeneratorContext): CombinableArbitrary<LocalDateTime> =
                    CombinableArbitrary.from { fixture() }

                override fun localTimes(context: ArbitraryGeneratorContext): CombinableArbitrary<LocalTime> =
                    CombinableArbitrary.from { fixture() }

                override fun zonedDateTimes(context: ArbitraryGeneratorContext): CombinableArbitrary<ZonedDateTime> =
                    CombinableArbitrary.from { fixture() }

                override fun monthDays(context: ArbitraryGeneratorContext): CombinableArbitrary<MonthDay> =
                    CombinableArbitrary.from { fixture() }

                override fun offsetDateTimes(context: ArbitraryGeneratorContext): CombinableArbitrary<OffsetDateTime> =
                    CombinableArbitrary.from { fixture() }

                override fun offsetTimes(context: ArbitraryGeneratorContext): CombinableArbitrary<OffsetTime> =
                    CombinableArbitrary.from { fixture() }

                override fun periods(context: ArbitraryGeneratorContext): CombinableArbitrary<Period> =
                    CombinableArbitrary.from { fixture() }

                override fun durations(context: ArbitraryGeneratorContext): CombinableArbitrary<Duration> =
                    CombinableArbitrary.from { fixture() }

                override fun years(context: ArbitraryGeneratorContext): CombinableArbitrary<Year> =
                    CombinableArbitrary.from { fixture() }

                override fun yearMonths(context: ArbitraryGeneratorContext): CombinableArbitrary<YearMonth> =
                    CombinableArbitrary.from { fixture() }

                override fun zoneOffsets(context: ArbitraryGeneratorContext): CombinableArbitrary<ZoneOffset> =
                    CombinableArbitrary.from { fixture() }
            })
    }
}
