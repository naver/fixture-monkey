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

package com.navercorp.fixturemonkey.arbitrary;

import static com.navercorp.fixturemonkey.Constants.DEFAULT_ELEMENT_MAX_SIZE;
import static com.navercorp.fixturemonkey.Constants.DEFAULT_ELEMENT_MIN_SIZE;

import javax.annotation.Nullable;

import net.jqwik.api.Arbitraries;

public final class ContainerSizeConstraint {
	@Nullable
	private final Integer minSize;
	@Nullable
	private final Integer maxSize;
	private final boolean minManipulated;
	private final boolean maxManipulated;

	public ContainerSizeConstraint(@Nullable Integer minSize, @Nullable Integer maxSize) {
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.minManipulated = false;
		this.maxManipulated = false;
	}

	public ContainerSizeConstraint(
		@Nullable Integer minSize,
		@Nullable Integer maxSize,
		boolean minManipulated,
		boolean maxManipulated
	) {
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.minManipulated = minManipulated;
		this.maxManipulated = maxManipulated;
	}

	public int getMinSize() {
		return minSize == null ? DEFAULT_ELEMENT_MIN_SIZE : minSize;
	}

	public int getMaxSize() {
		if (maxSize == null
			&& minSize != null && minSize > DEFAULT_ELEMENT_MAX_SIZE
		) {
			return minSize + DEFAULT_ELEMENT_MAX_SIZE;
		}
		return maxSize == null ? DEFAULT_ELEMENT_MAX_SIZE : maxSize;
	}

	public ContainerSizeConstraint copy() {
		return new ContainerSizeConstraint(this.minSize, this.maxSize);
	}

	public int getArbitraryElementSize() {
		return Arbitraries.integers().between(getMinSize(), getMaxSize()).sample();
	}

	public ContainerSizeConstraint withMinSize(@Nullable Integer minSize) {
		Integer manipulatedMaxSize = this.maxManipulated ? this.maxSize : null;
		if (minSize == null && this.minSize != null) {
			return new ContainerSizeConstraint(this.minSize, manipulatedMaxSize, true, this.maxManipulated);
		}
		return new ContainerSizeConstraint(minSize, manipulatedMaxSize, true, this.maxManipulated);
	}

	public ContainerSizeConstraint withMaxSize(@Nullable Integer maxSize) {
		Integer manipulatedMinSize = this.minManipulated ? this.minSize : null;
		if (maxSize == null && this.maxSize != null) {
			return new ContainerSizeConstraint(manipulatedMinSize, this.maxSize, this.minManipulated, true);
		}
		return new ContainerSizeConstraint(manipulatedMinSize, maxSize, this.minManipulated, true);

	}
}
