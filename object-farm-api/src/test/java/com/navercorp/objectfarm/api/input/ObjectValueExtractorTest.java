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

package com.navercorp.objectfarm.api.input;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.navercorp.objectfarm.api.expression.PathExpression;

class ObjectValueExtractorTest {

	private final ObjectValueExtractor sut = new ObjectValueExtractor();

	@Test
	void extractPojoFields() {
		// given
		SimpleObject obj = new SimpleObject("hello", 42);
		PathExpression basePath = PathExpression.root();

		// when
		Map<PathExpression, Object> result = sut.extract(obj, basePath);

		// then
		then(result).containsEntry(PathExpression.of("$.name"), "hello");
		then(result).containsEntry(PathExpression.of("$.value"), 42);
	}

	@Test
	void extractNestedPojo() {
		// given
		Address address = new Address("Seoul", "12345");
		Person person = new Person("John", address);
		PathExpression basePath = PathExpression.root();

		// when
		Map<PathExpression, Object> result = sut.extract(person, basePath);

		// then
		then(result).containsEntry(PathExpression.of("$.name"), "John");
		then(result).containsEntry(PathExpression.of("$.address"), address);
		then(result).containsEntry(PathExpression.of("$.address.city"), "Seoul");
		then(result).containsEntry(PathExpression.of("$.address.zipCode"), "12345");
	}

	@Test
	void extractCollection() {
		// given
		List<SimpleObject> list = Arrays.asList(new SimpleObject("a", 1), new SimpleObject("b", 2));
		PathExpression basePath = PathExpression.root();

		// when
		Map<PathExpression, Object> result = sut.extract(list, basePath);

		// then
		then(result).containsEntry(PathExpression.of("$[0]"), list.get(0));
		then(result).containsEntry(PathExpression.of("$[0].name"), "a");
		then(result).containsEntry(PathExpression.of("$[0].value"), 1);
		then(result).containsEntry(PathExpression.of("$[1]"), list.get(1));
		then(result).containsEntry(PathExpression.of("$[1].name"), "b");
		then(result).containsEntry(PathExpression.of("$[1].value"), 2);
	}

	@Test
	void extractArray() {
		// given
		String[] array = {"x", "y", "z"};
		PathExpression basePath = PathExpression.root();

		// when
		Map<PathExpression, Object> result = sut.extract(array, basePath);

		// then
		then(result).containsEntry(PathExpression.of("$[0]"), "x");
		then(result).containsEntry(PathExpression.of("$[1]"), "y");
		then(result).containsEntry(PathExpression.of("$[2]"), "z");
	}

	@Test
	void extractMap() {
		// given
		Map<String, Integer> map = new LinkedHashMap<>();
		map.put("key1", 100);
		map.put("key2", 200);
		PathExpression basePath = PathExpression.root();

		// when
		Map<PathExpression, Object> result = sut.extract(map, basePath);

		// then
		then(result).containsEntry(PathExpression.of("$[0]").key(), "key1");
		then(result).containsEntry(PathExpression.of("$[0]").value(), 100);
		then(result).containsEntry(PathExpression.of("$[1]").key(), "key2");
		then(result).containsEntry(PathExpression.of("$[1]").value(), 200);
	}

	@Test
	void extractPojoWithList() {
		// given
		Team team = new Team("dev", Arrays.asList(new SimpleObject("a", 1), new SimpleObject("b", 2)));
		PathExpression basePath = PathExpression.root();

		// when
		Map<PathExpression, Object> result = sut.extract(team, basePath);

		// then
		then(result).containsEntry(PathExpression.of("$.teamName"), "dev");
		then(result).containsKey(PathExpression.of("$.members"));
		then(result).containsEntry(PathExpression.of("$.members[0]"), team.members.get(0));
		then(result).containsEntry(PathExpression.of("$.members[0].name"), "a");
		then(result).containsEntry(PathExpression.of("$.members[0].value"), 1);
		then(result).containsEntry(PathExpression.of("$.members[1]"), team.members.get(1));
		then(result).containsEntry(PathExpression.of("$.members[1].name"), "b");
	}

