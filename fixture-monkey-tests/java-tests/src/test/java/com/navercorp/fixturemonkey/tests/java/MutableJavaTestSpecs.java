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

package com.navercorp.fixturemonkey.tests.java;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

import lombok.Data;

class MutableJavaTestSpecs {
	@Data
	public static class JavaTypeObject {
		private String string;
		private int primitiveInteger;
		private float primitiveFloat;
		private long primitiveLong;
		private double primitiveDouble;
		private byte primitiveByte;
		private char primitiveCharacter;
		private short primitiveShort;
		private boolean primitiveBoolean;
		private Integer wrapperInteger;
		private Float wrapperFloat;
		private Long wrapperLong;
		private Double wrapperDouble;
		private Byte wrapperByte;
		private Character wrapperCharacter;
		private Short wrapperShort;
		private Boolean wrapperBoolean;
		private Enum enumValue;
	}

	@Data
	public static class DateTimeObject {
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

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@Data
	public static class ContainerObject {
		private int[] primitiveArray;
		private String[] array;
		private JavaTypeObject[] complexArray;
		private List<String> list;
		private List<JavaTypeObject> complexList;
		private Set<String> set;
		private Set<JavaTypeObject> complexSet;
		private Map<String, Integer> map;
		private Map<String, JavaTypeObject> complexMap;
		private Map.Entry<String, Integer> mapEntry;
		private Map.Entry<String, JavaTypeObject> complexMapEntry;
		private Optional<String> optional;
		private OptionalInt optionalInt;
		private OptionalLong optionalLong;
		private OptionalDouble optionalDouble;
	}

	public enum Enum {
		ONE,
		TWO,
		THREE
	}
}
