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

import java.math.BigInteger;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.arbitrary.BigIntegerCombinableArbitrary;

@API(since = "1.1.16", status = Status.EXPERIMENTAL)
public final class JqwikBigIntegerCombinableArbitrary implements BigIntegerCombinableArbitrary {
	private static final BigInteger TWO = BigInteger.valueOf(2);

	private final Arbitrary<BigInteger> bigIntegerArbitrary;

	public JqwikBigIntegerCombinableArbitrary() {
		this(Arbitraries.bigIntegers());
	}

	private JqwikBigIntegerCombinableArbitrary(Arbitrary<BigInteger> bigIntegerArbitrary) {
		this.bigIntegerArbitrary = bigIntegerArbitrary;
	}

	@Override
	public BigInteger combined() {
		return this.bigIntegerArbitrary.sample();
	}

	@Override
	public BigInteger rawValue() {
		return this.combined();
	}

	@Override
	public BigIntegerCombinableArbitrary withRange(BigInteger min, BigInteger max) {
		return new JqwikBigIntegerCombinableArbitrary(
			Arbitraries.bigIntegers().between(min, max)
		);
	}

	@Override
	public BigIntegerCombinableArbitrary positive() {
		return new JqwikBigIntegerCombinableArbitrary(
			Arbitraries.bigIntegers().greaterOrEqual(BigInteger.ONE)
		);
	}

	@Override
	public BigIntegerCombinableArbitrary negative() {
		return new JqwikBigIntegerCombinableArbitrary(
			Arbitraries.bigIntegers().lessOrEqual(BigInteger.valueOf(-1))
		);
	}

	@Override
	public BigIntegerCombinableArbitrary nonZero() {
		return new JqwikBigIntegerCombinableArbitrary(
			this.bigIntegerArbitrary.filter(it -> !it.equals(BigInteger.ZERO))
		);
	}

	@Override
	public BigIntegerCombinableArbitrary percentage() {
		return new JqwikBigIntegerCombinableArbitrary(
			Arbitraries.bigIntegers().between(BigInteger.ZERO, BigInteger.valueOf(100))
		);
	}

	@Override
	public BigIntegerCombinableArbitrary score() {
		return new JqwikBigIntegerCombinableArbitrary(
			Arbitraries.bigIntegers().between(BigInteger.ZERO, BigInteger.valueOf(100))
		);
	}

	@Override
	public BigIntegerCombinableArbitrary score(BigInteger min, BigInteger max) {
		return withRange(min, max);
	}

	@Override
	public BigIntegerCombinableArbitrary even() {
		return new JqwikBigIntegerCombinableArbitrary(
			Arbitraries.bigIntegers().filter(it -> it.remainder(TWO).equals(BigInteger.ZERO))
		);
	}

	@Override
	public BigIntegerCombinableArbitrary odd() {
		return new JqwikBigIntegerCombinableArbitrary(
			Arbitraries.bigIntegers().filter(it -> it.remainder(TWO).abs().equals(BigInteger.ONE))
		);
	}

	@Override
	public BigIntegerCombinableArbitrary multipleOf(BigInteger divisor) {
		return new JqwikBigIntegerCombinableArbitrary(
			Arbitraries.bigIntegers().map(it -> it.multiply(divisor))
		);
	}

	@Override
	public BigIntegerCombinableArbitrary prime() {
		return new JqwikBigIntegerCombinableArbitrary(
			Arbitraries.bigIntegers()
				.between(BigInteger.valueOf(2), BigInteger.valueOf(1000))
				.filter(it -> it.isProbablePrime(10))
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
