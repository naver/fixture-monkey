package com.navercorp.fixturemonkey.jakarta.validation.spec;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimeFutureIntrospectorSpec {
	@Future
	private Calendar calendarFuture;

	@FutureOrPresent
	private Calendar calendarFutureOrPresent;

	@Future
	private Date dateFuture;

	@FutureOrPresent
	private Date dateFutureOrPresent;

	@Future
	private Instant instantFuture;

	@FutureOrPresent
	private Instant instantFutureOrPresent;

	@Future
	private LocalDate localDateFuture;

	@FutureOrPresent
	private LocalDate localDateFutureOrPresent;

	@Future
	private LocalDateTime localDateTimeFuture;

	@FutureOrPresent
	private LocalDateTime localDateTimeFutureOrPresent;

	@Future
	private LocalTime localTimeFuture;

	@FutureOrPresent
	private LocalTime localTimeFutureOrPresent;

	@Future
	private ZonedDateTime zonedDateTimeFuture;

	@FutureOrPresent
	private ZonedDateTime zonedDateTimeFutureOrPresent;

	@Future
	private MonthDay monthDayFuture;

	@FutureOrPresent
	private MonthDay monthDayFutureOrPresent;

	@Future
	private OffsetDateTime offsetDateTimeFuture;

	@FutureOrPresent
	private OffsetDateTime offsetDateTimeFutureOrPresent;

	@Future
	private OffsetTime offsetTimeFuture;

	@FutureOrPresent
	private OffsetTime offsetTimeFutureOrPresent;

	@Future
	private Year yearFuture;

	@FutureOrPresent
	private Year yearFutureOrPresent;

	@Future
	private YearMonth yearMonthFuture;

	@FutureOrPresent
	private YearMonth yearMonthFutureOrPresent;
}
