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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * A RandomSource implementation that caches random instances by seed values.
 * This enables deterministic random generation by reusing random instances for the same seed.
 * The cache is limited to a maximum of 32 entries to prevent memory leaks.
 *
 * @since 1.1.16
 */
@API(since = "1.1.16", status = Status.EXPERIMENTAL)
public final class DeterministicRandomSource implements RandomSource {
	private static final int MAX_CACHE_SIZE = 32;

	private final Map<Long, Random> seedCache;
	private final Random baseRandom;
	private long currentSeed;

	/**
	 * Creates a new DeterministicRandomSource with the given initial seed.
	 *
	 * @param initialSeed the initial seed value
	 */
	public DeterministicRandomSource(long initialSeed) {
		this.baseRandom = new Random(initialSeed);
		this.currentSeed = initialSeed;
		this.seedCache = new LinkedHashMap<Long, Random>(MAX_CACHE_SIZE, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<Long, Random> eldest) {
				return size() > MAX_CACHE_SIZE;
			}
		};
	}

	@Override
	public long nextSeed() {
		long seed = this.baseRandom.nextLong();
		this.currentSeed = seed;

		this.seedCache.computeIfAbsent(seed, Random::new);

		return seed;
	}

	@Override
	public Random getRandom(long seed) {
		return this.seedCache.get(seed);
	}

	@Override
	public Random getCurrentSeedRandom() {
		return this.seedCache.get(this.currentSeed);
	}
}
