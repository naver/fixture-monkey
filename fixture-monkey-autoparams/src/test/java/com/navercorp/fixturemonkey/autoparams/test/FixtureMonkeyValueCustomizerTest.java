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

package com.navercorp.fixturemonkey.autoparams.test;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import org.javaunit.autoparams.Repeat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;

import net.jqwik.api.Arbitrary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.navercorp.fixturemonkey.OldArbitraryBuilderImpl;
import com.navercorp.fixturemonkey.ArbitraryOption;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.autoparams.FixtureMonkeyAutoSource;
import com.navercorp.fixturemonkey.autoparams.customization.FixtureMonkeyCustomizer;
import com.navercorp.fixturemonkey.engine.jupiter.extension.FixtureMonkeySessionExtension;

@ExtendWith(FixtureMonkeySessionExtension.class)
class FixtureMonkeyValueCustomizerTest {
	@BeforeAll
	public static void beforeAll() {
		FixtureMonkeyCustomizer.setUp(
			FixtureMonkey.builder()
				.options(
					ArbitraryOption.builder()
						.defaultNotNull(false)
						.build()
				)
				.build()
		);
	}

	@ParameterizedTest
	@Repeat(100)
	@FixtureMonkeyAutoSource
	void integerWrapper(IntegerWrapperClass value) {
		then(value).isNotNull();
		then(value.getValue()).isGreaterThan(0);
	}

	@ParameterizedTest
	@Repeat(100)
	@FixtureMonkeyAutoSource
	void integerListWrapper(IntegerListClass value) {
		then(value).isNotNull();
		then(value.getValues()).isNotNull();
	}

	@ParameterizedTest
	@Repeat(100)
	@FixtureMonkeyAutoSource
	void stringWrapper(StringWrapperClass value) {
		then(value).isNotNull();
		then(value.getValue()).isNotNull();
	}

	@ParameterizedTest
	@Repeat(100)
	@FixtureMonkeyAutoSource
	void integerWrapperList(List<IntegerWrapperClass> values) {
		values.forEach(it -> then(it.getValue()).isGreaterThan(0));
	}

	@ParameterizedTest
	@Repeat(100)
	@FixtureMonkeyAutoSource
	void integerListWrapperStream(Stream<IntegerListClass> values) {
		values.limit(5).forEach(it -> then(it.getValues()).isNotNull());
	}

	@ParameterizedTest
	@Repeat(100)
	@FixtureMonkeyAutoSource
	void stringWrapperMap(Map<String, StringWrapperClass> map) {
		map.forEach((key, value) -> {
			then(key).isNotNull();
			then(value.getValue()).isNotNull();
		});
	}

	@ParameterizedTest
	@Repeat(100)
	@FixtureMonkeyAutoSource
	void integerWrapperArbitraryBuilder(OldArbitraryBuilderImpl<IntegerWrapperClass> builder) {
		then(builder).isNotNull();
		then(builder.sample().getValue()).isGreaterThan(0);
	}

	@ParameterizedTest
	@Repeat(100)
	@FixtureMonkeyAutoSource
	void integerListWrapperArbitraryBuilder(OldArbitraryBuilderImpl<IntegerListClass> builder) {
		then(builder).isNotNull();
		then(builder.sample().getValues()).isNotNull();
	}

	@ParameterizedTest
	@Repeat(100)
	@FixtureMonkeyAutoSource
	void stringWrapperArbitraryBuilder(OldArbitraryBuilderImpl<StringWrapperClass> builder) {
		then(builder).isNotNull();
		then(builder.sample().getValue()).isNotNull();
	}

	@ParameterizedTest
	@Repeat(100)
	@FixtureMonkeyAutoSource
	void integerWrapperArbitrary(Arbitrary<IntegerWrapperClass> arbitrary) {
		then(arbitrary).isNotNull();
		then(arbitrary.sample().getValue()).isGreaterThan(0);
	}

	@ParameterizedTest
	@Repeat(100)
	@FixtureMonkeyAutoSource
	void integerListWrapperArbitrary(Arbitrary<IntegerListClass> arbitrary) {
		then(arbitrary).isNotNull();
		then(arbitrary.sample().getValues()).isNotNull();
	}

	@ParameterizedTest
	@Repeat(100)
	@FixtureMonkeyAutoSource
	void stringWrapperArbitrary(Arbitrary<StringWrapperClass> arbitrary) {
		then(arbitrary).isNotNull();
		then(arbitrary.sample().getValue()).isNotNull();
	}

	@ParameterizedTest
	@Repeat(100)
	@FixtureMonkeyAutoSource
	void sutAlwaysGeneratesSameFixtureMonkeyInstances(FixtureMonkey fixture1, FixtureMonkey fixture2) {
		then(fixture1).isSameAs(fixture2);
	}

	@Data
	public static class IntegerWrapperClass {
		@Positive
		int value;
	}

	@Data
	public static class IntegerListClass {
		List<Integer> values;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class StringWrapperClass {
		@NotNull
		private String value;
	}
}
