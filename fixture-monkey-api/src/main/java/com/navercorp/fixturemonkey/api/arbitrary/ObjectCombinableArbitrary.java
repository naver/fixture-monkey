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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;

/**
 * It combines given {@link CombinableArbitrary} list into an object type {@link CombinableArbitrary}.
 */
@API(since = "0.6.0", status = Status.MAINTAINED)
final class ObjectCombinableArbitrary<T> implements CombinableArbitrary<T> {
	private final Map<ArbitraryProperty, CombinableArbitrary<?>> combinableArbitrariesByArbitraryProperty;
	private final Function<Map<ArbitraryProperty, Object>, T> combinator;

	ObjectCombinableArbitrary(
		Map<ArbitraryProperty, CombinableArbitrary<?>> combinableArbitrariesByArbitraryProperty,
		Function<Map<ArbitraryProperty, Object>, T> combinator
	) {
		this.combinableArbitrariesByArbitraryProperty = combinableArbitrariesByArbitraryProperty;
		this.combinator = combinator;
	}

	@SuppressWarnings("argument")
	@Override
	public T combined() {
		Map<ArbitraryProperty, Object> combinedPropertyValuesByArbitraryProperty = new HashMap<>();

		combinableArbitrariesByArbitraryProperty.forEach(
			(key, value) -> combinedPropertyValuesByArbitraryProperty.put(key, value.combined())
		);

		return combinator.apply(combinedPropertyValuesByArbitraryProperty);
	}

	@SuppressWarnings({"return", "argument"})
	@Override
	public Object rawValue() {
		Map<ArbitraryProperty, Object> rawPropertyValuesByArbitraryProperty = new HashMap<>();

		combinableArbitrariesByArbitraryProperty.forEach(
			(key, value) -> rawPropertyValuesByArbitraryProperty.put(key, value.rawValue())
		);

		return combinator.apply(rawPropertyValuesByArbitraryProperty);
	}

	@Override
	public void clear() {
		combinableArbitrariesByArbitraryProperty.forEach((property, arbitrary) -> {
			if (!arbitrary.fixed()) {
				arbitrary.clear();
			}
		});
	}

	@Override
	public boolean fixed() {
		return combinableArbitrariesByArbitraryProperty.values().stream().allMatch(CombinableArbitrary::fixed);
	}
}
