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

package com.navercorp.fixturemonkey.api.arbitrary;

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
import java.util.Date;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;

@API(since = "0.6.3", status = Status.EXPERIMENTAL)
public interface JavaTimeArbitraryGeneratorSet {
	CombinableArbitrary<Calendar> calendars(ArbitraryGeneratorContext context);

	CombinableArbitrary<Date> dates(ArbitraryGeneratorContext context);

	CombinableArbitrary<Instant> instants(ArbitraryGeneratorContext context);

	CombinableArbitrary<LocalDate> localDates(ArbitraryGeneratorContext context);

	CombinableArbitrary<LocalDateTime> localDateTimes(ArbitraryGeneratorContext context);

	CombinableArbitrary<LocalTime> localTimes(ArbitraryGeneratorContext context);

	CombinableArbitrary<ZonedDateTime> zonedDateTimes(ArbitraryGeneratorContext context);

	CombinableArbitrary<MonthDay> monthDays(ArbitraryGeneratorContext context);

	CombinableArbitrary<OffsetDateTime> offsetDateTimes(ArbitraryGeneratorContext context);

	CombinableArbitrary<OffsetTime> offsetTimes(ArbitraryGeneratorContext context);

	CombinableArbitrary<Period> periods(ArbitraryGeneratorContext context);

	CombinableArbitrary<Duration> durations(ArbitraryGeneratorContext context);

	CombinableArbitrary<Year> years(ArbitraryGeneratorContext context);

	CombinableArbitrary<YearMonth> yearMonths(ArbitraryGeneratorContext context);

	CombinableArbitrary<ZoneOffset> zoneOffsets(ArbitraryGeneratorContext context);

	CombinableArbitrary<ZoneId> zoneIds(ArbitraryGeneratorContext context);
}
