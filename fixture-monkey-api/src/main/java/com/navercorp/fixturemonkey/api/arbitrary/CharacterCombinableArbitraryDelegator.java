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

@API(since = "1.1.15", status = Status.EXPERIMENTAL)
final class CharacterCombinableArbitraryDelegator implements CharacterCombinableArbitrary {
	private final CombinableArbitrary<Character> delegate;

	public CharacterCombinableArbitraryDelegator(CombinableArbitrary<Character> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Character combined() {
		return delegate.combined();
	}

	@Override
	public Character rawValue() {
		return delegate.combined();
	}

	@Override
	public CharacterCombinableArbitrary withRange(char min, char max) {
		return CombinableArbitrary.chars().withRange(min, max);
	}

	@Override
	public CharacterCombinableArbitrary alphabetic() {
		return CombinableArbitrary.chars().alphabetic();
	}

	@Override
	public CharacterCombinableArbitrary numeric() {
		return CombinableArbitrary.chars().numeric();
	}

	@Override
	public CharacterCombinableArbitrary alphaNumeric() {
		return CombinableArbitrary.chars().alphaNumeric();
	}

	@Override
	public CharacterCombinableArbitrary ascii() {
		return CombinableArbitrary.chars().ascii();
	}

	@Override
	public CharacterCombinableArbitrary uppercase() {
		return CombinableArbitrary.chars().uppercase();
	}

	@Override
	public CharacterCombinableArbitrary lowercase() {
		return CombinableArbitrary.chars().lowercase();
	}

	@Override
	public CharacterCombinableArbitrary korean() {
		return CombinableArbitrary.chars().korean();
	}

	@Override
	public CharacterCombinableArbitrary emoji() {
		return CombinableArbitrary.chars().emoji();
	}

	@Override
	public CharacterCombinableArbitrary whitespace() {
		return CombinableArbitrary.chars().whitespace();
	}

	@Override
	public CharacterCombinableArbitrary punctuation() {
		return CombinableArbitrary.chars().punctuation();
	}

	@Override
	public CharacterCombinableArbitrary digit() {
		return CombinableArbitrary.chars().digit();
	}

	@Override
	public CharacterCombinableArbitrary control() {
		return CombinableArbitrary.chars().control();
	}

	@Override
	public CharacterCombinableArbitrary unicodeCategory(int category) {
		return CombinableArbitrary.chars().unicodeCategory(category);
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
