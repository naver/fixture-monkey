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

import static com.navercorp.fixturemonkey.api.jqwik.ArbitraryUtils.newThreadSafeArbitrary;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.EdgeCases;
import net.jqwik.api.ExhaustiveGenerator;
import net.jqwik.api.RandomDistribution;
import net.jqwik.api.RandomGenerator;
import net.jqwik.api.Shrinkable;
import net.jqwik.api.arbitraries.CharacterArbitrary;
import net.jqwik.api.arbitraries.StringArbitrary;
import net.jqwik.api.support.HashCodeSupport;
import net.jqwik.engine.properties.arbitraries.DefaultCharacterArbitrary;
import net.jqwik.engine.properties.arbitraries.EdgeCasesSupport;
import net.jqwik.engine.properties.arbitraries.exhaustive.ExhaustiveGenerators;
import net.jqwik.engine.properties.arbitraries.randomized.RandomGenerators;
import net.jqwik.engine.properties.shrinking.ShrinkableString;

/**
 * A StringArbitrary instance which supports filtering with a predicate
 * Same implementation as DefaultStringArbitrary
 */
@API(since = "0.6.0", status = Status.MAINTAINED)
public final class MonkeyStringArbitrary implements StringArbitrary {
	private CharacterArbitrary characterArbitrary = new DefaultCharacterArbitrary();

	private int minLength = 0;
	private @Nullable Integer maxLength = null;
	private Predicate<Character> filter = c -> true;
	@SuppressWarnings("assignment")
	private RandomDistribution lengthDistribution = null;
	private double repeatChars = 0.0;

	@Override
	public RandomGenerator<String> generator(int genSize) {
		long maxUniqueChars = characterArbitrary
			.exhaustive(maxLength())
			.<@NonNull Long>map(ExhaustiveGenerator::maxCount)
			.orElse((long)maxLength());
		return RandomGenerators.strings(
			randomCharacterGenerator(),
			minLength, maxLength(), maxUniqueChars,
			genSize, lengthDistribution,
			newThreadSafeArbitrary(characterArbitrary)
		);
	}

	// Removing a StoreRepository dependency, it is not useful without Jqwik engine.
	@Override
	public RandomGenerator<String> generator(int genSize, boolean withEdgeCases) {
		return this.generator(genSize);
	}

	@SuppressWarnings("argument")
	private int maxLength() {
		return RandomGenerators.collectionMaxSize(minLength, maxLength);
	}

	@Override
	public Optional<ExhaustiveGenerator<String>> exhaustive(long maxNumberOfSamples) {
		return ExhaustiveGenerators.strings(
			effectiveCharacterArbitrary(),
			minLength,
			maxLength(),
			maxNumberOfSamples
		);
	}

	@Override
	public EdgeCases<String> edgeCases(int maxEdgeCases) {
		// Optimization. Already handled by EdgeCases.concat(..)
		if (maxEdgeCases <= 0) {
			return EdgeCases.none();
		}

		EdgeCases<String> emptyStringEdgeCases =
			hasEmptyStringEdgeCase() ? emptyStringEdgeCase() : EdgeCases.none();

		int effectiveMaxEdgeCases = maxEdgeCases - emptyStringEdgeCases.size();
		EdgeCases<String> singleCharEdgeCases =
			hasSingleCharEdgeCases() ? fixedSizedEdgeCases(1, effectiveMaxEdgeCases) : EdgeCases.none();

		effectiveMaxEdgeCases = effectiveMaxEdgeCases - singleCharEdgeCases.size();
		EdgeCases<String> fixedSizeEdgeCases =
			hasMultiCharEdgeCases() ? fixedSizedEdgeCases(minLength, effectiveMaxEdgeCases) : EdgeCases.none();

		return EdgeCasesSupport.concat(asList(singleCharEdgeCases, emptyStringEdgeCases, fixedSizeEdgeCases),
			maxEdgeCases);
	}

	private boolean hasEmptyStringEdgeCase() {
		return minLength <= 0;
	}

	private boolean hasMultiCharEdgeCases() {
		return minLength <= maxLength() && minLength > 1;
	}

	private boolean hasSingleCharEdgeCases() {
		return minLength <= 1 && maxLength() >= 1;
	}

	private EdgeCases<String> emptyStringEdgeCase() {
		return EdgeCases.fromSupplier(
			() -> new ShrinkableString(Collections.emptyList(), minLength, maxLength(), characterArbitrary));
	}

