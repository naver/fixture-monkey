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

@API(since = "1.1.15", status = Status.EXPERIMENTAL)
public interface LongCombinableArbitrary extends CombinableArbitrary<Long> {
	@Override
	Long combined();

	@Override
	Long rawValue();

	/**
	 * Generates a LongCombinableArbitrary which produces longs within the specified range.
	 *
	 * @param min the minimum value (inclusive)
	 * @param max the maximum value (inclusive)
	 * @return the LongCombinableArbitrary producing longs between {@code min} and {@code max}
	 */
	LongCombinableArbitrary withRange(long min, long max);

	/**
	 * Generates a LongCombinableArbitrary which produces only positive longs.
	 *
	 * @return the LongCombinableArbitrary producing positive longs
	 */
	LongCombinableArbitrary positive();

	/**
	 * Generates a LongCombinableArbitrary which produces only negative longs.
	 *
	 * @return the LongCombinableArbitrary producing negative longs
	 */
	LongCombinableArbitrary negative();

	/**
	 * Generates a LongCombinableArbitrary which produces only even longs.
	 *
	 * @return the LongCombinableArbitrary producing even longs
	 */
	LongCombinableArbitrary even();

	/**
	 * Generates a LongCombinableArbitrary which produces only odd longs.
	 *
	 * @return the LongCombinableArbitrary producing odd longs
	 */
	LongCombinableArbitrary odd();

	@Override
	default LongCombinableArbitrary filter(Predicate<Long> predicate) {
		return this.filter(DEFAULT_MAX_TRIES, predicate);
	}

	@Override
	default LongCombinableArbitrary filter(int tries, Predicate<Long> predicate) {
		return new LongCombinableArbitraryDelegator(CombinableArbitrary.super.filter(tries, predicate));
	}

	@Override
	default LongCombinableArbitrary injectNull(double nullProbability) {
		return new LongCombinableArbitraryDelegator(CombinableArbitrary.super.injectNull(nullProbability));
	}

	@Override
	default LongCombinableArbitrary unique() {
		return new LongCombinableArbitraryDelegator(CombinableArbitrary.super.unique());
	}

	@Override
	void clear();

	@Override
	boolean fixed();
}
