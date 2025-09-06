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

import com.navercorp.fixturemonkey.api.arbitrary.CharacterCombinableArbitrary;

@API(since = "1.1.16", status = Status.EXPERIMENTAL)
public final class JqwikCharacterCombinableArbitrary implements CharacterCombinableArbitrary {
	private final Arbitrary<Character> characterArbitrary;

	public JqwikCharacterCombinableArbitrary() {
		this.characterArbitrary = Arbitraries.chars();
	}

	private JqwikCharacterCombinableArbitrary(Arbitrary<Character> characterArbitrary) {
		this.characterArbitrary = characterArbitrary;
	}

	@Override
	public Character rawValue() {
		return this.characterArbitrary.sample();
	}

	@Override
	public CharacterCombinableArbitrary withRange(char minValue, char maxValue) {
		return new JqwikCharacterCombinableArbitrary(
			Arbitraries.chars().range(minValue, maxValue)
		);
	}

	@Override
	public CharacterCombinableArbitrary alphabetic() {
		return new JqwikCharacterCombinableArbitrary(
			Arbitraries.chars().alpha()
		);
	}

	@Override
	public CharacterCombinableArbitrary numeric() {
		return new JqwikCharacterCombinableArbitrary(
			Arbitraries.chars().numeric()
		);
	}

	@Override
	public CharacterCombinableArbitrary alphaNumeric() {
		return new JqwikCharacterCombinableArbitrary(
			Arbitraries.chars().alpha().numeric()
		);
	}

	@Override
	public CharacterCombinableArbitrary ascii() {
		return new JqwikCharacterCombinableArbitrary(
			Arbitraries.chars().ascii()
		);
	}

	@Override
	public CharacterCombinableArbitrary uppercase() {
		return new JqwikCharacterCombinableArbitrary(
			Arbitraries.chars().range('A', 'Z')
		);
	}

	@Override
	public CharacterCombinableArbitrary lowercase() {
		return new JqwikCharacterCombinableArbitrary(
			Arbitraries.chars().range('a', 'z')
		);
	}

	@Override
	public CharacterCombinableArbitrary korean() {
		return new JqwikCharacterCombinableArbitrary(
			Arbitraries.chars().range('\uAC00', '\uD7A3')  // 가-힣
		);
	}

	@Override
	public CharacterCombinableArbitrary emoji() {
		return new JqwikCharacterCombinableArbitrary(
			Arbitraries.chars().range('\uD83D', '\uD83F')  // Basic emoji range
		);
	}

	@Override
	public CharacterCombinableArbitrary whitespace() {
		return new JqwikCharacterCombinableArbitrary(
			Arbitraries.of(' ', '\t', '\n', '\r', '\f')
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
	public Character combined() {
		return this.characterArbitrary.sample();
	}
}
