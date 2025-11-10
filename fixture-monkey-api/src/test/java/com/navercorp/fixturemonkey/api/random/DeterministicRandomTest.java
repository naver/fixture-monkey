package com.navercorp.fixturemonkey.api.random;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Random;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.LongRange;

class DeterministicRandomTest {

	@Property
	void nextLongCreatesRandomInstanceInCache(@ForAll @LongRange(min = 1L) long initialSeed) {
		// given
		DeterministicRandom.getSeedCache().clear();
		DeterministicRandom deterministicRandom = new DeterministicRandom(initialSeed);

		// when
		long seed = deterministicRandom.nextLong();

		// then
		then(DeterministicRandom.getSeedCache()).containsKey(seed);
		then(DeterministicRandom.getSeedCache().get(seed)).isNotNull();
	}

	@Property
	void getRandomInstanceRetrievesCachedInstance(@ForAll @LongRange(min = 1L) long initialSeed) {
		// given
		DeterministicRandom.getSeedCache().clear();
		DeterministicRandom deterministicRandom = new DeterministicRandom(initialSeed);
		long seed = deterministicRandom.nextLong();

		// when
		Random cachedRandom = deterministicRandom.getRandomInstance(seed);

		// then
		then(cachedRandom).isNotNull();
		then(cachedRandom).isSameAs(DeterministicRandom.getSeedCache().get(seed));
	}

	@Property
	void getCurrentSeedRandomReturnsCurrentSeedRandom(@ForAll @LongRange(min = 1L) long initialSeed) {
		// given
		DeterministicRandom.getSeedCache().clear();
		DeterministicRandom deterministicRandom = new DeterministicRandom(initialSeed);

		// when
		long firstSeed = deterministicRandom.nextLong();
		Random firstRandom = deterministicRandom.getCurrentSeedRandom();

		long secondSeed = deterministicRandom.nextLong();
		Random secondRandom = deterministicRandom.getCurrentSeedRandom();

		// then
		then(firstRandom).isSameAs(deterministicRandom.getRandomInstance(firstSeed));
		then(secondRandom).isSameAs(deterministicRandom.getRandomInstance(secondSeed));
		then(firstRandom).isNotSameAs(secondRandom);
	}

	@Property
	void cacheSizeIsLimitedTo32(@ForAll @LongRange(min = 1L) long initialSeed) {
		// given
		DeterministicRandom.getSeedCache().clear();
		DeterministicRandom deterministicRandom = new DeterministicRandom(initialSeed);

		// when
		for (int i = 0; i < 35; i++) {
			deterministicRandom.nextLong();
		}

		// then
		then(DeterministicRandom.getSeedCache().size()).isLessThanOrEqualTo(32);
	}

	@Property
	void sameInitialSeedProducesSameLongSequence(@ForAll @LongRange(min = 1L) long initialSeed) {
		// given
		DeterministicRandom.getSeedCache().clear();
		DeterministicRandom random1 = new DeterministicRandom(initialSeed);
		DeterministicRandom random2 = new DeterministicRandom(initialSeed);

		// when
		long value1 = random1.nextLong();
		long value2 = random2.nextLong();

		// then
		then(value1).isEqualTo(value2);
	}

	@Property
	void cachedRandomInstanceProducesDeterministicValues(@ForAll @LongRange(min = 1L) long initialSeed) {
		// given
		DeterministicRandom.getSeedCache().clear();
		DeterministicRandom deterministicRandom = new DeterministicRandom(initialSeed);
		long seed = deterministicRandom.nextLong();

		// when
		Random cached1 = deterministicRandom.getRandomInstance(seed);
		Random cached2 = deterministicRandom.getRandomInstance(seed);

		// then
		then(cached1).isSameAs(cached2);
		int value1 = cached1.nextInt();
		int value2 = cached2.nextInt();
		then(value1).isNotEqualTo(value2); // Same instance, so state progresses
	}

	@Property
	void multipleSeedsAreStoredIndependently(@ForAll @LongRange(min = 1L) long initialSeed) {
		// given
		DeterministicRandom.getSeedCache().clear();
		DeterministicRandom deterministicRandom = new DeterministicRandom(initialSeed);

		// when
		long seed1 = deterministicRandom.nextLong();
		long seed2 = deterministicRandom.nextLong();
		long seed3 = deterministicRandom.nextLong();

		// then
		then(DeterministicRandom.getSeedCache()).containsKeys(seed1, seed2, seed3);
		then(deterministicRandom.getRandomInstance(seed1))
			.isNotSameAs(deterministicRandom.getRandomInstance(seed2));
		then(deterministicRandom.getRandomInstance(seed2))
			.isNotSameAs(deterministicRandom.getRandomInstance(seed3));
	}
}
