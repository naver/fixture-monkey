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

@API(since = "1.1.12", status = Status.EXPERIMENTAL)
public interface IntegerCombinableArbitrary extends CombinableArbitrary<Integer> {
	@Override
	Integer combined();

	@Override
	Integer rawValue();

	/**
	 * Generates an IntegerCombinableArbitrary which produces integers within the specified range.
	 *
	 * @param min the minimum value (inclusive)
	 * @param max the maximum value (inclusive)
	 * @return the IntegerCombinableArbitrary producing integers between {@code min} and {@code max}
	 */
	IntegerCombinableArbitrary withRange(int min, int max);

	/**
	 * Generates an IntegerCombinableArbitrary which produces only positive integers.
	 *
	 * @return the IntegerCombinableArbitrary producing positive integers
	 */
	IntegerCombinableArbitrary positive();

	/**
	 * Generates an IntegerCombinableArbitrary which produces only negative integers.
	 *
	 * @return the IntegerCombinableArbitrary producing negative integers
	 */
	IntegerCombinableArbitrary negative();

	/**
	 * Generates an IntegerCombinableArbitrary which produces only even integers.
	 *
	 * @return the IntegerCombinableArbitrary producing even integers
	 */
	IntegerCombinableArbitrary even();

	/**
	 * Generates an IntegerCombinableArbitrary which produces only odd integers.
	 *
	 * @return the IntegerCombinableArbitrary producing odd integers
	 */
	IntegerCombinableArbitrary odd();

	@Override
	default IntegerCombinableArbitrary filter(Predicate<Integer> predicate) {
		return this.filter(DEFAULT_MAX_TRIES, predicate);
	}

	@Override
	default IntegerCombinableArbitrary filter(int tries, Predicate<Integer> predicate) {
		return new IntegerCombinableArbitraryDelegator(CombinableArbitrary.super.filter(tries, predicate));
	}

	@Override
	default IntegerCombinableArbitrary injectNull(double nullProbability) {
		return new IntegerCombinableArbitraryDelegator(CombinableArbitrary.super.injectNull(nullProbability));
	}

	@Override
	default IntegerCombinableArbitrary unique() {
		return new IntegerCombinableArbitraryDelegator(CombinableArbitrary.super.unique());
	}

	@Override
	void clear();

	@Override
	boolean fixed();
}
