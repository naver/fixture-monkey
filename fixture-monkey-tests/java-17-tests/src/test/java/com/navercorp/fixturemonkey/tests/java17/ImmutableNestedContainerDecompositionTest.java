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

package com.navercorp.fixturemonkey.tests.java17;

import static org.assertj.core.api.BDDAssertions.entry;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.type.TypeReference;

class ImmutableNestedContainerDecompositionTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@Test
	void setImmutableNestedListsThenSetInnerElement() {
		// when
		NestedLists actual = SUT.giveMeBuilder(NestedLists.class)
			.set("values", List.of(List.of("a", "b"), List.of("c", "d")))
			.set("values[1][0]", "x")
			.sample();

		// then
		then(actual.values()).containsExactly(List.of("a", "b"), List.of("x", "d"));
	}

	@Test
	void setImmutableNestedListsThenGrowInnerLists() {
		// when
		NestedLists actual = SUT.giveMeBuilder(NestedLists.class)
			.set("values", List.of(List.of("a", "b"), List.of("c", "d")))
			.size("values[*]", 3)
			.sample();

		// then
		then(actual.values()).hasSize(2);
		then(actual.values().get(0)).hasSize(3).startsWith("a", "b");
		then(actual.values().get(1)).hasSize(3).startsWith("c", "d");
	}

	@Test
	void setImmutableNestedSetsThenGrowInnerSets() {
		// when
		NestedSets actual = SUT.giveMeBuilder(NestedSets.class)
			.set("values", Set.of(Set.of("a", "b")))
			.size("values[*]", 3)
			.sample();

		// then
		then(actual.values()).hasSize(1);
		then(actual.values().iterator().next()).hasSize(3).contains("a", "b");
	}

	@Test
	void setImmutableNestedMapsThenSetInnerValue() {
		// when
		Map<String, Map<String, String>> actual = SUT
			.giveMeBuilder(new TypeReference<Map<String, Map<String, String>>>() {
			})
			.set("$", Map.of("k", Map.of("ik", "iv")))
			.set("$[0][value][0][value]", "x")
			.sample();

		// then
		then(actual).containsOnlyKeys("k");
		then(actual.get("k")).containsExactly(entry("ik", "x"));
	}

	public record NestedLists(List<List<String>> values) {
	}

	public record NestedSets(Set<Set<String>> values) {
	}
}
