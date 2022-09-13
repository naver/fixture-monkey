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

import static org.assertj.core.api.BDDAssertions.then;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

import net.jqwik.api.Property;
import net.jqwik.time.api.arbitraries.CalendarArbitrary;
import net.jqwik.time.api.arbitraries.DateArbitrary;
import net.jqwik.time.api.arbitraries.DurationArbitrary;
import net.jqwik.time.api.arbitraries.InstantArbitrary;
import net.jqwik.time.api.arbitraries.LocalDateArbitrary;
import net.jqwik.time.api.arbitraries.LocalDateTimeArbitrary;
import net.jqwik.time.api.arbitraries.OffsetDateTimeArbitrary;
import net.jqwik.time.api.arbitraries.PeriodArbitrary;
import net.jqwik.time.api.arbitraries.YearArbitrary;
import net.jqwik.time.api.arbitraries.YearMonthArbitrary;
import net.jqwik.time.api.arbitraries.ZonedDateTimeArbitrary;

class JavaTimeTypeArbitraryGeneratorTests {
	private final JavaTimeTypeArbitraryGenerator sut = new JavaTimeTypeArbitraryGenerator() {
	};

	@Property
	void calendars() {
		Instant now = Instant.now();
		Calendar min = Calendar.getInstance();
		min.setTimeInMillis(now.minus(366, ChronoUnit.DAYS).toEpochMilli());
		Calendar max = Calendar.getInstance();
		max.setTimeInMillis(now.plus(366, ChronoUnit.DAYS).toEpochMilli());

		CalendarArbitrary calendarArbitrary = this.sut.calendars();
		Calendar actual = calendarArbitrary.sample();
		then(actual.getTimeInMillis()).isGreaterThanOrEqualTo(min.getTimeInMillis());
		then(actual.getTimeInMillis()).isLessThanOrEqualTo(max.getTimeInMillis());
	}

	@Property
	void dates() {
		Instant now = Instant.now();
		Date min = new Date(now.minus(366, ChronoUnit.DAYS).toEpochMilli());
		Date max = new Date(now.plus(365, ChronoUnit.DAYS).toEpochMilli());

		DateArbitrary dateArbitrary = this.sut.dates();
		Date actual = dateArbitrary.sample();
		then(actual.getTime()).isGreaterThanOrEqualTo(min.getTime());
		then(actual.getTime()).isLessThanOrEqualTo(max.getTime());
	}

	@Property
	void instants() {
		Instant now = Instant.now();
		InstantArbitrary instantArbitrary = this.sut.instants();
		Instant actual = instantArbitrary.sample();
		then(actual).isAfterOrEqualTo(now.minus(365, ChronoUnit.DAYS));
		then(actual).isBeforeOrEqualTo(now.plus(366, ChronoUnit.DAYS));
	}

	@Property
	void instantsOverrideRange() {
		Instant now = Instant.now();
		InstantArbitrary instantArbitrary = this.sut.instants()
			.atTheEarliest(now.minus(500, ChronoUnit.DAYS))
			.atTheLatest(now.minus(400, ChronoUnit.DAYS));
		Instant actual = instantArbitrary.sample();

		then(actual).isAfterOrEqualTo(now.minus(500, ChronoUnit.DAYS));
		then(actual).isBeforeOrEqualTo(now.minus(399, ChronoUnit.DAYS));
	}

	@Property
	void localDates() {
		LocalDate now = LocalDate.now();
		LocalDateArbitrary localDateArbitrary = this.sut.localDates();
		LocalDate actual = localDateArbitrary.sample();
		then(actual).isAfterOrEqualTo(now.minusDays(365));
		then(actual).isBeforeOrEqualTo(now.plusDays(365));
	}

	@Property
	void localDateTimes() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTimeArbitrary localDateTimeArbitrary = this.sut.localDateTimes();
		LocalDateTime actual = localDateTimeArbitrary.sample();
		then(actual).isAfterOrEqualTo(now.minusDays(365));
		then(actual).isBeforeOrEqualTo(now.plusDays(366));
	}

	@Property
	void zonedDateTimes() {
		LocalDateTime now = LocalDateTime.now();
		ZonedDateTimeArbitrary zonedDateTimeArbitrary = this.sut.zonedDateTimes();
		ZonedDateTime actual = zonedDateTimeArbitrary.sample();
		then(actual.toLocalDateTime()).isAfterOrEqualTo(now.minusDays(365));
		then(actual.toLocalDateTime()).isBeforeOrEqualTo(now.plusDays(366));
	}

	@Property
	void offsetDateTimes() {
		LocalDateTime now = LocalDateTime.now();
		OffsetDateTimeArbitrary offsetDateTimeArbitrary = this.sut.offsetDateTimes();
		OffsetDateTime actual = offsetDateTimeArbitrary.sample();
		then(actual.toLocalDateTime()).isAfterOrEqualTo(now.minusDays(365));
		then(actual.toLocalDateTime()).isBeforeOrEqualTo(now.plusDays(366));
	}

	@Property
	void periods() {
		PeriodArbitrary periodArbitrary = this.sut.periods();
		Period actual = periodArbitrary.sample();
		then(actual.getDays()).isGreaterThanOrEqualTo(Period.ofDays(-365).getDays());
		then(actual.getDays()).isLessThanOrEqualTo(Period.ofDays(366).getDays());
	}

	@Property
	void durations() {
		DurationArbitrary durationArbitrary = this.sut.durations();
		Duration actual = durationArbitrary.sample();
		then(actual).isGreaterThanOrEqualTo(Duration.ofDays(-365));
		then(actual).isLessThanOrEqualTo(Duration.ofDays(366));
	}

	@Property
	void years() {
		YearArbitrary yearArbitrary = this.sut.years();
		Year actual = yearArbitrary.sample();
		then(actual).isGreaterThanOrEqualTo(Year.now().minusYears(10));
		then(actual).isLessThanOrEqualTo(Year.now().plusYears(10));
	}

	@Property
	void yearMonths() {
		YearMonthArbitrary yearMonthArbitrary = this.sut.yearMonths();
		YearMonth actual = yearMonthArbitrary.sample();
		then(actual).isGreaterThanOrEqualTo(YearMonth.now().minusYears(11));
		then(actual).isLessThanOrEqualTo(YearMonth.now().plusYears(11));
	}
}
