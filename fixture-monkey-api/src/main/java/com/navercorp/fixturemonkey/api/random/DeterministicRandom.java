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

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "1.1.16", status = Status.MAINTAINED)
public class DeterministicRandom extends Random {
	private static final Map<Long, Random> SEED_CACHE = new ConcurrentHashMap<>();
	private static final int MAX_CACHE_SIZE = 32;

	private long currentSeed;

	public DeterministicRandom(long seed) {
		super(seed);
		this.currentSeed = seed;
	}

	@Override
	public long nextLong() {
		long value = super.nextLong();
		this.currentSeed = value;

		SEED_CACHE.computeIfAbsent(value, seed -> {
			manageCacheSize();
			return new Random(seed);
		});

		return value;
	}

	public Random getRandomInstance(long seed) {
		return SEED_CACHE.get(seed);
	}

	public Random getCurrentSeedRandom() {
		return SEED_CACHE.get(this.currentSeed);
	}

	private static void manageCacheSize() {
		if (SEED_CACHE.size() >= MAX_CACHE_SIZE) {
			SEED_CACHE.clear();
		}
	}

	public static Map<Long, Random> getSeedCache() {
		return SEED_CACHE;
	}
}
