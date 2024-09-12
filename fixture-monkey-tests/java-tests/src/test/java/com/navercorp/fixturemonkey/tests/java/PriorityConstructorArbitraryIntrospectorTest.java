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

import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;

import java.sql.Timestamp;
import java.util.Arrays;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.PriorityConstructorArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.tests.java.ImmutableGenericTypeSpecs.GenericObject;
import com.navercorp.fixturemonkey.tests.java.ImmutableGenericTypeSpecs.TwoGenericObject;
import com.navercorp.fixturemonkey.tests.java.ImmutableGenericTypeSpecs.TwoGenericObjectConstructorParameterOrderDiff;
import com.navercorp.fixturemonkey.tests.java.ImmutableGenericTypeSpecs.TwoGenericObjectConstructorParameterOrderDiffNameDiff;

class PriorityConstructorArbitraryIntrospectorTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(PriorityConstructorArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@RepeatedTest(TEST_COUNT)
	void sample() {
		Timestamp actual = SUT.giveMeOne(Timestamp.class);

		then(actual).isNotNull();
	}

	@Test
	void setWithoutName() {
		Timestamp actual = SUT.giveMeBuilder(Timestamp.class)
			.set("time", 0)
			.sample();

		Timestamp expected = new Timestamp(0);
		then(actual).isNotEqualTo(expected);
	}

	@RepeatedTest(TEST_COUNT)
	void set() {
		// given
		PriorityConstructorArbitraryIntrospector priorityConstructorArbitraryIntrospector =
			PriorityConstructorArbitraryIntrospector.INSTANCE.withParameterNamesResolver(
				constructor -> Arrays.asList("time")
			);

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(priorityConstructorArbitraryIntrospector)
			.build();

		// when
		Timestamp actual = sut.giveMeBuilder(Timestamp.class)
			.set("time", 0)
			.sample();

		// then
		Timestamp expected = new Timestamp(0);
		then(actual).isEqualTo(expected);
	}

	@RepeatedTest(TEST_COUNT)
	void genericType() {
		String actual = SUT.giveMeOne(new TypeReference<GenericObject<String>>() {
		}).getValue();

		then(actual).isExactlyInstanceOf(String.class);
	}

	@RepeatedTest(TEST_COUNT)
	void twoGenericType() {
		TwoGenericObject<String, Integer> actual = SUT.giveMeOne(
			new TypeReference<TwoGenericObject<String, Integer>>() {
			});

		then(actual.getValue1()).isExactlyInstanceOf(String.class);
		then(actual.getValue2()).isExactlyInstanceOf(Integer.class);
	}

	@RepeatedTest(TEST_COUNT)
	void twoGenericObjectConstructorParameterOrderDiff() {
		TwoGenericObjectConstructorParameterOrderDiff<String, Integer> actual = SUT.giveMeOne(
			new TypeReference<TwoGenericObjectConstructorParameterOrderDiff<String, Integer>>() {
			});

		then(actual.getValue1()).isExactlyInstanceOf(String.class);
		then(actual.getValue2()).isExactlyInstanceOf(Integer.class);
	}

	@RepeatedTest(TEST_COUNT)
	void twoGenericObjectConstructorParameterOrderDiffNameDiff() {
		TwoGenericObjectConstructorParameterOrderDiffNameDiff<String, Integer> actual = SUT.giveMeOne(
			new TypeReference<TwoGenericObjectConstructorParameterOrderDiffNameDiff<String, Integer>>() {
			});

		then(actual.getValue1()).isExactlyInstanceOf(String.class);
		then(actual.getValue2()).isExactlyInstanceOf(Integer.class);
	}
}
