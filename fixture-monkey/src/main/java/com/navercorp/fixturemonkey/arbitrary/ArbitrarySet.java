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

import static com.navercorp.fixturemonkey.Constants.MAX_MANIPULATION_COUNT;

import java.util.Objects;

import javax.annotation.Nullable;

public final class ArbitrarySet<T> extends AbstractArbitrarySet<T> {
	private final T value;
	private long limit;

	public ArbitrarySet(ArbitraryExpression arbitraryExpression, T value, long limit) {
		super(arbitraryExpression);
		this.value = value;
		this.limit = limit;
	}

	public ArbitrarySet(ArbitraryExpression arbitraryExpression, T value) {
		this(arbitraryExpression, value, MAX_MANIPULATION_COUNT);
	}

	@Nullable
	@Override
	public T getApplicableValue() {
		if (this.limit > 0) {
			limit--;
			return value;
		} else {
			return null;
		}
	}

	@Override
	public boolean isApplicable() {
		return limit > 0;
	}

	@Override
	public Object getInputValue() {
		return value;
	}

	public long getLimit() {
		return limit;
	}


	@Override
	public ArbitrarySet<T> copy() {
		return new ArbitrarySet<>(this.getArbitraryExpression(), this.value, this.limit);
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
		ArbitrarySet<?> that = (ArbitrarySet<?>)obj;
		return value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), value);
	}
}
