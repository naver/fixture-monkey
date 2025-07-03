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
final class ByteCombinableArbitraryDelegator implements ByteCombinableArbitrary {
	private final CombinableArbitrary<Byte> delegate;

	public ByteCombinableArbitraryDelegator(CombinableArbitrary<Byte> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Byte combined() {
		return delegate.combined();
	}

	@Override
	public Byte rawValue() {
		return delegate.combined();
	}

	@Override
	public ByteCombinableArbitrary withRange(byte min, byte max) {
		return CombinableArbitrary.bytes().withRange(min, max);
	}

	@Override
	public ByteCombinableArbitrary positive() {
		return CombinableArbitrary.bytes().positive();
	}

	@Override
	public ByteCombinableArbitrary negative() {
		return CombinableArbitrary.bytes().negative();
	}

	@Override
	public ByteCombinableArbitrary even() {
		return CombinableArbitrary.bytes().even();
	}

	@Override
	public ByteCombinableArbitrary odd() {
		return CombinableArbitrary.bytes().odd();
	}

	@Override
	public ByteCombinableArbitrary ascii() {
		return CombinableArbitrary.bytes().ascii();
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
