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
import java.time.Period;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.time.api.DateTimes;
import net.jqwik.time.api.Dates;
import net.jqwik.time.api.Times;
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

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public interface IntrospectorTimeArbitraryGenerator {

	default CalendarArbitrary calendars() {
		Instant now = Instant.now();
		Calendar min = Calendar.getInstance();
		min.setTimeInMillis(now.minus(365, ChronoUnit.DAYS).toEpochMilli());
		Calendar max = Calendar.getInstance();
		max.setTimeInMillis(now.plus(365, ChronoUnit.DAYS).toEpochMilli());
		return Dates.datesAsCalendar()
			.between(min, max);
	}

	default DateArbitrary dates() {
		Instant now = Instant.now();
		Date min = new Date(now.minus(365, ChronoUnit.DAYS).toEpochMilli());
		Date max = new Date(now.plus(365, ChronoUnit.DAYS).toEpochMilli());
		return Dates.datesAsDate()
			.between(min, max);
	}

	default InstantArbitrary instants() {
		Instant now = Instant.now();
		return DateTimes.instants()
			.between(
				now.minus(365, ChronoUnit.DAYS),
				now.plus(365, ChronoUnit.DAYS)
			);
	}

	default LocalDateArbitrary localDates() {
		LocalDate now = LocalDate.now();
		return Dates.dates()
			.between(
				now.minusDays(365),
				now.plusDays(365)
			);
	}

	default LocalDateTimeArbitrary localDateTimes() {
		LocalDateTime now = LocalDateTime.now();
		return DateTimes.dateTimes()
			.between(
				now.minusDays(365),
				now.plusDays(365)
			);
	}

	default LocalTimeArbitrary localTimes() {
		return Times.times();
	}

	default ZonedDateTimeArbitrary zonedDateTimes() {
		LocalDateTime now = LocalDateTime.now();
		return DateTimes.zonedDateTimes()
			.between(
				now.minusDays(365),
				now.plusDays(365)
			);
	}

	default MonthDayArbitrary monthDays() {
		return Dates.monthDays();
	}

	default OffsetDateTimeArbitrary offsetDateTimes() {
		LocalDateTime now = LocalDateTime.now();
		return DateTimes.offsetDateTimes()
			.between(
				now.minusDays(365),
				now.plusDays(365)
			);
	}

	default OffsetTimeArbitrary offsetTimes() {
		return Times.offsetTimes();
	}

	default PeriodArbitrary periods() {
		return Dates.periods()
			.between(
				Period.ofDays(-365),
				Period.ofDays(365)
			);
	}

	default DurationArbitrary durations() {
		return Times.durations()
			.between(
				Duration.ofDays(-365),
				Duration.ofDays(365)
			);
	}

	default YearArbitrary years() {
		return Dates.years()
			.between(
				Year.now().minusYears(10),
				Year.now().plusYears(10)
			);
	}

	default YearMonthArbitrary yearMonths() {
		return Dates.yearMonths()
			.yearBetween(
				Year.now().minusYears(10),
				Year.now().plusYears(10)
			);
	}

	default ZoneOffsetArbitrary zoneOffsets() {
		return Times.zoneOffsets();
	}
}
