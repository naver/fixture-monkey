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

package com.navercorp.fixturemonkey.api.introspector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.EdgeCases;
import net.jqwik.api.ExhaustiveGenerator;
import net.jqwik.api.RandomGenerator;
import net.jqwik.api.Shrinkable;
import net.jqwik.api.support.HashCodeSupport;
import net.jqwik.api.support.LambdaSupport;
import net.jqwik.engine.properties.arbitraries.EdgeCasesSupport;
import net.jqwik.engine.properties.arbitraries.exhaustive.ExhaustiveGenerators;
import net.jqwik.engine.properties.shrinking.CombinedShrinkable;

/**
 * For combining arbitraries to create an object
 * It is a same Arbitrary instance as {@link net.jqwik.engine.properties.arbitraries.CombineArbitrary}
 * Added some features for Fixture Monkey
 */
// TODO: move package when unique manipulation works for any type
@SuppressWarnings("NullableProblems")
@API(since = "0.4.6", status = Status.EXPERIMENTAL)
final class MonkeyCombineArbitrary implements Arbitrary<Object> {
	private final Function<List<Object>, Object> combinator;
	private final Runnable combinationCallback;
	private final List<Arbitrary<Object>> arbitraries;

	public MonkeyCombineArbitrary(
		Function<List<Object>, Object> combinator,
		Runnable combinationCallback,
		List<Arbitrary<Object>> arbitraries
	) {
		this.combinator = combinator;
		this.combinationCallback = combinationCallback;
		this.arbitraries = arbitraries;
	}

	@Override
	public RandomGenerator<Object> generator(int genSize) {
		return combineGenerator(genSize, combinator, arbitraries);
	}

	@Override
	public RandomGenerator<Object> generatorWithEmbeddedEdgeCases(int genSize) {
		return combineGeneratorWithEmbeddedEdgeCases(genSize, combinator, arbitraries);
	}

	@Override
	public Optional<ExhaustiveGenerator<Object>> exhaustive(long maxNumberOfSamples) {
		return combineExhaustive(
			arbitraries,
			combinator,
			maxNumberOfSamples
		);
	}

	@Override
	public boolean isGeneratorMemoizable() {
		return isCombinedGeneratorMemoizable(arbitraries);
	}

	@Override
	public EdgeCases<Object> edgeCases(int maxEdgeCases) {
		return combineEdgeCases(
			arbitraries,
			combinator,
			maxEdgeCases
		);
	}

	private boolean isCombinedGeneratorMemoizable(List<Arbitrary<Object>> arbitraries) {
		return arbitraries.stream().allMatch(Arbitrary::isGeneratorMemoizable);
	}

	private RandomGenerator<Object> combineGenerator(
		int genSize,
		Function<List<Object>, Object> combineFunction,
		List<Arbitrary<Object>> arbitraries
	) {
		List<RandomGenerator<Object>> generators = arbitraries.stream()
			.map(a -> a.generator(genSize))
			.collect(Collectors.toList());
		return random -> {
			List<Shrinkable<Object>> shrinkables = generateShrinkables(generators, random);
			return combineShrinkables(shrinkables, combineFunction);
		};
	}

	private RandomGenerator<Object> combineGeneratorWithEmbeddedEdgeCases(
		int genSize,
		Function<List<Object>, Object> combineFunction,
		List<Arbitrary<Object>> arbitraries
	) {
		List<RandomGenerator<Object>> generators =
			arbitraries.stream()
				.map(a -> a.generatorWithEmbeddedEdgeCases(genSize))
				.collect(Collectors.toList());
		return random -> {
			List<Shrinkable<Object>> shrinkables = generateShrinkables(generators, random);
			return combineShrinkables(shrinkables, combineFunction);
		};
	}

	private List<Shrinkable<Object>> generateShrinkables(List<RandomGenerator<Object>> generators, Random random) {
		List<Shrinkable<Object>> list = new ArrayList<>();
		for (RandomGenerator<Object> generator : generators) {
			list.add(generator.next(random));
		}
		combinationCallback.run();
		return list;
	}

	private Shrinkable<Object> combineShrinkables(
		List<Shrinkable<Object>> shrinkables,
		Function<List<Object>, Object> combineFunction
	) {
		return new CombinedShrinkable<>(shrinkables, combineFunction);
	}

	private Optional<ExhaustiveGenerator<Object>> combineExhaustive(
		List<Arbitrary<Object>> arbitraries,
		Function<List<Object>, Object> combineFunction,
		long maxNumberOfSamples
	) {
		return ExhaustiveGenerators.combine(arbitraries, combineFunction, maxNumberOfSamples);
	}

	private EdgeCases<Object> combineEdgeCases(
		final List<Arbitrary<Object>> arbitraries,
		final Function<List<Object>, Object> combineFunction,
		int maxEdgeCases
	) {
		return EdgeCasesSupport.combine(arbitraries, combineFunction, maxEdgeCases);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		MonkeyCombineArbitrary that = (MonkeyCombineArbitrary)obj;
		if (!arbitraries.equals(that.arbitraries)) {
			return false;
		}
		return LambdaSupport.areEqual(combinator, that.combinator);
	}

	@Override
	public int hashCode() {
		return HashCodeSupport.hash(arbitraries);
	}
}
