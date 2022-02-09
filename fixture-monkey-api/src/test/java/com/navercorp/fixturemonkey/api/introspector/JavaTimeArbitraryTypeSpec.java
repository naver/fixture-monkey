package com.navercorp.fixturemonkey.api.introspector;

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

class JavaTimeArbitraryTypeSpec {
	private Calendar calendar;

	private Date date;

	private Instant instant;

	private LocalDate localDate;

	private LocalDateTime localDateTime;

	private LocalTime localTime;

	private ZonedDateTime zonedDateTime;

	private MonthDay monthDay;

	private OffsetDateTime offsetDateTime;

	private OffsetTime offsetTime;

	private Period period;

	private Duration duration;

	private Year year;

	private YearMonth yearMonth;

	private ZoneOffset zoneOffset;
}
