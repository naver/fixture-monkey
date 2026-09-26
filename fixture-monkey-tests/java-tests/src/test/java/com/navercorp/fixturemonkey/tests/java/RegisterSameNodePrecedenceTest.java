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

import java.beans.ConstructorProperties;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.RepeatedTest;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;

class RegisterSameNodePrecedenceTest {
	private static final FixtureMonkey SCOPE_LIMIT_SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.register(Bag.class, fm -> fm.giveMeBuilder(Bag.class).size("items", 3).set("items[*]", "x", 2))
		.build();

	@RepeatedTest(5)
	void registeredSizeOfHigherPriorityWinsOnSameNode() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				MatcherOperator.exactTypeMatchOperator(Bag.class, fm -> fm.giveMeBuilder(Bag.class).size("items", 1)),
				5
			)
			.register(Bag.class, fm -> fm.giveMeBuilder(Bag.class).size("items", 4), 1)
			.build();

		// when
		List<String> actual = sut.giveMeOne(BagHolder.class).getBag().getItems();

		// then
		then(actual).hasSize(4);
	}

	@RepeatedTest(5)
	void higherPriorityBuilderSetNullWinsOverLowerPrioritySetNotNullOfSameType() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.register(Bag.class, fm -> fm.giveMeBuilder(Bag.class).setNotNull("items"), 5)
			.register(Bag.class, fm -> fm.giveMeBuilder(Bag.class).setNull("items"), 1)
			.build();

		// when
		List<String> actual = sut.giveMeBuilder(BagHolder.class).setNotNull("bag").sample().getBag().getItems();

		// then
		then(actual).isNull();
	}

	@RepeatedTest(5)
	void higherPriorityBuilderRootValueWinsOverLowerPriorityFieldOfSameType() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(Bag.class, fm -> fm.giveMeBuilder(Bag.class).size("items", 2).set("items[0]", "lower"), 5)
			.register(
				Bag.class,
				fm -> fm.giveMeBuilder(Bag.class).set("$", new Bag(Collections.singletonList("higher"))),
				1
			)
			.build();

		// when
		List<String> actual = sut.giveMeOne(BagHolder.class).getBag().getItems();

		// then
		then(actual).containsExactly("higher");
	}

	@RepeatedTest(5)
	void registeredLimitAppliesWithinEachScope() {
		// when
		BagPair actual = SCOPE_LIMIT_SUT.giveMeOne(BagPair.class);

		// then
		then(actual.getFirst().getItems()).containsExactly("x", "x", actual.getFirst().getItems().get(2));
		then(actual.getFirst().getItems().get(2)).isNotEqualTo("x");
		then(actual.getSecond().getItems()).containsExactly("x", "x", actual.getSecond().getItems().get(2));
		then(actual.getSecond().getItems().get(2)).isNotEqualTo("x");
	}

	@RepeatedTest(5)
	void registeredLimitAppliesWhenSamplingScopeRoot() {
		// when
		List<String> actual = SCOPE_LIMIT_SUT.giveMeOne(Bag.class).getItems();

		// then
		then(actual).filteredOn("x"::equals).hasSize(2);
	}

	public static class Bag {
		private final List<String> items;

		@ConstructorProperties("items")
		public Bag(List<String> items) {
			this.items = items;
		}

		public List<String> getItems() {
			return items;
		}
	}

	public static class BagHolder {
		private final Bag bag;

		@ConstructorProperties("bag")
		public BagHolder(Bag bag) {
			this.bag = bag;
		}

		public Bag getBag() {
			return bag;
		}
	}

	public static class BagPair {
		private final Bag first;
		private final Bag second;

		@ConstructorProperties({"first", "second"})
		public BagPair(Bag first, Bag second) {
			this.first = first;
			this.second = second;
		}

		public Bag getFirst() {
			return first;
		}

		public Bag getSecond() {
			return second;
		}
	}
}
