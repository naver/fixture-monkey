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

import java.util.Objects;
import java.util.function.Supplier;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

public final class ArbitrarySetLazyValue<T> extends AbstractArbitrarySet<T> {
	private final Supplier<T> supplier;
	private long limit;

	public ArbitrarySetLazyValue(ArbitraryExpression arbitraryExpression, Supplier<T> supplier, long limit) {
		super(arbitraryExpression);
		this.supplier = supplier;
		this.limit = limit;
	}

	public ArbitrarySetLazyValue(ArbitraryExpression arbitraryExpression, Supplier<T> supplier) {
		this(arbitraryExpression, supplier, Long.MAX_VALUE);
	}

	@Override
	public T getValue() {
		return supplier.get();
	}

	@Override
	public Arbitrary<T> apply(Arbitrary<T> from) {
		if (this.limit > 0) {
			limit--;
			return Arbitraries.just(getValue());
		} else {
			return from;
		}
	}

	@Override
	public ArbitrarySetLazyValue<T> copy() {
		return new ArbitrarySetLazyValue<>(this.getArbitraryExpression(), this.supplier, this.limit);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		if (!super.equals(obj)) {
			return false;
		}
		ArbitrarySetLazyValue<?> that = (ArbitrarySetLazyValue<?>)obj;
		return getValue().equals(that.getValue());
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), getValue());
	}
}
