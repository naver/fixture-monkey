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

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public interface IntrospectorTimeArbitraryGenerator {

	default CalendarArbitrary calendars() {
		return Dates.datesAsCalendar();
	}

	default DateArbitrary dates() {
		return Dates.datesAsDate();
	}

	default InstantArbitrary instants() {
		return DateTimes.instants();
	}

	default LocalDateArbitrary localDates() {
		return Dates.dates();
	}

	default LocalDateTimeArbitrary localDateTimes() {
		return DateTimes.dateTimes();
	}

	default LocalTimeArbitrary localTimes() {
		return Times.times();
	}

	default MonthDayArbitrary monthDays() {
		return Dates.monthDays();
	}

	default OffsetDateTimeArbitrary offsetDateTimes() {
		return DateTimes.offsetDateTimes();
	}

	default OffsetTimeArbitrary offsetTimes() {
		return Times.offsetTimes();
	}

	default PeriodArbitrary periods() {
		return Dates.periods();
	}

	default DurationArbitrary durations() {
		return Times.durations();
	}

	default YearArbitrary years() {
		return Dates.years();
	}

	default YearMonthArbitrary yearMonths() {
		return Dates.yearMonths();
	}

	default ZoneOffsetArbitrary zonOffsets() {
		return Times.zoneOffsets();
	}
}
