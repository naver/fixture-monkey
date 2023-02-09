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

package com.navercorp.fixturemonkey.api.generator;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.random.Randoms;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ArbitraryContainerInfo {
	private final int elementMinSize;
	private final int elementMaxSize;

	/**
	 * The sequence of a size manipulation.
	 * It may dismiss if a sequence of set is bigger.
	 */
	@Nullable
	private final Integer manipulatingSequence;

	/**
	 * Constructs an object represents a size of container.
	 * It may be manipulated or not manipulated depends on existence of manipulatedSequence.
	 *
	 * @param elementMinSize      the min size of the container
	 * @param elementMaxSize      the max size of the container
	 * @param manipulatingSequence the sequence of a size manipulation
	 */
	public ArbitraryContainerInfo(
		int elementMinSize,
		int elementMaxSize,
		@Nullable Integer manipulatingSequence
	) {
		this.elementMinSize = elementMinSize;
		this.elementMaxSize = elementMaxSize;
		this.manipulatingSequence = manipulatingSequence;
	}

	/**
	 * Constructs an object represents a size of container which is not manipulated.
	 *
	 * @param elementMinSize the min size of the container
	 * @param elementMaxSize the max size of the container
	 */
	public ArbitraryContainerInfo(int elementMinSize, int elementMaxSize) {
		this(elementMinSize, elementMaxSize, null);
	}

	public int getElementMinSize() {
		return this.elementMinSize;
	}

	public int getElementMaxSize() {
		return this.elementMaxSize;
	}

	public boolean isManipulated() {
		return manipulatingSequence != null;
	}

	@Nullable
	public Integer getManipulatingSequence() {
		return manipulatingSequence;
	}

	public int getRandomSize() {
		if (this.elementMinSize == this.elementMaxSize) {
			return this.elementMinSize;
		}

		return this.elementMinSize + Randoms.nextInt(this.elementMaxSize - this.elementMinSize + 1);
	}

	@Override
	public String toString() {
		return "ArbitraryPropertyContainerInfo{"
			+ "elementMinSize=" + elementMinSize
			+ ", elementMaxSize=" + elementMaxSize
			+ '}';
	}
}
