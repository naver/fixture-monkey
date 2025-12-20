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

import java.math.BigInteger;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "1.1.16", status = Status.EXPERIMENTAL)
final class BigIntegerCombinableArbitraryDelegator implements BigIntegerCombinableArbitrary {
	private final CombinableArbitrary<BigInteger> delegate;

	public BigIntegerCombinableArbitraryDelegator(CombinableArbitrary<BigInteger> delegate) {
		this.delegate = delegate;
	}

	@Override
	public BigInteger combined() {
		return delegate.combined();
	}

	@Override
	public BigInteger rawValue() {
		return delegate.combined();
	}

	@Override
	public BigIntegerCombinableArbitrary withRange(BigInteger min, BigInteger max) {
		return CombinableArbitrary.bigIntegers().withRange(min, max);
	}

	@Override
	public BigIntegerCombinableArbitrary positive() {
		return CombinableArbitrary.bigIntegers().positive();
	}

	@Override
	public BigIntegerCombinableArbitrary negative() {
		return CombinableArbitrary.bigIntegers().negative();
	}

	@Override
	public BigIntegerCombinableArbitrary nonZero() {
		return CombinableArbitrary.bigIntegers().nonZero();
	}

	@Override
	public BigIntegerCombinableArbitrary percentage() {
		return CombinableArbitrary.bigIntegers().percentage();
	}

	@Override
	public BigIntegerCombinableArbitrary score() {
		return CombinableArbitrary.bigIntegers().score();
	}

	@Override
	public BigIntegerCombinableArbitrary score(BigInteger min, BigInteger max) {
		return CombinableArbitrary.bigIntegers().score(min, max);
	}

	@Override
	public BigIntegerCombinableArbitrary even() {
		return CombinableArbitrary.bigIntegers().even();
	}

	@Override
	public BigIntegerCombinableArbitrary odd() {
		return CombinableArbitrary.bigIntegers().odd();
	}

	@Override
	public BigIntegerCombinableArbitrary multipleOf(BigInteger divisor) {
		return CombinableArbitrary.bigIntegers().multipleOf(divisor);
	}

	@Override
	public BigIntegerCombinableArbitrary prime() {
		return CombinableArbitrary.bigIntegers().prime();
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
