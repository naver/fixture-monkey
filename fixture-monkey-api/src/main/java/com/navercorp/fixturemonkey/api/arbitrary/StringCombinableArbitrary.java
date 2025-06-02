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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * A combinable arbitrary for generating strings with various character sets and constraints.
 */
@API(since = "1.1.12", status = Status.EXPERIMENTAL)
public interface StringCombinableArbitrary extends CombinableArbitrary<String> {
	int STRING_DEFAULT_MIN_LENGTH = 0;
	int STRING_DEFAULT_MAX_LENGTH = 100;

	StringCombinableArbitrary withLength(int min, int max);

	/**
	 * Generates a StringCombinableArbitrary which contains only alphabetic characters.
	 * It conflicts with {@link #ascii()} and {@link #numeric()} and {@link #korean()}.
	 * Calling this method will ignore any previously called character set methods.
	 *
	 * <p>Example:
	 * <pre>{@code
	 * // Only the last character set method (alphabetic) will be applied
	 * stringArbitrary.numeric().alphabetic() // generates alphabetic characters only
	 *
	 * // Other configuration methods are also ignored when character set method is called
	 * stringArbitrary.withMinLength(5).alphabetic() // withMinLength(5) is ignored
	 * }</pre>
	 *
	 * @return the StringCombinableArbitrary which contains only alphabetic characters
	 */
	StringCombinableArbitrary alphabetic();

	/**
	 * Generates a StringCombinableArbitrary which contains only ASCII characters.
	 * It conflicts with {@link #alphabetic()} and {@link #numeric()} and {@link #korean()}.
	 * Calling this method will ignore any previously called character set methods.
	 *
	 * <p>Example:
	 * <pre>{@code
	 * // Only the last character set method (ascii) will be applied
	 * stringArbitrary.korean().ascii() // generates ASCII characters only
	 *
	 * // Other configuration methods are also ignored when character set method is called
	 * stringArbitrary.withMaxLength(10).ascii() // withMaxLength(10) is ignored
	 * }</pre>
	 *
	 * @return the StringCombinableArbitrary which contains only ASCII characters
	 */
	StringCombinableArbitrary ascii();

	/**
	 * Generates a StringCombinableArbitrary which contains only numeric characters.
	 * It conflicts with {@link #alphabetic()} and {@link #ascii()} and {@link #korean()}.
	 * Calling this method will ignore any previously called character set methods.
	 *
	 * <p>Example:
	 * <pre>{@code
	 * // Only the last character set method (numeric) will be applied
	 * stringArbitrary.alphabetic().numeric() // generates numeric characters only
	 *
	 * // Other configuration methods are also ignored when character set method is called
	 * stringArbitrary.withLength(3, 7).numeric() // withLength(3, 7) is ignored
	 * }</pre>
	 *
	 * @return the StringCombinableArbitrary which contains only numeric characters
	 */
	StringCombinableArbitrary numeric();

	/**
	 * Generates a StringCombinableArbitrary which contains only Korean characters.
	 * It conflicts with {@link #alphabetic()} and {@link #ascii()} and {@link #numeric()}.
	 * Calling this method will ignore any previously called character set methods.
	 *
	 * <p>Example:
	 * <pre>{@code
	 * // Only the last character set method (korean) will be applied
	 * stringArbitrary.ascii().korean() // generates Korean characters only
	 *
	 * // Other configuration methods are also ignored when character set method is called
	 * stringArbitrary.withMinLength(5).korean() // withMinLength(5) is ignored
	 * }</pre>
	 *
	 * @return the StringCombinableArbitrary which contains only Korean characters
	 */
	StringCombinableArbitrary korean();

	default StringCombinableArbitrary withMinLength(int min) {
		return this.withLength(min, STRING_DEFAULT_MAX_LENGTH);
	}

	default StringCombinableArbitrary withMaxLength(int max) {
		return this.withLength(STRING_DEFAULT_MIN_LENGTH, max);
	}

	/**
	 * Filters the generated strings using the given predicate.
	 * This method sets an invariant condition for string generation.
	 * This method can conflict with other methods and should be used carefully.
	 *
	 * @param predicate the predicate to filter strings
	 * @return the filtered StringCombinableArbitrary
	 */
	@Override
	default StringCombinableArbitrary filter(Predicate<String> predicate) {
		return this.filter(DEFAULT_MAX_TRIES, predicate);
	}

	/**
	 * Filters the generated strings using the given predicate with specified retry attempts.
	 * This method sets an invariant condition for string generation.
	 * This method can conflict with other methods and should be used carefully.
	 *
	 * @param tries the maximum number of tries to generate a valid string
	 * @param predicate the predicate to filter strings
	 * @return the filtered StringCombinableArbitrary
	 */
	@Override
	default StringCombinableArbitrary filter(int tries, Predicate<String> predicate) {
		return new StringCombinableArbitraryDelegator(CombinableArbitrary.super.filter(tries, predicate));
	}

	/**
	 * Filters individual characters in the generated strings using the given predicate.
	 * This method sets an invariant condition for character generation.
	 * This method can conflict with other methods and should be used carefully.
	 *
	 * @param predicate the predicate to filter characters
	 * @return the filtered StringCombinableArbitrary
	 */
	default StringCombinableArbitrary filterCharacter(Predicate<Character> predicate) {
		return this.filterCharacter(DEFAULT_MAX_TRIES, predicate);
	}

	/**
	 * Filters individual characters in the generated strings using the given predicate with specified retry attempts.
	 * This method sets an invariant condition for character generation.
	 * This method can conflict with other methods and should be used carefully.
	 *
	 * @param tries the maximum number of tries to generate a valid character
	 * @param predicate the predicate to filter characters
	 * @return the filtered StringCombinableArbitrary
	 */
	StringCombinableArbitrary filterCharacter(int tries, Predicate<Character> predicate);

	@Override
	default StringCombinableArbitrary injectNull(double nullProbability) {
		return new StringCombinableArbitraryDelegator(CombinableArbitrary.super.injectNull(nullProbability));
	}

	@Override
	default StringCombinableArbitrary unique() {
		return new StringCombinableArbitraryDelegator(CombinableArbitrary.super.unique());
	}
}
