package com.navercorp.fixturemonkey.javax.validation.introspector;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;

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

import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorContext;
import com.navercorp.fixturemonkey.api.introspector.TimeArbitraryIntrospector;

public class JavaxValidationTimeArbitraryIntrospector implements TimeArbitraryIntrospector {
	@Override
	public Arbitrary<Calendar> calendars(
		CalendarArbitrary calendarArbitrary,
		ArbitraryIntrospectorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<Date> dates(
		DateArbitrary dateArbitrary,
		ArbitraryIntrospectorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<Instant> instants(
		InstantArbitrary instantArbitrary,
		ArbitraryIntrospectorContext context
	) {
		Instant now = Instant.now();
		Instant min = now.minus(365, ChronoUnit.DAYS);
		Instant max = now.plus(365, ChronoUnit.DAYS);

		if (context.findAnnotation(Past.class).isPresent()) {
			max = now.minus(1, ChronoUnit.SECONDS);
		} else if (context.findAnnotation(PastOrPresent.class).isPresent()) {
			max = now;
		}

		if (context.findAnnotation(Future.class).isPresent()) {
			min = now.plus(3, ChronoUnit.SECONDS);	// 3000 is buffer for future time
		} else if (context.findAnnotation(FutureOrPresent.class).isPresent()) {
			min = now.plus(2, ChronoUnit.SECONDS);	// 1000 is buffer for future time
		}

		return instantArbitrary
			.between(min, max);
	}

	@Override
	public Arbitrary<LocalDate> localDates(
		LocalDateArbitrary localDateArbitrary,
		ArbitraryIntrospectorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<LocalDateTime> localDateTimes(
		LocalDateTimeArbitrary localDateTimeArbitrary,
		ArbitraryIntrospectorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<LocalTime> localTimes(
		LocalTimeArbitrary localTimeArbitrary,
		ArbitraryIntrospectorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<MonthDay> monthDays(
		MonthDayArbitrary monthDayArbitrary,
		ArbitraryIntrospectorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<OffsetDateTime> offsetDateTimes(
		OffsetDateTimeArbitrary offsetDateTimeArbitrary,
		ArbitraryIntrospectorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<net.jqwik.time.api.arbitraries.OffsetTimeArbitrary> offsetTimes(
		OffsetTimeArbitrary offsetTimeArbitrary,
		ArbitraryIntrospectorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<net.jqwik.time.api.arbitraries.PeriodArbitrary> periods(
		PeriodArbitrary periodArbitrary,
		ArbitraryIntrospectorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<net.jqwik.time.api.arbitraries.DurationArbitrary> durations(
		DurationArbitrary durationArbitrary,
		ArbitraryIntrospectorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<Year> years(
		YearArbitrary yearArbitrary,
		ArbitraryIntrospectorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<net.jqwik.time.api.arbitraries.YearMonthArbitrary> yearMonths(
		YearMonthArbitrary yearMonthArbitrary,
		ArbitraryIntrospectorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}

	@Override
	public Arbitrary<net.jqwik.time.api.arbitraries.ZoneOffsetArbitrary> zoneOffsets(
		ZoneOffsetArbitrary zoneOffsetArbitrary,
		ArbitraryIntrospectorContext context
	) {
		throw new UnsupportedOperationException("Not implement yet.");
	}
}
