package com.navercorp.fixturemonkey.generator;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

public class BuilderFieldArbitraries<B> extends FieldArbitraries {
	private final Class<B> builderType;
	@SuppressWarnings("rawtypes")
	private final List<Map.Entry<Arbitrary, Combinators.F2<B, ?, B>>> combinationChains;

	@SuppressWarnings("rawtypes")
	private BuilderFieldArbitraries(Class<B> builderType, Map<String, Arbitrary> arbitraryMap) {
		super(arbitraryMap);
		this.builderType = builderType;
		this.combinationChains = new ArrayList<>();
	}

	public static <B> BuilderFieldArbitraries<B> withBuilderType(Class<B> builderType) {
		return withBuilderType(builderType, new HashMap<>());
	}

	@SuppressWarnings("rawtypes")
	public static <B> BuilderFieldArbitraries<B> withBuilderType(
		Class<B> builderType, Map<String, Arbitrary> arbitraryMap) {

		return new BuilderFieldArbitraries<>(builderType, arbitraryMap);
	}

	public Class<B> getBuilderType() {
		return this.builderType;
	}

	public <T> CombinableBuilder<B, T> use(Arbitrary<T> arbitrary) {
		return new BuilderCombinator<>(this).use(arbitrary);
	}

	public BuilderFieldArbitraries<B> reset() {
		this.combinationChains.clear();
		return this;
	}

	@Override
	public BuilderFieldArbitraries<B> clear() {
		super.clear();
		return this;
	}

	@SuppressWarnings("rawtypes")
	public List<Map.Entry<Arbitrary, Combinators.F2<B, ?, B>>> getCombinationChains() {
		return Collections.unmodifiableList(this.combinationChains);
	}

	public static class BuilderCombinator<B> {
		private final BuilderFieldArbitraries<B> builderFieldArbitraries;

		private BuilderCombinator(BuilderFieldArbitraries<B> builderFieldArbitraries) {
			this.builderFieldArbitraries = builderFieldArbitraries;
		}

		public <T> CombinableBuilder<B, T> use(Arbitrary<T> arbitrary) {
			return new CombinableBuilder<>(this.builderFieldArbitraries, arbitrary);
		}

		public BuilderFieldArbitraries<B> build() {
			return this.builderFieldArbitraries;
		}
	}

	public static class CombinableBuilder<B, T> {
		private final BuilderFieldArbitraries<B> builderFieldArbitraries;
		private final Arbitrary<T> arbitrary;

		private CombinableBuilder(BuilderFieldArbitraries<B> builderFieldArbitraries, Arbitrary<T> arbitrary) {
			this.builderFieldArbitraries = builderFieldArbitraries;
			this.arbitrary = arbitrary;
		}

		public BuilderCombinator<B> in(Combinators.F2<B, T, B> toFunction) {
			this.builderFieldArbitraries.combinationChains.add(
				new AbstractMap.SimpleEntry<>(this.arbitrary, toFunction));
			return new BuilderCombinator<>(this.builderFieldArbitraries);
		}
	}
}
