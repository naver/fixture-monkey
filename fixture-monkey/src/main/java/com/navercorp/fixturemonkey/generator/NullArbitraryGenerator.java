package com.navercorp.fixturemonkey.generator;

import java.util.List;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryType;

public final class NullArbitraryGenerator implements ArbitraryGenerator {
	public static final NullArbitraryGenerator INSTANCE = new NullArbitraryGenerator();

	@SuppressWarnings("rawtypes")
	@Override
	public <T> Arbitrary<T> generate(ArbitraryType type, List<ArbitraryNode> nodes) {
		return Arbitraries.just(null);
	}
}
