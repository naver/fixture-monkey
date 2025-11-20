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

import java.util.function.Predicate;

final class StringCombinableArbitraryDelegator implements StringCombinableArbitrary {
	private final CombinableArbitrary<String> delegate;

	public StringCombinableArbitraryDelegator(CombinableArbitrary<String> delegate) {
		this.delegate = delegate;
	}

	@Override
	public String combined() {
		return delegate.combined();
	}

	@Override
	public String rawValue() {
		return delegate.combined();
	}

	@Override
	public StringCombinableArbitrary withLength(int min, int max) {
		return new StringCombinableArbitraryDelegator(delegate.filter(it -> min <= it.length() && it.length() <= max));
	}

	@Override
	public StringCombinableArbitrary alphabetic() {
		return CombinableArbitrary.strings().alphabetic();
	}

	@Override
	public StringCombinableArbitrary ascii() {
		return CombinableArbitrary.strings().ascii();
	}

	@Override
	public StringCombinableArbitrary numeric() {
		return CombinableArbitrary.strings().numeric();
	}

	@Override
	public StringCombinableArbitrary korean() {
		return CombinableArbitrary.strings().korean();
	}

	@Override
	public StringCombinableArbitrary filter(int tries, Predicate<String> predicate) {
		return new StringCombinableArbitraryDelegator(delegate.filter(tries, predicate));
	}

	@Override
	@SuppressWarnings("argument")
	public StringCombinableArbitrary filterCharacter(int tries, Predicate<Character> predicate) {
		return this.filter(tries, it -> it.chars().mapToObj(Character.class::cast).allMatch(predicate));
	}

	@Override
	public StringCombinableArbitrary unique() {
		return new StringCombinableArbitraryDelegator(delegate.unique());
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
