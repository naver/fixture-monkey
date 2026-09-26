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
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.expression.Segment;

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
	private static final ThreadLocal<@Nullable Random> CURRENT_RANDOM = new ThreadLocal<>();

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
	 * Returns the scope nested in this one under {@code key}. A child depends on its parent and its key, and on the
	 * order scopes are nested in, so {@code a.scope(x).scope(y)} differs from {@code a.scope(y).scope(x)}.
	 *
	 * @param key what tells the child apart from its siblings
	 * @return the child scope
	 */
	public SeedSnapshot scope(long key) {
		return new SeedSnapshot(mix(mix(baseSeed ^ sequence * GOLDEN_RATIO_PRIME) + key), 0);
	}

	/**
	 * Returns the scope of the node reached through {@code segment} from the node this scope belongs to.
	 *
	 * @param segment the segment leading to the child node
	 * @return the child node's scope
	 */
	public SeedSnapshot scope(Segment segment) {
		return scope(segment.toExpression().hashCode());
	}

	/**
	 * Returns the scope of the node at {@code path} when this is the scope of the root, nesting one scope per
	 * segment.
	 *
	 * @param path the path of the node
	 * @return the node's scope
	 */
	public SeedSnapshot scopeOf(PathExpression path) {
		SeedSnapshot scope = this;
		for (Segment segment : path.getSegments()) {
			scope = scope.scope(segment);
		}
		return scope;
	}

	/**
	 * Runs {@code action} with the random of {@code scope} as the current random, so values drawn while it runs
	 * come from the scope. The random is created once for the run, and the previous one is restored afterwards.
	 *
	 * @param scope  the scope to draw from
	 * @param action the action drawing values
	 * @param <T>    the result type
	 * @return the action's result
	 */
	public static <T> T runIn(SeedSnapshot scope, Supplier<T> action) {
		Random outer = CURRENT_RANDOM.get();
		CURRENT_RANDOM.set(new Random(scope.seedFor(0)));
		try {
			return action.get();
		} finally {
			if (outer != null) {
				CURRENT_RANDOM.set(outer);
			} else {
				CURRENT_RANDOM.remove();
			}
		}
	}

	/**
	 * Returns the random of the scope running on this thread.
	 *
	 * @return the current random, or null when no scope is running
	 */
	@Nullable
	public static Random currentRandom() {
		return CURRENT_RANDOM.get();
	}

	private static long mix(long value) {
		long mixed = value;
		mixed = (mixed ^ (mixed >>> 30)) * 0xbf58476d1ce4e5b9L;
		mixed = (mixed ^ (mixed >>> 27)) * 0x94d049bb133111ebL;
		return mixed ^ (mixed >>> 31);
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
