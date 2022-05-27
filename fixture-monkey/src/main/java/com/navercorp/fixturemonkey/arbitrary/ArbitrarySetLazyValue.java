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

import javax.annotation.Nullable;

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

	public long getLimit() {
		return limit;
	}

	@Nullable
	@Override
	public T getApplicableValue() {
		if (this.limit > 0) {
			limit--;
			return getInputValue();
		} else {
			return null;
		}
	}

	@Override
	public boolean isApplicable() {
		return limit > 0;
	}

	public boolean isArbitraryValue() {
		return getInputValue() instanceof Arbitrary;
	}

	@Override
	public T getInputValue() {
		return supplier.get();
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
		return supplier.equals(that.supplier);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), getInputValue());
	}
}
