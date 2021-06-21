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
