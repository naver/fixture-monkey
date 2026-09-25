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

package com.navercorp.fixturemonkey.api.jqwik;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.arbitrary.MonkeyStringArbitrary;
import com.navercorp.fixturemonkey.api.random.Randoms;

class ArbitraryUtilsTest {
	private static final long SEED = 20260926L;
	private static final int SAMPLE_SIZE = 200;

	@Test
	void toCombinableArbitrarySamplesSameValuesAsThreadSafeSample() {
		// given
		Arbitrary<String> arbitrary = new MonkeyStringArbitrary().filterCharacter(c -> !Character.isISOControl(c));
		List<String> expected = sampleWithSeed(() -> ArbitraryUtils.newThreadSafeArbitrary(arbitrary).sample());

		// when
		List<String> actual = sampleWithSeed(() -> ArbitraryUtils.toCombinableArbitrary(arbitrary).combined());

		// then
		then(actual).isEqualTo(expected);
	}

	@Test
	void toCombinableArbitrarySamplesSameIntegersAsThreadSafeSample() {
		// given
		Arbitrary<Integer> arbitrary = Arbitraries.integers().between(-1000, 1000);
		List<Integer> expected = sampleWithSeed(() -> ArbitraryUtils.newThreadSafeArbitrary(arbitrary).sample());

		// when
		List<Integer> actual = sampleWithSeed(() -> ArbitraryUtils.toCombinableArbitrary(arbitrary).combined());

		// then
		then(actual).isEqualTo(expected);
	}

	private static <T> List<T> sampleWithSeed(Supplier<T> sampler) {
		Randoms.newGlobalSeed(SEED);
		return IntStream.range(0, SAMPLE_SIZE).mapToObj(i -> sampler.get()).collect(Collectors.toList());
	}
}
