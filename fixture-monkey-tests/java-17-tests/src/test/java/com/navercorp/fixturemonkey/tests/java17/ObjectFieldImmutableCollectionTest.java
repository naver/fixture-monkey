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

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

class ObjectFieldImmutableCollectionTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@Test
	void setSingleElementImmutableListToObjectField() {
		// given
		List<String> expected = List.of("a");

		// when
		ObjectValue actual = SUT.giveMeBuilder(ObjectValue.class)
			.set("value", expected)
			.sample();

		// then
		then(actual.getValue()).isEqualTo(expected);
	}

	@Test
	void setMultipleElementsImmutableListToObjectField() {
		// given
		List<String> expected = List.of("a", "b", "c");

		// when
		ObjectValue actual = SUT.giveMeBuilder(ObjectValue.class)
			.set("value", expected)
			.sample();

		// then
		then(actual.getValue()).isEqualTo(expected);
	}

	@Test
	void setSingleElementImmutableSetToObjectField() {
		// given
		Set<String> expected = Set.of("a");

		// when
		ObjectValue actual = SUT.giveMeBuilder(ObjectValue.class)
			.set("value", expected)
			.sample();

		// then
		then(actual.getValue()).isEqualTo(expected);
	}

	@Test
	void setMultipleElementsImmutableSetToObjectField() {
		// given
		Set<String> expected = Set.of("a", "b", "c");

		// when
		ObjectValue actual = SUT.giveMeBuilder(ObjectValue.class)
			.set("value", expected)
			.sample();

		// then
		then(actual.getValue()).isEqualTo(expected);
	}

	public static class ObjectValue {
		private Object value;

		public Object getValue() {
			return value;
		}
	}
}
