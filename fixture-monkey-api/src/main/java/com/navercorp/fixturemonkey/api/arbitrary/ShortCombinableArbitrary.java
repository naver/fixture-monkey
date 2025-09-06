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

@API(since = "1.1.16", status = Status.EXPERIMENTAL)
public interface ShortCombinableArbitrary extends CombinableArbitrary<Short> {
	@Override
	Short combined();

	@Override
	Short rawValue();

	/**
	 * Generates a ShortCombinableArbitrary which produces shorts within the specified range.
	 *
	 * @param min the minimum value (inclusive)
	 * @param max the maximum value (inclusive)
	 * @return the ShortCombinableArbitrary producing shorts between {@code min} and {@code max}
	 */
	ShortCombinableArbitrary withRange(short min, short max);

	/**
	 * Generates a ShortCombinableArbitrary which produces only positive shorts.
	 *
	 * @return the ShortCombinableArbitrary producing positive shorts
	 */
	ShortCombinableArbitrary positive();

	/**
	 * Generates a ShortCombinableArbitrary which produces only negative shorts.
	 *
	 * @return the ShortCombinableArbitrary producing negative shorts
	 */
	ShortCombinableArbitrary negative();

	/**
	 * Generates a ShortCombinableArbitrary which produces only even shorts.
	 *
	 * @return the ShortCombinableArbitrary producing even shorts
	 */
	ShortCombinableArbitrary even();

	/**
	 * Generates a ShortCombinableArbitrary which produces only odd shorts.
	 *
	 * @return the ShortCombinableArbitrary producing odd shorts
	 */
	ShortCombinableArbitrary odd();

	/**
	 * Generates a ShortCombinableArbitrary which produces only non-zero shorts.
	 * This is useful for preventing division by zero errors.
	 *
	 * @return the ShortCombinableArbitrary producing non-zero shorts
	 */
	ShortCombinableArbitrary nonZero();

	/**
	 * Generates a ShortCombinableArbitrary which produces shorts that are multiples of the specified value.
	 *
	 * @param value the value to generate multiples of
	 * @return the ShortCombinableArbitrary producing multiples of {@code value}
	 */
	ShortCombinableArbitrary multipleOf(short value);

	/**
	 * Generates a ShortCombinableArbitrary which produces percentage values (0-100).
	 *
	 * @return the ShortCombinableArbitrary producing percentage values
	 */
	ShortCombinableArbitrary percentage();

	/**
	 * Generates a ShortCombinableArbitrary which produces score values (0-100).
	 *
	 * @return the ShortCombinableArbitrary producing score values
	 */
	ShortCombinableArbitrary score();

	/**
	 * Generates a ShortCombinableArbitrary which produces year values (1900-2100).
	 *
	 * @return the ShortCombinableArbitrary producing year values
	 */
	ShortCombinableArbitrary year();

	/**
	 * Generates a ShortCombinableArbitrary which produces month values (1-12).
	 *
	 * @return the ShortCombinableArbitrary producing month values
	 */
	ShortCombinableArbitrary month();

	/**
	 * Generates a ShortCombinableArbitrary which produces day values (1-31).
	 *
	 * @return the ShortCombinableArbitrary producing day values
	 */
	ShortCombinableArbitrary day();

	/**
	 * Generates a ShortCombinableArbitrary which produces hour values (0-23).
	 *
	 * @return the ShortCombinableArbitrary producing hour values
	 */
	ShortCombinableArbitrary hour();

	/**
	 * Generates a ShortCombinableArbitrary which produces minute values (0-59).
	 *
	 * @return the ShortCombinableArbitrary producing minute values
	 */
	ShortCombinableArbitrary minute();

	@Override
	default ShortCombinableArbitrary filter(Predicate<Short> predicate) {
		return this.filter(DEFAULT_MAX_TRIES, predicate);
	}

	@Override
	default ShortCombinableArbitrary filter(int tries, Predicate<Short> predicate) {
		return new ShortCombinableArbitraryDelegator(CombinableArbitrary.super.filter(tries, predicate));
	}

	@Override
	default ShortCombinableArbitrary injectNull(double nullProbability) {
		return new ShortCombinableArbitraryDelegator(CombinableArbitrary.super.injectNull(nullProbability));
	}

	@Override
	default ShortCombinableArbitrary unique() {
		return new ShortCombinableArbitraryDelegator(CombinableArbitrary.super.unique());
	}

	@Override
	void clear();

	@Override
	boolean fixed();
}
