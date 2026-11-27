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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.customizer.Scope;
import com.navercorp.fixturemonkey.customizer.ScopeSelector;
import com.navercorp.fixturemonkey.customizer.ScopeSet;
import com.navercorp.objectfarm.api.node.SeedState;

class NamedScopeResolverTest {
	private static final ScopeSelector ORDER = ScopeSelector.of(new AssignableTypeMatcher(Order.class));

	@Test
	void choosingAlternativeKeepsSharedSnapshotSequence() {
		// given
		SeedState seedState = new SeedState(0);
		NamedScopeResolver sut = new NamedScopeResolver(seedState);

		// when
		sut.resolve(
			Arrays.asList(named("delivered"), named("cancelled")),
			Collections.emptyList(),
			Collections.emptyMap()
		);

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

		List<String> selectedNames = Arrays.asList("cancelled", "delivered");

		// when
		List<Scope> actual = sut.resolve(
			Arrays.asList(named("delivered"), named("cancelled"), named("returned")),
			selectedNames,
			Collections.emptyMap()
		);

		// then
		ScopeSet scopeSet = scopeSetOf(actual, selectedNames);
		then(actual).extracting(Scope::getName).containsExactly("delivered", "cancelled");
		then(actual).extracting(scopeSet::getSelectionOrder).containsExactly(2, 1);
	}

	@Test
	void selectedAlternativeKeepsItsPriority() {
		// given
		Scope registered = new Scope(ORDER, -100, Collections.emptyList(), Collections.emptyMap());
		NamedScopeResolver sut = new NamedScopeResolver(new SeedState(0));

		List<String> selectedNames = Collections.singletonList("delivered");

		// when
		List<Scope> actual = sut.resolve(
			Arrays.asList(registered, named("delivered")),
			selectedNames,
			Collections.emptyMap()
		);

		// then
		ScopeSet scopeSet = scopeSetOf(actual, selectedNames);
		then(actual.get(1).getPriority()).isEqualTo(Integer.MAX_VALUE);
		then(scopeSet.getSelectionOrder(actual.get(1))).isEqualTo(1);
		then(scopeSet.getSelectionOrder(actual.get(0))).isZero();
	}

	@Test
	void alternativeIsPickedInProportionToItsWeight() {
		// given
		List<Scope> candidates = Arrays.asList(named("delivered"), named("cancelled"));
		Map<String, Integer> weightsByName = new HashMap<>();
		weightsByName.put("delivered", 1);
		weightsByName.put("cancelled", 3);

		// when
		List<String> actual = LongStream.rangeClosed(-1, 4)
			.mapToObj(point -> NamedScopeResolver.pickByWeight(candidates, weightsByName, randomOf(point)).getName())
			.collect(Collectors.toList());

		// then
		then(actual).containsExactly("cancelled", "delivered", "cancelled", "cancelled", "cancelled", "delivered");
	}

	@Test
	void onlySelectedAlternativeIsMarkedSelected() {
		// given
		NamedScopeResolver sut = new NamedScopeResolver(new SeedState(0));
		List<Scope> scopes = Arrays.asList(named("delivered"), named("cancelled"));

		List<String> selectedNames = Collections.singletonList("cancelled");

		// when
		List<Scope> selected = sut.resolve(scopes, selectedNames, Collections.emptyMap());
		List<Scope> chosen = sut.resolve(scopes, Collections.emptyList(), Collections.emptyMap());

		// then
		then(scopeSetOf(selected, selectedNames).getSelectionOrder(selected.get(0))).isPositive();
		then(scopeSetOf(chosen, Collections.emptyList()).getSelectionOrder(chosen.get(0))).isZero();
	}

	@Test
	void scopeWithoutNameIsKept() {
		// given
		Scope unnamed = new Scope(ORDER, Integer.MAX_VALUE, Collections.emptyList(), Collections.emptyMap());
		NamedScopeResolver sut = new NamedScopeResolver(new SeedState(0));

		// when
		List<Scope> actual = sut.resolve(
			Arrays.asList(unnamed, named("delivered"), named("cancelled")),
			Collections.singletonList("cancelled"),
			Collections.emptyMap()
		);

		// then
		then(actual).extracting(Scope::getName).containsExactly(null, "cancelled");
	}

	private static Scope named(String name) {
		return new Scope(ORDER, Integer.MAX_VALUE, Collections.emptyList(), Collections.emptyMap(), name);
	}

	private static ScopeSet scopeSetOf(List<Scope> definedScopes, List<String> selectedNames) {
		return new ScopeSet(Scope.root(Collections.emptyList(), Collections.emptyMap()), definedScopes, selectedNames);
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
			.map(it -> resolver.resolve(it, Collections.emptyList(), Collections.emptyMap()).get(0).getName())
			.collect(Collectors.toList());
	}

	static class Order {
	}
}
