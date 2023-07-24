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

@API(since = "0.6.0", status = Status.EXPERIMENTAL)
public final class ObjectCombineArbitraryBuilder {
	private final Map<ArbitraryProperty, CombinableArbitrary<?>> arbitraryListByArbitraryProperty;

	ObjectCombineArbitraryBuilder() {
		this.arbitraryListByArbitraryProperty = new HashMap<>();
	}

	public ObjectCombineArbitraryBuilder property(ArbitraryProperty property, CombinableArbitrary<?> arbitrary) {
		arbitraryListByArbitraryProperty.put(property, arbitrary);
		return this;
	}

	public ObjectCombineArbitraryBuilder properties(
		Map<ArbitraryProperty, CombinableArbitrary<?>> arbitraryListByArbitraryProperty
	) {
		this.arbitraryListByArbitraryProperty.putAll(arbitraryListByArbitraryProperty);
		return this;
	}

	public <T> CombinableArbitrary<T> build(Function<Map<ArbitraryProperty, Object>, T> combinator) {
		return new ObjectCombinableArbitrary<>(
			arbitraryListByArbitraryProperty,
			combinator
		);
	}
}
