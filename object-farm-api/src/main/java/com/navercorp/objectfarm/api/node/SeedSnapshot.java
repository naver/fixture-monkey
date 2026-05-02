/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.objectfarm.api.node;

import java.util.Random;

import org.jspecify.annotations.Nullable;

/**
 * An immutable snapshot of seed state for deterministic random generation.
 *
 * <p>Each snapshot captures a base seed and a sequence number, allowing
 * reproducible random values while ensuring different values across
 * multiple sample() calls.</p>
 *
 * <p>This class is thread-safe and can be safely passed to cached objects.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * SeedState state = new SeedState(12345L);
 *
 * // First sample() call
 * SeedSnapshot snapshot1 = state.snapshot();
 * int size1 = snapshot1.randomFor(type.hashCode()).nextInt(10);
 *
 * // Second sample() call - different sequence, different values
 * SeedSnapshot snapshot2 = state.snapshot();
 * int size2 = snapshot2.randomFor(type.hashCode()).nextInt(10);
 * }</pre>
 *
 * @see SeedState
 */
public final class SeedSnapshot {

	private static final long GOLDEN_RATIO_PRIME = 0x9E3779B97F4A7C15L;

	private final long baseSeed;
	private final long sequence;

	/**
	 * Creates a new seed snapshot.
	 *
	 * @param baseSeed the base seed value
	 * @param sequence the sequence number for this snapshot
	 */
	public SeedSnapshot(long baseSeed, long sequence) {
		this.baseSeed = baseSeed;
		this.sequence = sequence;
	}

	/**
	 * Computes a deterministic seed for the given type hash.
	 *
	 * <p>The resulting seed is unique for each combination of
	 * (baseSeed, sequence, typeHash), ensuring different random
	 * sequences for different types and different sample() calls.</p>
	 *
	 * <p>Uses a large prime multiplier (golden ratio derived) to spread
	 * sequential values across the seed space, avoiding clustering.</p>
	 *
	 * @param typeHash the hash code of the type
	 * @return a deterministic seed value
	 */
	public long seedFor(int typeHash) {
		long spreadSequence = sequence * GOLDEN_RATIO_PRIME;
		return baseSeed ^ spreadSequence ^ typeHash;
	}

	/**
	 * Creates a new Random instance seeded for the given type hash.
	 *
	 * @param typeHash the hash code of the type
	 * @return a new Random instance with deterministic seed
	 */
	public Random randomFor(int typeHash) {
		return new Random(seedFor(typeHash));
	}

	/**
	 * Returns the base seed of this snapshot.
	 *
	 * @return the base seed
	 */
	public long getBaseSeed() {
		return baseSeed;
	}

	/**
	 * Returns the sequence number of this snapshot.
	 *
	 * @return the sequence number
	 */
	public long getSequence() {
		return sequence;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		SeedSnapshot that = (SeedSnapshot) obj;
		return baseSeed == that.baseSeed && sequence == that.sequence;
	}

	@Override
	public int hashCode() {
		int result = Long.hashCode(baseSeed);
		result = 31 * result + Long.hashCode(sequence);
		return result;
	}

	@Override
	public String toString() {
		return "SeedSnapshot{baseSeed=" + baseSeed + ", sequence=" + sequence + "}";
	}
}
