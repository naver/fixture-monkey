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
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

import lombok.Builder;
import lombok.Value;

class ImmutableJavaTestSpecs {
	@Value
	@Builder
	public static class JavaTypeObject {
		String string;
		int primitiveInteger;
		float primitiveFloat;
		long primitiveLong;
		double primitiveDouble;
		byte primitiveByte;
		char primitiveCharacter;
		short primitiveShort;
		boolean primitiveBoolean;
		Integer wrapperInteger;
		Float wrapperFloat;
		Long wrapperLong;
		Double wrapperDouble;
		Byte wrapperByte;
		Character wrapperCharacter;
		Short wrapperShort;
		Boolean wrapperBoolean;
		Enum enumValue;
	}

	@Value
	@Builder
	public static class DateTimeObject {
		Calendar calendar;
		Date date;
		Instant instant;
		LocalDate localDate;
		LocalDateTime localDateTime;
		LocalTime localTime;
		ZonedDateTime zonedDateTime;
		MonthDay monthDay;
		OffsetDateTime offsetDateTime;
		OffsetTime offsetTime;
		Period period;
		Duration duration;
		Year year;
		YearMonth yearMonth;
		ZoneOffset zoneOffset;
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@Value
	@Builder
	public static class ContainerObject {
		int[] primitiveArray;
		String[] array;
		JavaTypeObject[] complexArray;
		List<String> list;
		List<JavaTypeObject> complexList;
		Set<String> set;
		Set<JavaTypeObject> complexSet;
		Map<String, Integer> map;
		Map<String, JavaTypeObject> complexMap;
		Map.Entry<String, Integer> mapEntry;
		Map.Entry<String, JavaTypeObject> complexMapEntry;
		Optional<String> optional;
		OptionalInt optionalInt;
		OptionalLong optionalLong;
		OptionalDouble optionalDouble;
	}

	@Value
	@Builder
	public static class RootJavaTypeObject {
		JavaTypeObject value;
	}

	public enum Enum {
		ONE,
		TWO,
		THREE
	}

	@Value
	@Builder
	public static class SetImplementationWithoutGeneric implements Set<String> {
		String value;

		@Override
		public int size() {
			return 0;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public boolean contains(Object o) {
			return false;
		}

		@Override
		public Iterator<String> iterator() {
			return null;
		}

		@Override
		public Object[] toArray() {
			return new Object[0];
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return null;
		}

		@Override
		public boolean add(String s) {
			return false;
		}

		@Override
		public boolean remove(Object o) {
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return false;
		}

		@Override
		public boolean addAll(Collection<? extends String> c) {
			return false;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return false;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return false;
		}

		@Override
		public void clear() {

		}
	}
}
