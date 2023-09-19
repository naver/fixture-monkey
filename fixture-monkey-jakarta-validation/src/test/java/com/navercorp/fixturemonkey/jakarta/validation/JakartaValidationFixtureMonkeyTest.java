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

package com.navercorp.fixturemonkey.jakarta.validation;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.exception.FilterMissException;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.navercorp.fixturemonkey.jakarta.validation.spec.BigDecimalIntrospectorSpec;
import com.navercorp.fixturemonkey.jakarta.validation.spec.BigIntegerIntrospectorSpec;
import com.navercorp.fixturemonkey.jakarta.validation.spec.BooleanIntrospectorSpec;
import com.navercorp.fixturemonkey.jakarta.validation.spec.ByteIntrospectorSpec;
import com.navercorp.fixturemonkey.jakarta.validation.spec.CharacterIntrospectorSpec;
import com.navercorp.fixturemonkey.jakarta.validation.spec.ContainerAnnotationIntrospectorSpec;
import com.navercorp.fixturemonkey.jakarta.validation.spec.DoubleIntrospectorSpec;
import com.navercorp.fixturemonkey.jakarta.validation.spec.FloatIntrospectorSpec;
import com.navercorp.fixturemonkey.jakarta.validation.spec.IntIntrospectorSpec;
import com.navercorp.fixturemonkey.jakarta.validation.spec.LongIntrospectorSpec;
import com.navercorp.fixturemonkey.jakarta.validation.spec.NullAnnotationIntrospectorSpec;
import com.navercorp.fixturemonkey.jakarta.validation.spec.ShortIntrospectorSpec;
import com.navercorp.fixturemonkey.jakarta.validation.spec.StringIntrospectorSpec;
import com.navercorp.fixturemonkey.jakarta.validation.spec.TimeFutureIntrospectorSpec;
import com.navercorp.fixturemonkey.jakarta.validation.spec.TimePastIntrospectorSpec;

class JakartaValidationFixtureMonkeyTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.plugin(new JakartaValidationPlugin())
		.defaultNotNull(true)
		.build();

	private static final ZoneId ZONED_ID = ZoneId.systemDefault();

	@Property(tries = 100)
	void sampleBigDecimal() {
		BigDecimalIntrospectorSpec actual = SUT.giveMeOne(BigDecimalIntrospectorSpec.class);

		then(actual.getDigitsValue()).isBetween(BigDecimal.valueOf(-999), BigDecimal.valueOf(999));
		then(actual.getDecimalMin()).isGreaterThanOrEqualTo(BigDecimal.valueOf(100.1));
		then(actual.getDecimalMinExclusive()).isGreaterThan(BigDecimal.valueOf(100.1));
		then(actual.getDecimalMax()).isLessThanOrEqualTo(BigDecimal.valueOf(100.1));
		then(actual.getDecimalMaxExclusive()).isLessThan(BigDecimal.valueOf(100.1));
		then(actual.getNegative()).isNegative();
		then(actual.getNegativeOrZero()).isLessThanOrEqualTo(BigDecimal.ZERO);
		then(actual.getPositive()).isPositive();
		then(actual.getPositiveOrZero()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
	}

	@Property(tries = 100)
	void sampleBigInteger() {
		BigIntegerIntrospectorSpec actual = SUT.giveMeOne(BigIntegerIntrospectorSpec.class);

		then(actual.getDigitsValue()).isBetween(BigInteger.valueOf(-999), BigInteger.valueOf(999));
		then(actual.getDecimalMin()).isGreaterThanOrEqualTo(BigInteger.valueOf(100));
		then(actual.getDecimalMinExclusive()).isGreaterThan(BigInteger.valueOf(100));
		then(actual.getDecimalMax()).isLessThanOrEqualTo(BigInteger.valueOf(100));
		then(actual.getDecimalMaxExclusive()).isLessThan(BigInteger.valueOf(100));
		then(actual.getNegative()).isNegative();
		then(actual.getNegativeOrZero()).isLessThanOrEqualTo(BigInteger.ZERO);
		then(actual.getPositive()).isPositive();
		then(actual.getPositiveOrZero()).isGreaterThanOrEqualTo(BigInteger.ZERO);
	}

	@Property(tries = 100)
	void sampleBoolean() {
		BooleanIntrospectorSpec actual = SUT.giveMeOne(BooleanIntrospectorSpec.class);

		then(actual.isBoolValue()).isIn(true, false);
		then(actual.isAssertFalse()).isFalse();
		then(actual.isAssertTrue()).isTrue();
	}

	@Property(tries = 100)
	void sampleByte() {
		ByteIntrospectorSpec actual = SUT.giveMeOne(ByteIntrospectorSpec.class);

		then(actual.getByteValue()).isBetween(Byte.MIN_VALUE, Byte.MAX_VALUE);
		then(actual.getDigitsValue()).isBetween((byte)-99, (byte)99);
		then(actual.getDecimalMin()).isGreaterThanOrEqualTo((byte)100);
		then(actual.getDecimalMinExclusive()).isGreaterThan((byte)100);
		then(actual.getDecimalMax()).isLessThanOrEqualTo((byte)100);
		then(actual.getDecimalMaxExclusive()).isLessThan((byte)100);
		then(actual.getNegative()).isNegative();
		then(actual.getNegativeOrZero()).isLessThanOrEqualTo((byte)0);
		then(actual.getPositive()).isPositive();
		then(actual.getPositiveOrZero()).isGreaterThanOrEqualTo((byte)0);
	}

	@Property(tries = 100)
	void sampleCharacter() {
		CharacterIntrospectorSpec actual = SUT.giveMeOne(CharacterIntrospectorSpec.class);

		then(actual.getCharacter()).isBetween(Character.MIN_VALUE, Character.MAX_VALUE);
	}

	@Property(tries = 100)
	void sampleDouble() {
		DoubleIntrospectorSpec actual = SUT.giveMeOne(DoubleIntrospectorSpec.class);

		then(actual.getDoubleValue()).isBetween(-Double.MAX_VALUE, Double.MAX_VALUE);
		then(actual.getDigitsValue()).isBetween(-999d, 999d);
		then(actual.getDecimalMin()).isGreaterThanOrEqualTo(100.1d);
		then(actual.getDecimalMinExclusive()).isGreaterThan(100.1d);
		then(actual.getDecimalMax()).isLessThanOrEqualTo(100.1d);
		then(actual.getDecimalMaxExclusive()).isLessThanOrEqualTo(100.1d);
		then(actual.getNegative()).isNegative();
		then(actual.getNegativeOrZero()).isLessThanOrEqualTo(0);
		then(actual.getPositive()).isPositive();
		then(actual.getPositiveOrZero()).isGreaterThanOrEqualTo(0);
	}

	@Property(tries = 100)
	void sampleFloat() {
		FloatIntrospectorSpec actual = SUT.giveMeOne(FloatIntrospectorSpec.class);

		then(actual.getFloatValue()).isBetween(-Float.MAX_VALUE, Float.MAX_VALUE);
		then(actual.getDigitsValue()).isBetween(-999f, 999f);
		then(actual.getDecimalMin()).isGreaterThanOrEqualTo(100.1f);
		then(actual.getDecimalMinExclusive()).isGreaterThan(100.1f);
		then(actual.getDecimalMax()).isLessThanOrEqualTo(100.1f);
		then(actual.getDecimalMaxExclusive()).isLessThan(1001.f);
		then(actual.getNegative()).isNegative();
		then(actual.getNegativeOrZero()).isLessThanOrEqualTo(0);
		then(actual.getPositive()).isPositive();
		then(actual.getPositiveOrZero()).isGreaterThanOrEqualTo(0);
	}

	@Property(tries = 100)
	void sampleInt() {
		IntIntrospectorSpec actual = SUT.giveMeOne(IntIntrospectorSpec.class);

		then(actual.getIntValue()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
		then(actual.getDigitsValue()).isBetween(-999, 999);
		then(actual.getDecimalMin()).isGreaterThanOrEqualTo(100);
		then(actual.getDecimalMinExclusive()).isGreaterThan(100);
		then(actual.getDecimalMax()).isLessThanOrEqualTo(100);
		then(actual.getDecimalMaxExclusive()).isLessThan(100);
		then(actual.getNegative()).isNegative();
		then(actual.getNegativeOrZero()).isLessThanOrEqualTo(0);
		then(actual.getPositive()).isPositive();
		then(actual.getPositiveOrZero()).isGreaterThanOrEqualTo(0);
	}

	@Property(tries = 100)
	void sampleLong() {
		LongIntrospectorSpec actual = SUT.giveMeOne(LongIntrospectorSpec.class);

		then(actual.getLongValue()).isBetween(Long.MIN_VALUE, Long.MAX_VALUE);
		then(actual.getDigitsValue()).isBetween(-999L, 999L);
		then(actual.getDecimalMin()).isGreaterThanOrEqualTo(100);
		then(actual.getDecimalMinExclusive()).isGreaterThan(100);
		then(actual.getDecimalMax()).isLessThanOrEqualTo(100);
		then(actual.getDecimalMaxExclusive()).isLessThan(100);
		then(actual.getNegative()).isNegative();
		then(actual.getNegativeOrZero()).isLessThanOrEqualTo(0);
		then(actual.getPositive()).isPositive();
		then(actual.getPositiveOrZero()).isGreaterThanOrEqualTo(0);
	}

	@Property(tries = 100)
	void sampleShort() {
		ShortIntrospectorSpec actual = SUT.giveMeOne(ShortIntrospectorSpec.class);

		then(actual.getShortValue()).isBetween(Short.MIN_VALUE, Short.MAX_VALUE);
		then(actual.getDigitsValue()).isBetween((short)-999, (short)999);
		then(actual.getDecimalMin()).isGreaterThanOrEqualTo((short)100);
		then(actual.getDecimalMinExclusive()).isGreaterThan((short)100);
		then(actual.getDecimalMax()).isLessThanOrEqualTo((short)100);
		then(actual.getDecimalMaxExclusive()).isLessThan((short)100);
		then(actual.getNegative()).isNegative();
		then(actual.getNegativeOrZero()).isLessThanOrEqualTo((short)0);
		then(actual.getPositive()).isPositive();
		then(actual.getPositiveOrZero()).isGreaterThanOrEqualTo((short)0);
	}

	@Property(tries = 100)
	void sampleString() {
		StringIntrospectorSpec actual = SUT.giveMeOne(StringIntrospectorSpec.class);

		then(actual.getNotBlank()).isNotBlank();
		then(actual.getNotEmpty()).isNotEmpty();
		then(actual.getSize()).hasSizeBetween(5, 10);
		then(actual.getDigits()).hasSizeLessThanOrEqualTo(10);
		thenNoException().isThrownBy(() -> Long.parseLong(actual.getDigits()));
		Pattern pattern = Pattern.compile("[e-o]");
		then(pattern.asPredicate().test(actual.getPattern())).isTrue();
		for (int i = 0; i < actual.getPattern().length(); i++) {
			char ch = actual.getPattern().charAt(i);
			then(ch).isBetween('e', 'o');
		}
		then(actual.getEmail()).containsOnlyOnce("@");
		Pattern controlCharacters = Pattern.compile("[\u0000-\u001f\u007f]");
		then(actual.getStr()).doesNotMatch(controlCharacters);
	}

	@Property(tries = 100)
	void samplePastCalendar() {
		TimePastIntrospectorSpec actual = SUT.giveMeOne(TimePastIntrospectorSpec.class);

		Calendar now = Calendar.getInstance();
		then(actual.getCalendarPast().toInstant().toEpochMilli()).isLessThan(now.toInstant().toEpochMilli());
		then(actual.getCalendarPastOrPresent().getTimeInMillis()).isLessThanOrEqualTo(now.toInstant().toEpochMilli());
	}

	@Property(tries = 100)
	void sampleFutureCalendar() {
		Calendar now = Calendar.getInstance();
		TimeFutureIntrospectorSpec actual = SUT.giveMeOne(TimeFutureIntrospectorSpec.class);

		then(actual.getCalendarFuture().getTimeInMillis()).isGreaterThan(now.toInstant().toEpochMilli());
		then(actual.getCalendarFutureOrPresent().getTimeInMillis())
			.isGreaterThanOrEqualTo(now.toInstant().toEpochMilli());
	}

	@Property(tries = 100)
	void samplePastDate() {
		TimePastIntrospectorSpec actual = SUT.giveMeOne(TimePastIntrospectorSpec.class);

		Date now = new Date();
		then(actual.getDatePast().getTime()).isLessThan(now.getTime());
		then(actual.getDatePastOrPresent().getTime()).isLessThanOrEqualTo(now.getTime());
	}

	@Property(tries = 100)
	void sampleFutureDate() {
		Date now = new Date();
		TimeFutureIntrospectorSpec actual = SUT.giveMeOne(TimeFutureIntrospectorSpec.class);

		then(actual.getDateFuture().getTime()).isGreaterThan(now.getTime());
		then(actual.getDateFutureOrPresent().getTime()).isGreaterThanOrEqualTo(now.getTime());
	}

	@Property(tries = 100)
	void samplePastInstant() {
		TimePastIntrospectorSpec actual = SUT.giveMeOne(TimePastIntrospectorSpec.class);

		Instant now = Instant.now();
		then(actual.getInstantPast()).isBefore(now);
		then(actual.getInstantPastOrPresent()).isBeforeOrEqualTo(now);
	}

	@Property(tries = 100)
	void sampleFutureInstant() {
		Instant now = Instant.now();
		TimeFutureIntrospectorSpec actual = SUT.giveMeOne(TimeFutureIntrospectorSpec.class);

		then(actual.getInstantFuture()).isAfter(now);
		then(actual.getInstantFutureOrPresent()).isAfterOrEqualTo(now);
	}

	@Property(tries = 100)
	void samplePastLocalDate() {
		TimePastIntrospectorSpec actual = SUT.giveMeOne(TimePastIntrospectorSpec.class);

		LocalDate now = LocalDate.now();
		then(actual.getLocalDatePast()).isBefore(now);
		then(actual.getLocalDatePastOrPresent()).isBeforeOrEqualTo(now);
	}

	@Property(tries = 100)
	void sampleFutureLocalDate() {
		LocalDate now = LocalDate.now();
		TimeFutureIntrospectorSpec actual = SUT.giveMeOne(TimeFutureIntrospectorSpec.class);

		then(actual.getLocalDateFuture()).isAfter(now);
		then(actual.getLocalDateFutureOrPresent()).isAfterOrEqualTo(now);
	}

	@Property(tries = 100)
	void samplePastLocalDateTime() {
		TimePastIntrospectorSpec actual = SUT.giveMeOne(TimePastIntrospectorSpec.class);

		LocalDateTime now = LocalDateTime.now();
		then(actual.getLocalDateTimePast()).isBefore(now);
		then(actual.getLocalDateTimePastOrPresent()).isBeforeOrEqualTo(now);
	}

	@Property(tries = 100)
	void sampleFutureLocalDateTime() {
		LocalDateTime now = LocalDateTime.now();
		TimeFutureIntrospectorSpec actual = SUT.giveMeOne(TimeFutureIntrospectorSpec.class);

		then(actual.getLocalDateTimeFuture()).isAfter(now);
		then(actual.getLocalDateTimeFutureOrPresent()).isAfterOrEqualTo(now);
	}

	@Property(tries = 100)
	void samplePastLocalTime() {
		TimePastIntrospectorSpec actual = SUT.giveMeOne(TimePastIntrospectorSpec.class);

		LocalTime now = LocalTime.now();
		then(actual.getLocalTimePast()).isBefore(now);
		then(actual.getLocalTimePastOrPresent()).isBeforeOrEqualTo(now);
	}

	@Property(tries = 100)
	void sampleFutureLocalTime() {
		LocalTime now = LocalTime.now();
		TimeFutureIntrospectorSpec actual = SUT.giveMeOne(TimeFutureIntrospectorSpec.class);

		then(actual.getLocalTimeFuture()).isAfter(now);
		then(actual.getLocalTimeFutureOrPresent()).isAfterOrEqualTo(now);
	}

	@Property(tries = 100)
	void samplePastZonedDateTime() {
		TimePastIntrospectorSpec actual = SUT.giveMeOne(TimePastIntrospectorSpec.class);

		ZonedDateTime now = ZonedDateTime.now().withZoneSameLocal(ZONED_ID);
		then(actual.getZonedDateTimePast().withZoneSameLocal(ZONED_ID)).isBefore(now);
		then(actual.getZonedDateTimePastOrPresent().withZoneSameLocal(ZONED_ID)).isBeforeOrEqualTo(now);
	}

	@Property(tries = 100)
	void sampleFutureZonedDateTime() {
		ZonedDateTime now = ZonedDateTime.now().withZoneSameLocal(ZONED_ID);
		TimeFutureIntrospectorSpec actual = SUT.giveMeOne(TimeFutureIntrospectorSpec.class);

		then(actual.getZonedDateTimeFuture().withZoneSameLocal(ZONED_ID)).isAfter(now);
		then(actual.getZonedDateTimeFutureOrPresent().withZoneSameLocal(ZONED_ID)).isAfterOrEqualTo(now);
	}

	@Property(tries = 100)
	void samplePastMonthDay() {
		TimePastIntrospectorSpec actual = SUT.giveMeOne(TimePastIntrospectorSpec.class);

		MonthDay now = MonthDay.now();
		then(actual.getMonthDayPast()).isLessThan(now);
		then(actual.getMonthDayPastOrPresent()).isLessThanOrEqualTo(now);
	}

	@Property(tries = 100)
	void sampleFutureMonthDay() {
		MonthDay now = MonthDay.now();
		TimeFutureIntrospectorSpec actual = SUT.giveMeOne(TimeFutureIntrospectorSpec.class);

		then(actual.getMonthDayFuture()).isGreaterThan(now);
		then(actual.getMonthDayFutureOrPresent()).isGreaterThanOrEqualTo(now);
	}

	@Property(tries = 100)
	void samplePastOffsetDateTime() {
		TimePastIntrospectorSpec actual = SUT.giveMeOne(TimePastIntrospectorSpec.class);

		OffsetDateTime now = OffsetDateTime.now();
		then(actual.getOffsetDateTimePast()).isBefore(now);
		then(actual.getOffsetDateTimePastOrPresent()).isBeforeOrEqualTo(now);
	}

	@Property(tries = 100)
	void sampleFutureOffsetDateTime() {
		OffsetDateTime now = OffsetDateTime.now();

		TimeFutureIntrospectorSpec actual = SUT.giveMeOne(TimeFutureIntrospectorSpec.class);

		then(actual.getOffsetDateTimeFuture()).isAfter(now);
		then(actual.getOffsetDateTimeFutureOrPresent()).isAfterOrEqualTo(now);
	}

	@Property(tries = 100)
	void samplePastOffsetTime() {
		TimePastIntrospectorSpec actual = SUT.giveMeOne(TimePastIntrospectorSpec.class);

		OffsetTime now = OffsetTime.now();
		then(actual.getOffsetTimePast()).isBefore(now);
		then(actual.getOffsetTimePastOrPresent()).isBeforeOrEqualTo(now);
	}

	@Property(tries = 100)
	void sampleFutureOffsetTime() {
		OffsetTime now = OffsetTime.now();

		TimeFutureIntrospectorSpec actual = SUT.giveMeOne(TimeFutureIntrospectorSpec.class);

		then(actual.getOffsetTimeFuture()).isAfter(now);
		then(actual.getOffsetTimeFutureOrPresent()).isAfterOrEqualTo(now);
	}

	@Property(tries = 100)
	void samplePastYear() {
		TimePastIntrospectorSpec actual = SUT.giveMeOne(TimePastIntrospectorSpec.class);

		Year now = Year.now();
		then(actual.getYearPast()).isLessThan(now);
		then(actual.getYearPastOrPresent()).isLessThanOrEqualTo(now);
	}

	@Property(tries = 100)
	void sampleFutureYear() {
		Year now = Year.now();
		TimeFutureIntrospectorSpec actual = SUT.giveMeOne(TimeFutureIntrospectorSpec.class);

		then(actual.getYearFuture()).isGreaterThan(now);
		then(actual.getYearFutureOrPresent()).isGreaterThanOrEqualTo(now);
	}

	@Property(tries = 100)
	void samplePastYearMonth() {
		TimePastIntrospectorSpec actual = SUT.giveMeOne(TimePastIntrospectorSpec.class);

		YearMonth now = YearMonth.now();
		then(actual.getYearMonthPast()).isLessThan(now);
		then(actual.getYearMonthPastOrPresent()).isLessThanOrEqualTo(now);
	}

	@Property(tries = 100)
	void sampleFutureYearMonth() {
		YearMonth now = YearMonth.now();

		TimeFutureIntrospectorSpec actual = SUT.giveMeOne(TimeFutureIntrospectorSpec.class);

		then(actual.getYearMonthFuture()).isGreaterThan(now);
		then(actual.getYearMonthFutureOrPresent()).isGreaterThanOrEqualTo(now);
	}

	@Property(tries = 100)
	void sampleNullAnnotations() {
		NullAnnotationIntrospectorSpec actual = SUT.giveMeOne(NullAnnotationIntrospectorSpec.class);

		then(actual.getNullValue()).isNull();
		then(actual.getNotNull()).isNotNull();
		then(actual.getNotBlank()).isNotBlank();
		then(actual.getNotEmpty()).isNotEmpty();
		then(actual.getNotEmptyContainer()).isNotEmpty();
	}

	@Property(tries = 100)
	void sampleContainerAnnotations() {
		ContainerAnnotationIntrospectorSpec actual = SUT.giveMeOne(ContainerAnnotationIntrospectorSpec.class);

		then(actual.getDefaultSizeContainer()).hasSizeBetween(0, 3);
		then(actual.getSizeContainer()).hasSizeBetween(5, 10);
		then(actual.getMinSizeContainer()).hasSizeGreaterThanOrEqualTo(3);
		then(actual.getMaxSizeContainer()).hasSizeLessThanOrEqualTo(5);
		then(actual.getNotEmptyContainer()).isNotEmpty();
		then(actual.getNotEmptyAndMaxSizeContainer()).hasSizeBetween(1, 5);
	}

	@Property(tries = 1)
	void logFailedProperties() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(NullAnnotationIntrospectorSpec.class)
				.setNull("notNull")
				.setNull("notBlank")
				.sample()
		)
			.isExactlyInstanceOf(FilterMissException.class);
	}
}
