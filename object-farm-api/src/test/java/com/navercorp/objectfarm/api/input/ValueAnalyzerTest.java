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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ValueAnalyzerTest {

	private final ValueAnalyzer analyzer = new ValueAnalyzer();

	@Test
	void analyzeNullReturnsEmpty() {
		// when
		ValueAnalysisResult result = analyzer.analyze(null, "$");

		// then
		then(result.isEmpty()).isTrue();
	}

	@Test
	void analyzeSimpleObjectExtractsFieldValues() {
		// given
		SimpleObject obj = new SimpleObject("test", 42);

		// when
		ValueAnalysisResult result = analyzer.analyze(obj, "$");

		// then
		then(result.getValuesByPath()).containsEntry("$", obj);
		then(result.getValuesByPath()).containsEntry("$.name", "test");
		then(result.getValuesByPath()).containsEntry("$.value", 42);
	}

	@Test
	void analyzeListExtractsContainerSize() {
		// given
		List<String> list = Arrays.asList("a", "b", "c");

		// when
		ValueAnalysisResult result = analyzer.analyze(list, "$");

		// then
		then(result.getContainerSizeResolvers()).hasSize(1);
		then(result.getValuesByPath()).containsEntry("$", list);
	}

	@Test
	void analyzeNestedListExtractsAllContainerSizes() {
		// given
		List<List<String>> nested = Arrays.asList(
			Arrays.asList("a", "b"),
			Arrays.asList("c", "d", "e")
		);

		// when
		ValueAnalysisResult result = analyzer.analyze(nested, "$");

		// then
		then(result.getContainerSizeResolvers()).hasSize(3); // outer + 2 inner
	}

	@Test
	void analyzeMapExtractsContainerSize() {
		// given
		Map<String, Integer> map = new HashMap<>();
		map.put("one", 1);
		map.put("two", 2);

		// when
		ValueAnalysisResult result = analyzer.analyze(map, "$");

		// then
		then(result.getContainerSizeResolvers()).hasSize(1);
		// Generic type resolver for map + for each entry's value (Integer type)
		then(result.getGenericTypeResolvers()).isNotEmpty();
	}

	@Test
	void analyzeArrayExtractsContainerSize() {
		// given
		String[] array = {"a", "b", "c"};

		// when
		ValueAnalysisResult result = analyzer.analyze(array, "$");

		// then
		then(result.getContainerSizeResolvers()).hasSize(1);
	}

	@Test
	void analyzeInterfaceImplementationExtractsInterfaceResolver() {
		// given
		Animal animal = new Dog("Buddy");

		// when
		ValueAnalysisResult result = analyzer.analyze(animal, "$");

		// then
		then(result.getInterfaceResolvers()).hasSize(1);
	}

	@Test
	void analyzeListOfInterfacesExtractsInterfaceResolversForElements() {
		// given
		List<Animal> animals = new ArrayList<>();
		animals.add(new Dog("Buddy"));
		animals.add(new Cat("Whiskers"));

		// when
		ValueAnalysisResult result = analyzer.analyze(animals, "$");

		// then
		// Interface resolvers: 1 for ArrayList + 1 for Dog + 1 for Cat
		then(result.getInterfaceResolvers()).hasSize(3);
		then(result.getContainerSizeResolvers()).hasSize(1);
	}

	@Test
	void analyzeObjectWithNestedObjectExtractsFieldValues() {
		// given
		Address address = new Address("Seoul", "123-456");
		Person person = new Person("John", address);

		// when
		ValueAnalysisResult result = analyzer.analyze(person, "$");

		// then
		then(result.getValuesByPath()).containsEntry("$", person);
		then(result.getValuesByPath()).containsEntry("$.name", "John");
		then(result.getValuesByPath()).containsEntry("$.address", address);
	}

	@Test
	void analyzePrimitiveDoesNotExtractFields() {
		// given
		Integer value = 42;

		// when
		ValueAnalysisResult result = analyzer.analyze(value, "$");

		// then
		then(result.getValuesByPath()).hasSize(1);
		then(result.getValuesByPath()).containsEntry("$", value);
	}

	@Test
	void analyzeStringDoesNotExtractFields() {
		// given
		String value = "test";

		// when
		ValueAnalysisResult result = analyzer.analyze(value, "$");

		// then
		then(result.getValuesByPath()).hasSize(1);
		then(result.getValuesByPath()).containsEntry("$", value);
	}

	@Test
	void toResolverContextCreatesValidContext() {
		// given
		List<Animal> animals = new ArrayList<>();
		animals.add(new Dog("Buddy"));

		// when
		ValueAnalysisResult result = analyzer.analyze(animals, "$");

		// then
		then(result.toResolverContext()).isNotNull();
	}

	@Test
	void analyzeWithCustomContainerDetector() {
		// given
		ContainerDetector customDetector = ContainerDetector.standard();
		ValueAnalyzer customAnalyzer = new ValueAnalyzer(customDetector);
		List<String> list = Arrays.asList("a", "b");

		// when
		ValueAnalysisResult result = customAnalyzer.analyze(list, "$");

		// then
		then(result.getContainerSizeResolvers()).hasSize(1);
	}

	@Test
	void analyzeWithCustomFieldExtractor() {
		// given
		FieldExtractor noopExtractor = FieldExtractor.none();
		ValueAnalyzer customAnalyzer = new ValueAnalyzer(ContainerDetector.standard(), noopExtractor);
		SimpleObject obj = new SimpleObject("test", 42);

		// when
		ValueAnalysisResult result = customAnalyzer.analyze(obj, "$");

		// then
		// Only the root value should be present, no field values
		then(result.getValuesByPath()).hasSize(1);
		then(result.getValuesByPath()).containsEntry("$", obj);
	}

	@Test
	void analyzePrimitiveWrapperDoesNotGenerateInterfaceResolver() {
		// given
		Long longValue = 42L;
		Integer intValue = 42;
		Double doubleValue = 3.14;

		// when
		ValueAnalysisResult longResult = analyzer.analyze(longValue, "$.userNo");
		ValueAnalysisResult intResult = analyzer.analyze(intValue, "$.count");
		ValueAnalysisResult doubleResult = analyzer.analyze(doubleValue, "$.rate");

		// then
		then(longResult.getInterfaceResolvers()).isEmpty();
		then(intResult.getInterfaceResolvers()).isEmpty();
		then(doubleResult.getInterfaceResolvers()).isEmpty();
	}

	@Test
	void analyzeDecomposedInterfaceFieldGeneratesInterfaceResolver() {
		// given
		ObjectWithInterfaceField obj = new ObjectWithInterfaceField(
			new ArrayList<>(Arrays.asList("a", "b"))
		);

		// when
		ValueAnalysisResult result = analyzer.analyzeDecomposed(obj, "$");

		// then
		then(result.getInterfaceResolvers()).isNotEmpty();
		boolean hasItemsResolver = result.getInterfaceResolvers().stream()
			.anyMatch(r -> r.matches(com.navercorp.objectfarm.api.expression.PathExpression.of("$.items")));
		then(hasItemsResolver).isTrue();
	}

	@Test
	void analyzeDecomposedConcreteFieldDoesNotGenerateInterfaceResolver() {
		// given
		ObjectWithConcreteField obj = new ObjectWithConcreteField(
			new ArrayList<>(Arrays.asList("a", "b"))
		);

		// when
		ValueAnalysisResult result = analyzer.analyzeDecomposed(obj, "$");

		// then
		boolean hasItemsResolver = result.getInterfaceResolvers().stream()
			.anyMatch(r -> r.matches(com.navercorp.objectfarm.api.expression.PathExpression.of("$.items")));
		then(hasItemsResolver).isFalse();
	}

	@Test
	void analyzeDecomposedMapInterfaceFieldGeneratesInterfaceResolver() {
		// given
		HashMap<String, String> map = new HashMap<>();
		map.put("key", "value");
		ObjectWithMapInterfaceField obj = new ObjectWithMapInterfaceField(map);

		// when
		ValueAnalysisResult result = analyzer.analyzeDecomposed(obj, "$");

		// then
		boolean hasMetadataResolver = result.getInterfaceResolvers().stream()
			.anyMatch(r -> r.matches(com.navercorp.objectfarm.api.expression.PathExpression.of("$.metadata")));
		then(hasMetadataResolver).isTrue();
	}

	@Test
	void analyzeDecomposedConcreteMapFieldDoesNotGenerateInterfaceResolver() {
		// given
		HashMap<String, String> map = new HashMap<>();
		map.put("key", "value");
		ObjectWithConcreteMapField obj = new ObjectWithConcreteMapField(map);

		// when
		ValueAnalysisResult result = analyzer.analyzeDecomposed(obj, "$");

		// then
		boolean hasMetadataResolver = result.getInterfaceResolvers().stream()
			.anyMatch(r -> r.matches(com.navercorp.objectfarm.api.expression.PathExpression.of("$.metadata")));
		then(hasMetadataResolver).isFalse();
	}

	@Test
	void analyzeNonPublicJdkContainerGeneratesResolverThatSkipsResolve() {
		// given
		List<String> list = Arrays.asList("a", "b"); // Arrays$ArrayList is a non-public JDK inner class

		// when
		ValueAnalysisResult result = analyzer.analyze(list, "$.values");

		// then
		then(result.getInterfaceResolvers()).hasSize(1);
	}

	@Test
	void analyzeDecomposedNonPublicJdkContainerGeneratesResolverThatSkipsResolve() {
		// given
		List<String> list = Arrays.asList("a", "b"); // Arrays$ArrayList is a non-public JDK inner class

		// when
		ValueAnalysisResult result = analyzer.analyzeDecomposed(list, "$.values");

		// then
		then(result.getInterfaceResolvers()).hasSize(1);
	}

	@Test
	void analyzePublicContainerGeneratesInterfaceResolver() {
		// given
		List<String> list = new ArrayList<>(Arrays.asList("a", "b")); // ArrayList is a public class

		// when
		ValueAnalysisResult result = analyzer.analyze(list, "$.values");

		// then
		then(result.getInterfaceResolvers()).hasSize(1);
	}

	@Test
	void analyzeDecomposedPublicContainerGeneratesInterfaceResolver() {
		// given
		List<String> list = new ArrayList<>(Arrays.asList("a", "b")); // ArrayList is a public class

		// when
		ValueAnalysisResult result = analyzer.analyzeDecomposed(list, "$.values");

		// then
		then(result.getInterfaceResolvers()).hasSize(1);
	}

	@Test
	void analyzeDecomposedAbstractFieldDoesNotUseElseIfBranch() {
		// given - Number is abstract but Integer is not a container, so it goes through recursive analyze
		ObjectWithAbstractField obj = new ObjectWithAbstractField(42);

		// when
		ValueAnalysisResult result = analyzer.analyzeDecomposed(obj, "$");

		// then - Integer is a JDK value type, so no interface resolver is generated
		boolean hasValueResolver = result.getInterfaceResolvers().stream()
			.anyMatch(r -> r.matches(com.navercorp.objectfarm.api.expression.PathExpression.of("$.value")));
		then(hasValueResolver).isFalse();
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

	interface Animal {
		String getName();
	}

	static class Dog implements Animal {
		private final String name;

		Dog(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}
	}

	static class Cat implements Animal {
		private final String name;

		Cat(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
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

	static class ObjectWithInterfaceField {
		private final List<String> items;

		ObjectWithInterfaceField(List<String> items) {
			this.items = items;
		}
	}

	static class ObjectWithConcreteField {
		private final ArrayList<String> items;

		ObjectWithConcreteField(ArrayList<String> items) {
			this.items = items;
		}
	}

	static class ObjectWithAbstractField {
		private final Number value;

		ObjectWithAbstractField(Number value) {
			this.value = value;
		}
	}

	static class ObjectWithMapInterfaceField {
		private final Map<String, String> metadata;

		ObjectWithMapInterfaceField(Map<String, String> metadata) {
			this.metadata = metadata;
		}
	}

	static class ObjectWithConcreteMapField {
		private final HashMap<String, String> metadata;

		ObjectWithConcreteMapField(HashMap<String, String> metadata) {
			this.metadata = metadata;
		}
	}
}
