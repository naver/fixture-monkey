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

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.arbitrary.JavaTimeArbitraryGeneratorSet;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeArbitraryResolver;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeTypeArbitraryGenerator;

/**
 * It would be moved into jqwik module in 0.7.0.
 */
@API(since = "0.6.3", status = Status.EXPERIMENTAL)
public final class JqwikJavaTimeArbitraryGeneratorSet implements JavaTimeArbitraryGeneratorSet {
	private final JavaTimeTypeArbitraryGenerator arbitraryGenerator;
	private final JavaTimeArbitraryResolver arbitraryResolver;

	public JqwikJavaTimeArbitraryGeneratorSet(
		JavaTimeTypeArbitraryGenerator arbitraryGenerator,
		JavaTimeArbitraryResolver arbitraryResolver
	) {
		this.arbitraryGenerator = arbitraryGenerator;
		this.arbitraryResolver = arbitraryResolver;
	}

	@Override
	public CombinableArbitrary<Calendar> calendars(ArbitraryGeneratorContext context) {
		return CombinableArbitrary.from(arbitraryResolver.calendars(arbitraryGenerator.calendars(), context));
	}

	@Override
	public CombinableArbitrary<Date> dates(ArbitraryGeneratorContext context) {
		return CombinableArbitrary.from(arbitraryResolver.dates(arbitraryGenerator.dates(), context));
	}

	@Override
	public CombinableArbitrary<Instant> instants(ArbitraryGeneratorContext context) {
		return CombinableArbitrary.from(arbitraryResolver.instants(arbitraryGenerator.instants(), context));
	}

	@Override
	public CombinableArbitrary<LocalDate> localDates(ArbitraryGeneratorContext context) {
		return CombinableArbitrary.from(arbitraryResolver.localDates(arbitraryGenerator.localDates(), context));
	}

	@Override
	public CombinableArbitrary<LocalDateTime> localDateTimes(ArbitraryGeneratorContext context) {
		return CombinableArbitrary.from(arbitraryResolver.localDateTimes(arbitraryGenerator.localDateTimes(), context));
	}

	@Override
	public CombinableArbitrary<LocalTime> localTimes(ArbitraryGeneratorContext context) {
		return CombinableArbitrary.from(arbitraryResolver.localTimes(arbitraryGenerator.localTimes(), context));
	}

	@Override
	public CombinableArbitrary<ZonedDateTime> zonedDateTimes(ArbitraryGeneratorContext context) {
		return CombinableArbitrary.from(arbitraryResolver.zonedDateTimes(arbitraryGenerator.zonedDateTimes(), context));
	}

	@Override
	public CombinableArbitrary<MonthDay> monthDays(ArbitraryGeneratorContext context) {
		return CombinableArbitrary.from(arbitraryResolver.monthDays(arbitraryGenerator.monthDays(), context));
	}

	@Override
	public CombinableArbitrary<OffsetDateTime> offsetDateTimes(ArbitraryGeneratorContext context) {
		return CombinableArbitrary.from(
			arbitraryResolver.offsetDateTimes(arbitraryGenerator.offsetDateTimes(), context)
		);
	}

	@Override
	public CombinableArbitrary<OffsetTime> offsetTimes(ArbitraryGeneratorContext context) {
		return CombinableArbitrary.from(arbitraryResolver.offsetTimes(arbitraryGenerator.offsetTimes(), context));
	}

	@Override
	public CombinableArbitrary<Period> periods(ArbitraryGeneratorContext context) {
		return CombinableArbitrary.from(arbitraryResolver.periods(arbitraryGenerator.periods(), context));
	}

	@Override
	public CombinableArbitrary<Duration> durations(ArbitraryGeneratorContext context) {
		return CombinableArbitrary.from(arbitraryResolver.durations(arbitraryGenerator.durations(), context));
	}

	@Override
	public CombinableArbitrary<Year> years(ArbitraryGeneratorContext context) {
		return CombinableArbitrary.from(arbitraryResolver.years(arbitraryGenerator.years(), context));
	}

	@Override
	public CombinableArbitrary<YearMonth> yearMonths(ArbitraryGeneratorContext context) {
		return CombinableArbitrary.from(arbitraryResolver.yearMonths(arbitraryGenerator.yearMonths(), context));
	}

	@Override
	public CombinableArbitrary<ZoneOffset> zoneOffsets(ArbitraryGeneratorContext context) {
		return CombinableArbitrary.from(arbitraryResolver.zoneOffsets(arbitraryGenerator.zoneOffsets(), context));
	}
}
