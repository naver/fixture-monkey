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

import com.navercorp.fixturemonkey.api.arbitrary.ShortCombinableArbitrary;

@API(since = "1.1.15", status = Status.EXPERIMENTAL)
public final class JqwikShortCombinableArbitrary implements ShortCombinableArbitrary {
	private final Arbitrary<Short> shortArbitrary;

	public JqwikShortCombinableArbitrary() {
		this(Arbitraries.shorts());
	}

	private JqwikShortCombinableArbitrary(Arbitrary<Short> shortArbitrary) {
		this.shortArbitrary = shortArbitrary;
	}

	@Override
	public Short combined() {
		return this.shortArbitrary.sample();
	}

	@Override
	public Short rawValue() {
		return this.combined();
	}

	@Override
	public ShortCombinableArbitrary withRange(short minValue, short maxValue) {
		return new JqwikShortCombinableArbitrary(
			Arbitraries.shorts().between(minValue, maxValue)
		);
	}

	@Override
	public ShortCombinableArbitrary positive() {
		return new JqwikShortCombinableArbitrary(Arbitraries.shorts().greaterOrEqual((short)1));
	}

	@Override
	public ShortCombinableArbitrary negative() {
		return new JqwikShortCombinableArbitrary(Arbitraries.shorts().lessOrEqual((short)-1));
	}

	@Override
	public ShortCombinableArbitrary even() {
		return new JqwikShortCombinableArbitrary(Arbitraries.shorts().filter(it -> it % 2 == 0));
	}

	@Override
	public ShortCombinableArbitrary odd() {
		return new JqwikShortCombinableArbitrary(Arbitraries.shorts().filter(it -> it % 2 != 0));
	}

	@Override
	public ShortCombinableArbitrary nonZero() {
		return new JqwikShortCombinableArbitrary(Arbitraries.shorts().filter(it -> it != 0));
	}

	@Override
	public ShortCombinableArbitrary multipleOf(short value) {
		return new JqwikShortCombinableArbitrary(Arbitraries.shorts().filter(it -> it % value == 0));
	}

	@Override
	public ShortCombinableArbitrary percentage() {
		return new JqwikShortCombinableArbitrary(Arbitraries.shorts().between((short)0, (short)100));
	}

	@Override
	public ShortCombinableArbitrary score() {
		return new JqwikShortCombinableArbitrary(Arbitraries.shorts().between((short)0, (short)100));
	}

	@Override
	public ShortCombinableArbitrary year() {
		return new JqwikShortCombinableArbitrary(Arbitraries.shorts().between((short)1900, (short)2100));
	}

	@Override
	public ShortCombinableArbitrary month() {
		return new JqwikShortCombinableArbitrary(Arbitraries.shorts().between((short)1, (short)12));
	}

	@Override
	public ShortCombinableArbitrary day() {
		return new JqwikShortCombinableArbitrary(Arbitraries.shorts().between((short)1, (short)31));
	}

	@Override
	public ShortCombinableArbitrary hour() {
		return new JqwikShortCombinableArbitrary(Arbitraries.shorts().between((short)0, (short)23));
	}

	@Override
	public ShortCombinableArbitrary minute() {
		return new JqwikShortCombinableArbitrary(Arbitraries.shorts().between((short)0, (short)59));
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
