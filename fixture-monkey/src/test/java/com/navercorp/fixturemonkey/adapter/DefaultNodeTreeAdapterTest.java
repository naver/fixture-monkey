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

package com.navercorp.fixturemonkey.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.customizer.ContainerInfoManipulator;
import com.navercorp.fixturemonkey.customizer.ManipulatorSet;
import com.navercorp.fixturemonkey.tree.NextNodePredicate;
import com.navercorp.fixturemonkey.tree.PropertyNameNodePredicate;
import com.navercorp.fixturemonkey.tree.StartNodePredicate;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.tree.JvmNodeTree;
import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;

class DefaultNodeTreeAdapterTest {

	static class SimpleClass {

		private String name;
		private int value;
	}

	static class ClassWithList {

		private List<String> items;
	}

	static class ClassWithNestedList {

		private List<List<String>> nestedItems;
	}

	static class ClassWithMap {

		private Map<String, Integer> data;
	}

	@Property
	void adaptsSimpleClassWithoutManipulators() {
		DefaultNodeTreeAdapter adapter = new DefaultNodeTreeAdapter(12345L);
		JvmType rootType = new JavaType(SimpleClass.class);
		ManipulatorSet manipulatorSet = new ManipulatorSet(Collections.emptyList(), Collections.emptyList());

		JvmNodeTree tree = adapter.adapt(rootType, manipulatorSet).getNodeTree();

		assertThat(tree).isNotNull();
		assertThat(tree.getRootNode()).isNotNull();
		assertThat(tree.getRootNode().getConcreteType().getRawType()).isEqualTo(SimpleClass.class);

		List<JvmNode> children = tree.getChildren(tree.getRootNode());
		assertThat(children).hasSize(2);
	}

	@Property
	void adaptsClassWithListUsingContainerInfoManipulator() {
		DefaultNodeTreeAdapter adapter = new DefaultNodeTreeAdapter(12345L);

		JvmType rootType = new JavaType(ClassWithList.class, Collections.emptyList(), Collections.emptyList());

		List<NextNodePredicate> predicates = Arrays.asList(
			StartNodePredicate.INSTANCE,
			new PropertyNameNodePredicate("items")
		);
		ContainerInfoManipulator containerManipulator = new ContainerInfoManipulator(
			predicates,
			new ArbitraryContainerInfo(3, 3),
			0
		);

		ManipulatorSet manipulatorSet = new ManipulatorSet(
			Collections.emptyList(),
			Collections.singletonList(containerManipulator)
		);

		JvmNodeTree tree = adapter.adapt(rootType, manipulatorSet).getNodeTree();

		assertThat(tree).isNotNull();

		List<JvmNode> children = tree.getChildren(tree.getRootNode());
		JvmNode itemsNode = children
			.stream()
			.filter(node -> "items".equals(node.getNodeName()))
			.findFirst()
			.orElse(null);

		assertThat(itemsNode).isNotNull();

		List<JvmNode> itemsChildren = tree.getChildren(itemsNode);
		assertThat(itemsChildren).hasSize(3);
	}

	@Property
	void adaptsClassWithNestedListUsingMultipleContainerInfoManipulators() {
		DefaultNodeTreeAdapter adapter = new DefaultNodeTreeAdapter(12345L);

		JvmType rootType = new JavaType(ClassWithNestedList.class);

		List<NextNodePredicate> outerPredicates = Arrays.asList(
			StartNodePredicate.INSTANCE,
			new PropertyNameNodePredicate("nestedItems")
		);
		ContainerInfoManipulator outerManipulator = new ContainerInfoManipulator(
			outerPredicates,
			new ArbitraryContainerInfo(2, 2),
			0
		);

		ManipulatorSet manipulatorSet = new ManipulatorSet(
			Collections.emptyList(),
			Collections.singletonList(outerManipulator)
		);

		JvmNodeTree tree = adapter.adapt(rootType, manipulatorSet).getNodeTree();

		assertThat(tree).isNotNull();

		List<JvmNode> children = tree.getChildren(tree.getRootNode());
		JvmNode nestedItemsNode = children
			.stream()
			.filter(node -> "nestedItems".equals(node.getNodeName()))
			.findFirst()
			.orElse(null);

		assertThat(nestedItemsNode).isNotNull();

		List<JvmNode> outerChildren = tree.getChildren(nestedItemsNode);
		assertThat(outerChildren).hasSize(2);
	}

	@Property
	void emptyManipulatorSetProducesTreeWithDefaultContainerSizes() {
		DefaultNodeTreeAdapter adapter = new DefaultNodeTreeAdapter(12345L);

		JvmType rootType = new JavaType(ClassWithList.class);
		ManipulatorSet manipulatorSet = new ManipulatorSet(Collections.emptyList(), Collections.emptyList());

		JvmNodeTree tree = adapter.adapt(rootType, manipulatorSet).getNodeTree();

		assertThat(tree).isNotNull();

		List<JvmNode> children = tree.getChildren(tree.getRootNode());
		JvmNode itemsNode = children
			.stream()
			.filter(node -> "items".equals(node.getNodeName()))
			.findFirst()
			.orElse(null);

		assertThat(itemsNode).isNotNull();

		List<JvmNode> itemsChildren = tree.getChildren(itemsNode);
		assertThat(itemsChildren.size()).isBetween(0, 3);
	}

	@Property
	void sameSeedProducesSameTreeStructure() {
		long seed = 12345L;

		DefaultNodeTreeAdapter adapter1 = new DefaultNodeTreeAdapter(seed);
		DefaultNodeTreeAdapter adapter2 = new DefaultNodeTreeAdapter(seed);

		JvmType rootType = new JavaType(ClassWithList.class);
		ManipulatorSet manipulatorSet = new ManipulatorSet(Collections.emptyList(), Collections.emptyList());

		JvmNodeTree tree1 = adapter1.adapt(rootType, manipulatorSet).getNodeTree();
		JvmNodeTree tree2 = adapter2.adapt(rootType, manipulatorSet).getNodeTree();

		int itemsChildrenCount1 = tree1
			.getChildren(
				tree1
					.getChildren(tree1.getRootNode())
					.stream()
					.filter(node -> "items".equals(node.getNodeName()))
					.findFirst()
					.orElse(null)
			)
			.size();

		int itemsChildrenCount2 = tree2
			.getChildren(
				tree2
					.getChildren(tree2.getRootNode())
					.stream()
					.filter(node -> "items".equals(node.getNodeName()))
					.findFirst()
					.orElse(null)
			)
			.size();

		assertThat(itemsChildrenCount1).isEqualTo(itemsChildrenCount2);
	}
}
