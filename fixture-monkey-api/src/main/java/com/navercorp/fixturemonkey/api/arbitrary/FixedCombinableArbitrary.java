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

package com.navercorp.fixturemonkey.api.arbitrary;

import java.util.Objects;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

/**
 * It would generate a fixed value {@code object}.
 */
@API(since = "0.6.0", status = Status.MAINTAINED)
final class FixedCombinableArbitrary<T> implements CombinableArbitrary<T> {
	private final T object;

	FixedCombinableArbitrary(T object) {
		this.object = object;
	}

	@Override
	public T combined() {
		return object;
	}

	@Override
	@SuppressWarnings("override.return")
	public T rawValue() {
		return object;
	}

	@Override
	public void clear() {
	}

	@Override
	public boolean fixed() {
		return true;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		FixedCombinableArbitrary<?> that = (FixedCombinableArbitrary<?>)obj;
		return Objects.equals(object, that.object);
	}

	@Override
	public int hashCode() {
		return Objects.hash(object);
	}
}
