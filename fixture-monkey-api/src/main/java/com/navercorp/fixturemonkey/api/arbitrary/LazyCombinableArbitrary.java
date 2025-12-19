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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;

/**
 * It would generate an arbitrary object.
 * A generated object would be changed when {@link #combined()} and {@link #rawValue()} is called.
 */
@API(since = "0.5.0", status = Status.MAINTAINED)
final class LazyCombinableArbitrary<T> implements CombinableArbitrary<T> {
	private final LazyArbitrary<T> introspected;

	LazyCombinableArbitrary(LazyArbitrary<T> introspected) {
		this.introspected = introspected;
	}

	@Override
	public T combined() {
		T combined = introspected.getValue();
		introspected.clear();
		return combined;
	}

	@Override
	@SuppressWarnings("return")
	public Object rawValue() {
		Object rawValue = introspected.getValue();
		introspected.clear();
		return rawValue;
	}

	@Override
	public void clear() {
		introspected.clear();
	}

	@Override
	public boolean fixed() {
		return false;
	}
}
