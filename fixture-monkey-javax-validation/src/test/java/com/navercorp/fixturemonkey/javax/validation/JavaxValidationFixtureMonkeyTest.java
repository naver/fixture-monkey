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

package com.navercorp.fixturemonkey.javax.validation;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;

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
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.LabMonkey;
import com.navercorp.fixturemonkey.javax.validation.plugin.JavaxValidationPlugin;
import com.navercorp.fixturemonkey.javax.validation.spec.BigDecimalIntrospectorSpec;
import com.navercorp.fixturemonkey.javax.validation.spec.BigIntegerIntrospectorSpec;
import com.navercorp.fixturemonkey.javax.validation.spec.BooleanIntrospectorSpec;
import com.navercorp.fixturemonkey.javax.validation.spec.ByteIntrospectorSpec;
import com.navercorp.fixturemonkey.javax.validation.spec.CharacterIntrospectorSpec;
import com.navercorp.fixturemonkey.javax.validation.spec.ContainerAnnotationIntrospectorSpec;
import com.navercorp.fixturemonkey.javax.validation.spec.DoubleIntrospectorSpec;
import com.navercorp.fixturemonkey.javax.validation.spec.FloatIntrospectorSpec;
import com.navercorp.fixturemonkey.javax.validation.spec.IntIntrospectorSpec;
import com.navercorp.fixturemonkey.javax.validation.spec.LongIntrospectorSpec;
import com.navercorp.fixturemonkey.javax.validation.spec.NullAnnotationIntrospectorSpec;
import com.navercorp.fixturemonkey.javax.validation.spec.ShortIntrospectorSpec;
import com.navercorp.fixturemonkey.javax.validation.spec.StringIntrospectorSpec;
import com.navercorp.fixturemonkey.javax.validation.spec.TimeIntrospectorSpec;

class JavaxValidationFixtureMonkeyTest {
	private static final LabMonkey SUT = LabMonkey.labMonkeyBuilder()
		.plugin(new JavaxValidationPlugin())
		.defaultNotNull(true)
		.build();

	@Property
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

	@Property
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

	@Property
	void sampleBoolean() {
		BooleanIntrospectorSpec actual = SUT.giveMeOne(BooleanIntrospectorSpec.class);

		then(actual.isBoolValue()).isIn(true, false);
		then(actual.isAssertFalse()).isFalse();
		then(actual.isAssertTrue()).isTrue();
	}

	@Property
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

	@Property
	void sampleCharacter() {
		CharacterIntrospectorSpec actual = SUT.giveMeOne(CharacterIntrospectorSpec.class);

		then(actual.getCharacter()).isBetween(Character.MIN_VALUE, Character.MAX_VALUE);
	}

	@Property
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

	@Property
	void sampleFloat() {
		FloatIntrospectorSpec actual = SUT.giveMeOne(FloatIntrospectorSpec.class);

		then(actual.getFloatValue()).isBetween(-Float.MAX_VALUE, Float.MAX_VALUE);
		then(actual.getDigitsValue()).isBetween(-999f, 999f);
		then(actual.getDecimalMin()).isGreaterThanOrEqualTo(100);
		then(actual.getDecimalMinExclusive()).isGreaterThan(100);
		then(actual.getDecimalMax()).isLessThanOrEqualTo(100);
		then(actual.getDecimalMaxExclusive()).isLessThan(100);
		then(actual.getNegative()).isNegative();
		then(actual.getNegativeOrZero()).isLessThanOrEqualTo(0);
		then(actual.getPositive()).isPositive();
		then(actual.getPositiveOrZero()).isGreaterThanOrEqualTo(0);
	}

	@Property
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

	@Property
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

	@Property
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

