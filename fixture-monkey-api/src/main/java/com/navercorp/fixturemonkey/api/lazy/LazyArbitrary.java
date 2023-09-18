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

/**
 * Represents an arbitrary value with lazy initialization.
 * <p>
 * Similar to <a href="https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-lazy/">Kotlin Lazy</a>
 * But it could initialize value multiple times if call clear() method.
 * Due to supporting {@link net.jqwik.api.Arbitrary}.
 **/
@API(since = "0.4.0", status = Status.MAINTAINED)
public interface LazyArbitrary<T> {
	T getValue();

	boolean isInitialized();

	void clear();

	static <T> LazyArbitrary<T> lazy(Supplier<T> initializer, boolean fixed, LazyThreadSafetyMode mode) {
		if (mode == LazyThreadSafetyMode.NONE) {
			return new UnSafeLazyArbitraryImpl<>(initializer, fixed);
		} else if (mode == LazyThreadSafetyMode.SYNCHRONIZED) {
			return new SynchronizedLazyArbitraryImpl<>(initializer, fixed);
		}
		throw new IllegalArgumentException("Unsupported lazy thread safety mode: " + mode);
	}

	static <T> LazyArbitrary<T> lazy(Supplier<T> initializer, LazyThreadSafetyMode mode) {
		return lazy(initializer, false, mode);
	}

	static <T> LazyArbitrary<T> lazy(Supplier<T> initializer) {
		return lazy(initializer, LazyThreadSafetyMode.NONE);
	}

	static <T> LazyArbitrary<T> lazy(Supplier<T> initializer, boolean fixed) {
		return lazy(initializer, fixed, LazyThreadSafetyMode.NONE);
	}

	enum LazyThreadSafetyMode {
		NONE,
		SYNCHRONIZED,
	}
}
