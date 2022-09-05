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

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;

class TimeIntrospectorSpec {
	private Calendar calendar;

	@Past
	private Calendar calendarPast;

	@PastOrPresent
	private Calendar calendarPastOrPresent;

	@Future
	private Calendar calendarFuture;

	@FutureOrPresent
	private Calendar calendarFutureOrPresent;

	private Date date;

	@Past
	private Date datePast;

	@PastOrPresent
	private Date datePastOrPresent;

	@Future
	private Date dateFuture;

	@FutureOrPresent
	private Date dateFutureOrPresent;

	private Instant instant;

	@Past
	private Instant instantPast;

	@PastOrPresent
	private Instant instantPastOrPresent;

	@Future
	private Instant instantFuture;

	@FutureOrPresent
	private Instant instantFutureOrPresent;

	private LocalDate localDate;

	@Past
	private LocalDate localDatePast;

	@PastOrPresent
	private LocalDate localDatePastOrPresent;

	@Future
	private LocalDate localDateFuture;

	@FutureOrPresent
	private LocalDate localDateFutureOrPresent;

	private LocalDateTime localDateTime;

	@Past
	private LocalDateTime localDateTimePast;

	@PastOrPresent
	private LocalDateTime localDateTimePastOrPresent;

	@Future
	private LocalDateTime localDateTimeFuture;

	@FutureOrPresent
	private LocalDateTime localDateTimeFutureOrPresent;

	private LocalTime localTime;

	@Past
	private LocalTime localTimePast;

	@PastOrPresent
	private LocalTime localTimePastOrPresent;

	@Future
	private LocalTime localTimeFuture;

	@FutureOrPresent
	private LocalTime localTimeFutureOrPresent;

	private ZonedDateTime zonedDateTime;

	@Past
	private ZonedDateTime zonedDateTimePast;

	@PastOrPresent
	private ZonedDateTime zonedDateTimePastOrPresent;

	@Future
	private ZonedDateTime zonedDateTimeFuture;

	@FutureOrPresent
	private ZonedDateTime zonedDateTimeFutureOrPresent;

	private MonthDay monthDay;

	@Past
	private MonthDay monthDayPast;

	@PastOrPresent
	private MonthDay monthDayPastOrPresent;

	@Future
	private MonthDay monthDayFuture;

	@FutureOrPresent
	private MonthDay monthDayFutureOrPresent;

	private OffsetDateTime offsetDateTime;

	@Past
	private OffsetDateTime offsetDateTimePast;

	@PastOrPresent
	private OffsetDateTime offsetDateTimePastOrPresent;

	@Future
	private OffsetDateTime offsetDateTimeFuture;

	@FutureOrPresent
	private OffsetDateTime offsetDateTimeFutureOrPresent;

	private OffsetTime offsetTime;

	@Past
	private OffsetTime offsetTimePast;

	@PastOrPresent
	private OffsetTime offsetTimePastOrPresent;

	@Future
	private OffsetTime offsetTimeFuture;

	@FutureOrPresent
	private OffsetTime offsetTimeFutureOrPresent;

	private Year year;

	@Past
	private Year yearPast;

	@PastOrPresent
	private Year yearPastOrPresent;

	@Future
	private Year yearFuture;

	@FutureOrPresent
	private Year yearFutureOrPresent;

	private YearMonth yearMonth;

	@Past
	private YearMonth yearMonthPast;

	@PastOrPresent
	private YearMonth yearMonthPastOrPresent;

	@Future
	private YearMonth yearMonthFuture;

	@FutureOrPresent
	private YearMonth yearMonthFutureOrPresent;

	private Period period;

	private Duration duration;

	private ZoneOffset zoneOffset;
}