	@Property
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
	}

	@Property
	void sampleCalendar() {
		TimeIntrospectorSpec actual = SUT.giveMeOne(TimeIntrospectorSpec.class);

		Calendar now = Calendar.getInstance();
		then(actual.getCalendarPast().toInstant().toEpochMilli()).isLessThan(now.toInstant().toEpochMilli());
		then(actual.getCalendarPastOrPresent().getTimeInMillis()).isLessThanOrEqualTo(now.toInstant().toEpochMilli());
		then(actual.getCalendarFuture().getTimeInMillis()).isGreaterThan(now.toInstant().toEpochMilli());
		then(actual.getCalendarFutureOrPresent().getTimeInMillis())
			.isGreaterThanOrEqualTo(now.toInstant().toEpochMilli());
	}

	@Property
	void sampleDate() {
		TimeIntrospectorSpec actual = SUT.giveMeOne(TimeIntrospectorSpec.class);

		Date now = new Date();
		then(actual.getDatePast().getTime()).isLessThan(now.getTime());
		then(actual.getDatePastOrPresent().getTime()).isLessThanOrEqualTo(now.getTime());
		then(actual.getDateFuture().getTime()).isGreaterThan(now.getTime());
		then(actual.getDateFutureOrPresent().getTime()).isGreaterThanOrEqualTo(now.getTime());
	}

	@Property
	void sampleInstant() {
		TimeIntrospectorSpec actual = SUT.giveMeOne(TimeIntrospectorSpec.class);

		Instant now = Instant.now();
		then(actual.getInstantPast()).isBefore(now);
		then(actual.getInstantPastOrPresent()).isBeforeOrEqualTo(now);
		then(actual.getInstantFuture()).isAfter(now);
		then(actual.getInstantFutureOrPresent()).isAfterOrEqualTo(now);
	}

	@Property
	void sampleLocalDate() {
		TimeIntrospectorSpec actual = SUT.giveMeOne(TimeIntrospectorSpec.class);

		LocalDate now = LocalDate.now();
		then(actual.getLocalDatePast()).isBefore(now);
		then(actual.getLocalDatePastOrPresent()).isBeforeOrEqualTo(now);
		then(actual.getLocalDateFuture()).isAfter(now);
		then(actual.getLocalDateFutureOrPresent()).isAfterOrEqualTo(now);
	}

	@Property
	void sampleLocalDateTime() {
		TimeIntrospectorSpec actual = SUT.giveMeOne(TimeIntrospectorSpec.class);

		LocalDateTime now = LocalDateTime.now();
		then(actual.getLocalDateTimePast()).isBefore(now);
		then(actual.getLocalDateTimePastOrPresent()).isBeforeOrEqualTo(now);
		then(actual.getLocalDateTimeFuture()).isAfter(now);
		then(actual.getLocalDateTimeFutureOrPresent()).isAfterOrEqualTo(now);
	}

	@Property
	void sampleLocalTime() {
		TimeIntrospectorSpec actual = SUT.giveMeOne(TimeIntrospectorSpec.class);

		LocalTime now = LocalTime.now();
		then(actual.getLocalTimePast()).isBefore(now);
		then(actual.getLocalTimePastOrPresent()).isBeforeOrEqualTo(now);
		then(actual.getLocalTimeFuture()).isAfter(now);
		then(actual.getLocalTimeFutureOrPresent()).isAfterOrEqualTo(now);
	}

	@Property
	void sampleZonedDateTime() {
		TimeIntrospectorSpec actual = SUT.giveMeOne(TimeIntrospectorSpec.class);

		ZonedDateTime now = ZonedDateTime.now();
		then(actual.getZonedDateTimePast()).isBefore(now);
		then(actual.getZonedDateTimePastOrPresent()).isBeforeOrEqualTo(now);
		then(actual.getZonedDateTimeFuture()).isAfter(now);
		then(actual.getZonedDateTimeFutureOrPresent()).isAfterOrEqualTo(now);
	}

	@Property
	void sampleMonthDay() {
		TimeIntrospectorSpec actual = SUT.giveMeOne(TimeIntrospectorSpec.class);

		MonthDay now = MonthDay.now();
		then(actual.getMonthDayPast()).isLessThan(now);
		then(actual.getMonthDayPastOrPresent()).isLessThanOrEqualTo(now);
		then(actual.getMonthDayFuture()).isGreaterThan(now);
		then(actual.getMonthDayFutureOrPresent()).isGreaterThanOrEqualTo(now);
	}

	@Property
	void sampleOffsetDateTime() {
		TimeIntrospectorSpec actual = SUT.giveMeOne(TimeIntrospectorSpec.class);

		OffsetDateTime now = OffsetDateTime.now();
		then(actual.getOffsetDateTimePast()).isBefore(now);
		then(actual.getOffsetDateTimePastOrPresent()).isBeforeOrEqualTo(now);
		then(actual.getOffsetDateTimeFuture()).isAfter(now);
		then(actual.getOffsetDateTimeFutureOrPresent()).isAfterOrEqualTo(now);
	}

	@Property
	void sampleOffsetTime() {
		TimeIntrospectorSpec actual = SUT.giveMeOne(TimeIntrospectorSpec.class);

		OffsetTime now = OffsetTime.now();
		then(actual.getOffsetTimePast()).isBefore(now);
		then(actual.getOffsetTimePastOrPresent()).isBeforeOrEqualTo(now);
		then(actual.getOffsetTimeFuture()).isAfter(now);
		then(actual.getOffsetTimeFutureOrPresent()).isAfterOrEqualTo(now);
	}

	@Property
	void sampleYear() {
		TimeIntrospectorSpec actual = SUT.giveMeOne(TimeIntrospectorSpec.class);

		Year now = Year.now();
		then(actual.getYearPast()).isLessThan(now);
		then(actual.getYearPastOrPresent()).isLessThanOrEqualTo(now);
		then(actual.getYearFuture()).isGreaterThan(now);
		then(actual.getYearFutureOrPresent()).isGreaterThanOrEqualTo(now);
	}

	@Property
	void sampleYearMonth() {
		TimeIntrospectorSpec actual = SUT.giveMeOne(TimeIntrospectorSpec.class);

		YearMonth now = YearMonth.now();
		then(actual.getYearMonthPast()).isLessThan(now);
		then(actual.getYearMonthPastOrPresent()).isLessThanOrEqualTo(now);
		then(actual.getYearMonthFuture()).isGreaterThan(now);
		then(actual.getYearMonthFutureOrPresent()).isGreaterThanOrEqualTo(now);
	}

	@Property
	void sampleNullAnnotations() {
		NullAnnotationIntrospectorSpec actual = SUT.giveMeOne(NullAnnotationIntrospectorSpec.class);

		then(actual.getNullValue()).isNull();
		then(actual.getNotNull()).isNotNull();
		then(actual.getNotBlank()).isNotBlank();
		then(actual.getNotEmpty()).isNotEmpty();
		then(actual.getNotEmptyContainer()).isNotEmpty();
	}

	@Property
	void sampleContainerAnnotations() {
		ContainerAnnotationIntrospectorSpec actual = SUT.giveMeOne(ContainerAnnotationIntrospectorSpec.class);

		then(actual.getDefaultSizeContainer()).hasSizeBetween(0, 3);
		then(actual.getSizeContainer()).hasSizeBetween(5, 10);
		then(actual.getMinSizeContainer()).hasSizeGreaterThanOrEqualTo(3);
		then(actual.getMaxSizeContainer()).hasSizeLessThanOrEqualTo(5);
		then(actual.getNotEmptyContainer()).isNotEmpty();
		then(actual.getNotEmptyAndMaxSizeContainer()).hasSizeBetween(1, 5);
	}
}
