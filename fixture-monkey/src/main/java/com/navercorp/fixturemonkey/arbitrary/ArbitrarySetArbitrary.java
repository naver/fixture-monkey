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

import javax.annotation.Nullable;

import net.jqwik.api.Arbitrary;

public final class ArbitrarySetArbitrary<T> extends AbstractArbitrarySet<T> {
	private final Arbitrary<T> value;
	private long limit;

	public ArbitrarySetArbitrary(ArbitraryExpression arbitraryExpression, Arbitrary<T> value, long limit) {
		super(arbitraryExpression);
		this.value = value;
		this.limit = limit;
	}

	public ArbitrarySetArbitrary(ArbitraryExpression arbitraryExpression, Arbitrary<T> value) {
		this(arbitraryExpression, value, Long.MAX_VALUE);
	}

	@Nullable
	@Override
	public T getValue() {
		if (this.limit > 0) {
			limit--;
			return value.sample();
		}
		return null;
	}

	@Override
	public Object getRawValue() {
		return value;
	}

	@SuppressWarnings("unchecked")
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
		ArbitrarySetArbitrary<T> that = (ArbitrarySetArbitrary<T>)obj;
		// can not equal, can not apply caching
		return value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), value);
	}

	@Override
	public ArbitrarySetArbitrary<T> copy() {
		return new ArbitrarySetArbitrary<>(this.getArbitraryExpression(), this.value);
	}
}
