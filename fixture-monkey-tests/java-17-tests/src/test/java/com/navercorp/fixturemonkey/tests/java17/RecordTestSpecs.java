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

package com.navercorp.fixturemonkey.tests.java17;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.Stream;

final class RecordTestSpecs {
	public record JavaTypeRecord(
		String string,
		int primitiveInteger,
		float primitiveFloat,
		long primitiveLong,
		double primitiveDouble,
		byte primitiveByte,
		char primitiveCharacter,
		short primitiveShort,
		boolean primitiveBoolean,
		Integer wrapperInteger,
		Float wrapperFloat,
		Long wrapperLong,
		Double wrapperDouble,
		Byte wrapperByte,
		Character wrapperCharacter,
		Short wrapperShort,
		Boolean wrapperBoolean,
		Enum enumValue
	) {
	}

	public enum Enum {
		ONE,
		TWO,
		THREE
	}

	public record DateTimeRecord(
		Calendar calendar,
		Date date,
		Instant instant,
		LocalDate localDate,
		LocalDateTime localDateTime,
		LocalTime localTime,
		ZonedDateTime zonedDateTime,
		MonthDay monthDay,
		OffsetDateTime offsetDateTime,
		OffsetTime offsetTime,
		Period period,
		Duration duration,
		Year year,
		YearMonth yearMonth,
		ZoneOffset zoneOffset
	) {
	}

	public record ContainerRecord(
		int[] primitiveArray,
		String[] array,
		JavaTypeRecord[] complexArray,
		List<String> list,
		List<JavaTypeRecord> complexList,
		Set<String> set,
		Set<JavaTypeRecord> complexSet,
		Map<String, Integer> map,
		Map<String, JavaTypeRecord> complexMap,
		Map.Entry<String, Integer> mapEntry,
		Map.Entry<String, JavaTypeRecord> complexMapEntry,
		Optional<String> optional,
		OptionalInt optionalInt,
		OptionalLong optionalLong,
		OptionalDouble optionalDouble
	) {
	}

	public record ComplexContainerRecord(
		Iterable<String> iterable,
		Iterator<String> iterator,
		Stream<String> stream
	) {
	}

	public record NoArgsConstructorRecord() {
	}

	public record TwoConstructorsRecord(
		String string
	) {
		public TwoConstructorsRecord(Integer integer) {
			this(String.valueOf(integer));
		}
	}

	public record CompactConstructorRecord(
		String string
	) {
		public CompactConstructorRecord {
			string = "12345";
		}
	}

	public enum EnumClass {
		ENUM_A {
			@Override
			public String getString() {
				return "a";
			}
		},
		ENUM_B {
			@Override
			public String getString() {
				return "b";
			}
		};

		public abstract String getString();
	}
}
