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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * It combines given element {@link CombinableArbitrary} list into a container type {@link CombinableArbitrary}.
 */
@API(since = "0.6.0", status = Status.MAINTAINED)
final class ContainerCombinableArbitrary<T> implements CombinableArbitrary<T> {
	private static final Object EXISTED = new Object();

	private final List<CombinableArbitrary<?>> combinableArbitraryList;
	private final Function<List<Object>, T> combinator;
	private final Map<Object, Object> generatedMap;

	ContainerCombinableArbitrary(
		List<CombinableArbitrary<?>> combinableArbitraryList,
		Function<List<Object>, T> combinator,
		Map<Object, Object> generatedMap
	) {
		this.combinableArbitraryList = combinableArbitraryList;
		this.combinator = combinator;
		this.generatedMap = generatedMap;
	}

	@Override
	@SuppressWarnings("type.arguments.not.inferred")
	public T combined() {
		List<Object> combinedList = combinableArbitraryList.stream()
			.map(CombinableArbitrary::combined)
			.collect(Collectors.toList());

		return combinator.apply(combinedList);
	}

	@Override
	@SuppressWarnings({"return"})
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
		if (combinableArbitraryList.isEmpty()) {
			return false;
		}

		return combinableArbitraryList.stream()
			.allMatch(CombinableArbitrary::fixed);
	}

	@Override
	@SuppressWarnings("argument")
	public CombinableArbitrary<T> unique() {
		List<CombinableArbitrary<?>> uniqueCombinableArbitraryList = this.combinableArbitraryList.stream()
			.map(arbitrary -> arbitrary.filter(it -> {
				if (!generatedMap.containsKey(it)) {
					generatedMap.put(it, EXISTED);
					return true;
				}
				return false;
			}))
			.collect(Collectors.toList());

		return CombinableArbitrary.containerBuilder()
			.elements(uniqueCombinableArbitraryList)
			.build(this.combinator);
	}
}
