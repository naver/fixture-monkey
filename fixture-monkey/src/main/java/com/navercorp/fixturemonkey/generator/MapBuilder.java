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
			Arbitrary<?> keyArbitrary = (Arbitrary<?>)nodes.get(nodeIndex).getArbitrary().unique();
			Arbitrary<?> valueArbitrary = (Arbitrary<?>)nodes.get(nodeIndex + 1).getArbitrary();
			mapBuilder = mapBuilder
				.use(keyArbitrary).in(MapBuilderFrame::key)
				.use(valueArbitrary).in(MapBuilderFrame::value);
		}

		return (Arbitrary<T>)mapBuilder.build(MapBuilderFrame::build);
	}

	private static class MapBuilderFrame<K, V> {
		private final Map<K, V> map;
		private K key;

		public MapBuilderFrame() {
			map = new HashMap<>();
		}

		MapBuilderFrame<K, V> key(K key) {
			if (map.containsKey(key)) {
				throw new IllegalArgumentException("Map has duplicated Key : " + key);
			}
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
