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

package com.navercorp.fixturemonkey.api.generator;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * It would generate unique element objects.
 * It would repeat {@link #MAX_TRIES} times to make unique element objects.
 */
@API(since = "0.6.0", status = Status.EXPERIMENTAL)
public final class UniqueContainerCombinableArbitrary implements CombinableArbitrary {
	private static final int MAX_UNIQUE_TRIES = 1_000;

	private final List<CombinableArbitrary> combinableArbitraryList;
	private final Predicate<Object> cachePutAndUnqiuePredicate;
	private final Runnable evictUnique;
	private final Function<List<Object>, Object> combinator;

	public UniqueContainerCombinableArbitrary(
		List<CombinableArbitrary> combinableArbitraryList,
		Predicate<Object> cachePutAndUnqiuePredicate,
		Runnable evictUnique,
		Function<List<Object>, Object> combinator
	) {
		this.combinableArbitraryList = combinableArbitraryList;
		this.cachePutAndUnqiuePredicate = cachePutAndUnqiuePredicate;
		this.evictUnique = evictUnique;
		this.combinator = combinator;
	}

	@Override
	public Object combined() {
		List<Object> combinedList = combinableArbitraryList.stream()
			.map(it -> new FilteredCombinableArbitrary(MAX_UNIQUE_TRIES, it, cachePutAndUnqiuePredicate))
			.map(CombinableArbitrary::combined)
			.collect(Collectors.toList());

		Object combined = combinator.apply(combinedList);
		evictUnique.run();
		return combined;
	}

	@Override
	public Object rawValue() {
		List<Object> combinedList = combinableArbitraryList.stream()
			.map(it -> new FilteredCombinableArbitrary(MAX_UNIQUE_TRIES, it, cachePutAndUnqiuePredicate))
			.map(CombinableArbitrary::rawValue)
			.collect(Collectors.toList());

		Object combined = combinator.apply(combinedList);
		evictUnique.run();
		return combined;
	}

	@Override
	public void clear() {
		combinableArbitraryList.forEach(CombinableArbitrary::clear);
	}

	@Override
	public boolean fixed() {
		return combinableArbitraryList.stream().allMatch(CombinableArbitrary::fixed);
	}
}
