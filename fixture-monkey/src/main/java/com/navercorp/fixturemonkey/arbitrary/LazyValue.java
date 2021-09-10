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

import java.util.function.Supplier;

public class LazyValue<T> {
	private T value;
	private final Supplier<T> supplier;
	private final boolean fixed;

	public LazyValue(T value) {
		this.value = value;
		this.supplier = () -> value;
		this.fixed = true;
	}

	public LazyValue(Supplier<T> supplier, boolean fixed) {
		this.supplier = supplier;
		this.fixed = fixed;
	}

	public LazyValue(Supplier<T> supplier) {
		this.supplier = supplier;
		this.fixed = false;
	}

	@SuppressWarnings("unchecked")
	public ArbitraryType<T> getArbitraryType() {
		if (get() == null) {
			return NullArbitraryType.INSTANCE;
		}
		return new ArbitraryType<>((Class<T>)get().getClass());
	}

	public T get() {
		if (value == null) {
			value = supplier.get();
		}
		return value;
	}

	public boolean isEmpty() {
		return get() == null;
	}

	public void clear() {
		if (!fixed) {
			this.value = null;
		}
	}
}
