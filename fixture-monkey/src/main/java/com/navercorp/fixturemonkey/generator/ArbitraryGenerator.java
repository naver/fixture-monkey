package com.navercorp.fixturemonkey.generator;

import java.util.List;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryType;

@FunctionalInterface
public interface ArbitraryGenerator {
	@SuppressWarnings("rawtypes")
	<T> Arbitrary<T> generate(ArbitraryType type, List<ArbitraryNode> nodes);
}
