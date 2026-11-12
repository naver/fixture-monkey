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

package com.navercorp.fixturemonkey.tests.java;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.customizer.Values;

class SetContainerIsolationTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@Test
	void setListIsNotSharedAcrossSamples() {
		// given
		List<String> supplied = new ArrayList<>(Arrays.asList("a", "b"));
		ArbitraryBuilder<Holder> builder = SUT.giveMeBuilder(Holder.class)
			.set("list", supplied);

		// when
		Holder first = builder.sample();
		Holder second = builder.sample();
		first.list.add("c");

		// then
		then(second.list).containsExactly("a", "b");
		then(supplied).containsExactly("a", "b");
	}

	@Test
	void setFixedSizeListIsMutableOnSample() {
		// given
		Holder actual = SUT.giveMeBuilder(Holder.class)
			.set("list", Arrays.asList("a", "b"))
			.sample();

		// when
		actual.list.remove(0);

		// then
		then(actual.list).containsExactly("b");
	}

	@Test
	void setSetIsNotSharedAcrossSamples() {
		// given
		Set<String> supplied = new HashSet<>(Arrays.asList("a", "b"));
		ArbitraryBuilder<Holder> builder = SUT.giveMeBuilder(Holder.class)
			.set("set", supplied);

		// when
		Holder first = builder.sample();
		Holder second = builder.sample();
		first.set.add("c");

		// then
		then(second.set).containsExactlyInAnyOrder("a", "b");
		then(supplied).containsExactlyInAnyOrder("a", "b");
	}

	@Test
	void setUnmodifiableSetIsMutableOnSample() {
		// given
		Holder actual = SUT.giveMeBuilder(Holder.class)
			.set("set", Collections.unmodifiableSet(new HashSet<>(Arrays.asList("a", "b"))))
			.sample();

		// when
		actual.set.remove("a");

		// then
		then(actual.set).containsExactly("b");
	}

	@Test
	void setLinkedListIsNotSharedAcrossSamples() {
		// given
		LinkedList<String> supplied = new LinkedList<>(Arrays.asList("a", "b"));
		ArbitraryBuilder<Holder> builder = SUT.giveMeBuilder(Holder.class)
			.set("linkedList", supplied);

		// when
		Holder first = builder.sample();
		Holder second = builder.sample();
		first.linkedList.add("c");

		// then
		then(second.linkedList).isInstanceOf(LinkedList.class).containsExactly("a", "b");
		then(supplied).containsExactly("a", "b");
	}

	@Test
	void setMapIsNotSharedAcrossSamples() {
		// given
		Map<String, String> supplied = new HashMap<>();
		supplied.put("a", "1");
		ArbitraryBuilder<Holder> builder = SUT.giveMeBuilder(Holder.class)
			.set("map", supplied);

		// when
		Holder first = builder.sample();
		Holder second = builder.sample();
		first.map.put("b", "2");

		// then
		then(second.map).containsOnlyKeys("a").containsEntry("a", "1");
		then(supplied).containsOnlyKeys("a");
	}

	@Test
	void setUnmodifiableMapIsMutableOnSample() {
		// given
		Holder actual = SUT.giveMeBuilder(Holder.class)
			.set("map", Collections.singletonMap("a", "1"))
			.sample();

		// when
		actual.map.put("b", "2");

		// then
		then(actual.map).containsOnlyKeys("a", "b");
	}

	@Test
	void setEmptyListIsNotSharedAcrossSamples() {
		// given
		List<String> supplied = new ArrayList<>();
		ArbitraryBuilder<Holder> builder = SUT.giveMeBuilder(Holder.class)
			.set("list", supplied);

		// when
		Holder first = builder.sample();
		Holder second = builder.sample();
		first.list.add("c");

		// then
		then(second.list).isEmpty();
		then(supplied).isEmpty();
	}

	@Test
	void setEmptyMapIsNotSharedAcrossSamples() {
		// given
		Map<String, String> supplied = new HashMap<>();
		ArbitraryBuilder<Holder> builder = SUT.giveMeBuilder(Holder.class)
			.set("map", supplied);

		// when
		Holder first = builder.sample();
		Holder second = builder.sample();
		first.map.put("b", "2");

		// then
		then(second.map).isEmpty();
		then(supplied).isEmpty();
	}

	@Test
	void setListWithNullElementKeepsNull() {
		// given
		ArbitraryBuilder<Holder> builder = SUT.giveMeBuilder(Holder.class)
			.set("list", new ArrayList<>(Arrays.asList("a", null)));

		// when
		Holder actual = builder.sample();

		// then
		then(actual.list).containsExactly("a", null);
	}

	@Test
	void setNestedListWithNullElementKeepsNull() {
		// given
		List<List<String>> supplied = new ArrayList<>();
		supplied.add(new ArrayList<>(Arrays.asList("a", null)));
		ArbitraryBuilder<NestedHolder> builder = SUT.giveMeBuilder(NestedHolder.class)
			.set("lists", supplied);

		// when
		NestedHolder actual = builder.sample();

		// then
		then(actual.lists).hasSize(1);
		then(actual.lists.get(0)).containsExactly("a", null);
	}

	@Test
	void registeredListWithNullElementKeepsNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(
				Holder.class,
				fixture -> fixture.giveMeBuilder(Holder.class).set("list", new ArrayList<>(Arrays.asList("a", null)))
			)
			.build();

		// when
		Holder actual = sut.giveMeOne(Holder.class);

		// then
		then(actual.list).containsExactly("a", null);
	}

	@Test
	void setRootListIsNotSharedAcrossSamples() {
		// given
		List<String> supplied = new ArrayList<>(Arrays.asList("a", "b"));
		ArbitraryBuilder<List<String>> builder = SUT.giveMeBuilder(new TypeReference<List<String>>() {
			})
			.set("$", supplied);

		// when
		List<String> first = builder.sample();
		List<String> second = builder.sample();
		first.add("c");

		// then
		then(second).containsExactly("a", "b");
		then(supplied).containsExactly("a", "b");
	}

	@Test
	void setWildcardListIsNotSharedAcrossElementsAndSamples() {
		// given
		List<String> supplied = new ArrayList<>(Arrays.asList("a", "b"));
		ArbitraryBuilder<NestedHolder> builder = SUT.giveMeBuilder(NestedHolder.class)
			.size("lists", 2)
			.set("lists[*]", supplied);

		// when
		NestedHolder first = builder.sample();
		NestedHolder second = builder.sample();
		first.lists.get(0).add("c");

		// then
		then(first.lists.get(1)).containsExactly("a", "b");
		then(second.lists.get(0)).containsExactly("a", "b");
		then(supplied).containsExactly("a", "b");
	}

	@Test
	void registeredListIsNotSharedAcrossSamples() {
		// given
		List<String> supplied = new ArrayList<>(Arrays.asList("a", "b"));
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.register(Holder.class, fixture -> fixture.giveMeBuilder(Holder.class).set("list", supplied))
			.build();
		ArbitraryBuilder<Holder> builder = sut.giveMeBuilder(Holder.class);

		// when
		Holder first = builder.sample();
		Holder second = builder.sample();
		first.list.add("c");

		// then
		then(second.list).containsExactly("a", "b");
		then(supplied).containsExactly("a", "b");
	}

	@Test
	void setLazyListReturningSameInstanceIsNotSharedAcrossSamples() {
		// given
		List<String> supplied = new ArrayList<>(Arrays.asList("a", "b"));
		ArbitraryBuilder<Holder> builder = SUT.giveMeBuilder(Holder.class)
			.setLazy("list", () -> supplied);

		// when
		Holder first = builder.sample();
		Holder second = builder.sample();
		first.list.add("c");

		// then
		then(second.list).containsExactly("a", "b");
		then(supplied).containsExactly("a", "b");
	}

	@Test
	void setJustListKeepsSuppliedInstance() {
		// given
		List<String> supplied = new ArrayList<>(Arrays.asList("a", "b"));

		// when
		Holder actual = SUT.giveMeBuilder(Holder.class)
			.set("list", Values.just(supplied))
			.sample();

		// then
		then(actual.list).isSameAs(supplied);
	}

	public static class Holder {
		private List<String> list;
		private Set<String> set;
		private LinkedList<String> linkedList;
		private Map<String, String> map;
	}

	public static class NestedHolder {
		private List<List<String>> lists;
	}
}
