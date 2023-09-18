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

@API(since = "0.6.9", status = Status.EXPERIMENTAL)
public final class SynchronizedLazyArbitraryImpl<T> implements LazyArbitrary<T> {
	private static final Object UNINITIALIZED_VALUE = new Object();

	private final Supplier<T> initializer;
	private final boolean fixed;

	private volatile Object value = UNINITIALIZED_VALUE;
	private final Object lock = this;

	SynchronizedLazyArbitraryImpl(T value) {
		this.value = value;
		this.initializer = () -> value;
		this.fixed = true;
	}

	SynchronizedLazyArbitraryImpl(Supplier<T> initializer, boolean fixed) {
		this.initializer = initializer;
		this.fixed = fixed;
	}

	SynchronizedLazyArbitraryImpl(Supplier<T> initializer) {
		this(initializer, false);
	}

	@SuppressWarnings("unchecked")
	public T getValue() {
		Object firstReturned = value;
		if (firstReturned != UNINITIALIZED_VALUE) {
			return (T)firstReturned;
		}

		synchronized (lock) {
			Object secondReturned = value;
			if (secondReturned != UNINITIALIZED_VALUE) {
				return (T)secondReturned;
			}
			Object returned = initializer.get();
			value = returned;
			return (T)returned;
		}
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
