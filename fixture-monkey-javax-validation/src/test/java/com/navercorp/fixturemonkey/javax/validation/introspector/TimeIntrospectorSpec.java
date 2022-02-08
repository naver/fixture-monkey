package com.navercorp.fixturemonkey.javax.validation.introspector;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;

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

	private LocalDate localDate;

	@Past
	private LocalDate localDatePast;

	@PastOrPresent
	private LocalDate localDatePastOrPresent;

	@Future
	private LocalDate localDateFuture;

	@FutureOrPresent
	private LocalDate localDateFutureOrPresent;

	private LocalTime localTime;

	@Past
	private LocalTime localTimePast;

	@PastOrPresent
	private LocalTime localTimePastOrPresent;

	@Future
	private LocalTime localTimeFuture;

	@FutureOrPresent
	private LocalTime localTimeFutureOrPresent;
}
