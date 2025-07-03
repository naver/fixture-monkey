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
public interface ByteCombinableArbitrary extends CombinableArbitrary<Byte> {
	@Override
	Byte combined();

	@Override
	Byte rawValue();

	/**
	 * Generates a ByteCombinableArbitrary which produces bytes within the specified range.
	 *
	 * @param min the minimum value (inclusive)
	 * @param max the maximum value (inclusive)
	 * @return the ByteCombinableArbitrary producing bytes between {@code min} and {@code max}
	 */
	ByteCombinableArbitrary withRange(byte min, byte max);

	/**
	 * Generates a ByteCombinableArbitrary which produces only positive bytes.
	 *
	 * @return the ByteCombinableArbitrary producing positive bytes
	 */
	ByteCombinableArbitrary positive();

	/**
	 * Generates a ByteCombinableArbitrary which produces only negative bytes.
	 *
	 * @return the ByteCombinableArbitrary producing negative bytes
	 */
	ByteCombinableArbitrary negative();

	/**
	 * Generates a ByteCombinableArbitrary which produces only even bytes.
	 *
	 * @return the ByteCombinableArbitrary producing even bytes
	 */
	ByteCombinableArbitrary even();

	/**
	 * Generates a ByteCombinableArbitrary which produces only odd bytes.
	 *
	 * @return the ByteCombinableArbitrary producing odd bytes
	 */
	ByteCombinableArbitrary odd();

	/**
	 * Generates a ByteCombinableArbitrary which produces bytes in ASCII range (0-127).
	 *
	 * @return the ByteCombinableArbitrary producing ASCII bytes
	 */
	ByteCombinableArbitrary ascii();

	@Override
	default ByteCombinableArbitrary filter(Predicate<Byte> predicate) {
		return this.filter(DEFAULT_MAX_TRIES, predicate);
	}

	@Override
	default ByteCombinableArbitrary filter(int tries, Predicate<Byte> predicate) {
		return new ByteCombinableArbitraryDelegator(CombinableArbitrary.super.filter(tries, predicate));
	}

	@Override
	default ByteCombinableArbitrary injectNull(double nullProbability) {
		return new ByteCombinableArbitraryDelegator(CombinableArbitrary.super.injectNull(nullProbability));
	}

	@Override
	default ByteCombinableArbitrary unique() {
		return new ByteCombinableArbitraryDelegator(CombinableArbitrary.super.unique());
	}

	@Override
	void clear();

	@Override
	boolean fixed();
}
