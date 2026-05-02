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

package com.navercorp.objectfarm.api.node;

import java.util.Random;

import com.navercorp.objectfarm.api.type.JvmType;

/**
 * A container size resolver that generates random container sizes within a specified range.
 * The random generation uses a seed to ensure reproducible results across multiple runs.
 */
public final class RandomContainerSizeResolver implements ContainerSizeResolver {
	private final int minSize;
	private final int maxSize;
	private final Random random;

	/**
	 * Creates a RandomContainerSizeResolver with the specified range and seed.
	 *
	 * @param minSize the minimum container size (inclusive)
	 * @param maxSize the maximum container size (inclusive)
	 * @param seed the random seed for reproducible results
	 * @throws IllegalArgumentException if minSize is negative or maxSize is less than minSize
	 */
	public RandomContainerSizeResolver(int minSize, int maxSize, long seed) {
		if (minSize < 0) {
			throw new IllegalArgumentException("minSize must be non-negative, but was: " + minSize);
		}
		if (maxSize < minSize) {
			throw new IllegalArgumentException("maxSize must be >= minSize, but was: " + maxSize + " < " + minSize);
		}

		this.minSize = minSize;
		this.maxSize = maxSize;
		this.random = new Random(seed);
	}

	/**
	 * Creates a RandomContainerSizeResolver with the specified range and default seed.
	 *
	 * @param minSize the minimum container size (inclusive)
	 * @param maxSize the maximum container size (inclusive)
	 * @throws IllegalArgumentException if minSize is negative or maxSize is less than minSize
	 */
	public RandomContainerSizeResolver(int minSize, int maxSize) {
		this(minSize, maxSize, System.currentTimeMillis());
	}

	/**
	 * Creates a RandomContainerSizeResolver with default range (0-3) and the specified seed.
	 *
	 * @param seed the random seed for reproducible results
	 */
	public RandomContainerSizeResolver(long seed) {
		this(0, 3, seed);
	}

	/**
	 * Creates a RandomContainerSizeResolver with default range (0-3) and default seed.
	 */
	public RandomContainerSizeResolver() {
		this(0, 3);
	}

	@Override
	public int resolveContainerSize(JvmType containerType) {
		if (minSize == maxSize) {
			return minSize;
		}
		return random.nextInt(maxSize - minSize + 1) + minSize;
	}
}

