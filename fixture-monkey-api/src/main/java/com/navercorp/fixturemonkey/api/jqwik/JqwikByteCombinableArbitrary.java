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

import com.navercorp.fixturemonkey.api.arbitrary.ByteCombinableArbitrary;

@API(since = "1.1.15", status = Status.EXPERIMENTAL)
public final class JqwikByteCombinableArbitrary implements ByteCombinableArbitrary {
	private final Arbitrary<Byte> byteArbitrary;

	public JqwikByteCombinableArbitrary() {
		this(Arbitraries.bytes());
	}

	private JqwikByteCombinableArbitrary(Arbitrary<Byte> byteArbitrary) {
		this.byteArbitrary = byteArbitrary;
	}

	@Override
	public Byte combined() {
		return this.byteArbitrary.sample();
	}

	@Override
	public Byte rawValue() {
		return this.combined();
	}

	@Override
	public ByteCombinableArbitrary withRange(byte minValue, byte maxValue) {
		return new JqwikByteCombinableArbitrary(
			Arbitraries.bytes().between(minValue, maxValue)
		);
	}

	@Override
	public ByteCombinableArbitrary positive() {
		return new JqwikByteCombinableArbitrary(Arbitraries.bytes().greaterOrEqual((byte)1));
	}

	@Override
	public ByteCombinableArbitrary negative() {
		return new JqwikByteCombinableArbitrary(Arbitraries.bytes().lessOrEqual((byte)-1));
	}

	@Override
	public ByteCombinableArbitrary even() {
		return new JqwikByteCombinableArbitrary(Arbitraries.bytes().filter(it -> it % 2 == 0));
	}

	@Override
	public ByteCombinableArbitrary odd() {
		return new JqwikByteCombinableArbitrary(Arbitraries.bytes().filter(it -> it % 2 != 0));
	}

	@Override
	public ByteCombinableArbitrary ascii() {
		return new JqwikByteCombinableArbitrary(Arbitraries.bytes().between((byte)0, (byte)127));
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
