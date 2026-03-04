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

import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages seed state for deterministic random generation across multiple sample() calls.
 *
 * <p>This class maintains a base seed and an incrementing sequence counter.
 * Each call to {@link #snapshot()} returns an immutable {@link SeedSnapshot}
 * with a unique sequence number, ensuring different random values for each
 * sample() call while maintaining reproducibility.</p>
 *
 * <p>This class is thread-safe.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * SeedState state = new SeedState(12345L);
 *
 * // Each snapshot has a unique sequence
 * SeedSnapshot snap1 = state.snapshot(); // sequence=0
 * SeedSnapshot snap2 = state.snapshot(); // sequence=1
 * SeedSnapshot snap3 = state.snapshot(); // sequence=2
 *
 * // Reproducibility: same initial seed produces same sequence
 * SeedState state2 = new SeedState(12345L);
 * SeedSnapshot snap2_1 = state2.snapshot(); // sequence=0, same as snap1
 * }</pre>
 *
 * @see SeedSnapshot
 */
public final class SeedState {
	private final long initialSeed;
	private final AtomicLong counter;

	/**
	 * Creates a new SeedState with the specified initial seed.
	 *
	 * @param initialSeed the initial seed value
	 */
	public SeedState(long initialSeed) {
		this.initialSeed = initialSeed;
		this.counter = new AtomicLong(0);
	}

	/**
	 * Creates an immutable snapshot of the current seed state.
	 *
	 * <p>Each call increments the internal sequence counter, ensuring
	 * that successive snapshots produce different random values.</p>
	 *
	 * @return a new immutable SeedSnapshot
	 */
	public SeedSnapshot snapshot() {
		return new SeedSnapshot(initialSeed, counter.getAndIncrement());
	}

	/**
	 * Creates an immutable snapshot at a specific sequence number
	 * without incrementing the internal counter.
	 *
	 * <p>This is useful for deterministic container size resolution
	 * when {@code fixed()} is called — every resolve uses the same
	 * sequence, producing identical random values.</p>
	 *
	 * @param sequence the sequence number to use
	 * @return a new immutable SeedSnapshot at the given sequence
	 */
	public SeedSnapshot snapshotAt(long sequence) {
		return new SeedSnapshot(initialSeed, sequence);
	}

	/**
	 * Returns the initial seed value.
	 *
	 * @return the initial seed
	 */
	public long getInitialSeed() {
		return initialSeed;
	}

	/**
	 * Returns the current sequence count (for debugging/testing).
	 *
	 * @return the current sequence count
	 */
	public long getCurrentSequence() {
		return counter.get();
	}
}
