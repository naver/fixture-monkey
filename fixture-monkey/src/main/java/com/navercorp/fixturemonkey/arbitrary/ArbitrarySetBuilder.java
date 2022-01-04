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

import com.navercorp.fixturemonkey.ArbitraryBuilder;

public final class ArbitrarySetBuilder<T> extends AbstractArbitrarySet<T> {
	private final ArbitraryBuilder<T> builder;
	private long limit;

	public ArbitrarySetBuilder(ArbitraryExpression arbitraryExpression, ArbitraryBuilder<T> builder, long limit) {
		super(arbitraryExpression);
		this.builder = builder.copy();
		this.limit = limit;
	}

	public ArbitrarySetBuilder(ArbitraryExpression arbitraryExpression, ArbitraryBuilder<T> builder) {
		this(arbitraryExpression, builder, Long.MAX_VALUE);
	}

	@Nullable
	@Override
	public T getApplicableValue() {
		if (this.limit > 0) {
			limit--;
			return builder.sample();
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
		return builder;
	}

	@Override
	public ArbitrarySetBuilder<T> copy() {
		return new ArbitrarySetBuilder<>(this.getArbitraryExpression(), this.builder, this.limit);
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
		ArbitrarySetBuilder<?> that = (ArbitrarySetBuilder<?>)obj;
		return builder.equals(that.builder);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), builder);
	}
}
