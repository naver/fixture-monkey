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
import com.navercorp.fixturemonkey.api.introspector.JavaTimeArbitraryResolver;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class JavaxValidationJavaTimeArbitraryResolver implements JavaTimeArbitraryResolver {
	private final JavaxValidationTimeConstraintGenerator constraintGenerator;

	public JavaxValidationJavaTimeArbitraryResolver() {
		this(new JavaxValidationTimeConstraintGenerator());
	}

	public JavaxValidationJavaTimeArbitraryResolver(JavaxValidationTimeConstraintGenerator constraintGenerator) {
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
		JavaxValidationDateTimeConstraint constraint =
			this.constraintGenerator.generateDateTimeConstraint(context);
		LocalDateTime min = constraint.getMin();
		LocalDateTime max = constraint.getMax();

		if (min != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(min.getYear(), min.getMonth().ordinal(), min.getDayOfMonth() + 1);
			dateArbitrary = dateArbitrary.atTheEarliest(calendar.getTime());
		}
		if (max != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(max.getYear(), max.getMonth().ordinal(), max.getDayOfMonth());
			dateArbitrary = dateArbitrary.atTheLatest(calendar.getTime());
		}

		return dateArbitrary;
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
		JavaxValidationDateConstraint constraint = this.constraintGenerator.generateDateConstraint(context);
		LocalDate min = constraint.getMin();
		LocalDate max = constraint.getMax();

		if (min != null) {
			localDateArbitrary = localDateArbitrary.atTheEarliest(min);
		}
		if (max != null) {
			localDateArbitrary = localDateArbitrary.atTheLatest(max);
		}

		return localDateArbitrary;
	}

	@Override
	public Arbitrary<LocalDateTime> localDateTimes(
		LocalDateTimeArbitrary localDateTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaxValidationDateTimeConstraint constraint =
			this.constraintGenerator.generateDateTimeConstraint(context);
		LocalDateTime min = constraint.getMin();
		LocalDateTime max = constraint.getMax();

		if (min != null) {
			localDateTimeArbitrary = localDateTimeArbitrary.atTheEarliest(min);
		}
		if (max != null) {
			localDateTimeArbitrary = localDateTimeArbitrary.atTheLatest(max);
		}

		return localDateTimeArbitrary;
	}

	@Override
	public Arbitrary<LocalTime> localTimes(
		LocalTimeArbitrary localTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaxValidationTimeConstraint constraint = this.constraintGenerator.generateTimeConstraint(context);
		LocalTime min = constraint.getMin();
		LocalTime max = constraint.getMax();

		if (min != null) {
			localTimeArbitrary = localTimeArbitrary.atTheEarliest(min);
		}
		if (max != null) {
			localTimeArbitrary = localTimeArbitrary.atTheLatest(max);
		}

		return localTimeArbitrary;
	}

	@Override
	public Arbitrary<ZonedDateTime> zonedDateTimes(
		ZonedDateTimeArbitrary zonedDateTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaxValidationDateTimeConstraint constraint =
			this.constraintGenerator.generateDateTimeConstraint(context);
		LocalDateTime min = constraint.getMin();
		LocalDateTime max = constraint.getMax();

		if (min != null) {
			zonedDateTimeArbitrary = zonedDateTimeArbitrary.atTheEarliest(min);
		}
		if (max != null) {
			zonedDateTimeArbitrary = zonedDateTimeArbitrary.atTheLatest(max);
		}

		return zonedDateTimeArbitrary;
	}

	@Override
	public Arbitrary<MonthDay> monthDays(
		MonthDayArbitrary monthDayArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaxValidationDateConstraint constraint = this.constraintGenerator.generateDateConstraint(context);
		LocalDate min = constraint.getMin();
		LocalDate max = constraint.getMax();

		if (min != null) {
			monthDayArbitrary = monthDayArbitrary.atTheEarliest(MonthDay.of(min.getMonth(), min.getDayOfMonth()));
		}
		if (max != null) {
			monthDayArbitrary = monthDayArbitrary.atTheLatest(MonthDay.of(max.getMonth(), max.getDayOfMonth()));
		}

		return monthDayArbitrary;
	}

	@Override
	public Arbitrary<OffsetDateTime> offsetDateTimes(
		OffsetDateTimeArbitrary offsetDateTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaxValidationDateTimeConstraint constraint =
			this.constraintGenerator.generateDateTimeConstraint(context);
		LocalDateTime min = constraint.getMin();
		LocalDateTime max = constraint.getMax();

		if (min != null) {
			offsetDateTimeArbitrary = offsetDateTimeArbitrary.atTheEarliest(min);
		}
		if (max != null) {
			offsetDateTimeArbitrary = offsetDateTimeArbitrary.atTheLatest(max);
		}

		return offsetDateTimeArbitrary;
	}

	@Override
	public Arbitrary<OffsetTime> offsetTimes(
		OffsetTimeArbitrary offsetTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaxValidationTimeConstraint constraint = this.constraintGenerator.generateTimeConstraint(context);
		LocalTime min = constraint.getMin();
		LocalTime max = constraint.getMax();

		if (min != null) {
			offsetTimeArbitrary = offsetTimeArbitrary.atTheEarliest(min);
		}
		if (max != null) {
			offsetTimeArbitrary = offsetTimeArbitrary.atTheLatest(max);
		}

		return offsetTimeArbitrary;
	}

	@Override
	public Arbitrary<Year> years(
		YearArbitrary yearArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaxValidationYearConstraint constraint = this.constraintGenerator.generateYearConstraint(context);
		Year min = constraint.getMin();
		Year max = constraint.getMax();

		if (min != null) {
			yearArbitrary = yearArbitrary.between(min.getValue(), Year.MAX_VALUE);
		}
		if (max != null) {
			yearArbitrary = yearArbitrary.between(Year.MIN_VALUE, max.getValue());
		}

		return yearArbitrary;
	}

	@Override
	public Arbitrary<YearMonth> yearMonths(
		YearMonthArbitrary yearMonthArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaxValidationYearMonthConstraint constraint = this.constraintGenerator.generateYearMonthConstraint(context);
		YearMonth min = constraint.getMin();
		YearMonth max = constraint.getMax();

		if (min != null) {
			yearMonthArbitrary = yearMonthArbitrary.atTheEarliest(min);
		}
		if (max != null) {
			yearMonthArbitrary = yearMonthArbitrary.atTheLatest(max);
		}

		return yearMonthArbitrary;
	}

	@Override
	public Arbitrary<Period> periods(
		PeriodArbitrary periodArbitrary,
		ArbitraryGeneratorContext context
	) {
		return periodArbitrary;
	}

	@Override
	public Arbitrary<Duration> durations(
		DurationArbitrary durationArbitrary,
		ArbitraryGeneratorContext context
	) {
		return durationArbitrary;
	}

	@Override
	public Arbitrary<ZoneOffset> zoneOffsets(
		ZoneOffsetArbitrary zoneOffsetArbitrary,
		ArbitraryGeneratorContext context
	) {
		return zoneOffsetArbitrary;
	}
}
