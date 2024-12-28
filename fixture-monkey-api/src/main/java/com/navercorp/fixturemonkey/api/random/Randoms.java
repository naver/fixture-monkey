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

import net.jqwik.engine.SourceOfRandomness;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Reference jqwik SourceOfRandomness
 */
@API(since = "0.4.0", status = Status.INTERNAL)
@SuppressFBWarnings("DMI_RANDOM_USED_ONLY_ONCE")
public abstract class Randoms {
	private static final boolean USE_JQWIK_ENGINE;
	private static final ThreadLocal<Random> CURRENT;
	private static final ThreadLocal<Long> SEED;

	static {
		boolean useJqwikEngine;
		try {
			Class.forName("net.jqwik.engine.SourceOfRandomness");
			useJqwikEngine = true;
		} catch (ClassNotFoundException e) {
			useJqwikEngine = false;
		}
		USE_JQWIK_ENGINE = useJqwikEngine;
		SEED = ThreadLocal.withInitial(System::nanoTime);
		CURRENT = ThreadLocal.withInitial(() -> Randoms.create(SEED.get()));
	}

	/**
	 * It is deprecated. Do not use this method.
	 * Use {@link #setSeed(long)} instead.
	 */
	@Deprecated
	public static Random create(String seed) {
		setSeed(Long.parseLong(seed));
		return CURRENT.get();
	}

	public static void setSeed(long seed) {
		SEED.set(seed);
	}

	public static Random current() {
		return USE_JQWIK_ENGINE
			? SourceOfRandomness.current()
			: CURRENT.get();
	}

	public static long currentSeed() {
		return SEED.get();
	}

	public static int nextInt(int bound) {
		return current().nextInt(bound);
	}

	private static Random create(long seed) {
		if (USE_JQWIK_ENGINE) {
			SEED.set(seed);
			return SourceOfRandomness.create(String.valueOf(seed));
		}

		try {
			Random random = newRandom(seed);
			CURRENT.set(random);
			SEED.set(seed);
			return random;
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException(String.format("[%s] is not a valid random seed.", seed));
		}
	}

	private static Random newRandom(final long seed) {
		return USE_JQWIK_ENGINE
			? SourceOfRandomness.newRandom(seed)
			: new XorShiftRandom(seed);
	}

	/**
	 * A faster but not thread safe implementation of {@linkplain java.util.Random}.
	 * It also has a period of 2^n - 1 and better statistical randomness.
	 * <p>
	 * See for details: https://www.javamex.com/tutorials/random_numbers/xorshift.shtml
	 *
	 * <p>
	 * For further performance improvements within jqwik, consider to override:
	 * <ul>
	 *     <li>nextDouble()</li>
	 *     <li>nextBytes(int)</li>
	 * </ul>
	 */
	private static class XorShiftRandom extends Random {
		private long seed;

		private XorShiftRandom(long seed) {
			if (seed == 0L) {
				throw new IllegalArgumentException("0L is not an allowed seed value");
			}
			this.seed = seed;
		}

		@Override
		protected int next(int nbits) {
			long value = nextLong();
			value &= ((1L << nbits) - 1);
			return (int)value;
		}

		/**
		 * Will never generate 0L
		 */
		@Override
		public long nextLong() {
			long value = this.seed;
			value ^= (value << 21);
			value ^= (value >>> 35);
			value ^= (value << 4);
			this.seed = value;
			return value;
		}
	}
}
