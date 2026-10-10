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

package com.navercorp.fixturemonkey.planner;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.customizer.Scope;
import com.navercorp.fixturemonkey.customizer.ScopeSelector;
import com.navercorp.objectfarm.api.node.SeedState;

class NamedScopeResolverTest {
	private static final ScopeSelector ORDER = ScopeSelector.of(new AssignableTypeMatcher(Order.class));

	@Test
	void choosingAlternativeKeepsSharedSnapshotSequence() {
		// given
		SeedState seedState = new SeedState(0);
		NamedScopeResolver sut = new NamedScopeResolver(seedState);

		// when
		sut.resolve(Arrays.asList(named("delivered"), named("cancelled")), Collections.emptyList());

		// then
		then(seedState.getCurrentSequence()).isZero();
	}

	@Test
	void sameSeedChoosesSameAlternatives() {
		// given
		List<Scope> scopes = Arrays.asList(named("delivered"), named("cancelled"));

		// when
		List<String> first = chosenNames(new NamedScopeResolver(new SeedState(7)), scopes);
		List<String> second = chosenNames(new NamedScopeResolver(new SeedState(7)), scopes);

		// then
		then(first).contains("delivered", "cancelled");
		then(second).isEqualTo(first);
	}

	@Test
	void laterSelectedAlternativeTakesHigherSelectionOrder() {
		// given
		NamedScopeResolver sut = new NamedScopeResolver(new SeedState(0));

		// when
		List<Scope> actual = sut.resolve(
			Arrays.asList(named("delivered"), named("cancelled"), named("returned")),
			Arrays.asList("cancelled", "delivered")
		);

		// then
		then(actual).extracting(Scope::getName).containsExactly("delivered", "cancelled");
		then(actual).extracting(Scope::getSelectionOrder).containsExactly(2, 1);
	}

	@Test
	void selectedAlternativeKeepsItsPriority() {
		// given
		Scope registered = new Scope(ORDER, -100, Collections.emptyList(), Collections.emptyMap());
		NamedScopeResolver sut = new NamedScopeResolver(new SeedState(0));

		// when
		List<Scope> actual = sut.resolve(
			Arrays.asList(registered, named("delivered")),
			Collections.singletonList("delivered")
		);

		// then
		then(actual.get(1).getPriority()).isEqualTo(Integer.MAX_VALUE);
		then(actual.get(1).getSelectionOrder()).isEqualTo(1);
		then(actual.get(0).getSelectionOrder()).isZero();
	}

	@Test
	void alternativeIsPickedInProportionToItsWeight() {
		// given
		List<Scope> candidates = Arrays.asList(named("delivered", 1), named("cancelled", 3));

		// when
		List<String> actual = LongStream.rangeClosed(-1, 4)
			.mapToObj(point -> NamedScopeResolver.pickByWeight(candidates, randomOf(point)).getName())
			.collect(Collectors.toList());

		// then
		then(actual).containsExactly("cancelled", "delivered", "cancelled", "cancelled", "cancelled", "delivered");
	}

	@Test
	void onlySelectedAlternativeIsMarkedSelected() {
		// given
		NamedScopeResolver sut = new NamedScopeResolver(new SeedState(0));
		List<Scope> scopes = Arrays.asList(named("delivered"), named("cancelled"));

		// when
		Scope selected = sut.resolve(scopes, Collections.singletonList("cancelled")).get(0);
		Scope chosen = sut.resolve(scopes, Collections.emptyList()).get(0);

		// then
		then(selected.isSelected()).isTrue();
		then(chosen.isSelected()).isFalse();
	}

	@Test
	void scopeWithoutNameIsKept() {
		// given
		Scope unnamed = new Scope(ORDER, Integer.MAX_VALUE, Collections.emptyList(), Collections.emptyMap());
		NamedScopeResolver sut = new NamedScopeResolver(new SeedState(0));

		// when
		List<Scope> actual = sut.resolve(
			Arrays.asList(unnamed, named("delivered"), named("cancelled")),
			Collections.singletonList("cancelled")
		);

		// then
		then(actual).extracting(Scope::getName).containsExactly(null, "cancelled");
	}

	private static Scope named(String name) {
		return named(name, 1);
	}

	private static Scope named(String name, int weight) {
		return new Scope(ORDER, Integer.MAX_VALUE, Collections.emptyList(), Collections.emptyMap(), name, weight);
	}

	private static Random randomOf(long value) {
		return new Random() {
			@Override
			public long nextLong() {
				return value;
			}
		};
	}

	private static List<String> chosenNames(NamedScopeResolver resolver, List<Scope> scopes) {
		return Collections.nCopies(30, scopes).stream()
			.map(it -> resolver.resolve(it, Collections.emptyList()).get(0).getName())
			.collect(Collectors.toList());
	}

	static class Order {
	}
}
