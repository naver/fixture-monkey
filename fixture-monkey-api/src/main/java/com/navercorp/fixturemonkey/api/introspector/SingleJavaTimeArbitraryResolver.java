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
import java.util.List;

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
import com.navercorp.fixturemonkey.api.matcher.Matcher;

@API(since = "0.5.6", status = Status.EXPERIMENTAL)
public final class SingleJavaTimeArbitraryResolver implements JavaTimeArbitraryResolver {
	private static final JavaTimeArbitraryResolver DEFAULT_NONE_JAVA_TIME_ARBITRARY_RESOLVER =
		new JavaTimeArbitraryResolver() {
		};

	private final List<JavaTimeArbitraryResolver> javaTimeArbitraryResolvers;

	public SingleJavaTimeArbitraryResolver(List<JavaTimeArbitraryResolver> javaTimeArbitraryResolvers) {
		this.javaTimeArbitraryResolvers = javaTimeArbitraryResolvers;
	}

	@Override
	public Arbitrary<Calendar> calendars(CalendarArbitrary calendarArbitrary, ArbitraryGeneratorContext context) {
		return selectResolver(context).calendars(calendarArbitrary, context);
	}

	@Override
	public Arbitrary<Date> dates(DateArbitrary dateArbitrary, ArbitraryGeneratorContext context) {
		return selectResolver(context).dates(dateArbitrary, context);
	}

	@Override
	public Arbitrary<Instant> instants(InstantArbitrary instantArbitrary, ArbitraryGeneratorContext context) {
		return selectResolver(context).instants(instantArbitrary, context);
	}

	@Override
	public Arbitrary<LocalDate> localDates(LocalDateArbitrary localDateArbitrary, ArbitraryGeneratorContext context) {
		return selectResolver(context).localDates(localDateArbitrary, context);
	}

	@Override
	public Arbitrary<LocalDateTime> localDateTimes(
		LocalDateTimeArbitrary localDateTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		return selectResolver(context).localDateTimes(localDateTimeArbitrary, context);
	}

	@Override
	public Arbitrary<LocalTime> localTimes(LocalTimeArbitrary localTimeArbitrary, ArbitraryGeneratorContext context) {
		return selectResolver(context).localTimes(localTimeArbitrary, context);
	}

	@Override
	public Arbitrary<ZonedDateTime> zonedDateTimes(
		ZonedDateTimeArbitrary zonedDateTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		return selectResolver(context).zonedDateTimes(zonedDateTimeArbitrary, context);
	}

	@Override
	public Arbitrary<MonthDay> monthDays(MonthDayArbitrary monthDayArbitrary, ArbitraryGeneratorContext context) {
		return selectResolver(context).monthDays(monthDayArbitrary, context);
	}

	@Override
	public Arbitrary<OffsetDateTime> offsetDateTimes(OffsetDateTimeArbitrary offsetDateTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		return selectResolver(context).offsetDateTimes(offsetDateTimeArbitrary, context);
	}

	@Override
	public Arbitrary<OffsetTime> offsetTimes(
		OffsetTimeArbitrary offsetTimeArbitrary,
		ArbitraryGeneratorContext context
	) {
		return selectResolver(context).offsetTimes(offsetTimeArbitrary, context);
	}

	@Override
	public Arbitrary<Period> periods(PeriodArbitrary periodArbitrary, ArbitraryGeneratorContext context) {
		return selectResolver(context).periods(periodArbitrary, context);
	}

	@Override
	public Arbitrary<Duration> durations(DurationArbitrary durationArbitrary, ArbitraryGeneratorContext context) {
		return selectResolver(context).durations(durationArbitrary, context);
	}

	@Override
	public Arbitrary<Year> years(YearArbitrary yearArbitrary, ArbitraryGeneratorContext context) {
		return selectResolver(context).years(yearArbitrary, context);
	}

	@Override
	public Arbitrary<YearMonth> yearMonths(YearMonthArbitrary yearMonthArbitrary, ArbitraryGeneratorContext context) {
		return selectResolver(context).yearMonths(yearMonthArbitrary, context);
	}

	@Override
	public Arbitrary<ZoneOffset> zoneOffsets(
		ZoneOffsetArbitrary zoneOffsetArbitrary,
		ArbitraryGeneratorContext context
	) {
		return selectResolver(context).zoneOffsets(zoneOffsetArbitrary, context);
	}

	private JavaTimeArbitraryResolver selectResolver(
		ArbitraryGeneratorContext context
	) {
		for (JavaTimeArbitraryResolver resolver : this.javaTimeArbitraryResolvers) {
			if (resolver instanceof Matcher) {
				if (((Matcher)resolver).match(context.getResolvedProperty())) {
					return resolver;
				}
			}
		}
		return javaTimeArbitraryResolvers.isEmpty()
			? DEFAULT_NONE_JAVA_TIME_ARBITRARY_RESOLVER : javaTimeArbitraryResolvers.get(0);
	}
}
