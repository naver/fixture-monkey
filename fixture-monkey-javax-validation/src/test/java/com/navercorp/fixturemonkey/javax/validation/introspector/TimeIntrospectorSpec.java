package com.navercorp.fixturemonkey.javax.validation.introspector;

import java.time.Instant;
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
}
