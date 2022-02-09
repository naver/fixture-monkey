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

package com.navercorp.fixturemonkey.api.introspector;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;
import net.jqwik.time.api.arbitraries.CalendarArbitrary;
import net.jqwik.time.api.arbitraries.DateArbitrary;
import net.jqwik.time.api.arbitraries.DurationArbitrary;
import net.jqwik.time.api.arbitraries.InstantArbitrary;
import net.jqwik.time.api.arbitraries.LocalDateArbitrary;
import net.jqwik.time.api.arbitraries.LocalDateTimeArbitrary;
import net.jqwik.time.api.arbitraries.LocalTimeArbitrary;
import net.jqwik.time.api.arbitraries.MonthDayArbitrary;
import net.jqwik.time.api.arbitraries.OffsetDateTimeArbitrary;
import net.jqwik.time.api.arbitraries.OffsetTimeArbitrary;
import net.jqwik.time.api.arbitraries.PeriodArbitrary;
import net.jqwik.time.api.arbitraries.YearArbitrary;
import net.jqwik.time.api.arbitraries.YearMonthArbitrary;
import net.jqwik.time.api.arbitraries.ZoneOffsetArbitrary;
import net.jqwik.time.api.arbitraries.ZonedDateTimeArbitrary;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public interface TimeArbitraryIntrospector {
	default Arbitrary<Calendar> calendars(CalendarArbitrary calendarArbitrary, ArbitraryGeneratorContext context) {
		return calendarArbitrary;
	}

	default Arbitrary<Date> dates(DateArbitrary dateArbitrary, ArbitraryGeneratorContext context) {
		return dateArbitrary;
	}

	default Arbitrary<Instant> instants(InstantArbitrary instantArbitrary, ArbitraryGeneratorContext context) {
		return instantArbitrary;
	}

	default Arbitrary<LocalDate> localDates(LocalDateArbitrary localDateArbitrary, ArbitraryGeneratorContext context) {
		return localDateArbitrary;
	}

	default Arbitrary<LocalDateTime> localDateTimes(
		LocalDateTimeArbitrary localDateTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		return localDateTimeArbitrary;
	}

	default Arbitrary<LocalTime> localTimes(
		LocalTimeArbitrary localTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		return localTimeArbitrary;
	}

	default Arbitrary<ZonedDateTime> zonedDateTimes(
		ZonedDateTimeArbitrary zonedDateTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		return zonedDateTimeArbitrary;
	}

	default Arbitrary<MonthDay> monthDays(
		MonthDayArbitrary monthDayArbitrary,
		ArbitraryGeneratorContext context
	) {
		return monthDayArbitrary;
	}

	default Arbitrary<OffsetDateTime> offsetDateTimes(
		OffsetDateTimeArbitrary offsetDateTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		return offsetDateTimeArbitrary;
	}

	default Arbitrary<OffsetTime> offsetTimes(
		OffsetTimeArbitrary offsetTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		return offsetTimeArbitrary;
	}

	default Arbitrary<Period> periods(PeriodArbitrary periodArbitrary, ArbitraryGeneratorContext context) {
		return periodArbitrary;
	}

	default Arbitrary<Duration> durations(DurationArbitrary durationArbitrary, ArbitraryGeneratorContext context) {
		return durationArbitrary;
	}

	default Arbitrary<Year> years(YearArbitrary yearArbitrary, ArbitraryGeneratorContext context) {
		return yearArbitrary;
	}

	default Arbitrary<YearMonth> yearMonths(
		YearMonthArbitrary yearMonthArbitrary,
		ArbitraryGeneratorContext context
	) {
		return yearMonthArbitrary;
	}

	default Arbitrary<ZoneOffset> zoneOffsets(
		ZoneOffsetArbitrary zoneOffsetArbitrary,
		ArbitraryGeneratorContext context
	) {
		return zoneOffsetArbitrary;
	}
}
