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

package com.navercorp.fixturemonkey;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

public class FixtureMonkeySeedTest {
	@Property
	void giveMeOneGeneratesSameValues(@ForAll("seedValues") long seed) {
		FixtureMonkey otherFixture = new FixtureMonkey(seed);
		List<Integer> expected = IntStream.range(0, 10).boxed()
			.map(it -> otherFixture.giveMeOne(Integer.class))
			.collect(toList());
		FixtureMonkey sut = new FixtureMonkey(seed);

		List<Integer> actual = IntStream.range(0, 10).boxed()
			.map(it -> sut.giveMeOne(Integer.class))
			.collect(toList());

		then(actual).hasSameElementsAs(expected);
	}

	@Property
	void giveMeOneGeneratesSeveralDistinctValues(@ForAll("seedValues") long seed) {
		FixtureMonkey sut = new FixtureMonkey(seed);

		Set<Long> actual = IntStream.range(0, 100).boxed()
			.map(it -> sut.giveMeOne(Long.class))
			.collect(toSet());

		then(actual.size()).isGreaterThan(10);
	}

	@Property
	void giveMeOneGeneratesSameValuesWithList(@ForAll("seedValues") long seed) {
		FixtureMonkey otherFixture = new FixtureMonkey(seed);
		List<Byte> expected = otherFixture.giveMe(Byte.class, 10);
		FixtureMonkey sut = new FixtureMonkey(seed);

		List<Byte> actual = IntStream.range(0, 10).boxed()
			.map(it -> sut.giveMeOne(Byte.class))
			.collect(toList());

		then(actual).hasSameElementsAs(expected);
	}

	@Provide
	@SuppressWarnings("unused")
	Arbitrary<Long> seedValues() {
		// Zero is not allowed to a seed value.
		return Arbitraries.longs().filter(it -> it != 0);
	}
}
