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

import com.navercorp.fixturemonkey.api.arbitrary.IntegerCombinableArbitrary;

@API(since = "1.1.12", status = Status.EXPERIMENTAL)
public final class JqwikIntegerCombinableArbitrary implements IntegerCombinableArbitrary {
	private final Arbitrary<Integer> integerArbitrary;

	public JqwikIntegerCombinableArbitrary() {
		this.integerArbitrary = Arbitraries.integers();
	}

	private JqwikIntegerCombinableArbitrary(Arbitrary<Integer> integerArbitrary) {
		this.integerArbitrary = integerArbitrary;
	}

	@Override
	public Integer combined() {
		return this.integerArbitrary.sample();
	}

	@Override
	public Integer rawValue() {
		return this.combined();
	}

	@Override
	public IntegerCombinableArbitrary withRange(int minValue, int maxValue) {
		return new JqwikIntegerCombinableArbitrary(
			Arbitraries.integers().between(minValue, maxValue)
		);
	}

	@Override
	public IntegerCombinableArbitrary positive() {
		return new JqwikIntegerCombinableArbitrary(Arbitraries.integers().greaterOrEqual(1));
	}

	@Override
	public IntegerCombinableArbitrary negative() {
		return new JqwikIntegerCombinableArbitrary(Arbitraries.integers().lessOrEqual(-1));
	}

	@Override
	public IntegerCombinableArbitrary even() {
		return new JqwikIntegerCombinableArbitrary(Arbitraries.integers().filter(it -> it % 2 == 0));
	}

	@Override
	public IntegerCombinableArbitrary odd() {
		return new JqwikIntegerCombinableArbitrary(Arbitraries.integers().filter(it -> it % 2 != 0));
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