	private EdgeCases<String> fixedSizedEdgeCases(int fixedSize, int maxEdgeCases) {
		return EdgeCasesSupport.mapShrinkable(
			effectiveCharacterArbitrary().edgeCases(maxEdgeCases),
			shrinkableChar -> {
				List<Shrinkable<Character>> shrinkableChars = new ArrayList<>(
					Collections.nCopies(fixedSize, shrinkableChar));
				return new ShrinkableString(shrinkableChars, minLength, maxLength(), characterArbitrary);
			}
		);
	}

	@Override
	public StringArbitrary ofMinLength(int minLength) {
		if (minLength < 0) {
			String message = String.format("minLength (%s) must be between 0 and 2147483647", minLength);
			throw new IllegalArgumentException(message);
		}
		this.minLength = minLength;
		return this;
	}

	@Override
	public StringArbitrary ofMaxLength(int maxLength) {
		if (maxLength < 0) {
			String message = String.format("maxLength (%s) must be between 0 and 2147483647", maxLength);
			throw new IllegalArgumentException(message);
		}
		if (maxLength < minLength) {
			String message = String.format("minLength (%s) must not be larger than maxLength (%s)", minLength,
				maxLength);
			throw new IllegalArgumentException(message);
		}

		this.maxLength = maxLength;
		return this;
	}

	@Override
	public StringArbitrary withLengthDistribution(RandomDistribution distribution) {
		this.lengthDistribution = distribution;
		return this;
	}

	@Override
	public StringArbitrary repeatChars(double repeatProbability) {
		if (repeatProbability < 0 || repeatProbability >= 1) {
			throw new IllegalArgumentException("repeatProbability must be between 0 (included) and 1 (excluded)");
		}
		this.repeatChars = repeatProbability;
		return this;
	}

	@Override
	public StringArbitrary withChars(char... chars) {
		this.characterArbitrary = this.characterArbitrary.with(chars);
		return this;
	}

	@Override
	public StringArbitrary withChars(CharSequence chars) {
		this.characterArbitrary = this.characterArbitrary.with(chars);
		return this;
	}

	@Override
	public StringArbitrary withCharRange(char from, char to) {
		this.characterArbitrary = this.characterArbitrary.range(from, to);
		return this;
	}

	@Override
	public StringArbitrary ascii() {
		this.characterArbitrary = characterArbitrary.ascii();
		return this;
	}

	@Override
	public StringArbitrary alpha() {
		this.characterArbitrary = this.characterArbitrary.alpha();
		return this;
	}

	public StringArbitrary korean() {
		this.characterArbitrary = this.characterArbitrary.range('가', '힣');
		return this;
	}

	@Override
	public StringArbitrary numeric() {
		this.characterArbitrary = this.characterArbitrary.numeric();
		return this;
	}

	@Override
	public StringArbitrary whitespace() {
		this.characterArbitrary = this.characterArbitrary.whitespace();
		return this;
	}

	@Override
	public StringArbitrary all() {
		return this.withCharRange(Character.MIN_VALUE, Character.MAX_VALUE);
	}

	@Override
	public StringArbitrary excludeChars(char... charsToExclude) {
		Set<Character> excludedChars = new LinkedHashSet<>();
		for (char c : charsToExclude) {
			excludedChars.add(c);
		}
		this.filterCharacter(c -> !excludedChars.contains(c));
		return this;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		MonkeyStringArbitrary that = (MonkeyStringArbitrary)obj;
		if (minLength != that.minLength) {
			return false;
		}
		if (!Objects.equals(maxLength, that.maxLength)) {
			return false;
		}
		if (Double.compare(that.repeatChars, repeatChars) != 0) {
			return false;
		}
		if (!characterArbitrary.equals(that.characterArbitrary)) {
			return false;
		}
		if (!filter.equals(that.filter)) {
			return false;
		}
		return Objects.equals(lengthDistribution, that.lengthDistribution);
	}

	@Override
	public int hashCode() {
		return HashCodeSupport.hash(characterArbitrary, minLength, maxLength, repeatChars, filter, lengthDistribution);
	}

	public MonkeyStringArbitrary filterCharacter(Predicate<Character> predicate) {
		this.filter = this.filter.and(predicate);
		return this;

	}

	private RandomGenerator<Character> randomCharacterGenerator() {
		RandomGenerator<Character> characterGenerator = effectiveCharacterArbitrary().generator(1, false);
		if (repeatChars > 0) {
			return characterGenerator.injectDuplicates(repeatChars);
		} else {
			return characterGenerator;
		}
	}

	private Arbitrary<Character> effectiveCharacterArbitrary() {
		Arbitrary<Character> characterArbitrary = this.characterArbitrary;
		return newThreadSafeArbitrary(characterArbitrary.filter(this.filter));
	}
}
