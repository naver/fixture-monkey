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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.arbitrary.FloatCombinableArbitrary;

@API(since = "1.1.16", status = Status.EXPERIMENTAL)
public final class JqwikFloatCombinableArbitrary implements FloatCombinableArbitrary {
	private final Arbitrary<Float> floatArbitrary;

	public JqwikFloatCombinableArbitrary() {
		this.floatArbitrary = Arbitraries.floats();
	}

	private JqwikFloatCombinableArbitrary(Arbitrary<Float> floatArbitrary) {
		this.floatArbitrary = floatArbitrary;
	}

	@Override
	public Float rawValue() {
		return this.floatArbitrary.sample();
	}

	@Override
	public FloatCombinableArbitrary withRange(float minValue, float maxValue) {
		return new JqwikFloatCombinableArbitrary(
			Arbitraries.floats().between(minValue, maxValue)
		);
	}

	@Override
	public FloatCombinableArbitrary positive() {
		return new JqwikFloatCombinableArbitrary(
			Arbitraries.floats().greaterThan(0.0f)
		);
	}

	@Override
	public FloatCombinableArbitrary negative() {
		return new JqwikFloatCombinableArbitrary(
			Arbitraries.floats().lessThan(0.0f)
		);
	}

	@Override
	public FloatCombinableArbitrary nonZero() {
		return new JqwikFloatCombinableArbitrary(
			Arbitraries.floats().filter(f -> f != 0.0f)
		);
	}

	@Override
	public FloatCombinableArbitrary withPrecision(int scale) {
		return new JqwikFloatCombinableArbitrary(
			Arbitraries.floats().ofScale(scale)
		);
	}

	@Override
	public FloatCombinableArbitrary finite() {
		return new JqwikFloatCombinableArbitrary(
			Arbitraries.floats().filter(f -> Float.isFinite(f))
		);
	}

	@Override
	public FloatCombinableArbitrary infinite() {
		return new JqwikFloatCombinableArbitrary(
			Arbitraries.of(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)
		);
	}

	@Override
	public FloatCombinableArbitrary normalized() {
		return new JqwikFloatCombinableArbitrary(
			Arbitraries.floats().between(0.0f, 1.0f)
		);
	}

	@Override
	public FloatCombinableArbitrary nan() {
		return new JqwikFloatCombinableArbitrary(
			Arbitraries.of(Float.NaN)
		);
	}

	@Override
	public FloatCombinableArbitrary percentage() {
		return new JqwikFloatCombinableArbitrary(
			Arbitraries.floats().between(0.0f, 100.0f)
		);
	}

	@Override
	public FloatCombinableArbitrary score() {
		return new JqwikFloatCombinableArbitrary(
			Arbitraries.floats().between(0.0f, 100.0f)
		);
	}

	@Override
	public FloatCombinableArbitrary withSpecialValue(float special) {
		return new JqwikFloatCombinableArbitrary(
			Arbitraries.oneOf(this.floatArbitrary, Arbitraries.of(special))
		);
	}

	@Override
	public FloatCombinableArbitrary withStandardSpecialValues() {
		return new JqwikFloatCombinableArbitrary(
			Arbitraries.oneOf(
				this.floatArbitrary,
				Arbitraries.of(
					Float.NaN,
					Float.MIN_VALUE,
					Float.MIN_NORMAL,
					Float.POSITIVE_INFINITY,
					Float.NEGATIVE_INFINITY
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
	public Float combined() {
		return this.floatArbitrary.sample();
	}
}
