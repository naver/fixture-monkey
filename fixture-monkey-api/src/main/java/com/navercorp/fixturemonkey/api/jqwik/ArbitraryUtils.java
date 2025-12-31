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
import net.jqwik.api.EdgeCases;
import net.jqwik.api.RandomGenerator;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;

@SuppressWarnings("NullableProblems")
@API(since = "0.6.9", status = Status.MAINTAINED)
public abstract class ArbitraryUtils {
	@SuppressWarnings("return")
	public static <T> CombinableArbitrary<T> toCombinableArbitrary(Arbitrary<T> arbitrary) {
		return CombinableArbitrary.from(LazyArbitrary.lazy(
			() -> {
				if (arbitrary != null) {
					return newThreadSafeArbitrary(arbitrary).sample();
				}
				return null;
			}
		));
	}

	public static <T> Arbitrary<T> newThreadSafeArbitrary(Arbitrary<T> delegate) {
		return new Arbitrary<T>() {
			@Override
			public RandomGenerator<T> generator(int genSize) {
				return delegate.generator(genSize);
			}

			// Removing a StoreRepository dependency, it is not useful without Jqwik engine.
			@Override
			public RandomGenerator<T> generator(int genSize, boolean withEdgeCases) {
				return delegate.generator(genSize);
			}

			@Override
			public EdgeCases<T> edgeCases(int maxEdgeCases) {
				return delegate.edgeCases(maxEdgeCases);
			}
		};
	}
}
