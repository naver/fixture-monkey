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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "1.1.15", status = Status.EXPERIMENTAL)
final class LongCombinableArbitraryDelegator implements LongCombinableArbitrary {
	private final CombinableArbitrary<Long> delegate;

	public LongCombinableArbitraryDelegator(CombinableArbitrary<Long> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Long combined() {
		return delegate.combined();
	}

	@Override
	public Long rawValue() {
		return delegate.combined();
	}

	@Override
	public LongCombinableArbitrary withRange(long min, long max) {
		return CombinableArbitrary.longs().withRange(min, max);
	}

	@Override
	public LongCombinableArbitrary positive() {
		return CombinableArbitrary.longs().positive();
	}

	@Override
	public LongCombinableArbitrary negative() {
		return CombinableArbitrary.longs().negative();
	}

	@Override
	public LongCombinableArbitrary even() {
		return CombinableArbitrary.longs().even();
	}

	@Override
	public LongCombinableArbitrary odd() {
		return CombinableArbitrary.longs().odd();
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
