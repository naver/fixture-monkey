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
final class FloatCombinableArbitraryDelegator implements FloatCombinableArbitrary {
	private final CombinableArbitrary<Float> delegate;

	public FloatCombinableArbitraryDelegator(CombinableArbitrary<Float> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Float combined() {
		return delegate.combined();
	}

	@Override
	public Float rawValue() {
		return (Float)delegate.rawValue();
	}

	@Override
	public FloatCombinableArbitrary withRange(float min, float max) {
		return CombinableArbitrary.floats().withRange(min, max);
	}

	@Override
	public FloatCombinableArbitrary positive() {
		return CombinableArbitrary.floats().positive();
	}

	@Override
	public FloatCombinableArbitrary negative() {
		return CombinableArbitrary.floats().negative();
	}

	@Override
	public FloatCombinableArbitrary nonZero() {
		return CombinableArbitrary.floats().nonZero();
	}

	@Override
	public FloatCombinableArbitrary withPrecision(int scale) {
		return CombinableArbitrary.floats().withPrecision(scale);
	}

	@Override
	public FloatCombinableArbitrary finite() {
		return CombinableArbitrary.floats().finite();
	}

	@Override
	public FloatCombinableArbitrary infinite() {
		return CombinableArbitrary.floats().infinite();
	}

	@Override
	public FloatCombinableArbitrary normalized() {
		return CombinableArbitrary.floats().normalized();
	}

	@Override
	public FloatCombinableArbitrary nan() {
		return CombinableArbitrary.floats().nan();
	}

	@Override
	public FloatCombinableArbitrary percentage() {
		return CombinableArbitrary.floats().percentage();
	}

	@Override
	public FloatCombinableArbitrary score() {
		return CombinableArbitrary.floats().score();
	}

	@Override
	public FloatCombinableArbitrary withSpecialValue(float special) {
		return CombinableArbitrary.floats().withSpecialValue(special);
	}

	@Override
	public FloatCombinableArbitrary withStandardSpecialValues() {
		return CombinableArbitrary.floats().withStandardSpecialValues();
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
