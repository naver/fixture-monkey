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

package com.navercorp.fixturemonkey.api.lazy;

import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class UnSafeLazyArbitraryImpl<T> implements LazyArbitrary<T> {
	private static final Object UNINITIALIZED_VALUE = new Object();

	private final Supplier<T> initializer;
	private final boolean fixed;

	private Object value = UNINITIALIZED_VALUE;

	@SuppressWarnings("assignment")
	UnSafeLazyArbitraryImpl(T value) {
		this.value = value;
		this.initializer = () -> value;
		this.fixed = true;
	}

	UnSafeLazyArbitraryImpl(Supplier<T> initializer, boolean fixed) {
		this.initializer = initializer;
		this.fixed = fixed;
	}

	UnSafeLazyArbitraryImpl(Supplier<T> initializer) {
		this(initializer, false);
	}

	@SuppressWarnings({"unchecked", "assignment"})
	public T getValue() {
		if (value == UNINITIALIZED_VALUE) {
			value = initializer.get();
		}
		return (T)value;
	}

	@Override
	public boolean isInitialized() {
		return value != UNINITIALIZED_VALUE;
	}

	public void clear() {
		if (!fixed) {
			this.value = UNINITIALIZED_VALUE;
		}
	}
}
