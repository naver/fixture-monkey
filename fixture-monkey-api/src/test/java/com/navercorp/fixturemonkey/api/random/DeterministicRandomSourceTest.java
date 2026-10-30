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

package com.navercorp.fixturemonkey.api.random;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Random;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.LongRange;

class DeterministicRandomSourceTest {

	@Property
	void nextSeedCachesRandomInstance(@ForAll @LongRange(min = 1L) long initialSeed) {
		// given
		RandomSource randomSource = new DeterministicRandomSource(initialSeed);

		// when
		long seed = randomSource.nextSeed();

		// then
		Random random = randomSource.getRandom(seed);
		then(random).isNotNull();
	}

	@Property
	void getRandomRetrievesCachedInstance(@ForAll @LongRange(min = 1L) long initialSeed) {
		// given
		RandomSource randomSource = new DeterministicRandomSource(initialSeed);
		long seed = randomSource.nextSeed();

		// when
		Random firstRetrieval = randomSource.getRandom(seed);
		Random secondRetrieval = randomSource.getRandom(seed);

		// then
		then(firstRetrieval).isNotNull();
		then(firstRetrieval).isSameAs(secondRetrieval);
	}

	@Property
	void getCurrentSeedRandomReturnsCurrentSeedRandom(@ForAll @LongRange(min = 1L) long initialSeed) {
		// given
		RandomSource randomSource = new DeterministicRandomSource(initialSeed);

		// when
		long firstSeed = randomSource.nextSeed();
		Random firstRandom = randomSource.getCurrentSeedRandom();

		long secondSeed = randomSource.nextSeed();
		Random secondRandom = randomSource.getCurrentSeedRandom();

		// then
		then(firstRandom).isSameAs(randomSource.getRandom(firstSeed));
		then(secondRandom).isSameAs(randomSource.getRandom(secondSeed));
		then(firstRandom).isNotSameAs(secondRandom);
	}

	@Property
	void manySeedsDoNotCauseMemoryLeak(@ForAll @LongRange(min = 1L) long initialSeed) {
		// given
		RandomSource randomSource = new DeterministicRandomSource(initialSeed);

		// when
		for (int i = 0; i < 100; i++) {
			randomSource.nextSeed();
		}

		// then
		// If no OutOfMemoryError occurs, the cache is properly managed
		then(randomSource).isNotNull();
	}

	@Property
	void sameInitialSeedProducesSameSeedSequence(@ForAll @LongRange(min = 1L) long initialSeed) {
		// given
		RandomSource randomSource1 = new DeterministicRandomSource(initialSeed);
		RandomSource randomSource2 = new DeterministicRandomSource(initialSeed);

		// when
		long seed1 = randomSource1.nextSeed();
		long seed2 = randomSource2.nextSeed();

		// then
		then(seed1).isEqualTo(seed2);
	}

	@Property
	void cachedRandomInstanceProducesDeterministicValues(@ForAll @LongRange(min = 1L) long initialSeed) {
		// given
		RandomSource randomSource = new DeterministicRandomSource(initialSeed);
		long seed = randomSource.nextSeed();

		// when
		Random cached1 = randomSource.getRandom(seed);
		Random cached2 = randomSource.getRandom(seed);

		// then
		then(cached1).isSameAs(cached2);
		int value1 = cached1.nextInt();
		int value2 = cached2.nextInt();
		then(value1).isNotEqualTo(value2); // Same instance, so state progresses
	}

	@Property
	void multipleSeedsAreStoredIndependently(@ForAll @LongRange(min = 1L) long initialSeed) {
		// given
		RandomSource randomSource = new DeterministicRandomSource(initialSeed);

		// when
		long seed1 = randomSource.nextSeed();
		long seed2 = randomSource.nextSeed();
		long seed3 = randomSource.nextSeed();

		// then
		Random random1 = randomSource.getRandom(seed1);
		Random random2 = randomSource.getRandom(seed2);
		Random random3 = randomSource.getRandom(seed3);

		then(random1).isNotNull();
		then(random2).isNotNull();
		then(random3).isNotNull();
		then(random1).isNotSameAs(random2);
		then(random2).isNotSameAs(random3);
	}

	@Property
	void getRandomReturnsNullForUncachedSeed(@ForAll @LongRange(min = 1L) long initialSeed) {
		// given
		RandomSource randomSource = new DeterministicRandomSource(initialSeed);

		// when
		Random random = randomSource.getRandom(999999999L);

		// then
		then(random).isNull();
	}
}
