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
import java.time.Period;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

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
import net.jqwik.time.api.arbitraries.PeriodArbitrary;
import net.jqwik.time.api.arbitraries.ZoneOffsetArbitrary;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.property.PropertyCache;

class JavaxValidationTimeArbitraryIntrospectorTest {
	private final JavaxValidationTimeArbitraryIntrospector sut = new JavaxValidationTimeArbitraryIntrospector();

	@Property
	void calendar() {
		// given
		CalendarArbitrary calendarArbitrary = Dates.datesAsCalendar();
		String propertyName = "calendar";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "calendarPast";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "calendarPastOrPresent";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "calendarFuture";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "calendarFutureOrPresent";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "date";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "datePast";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "datePastOrPresent";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "dateFuture";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "dateFutureOrPresent";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "instant";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "instantPast";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "instantPastOrPresent";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "instantFuture";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "instantFutureOrPresent";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "localDate";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "localDatePast";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "localDatePastOrPresent";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "localDateFuture";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "localDateFutureOrPresent";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "localDateTime";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "localDateTimePast";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "localDateTimePastOrPresent";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "localDateTimeFuture";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "localDateTimeFutureOrPresent";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "localTime";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "localTimePast";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "localTimePastOrPresent";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "localTimeFuture";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
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
		String propertyName = "localTimeFutureOrPresent";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		Arbitrary<LocalTime> actual = this.sut.localTimes(localTimeArbitrary, context);

		// then
		LocalTime localTime = actual.sample();
		then(localTime).isAfterOrEqualTo(now);
	}

	@Property
	void durations() {
		// given
		DurationArbitrary durationArbitrary = Times.durations();
		String propertyName = "duration";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		Arbitrary<Duration> actual = this.sut.durations(durationArbitrary, context);

		// then
		then(actual).isNotNull();
	}

	@Property
	void periods() {
		// given
		PeriodArbitrary periodArbitrary = Dates.periods();
		String propertyName = "period";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		Arbitrary<Period> actual = this.sut.periods(periodArbitrary, context);

		// then
		then(actual).isNotNull();
	}

	@Property
	void zoneOffsets() {
		// given
		ZoneOffsetArbitrary zoneOffsetArbitrary = Times.zoneOffsets();
		String propertyName = "zoneOffset";
		com.navercorp.fixturemonkey.api.property.Property property =
			PropertyCache.getProperty(TimeIntrospectorSpec.class, propertyName).get();
		ArbitraryGeneratorContext context = new ArbitraryGeneratorContext(
			new ArbitraryProperty(property, "", null, false, 0.0D),
			Collections.emptyList()
		);

		// when
		Arbitrary<ZoneOffset> actual = this.sut.zoneOffsets(zoneOffsetArbitrary, context);

		// then
		then(actual).isNotNull();
	}
}
