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

@API(since = "1.1.16", status = Status.EXPERIMENTAL)
final class ShortCombinableArbitraryDelegator implements ShortCombinableArbitrary {
	private final CombinableArbitrary<Short> delegate;

	public ShortCombinableArbitraryDelegator(CombinableArbitrary<Short> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Short combined() {
		return delegate.combined();
	}

	@Override
	public Short rawValue() {
		return delegate.combined();
	}

	@Override
	public ShortCombinableArbitrary withRange(short min, short max) {
		return CombinableArbitrary.shorts().withRange(min, max);
	}

	@Override
	public ShortCombinableArbitrary positive() {
		return CombinableArbitrary.shorts().positive();
	}

	@Override
	public ShortCombinableArbitrary negative() {
		return CombinableArbitrary.shorts().negative();
	}

	@Override
	public ShortCombinableArbitrary even() {
		return CombinableArbitrary.shorts().even();
	}

	@Override
	public ShortCombinableArbitrary odd() {
		return CombinableArbitrary.shorts().odd();
	}

	@Override
	public ShortCombinableArbitrary nonZero() {
		return CombinableArbitrary.shorts().nonZero();
	}

	@Override
	public ShortCombinableArbitrary multipleOf(short value) {
		return CombinableArbitrary.shorts().multipleOf(value);
	}

	@Override
	public ShortCombinableArbitrary percentage() {
		return CombinableArbitrary.shorts().percentage();
	}

	@Override
	public ShortCombinableArbitrary score() {
		return CombinableArbitrary.shorts().score();
	}

	@Override
	public ShortCombinableArbitrary year() {
		return CombinableArbitrary.shorts().year();
	}

	@Override
	public ShortCombinableArbitrary month() {
		return CombinableArbitrary.shorts().month();
	}

	@Override
	public ShortCombinableArbitrary day() {
		return CombinableArbitrary.shorts().day();
	}

	@Override
	public ShortCombinableArbitrary hour() {
		return CombinableArbitrary.shorts().hour();
	}

	@Override
	public ShortCombinableArbitrary minute() {
		return CombinableArbitrary.shorts().minute();
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
