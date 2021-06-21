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

import static com.navercorp.fixturemonkey.Constants.DEFAULT_ELEMENT_MAX_LIMIT;
import static com.navercorp.fixturemonkey.Constants.DEFAULT_ELEMENT_MAX_SIZE;
import static com.navercorp.fixturemonkey.Constants.DEFAULT_ELEMENT_MIN_LIMIT;
import static com.navercorp.fixturemonkey.Constants.DEFAULT_ELEMENT_MIN_SIZE;

import java.util.function.Function;

import javax.annotation.Nullable;

import net.jqwik.api.Arbitraries;

public final class ContainerSizeConstraint {
	@Nullable
	private final Integer minSize;

	@Nullable
	private final Integer maxSize;

	public ContainerSizeConstraint(@Nullable Integer minSize, @Nullable Integer maxSize) {
		this.minSize = ifNotNullApply(minSize, it -> Math.max(DEFAULT_ELEMENT_MIN_LIMIT, it));
		this.maxSize = ifNotNullApply(maxSize, it -> Math.min(DEFAULT_ELEMENT_MAX_LIMIT, it));
	}

	public int getMinSize() {
		return minSize == null ? DEFAULT_ELEMENT_MIN_SIZE : minSize;
	}

	public int getMaxSize() {
		return maxSize == null ? DEFAULT_ELEMENT_MAX_SIZE : maxSize;
	}

	public ContainerSizeConstraint copy() {
		return new ContainerSizeConstraint(this.minSize, this.maxSize);
	}

	public int getArbitraryElementSize() {
		return Arbitraries.integers().between(getMinSize(), getMaxSize()).sample();
	}

	private <T, U> U ifNotNullApply(T object, Function<T, U> function) {
		if (object == null) {
			return null;
		}
		return function.apply(object);
	}
}
