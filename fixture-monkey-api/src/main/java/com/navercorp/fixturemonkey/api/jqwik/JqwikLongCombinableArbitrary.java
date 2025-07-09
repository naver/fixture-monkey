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

import com.navercorp.fixturemonkey.api.arbitrary.LongCombinableArbitrary;

@API(since = "1.1.12", status = Status.EXPERIMENTAL)
public final class JqwikLongCombinableArbitrary implements LongCombinableArbitrary {
	private final Arbitrary<Long> longArbitrary;

	public JqwikLongCombinableArbitrary() {
		this(Arbitraries.longs());
	}

	private JqwikLongCombinableArbitrary(Arbitrary<Long> longArbitrary) {
		this.longArbitrary = longArbitrary;
	}

	@Override
	public Long combined() {
		return this.longArbitrary.sample();
	}

	@Override
	public Long rawValue() {
		return this.combined();
	}

	@Override
	public LongCombinableArbitrary withRange(long minValue, long maxValue) {
		return new JqwikLongCombinableArbitrary(
			Arbitraries.longs().between(minValue, maxValue)
		);
	}

	@Override
	public LongCombinableArbitrary positive() {
		return new JqwikLongCombinableArbitrary(Arbitraries.longs().greaterOrEqual(1L));
	}

	@Override
	public LongCombinableArbitrary negative() {
		return new JqwikLongCombinableArbitrary(Arbitraries.longs().lessOrEqual(-1L));
	}

	@Override
	public LongCombinableArbitrary even() {
		return new JqwikLongCombinableArbitrary(Arbitraries.longs().filter(it -> it % 2 == 0));
	}

	@Override
	public LongCombinableArbitrary odd() {
		return new JqwikLongCombinableArbitrary(Arbitraries.longs().filter(it -> it % 2 != 0));
	}

	@Override
	public LongCombinableArbitrary nonZero() {
		return new JqwikLongCombinableArbitrary(Arbitraries.longs().filter(it -> it != 0));
	}

	@Override
	public LongCombinableArbitrary multipleOf(long divisor) {
		return new JqwikLongCombinableArbitrary(Arbitraries.longs().filter(it -> it % divisor == 0));
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
