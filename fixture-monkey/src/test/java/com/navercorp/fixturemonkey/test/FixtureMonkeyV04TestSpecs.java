
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class FixtureMonkeyV04TestSpecs {
	public static class ComplexObject {
		private String str;
		private int integer;
		private Long wrapperLong;
		private List<String> strList;
		private SimpleEnum enumValue;
		private LocalDateTime localDateTime;
		private SimpleObject object;
		private List<SimpleObject> list;
		private Map<String, SimpleObject> map;

		public String getStr() {
			return str;
		}

		public void setStr(String str) {
			this.str = str;
		}

		public int getInteger() {
			return integer;
		}

		public void setInteger(int integer) {
			this.integer = integer;
		}

		public Long getWrapperLong() {
			return wrapperLong;
		}

		public void setWrapperLong(Long wrapperLong) {
			this.wrapperLong = wrapperLong;
		}

		public List<String> getStrList() {
			return strList;
		}

		public void setStrList(List<String> strList) {
			this.strList = strList;
		}

		public SimpleEnum getEnumValue() {
			return enumValue;
		}

		public void setEnumValue(SimpleEnum enumValue) {
			this.enumValue = enumValue;
		}

		public LocalDateTime getLocalDateTime() {
			return localDateTime;
		}

		public void setLocalDateTime(LocalDateTime localDateTime) {
			this.localDateTime = localDateTime;
		}

		public SimpleObject getObject() {
			return object;
		}

		public void setObject(SimpleObject object) {
			this.object = object;
		}

		public List<SimpleObject> getList() {
			return list;
		}

		public void setList(List<SimpleObject> list) {
			this.list = list;
		}

		public Map<String, SimpleObject> getMap() {
			return map;
		}

		public void setMap(
			Map<String, SimpleObject> map) {
			this.map = map;
		}
	}

	public enum SimpleEnum {
		ENUM_1, ENUM_2, ENUM_3, ENUM_4
	}

	public static class SimpleObject {
		private String str;
		private Optional<String> optionalString;
		private Instant instant;

		public String getStr() {
			return str;
		}

		public void setStr(String str) {
			this.str = str;
		}

		public Optional<String> getOptionalString() {
			return optionalString;
		}

		public void setOptionalString(Optional<String> optionalString) {
			this.optionalString = optionalString;
		}

		public Instant getInstant() {
			return instant;
		}

		public void setInstant(Instant instant) {
			this.instant = instant;
		}
	}
}
