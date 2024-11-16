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
import java.util.regex.Pattern;

public interface StringCombinableArbitrary extends CombinableArbitrary<String> {
	int STRING_DEFAULT_MIN_LENGTH = 0;
	int STRING_DEFAULT_MAX_LENGTH = 100;

	@Override
	String combined();

	@Override
	String rawValue();

	StringCombinableArbitrary withLength(int min, int max);

	StringCombinableArbitrary ascii();

	StringCombinableArbitrary numeric();

	default StringCombinableArbitrary withMinLength(int min) {
		return this.withLength(min, STRING_DEFAULT_MAX_LENGTH);
	}

	default StringCombinableArbitrary withMaxLength(int max) {
		return this.withLength(STRING_DEFAULT_MIN_LENGTH, max);
	}

	@Override
	default StringCombinableArbitrary filter(Predicate<String> predicate) {
		return this.filter(DEFAULT_MAX_TRIES, predicate);
	}

	@Override
	default StringCombinableArbitrary filter(int tries, Predicate<String> predicate) {
		return new StringCombinableArbitraryDelegator(CombinableArbitrary.super.filter(tries, predicate));
	}

	default StringCombinableArbitrary filterCharacter(Predicate<Character> predicate) {
		return this.filterCharacter(DEFAULT_MAX_TRIES, predicate);
	}

	StringCombinableArbitrary filterCharacter(int tries, Predicate<Character> predicate);

	/**
	 * Generates the pattern matched arbitrary String.
	 * Its performance differs from the generation engine you're using.
	 *
	 * @param stringPattern the regular expression pattern
	 * @return the pattern matched StringCombinableArbitrary
	 */
	default StringCombinableArbitrary pattern(int tries, String stringPattern) {
		Pattern pattern = Pattern.compile(stringPattern);
		return this.filter(tries, it -> pattern.matcher(it).matches());
	}

	@Override
	default StringCombinableArbitrary injectNull(double nullProbability) {
		return new StringCombinableArbitraryDelegator(CombinableArbitrary.super.injectNull(nullProbability));
	}

	@Override
	default StringCombinableArbitrary unique() {
		return new StringCombinableArbitraryDelegator(CombinableArbitrary.super.unique());
	}

	@Override
	void clear();

	@Override
	boolean fixed();
}
