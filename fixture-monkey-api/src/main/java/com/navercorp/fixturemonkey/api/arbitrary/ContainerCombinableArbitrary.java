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

import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;

/**
 * It combines given element {@link CombinableArbitrary} list into a container type {@link CombinableArbitrary}.
 */
@API(since = "0.6.0", status = Status.MAINTAINED)
final class ContainerCombinableArbitrary<T> implements CombinableArbitrary<T> {
	private final List<CombinableArbitrary<?>> combinableArbitraryList;
	private final Function<List<Object>, T> combinator;

	ContainerCombinableArbitrary(
		List<CombinableArbitrary<?>> combinableArbitraryList,
		Function<List<Object>, T> combinator
	) {
		this.combinableArbitraryList = combinableArbitraryList;
		this.combinator = combinator;
	}

	@Override
	public T combined() {
		List<Object> combinedList = combinableArbitraryList.stream()
			.map(CombinableArbitrary::combined)
			.collect(Collectors.toList());

		return combinator.apply(combinedList);
	}

	@Override
	public Object rawValue() {
		List<Object> rawValues = combinableArbitraryList.stream()
			.map(CombinableArbitrary::rawValue)
			.collect(Collectors.toList());

		return combinator.apply(rawValues);
	}

	@Override
	public void clear() {
		combinableArbitraryList.forEach(arbitrary -> {
			if (!arbitrary.fixed()) {
				arbitrary.clear();
			}
		});
	}

	@Override
	public boolean fixed() {
		return combinableArbitraryList.stream()
			.allMatch(CombinableArbitrary::fixed);
	}

	@Override
	public CombinableArbitrary<T> unique(Map<Object, Object> uniqueMap) {
		List<CombinableArbitrary<?>> uniqueCombinableArbitraryList = this.combinableArbitraryList.stream()
			.map(it -> it.unique(uniqueMap))
			.collect(Collectors.toList());

		return CombinableArbitrary.containerBuilder()
			.elements(uniqueCombinableArbitraryList)
			.build(this.combinator);
	}
}
