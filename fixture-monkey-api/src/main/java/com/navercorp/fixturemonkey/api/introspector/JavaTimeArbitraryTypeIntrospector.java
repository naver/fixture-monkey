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

import java.lang.reflect.Type;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.matcher.TypeMatcher;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class JavaTimeArbitraryTypeIntrospector implements ArbitraryTypeIntrospector, TypeMatcher {
	private final Map<Class<?>, Function<ArbitraryGeneratorContext, ArbitraryIntrospectorResult>> introspector;

	public JavaTimeArbitraryTypeIntrospector() {
		this(
			new IntrospectorTimeArbitraryGenerator() {
			},
			new TimeArbitraryIntrospector() {
			}
		);
	}

	public JavaTimeArbitraryTypeIntrospector(
		IntrospectorTimeArbitraryGenerator introspectorTimeArbitraryGenerator,
		TimeArbitraryIntrospector timeArbitraryIntrospector
	) {
		this.introspector = introspectors(introspectorTimeArbitraryGenerator, timeArbitraryIntrospector);
	}

	@Override
	public boolean match(Type type) {
		Class<?> actualType = Types.getActualType(type);
		return this.introspector.containsKey(actualType);
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		Class<?> type = Types.getActualType(context.getType());
		return this.introspector.getOrDefault(
				type,
				ctx -> ArbitraryIntrospectorResult.EMPTY
			)
			.apply(context);
	}

	private static Map<Class<?>, Function<ArbitraryGeneratorContext, ArbitraryIntrospectorResult>> introspectors(
		IntrospectorTimeArbitraryGenerator introspectorTimeArbitraryGenerator,
		TimeArbitraryIntrospector timeArbitraryIntrospector
	) {
		Map<Class<?>, Function<ArbitraryGeneratorContext, ArbitraryIntrospectorResult>> introspector = new HashMap<>();

		introspector.put(
			Calendar.class,
			ctx -> new ArbitraryIntrospectorResult(
				timeArbitraryIntrospector.calendars(
					introspectorTimeArbitraryGenerator.calendars(),
					ctx
				)
			)
		);

		introspector.put(
			Date.class,
			ctx -> new ArbitraryIntrospectorResult(
				timeArbitraryIntrospector.dates(
					introspectorTimeArbitraryGenerator.dates(),
					ctx
				)
			)
		);

		introspector.put(
			Instant.class,
			ctx -> new ArbitraryIntrospectorResult(
				timeArbitraryIntrospector.instants(
					introspectorTimeArbitraryGenerator.instants(),
					ctx
				)
			)
		);

		introspector.put(
			LocalDate.class,
			ctx -> new ArbitraryIntrospectorResult(
				timeArbitraryIntrospector.localDates(
					introspectorTimeArbitraryGenerator.localDates(),
					ctx
				)
			)
		);

		introspector.put(
			LocalDateTime.class,
			ctx -> new ArbitraryIntrospectorResult(
				timeArbitraryIntrospector.localDateTimes(
					introspectorTimeArbitraryGenerator.localDateTimes(),
					ctx
				)
			)
		);

		introspector.put(
			LocalTime.class,
			ctx -> new ArbitraryIntrospectorResult(
				timeArbitraryIntrospector.localTimes(
					introspectorTimeArbitraryGenerator.localTimes(),
					ctx
				)
			)
		);

		introspector.put(
			ZonedDateTime.class,
			ctx -> new ArbitraryIntrospectorResult(
				timeArbitraryIntrospector.zonedDateTimes(
					introspectorTimeArbitraryGenerator.zonedDateTimes(),
					ctx
				)
			)
		);

		introspector.put(
			MonthDay.class,
			ctx -> new ArbitraryIntrospectorResult(
				timeArbitraryIntrospector.monthDays(
					introspectorTimeArbitraryGenerator.monthDays(),
					ctx
				)
			)
		);

		introspector.put(
			OffsetDateTime.class,
			ctx -> new ArbitraryIntrospectorResult(
				timeArbitraryIntrospector.offsetDateTimes(
					introspectorTimeArbitraryGenerator.offsetDateTimes(),
					ctx
				)
			)
		);

		introspector.put(
			OffsetTime.class,
			ctx -> new ArbitraryIntrospectorResult(
				timeArbitraryIntrospector.offsetTimes(
					introspectorTimeArbitraryGenerator.offsetTimes(),
					ctx
				)
			)
		);

		introspector.put(
			Period.class,
			ctx -> new ArbitraryIntrospectorResult(
				timeArbitraryIntrospector.periods(
					introspectorTimeArbitraryGenerator.periods(),
					ctx
				)
			)
		);

		introspector.put(
			Duration.class,
			ctx -> new ArbitraryIntrospectorResult(
				timeArbitraryIntrospector.durations(
					introspectorTimeArbitraryGenerator.durations(),
					ctx
				)
			)
		);

		introspector.put(
			Year.class,
			ctx -> new ArbitraryIntrospectorResult(
				timeArbitraryIntrospector.years(
					introspectorTimeArbitraryGenerator.years(),
					ctx
				)
			)
		);

		introspector.put(
			YearMonth.class,
			ctx -> new ArbitraryIntrospectorResult(
				timeArbitraryIntrospector.yearMonths(
					introspectorTimeArbitraryGenerator.yearMonths(),
					ctx
				)
			)
		);

		introspector.put(
			ZoneOffset.class,
			ctx -> new ArbitraryIntrospectorResult(
				timeArbitraryIntrospector.zoneOffsets(
					introspectorTimeArbitraryGenerator.zoneOffsets(),
					ctx
				)
			)
		);

		return Collections.unmodifiableMap(introspector);
	}
}
