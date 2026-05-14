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

package com.navercorp.fixturemonkey.api.jqwik;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;
import net.jqwik.time.api.arbitraries.CalendarArbitrary;
import net.jqwik.time.api.arbitraries.DateArbitrary;
import net.jqwik.time.api.arbitraries.InstantArbitrary;
import net.jqwik.time.api.arbitraries.LocalDateArbitrary;
import net.jqwik.time.api.arbitraries.LocalDateTimeArbitrary;
import net.jqwik.time.api.arbitraries.LocalTimeArbitrary;
import net.jqwik.time.api.arbitraries.MonthDayArbitrary;
import net.jqwik.time.api.arbitraries.OffsetDateTimeArbitrary;
import net.jqwik.time.api.arbitraries.OffsetTimeArbitrary;
import net.jqwik.time.api.arbitraries.YearArbitrary;
import net.jqwik.time.api.arbitraries.YearMonthArbitrary;
import net.jqwik.time.api.arbitraries.ZonedDateTimeArbitrary;

import com.navercorp.fixturemonkey.api.constraint.JavaConstraintGenerator;
import com.navercorp.fixturemonkey.api.constraint.JavaDateTimeConstraint;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;

/**
 * It would be moved into jqwik module in 0.7.0.
 */
@API(since = "0.6.9", status = Status.EXPERIMENTAL)
public final class JqwikJavaTimeArbitraryResolver implements JavaTimeArbitraryResolver {
	private static final ZoneOffset ZONE_OFFSET = OffsetDateTime.now().getOffset();
	private static final ZoneId ZONE_ID = ZoneId.systemDefault();

	private final JavaConstraintGenerator constraintGenerator;

	public JqwikJavaTimeArbitraryResolver(JavaConstraintGenerator constraintGenerator) {
		this.constraintGenerator = constraintGenerator;
	}

	@Override
	public Arbitrary<Calendar> calendars(
		CalendarArbitrary calendarArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaDateTimeConstraint constraint =
			this.constraintGenerator.generateDateTimeConstraint(context);
		if (constraint == null) {
			return calendarArbitrary;
		}

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
		JavaDateTimeConstraint constraint =
			this.constraintGenerator.generateDateTimeConstraint(context);
		if (constraint == null) {
			return dateArbitrary;
		}

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
		JavaDateTimeConstraint constraint =
			this.constraintGenerator.generateDateTimeConstraint(context);
		if (constraint == null) {
			return instantArbitrary;
		}

		LocalDateTime min = constraint.getMin();
		LocalDateTime max = constraint.getMax();

		if (min != null) {
			instantArbitrary = instantArbitrary.atTheEarliest(min.atZone(ZONE_ID).toInstant());
		}
		if (max != null) {
			instantArbitrary = instantArbitrary.atTheLatest(max.atZone(ZONE_ID).toInstant());
		}

		return instantArbitrary.map(it -> it.atZone(ZONE_ID).toInstant());
	}

	@Override
	public Arbitrary<LocalDate> localDates(
		LocalDateArbitrary localDateArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaDateTimeConstraint constraint = this.constraintGenerator.generateDateTimeConstraint(context);
		if (constraint == null) {
			return localDateArbitrary;
		}

		LocalDateTime min = constraint.getMin();
		LocalDateTime max = constraint.getMax();

		if (min != null) {
			localDateArbitrary = localDateArbitrary.atTheEarliest(min.toLocalDate());
		}
		if (max != null) {
			localDateArbitrary = localDateArbitrary.atTheLatest(max.toLocalDate());
		}

		return localDateArbitrary;
	}

	@Override
	public Arbitrary<LocalDateTime> localDateTimes(
		LocalDateTimeArbitrary localDateTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaDateTimeConstraint constraint =
			this.constraintGenerator.generateDateTimeConstraint(context);
		if (constraint == null) {
			return localDateTimeArbitrary;
		}

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
		JavaDateTimeConstraint constraint = this.constraintGenerator.generateDateTimeConstraint(context);
		if (constraint == null) {
			return localTimeArbitrary;
		}

		LocalDateTime min = constraint.getMin();
		LocalDateTime max = constraint.getMax();

		if (min != null) {
			localTimeArbitrary = localTimeArbitrary.atTheEarliest(min.toLocalTime());
		}
		if (max != null) {
			localTimeArbitrary = localTimeArbitrary.atTheLatest(max.toLocalTime());
		}

		return localTimeArbitrary;
	}

