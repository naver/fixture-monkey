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

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.OldArbitraryBuilderImpl;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
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

	@SuppressWarnings({"rawtypes"})
	@Override
	public void accept(OldArbitraryBuilderImpl arbitraryBuilder) {
		ArbitraryExpression arbitraryExpression = this.getArbitraryExpression();
		T value = this.getApplicableValue();
		long limit = this.limit;
		BuilderManipulator builderManipulator;
		if (value instanceof Arbitrary) {
			builderManipulator = new ArbitrarySetArbitrary<>(arbitraryExpression, (Arbitrary<?>)value, limit);
		} else {
			builderManipulator = new ArbitrarySet<>(arbitraryExpression, value, limit);
		}
		arbitraryBuilder.apply(builderManipulator);
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

	@Override
	public T getInputValue() {
		return supplier.get();
	}

	public long getLimit() {
		return limit;
	}

	@Override
	public ArbitrarySetLazyValue<T> copy() {
		return this;
	}
}
