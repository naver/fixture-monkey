
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

package com.navercorp.fixturemonkey.test;

import java.math.BigDecimal;
import java.math.BigInteger;
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
import java.util.stream.Stream;

import javax.annotation.Nullable;

import lombok.Data;
import lombok.EqualsAndHashCode;

class FixtureMonkeyV04TestSpecs {
	@Data
	@EqualsAndHashCode(exclude = {"strIterator", "strStream"})
	public static class ComplexObject {
		private String str;
		private int integer;
		private int[] intArray;
		private List<String> strList;
		private String[] strArray;
		private SimpleEnum enumValue;
		private SimpleObject object;
		private List<SimpleObject> list;
		private Map<String, SimpleObject> map;
		private Map.Entry<String, SimpleObject> mapEntry;
		private Iterable<String> strIterable;
		private Iterator<String> strIterator;
		private Stream<String> strStream;
	}

	public enum SimpleEnum {
		ENUM_1, ENUM_2, ENUM_3, ENUM_4
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@Data
	public static class SimpleObject {
		private String str;
		private char character;
		private Character wrapperCharacter;
		private short primitiveShort;
		private Short wrapperShort;
		private byte primitiveByte;
		private Byte wrapperByte;
		private double primitiveDouble;
		private Double wrapperDouble;
		private float primitiveFloat;
		private Float wrapperFloat;
		private int integer;
		private Integer wrapperInteger;
		private long primitiveLong;
		private Long wrapperLong;
		private boolean primitiveBoolean;
		private Boolean wrapperBoolean;
		private BigInteger bigInteger;
		private BigDecimal bigDecimal;
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
		private Optional<String> optionalString;
		private OptionalInt optionalInt;
		private OptionalLong optionalLong;
		private OptionalDouble optionalDouble;
	}

	@Data
	public static class StringPair {
		private String value1;
		private String value2;
	}

	@Data
	public static class ListStringObject {
		private List<String> values;
	}

	@Data
	public static class StaticFieldObject {
		public static final StaticFieldObject CONSTANT = new StaticFieldObject();
	}

	@Data
	public static class NullableObject {
		@Nullable
		List<String> values;
	}

	public interface Interface {
	}

	@Data
	public static class InterfaceImplementation implements Interface {
		private String value;
	}

	@Data
	public static class InterfaceFieldObject {
		InterfaceImplementation value;
	}

	public enum TwoEnum {
		ONE, TWO
	}

	public enum EnumObject {
		ONE,
		TWO,
		THREE
	}
}
