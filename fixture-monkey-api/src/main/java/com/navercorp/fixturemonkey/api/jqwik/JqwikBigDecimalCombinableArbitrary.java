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

package com.navercorp.fixturemonkey.api.jqwik;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.arbitrary.BigDecimalCombinableArbitrary;

@API(since = "1.1.16", status = Status.EXPERIMENTAL)
public final class JqwikBigDecimalCombinableArbitrary implements BigDecimalCombinableArbitrary {
	private final Arbitrary<BigDecimal> bigDecimalArbitrary;

	public JqwikBigDecimalCombinableArbitrary() {
		this(Arbitraries.bigDecimals());
	}

	private JqwikBigDecimalCombinableArbitrary(Arbitrary<BigDecimal> bigDecimalArbitrary) {
		this.bigDecimalArbitrary = bigDecimalArbitrary;
	}

	@Override
	public BigDecimal combined() {
		return this.bigDecimalArbitrary.sample();
	}

	@Override
	public BigDecimal rawValue() {
		return this.combined();
	}

	@Override
	public BigDecimalCombinableArbitrary withRange(BigDecimal min, BigDecimal max) {
		return new JqwikBigDecimalCombinableArbitrary(
			Arbitraries.bigDecimals().between(min, max)
		);
	}

	@Override
	public BigDecimalCombinableArbitrary positive() {
		return new JqwikBigDecimalCombinableArbitrary(
			Arbitraries.bigDecimals().greaterOrEqual(BigDecimal.ONE)
		);
	}

	@Override
	public BigDecimalCombinableArbitrary negative() {
		return new JqwikBigDecimalCombinableArbitrary(
			Arbitraries.bigDecimals().lessOrEqual(BigDecimal.valueOf(-1))
		);
	}

	@Override
	public BigDecimalCombinableArbitrary nonZero() {
		return new JqwikBigDecimalCombinableArbitrary(
			this.bigDecimalArbitrary.filter(it -> it.compareTo(BigDecimal.ZERO) != 0)
		);
	}

	@Override
	public BigDecimalCombinableArbitrary percentage() {
		return new JqwikBigDecimalCombinableArbitrary(
			Arbitraries.bigDecimals().between(BigDecimal.ZERO, BigDecimal.valueOf(100))
		);
	}

	@Override
	public BigDecimalCombinableArbitrary score() {
		return new JqwikBigDecimalCombinableArbitrary(
			Arbitraries.bigDecimals().between(BigDecimal.ZERO, BigDecimal.valueOf(100))
		);
	}

	@Override
	public BigDecimalCombinableArbitrary score(BigDecimal min, BigDecimal max) {
		return withRange(min, max);
	}

	@Override
	public BigDecimalCombinableArbitrary withPrecision(int precision) {
		return new JqwikBigDecimalCombinableArbitrary(
			this.bigDecimalArbitrary.map(it -> it.round(new java.math.MathContext(precision)))
		);
	}

	@Override
	public BigDecimalCombinableArbitrary withScale(int scale) {
		return new JqwikBigDecimalCombinableArbitrary(
			this.bigDecimalArbitrary.map(it -> it.setScale(scale, RoundingMode.HALF_UP))
		);
	}

	@Override
	public BigDecimalCombinableArbitrary normalized() {
		return new JqwikBigDecimalCombinableArbitrary(
			Arbitraries.bigDecimals().between(BigDecimal.ZERO, BigDecimal.ONE)
		);
	}

	@Override
	public BigDecimalCombinableArbitrary stripTrailingZeros() {
		return new JqwikBigDecimalCombinableArbitrary(
			this.bigDecimalArbitrary.map(BigDecimal::stripTrailingZeros)
		);
	}

	@Override
	public void clear() {
		// ignored
	}

	@Override
	public boolean fixed() {
		return false;
	}
}
