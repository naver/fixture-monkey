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
	 * <p><strong>Note:</strong> When combined with {@link #ascii()}, this will produce
	 * only positive bytes within the ASCII range (1-127).
	 *
	 * @return the ByteCombinableArbitrary producing positive bytes
	 */
	ByteCombinableArbitrary positive();

	/**
	 * Generates a ByteCombinableArbitrary which produces only negative bytes.
	 * <p><strong>Note:</strong> Cannot be combined with {@link #ascii()} as ASCII range
	 * contains no negative values.
	 *
	 * @return the ByteCombinableArbitrary producing negative bytes
	 */
	ByteCombinableArbitrary negative();

	/**
	 * Generates a ByteCombinableArbitrary which produces only even bytes.
	 * <p><strong>Note:</strong> When combined with {@link #ascii()}, this will produce
	 * only even bytes within the ASCII range (0, 2, 4, ..., 126).
	 *
	 * @return the ByteCombinableArbitrary producing even bytes
	 */
	ByteCombinableArbitrary even();

	/**
	 * Generates a ByteCombinableArbitrary which produces only odd bytes.
	 * <p><strong>Note:</strong> When combined with {@link #ascii()}, this will produce
	 * only odd bytes within the ASCII range (1, 3, 5, ..., 127).
	 *
	 * @return the ByteCombinableArbitrary producing odd bytes
	 */
	ByteCombinableArbitrary odd();

	/**
	 * Generates a ByteCombinableArbitrary which produces bytes in ASCII range (0-127).
	 * <p><strong>Note:</strong> When combined with {@link #odd()}, this will produce
	 * only odd bytes within the ASCII range (1, 3, 5, ..., 127).
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
