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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.Combinators.BuilderCombinator;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;

final class MapBuilder {
	public static MapBuilder INSTANCE = new MapBuilder();

	@SuppressWarnings({"rawtypes", "unchecked"})
	<T> Arbitrary<T> build(List<ArbitraryNode> nodes) {
		BuilderCombinator<MapBuilderFrame> mapBuilder =
			Combinators.withBuilder(MapBuilderFrame::new);

		if (nodes.isEmpty()) {
			return (Arbitrary<T>)mapBuilder.build(MapBuilderFrame::build);
		}

		if (nodes.size() % 2 != 0) {
			throw new IllegalArgumentException("Key and Value should be existed.");
		}

		for (int i = 0; i < nodes.size() / 2; i++) {
			int nodeIndex = i * 2;
			Arbitrary<?> keyArbitrary = (Arbitrary<?>)nodes.get(nodeIndex).getArbitrary();
			Arbitrary<?> valueArbitrary = (Arbitrary<?>)nodes.get(nodeIndex + 1).getArbitrary();
			mapBuilder = mapBuilder
				.use(keyArbitrary).in(MapBuilderFrame::key)
				.use(valueArbitrary).in(MapBuilderFrame::value);
		}

		return (Arbitrary<T>)mapBuilder.build(MapBuilderFrame::build)
			.filter(it -> it.size() == nodes.size() / 2);
	}

	private static class MapBuilderFrame<K, V> {
		private final Map<K, V> map;
		private K key;

		public MapBuilderFrame() {
			map = new HashMap<>();
		}

		MapBuilderFrame<K, V> key(K key) {
			this.key = key;
			return this;
		}

		MapBuilderFrame<K, V> value(V value) {
			if (key == null) {
				throw new IllegalArgumentException("Key can not be null when builder map.");
			}
			map.put(this.key, value);
			return this;
		}

		Map<K, V> build() {
			return new HashMap<>(map);
		}
	}
}
