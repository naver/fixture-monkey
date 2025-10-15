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

import java.util.List;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.arbitraries.ListArbitrary;

import com.navercorp.fixturemonkey.api.arbitrary.StringCombinableArbitrary;

@API(since = "1.1.12", status = Status.EXPERIMENTAL)
public final class JqwikStringCombinableArbitrary implements StringCombinableArbitrary {
	private final Arbitrary<Character> characterArbitrary;
	@Nullable
	private Integer minSize = null;
	@Nullable
	private Integer maxSize = null;

	public JqwikStringCombinableArbitrary() {
		this.characterArbitrary = Arbitraries.chars();
	}

	private JqwikStringCombinableArbitrary(Arbitrary<Character> characterArbitrary) {
		this.characterArbitrary = characterArbitrary;
	}

	@Override
	public String combined() {
		ListArbitrary<Character> characterListArbitrary = characterArbitrary.list();
		if (this.minSize != null) {
			characterListArbitrary = characterListArbitrary.ofMinSize(this.minSize);
		}

		if (this.maxSize != null) {
			characterListArbitrary = characterListArbitrary.ofMaxSize(this.maxSize);
		}

		List<Character> characters = characterListArbitrary.sample();
		StringBuilder stringBuilder = new StringBuilder();
		for (Character character : characters) {
			stringBuilder.append(character);
		}
		return stringBuilder.toString();
	}

	@Override
	public String rawValue() {
		return this.combined();
	}

	@Override
	public StringCombinableArbitrary withLength(int minSize, int maxSize) {
		this.minSize = minSize;
		this.maxSize = maxSize;
		return this;
	}

	@Override
	public StringCombinableArbitrary alphabetic() {
		return new JqwikStringCombinableArbitrary(Arbitraries.chars().alpha());
	}

	@Override
	public StringCombinableArbitrary ascii() {
		return new JqwikStringCombinableArbitrary(Arbitraries.chars().ascii());
	}

	@Override
	public StringCombinableArbitrary numeric() {
		return new JqwikStringCombinableArbitrary(Arbitraries.chars().numeric());
	}

	@Override
	public StringCombinableArbitrary korean() {
		return new JqwikStringCombinableArbitrary(Arbitraries.chars().filter(
			it -> '가' <= it && it <= '힣'
		));
	}

	@Override
	public StringCombinableArbitrary filterCharacter(int tries, Predicate<Character> predicate) {
		return new JqwikStringCombinableArbitrary(this.characterArbitrary.filter(tries, predicate));
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
