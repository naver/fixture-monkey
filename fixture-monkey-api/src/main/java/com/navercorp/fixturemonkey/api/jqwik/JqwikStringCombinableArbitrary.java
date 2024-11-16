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

import javax.annotation.Nullable;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.arbitraries.ListArbitrary;

import com.navercorp.fixturemonkey.api.arbitrary.StringCombinableArbitrary;

public final class JqwikStringCombinableArbitrary implements StringCombinableArbitrary {
	private static final int MAX_ASCII_CODEPOINT = 0x007F;

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
	public StringCombinableArbitrary ascii() {
		return new JqwikStringCombinableArbitrary(
			this.characterArbitrary.filter(
				it -> (char)Character.MIN_CODE_POINT <= it && it <= (char)MAX_ASCII_CODEPOINT
			)
		);
	}

	@Override
	public StringCombinableArbitrary numeric() {
		return new JqwikStringCombinableArbitrary(this.characterArbitrary.filter(it -> '0' <= it && it <= '9'));
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
