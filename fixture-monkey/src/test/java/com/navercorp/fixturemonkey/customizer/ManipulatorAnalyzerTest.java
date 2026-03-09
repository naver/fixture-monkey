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

package com.navercorp.fixturemonkey.customizer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.adapter.analysis.AnalysisResult;
import com.navercorp.fixturemonkey.adapter.analysis.ManipulatorAnalyzer;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.container.DecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.api.container.DefaultDecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.customizer.Values.Just;
import com.navercorp.fixturemonkey.tree.CompositeNodeResolver;
import com.navercorp.fixturemonkey.tree.NodePredicateResolver;
import com.navercorp.fixturemonkey.tree.NodeResolver;
import com.navercorp.fixturemonkey.tree.PropertyNameNodePredicate;
import com.navercorp.fixturemonkey.tree.StartNodePredicate;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.GenericTypeResolver;
import com.navercorp.objectfarm.api.node.InterfaceResolver;
import com.navercorp.objectfarm.api.tree.PathResolver;
import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;

class ManipulatorAnalyzerTest {
	@Property
	void analyzesNodeSetDecomposedValueManipulator() {
		Dog dog = new Dog();
		NodeResolver nodeResolver = createNodeResolver("animal");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			dog
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getInterfaceResolvers()).hasSize(1);

		PathResolver<InterfaceResolver> resolver = result.getInterfaceResolvers().get(0);
		PathExpression animalPath = PathExpression.root().child("animal");
		assertThat(resolver.matches(animalPath)).isTrue();

