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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.Year;
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
import net.jqwik.time.api.arbitraries.PeriodArbitrary;
import net.jqwik.time.api.arbitraries.YearArbitrary;
import net.jqwik.time.api.arbitraries.YearMonthArbitrary;
import net.jqwik.time.api.arbitraries.ZoneOffsetArbitrary;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public interface TimeArbitraryIntrospector {
	Arbitrary<Calendar> calendars(CalendarArbitrary calendarArbitrary, ArbitraryIntrospectorContext context);

	Arbitrary<Date> dates(DateArbitrary dateArbitrary, ArbitraryIntrospectorContext context);

	Arbitrary<Instant> instants(InstantArbitrary instantArbitrary, ArbitraryIntrospectorContext context);

	Arbitrary<LocalDate> localDates(LocalDateArbitrary localDateArbitrary, ArbitraryIntrospectorContext context);

	Arbitrary<LocalDateTime> localDateTimes(
		LocalDateTimeArbitrary localDateTimeArbitrary,
		ArbitraryIntrospectorContext context
	);

	Arbitrary<LocalTime> localTimes(
		LocalTimeArbitrary localTimeArbitrary,
		ArbitraryIntrospectorContext context
	);

	Arbitrary<MonthDay> monthDays(
		MonthDayArbitrary monthDayArbitrary,
		ArbitraryIntrospectorContext context
	);

	Arbitrary<OffsetDateTime> offsetDateTimes(
		OffsetDateTimeArbitrary offsetDateTimeArbitrary,
		ArbitraryIntrospectorContext context
	);

	Arbitrary<OffsetTimeArbitrary> offsetTimes(
		OffsetTimeArbitrary offsetTimeArbitrary,
		ArbitraryIntrospectorContext context
	);

	Arbitrary<PeriodArbitrary> periods(PeriodArbitrary periodArbitrary, ArbitraryIntrospectorContext context);

	Arbitrary<Year> years(YearArbitrary yearArbitrary, ArbitraryIntrospectorContext context);

	Arbitrary<YearMonthArbitrary> yearMonths(
		YearMonthArbitrary yearMonthArbitrary,
		ArbitraryIntrospectorContext context
	);

	Arbitrary<ZoneOffsetArbitrary> zoneOffsets(
		ZoneOffsetArbitrary zoneOffsetArbitrary,
		ArbitraryIntrospectorContext context
	);
}