	@Test
	void extractNullReturnsEmpty() {
		// given
		PathExpression basePath = PathExpression.root();

		// when
		Map<PathExpression, Object> result = sut.extract(null, basePath);

		// then
		then(result).isEmpty();
	}

	@Test
	void extractNullFieldIncluded() {
		// given
		Person person = new Person("John", null);
		PathExpression basePath = PathExpression.root();

		// when
		Map<PathExpression, Object> result = sut.extract(person, basePath);

		// then
		then(result).containsEntry(PathExpression.of("$.name"), "John");
		then(result).containsEntry(PathExpression.of("$.address"), null);
	}

	@Test
	void extractCollectionWithNullElement() {
		// given
		List<String> list = Arrays.asList("a", null, "c");
		PathExpression basePath = PathExpression.root();

		// when
		Map<PathExpression, Object> result = sut.extract(list, basePath);

		// then
		then(result).containsEntry(PathExpression.of("$[0]"), "a");
		then(result).doesNotContainKey(PathExpression.of("$[1]"));
		then(result).containsEntry(PathExpression.of("$[2]"), "c");
	}

	@Test
	void extractCircularReferenceDoesNotStackOverflow() {
		// given
		CircularNode nodeA = new CircularNode("a");
		CircularNode nodeB = new CircularNode("b");
		nodeA.neighbor = nodeB;
		nodeB.neighbor = nodeA;
		PathExpression basePath = PathExpression.root();

		// when
		Map<PathExpression, Object> result = sut.extract(nodeA, basePath);

		// then
		then(result).containsEntry(PathExpression.of("$.name"), "a");
		then(result).containsEntry(PathExpression.of("$.neighbor"), nodeB);
		then(result).containsEntry(PathExpression.of("$.neighbor.name"), "b");
		then(result).containsEntry(PathExpression.of("$.neighbor.neighbor"), nodeA);
		then(result).doesNotContainKey(PathExpression.of("$.neighbor.neighbor.name"));
	}

	@Test
	void extractSharedInstanceExtractsBothPaths() {
		// given
		Address shared = new Address("Seoul", "12345");
		TwoAddresses obj = new TwoAddresses(shared, shared);
		PathExpression basePath = PathExpression.root();

		// when
		Map<PathExpression, Object> result = sut.extract(obj, basePath);

		// then
		then(result).containsEntry(PathExpression.of("$.address1"), shared);
		then(result).containsEntry(PathExpression.of("$.address1.city"), "Seoul");
		then(result).containsEntry(PathExpression.of("$.address1.zipCode"), "12345");
		then(result).containsEntry(PathExpression.of("$.address2"), shared);
		then(result).containsEntry(PathExpression.of("$.address2.city"), "Seoul");
		then(result).containsEntry(PathExpression.of("$.address2.zipCode"), "12345");
	}

	// Test helper classes

	static class SimpleObject {

		private final String name;
		private final int value;

		SimpleObject(String name, int value) {
			this.name = name;
			this.value = value;
		}
	}

	static class Address {

		private final String city;
		private final String zipCode;

		Address(String city, String zipCode) {
			this.city = city;
			this.zipCode = zipCode;
		}
	}

	static class Person {

		private final String name;
		private final Address address;

		Person(String name, Address address) {
			this.name = name;
			this.address = address;
		}
	}

	static class Team {

		private final String teamName;
		private final List<SimpleObject> members;

		Team(String teamName, List<SimpleObject> members) {
			this.teamName = teamName;
			this.members = members;
		}
	}

	static class CircularNode {

		private final String name;
		CircularNode neighbor;

		CircularNode(String name) {
			this.name = name;
		}
	}

	static class TwoAddresses {

		private final Address address1;
		private final Address address2;

		TwoAddresses(Address address1, Address address2) {
			this.address1 = address1;
			this.address2 = address2;
		}
	}
}
