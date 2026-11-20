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
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.type.TypeReference;

class ImmutableNestedContainerDecompositionTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();
	private static final FixtureMonkey FIXED_CONTAINER_SIZE_SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(3, 3))
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

	@Test
	void setImmutableListKeepsContainerTwoLevelsInsideRecordElement() {
		// when
		Feed actual = FIXED_CONTAINER_SIZE_SUT.giveMeBuilder(Feed.class)
			.set("notes", List.of(new Note(new Tags(List.of("a", "b")))))
			.sample();

		// then
		then(actual.notes()).hasSize(1);
		then(actual.notes().get(0).tags().values()).containsExactly("a", "b");
	}

	@Test
	void setImmutableListKeepsEmptyContainerTwoLevelsInsideRecordElement() {
		// when
		Feed actual = FIXED_CONTAINER_SIZE_SUT.giveMeBuilder(Feed.class)
			.set("notes", List.of(new Note(new Tags(List.of()))))
			.sample();

		// then
		then(actual.notes().get(0).tags().values()).isEmpty();
	}

	@Test
	void setImmutableSetKeepsContainerTwoLevelsInsideRecordElement() {
		// when
		SetFeed actual = FIXED_CONTAINER_SIZE_SUT.giveMeBuilder(SetFeed.class)
			.set("notes", Set.of(new Note(new Tags(List.of("a", "b")))))
			.sample();

		// then
		then(actual.notes()).hasSize(1);
		then(actual.notes().iterator().next().tags().values()).containsExactly("a", "b");
	}

	@Test
	void setImmutableMapKeepsContainerTwoLevelsInsideRecordValue() {
		// when
		MapFeed actual = FIXED_CONTAINER_SIZE_SUT.giveMeBuilder(MapFeed.class)
			.set("notes", Map.of("key", new Note(new Tags(List.of("a", "b")))))
			.sample();

		// then
		then(actual.notes().get("key").tags().values()).containsExactly("a", "b");
	}

	@Test
	void setListKeepsSealedImplementationInsideRecordElement() {
		// when
		PostFeed actual = FIXED_CONTAINER_SIZE_SUT.giveMeBuilder(PostFeed.class)
			.set("posts", List.of(new Post(new TextContent(List.of("a", "b")))))
			.sample();

		// then
		then(actual.posts().get(0).content()).isExactlyInstanceOf(TextContent.class);
		then(((TextContent)actual.posts().get(0).content()).lines()).containsExactly("a", "b");
	}

	public record NestedLists(List<List<String>> values) {
	}

	public record NestedSets(Set<Set<String>> values) {
	}

	public record Tags(List<String> values) {
	}

	public record Note(Tags tags) {
	}

	public record Feed(List<Note> notes) {
	}

	public record SetFeed(Set<Note> notes) {
	}

	public record MapFeed(Map<String, Note> notes) {
	}

	public sealed interface Content permits TextContent, ImageContent {
	}

	public record TextContent(List<String> lines) implements Content {
	}

	public record ImageContent(String url) implements Content {
	}

	public record Post(Content content) {
	}

	public record PostFeed(List<Post> posts) {
	}
}
