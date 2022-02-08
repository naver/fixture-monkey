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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.Year;
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
import com.navercorp.fixturemonkey.api.introspector.TimeArbitraryIntrospector;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class JavaxValidationTimeArbitraryIntrospector implements TimeArbitraryIntrospector {
	private final JavaxValidationTimeConstraintGenerator constraintGenerator;

	public JavaxValidationTimeArbitraryIntrospector() {
		this(new JavaxValidationTimeConstraintGenerator());
	}

	public JavaxValidationTimeArbitraryIntrospector(JavaxValidationTimeConstraintGenerator constraintGenerator) {
		this.constraintGenerator = constraintGenerator;
	}

	@Override
	public Arbitrary<Calendar> calendars(
		CalendarArbitrary calendarArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaxValidationDateTimeConstraint constraint =
			this.constraintGenerator.generateDateTimeConstraint(context);
		LocalDateTime min = constraint.getMin();
		LocalDateTime max = constraint.getMax();

		if (min != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(min.getYear(), min.getMonth().ordinal(), min.getDayOfMonth() + 1);
			calendarArbitrary = calendarArbitrary.atTheEarliest(calendar);
		}
		if (max != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(max.getYear(), max.getMonth().ordinal(), max.getDayOfMonth());
			calendarArbitrary = calendarArbitrary.atTheLatest(calendar);
		}

		return calendarArbitrary;
	}

	@Override
	public Arbitrary<Date> dates(
		DateArbitrary dateArbitrary,
		ArbitraryGeneratorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<Instant> instants(
		InstantArbitrary instantArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaxValidationDateTimeConstraint constraint =
			this.constraintGenerator.generateDateTimeConstraint(context);
		LocalDateTime min = constraint.getMin();
		LocalDateTime max = constraint.getMax();

		if (min != null) {
			instantArbitrary = instantArbitrary.atTheEarliest(min.atZone(ZoneOffset.systemDefault()).toInstant());
		}
		if (max != null) {
			instantArbitrary = instantArbitrary.atTheLatest(max.atZone(ZoneOffset.systemDefault()).toInstant());
		}

		return instantArbitrary;
	}

	@Override
	public Arbitrary<LocalDate> localDates(
		LocalDateArbitrary localDateArbitrary,
		ArbitraryGeneratorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<LocalDateTime> localDateTimes(
		LocalDateTimeArbitrary localDateTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<LocalTime> localTimes(
		LocalTimeArbitrary localTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<ZonedDateTime> zonedDateTimes(
		ZonedDateTimeArbitrary zonedDateTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<MonthDay> monthDays(
		MonthDayArbitrary monthDayArbitrary,
		ArbitraryGeneratorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<OffsetDateTime> offsetDateTimes(
		OffsetDateTimeArbitrary offsetDateTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<OffsetTimeArbitrary> offsetTimes(
		OffsetTimeArbitrary offsetTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<PeriodArbitrary> periods(
		PeriodArbitrary periodArbitrary,
		ArbitraryGeneratorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<DurationArbitrary> durations(
		DurationArbitrary durationArbitrary,
		ArbitraryGeneratorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<Year> years(
		YearArbitrary yearArbitrary,
		ArbitraryGeneratorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<YearMonthArbitrary> yearMonths(
		YearMonthArbitrary yearMonthArbitrary,
		ArbitraryGeneratorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<ZoneOffsetArbitrary> zoneOffsets(
		ZoneOffsetArbitrary zoneOffsetArbitrary,
		ArbitraryGeneratorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}
}
