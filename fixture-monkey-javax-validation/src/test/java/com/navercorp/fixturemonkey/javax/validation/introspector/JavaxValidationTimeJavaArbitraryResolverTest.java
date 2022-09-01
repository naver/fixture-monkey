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
import net.jqwik.api.Arbitrary;
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
		Arbitrary<Calendar> actual = this.sut.calendars(calendarArbitrary, context);

		// then
		Calendar calendar = actual.sample();
		then(calendar).isNotNull();
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
		Arbitrary<Calendar> actual = this.sut.calendars(calendarArbitrary, context);

		// then
		Calendar calendar = actual.sample();

		Calendar now = Calendar.getInstance();
		then(calendar.toInstant().toEpochMilli()).isLessThan(now.toInstant().toEpochMilli());
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
		Arbitrary<Calendar> actual = this.sut.calendars(calendarArbitrary, context);

		// then
		Calendar calendar = actual.sample();

		Calendar now = Calendar.getInstance();
		then(calendar.getTimeInMillis()).isLessThanOrEqualTo(now.toInstant().toEpochMilli());
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
		Arbitrary<Calendar> actual = this.sut.calendars(calendarArbitrary, context);

		// then
		Calendar calendar = actual.sample();

		Calendar now = Calendar.getInstance();
		then(calendar.getTimeInMillis()).isGreaterThan(now.toInstant().toEpochMilli());
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
		Arbitrary<Calendar> actual = this.sut.calendars(calendarArbitrary, context);

		// then
		Calendar calendar = actual.sample();

		Calendar now = Calendar.getInstance();
		then(calendar.getTimeInMillis()).isGreaterThanOrEqualTo(now.toInstant().toEpochMilli());
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
		Arbitrary<Date> actual = this.sut.dates(dateArbitrary, context);

		// then
		Date date = actual.sample();
		then(date).isNotNull();
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
		Arbitrary<Date> actual = this.sut.dates(dateArbitrary, context);

		// then
		Date date = actual.sample();

		Date now = new Date();
		then(date.getTime()).isLessThan(now.getTime());
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
		Arbitrary<Date> actual = this.sut.dates(dateArbitrary, context);

		// then
		Date date = actual.sample();

		Date now = new Date();
		then(date.getTime()).isLessThanOrEqualTo(now.getTime());
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
		Arbitrary<Date> actual = this.sut.dates(dateArbitrary, context);

		// then
		Date date = actual.sample();

		Date now = new Date();
		then(date.getTime()).isGreaterThan(now.getTime());
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
		Arbitrary<Date> actual = this.sut.dates(dateArbitrary, context);

		// then
		Date date = actual.sample();

		Date now = new Date();
		then(date.getTime()).isGreaterThanOrEqualTo(now.getTime());
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
		Arbitrary<Instant> actual = this.sut.instants(instantArbitrary, context);

		// then
		Instant instant = actual.sample();
		then(instant).isNotNull();
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
		Arbitrary<Instant> actual = this.sut.instants(instantArbitrary, context);

		// then
		Instant instant = actual.sample();
		then(instant).isNotNull();

		Instant now = Instant.now();
		then(instant).isBefore(now);
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
		Arbitrary<Instant> actual = this.sut.instants(instantArbitrary, context);

		// then
		Instant instant = actual.sample();
		then(instant).isNotNull();

		Instant now = Instant.now();
		then(instant).isBeforeOrEqualTo(now);
	}

	@Property
	void instantFuture() {
		// given
		Instant now = Instant.now();
		InstantArbitrary instantArbitrary = DateTimes.instants();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"instantFuture"
		);

		// when
		Arbitrary<Instant> actual = this.sut.instants(instantArbitrary, context);

		// then
		Instant instant = actual.sample();
		then(instant).isAfter(now);
	}

	@Property
	void instantFutureOrPresent() {
		// given
		Instant now = Instant.now();
		InstantArbitrary instantArbitrary = DateTimes.instants();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"instantFutureOrPresent"
		);

		// when
		Arbitrary<Instant> actual = this.sut.instants(instantArbitrary, context);

		// then
		Instant instant = actual.sample();
		then(instant).isAfterOrEqualTo(now);
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
		Arbitrary<LocalDate> actual = this.sut.localDates(localDateArbitrary, context);

		// then
		LocalDate localDate = actual.sample();
		then(localDate).isNotNull();
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
		Arbitrary<LocalDate> actual = this.sut.localDates(localDateArbitrary, context);

		// then
		LocalDate localDate = actual.sample();
		LocalDate now = LocalDate.now();
		then(localDate).isBefore(now);
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
		Arbitrary<LocalDate> actual = this.sut.localDates(localDateArbitrary, context);

		// then
		LocalDate localDate = actual.sample();
		LocalDate now = LocalDate.now();
		then(localDate).isBeforeOrEqualTo(now);
	}

	@Property
	void localDatesFuture() {
		// given
		LocalDate now = LocalDate.now();
		LocalDateArbitrary localDateArbitrary = Dates.dates();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"localDateFuture"
		);

		// when
		Arbitrary<LocalDate> actual = this.sut.localDates(localDateArbitrary, context);

		// then
		LocalDate localDate = actual.sample();
		then(localDate).isAfter(now);
	}

	@Property
	void localDatesFutureOrPresent() {
		// given
		LocalDate now = LocalDate.now();
		LocalDateArbitrary localDateArbitrary = Dates.dates();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"localDateFutureOrPresent"
		);

		// when
		Arbitrary<LocalDate> actual = this.sut.localDates(localDateArbitrary, context);

		// then
		LocalDate localDate = actual.sample();
		then(localDate).isAfterOrEqualTo(now);
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
		Arbitrary<LocalDateTime> actual = this.sut.localDateTimes(localDateTimeArbitrary, context);

		// then
		LocalDateTime localDateTime = actual.sample();
		then(localDateTime).isNotNull();
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
		Arbitrary<LocalDateTime> actual = this.sut.localDateTimes(localDateTimeArbitrary, context);

		// then
		LocalDateTime localDateTime = actual.sample();
		LocalDateTime now = LocalDateTime.now();
		then(localDateTime).isBefore(now);
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
		Arbitrary<LocalDateTime> actual = this.sut.localDateTimes(localDateTimeArbitrary, context);

		// then
		LocalDateTime localDateTime = actual.sample();
		LocalDateTime now = LocalDateTime.now();
		then(localDateTime).isBeforeOrEqualTo(now);
	}

	@Property
	void localDateTimesFuture() {
		// given
		LocalDateTime now = LocalDateTime.now();
		LocalDateTimeArbitrary localDateTimeArbitrary = DateTimes.dateTimes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"localDateTimeFuture"
		);

		// when
		Arbitrary<LocalDateTime> actual = this.sut.localDateTimes(localDateTimeArbitrary, context);

		// then
		LocalDateTime localDateTime = actual.sample();
		then(localDateTime).isAfter(now);
	}

	@Property
	void localDateTimesFutureOrPresent() {
		// given
		LocalDateTime now = LocalDateTime.now();
		LocalDateTimeArbitrary localDateTimeArbitrary = DateTimes.dateTimes();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"localDateTimeFutureOrPresent"
		);

		// when
		Arbitrary<LocalDateTime> actual = this.sut.localDateTimes(localDateTimeArbitrary, context);

		// then
		LocalDateTime localDateTime = actual.sample();
		then(localDateTime).isAfterOrEqualTo(now);
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
		Arbitrary<LocalTime> actual = this.sut.localTimes(localTimeArbitrary, context);

		// then
		LocalTime localTime = actual.sample();
		then(localTime).isNotNull();
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
		Arbitrary<LocalTime> actual = this.sut.localTimes(localTimeArbitrary, context);

		// then
		LocalTime localTime = actual.sample();
		LocalTime now = LocalTime.now();
		then(localTime).isBefore(now);
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
		Arbitrary<LocalTime> actual = this.sut.localTimes(localTimeArbitrary, context);

		// then
		LocalTime localTime = actual.sample();
		LocalTime now = LocalTime.now();
		then(localTime).isBeforeOrEqualTo(now);
	}

	@Property
	void localTimesFuture() {
		// given
		LocalTime now = LocalTime.now();
		LocalTimeArbitrary localTimeArbitrary = Times.times();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"localTimeFuture"
		);

		// when
		Arbitrary<LocalTime> actual = this.sut.localTimes(localTimeArbitrary, context);

		// then
		LocalTime localTime = actual.sample();
		then(localTime).isAfter(now);
	}

	@Property
	void localTimesFutureOrPresent() {
		// given
		LocalTime now = LocalTime.now();
		LocalTimeArbitrary localTimeArbitrary = Times.times();
		ArbitraryGeneratorContext context = makeContext(
			new TypeReference<TimeIntrospectorSpec>() {
			},
			"localTimeFutureOrPresent"
		);

		// when
		Arbitrary<LocalTime> actual = this.sut.localTimes(localTimeArbitrary, context);

		// then
		LocalTime localTime = actual.sample();
		then(localTime).isAfterOrEqualTo(now);
	}

	//Todo: zonedDateTimes, monthdays, offsetDatetimes, offsetTimes, years, yearmonths
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
		Arbitrary<ZonedDateTime> actual = this.sut.zonedDateTimes(zonedDateTimeArbitrary, context);

		// then
		ZonedDateTime zonedDateTime = actual.sample();
		then(zonedDateTime).isNotNull();
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
		Arbitrary<ZonedDateTime> actual = this.sut.zonedDateTimes(zonedDateTimeArbitrary, context);

		// then
		ZonedDateTime zonedDateTime = actual.sample().withZoneSameLocal(ZoneId.systemDefault());

		ZonedDateTime now = ZonedDateTime.now();
		then(zonedDateTime).isBefore(now);
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
		Arbitrary<ZonedDateTime> actual = this.sut.zonedDateTimes(zonedDateTimeArbitrary, context);

		// then
		ZonedDateTime zonedDateTime = actual.sample().withZoneSameLocal(ZoneId.systemDefault());

		ZonedDateTime now = ZonedDateTime.now();
		then(zonedDateTime).isBeforeOrEqualTo(now);
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
		Arbitrary<ZonedDateTime> actual = this.sut.zonedDateTimes(zonedDateTimeArbitrary, context);

		// then
		ZonedDateTime zonedDateTime = actual.sample().withZoneSameLocal(ZoneId.systemDefault());

		ZonedDateTime now = ZonedDateTime.now();
		then(zonedDateTime).isAfter(now);
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
		Arbitrary<ZonedDateTime> actual = this.sut.zonedDateTimes(zonedDateTimeArbitrary, context);

		// then
		ZonedDateTime zonedDateTime = actual.sample().withZoneSameLocal(ZoneId.systemDefault());

		ZonedDateTime now = ZonedDateTime.now();
		then(zonedDateTime).isAfterOrEqualTo(now);
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
		Arbitrary<MonthDay> actual = this.sut.monthDays(monthDayArbitrary, context);

		// then
		MonthDay monthDay = actual.sample();
		then(monthDay).isNotNull();
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
		Arbitrary<MonthDay> actual = this.sut.monthDays(monthDayArbitrary, context);

		// then
		MonthDay monthDay = actual.sample();

		MonthDay now = MonthDay.now();
		then(monthDay).isLessThan(now);
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
		Arbitrary<MonthDay> actual = this.sut.monthDays(monthDayArbitrary, context);

		// then
		MonthDay monthDay = actual.sample();

		MonthDay now = MonthDay.now();
		then(monthDay).isLessThanOrEqualTo(now);
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
		Arbitrary<MonthDay> actual = this.sut.monthDays(monthDayArbitrary, context);

		// then
		MonthDay monthDay = actual.sample();

		MonthDay now = MonthDay.now();
		then(monthDay).isGreaterThan(now);
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
		Arbitrary<MonthDay> actual = this.sut.monthDays(monthDayArbitrary, context);

		// then
		MonthDay monthDay = actual.sample();

		MonthDay now = MonthDay.now();
		then(monthDay).isGreaterThanOrEqualTo(now);
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
		Arbitrary<OffsetDateTime> actual = this.sut.offsetDateTimes(offsetDateTimeArbitrary, context);

		// then
		OffsetDateTime offsetDateTime = actual.sample();
		then(offsetDateTime).isNotNull();
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
		Arbitrary<OffsetDateTime> actual = this.sut.offsetDateTimes(offsetDateTimeArbitrary, context);

		// then
		OffsetDateTime offsetDateTime = actual.sample().withOffsetSameLocal(OffsetDateTime.now().getOffset());

		OffsetDateTime now = OffsetDateTime.now();
		then(offsetDateTime).isBefore(now);
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
		Arbitrary<OffsetDateTime> actual = this.sut.offsetDateTimes(offsetDateTimeArbitrary, context);

		// then
		OffsetDateTime offsetDateTime = actual.sample().withOffsetSameLocal(OffsetDateTime.now().getOffset());

		OffsetDateTime now = OffsetDateTime.now();
		then(offsetDateTime).isBeforeOrEqualTo(now);
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
		Arbitrary<OffsetDateTime> actual = this.sut.offsetDateTimes(offsetDateTimeArbitrary, context);

		// then
		OffsetDateTime offsetDateTime = actual.sample().withOffsetSameLocal(OffsetDateTime.now().getOffset());

		OffsetDateTime now = OffsetDateTime.now();
		then(offsetDateTime).isAfter(now);
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
		Arbitrary<OffsetDateTime> actual = this.sut.offsetDateTimes(offsetDateTimeArbitrary, context);

		// then
		OffsetDateTime offsetDateTime = actual.sample().withOffsetSameLocal(OffsetDateTime.now().getOffset());

		OffsetDateTime now = OffsetDateTime.now();
		then(offsetDateTime).isAfterOrEqualTo(now);
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
		Arbitrary<OffsetTime> actual = this.sut.offsetTimes(offsetTimeArbitrary, context);

		// then
		OffsetTime offsetTime = actual.sample();
		then(offsetTime).isNotNull();
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
		Arbitrary<OffsetTime> actual = this.sut.offsetTimes(offsetTimeArbitrary, context);

		// then
		OffsetTime offsetTime = actual.sample().withOffsetSameLocal(OffsetTime.now().getOffset());

		OffsetTime now = OffsetTime.now();
		then(offsetTime).isBefore(now);
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
		Arbitrary<OffsetTime> actual = this.sut.offsetTimes(offsetTimeArbitrary, context);

		// then
		OffsetTime offsetTime = actual.sample().withOffsetSameLocal(OffsetTime.now().getOffset());

		OffsetTime now = OffsetTime.now();
		then(offsetTime).isBeforeOrEqualTo(now);
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
		Arbitrary<OffsetTime> actual = this.sut.offsetTimes(offsetTimeArbitrary, context);

		// then
		OffsetTime offsetTime = actual.sample().withOffsetSameLocal(OffsetTime.now().getOffset());

		OffsetTime now = OffsetTime.now();
		then(offsetTime).isAfter(now);
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
		Arbitrary<OffsetTime> actual = this.sut.offsetTimes(offsetTimeArbitrary, context);

		// then
		OffsetTime offsetTime = actual.sample().withOffsetSameLocal(OffsetTime.now().getOffset());

		OffsetTime now = OffsetTime.now();
		then(offsetTime).isAfterOrEqualTo(now);
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
		Arbitrary<Year> actual = this.sut.years(yearArbitrary, context);

		// then
		Year year = actual.sample();
		then(year).isNotNull();
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
		Arbitrary<Year> actual = this.sut.years(yearArbitrary, context);

		// then
		Year year = actual.sample();

		Year now = Year.now();
		then(year).isLessThan(now);
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
		Arbitrary<Year> actual = this.sut.years(yearArbitrary, context);

		// then
		Year year = actual.sample();

		Year now = Year.now();
		then(year).isLessThanOrEqualTo(now);
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
		Arbitrary<Year> actual = this.sut.years(yearArbitrary, context);

		// then
		Year year = actual.sample();

		Year now = Year.now();
		then(year).isGreaterThan(now);
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
		Arbitrary<Year> actual = this.sut.years(yearArbitrary, context);

		// then
		Year year = actual.sample();

		Year now = Year.now();
		then(year).isGreaterThanOrEqualTo(now);
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
		Arbitrary<YearMonth> actual = this.sut.yearMonths(yearMonthArbitrary, context);

		// then
		YearMonth yearMonth = actual.sample();
		then(yearMonth).isNotNull();
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
		Arbitrary<YearMonth> actual = this.sut.yearMonths(yearMonthArbitrary, context);

		// then
		YearMonth yearMonth = actual.sample();

		YearMonth now = YearMonth.now();
		then(yearMonth).isLessThan(now);
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
		Arbitrary<YearMonth> actual = this.sut.yearMonths(yearMonthArbitrary, context);

		// then
		YearMonth yearMonth = actual.sample();

		YearMonth now = YearMonth.now();
		then(yearMonth).isLessThanOrEqualTo(now);
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
		Arbitrary<YearMonth> actual = this.sut.yearMonths(yearMonthArbitrary, context);

		// then
		YearMonth yearMonth = actual.sample();

		YearMonth now = YearMonth.now();
		then(yearMonth).isGreaterThan(now);
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
		Arbitrary<YearMonth> actual = this.sut.yearMonths(yearMonthArbitrary, context);

		// then
		YearMonth yearMonth = actual.sample();

		YearMonth now = YearMonth.now();
		then(yearMonth).isGreaterThanOrEqualTo(now);
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
		Arbitrary<Period> actual = this.sut.periods(periodArbitrary, context);

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
		Arbitrary<Duration> actual = this.sut.durations(durationArbitrary, context);

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
		Arbitrary<ZoneOffset> actual = this.sut.zoneOffsets(zoneOffsetArbitrary, context);

		// then
		then(actual).isNotNull();
	}

	private <T> ArbitraryGeneratorContext makeContext(TypeReference<T> typeReference, String propertyName) {
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(typeReference.getAnnotatedType(), propertyName).get();

		return new ArbitraryGeneratorContext(
			new ArbitraryProperty(
				property,
				PropertyNameResolver.IDENTITY,
				0.0D,
				null,
				Collections.emptyList(),
				null
			),
			Collections.emptyList(),
			null,
			(ctx, prop) -> Arbitraries.just(null),
			Collections.emptyList()
		);
	}
}
