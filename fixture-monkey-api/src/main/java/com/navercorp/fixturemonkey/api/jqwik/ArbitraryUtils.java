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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;

@API(since = "0.6.9", status = Status.EXPERIMENTAL)
public final class ArbitraryUtils {
	public static <T> CombinableArbitrary<T> toCombinableArbitrary(Arbitrary<T> arbitrary) {
		return CombinableArbitrary.from(LazyArbitrary.lazy(
			() -> {
				if (arbitrary != null) {
					return arbitrary.sample();
				}
				return null;
			}
		));
	}
}
