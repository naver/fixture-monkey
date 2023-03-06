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
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.JqwikException;
import net.jqwik.engine.SourceOfRandomness;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Reference jqwik SourceOfRandomness
 */
@API(since = "0.4.0", status = Status.MAINTAINED)
@SuppressFBWarnings("DMI_RANDOM_USED_ONLY_ONCE")
public class Randoms {
	private static final boolean USE_JQWIK_ENGINE;
	private static final Supplier<Random> RNG = ThreadLocalRandom::current;
	private static final ThreadLocal<Random> CURRENT = ThreadLocal.withInitial(Randoms::newRandom);

	static {
		boolean useJqwikEngine;
		try {
			Class.forName("net.jqwik.engine.SourceOfRandomness");
			useJqwikEngine = true;
		} catch (ClassNotFoundException e) {
			useJqwikEngine = false;
		}
		USE_JQWIK_ENGINE = useJqwikEngine;
	}

	private Randoms() {
	}

	public static String createRandomSeed() {
		return USE_JQWIK_ENGINE
			? SourceOfRandomness.createRandomSeed()
			: Long.toString(RNG.get().nextLong());
	}

	public static Random create(String seed) {
		if (USE_JQWIK_ENGINE) {
			return SourceOfRandomness.create(seed);
		}

		try {
			Random random = newRandom(Long.parseLong(seed));
			CURRENT.set(random);
			return random;
		} catch (NumberFormatException nfe) {
			throw new JqwikException(String.format("[%s] is not a valid random seed.", seed));
		}
	}

	public static Random newRandom() {
		return USE_JQWIK_ENGINE
			? SourceOfRandomness.newRandom()
			: new XorShiftRandom();
	}

	public static Random newRandom(final long seed) {
		return USE_JQWIK_ENGINE
			? SourceOfRandomness.newRandom(seed)
			: new XorShiftRandom(seed);
	}

	public static Random current() {
		return USE_JQWIK_ENGINE
			? SourceOfRandomness.current()
			: CURRENT.get();
	}

	public static int nextInt(int bound) {
		return current().nextInt(bound);
	}

	/**
	 * A faster but not thread safe implementation of {@linkplain java.util.Random}.
	 * It also has a period of 2^n - 1 and better statistical randomness.
	 *
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

		private XorShiftRandom() {
			this(System.nanoTime());
		}

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
