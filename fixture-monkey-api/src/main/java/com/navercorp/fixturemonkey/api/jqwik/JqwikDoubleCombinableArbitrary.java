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

import com.navercorp.fixturemonkey.api.arbitrary.DoubleCombinableArbitrary;

@API(since = "1.1.16", status = Status.EXPERIMENTAL)
public final class JqwikDoubleCombinableArbitrary implements DoubleCombinableArbitrary {
	private final Arbitrary<Double> doubleArbitrary;

	public JqwikDoubleCombinableArbitrary() {
		this.doubleArbitrary = Arbitraries.doubles();
	}

	private JqwikDoubleCombinableArbitrary(Arbitrary<Double> doubleArbitrary) {
		this.doubleArbitrary = doubleArbitrary;
	}

	@Override
	public Double rawValue() {
		return this.doubleArbitrary.sample();
	}

	@Override
	public DoubleCombinableArbitrary withRange(double minValue, double maxValue) {
		return new JqwikDoubleCombinableArbitrary(
			Arbitraries.doubles().between(minValue, maxValue)
		);
	}

	@Override
	public DoubleCombinableArbitrary positive() {
		return new JqwikDoubleCombinableArbitrary(
			Arbitraries.doubles().greaterThan(0.0)
		);
	}

	@Override
	public DoubleCombinableArbitrary negative() {
		return new JqwikDoubleCombinableArbitrary(
			Arbitraries.doubles().lessThan(0.0)
		);
	}

	@Override
	public DoubleCombinableArbitrary nonZero() {
		return new JqwikDoubleCombinableArbitrary(
			Arbitraries.doubles().filter(d -> d != 0.0)
		);
	}

	@Override
	public DoubleCombinableArbitrary withPrecision(int scale) {
		return new JqwikDoubleCombinableArbitrary(
			this.doubleArbitrary.map(d -> BigDecimal.valueOf(d)
				.setScale(scale, RoundingMode.HALF_UP)
				.doubleValue())
		);
	}

	@Override
	public DoubleCombinableArbitrary finite() {
		return new JqwikDoubleCombinableArbitrary(
			Arbitraries.doubles().filter(d -> Double.isFinite(d))
		);
	}

	@Override
	public DoubleCombinableArbitrary infinite() {
		return new JqwikDoubleCombinableArbitrary(
			Arbitraries.of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)
		);
	}

	@Override
	public DoubleCombinableArbitrary normalized() {
		return new JqwikDoubleCombinableArbitrary(
			Arbitraries.doubles().between(0.0, 1.0)
		);
	}

	@Override
	public DoubleCombinableArbitrary nan() {
		return new JqwikDoubleCombinableArbitrary(
			Arbitraries.of(Double.NaN)
		);
	}

	@Override
	public DoubleCombinableArbitrary percentage() {
		return new JqwikDoubleCombinableArbitrary(
			Arbitraries.doubles().between(0.0, 100.0)
		);
	}

	@Override
	public DoubleCombinableArbitrary score() {
		return new JqwikDoubleCombinableArbitrary(
			Arbitraries.doubles().between(0.0, 100.0)
		);
	}

	@Override
	public DoubleCombinableArbitrary withSpecialValue(double special) {
		return new JqwikDoubleCombinableArbitrary(
			Arbitraries.oneOf(this.doubleArbitrary, Arbitraries.of(special))
		);
	}

	@Override
	public DoubleCombinableArbitrary withStandardSpecialValues() {
		return new JqwikDoubleCombinableArbitrary(
			Arbitraries.oneOf(
				this.doubleArbitrary,
				Arbitraries.of(
					Double.NaN,
					Double.MIN_VALUE,
					Double.MIN_NORMAL,
					Double.POSITIVE_INFINITY,
					Double.NEGATIVE_INFINITY
				)
			)
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

	@Override
	public Double combined() {
		return this.doubleArbitrary.sample();
	}
}
