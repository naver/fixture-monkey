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
final class DoubleCombinableArbitraryDelegator implements DoubleCombinableArbitrary {
	private final CombinableArbitrary<Double> delegate;

	public DoubleCombinableArbitraryDelegator(CombinableArbitrary<Double> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Double combined() {
		return delegate.combined();
	}

	@Override
	public Double rawValue() {
		return (Double)delegate.rawValue();
	}

	@Override
	public DoubleCombinableArbitrary withRange(double min, double max) {
		return CombinableArbitrary.doubles().withRange(min, max);
	}

	@Override
	public DoubleCombinableArbitrary positive() {
		return CombinableArbitrary.doubles().positive();
	}

	@Override
	public DoubleCombinableArbitrary negative() {
		return CombinableArbitrary.doubles().negative();
	}

	@Override
	public DoubleCombinableArbitrary nonZero() {
		return CombinableArbitrary.doubles().nonZero();
	}

	@Override
	public DoubleCombinableArbitrary withPrecision(int scale) {
		return CombinableArbitrary.doubles().withPrecision(scale);
	}

	@Override
	public DoubleCombinableArbitrary finite() {
		return CombinableArbitrary.doubles().finite();
	}

	@Override
	public DoubleCombinableArbitrary infinite() {
		return CombinableArbitrary.doubles().infinite();
	}

	@Override
	public DoubleCombinableArbitrary normalized() {
		return CombinableArbitrary.doubles().normalized();
	}

	@Override
	public DoubleCombinableArbitrary nan() {
		return CombinableArbitrary.doubles().nan();
	}

	@Override
	public DoubleCombinableArbitrary percentage() {
		return CombinableArbitrary.doubles().percentage();
	}

	@Override
	public DoubleCombinableArbitrary score() {
		return CombinableArbitrary.doubles().score();
	}

	@Override
	public DoubleCombinableArbitrary withSpecialValue(double special) {
		return CombinableArbitrary.doubles().withSpecialValue(special);
	}

	@Override
	public DoubleCombinableArbitrary withStandardSpecialValues() {
		return CombinableArbitrary.doubles().withStandardSpecialValues();
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
