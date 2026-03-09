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

import java.util.Random;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Provides random instances with seed management capabilities.
 * Implementations may cache random instances for deterministic behavior.
 *
 * @since 1.1.16
 */
@API(since = "1.1.16", status = Status.EXPERIMENTAL)
public interface RandomSource {
	/**
	 * Generates and returns the next seed value.
	 * The implementation may cache a random instance associated with the returned seed.
	 *
	 * @return the next seed value
	 */
	long nextSeed();

	/**
	 * Retrieves a random instance associated with the given seed.
	 * Returns null if no random instance is associated with the seed.
	 *
	 * @param seed the seed value
	 * @return the random instance associated with the seed, or null if not found
	 */
	Random getRandom(long seed);

	/**
	 * Retrieves the random instance associated with the current seed.
	 * The current seed is the last value returned by {@link #nextSeed()}.
	 *
	 * @return the random instance associated with the current seed, or null if not found
	 */
	Random getCurrentSeedRandom();
}
