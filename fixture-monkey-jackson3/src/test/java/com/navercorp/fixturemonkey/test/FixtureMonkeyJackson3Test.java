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

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.jqwik.api.Property;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import lombok.Value;
import tools.jackson.databind.JsonNode;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.customizer.InnerSpec;
import com.navercorp.fixturemonkey.jackson3.plugin.Jackson3Plugin;

class FixtureMonkeyJackson3Test {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.plugin(new Jackson3Plugin())
		.defaultNotNull(true)
		.build();

	@Property
	void jsonFormat() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(JsonFormatSpec.class));
	}

	@Property
	void jsonNode() {
		JsonNodeWrapperClass actual = SUT.giveMeOne(JsonNodeWrapperClass.class);

		then(actual).isNotNull();
		then(actual.value).isNotNull();
	}

	@Property
	void sampleNested() {
		thenNoException()
			.isThrownBy(() -> SUT.giveMeOne(NestedStringValue.class));
	}

	@Property
	void sampleGenericObject() {
		StringValue actual = SUT.giveMeOne(new TypeReference<GenericObject<StringValue>>() {
			})
			.getValue();

		then(actual).isInstanceOf(StringValue.class);
		then(actual).isNotNull();
	}

	@Property
	void sampleListNestedElement() {
		StringValue actual = SUT.giveMeBuilder(new TypeReference<List<List<StringValue>>>() {
			})
			.size("$", 1)
			.size("$[0]", 1)
			.sample()
			.get(0)
			.get(0);

		then(actual).isInstanceOf(StringValue.class);
		then(actual).isNotNull();
	}

	@Property
	void sampleMapNestedListValue() {
		StringValue actual = SUT.giveMeBuilder(new TypeReference<Map<String, List<StringValue>>>() {
			})
			.setInner(
				new InnerSpec()
					.size(1)
					.value(v -> v.size(1))
			)
			.sample()
			.values().stream()
			.flatMap(Collection::stream)
			.toList()
			.get(0);

		then(actual).isInstanceOf(StringValue.class);
		then(actual).isNotNull();
	}

	@Property
	void sampleGenericArray() {
		StringValue actual = SUT.giveMeBuilder(new TypeReference<StringValue[][]>() {
			})
			.size("$", 1)
			.size("$[0]", 1)
			.sample()[0][0];

		then(actual).isInstanceOf(StringValue.class);
		then(actual).isNotNull();
	}

	@Value
	public static class JsonFormatSpec {
		@JsonFormat(shape = Shape.NUMBER)
		JsonEnum jsonEnum;

		@JsonFormat(pattern = "yyyy MM dd")
		Date date;

		@JsonFormat(pattern = "yyyy MM dd")
		LocalDate localDate;

		@JsonFormat(pattern = "HH:mm:ss")
		LocalTime localTime;

		@JsonFormat(pattern = "yyyy MM dd HH:mm:ss")
		LocalDateTime localDateTime;

		@JsonFormat(pattern = "yyyy MM dd HH:mm:ssZ")
		Instant instant;

		@JsonFormat(pattern = "yyyy MM dd HH:mm:ssZ")
		ZonedDateTime zonedDateTime;

		@JsonFormat(pattern = "yyyy MM dd HH:mm:ssZ")
		OffsetDateTime offsetDateTime;
	}

	public enum JsonEnum {
		ONE, TWO, THREE
	}

	@Value
	public static class JsonNodeWrapperClass {
		JsonNode value;
	}

	@Value
	public static class StringValue {
		String innerValue;
	}

	@Value
	public static class NestedStringValue {
		StringValue value;
	}

	@Value
	public static class GenericObject<T> {
		T value;
	}
}
