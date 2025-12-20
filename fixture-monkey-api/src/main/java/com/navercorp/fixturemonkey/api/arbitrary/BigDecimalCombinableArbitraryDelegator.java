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

import java.math.BigDecimal;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "1.1.16", status = Status.EXPERIMENTAL)
final class BigDecimalCombinableArbitraryDelegator implements BigDecimalCombinableArbitrary {
	private final CombinableArbitrary<BigDecimal> delegate;

	public BigDecimalCombinableArbitraryDelegator(CombinableArbitrary<BigDecimal> delegate) {
		this.delegate = delegate;
	}

	@Override
	public BigDecimal combined() {
		return delegate.combined();
	}

	@Override
	public BigDecimal rawValue() {
		return delegate.combined();
	}

	@Override
	public BigDecimalCombinableArbitrary withRange(BigDecimal min, BigDecimal max) {
		return CombinableArbitrary.bigDecimals().withRange(min, max);
	}

	@Override
	public BigDecimalCombinableArbitrary positive() {
		return CombinableArbitrary.bigDecimals().positive();
	}

	@Override
	public BigDecimalCombinableArbitrary negative() {
		return CombinableArbitrary.bigDecimals().negative();
	}

	@Override
	public BigDecimalCombinableArbitrary nonZero() {
		return CombinableArbitrary.bigDecimals().nonZero();
	}

	@Override
	public BigDecimalCombinableArbitrary percentage() {
		return CombinableArbitrary.bigDecimals().percentage();
	}

	@Override
	public BigDecimalCombinableArbitrary score() {
		return CombinableArbitrary.bigDecimals().score();
	}

	@Override
	public BigDecimalCombinableArbitrary score(BigDecimal min, BigDecimal max) {
		return CombinableArbitrary.bigDecimals().score(min, max);
	}

	@Override
	public BigDecimalCombinableArbitrary withPrecision(int precision) {
		return CombinableArbitrary.bigDecimals().withPrecision(precision);
	}

	@Override
	public BigDecimalCombinableArbitrary withScale(int scale) {
		return CombinableArbitrary.bigDecimals().withScale(scale);
	}

	@Override
	public BigDecimalCombinableArbitrary normalized() {
		return CombinableArbitrary.bigDecimals().normalized();
	}

	@Override
	public BigDecimalCombinableArbitrary stripTrailingZeros() {
		return CombinableArbitrary.bigDecimals().stripTrailingZeros();
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
