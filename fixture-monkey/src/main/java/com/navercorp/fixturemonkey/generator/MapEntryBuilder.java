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

package com.navercorp.fixturemonkey.generator;

import java.util.List;
import java.util.Map;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;

final class MapEntryBuilder {
	public static MapEntryBuilder INSTANCE = new MapEntryBuilder();

	@SuppressWarnings({"rawtypes", "unchecked"})
	<T> Arbitrary<T> build(List<ArbitraryNode> nodes) {
		if (nodes.isEmpty()) {
			return Arbitraries.just(null);
		}
		Arbitrary<?> keyArbitrary = (Arbitrary<?>)nodes.get(0).getArbitrary();
		Arbitrary<?> valueArbitrary = (Arbitrary<?>)nodes.get(1).getArbitrary();

		Arbitrary<T> mapArbitrary = (Arbitrary<T>)Arbitraries.maps(keyArbitrary, valueArbitrary).ofSize(1);
		return (Arbitrary<T>)mapArbitrary.map(m -> {
			if (m == null) {
				return null;
			}

			Map<?, ?> map = (Map<?, ?>)m;
			if (map.isEmpty()) {
				return null;
			}
			return map.entrySet().iterator().next();
		});
	}
}