	@Override
	public Arbitrary<ZonedDateTime> zonedDateTimes(
		ZonedDateTimeArbitrary zonedDateTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaDateTimeConstraint constraint =
			this.constraintGenerator.generateDateTimeConstraint(context);
		if (constraint == null) {
			return zonedDateTimeArbitrary;
		}

		LocalDateTime min = constraint.getMin();
		LocalDateTime max = constraint.getMax();

		if (min != null) {
			zonedDateTimeArbitrary = zonedDateTimeArbitrary.atTheEarliest(min);
		}
		if (max != null) {
			zonedDateTimeArbitrary = zonedDateTimeArbitrary.atTheLatest(max);
		}

		return zonedDateTimeArbitrary.map(it -> it.withZoneSameLocal(ZONE_ID));
	}

	@Override
	public Arbitrary<MonthDay> monthDays(
		MonthDayArbitrary monthDayArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaDateTimeConstraint constraint = this.constraintGenerator.generateDateTimeConstraint(context);
		if (constraint == null) {
			return monthDayArbitrary;
		}

		LocalDateTime min = constraint.getMin();
		LocalDateTime max = constraint.getMax();

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
		JavaDateTimeConstraint constraint =
			this.constraintGenerator.generateDateTimeConstraint(context);
		if (constraint == null) {
			return offsetDateTimeArbitrary;
		}

		LocalDateTime min = constraint.getMin();
		LocalDateTime max = constraint.getMax();

		if (min != null) {
			offsetDateTimeArbitrary = offsetDateTimeArbitrary.atTheEarliest(min);
		}
		if (max != null) {
			offsetDateTimeArbitrary = offsetDateTimeArbitrary.atTheLatest(max);
		}

		offsetDateTimeArbitrary = offsetDateTimeArbitrary.offsetBetween(ZONE_OFFSET, ZONE_OFFSET);

		return offsetDateTimeArbitrary;
	}

	@Override
	public Arbitrary<OffsetTime> offsetTimes(
		OffsetTimeArbitrary offsetTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaDateTimeConstraint constraint = this.constraintGenerator.generateDateTimeConstraint(context);
		if (constraint == null) {
			return offsetTimeArbitrary;
		}

		LocalDateTime min = constraint.getMin();
		LocalDateTime max = constraint.getMax();

		if (min != null) {
			offsetTimeArbitrary = offsetTimeArbitrary.atTheEarliest(min.toLocalTime());
		}
		if (max != null) {
			offsetTimeArbitrary = offsetTimeArbitrary.atTheLatest(max.toLocalTime());
		}

		return offsetTimeArbitrary;
	}

	@Override
	public Arbitrary<Year> years(
		YearArbitrary yearArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaDateTimeConstraint constraint = this.constraintGenerator.generateDateTimeConstraint(context);
		if (constraint == null) {
			return yearArbitrary;
		}

		LocalDateTime min = constraint.getMin();
		LocalDateTime max = constraint.getMax();

		if (min != null) {
			yearArbitrary = yearArbitrary.between(min.plusYears(1).getYear(), Year.MAX_VALUE);
		}
		if (max != null) {
			yearArbitrary = yearArbitrary.between(Year.MIN_VALUE, max.minusYears(1).getYear());
		}

		return yearArbitrary;
	}

	@Override
	public Arbitrary<YearMonth> yearMonths(
		YearMonthArbitrary yearMonthArbitrary,
		ArbitraryGeneratorContext context
	) {
		JavaDateTimeConstraint constraint = this.constraintGenerator.generateDateTimeConstraint(context);
		if (constraint == null) {
			return yearMonthArbitrary;
		}

		LocalDateTime min = constraint.getMin();
		LocalDateTime max = constraint.getMax();

		if (min != null) {
			yearMonthArbitrary = yearMonthArbitrary.atTheEarliest(YearMonth.from(min.plusMonths(1)));
		}
		if (max != null) {
			yearMonthArbitrary = yearMonthArbitrary.atTheLatest(YearMonth.from(max.minusMonths(1)));
		}

		return yearMonthArbitrary;
	}
}