		JvmType resolvedType = resolver.getCustomizer().resolve(new JavaType(Animal.class));
		assertThat(resolvedType).isNotNull();
		assertThat(resolvedType.getRawType()).isEqualTo(Dog.class);
	}

	@Property
	void analyzesNodeSetJustManipulator() {
		NodeResolver nodeResolver = createNodeResolver("value");
		Just justValue = Values.just(CombinableArbitrary.from("test"));
		NodeManipulator nodeManipulator = new NodeSetJustManipulator(justValue);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getJustPaths()).hasSize(1);
		assertThat(result.getJustPaths().get(0)).isEqualTo(PathExpression.of("$.value"));
	}

	@Property
	void analyzesNodeNullityManipulatorSetToNull() {
		NodeResolver nodeResolver = createNodeResolver("nullable");
		NodeManipulator nodeManipulator = new NodeNullityManipulator(0, true);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getValuesByPath()).containsKey(PathExpression.of("$.nullable"));
		assertThat(result.getValuesByPath().get(PathExpression.of("$.nullable"))).isNull();
	}

	@Property
	void analyzesNodeNullityManipulatorSetToNotNull() {
		NodeResolver nodeResolver = createNodeResolver("required");
		NodeManipulator nodeManipulator = new NodeNullityManipulator(0, false);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getValuesByPath()).doesNotContainKey(PathExpression.of("$.required"));
	}

	@Property
	void analyzesCompositeNodeManipulator() {
		NodeResolver nodeResolver = createNodeResolver("composite");

		NodeManipulator inner1 = new NodeNullityManipulator(0, true);
		NodeManipulator inner2 = new NodeSetJustManipulator(Values.just(CombinableArbitrary.from("test")));
		NodeManipulator compositeManipulator = new CompositeNodeManipulator(inner1, inner2);

		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, compositeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getJustPaths()).containsExactly(PathExpression.of("$.composite"));
		assertThat(result.getValuesByPath()).containsKey(PathExpression.of("$.composite"));
	}

	@Property
	void analyzesApplyNodeCountManipulator() {
		NodeResolver nodeResolver = createNodeResolver("counted");

		NodeManipulator innerManipulator = new NodeNullityManipulator(0, true);
		NodeManipulator countManipulator = new ApplyNodeCountManipulator(innerManipulator, 3);

		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, countManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getValuesByPath()).containsKey(PathExpression.of("$.counted"));
		assertThat(result.getValuesByPath().get(PathExpression.of("$.counted"))).isNull();
		assertThat(result.getLimitsByPath()).containsKey(PathExpression.of("$.counted"));
		assertThat(result.getLimitsByPath().get(PathExpression.of("$.counted"))).isEqualTo(3);
	}

	@Property
	void analyzesNestedCompositeWithApplyNodeCount() {
		NodeResolver nodeResolver = createNodeResolver("nested");

		NodeManipulator nullity = new NodeNullityManipulator(0, true);
		NodeManipulator just = new NodeSetJustManipulator(Values.just(CombinableArbitrary.from("value")));
		NodeManipulator composite = new CompositeNodeManipulator(nullity, just);
		NodeManipulator counted = new ApplyNodeCountManipulator(composite, 2);

		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, counted);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getJustPaths()).containsExactly(PathExpression.of("$.nested"));
		assertThat(result.getValuesByPath()).containsKey(PathExpression.of("$.nested"));
	}

	@Property
	void analyzesCompositeContainingApplyNodeCount() {
		NodeResolver nodeResolver = createNodeResolver("path");

		NodeManipulator nullity = new NodeNullityManipulator(0, true);
		NodeManipulator counted = new ApplyNodeCountManipulator(nullity, 1);
		NodeManipulator just = new NodeSetJustManipulator(Values.just(CombinableArbitrary.from("test")));
		NodeManipulator composite = new CompositeNodeManipulator(counted, just);

		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, composite);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getJustPaths()).containsExactly(PathExpression.of("$.path"));
		assertThat(result.getValuesByPath()).containsKey(PathExpression.of("$.path"));
	}

	@Property
	void analyzesMultipleManipulators() {
		ArbitraryManipulator manipulator1 = new ArbitraryManipulator(
			createNodeResolver("field1"),
			new NodeNullityManipulator(0, true)
		);

		ArbitraryManipulator manipulator2 = new ArbitraryManipulator(
			createNodeResolver("field2"),
			new NodeSetJustManipulator(Values.just(CombinableArbitrary.from("value")))
		);

		Dog dog = new Dog();
		ArbitraryManipulator manipulator3 = new ArbitraryManipulator(
			createNodeResolver("field3"),
			new NodeSetDecomposedValueManipulator<>(
				1,
				createDecomposedContainerValueFactory(),
				Collections.emptyList(),
				dog
			)
		);

		List<ArbitraryManipulator> manipulators = Arrays.asList(manipulator1, manipulator2, manipulator3);

		AnalysisResult result = ManipulatorAnalyzer.analyze(manipulators);

		assertThat(result.getValuesByPath()).containsKey(PathExpression.of("$.field1"));
		assertThat(result.getValuesByPath().get(PathExpression.of("$.field1"))).isNull();
		assertThat(result.getJustPaths()).containsExactly(PathExpression.of("$.field2"));
		assertThat(result.getInterfaceResolvers()).hasSize(1);
	}

	@Property
	void emptyManipulatorsReturnsEmptyResult() {
		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.emptyList());

		assertThat(result.getInterfaceResolvers()).isEmpty();
		assertThat(result.getGenericTypeResolvers()).isEmpty();
		assertThat(result.getContainerSizeResolvers()).isEmpty();
		assertThat(result.getJustPaths()).isEmpty();
		assertThat(result.getValuesByPath()).isEmpty();
	}

	@Property
	void analyzesNestedObjectWithAddress() {
		Address address = new Address("123 Main St", "Seoul", "12345");
		Person person = new Person("John", 30, address, Arrays.asList("reading", "coding"));

		NodeResolver nodeResolver = createNodeResolver("person");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			person
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		// Person.hobbies (List -> ArrayList) generates an interface resolver for the container field
		assertThat(result.getInterfaceResolvers()).hasSize(1);
		PathExpression hobbiesPath = PathExpression.of("$.person.hobbies");
		assertThat(result.getInterfaceResolvers().get(0).matches(hobbiesPath)).isTrue();
	}

	@Property
	void analyzesListOfStrings() {
		List<String> items = new ArrayList<>(Arrays.asList("item1", "item2", "item3"));

		NodeResolver nodeResolver = createNodeResolver("items");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			items
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getInterfaceResolvers()).hasSize(1);

		PathResolver<InterfaceResolver> resolver = result.getInterfaceResolvers().get(0);
		PathExpression itemsPath = PathExpression.root().child("items");
		assertThat(resolver.matches(itemsPath)).isTrue();

		InterfaceResolver interfaceResolver = resolver.getCustomizer();
		JvmType resolvedType = interfaceResolver.resolve(new JavaType(List.class));
		assertThat(resolvedType).isNotNull();
		assertThat(List.class.isAssignableFrom(resolvedType.getRawType())).isTrue();
	}

	@Property
	void analyzesListOfComplexObjects() {
		List<OrderItem> items = Arrays.asList(new OrderItem("Product A", 2, 10.0), new OrderItem("Product B", 1, 20.0));

		NodeResolver nodeResolver = createNodeResolver("orderItems");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			items
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getInterfaceResolvers()).hasSize(1);

		PathResolver<InterfaceResolver> resolver = result.getInterfaceResolvers().get(0);
		PathExpression path = PathExpression.root().child("orderItems");
		assertThat(resolver.matches(path)).isTrue();

		PathExpression wrongPath = PathExpression.root().child("otherItems");
		assertThat(resolver.matches(wrongPath)).isFalse();
	}

	@Property
	void analyzesMapOfStringToString() {
		Map<String, String> metadata = new HashMap<>();
		metadata.put("key1", "value1");
		metadata.put("key2", "value2");

		NodeResolver nodeResolver = createNodeResolver("metadata");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			metadata
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getInterfaceResolvers()).hasSize(1);

		PathResolver<InterfaceResolver> resolver = result.getInterfaceResolvers().get(0);
		InterfaceResolver interfaceResolver = resolver.getCustomizer();

		JvmType resolvedType = interfaceResolver.resolve(new JavaType(Map.class));
		assertThat(resolvedType).isNotNull();
		assertThat(resolvedType.getRawType()).isEqualTo(HashMap.class);
	}

	@Property
	void analyzesMapWithComplexValues() {
		Map<String, Address> addressMap = new HashMap<>();
		addressMap.put("home", new Address("123 Home St", "Seoul", "11111"));
		addressMap.put("work", new Address("456 Work Ave", "Busan", "22222"));

		NodeResolver nodeResolver = createNodeResolver("addresses");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			addressMap
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getInterfaceResolvers()).hasSize(1);

		PathResolver<InterfaceResolver> resolver = result.getInterfaceResolvers().get(0);
		PathExpression path = PathExpression.root().child("addresses");
		assertThat(resolver.matches(path)).isTrue();
	}

	@Property
	void analyzesGenericContainerWithString() {
		Container<String> container = new Container<>("test value", "Test Label");

		NodeResolver nodeResolver = createNodeResolver("container");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			container
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getInterfaceResolvers()).isEmpty();
	}

	@Property
	void analyzesGenericContainerWithComplexType() {
		Container<Address> container = new Container<>(
			new Address("789 Container St", "Incheon", "33333"),
			"Address Container"
		);

		NodeResolver nodeResolver = createNodeResolver("addressContainer");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			container
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getInterfaceResolvers()).isEmpty();
	}

	@Property
	void analyzesDeepNestedOrder() {
		List<OrderItem> items = Arrays.asList(new OrderItem("Laptop", 1, 1500.0), new OrderItem("Mouse", 2, 25.0));
		Map<String, String> metadata = new HashMap<>();
		metadata.put("source", "web");
		metadata.put("priority", "high");

		Order order = new Order("ORD-001", items, metadata);

		NodeResolver nodeResolver = createNodeResolver("order");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			order
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		// Order.items (List -> ArrayList) and Order.metadata (Map -> HashMap) generate interface resolvers
		assertThat(result.getInterfaceResolvers()).hasSize(2);
	}

	@Property
	void analyzesInterfaceImplementation() {
		Animal animal = new Dog("Labrador");

		NodeResolver nodeResolver = createNodeResolver("pet");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			animal
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getInterfaceResolvers()).hasSize(1);

		PathResolver<InterfaceResolver> resolver = result.getInterfaceResolvers().get(0);
		PathExpression petPath = PathExpression.root().child("pet");
		assertThat(resolver.matches(petPath)).isTrue();

		InterfaceResolver interfaceResolver = resolver.getCustomizer();
		JvmType resolvedType = interfaceResolver.resolve(new JavaType(Animal.class));
		assertThat(resolvedType).isNotNull();
		assertThat(resolvedType.getRawType()).isEqualTo(Dog.class);
	}

	@Property
	void analyzesListOfInterfaceImplementations() {
		List<Animal> animals = new ArrayList<>(Arrays.asList(new Dog("Poodle"), new Cat()));

		NodeResolver nodeResolver = createNodeResolver("animals");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			animals
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getInterfaceResolvers()).hasSize(3);

		PathResolver<InterfaceResolver> containerResolver = result
			.getInterfaceResolvers()
			.stream()
			.filter(r -> r.matches(PathExpression.root().child("animals")))
			.findFirst()
			.orElse(null);
		assertThat(containerResolver).isNotNull();
		JvmType resolvedType = containerResolver.getCustomizer().resolve(new JavaType(List.class));
		assertThat(resolvedType).isNotNull();
		assertThat(List.class.isAssignableFrom(resolvedType.getRawType())).isTrue();
	}

	@Property
	void analyzesNestedListOfLists() {
		List<List<String>> nestedList = Arrays.asList(
			Arrays.asList("a", "b", "c"),
			Arrays.asList("d", "e"),
			Arrays.asList("f")
		);

		NodeResolver nodeResolver = createNodeResolver("matrix");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			nestedList
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getInterfaceResolvers()).hasSize(1);

		PathResolver<InterfaceResolver> resolver = result.getInterfaceResolvers().get(0);
		PathExpression matrixPath = PathExpression.root().child("matrix");
		assertThat(resolver.matches(matrixPath)).isTrue();
	}

	@Property
	void analyzesEmptyCollection() {
		List<String> emptyList = new ArrayList<>();

		NodeResolver nodeResolver = createNodeResolver("emptyList");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			emptyList
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getInterfaceResolvers()).hasSize(1);

		PathResolver<InterfaceResolver> resolver = result.getInterfaceResolvers().get(0);
		InterfaceResolver interfaceResolver = resolver.getCustomizer();

		JvmType resolvedType = interfaceResolver.resolve(new JavaType(List.class));
		assertThat(resolvedType).isNotNull();
		assertThat(resolvedType.getRawType()).isEqualTo(ArrayList.class);
	}

	@Property
	void analyzesEmptyMap() {
		Map<String, String> emptyMap = new HashMap<>();

		NodeResolver nodeResolver = createNodeResolver("emptyMap");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			emptyMap
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getInterfaceResolvers()).hasSize(1);

		PathResolver<InterfaceResolver> resolver = result.getInterfaceResolvers().get(0);
		InterfaceResolver interfaceResolver = resolver.getCustomizer();

		JvmType resolvedType = interfaceResolver.resolve(new JavaType(Map.class));
		assertThat(resolvedType).isNotNull();
		assertThat(resolvedType.getRawType()).isEqualTo(HashMap.class);
	}

	@Property
	void analyzesNullValue() {
		NodeResolver nodeResolver = createNodeResolver("nullField");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			null
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getInterfaceResolvers()).isEmpty();
	}

	@Property
	void analyzesPrimitiveWrapperTypes() {
		// given
		Integer intValue = 42;

		NodeResolver nodeResolver = createNodeResolver("number");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			intValue
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		// when
		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		// then
		// Primitive wrappers (Integer, Long, etc.) are JDK value types — leaf nodes
		// that never get expanded into a node tree. No interface resolver is needed.
		assertThat(result.getInterfaceResolvers()).isEmpty();
	}

	@Property
	void analyzesMultipleComplexManipulators() {
		Person person = new Person("Alice", 25, new Address("1st St", "Tokyo", "10000"), Arrays.asList("music"));
		Order order = new Order("ORD-002", Collections.emptyList(), Collections.emptyMap());
		Animal pet = new Cat();

		ArbitraryManipulator manipulator1 = new ArbitraryManipulator(
			createNodeResolver("person"),
			new NodeSetDecomposedValueManipulator<>(
				1,
				createDecomposedContainerValueFactory(),
				Collections.emptyList(),
				person
			)
		);

		ArbitraryManipulator manipulator2 = new ArbitraryManipulator(
			createNodeResolver("order"),
			new NodeSetDecomposedValueManipulator<>(
				2,
				createDecomposedContainerValueFactory(),
				Collections.emptyList(),
				order
			)
		);

		ArbitraryManipulator manipulator3 = new ArbitraryManipulator(
			createNodeResolver("pet"),
			new NodeSetDecomposedValueManipulator<>(
				3,
				createDecomposedContainerValueFactory(),
				Collections.emptyList(),
				pet
			)
		);

		List<ArbitraryManipulator> manipulators = Arrays.asList(manipulator1, manipulator2, manipulator3);

		AnalysisResult result = ManipulatorAnalyzer.analyze(manipulators);

		// Person.hobbies(1) + Order.items(1) + Order.metadata(1) + Cat/pet(1) = 4
		assertThat(result.getInterfaceResolvers()).hasSize(4);

		PathExpression petPath = PathExpression.root().child("pet");
		PathResolver<InterfaceResolver> petResolver = result.getInterfaceResolvers().stream()
			.filter(r -> r.matches(petPath))
			.findFirst()
			.orElse(null);
		assertThat(petResolver).isNotNull();

		JvmType resolvedPetType = petResolver.getCustomizer().resolve(new JavaType(Animal.class));
		assertThat(resolvedPetType).isNotNull();
		assertThat(resolvedPetType.getRawType()).isEqualTo(Cat.class);
	}

	@Property
	void analyzesCompositeWithDecomposedValue() {
		Dog dog = new Dog("Shiba");

		NodeResolver nodeResolver = createNodeResolver("field");
		NodeManipulator decomposed = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			dog
		);
		NodeManipulator nullity = new NodeNullityManipulator(0, false);
		NodeManipulator composite = new CompositeNodeManipulator(decomposed, nullity);

		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, composite);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getInterfaceResolvers()).hasSize(1);

		PathResolver<InterfaceResolver> resolver = result.getInterfaceResolvers().get(0);
		PathExpression fieldPath = PathExpression.root().child("field");
		assertThat(resolver.matches(fieldPath)).isTrue();

		JvmType resolvedType = resolver.getCustomizer().resolve(new JavaType(Animal.class));
		assertThat(resolvedType).isNotNull();
		assertThat(resolvedType.getRawType()).isEqualTo(Dog.class);
	}

	@Property
	void analyzesApplyNodeCountWithDecomposedValue() {
		Dog dog = new Dog("Test Dog");

		NodeResolver nodeResolver = createNodeResolver("pet");
		NodeManipulator decomposed = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			dog
		);
		NodeManipulator counted = new ApplyNodeCountManipulator(decomposed, 5);

		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, counted);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getInterfaceResolvers()).hasSize(1);

		PathResolver<InterfaceResolver> resolver = result.getInterfaceResolvers().get(0);
		PathExpression petPath = PathExpression.root().child("pet");
		assertThat(resolver.matches(petPath)).isTrue();

		JvmType resolvedType = resolver.getCustomizer().resolve(new JavaType(Animal.class));
		assertThat(resolvedType).isNotNull();
		assertThat(resolvedType.getRawType()).isEqualTo(Dog.class);
	}

	@Property
	void analyzesApplyNodeCountWithConcreteClass() {
		Address address = new Address("Test St", "Test City", "00000");

		NodeResolver nodeResolver = createNodeResolver("address");
		NodeManipulator decomposed = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			address
		);
		NodeManipulator counted = new ApplyNodeCountManipulator(decomposed, 5);

		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, counted);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getInterfaceResolvers()).isEmpty();
	}

	@Property
	void resolverDoesNotMatchIncompatibleTypes() {
		Dog dog = new Dog("Corgi");

		NodeResolver nodeResolver = createNodeResolver("pet");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			dog
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getInterfaceResolvers()).hasSize(1);

		PathResolver<InterfaceResolver> resolver = result.getInterfaceResolvers().get(0);
		InterfaceResolver interfaceResolver = resolver.getCustomizer();

		JvmType resolvedType = interfaceResolver.resolve(new JavaType(String.class));
		assertThat(resolvedType).isNull();
	}

	@Property
	void extractsGenericTypeFromListOfStrings() {
		List<String> items = Arrays.asList("item1", "item2", "item3");

		NodeResolver nodeResolver = createNodeResolver("items");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			items
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getGenericTypeResolvers()).hasSize(1);

		PathResolver<GenericTypeResolver> resolver = result.getGenericTypeResolvers().get(0);
		PathExpression itemsPath = PathExpression.root().child("items");
		assertThat(resolver.matches(itemsPath)).isTrue();

		GenericTypeResolver genericResolver = resolver.getCustomizer();
		JvmType resolvedType = genericResolver.resolve(new JavaType(List.class));
		assertThat(resolvedType).isNotNull();
		assertThat(resolvedType.getTypeVariables()).hasSize(1);
		assertThat(resolvedType.getTypeVariables().get(0).getRawType()).isEqualTo(String.class);
	}

	@Property
	void extractsGenericTypeFromListOfIntegers() {
		List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

		NodeResolver nodeResolver = createNodeResolver("numbers");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			numbers
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getGenericTypeResolvers()).hasSize(1);

		PathResolver<GenericTypeResolver> resolver = result.getGenericTypeResolvers().get(0);
		GenericTypeResolver genericResolver = resolver.getCustomizer();
		JvmType resolvedType = genericResolver.resolve(new JavaType(List.class));

		assertThat(resolvedType.getTypeVariables()).hasSize(1);
		assertThat(resolvedType.getTypeVariables().get(0).getRawType()).isEqualTo(Integer.class);
	}

	@Property
	void extractsGenericTypeFromListOfComplexObjects() {
		List<OrderItem> orderItems = Arrays.asList(
			new OrderItem("Product A", 2, 10.0),
			new OrderItem("Product B", 1, 20.0)
		);

		NodeResolver nodeResolver = createNodeResolver("orderItems");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			orderItems
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getGenericTypeResolvers()).hasSize(1);

		PathResolver<GenericTypeResolver> resolver = result.getGenericTypeResolvers().get(0);
		GenericTypeResolver genericResolver = resolver.getCustomizer();
		JvmType resolvedType = genericResolver.resolve(new JavaType(List.class));

		assertThat(resolvedType.getTypeVariables()).hasSize(1);
		assertThat(resolvedType.getTypeVariables().get(0).getRawType()).isEqualTo(OrderItem.class);
	}

	@Property
	void extractsGenericTypeFromMapOfStringToString() {
		Map<String, String> metadata = new HashMap<>();
		metadata.put("key1", "value1");
		metadata.put("key2", "value2");

		NodeResolver nodeResolver = createNodeResolver("metadata");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			metadata
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getGenericTypeResolvers()).hasSize(1);

		PathResolver<GenericTypeResolver> resolver = result.getGenericTypeResolvers().get(0);
		PathExpression metadataPath = PathExpression.root().child("metadata");
		assertThat(resolver.matches(metadataPath)).isTrue();

		GenericTypeResolver genericResolver = resolver.getCustomizer();
		JvmType resolvedType = genericResolver.resolve(new JavaType(Map.class));

		assertThat(resolvedType.getTypeVariables()).hasSize(2);
		assertThat(resolvedType.getTypeVariables().get(0).getRawType()).isEqualTo(String.class);
		assertThat(resolvedType.getTypeVariables().get(1).getRawType()).isEqualTo(String.class);
	}

	@Property
	void extractsGenericTypeFromMapOfStringToInteger() {
		Map<String, Integer> scores = new HashMap<>();
		scores.put("Alice", 100);
		scores.put("Bob", 95);

		NodeResolver nodeResolver = createNodeResolver("scores");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			scores
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getGenericTypeResolvers()).hasSize(1);

		PathResolver<GenericTypeResolver> resolver = result.getGenericTypeResolvers().get(0);
		GenericTypeResolver genericResolver = resolver.getCustomizer();
		JvmType resolvedType = genericResolver.resolve(new JavaType(Map.class));

		assertThat(resolvedType.getTypeVariables()).hasSize(2);
		assertThat(resolvedType.getTypeVariables().get(0).getRawType()).isEqualTo(String.class);
		assertThat(resolvedType.getTypeVariables().get(1).getRawType()).isEqualTo(Integer.class);
	}

	@Property
	void extractsGenericTypeFromMapWithComplexValues() {
		Map<String, Address> addressMap = new HashMap<>();
		addressMap.put("home", new Address("123 Home St", "Seoul", "11111"));
		addressMap.put("work", new Address("456 Work Ave", "Busan", "22222"));

		NodeResolver nodeResolver = createNodeResolver("addresses");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			addressMap
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getGenericTypeResolvers()).hasSize(1);

		PathResolver<GenericTypeResolver> resolver = result.getGenericTypeResolvers().get(0);
		GenericTypeResolver genericResolver = resolver.getCustomizer();
		JvmType resolvedType = genericResolver.resolve(new JavaType(Map.class));

		assertThat(resolvedType.getTypeVariables()).hasSize(2);
		assertThat(resolvedType.getTypeVariables().get(0).getRawType()).isEqualTo(String.class);
		assertThat(resolvedType.getTypeVariables().get(1).getRawType()).isEqualTo(Address.class);
	}

	@Property
	void doesNotExtractGenericTypeFromEmptyCollection() {
		List<String> emptyList = new ArrayList<>();

		NodeResolver nodeResolver = createNodeResolver("emptyList");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			emptyList
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getGenericTypeResolvers()).isEmpty();

		assertThat(result.getInterfaceResolvers()).hasSize(1);
	}

	@Property
	void doesNotExtractGenericTypeFromEmptyMap() {
		Map<String, String> emptyMap = new HashMap<>();

		NodeResolver nodeResolver = createNodeResolver("emptyMap");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			emptyMap
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getGenericTypeResolvers()).isEmpty();

		assertThat(result.getInterfaceResolvers()).hasSize(1);
	}

	@Property
	void doesNotExtractGenericTypeFromNonCollectionTypes() {
		Address address = new Address("123 St", "City", "00000");

		NodeResolver nodeResolver = createNodeResolver("address");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			address
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getGenericTypeResolvers()).isEmpty();
	}

	@Property
	void extractsGenericTypeFromListOfInterfaceImplementations() {
		List<Animal> animals = Arrays.asList(new Dog("Poodle"), new Cat());

		NodeResolver nodeResolver = createNodeResolver("animals");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			animals
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getGenericTypeResolvers()).hasSize(1);

		PathResolver<GenericTypeResolver> resolver = result.getGenericTypeResolvers().get(0);
		GenericTypeResolver genericResolver = resolver.getCustomizer();
		JvmType resolvedType = genericResolver.resolve(new JavaType(List.class));

		assertThat(resolvedType.getTypeVariables()).hasSize(1);
		assertThat(resolvedType.getTypeVariables().get(0).getRawType()).isEqualTo(Dog.class);
	}

	@Property
	void extractsBothInterfaceAndGenericResolversFromList() {
		List<String> items = new ArrayList<>(Arrays.asList("a", "b", "c"));

		NodeResolver nodeResolver = createNodeResolver("items");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			items
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getInterfaceResolvers()).hasSize(1);
		assertThat(result.getGenericTypeResolvers()).hasSize(1);

		PathResolver<InterfaceResolver> interfaceResolver = result.getInterfaceResolvers().get(0);
		JvmType containerType = interfaceResolver.getCustomizer().resolve(new JavaType(List.class));
		assertThat(containerType).isNotNull();
		assertThat(List.class.isAssignableFrom(containerType.getRawType())).isTrue();

		PathResolver<GenericTypeResolver> genericResolver = result.getGenericTypeResolvers().get(0);
		JvmType resolvedType = genericResolver.getCustomizer().resolve(new JavaType(List.class));
		assertThat(resolvedType.getTypeVariables().get(0).getRawType()).isEqualTo(String.class);
	}

	@Property
	void extractsGenericTypeFromNestedListOfLists() {
		List<List<String>> nestedList = Arrays.asList(Arrays.asList("a", "b"), Arrays.asList("c", "d"));

		NodeResolver nodeResolver = createNodeResolver("matrix");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			nestedList
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getGenericTypeResolvers()).hasSize(1);

		PathResolver<GenericTypeResolver> resolver = result.getGenericTypeResolvers().get(0);
		GenericTypeResolver genericResolver = resolver.getCustomizer();
		JvmType resolvedType = genericResolver.resolve(new JavaType(List.class));

		assertThat(resolvedType.getTypeVariables()).hasSize(1);
		assertThat(List.class.isAssignableFrom(resolvedType.getTypeVariables().get(0).getRawType())).isTrue();
	}

	@Property
	void extractsGenericTypeFromCustomGenericSubclass() {
		StringContainer container = new StringContainer("test value", "Test Label");

		NodeResolver nodeResolver = createNodeResolver("container");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			container
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getGenericTypeResolvers()).hasSize(1);

		PathResolver<GenericTypeResolver> resolver = result.getGenericTypeResolvers().get(0);
		PathExpression containerPath = PathExpression.root().child("container");
		assertThat(resolver.matches(containerPath)).isTrue();

		GenericTypeResolver genericResolver = resolver.getCustomizer();
		JvmType resolvedType = genericResolver.resolve(new JavaType(Container.class));

		assertThat(resolvedType.getTypeVariables()).hasSize(1);
		assertThat(resolvedType.getTypeVariables().get(0).getRawType()).isEqualTo(String.class);
	}

	@Property
	void extractsGenericTypeFromCustomGenericSubclassWithComplexType() {
		AddressContainer container = new AddressContainer(new Address("123 St", "City", "00000"), "Address Label");

		NodeResolver nodeResolver = createNodeResolver("addressContainer");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			container
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getGenericTypeResolvers()).hasSize(1);

		PathResolver<GenericTypeResolver> resolver = result.getGenericTypeResolvers().get(0);
		GenericTypeResolver genericResolver = resolver.getCustomizer();
		JvmType resolvedType = genericResolver.resolve(new JavaType(Container.class));

		assertThat(resolvedType.getTypeVariables()).hasSize(1);
		assertThat(resolvedType.getTypeVariables().get(0).getRawType()).isEqualTo(Address.class);
	}

	@Property
	void extractsGenericTypeFromInterfaceImplementation() {
		IntegerWrapper wrapper = new IntegerWrapper(42);

		NodeResolver nodeResolver = createNodeResolver("wrapper");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			wrapper
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getGenericTypeResolvers()).hasSize(1);

		PathResolver<GenericTypeResolver> resolver = result.getGenericTypeResolvers().get(0);
		GenericTypeResolver genericResolver = resolver.getCustomizer();
		JvmType resolvedType = genericResolver.resolve(new JavaType(Wrapper.class));

		assertThat(resolvedType.getTypeVariables()).hasSize(1);
		assertThat(resolvedType.getTypeVariables().get(0).getRawType()).isEqualTo(Integer.class);
	}

	@Property
	void extractsMultipleGenericTypeArgumentsFromSubclass() {
		StringIntPair pair = new StringIntPair("key", 100);

		NodeResolver nodeResolver = createNodeResolver("pair");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			pair
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getGenericTypeResolvers()).hasSize(1);

		PathResolver<GenericTypeResolver> resolver = result.getGenericTypeResolvers().get(0);
		GenericTypeResolver genericResolver = resolver.getCustomizer();
		JvmType resolvedType = genericResolver.resolve(new JavaType(Pair.class));

		assertThat(resolvedType.getTypeVariables()).hasSize(2);
		assertThat(resolvedType.getTypeVariables().get(0).getRawType()).isEqualTo(String.class);
		assertThat(resolvedType.getTypeVariables().get(1).getRawType()).isEqualTo(Integer.class);
	}

	@Property
	void doesNotExtractGenericTypeFromRawGenericType() {
		Container<String> container = new Container<>("test", "label");

		NodeResolver nodeResolver = createNodeResolver("rawContainer");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			container
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getGenericTypeResolvers()).isEmpty();
	}

	@Property
	void extractsGenericTypeFromAnonymousSubclass() {
		Container<Double> container = new Container<Double>(3.14, "Pi") {
		};

		NodeResolver nodeResolver = createNodeResolver("anonymousContainer");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			container
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getGenericTypeResolvers()).hasSize(1);

		PathResolver<GenericTypeResolver> resolver = result.getGenericTypeResolvers().get(0);
		GenericTypeResolver genericResolver = resolver.getCustomizer();
		JvmType resolvedType = genericResolver.resolve(new JavaType(Container.class));

		assertThat(resolvedType.getTypeVariables()).hasSize(1);
		assertThat(resolvedType.getTypeVariables().get(0).getRawType()).isEqualTo(Double.class);
	}

	@Property
	void extractsContainerSizeFromList() {
		List<String> items = Arrays.asList("a", "b", "c");

		NodeResolver nodeResolver = createNodeResolver("items");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			items
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getContainerSizeResolvers()).hasSize(1);

		PathResolver<ContainerSizeResolver> resolver = result.getContainerSizeResolvers().get(0);
		PathExpression itemsPath = PathExpression.root().child("items");
		assertThat(resolver.matches(itemsPath)).isTrue();

		ContainerSizeResolver sizeResolver = resolver.getCustomizer();
		assertThat(sizeResolver.resolveContainerSize(new JavaType(List.class))).isEqualTo(3);
	}

	@Property
	void extractsContainerSizeFromMap() {
		Map<String, Integer> scores = new HashMap<>();
		scores.put("Alice", 100);
		scores.put("Bob", 95);

		NodeResolver nodeResolver = createNodeResolver("scores");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			scores
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getContainerSizeResolvers()).hasSize(1);

		PathResolver<ContainerSizeResolver> resolver = result.getContainerSizeResolvers().get(0);
		ContainerSizeResolver sizeResolver = resolver.getCustomizer();
		assertThat(sizeResolver.resolveContainerSize(new JavaType(Map.class))).isEqualTo(2);
	}

	@Property
	void extractsContainerSizeFromArray() {
		String[] items = {"a", "b", "c", "d"};

		NodeResolver nodeResolver = createNodeResolver("items");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			items
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getContainerSizeResolvers()).hasSize(1);

		PathResolver<ContainerSizeResolver> resolver = result.getContainerSizeResolvers().get(0);
		ContainerSizeResolver sizeResolver = resolver.getCustomizer();
		assertThat(sizeResolver.resolveContainerSize(new JavaType(String[].class))).isEqualTo(4);
	}

	@Property
	void extractsContainerSizeFromEmptyCollection() {
		List<String> emptyList = new ArrayList<>();

		NodeResolver nodeResolver = createNodeResolver("emptyList");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			emptyList
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getContainerSizeResolvers()).hasSize(1);

		PathResolver<ContainerSizeResolver> resolver = result.getContainerSizeResolvers().get(0);
		ContainerSizeResolver sizeResolver = resolver.getCustomizer();
		assertThat(sizeResolver.resolveContainerSize(new JavaType(List.class))).isEqualTo(0);
	}

	@Property
	void doesNotExtractContainerSizeFromNonContainer() {
		Address address = new Address("123 St", "City", "00000");

		NodeResolver nodeResolver = createNodeResolver("address");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			address
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getContainerSizeResolvers()).isEmpty();
	}

	@Property
	void extractsAllThreeResolversFromList() {
		List<String> items = new ArrayList<>(Arrays.asList("a", "b", "c"));

		NodeResolver nodeResolver = createNodeResolver("items");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			items
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getContainerSizeResolvers()).hasSize(1);
		assertThat(result.getGenericTypeResolvers()).hasSize(1);
		assertThat(result.getInterfaceResolvers()).hasSize(1);

		ContainerSizeResolver sizeResolver = result.getContainerSizeResolvers().get(0).getCustomizer();
		assertThat(sizeResolver.resolveContainerSize(new JavaType(List.class))).isEqualTo(3);

		GenericTypeResolver genericResolver = result.getGenericTypeResolvers().get(0).getCustomizer();
		JvmType resolvedGeneric = genericResolver.resolve(new JavaType(List.class));
		assertThat(resolvedGeneric.getTypeVariables().get(0).getRawType()).isEqualTo(String.class);

		InterfaceResolver interfaceResolver = result.getInterfaceResolvers().get(0).getCustomizer();
		JvmType resolvedInterface = interfaceResolver.resolve(new JavaType(List.class));
		assertThat(resolvedInterface).isNotNull();
	}

	@Property
	void analyzesNestedContainersExtractsAllLevels() {
		List<List<String>> nestedList = Arrays.asList(Arrays.asList("a", "b"), Arrays.asList("c", "d", "e"));

		NodeResolver nodeResolver = createNodeResolver("matrix");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			nestedList
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getContainerSizeResolvers()).hasSize(3);
		assertThat(result.getGenericTypeResolvers()).isNotEmpty();
		assertThat(result.getInterfaceResolvers()).isNotEmpty();

		PathResolver<ContainerSizeResolver> outerSizeResolver = result
			.getContainerSizeResolvers()
			.stream()
			.filter(r -> r.matches(PathExpression.root().child("matrix")))
			.findFirst()
			.orElse(null);
		assertThat(outerSizeResolver).isNotNull();
		assertThat(outerSizeResolver.getCustomizer().resolveContainerSize(new JavaType(List.class))).isEqualTo(2);

		PathResolver<ContainerSizeResolver> innerSizeResolver0 = result
			.getContainerSizeResolvers()
			.stream()
			.filter(r -> r.matches(PathExpression.root().child("matrix").index(0)))
			.findFirst()
			.orElse(null);
		assertThat(innerSizeResolver0).isNotNull();
		assertThat(innerSizeResolver0.getCustomizer().resolveContainerSize(new JavaType(List.class))).isEqualTo(2);

		PathResolver<ContainerSizeResolver> innerSizeResolver1 = result
			.getContainerSizeResolvers()
			.stream()
			.filter(r -> r.matches(PathExpression.root().child("matrix").index(1)))
			.findFirst()
			.orElse(null);
		assertThat(innerSizeResolver1).isNotNull();
		assertThat(innerSizeResolver1.getCustomizer().resolveContainerSize(new JavaType(List.class))).isEqualTo(3);
	}

	@Property
	void analyzesMapWithListValues() {
		Map<String, List<Integer>> mapWithLists = new HashMap<>();
		mapWithLists.put("scores", Arrays.asList(100, 95, 88));
		mapWithLists.put("ages", Arrays.asList(25, 30));

		NodeResolver nodeResolver = createNodeResolver("data");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			mapWithLists
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getContainerSizeResolvers()).hasSize(3);

		PathResolver<ContainerSizeResolver> mapSizeResolver = result
			.getContainerSizeResolvers()
			.stream()
			.filter(r -> r.matches(PathExpression.root().child("data")))
			.findFirst()
			.orElse(null);
		assertThat(mapSizeResolver).isNotNull();
		assertThat(mapSizeResolver.getCustomizer().resolveContainerSize(new JavaType(Map.class))).isEqualTo(2);

		assertThat(result.getGenericTypeResolvers()).isNotEmpty();
	}

	@Property
	void analyzesListOfMaps() {
		List<Map<String, Integer>> listOfMaps = Arrays.asList(createMap("a", 1, "b", 2), createMap("c", 3));

		NodeResolver nodeResolver = createNodeResolver("records");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			listOfMaps
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getContainerSizeResolvers()).hasSize(3);

		PathResolver<ContainerSizeResolver> listSizeResolver = result
			.getContainerSizeResolvers()
			.stream()
			.filter(r -> r.matches(PathExpression.root().child("records")))
			.findFirst()
			.orElse(null);
		assertThat(listSizeResolver).isNotNull();
		assertThat(listSizeResolver.getCustomizer().resolveContainerSize(new JavaType(List.class))).isEqualTo(2);

		PathResolver<ContainerSizeResolver> innerMapResolver0 = result
			.getContainerSizeResolvers()
			.stream()
			.filter(r -> r.matches(PathExpression.root().child("records").index(0)))
			.findFirst()
			.orElse(null);
		assertThat(innerMapResolver0).isNotNull();
		assertThat(innerMapResolver0.getCustomizer().resolveContainerSize(new JavaType(Map.class))).isEqualTo(2);

		PathResolver<ContainerSizeResolver> innerMapResolver1 = result
			.getContainerSizeResolvers()
			.stream()
			.filter(r -> r.matches(PathExpression.root().child("records").index(1)))
			.findFirst()
			.orElse(null);
		assertThat(innerMapResolver1).isNotNull();
		assertThat(innerMapResolver1.getCustomizer().resolveContainerSize(new JavaType(Map.class))).isEqualTo(1);
	}

	@Property
	void analyzesDeeplyNestedStructure() {
		List<List<List<String>>> deeplyNested = Arrays.asList(
			Arrays.asList(Arrays.asList("a", "b"), Arrays.asList("c")),
			Arrays.asList(Arrays.asList("d", "e", "f"))
		);

		NodeResolver nodeResolver = createNodeResolver("cube");
		NodeManipulator nodeManipulator = new NodeSetDecomposedValueManipulator<>(
			1,
			createDecomposedContainerValueFactory(),
			Collections.emptyList(),
			deeplyNested
		);
		ArbitraryManipulator manipulator = new ArbitraryManipulator(nodeResolver, nodeManipulator);

		AnalysisResult result = ManipulatorAnalyzer.analyze(Collections.singletonList(manipulator));

		assertThat(result.getContainerSizeResolvers()).hasSize(6);

		PathResolver<ContainerSizeResolver> rootResolver = result
			.getContainerSizeResolvers()
			.stream()
			.filter(r -> r.matches(PathExpression.root().child("cube")))
			.findFirst()
			.orElse(null);
		assertThat(rootResolver).isNotNull();
		assertThat(rootResolver.getCustomizer().resolveContainerSize(new JavaType(List.class))).isEqualTo(2);

		PathResolver<ContainerSizeResolver> level1Index0 = result
			.getContainerSizeResolvers()
			.stream()
			.filter(r -> r.matches(PathExpression.root().child("cube").index(0)))
			.findFirst()
			.orElse(null);
		assertThat(level1Index0).isNotNull();
		assertThat(level1Index0.getCustomizer().resolveContainerSize(new JavaType(List.class))).isEqualTo(2);

		PathResolver<ContainerSizeResolver> level1Index1 = result
			.getContainerSizeResolvers()
			.stream()
			.filter(r -> r.matches(PathExpression.root().child("cube").index(1)))
			.findFirst()
			.orElse(null);
		assertThat(level1Index1).isNotNull();
		assertThat(level1Index1.getCustomizer().resolveContainerSize(new JavaType(List.class))).isEqualTo(1);

		PathResolver<ContainerSizeResolver> level2Index00 = result
			.getContainerSizeResolvers()
			.stream()
			.filter(r -> r.matches(PathExpression.root().child("cube").index(0).index(0)))
			.findFirst()
			.orElse(null);
		assertThat(level2Index00).isNotNull();
		assertThat(level2Index00.getCustomizer().resolveContainerSize(new JavaType(List.class))).isEqualTo(2);

		PathResolver<ContainerSizeResolver> level2Index01 = result
			.getContainerSizeResolvers()
			.stream()
			.filter(r -> r.matches(PathExpression.root().child("cube").index(0).index(1)))
			.findFirst()
			.orElse(null);
		assertThat(level2Index01).isNotNull();
		assertThat(level2Index01.getCustomizer().resolveContainerSize(new JavaType(List.class))).isEqualTo(1);

		PathResolver<ContainerSizeResolver> level2Index10 = result
			.getContainerSizeResolvers()
			.stream()
			.filter(r -> r.matches(PathExpression.root().child("cube").index(1).index(0)))
			.findFirst()
			.orElse(null);
		assertThat(level2Index10).isNotNull();
		assertThat(level2Index10.getCustomizer().resolveContainerSize(new JavaType(List.class))).isEqualTo(3);
	}

	private Map<String, Integer> createMap(Object... keyValues) {
		Map<String, Integer> map = new HashMap<>();
		for (int i = 0; i < keyValues.length; i += 2) {
			map.put((String)keyValues[i], (Integer)keyValues[i + 1]);
		}
		return map;
	}

	private NodeResolver createNodeResolver(String propertyName) {
		return new CompositeNodeResolver(
			new NodePredicateResolver(StartNodePredicate.INSTANCE),
			new NodePredicateResolver(new PropertyNameNodePredicate(propertyName))
		);
	}

	private DecomposedContainerValueFactory createDecomposedContainerValueFactory() {
		return new DefaultDecomposedContainerValueFactory(value -> null);
	}

	interface Animal {
		String getName();
	}

	static class Dog implements Animal {

		private String breed;

		public Dog() {
		}

		public Dog(String breed) {
			this.breed = breed;
		}

		@Override
		public String getName() {
			return "Dog";
		}

		public String getBreed() {
			return breed;
		}
	}

	static class Cat implements Animal {

		@Override
		public String getName() {
			return "Cat";
		}
	}

	static class Person {

		private String name;
		private int age;
		private Address address;
		private List<String> hobbies;

		public Person() {
		}

		public Person(String name, int age, Address address, List<String> hobbies) {
			this.name = name;
			this.age = age;
			this.address = address;
			this.hobbies = hobbies;
		}

		public String getName() {
			return name;
		}

		public int getAge() {
			return age;
		}

		public Address getAddress() {
			return address;
		}

		public List<String> getHobbies() {
			return hobbies;
		}
	}

	static class Address {

		private String street;
		private String city;
		private String zipCode;

		public Address() {
		}

		public Address(String street, String city, String zipCode) {
			this.street = street;
			this.city = city;
			this.zipCode = zipCode;
		}

		public String getStreet() {
			return street;
		}

		public String getCity() {
			return city;
		}

		public String getZipCode() {
			return zipCode;
		}
	}

	static class Container<T> {

		private T value;
		private String label;

		public Container() {
		}

		public Container(T value, String label) {
			this.value = value;
			this.label = label;
		}

		public T getValue() {
			return value;
		}

		public String getLabel() {
			return label;
		}
	}

	static class StringContainer extends Container<String> {

		public StringContainer() {
			super();
		}

		public StringContainer(String value, String label) {
			super(value, label);
		}
	}

	static class AddressContainer extends Container<Address> {

		public AddressContainer() {
			super();
		}

		public AddressContainer(Address value, String label) {
			super(value, label);
		}
	}

	interface Wrapper<T> {
		T unwrap();
	}

	static class IntegerWrapper implements Wrapper<Integer> {

		private Integer value;

		public IntegerWrapper(Integer value) {
			this.value = value;
		}

		@Override
		public Integer unwrap() {
			return value;
		}
	}

	static class Pair<K, V> {

		private K first;
		private V second;

		public Pair() {
		}

		public Pair(K first, V second) {
			this.first = first;
			this.second = second;
		}

		public K getFirst() {
			return first;
		}

		public V getSecond() {
			return second;
		}
	}

	static class StringIntPair extends Pair<String, Integer> {

		public StringIntPair() {
			super();
		}

		public StringIntPair(String first, Integer second) {
			super(first, second);
		}
	}

	static class Order {

		private String orderId;
		private List<OrderItem> items;
		private Map<String, String> metadata;

		public Order() {
		}

		public Order(String orderId, List<OrderItem> items, Map<String, String> metadata) {
			this.orderId = orderId;
			this.items = items;
			this.metadata = metadata;
		}

		public String getOrderId() {
			return orderId;
		}

		public List<OrderItem> getItems() {
			return items;
		}

		public Map<String, String> getMetadata() {
			return metadata;
		}
	}

	static class OrderItem {

		private String productName;
		private int quantity;
		private double price;

		public OrderItem() {
		}

		public OrderItem(String productName, int quantity, double price) {
			this.productName = productName;
			this.quantity = quantity;
			this.price = price;
		}

		public String getProductName() {
			return productName;
		}

		public int getQuantity() {
			return quantity;
		}

		public double getPrice() {
			return price;
		}
	}
}
