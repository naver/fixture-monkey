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

package com.navercorp.fixturemonkey.javax.validation.introspector;

import static org.assertj.core.api.BDDAssertions.then;

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
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Property;
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

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ObjectProperty;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;
import com.navercorp.fixturemonkey.api.type.TypeReference;

class JavaxValidationTimeJavaArbitraryResolverTest {
	private final JavaxValidationJavaTimeArbitraryResolver sut = new JavaxValidationJavaTimeArbitraryResolver();

	@Property
	void calendar() {
		// given
		CalendarArbitrary calendarArbitrary = Dates.datesAsCalendar();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"calendar"
		);

		// when
		Calendar actual = this.sut.calendars(calendarArbitrary, context).sample();

		// then
		then(actual).isNotNull();
	}

	@Property
	void calendarPast() {
		// given
		CalendarArbitrary calendarArbitrary = Dates.datesAsCalendar();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"calendarPast"
		);

		// when
		Calendar actual = this.sut.calendars(calendarArbitrary, context).sample();

		// then
		Calendar now = Calendar.getInstance();
		then(actual.toInstant().toEpochMilli()).isLessThan(now.toInstant().toEpochMilli());
	}

	@Property
	void calendarPastOrPresent() {
		// given
		CalendarArbitrary calendarArbitrary = Dates.datesAsCalendar();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"calendarPastOrPresent"
		);

		// when
		Calendar actual = this.sut.calendars(calendarArbitrary, context).sample();

		// then
		Calendar now = Calendar.getInstance();
		then(actual.getTimeInMillis()).isLessThanOrEqualTo(now.toInstant().toEpochMilli());
	}

	@Property
	void calendarFuture() {
		// given
		CalendarArbitrary calendarArbitrary = Dates.datesAsCalendar();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"calendarFuture"
		);

		// when
		Calendar actual = this.sut.calendars(calendarArbitrary, context).sample();

		// then
		Calendar now = Calendar.getInstance();
		then(actual.getTimeInMillis()).isGreaterThan(now.toInstant().toEpochMilli());
	}

	@Property
	void calendarFutureOrPresent() {
		// given
		CalendarArbitrary calendarArbitrary = Dates.datesAsCalendar();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"calendarFutureOrPresent"
		);

		// when
		Calendar actual = this.sut.calendars(calendarArbitrary, context).sample();

		// then
		Calendar now = Calendar.getInstance();
		then(actual.getTimeInMillis()).isGreaterThanOrEqualTo(now.toInstant().toEpochMilli());
	}

	@Property
	void dates() {
		// given
		DateArbitrary dateArbitrary = Dates.datesAsDate();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"date"
		);

		// when
		Date actual = this.sut.dates(dateArbitrary, context).sample();

		// then
		then(actual).isNotNull();
	}

	@Property
	void datesPast() {
		// given
		DateArbitrary dateArbitrary = Dates.datesAsDate();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"datePast"
		);

		// when
		Date actual = this.sut.dates(dateArbitrary, context).sample();

		// then
		Date now = new Date();
		then(actual.getTime()).isLessThan(now.getTime());
	}

	@Property
	void datesPastOrPresent() {
		// given
		DateArbitrary dateArbitrary = Dates.datesAsDate();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"datePastOrPresent"
		);

		// when
		Date actual = this.sut.dates(dateArbitrary, context).sample();

		// then
		Date now = new Date();
		then(actual.getTime()).isLessThanOrEqualTo(now.getTime());
	}

	@Property
	void datesFuture() {
		// given
		DateArbitrary dateArbitrary = Dates.datesAsDate();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"dateFuture"
		);

		// when
		Date actual = this.sut.dates(dateArbitrary, context).sample();

		// then
		Date now = new Date();
		then(actual.getTime()).isGreaterThan(now.getTime());
	}

	@Property
	void datesFutureOrPresent() {
		// given
		DateArbitrary dateArbitrary = Dates.datesAsDate();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"dateFutureOrPresent"
		);

		// when
		Date actual = this.sut.dates(dateArbitrary, context).sample();

		// then
		Date now = new Date();
		then(actual.getTime()).isGreaterThanOrEqualTo(now.getTime());
	}

	@Property
	void instant() {
		// given
		InstantArbitrary instantArbitrary = DateTimes.instants();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"instant"
		);

		// when
		Instant actual = this.sut.instants(instantArbitrary, context).sample();

		// then
		then(actual).isNotNull();
	}

	@Property
	void instantPast() {
		// given
		InstantArbitrary instantArbitrary = DateTimes.instants();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"instantPast"
		);

		// when
		Instant actual = this.sut.instants(instantArbitrary, context).sample();

		// then
		Instant now = Instant.now();
		then(actual).isBefore(now);
	}

	@Property
	void instantPastOrPresent() {
		// given
		InstantArbitrary instantArbitrary = DateTimes.instants();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"instantPastOrPresent"
		);

		// when
		Instant actual = this.sut.instants(instantArbitrary, context).sample();

		// then
		Instant now = Instant.now();
		then(actual).isBeforeOrEqualTo(now);
	}

	@Property
	void instantFuture() {
		// given
		InstantArbitrary instantArbitrary = DateTimes.instants();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"instantFuture"
		);

		// when
		Instant actual = this.sut.instants(instantArbitrary, context).sample();

		// then
		Instant now = Instant.now();
		then(actual).isAfter(now);
	}

	@Property
	void instantFutureOrPresent() {
		// given
		InstantArbitrary instantArbitrary = DateTimes.instants();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"instantFutureOrPresent"
		);

		// when
		Instant actual = this.sut.instants(instantArbitrary, context).sample();

		// then
		Instant now = Instant.now();
		then(actual).isAfterOrEqualTo(now);
	}

	@Property
	void localDates() {
		// given
		LocalDateArbitrary localDateArbitrary = Dates.dates();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"localDate"
		);

		// when
		LocalDate actual = this.sut.localDates(localDateArbitrary, context).sample();

		// then
		then(actual).isNotNull();
	}

	@Property
	void localDatesPast() {
		// given
		LocalDateArbitrary localDateArbitrary = Dates.dates();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"localDatePast"
		);

		// when
		LocalDate actual = this.sut.localDates(localDateArbitrary, context).sample();

		// then
		LocalDate now = LocalDate.now();
		then(actual).isBefore(now);
	}

	@Property
	void localDatesPastOrPresent() {
		// given
		LocalDateArbitrary localDateArbitrary = Dates.dates();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"localDatePastOrPresent"
		);

		// when
		LocalDate actual = this.sut.localDates(localDateArbitrary, context).sample();

		// then
		LocalDate now = LocalDate.now();
		then(actual).isBeforeOrEqualTo(now);
	}

	@Property
	void localDatesFuture() {
		// given
		LocalDateArbitrary localDateArbitrary = Dates.dates();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"localDateFuture"
		);

		// when
		LocalDate actual = this.sut.localDates(localDateArbitrary, context).sample();

		// then
		LocalDate now = LocalDate.now();
		then(actual).isAfter(now);
	}

	@Property
	void localDatesFutureOrPresent() {
		// given
		LocalDateArbitrary localDateArbitrary = Dates.dates();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"localDateFutureOrPresent"
		);

		// when
		LocalDate actual = this.sut.localDates(localDateArbitrary, context).sample();

		// then
		LocalDate now = LocalDate.now();
		then(actual).isAfterOrEqualTo(now);
	}

	@Property
	void localDateTimes() {
		// given
		LocalDateTimeArbitrary localDateTimeArbitrary = DateTimes.dateTimes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"localDateTime"
		);

		// when
		LocalDateTime actual = this.sut.localDateTimes(localDateTimeArbitrary, context).sample();

		// then
		then(actual).isNotNull();
	}

	@Property
	void localDateTimesPast() {
		// given
		LocalDateTimeArbitrary localDateTimeArbitrary = DateTimes.dateTimes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"localDateTimePast"
		);

		// when
		LocalDateTime actual = this.sut.localDateTimes(localDateTimeArbitrary, context).sample();

		// then
		LocalDateTime now = LocalDateTime.now();
		then(actual).isBefore(now);
	}

	@Property
	void localDateTimesPastOrPresent() {
		// given
		LocalDateTimeArbitrary localDateTimeArbitrary = DateTimes.dateTimes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"localDateTimePastOrPresent"
		);

		// when
		LocalDateTime actual = this.sut.localDateTimes(localDateTimeArbitrary, context).sample();

		// then
		LocalDateTime now = LocalDateTime.now();
		then(actual).isBeforeOrEqualTo(now);
	}

	@Property
	void localDateTimesFuture() {
		// given
		LocalDateTimeArbitrary localDateTimeArbitrary = DateTimes.dateTimes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"localDateTimeFuture"
		);

		// when
		LocalDateTime actual = this.sut.localDateTimes(localDateTimeArbitrary, context).sample();

		// then
		LocalDateTime now = LocalDateTime.now();
		then(actual).isAfter(now);
	}

	@Property
	void localDateTimesFutureOrPresent() {
		// given
		LocalDateTimeArbitrary localDateTimeArbitrary = DateTimes.dateTimes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"localDateTimeFutureOrPresent"
		);

		// when
		LocalDateTime actual = this.sut.localDateTimes(localDateTimeArbitrary, context).sample();

		// then
		LocalDateTime now = LocalDateTime.now();
		then(actual).isAfterOrEqualTo(now);
	}

	@Property
	void localTimes() {
		// given
		LocalTimeArbitrary localTimeArbitrary = Times.times();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"localTime"
		);

		// when
		LocalTime actual = this.sut.localTimes(localTimeArbitrary, context).sample();

		// then
		then(actual).isNotNull();
	}

	@Property
	void localTimesPast() {
		// given
		LocalTimeArbitrary localTimeArbitrary = Times.times();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"localTimePast"
		);

		// when
		LocalTime actual = this.sut.localTimes(localTimeArbitrary, context).sample();

		// then
		LocalTime now = LocalTime.now();
		then(actual).isBefore(now);
	}

	@Property
	void localTimesPastOrPresent() {
		// given
		LocalTimeArbitrary localTimeArbitrary = Times.times();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"localTimePastOrPresent"
		);

		// when
		LocalTime actual = this.sut.localTimes(localTimeArbitrary, context).sample();

		// then
		LocalTime now = LocalTime.now();
		then(actual).isBeforeOrEqualTo(now);
	}

	@Property
	void localTimesFuture() {
		// given
		LocalTimeArbitrary localTimeArbitrary = Times.times();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"localTimeFuture"
		);

		// when
		LocalTime actual = this.sut.localTimes(localTimeArbitrary, context).sample();

		// then
		LocalTime now = LocalTime.now();
		then(actual).isAfter(now);
	}

	@Property
	void localTimesFutureOrPresent() {
		// given
		LocalTimeArbitrary localTimeArbitrary = Times.times();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"localTimeFutureOrPresent"
		);

		// when
		LocalTime actual = this.sut.localTimes(localTimeArbitrary, context).sample();

		// then
		LocalTime now = LocalTime.now();
		then(actual).isAfterOrEqualTo(now);
	}

	@Property
	void zonedDateTime() {
		// given
		ZonedDateTimeArbitrary zonedDateTimeArbitrary = DateTimes.zonedDateTimes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"zonedDateTime"
		);

		// when
		ZonedDateTime actual = this.sut.zonedDateTimes(zonedDateTimeArbitrary, context).sample();

		// then
		then(actual).isNotNull();
	}

	@Property
	void zonedDateTimePast() {
		// given
		ZonedDateTimeArbitrary zonedDateTimeArbitrary = DateTimes.zonedDateTimes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"zonedDateTimePast"
		);

		// when
		ZonedDateTime actual = this.sut.zonedDateTimes(zonedDateTimeArbitrary, context)
			.sample()
			.withZoneSameLocal(ZoneId.systemDefault());

		// then
		ZonedDateTime now = ZonedDateTime.now();
		then(actual).isBefore(now);
	}

	@Property
	void zonedDateTimePastOrPresent() {
		// given
		ZonedDateTimeArbitrary zonedDateTimeArbitrary = DateTimes.zonedDateTimes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"zonedDateTimePastOrPresent"
		);

		// when
		ZonedDateTime actual = this.sut.zonedDateTimes(zonedDateTimeArbitrary, context)
			.sample()
			.withZoneSameLocal(ZoneId.systemDefault());

		// then
		ZonedDateTime now = ZonedDateTime.now();
		then(actual).isBeforeOrEqualTo(now);
	}

	@Property
	void zonedDateTimeFuture() {
		// given
		ZonedDateTimeArbitrary zonedDateTimeArbitrary = DateTimes.zonedDateTimes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"zonedDateTimeFuture"
		);

		// when
		ZonedDateTime actual = this.sut.zonedDateTimes(zonedDateTimeArbitrary, context)
			.sample()
			.withZoneSameLocal(ZoneId.systemDefault());

		// then
		ZonedDateTime now = ZonedDateTime.now();
		then(actual).isAfter(now);
	}

	@Property
	void zonedDateTimeFutureOrPresent() {
		// given
		ZonedDateTimeArbitrary zonedDateTimeArbitrary = DateTimes.zonedDateTimes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"zonedDateTimeFutureOrPresent"
		);

		// when
		ZonedDateTime actual = this.sut.zonedDateTimes(zonedDateTimeArbitrary, context)
			.sample()
			.withZoneSameLocal(ZoneId.systemDefault());

		// then
		ZonedDateTime now = ZonedDateTime.now();
		then(actual).isAfterOrEqualTo(now);
	}

	@Property
	void monthDay() {
		// given
		MonthDayArbitrary monthDayArbitrary = Dates.monthDays();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"monthDay"
		);

		// when
		MonthDay actual = this.sut.monthDays(monthDayArbitrary, context).sample();

		// then
		then(actual).isNotNull();
	}

	@Property
	void monthDayPast() {
		// given
		MonthDayArbitrary monthDayArbitrary = Dates.monthDays();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"monthDayPast"
		);

		// when
		MonthDay actual = this.sut.monthDays(monthDayArbitrary, context).sample();

		// then
		MonthDay now = MonthDay.now();
		then(actual).isLessThan(now);
	}

	@Property
	void monthDayPastOrPresent() {
		// given
		MonthDayArbitrary monthDayArbitrary = Dates.monthDays();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"monthDayPastOrPresent"
		);

		// when
		MonthDay actual = this.sut.monthDays(monthDayArbitrary, context).sample();

		// then
		MonthDay now = MonthDay.now();
		then(actual).isLessThanOrEqualTo(now);
	}

	@Property
	void monthDayFuture() {
		// given
		MonthDayArbitrary monthDayArbitrary = Dates.monthDays();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"monthDayFuture"
		);

		// when
		MonthDay actual = this.sut.monthDays(monthDayArbitrary, context).sample();

		// then
		MonthDay now = MonthDay.now();
		then(actual).isGreaterThan(now);
	}

	@Property
	void monthDayFutureOrPresent() {
		// given
		MonthDayArbitrary monthDayArbitrary = Dates.monthDays();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"monthDayFutureOrPresent"
		);

		// when
		MonthDay actual = this.sut.monthDays(monthDayArbitrary, context).sample();

		// then
		MonthDay now = MonthDay.now();
		then(actual).isGreaterThanOrEqualTo(now);
	}

	@Property
	void offsetDateTime() {
		// given
		OffsetDateTimeArbitrary offsetDateTimeArbitrary = DateTimes.offsetDateTimes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"offsetDateTime"
		);

		// when
		OffsetDateTime actual = this.sut.offsetDateTimes(offsetDateTimeArbitrary, context).sample();

		// then
		then(actual).isNotNull();
	}

	@Property
	void offsetDateTimePast() {
		// given
		OffsetDateTimeArbitrary offsetDateTimeArbitrary = DateTimes.offsetDateTimes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"offsetDateTimePast"
		);

		// when
		OffsetDateTime actual = this.sut.offsetDateTimes(offsetDateTimeArbitrary, context)
			.sample()
			.withOffsetSameLocal(OffsetDateTime.now().getOffset());

		// then
		OffsetDateTime now = OffsetDateTime.now();
		then(actual).isBefore(now);
	}

	@Property
	void offsetDateTimePastOrPresent() {
		// given
		OffsetDateTimeArbitrary offsetDateTimeArbitrary = DateTimes.offsetDateTimes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"offsetDateTimePastOrPresent"
		);

		// when
		OffsetDateTime actual = this.sut.offsetDateTimes(offsetDateTimeArbitrary, context)
			.sample()
			.withOffsetSameLocal(OffsetDateTime.now().getOffset());

		// then
		OffsetDateTime now = OffsetDateTime.now();
		then(actual).isBeforeOrEqualTo(now);
	}

	@Property
	void offsetDateTimeFuture() {
		// given
		OffsetDateTimeArbitrary offsetDateTimeArbitrary = DateTimes.offsetDateTimes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"offsetDateTimeFuture"
		);

		// when
		OffsetDateTime actual = this.sut.offsetDateTimes(offsetDateTimeArbitrary, context)
			.sample()
			.withOffsetSameLocal(OffsetDateTime.now().getOffset());

		// then
		OffsetDateTime now = OffsetDateTime.now();
		then(actual).isAfter(now);
	}

	@Property
	void offsetDateTimeFutureOrPresent() {
		// given
		OffsetDateTimeArbitrary offsetDateTimeArbitrary = DateTimes.offsetDateTimes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"offsetDateTimeFutureOrPresent"
		);

		// when
		OffsetDateTime actual = this.sut.offsetDateTimes(offsetDateTimeArbitrary, context)
			.sample()
			.withOffsetSameLocal(OffsetDateTime.now().getOffset());

		// then
		OffsetDateTime now = OffsetDateTime.now();
		then(actual).isAfterOrEqualTo(now);
	}

	@Property
	void offsetTime() {
		// given
		OffsetTimeArbitrary offsetTimeArbitrary = Times.offsetTimes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"offsetTime"
		);

		// when
		OffsetTime actual = this.sut.offsetTimes(offsetTimeArbitrary, context).sample();

		// then
		then(actual).isNotNull();
	}

	@Property
	void offsetTimePast() {
		// given
		OffsetTimeArbitrary offsetTimeArbitrary = Times.offsetTimes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"offsetTimePast"
		);

		// when
		OffsetTime actual = this.sut.offsetTimes(offsetTimeArbitrary, context)
			.sample()
			.withOffsetSameLocal(OffsetTime.now().getOffset());

		// then
		OffsetTime now = OffsetTime.now();
		then(actual).isBefore(now);
	}

	@Property
	void offsetTimePastOrPresent() {
		// given
		OffsetTimeArbitrary offsetTimeArbitrary = Times.offsetTimes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"offsetTimePastOrPresent"
		);

		// when
		OffsetTime actual = this.sut.offsetTimes(offsetTimeArbitrary, context)
			.sample()
			.withOffsetSameLocal(OffsetTime.now().getOffset());

		// then
		OffsetTime now = OffsetTime.now();
		then(actual).isBeforeOrEqualTo(now);
	}

	@Property
	void offsetTimeFuture() {
		// given
		OffsetTimeArbitrary offsetTimeArbitrary = Times.offsetTimes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"offsetTimeFuture"
		);

		// when
		OffsetTime actual = this.sut.offsetTimes(offsetTimeArbitrary, context)
			.sample()
			.withOffsetSameLocal(OffsetTime.now().getOffset());

		// then
		OffsetTime now = OffsetTime.now();
		then(actual).isAfter(now);
	}

	@Property
	void offsetTimeFutureOrPresent() {
		// given
		OffsetTimeArbitrary offsetTimeArbitrary = Times.offsetTimes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"offsetTimeFutureOrPresent"
		);

		// when
		OffsetTime actual = this.sut.offsetTimes(offsetTimeArbitrary, context)
			.sample()
			.withOffsetSameLocal(OffsetTime.now().getOffset());

		// then
		OffsetTime now = OffsetTime.now();
		then(actual).isAfterOrEqualTo(now);
	}

	@Property
	void year() {
		// given
		YearArbitrary yearArbitrary = Dates.years();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"year"
		);

		// when
		Year actual = this.sut.years(yearArbitrary, context).sample();

		// then
		Year now = Year.now();
		then(actual).isNotNull();
	}

	@Property
	void yearPast() {
		// given
		YearArbitrary yearArbitrary = Dates.years();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"yearPast"
		);

		// when
		Year actual = this.sut.years(yearArbitrary, context).sample();

		// then
		Year now = Year.now();
		then(actual).isLessThan(now);
	}

	@Property
	void yearPastOrPresent() {
		// given
		YearArbitrary yearArbitrary = Dates.years();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"yearPastOrPresent"
		);

		// when
		Year actual = this.sut.years(yearArbitrary, context).sample();

		// then
		Year now = Year.now();
		then(actual).isLessThanOrEqualTo(now);
	}

	@Property
	void yearFuture() {
		// given
		YearArbitrary yearArbitrary = Dates.years();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"yearFuture"
		);

		// when
		Year actual = this.sut.years(yearArbitrary, context).sample();

		// then
		Year now = Year.now();
		then(actual).isGreaterThan(now);
	}

	@Property
	void yearFutureOrPresent() {
		// given
		YearArbitrary yearArbitrary = Dates.years();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"yearFutureOrPresent"
		);

		// when
		Year actual = this.sut.years(yearArbitrary, context).sample();

		// then
		Year now = Year.now();
		then(actual).isGreaterThanOrEqualTo(now);
	}

	@Property
	void yearMonth() {
		// given
		YearMonthArbitrary yearMonthArbitrary = Dates.yearMonths();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"yearMonth"
		);

		// when
		YearMonth actual = this.sut.yearMonths(yearMonthArbitrary, context).sample();

		// then
		YearMonth now = YearMonth.now();
		then(actual).isNotNull();
	}

	@Property
	void yearMonthPast() {
		// given
		YearMonthArbitrary yearMonthArbitrary = Dates.yearMonths();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"yearMonthPast"
		);

		// when
		YearMonth actual = this.sut.yearMonths(yearMonthArbitrary, context).sample();

		// then
		YearMonth now = YearMonth.now();
		then(actual).isLessThan(now);
	}

	@Property
	void yearMonthPastOrPresent() {
		// given
		YearMonthArbitrary yearMonthArbitrary = Dates.yearMonths();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"yearMonthPastOrPresent"
		);

		// when
		YearMonth actual = this.sut.yearMonths(yearMonthArbitrary, context).sample();

		// then
		YearMonth now = YearMonth.now();
		then(actual).isLessThanOrEqualTo(now);
	}

	@Property
	void yearMonthFuture() {
		// given
		YearMonthArbitrary yearMonthArbitrary = Dates.yearMonths();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"yearMonthFuture"
		);

		// when
		YearMonth actual = this.sut.yearMonths(yearMonthArbitrary, context).sample();

		// then
		YearMonth now = YearMonth.now();
		then(actual).isGreaterThan(now);
	}

	@Property
	void yearMonthFutureOrPresent() {
		// given
		YearMonthArbitrary yearMonthArbitrary = Dates.yearMonths();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"yearMonthFutureOrPresent"
		);

		// when
		YearMonth actual = this.sut.yearMonths(yearMonthArbitrary, context).sample();

		// then
		YearMonth now = YearMonth.now();
		then(actual).isGreaterThanOrEqualTo(now);
	}

	@Property
	void periods() {
		// given
		PeriodArbitrary periodArbitrary = Dates.periods();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"period"
		);

		// when
		Period actual = this.sut.periods(periodArbitrary, context).sample();

		// then
		then(actual).isNotNull();
	}

	@Property
	void durations() {
		// given
		DurationArbitrary durationArbitrary = Times.durations();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"duration"
		);

		// when
		Duration actual = this.sut.durations(durationArbitrary, context).sample();

		// then
		then(actual).isNotNull();
	}

	@Property
	void zoneOffsets() {
		// given
		ZoneOffsetArbitrary zoneOffsetArbitrary = Times.zoneOffsets();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"zoneOffset"
		);
		// when
		ZoneOffset actual = this.sut.zoneOffsets(zoneOffsetArbitrary, context).sample();

		// then
		then(actual).isNotNull();
	}

	private <T> ArbitraryGeneratorContext makeContext(TypeReference<T> typeReference, String propertyName) {
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();

		return new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				new ObjectProperty(
					property,
					PropertyNameResolver.IDENTITY,
					0.0D,
					null,
					Collections.emptyList()
				),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null),
			Collections.emptyList()
		);
	}
}
