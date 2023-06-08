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

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimePastIntrospectorSpec {
	@Past
	private Calendar calendarPast;

	@PastOrPresent
	private Calendar calendarPastOrPresent;

	@Past
	private Date datePast;

	@PastOrPresent
	private Date datePastOrPresent;

	@Past
	private Instant instantPast;

	@PastOrPresent
	private Instant instantPastOrPresent;

	@Past
	private LocalDate localDatePast;

	@PastOrPresent
	private LocalDate localDatePastOrPresent;

	@Past
	private LocalDateTime localDateTimePast;

	@PastOrPresent
	private LocalDateTime localDateTimePastOrPresent;

	@Past
	private LocalTime localTimePast;

	@PastOrPresent
	private LocalTime localTimePastOrPresent;

	@Past
	private ZonedDateTime zonedDateTimePast;

	@PastOrPresent
	private ZonedDateTime zonedDateTimePastOrPresent;

	@Past
	private MonthDay monthDayPast;

	@PastOrPresent
	private MonthDay monthDayPastOrPresent;

	@Past
	private OffsetDateTime offsetDateTimePast;

	@PastOrPresent
	private OffsetDateTime offsetDateTimePastOrPresent;

	@Past
	private OffsetTime offsetTimePast;

	@PastOrPresent
	private OffsetTime offsetTimePastOrPresent;

	@Past
	private Year yearPast;

	@PastOrPresent
	private Year yearPastOrPresent;

	@Past
	private YearMonth yearMonthPast;

	@PastOrPresent
	private YearMonth yearMonthPastOrPresent;
}
