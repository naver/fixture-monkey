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

import static org.assertj.core.api.BDDAssertions.then;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.RepeatedTest;

import lombok.Data;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

class UserDirectiveOrderTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@RepeatedTest(10)
	void wildcardSizeDeclaredAfterExactSizeWinsAtTheSamePath() {
		// when
		Outer actual = SUT.giveMeBuilder(Outer.class)
			.size("holders", 2)
			.size("holders[0].first", 1)
			.size("holders[*].first", 3)
			.sample();

		// then
		then(actual.getHolders().get(0).getFirst()).hasSize(3);
		then(actual.getHolders().get(1).getFirst()).hasSize(3);
	}

	@RepeatedTest(10)
	void exactSizeDeclaredAfterWildcardSizeWinsOnlyAtItsPath() {
		// when
		Outer actual = SUT.giveMeBuilder(Outer.class)
			.size("holders", 2)
			.size("holders[*].first", 3)
			.size("holders[0].first", 1)
			.sample();

		// then
		then(actual.getHolders().get(0).getFirst()).hasSize(1);
		then(actual.getHolders().get(1).getFirst()).hasSize(3);
	}

	@RepeatedTest(10)
	void sizeDeclaredAfterValueOfInterfaceTypedContainerWins() {
		// given
		Collection<String> value = new ArrayList<>(Arrays.asList("a", "b"));

		// when
		Collection<String> actual = SUT.giveMeBuilder(CollectionHolder.class)
			.set("values", value)
			.size("values", 4)
			.sample()
			.getValues();

		// then
		then(actual).hasSize(4);
	}

	@RepeatedTest(10)
	void valueDeclaredAfterSizeOfInterfaceTypedContainerWins() {
		// given
		Collection<String> value = new ArrayList<>(Arrays.asList("a", "b"));

		// when
		Collection<String> actual = SUT.giveMeBuilder(CollectionHolder.class)
			.size("values", 4)
			.set("values", value)
			.sample()
			.getValues();

		// then
		then(actual).containsExactly("a", "b");
	}

	@Data
	public static class Outer {
		private List<Holder> holders;
	}

	@Data
	public static class Holder {
		private List<String> first;
	}

	@Data
	public static class CollectionHolder {
		private Collection<String> values;
	}
}
