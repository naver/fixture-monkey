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

package com.navercorp.fixturemonkey.api.arbitrary;

final class IntegerCombinableArbitraryDelegator implements IntegerCombinableArbitrary {
	private final CombinableArbitrary<Integer> delegate;

	public IntegerCombinableArbitraryDelegator(CombinableArbitrary<Integer> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Integer combined() {
		return delegate.combined();
	}

	@Override
	public Integer rawValue() {
		return delegate.combined();
	}

	@Override
	public IntegerCombinableArbitrary withRange(int min, int max) {
		return CombinableArbitrary.integers().withRange(min, max);
	}

	@Override
	public IntegerCombinableArbitrary positive() {
		return CombinableArbitrary.integers().positive();
	}

	@Override
	public IntegerCombinableArbitrary negative() {
		return CombinableArbitrary.integers().negative();
	}

	@Override
	public IntegerCombinableArbitrary even() {
		return CombinableArbitrary.integers().even();
	}

	@Override
	public IntegerCombinableArbitrary odd() {
		return CombinableArbitrary.integers().odd();
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public boolean fixed() {
		return delegate.fixed();
	}
}
