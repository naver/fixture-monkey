
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
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

class FixtureMonkeyV04TestSpecs {
	@Setter
	@Getter
	@EqualsAndHashCode
	public static class ComplexObject {
		private String str;
		private int integer;
		private Long wrapperLong;
		private List<String> strList;
		private Set<String> strSet;
		private SimpleEnum enumValue;
		private LocalDateTime localDateTime;
		private SimpleObject object;
		private List<SimpleObject> list;
		private Map<String, SimpleObject> map;
		private Map.Entry<String, SimpleObject> mapEntry;
	}

	public enum SimpleEnum {
		ENUM_1, ENUM_2, ENUM_3, ENUM_4
	}

	@Getter
	@Setter
	@EqualsAndHashCode
	public static class SimpleObject {
		private String str;
		private Optional<String> optionalString;
		private OptionalInt optionalInt;
		private OptionalLong optionalLong;
		private OptionalDouble optionalDouble;
		private Instant instant;
	}
}
