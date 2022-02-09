package com.navercorp.fixturemonkey.javax.validation.introspector;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;

class TimeIntrospectorSpec {
	private Instant instant;

	@Past
	private Instant instantPast;

	@PastOrPresent
	private Instant instantPastOrPresent;

	@Future
	private Instant instantFuture;

	@FutureOrPresent
	private Instant instantFutureOrPresent;

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

	private Duration duration;

	private Period period;

	private ZoneOffset zoneOffset;
}
