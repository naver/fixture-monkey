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

package com.navercorp.fixturemonkey.api.option;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Strategy for selecting interface implementations during assembly.
 * <p>
 * This strategy is used when an interface or abstract type has multiple
 * candidate implementations and one needs to be selected for instantiation.
 *
 * @since 1.1.0
 */
@API(since = "1.1.0", status = Status.EXPERIMENTAL)
@FunctionalInterface
public interface InterfaceSelectionStrategy {
	/**
	 * Selects an index from the candidate implementations.
	 *
	 * @param candidateCount the number of candidate implementations
	 * @param assemblySeed the seed for this assembly (for deterministic randomization)
	 * @param sampleIndex the index of this sample (incremented per interface resolution in same assembly)
	 * @return the index to select (0 to candidateCount-1)
	 */
	int selectIndex(int candidateCount, long assemblySeed, int sampleIndex);

	/**
	 * Random selection strategy using seed-based randomization.
	 * This ensures different implementations are selected across samples.
	 */
	InterfaceSelectionStrategy RANDOM = (count, seed, idx) -> {
		long mixed = seed ^ idx;
		mixed = (mixed ^ (mixed >>> 33)) * 0xff51afd7ed558ccdL;
		mixed = (mixed ^ (mixed >>> 33)) * 0xc4ceb9fe1a85ec53L;
		mixed = mixed ^ (mixed >>> 33);
		return (int)(Math.abs(mixed) % count);
	};

	/**
	 * Round-robin selection strategy.
	 * Cycles through all implementations in order, guaranteeing all implementations
	 * appear when sampling multiple times.
	 */
	InterfaceSelectionStrategy ROUND_ROBIN = (count, seed, idx) -> idx % count;

	/**
	 * Fixed selection strategy.
	 * Always selects the first implementation.
	 */
	InterfaceSelectionStrategy FIXED = (count, seed, idx) -> 0;
}
