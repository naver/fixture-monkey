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
