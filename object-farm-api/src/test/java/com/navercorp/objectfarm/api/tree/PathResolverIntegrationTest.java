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

package com.navercorp.objectfarm.api.tree;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.FixedContainerSizeResolver;
import com.navercorp.objectfarm.api.node.JavaInterfaceNodePromoter;
import com.navercorp.objectfarm.api.node.JavaMapNodePromoter;
import com.navercorp.objectfarm.api.node.JavaNodeContext;
import com.navercorp.objectfarm.api.node.JavaObjectNodePromoter;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.node.JvmNodePromoter;
import com.navercorp.objectfarm.api.type.JavaType;

class PathResolverIntegrationTest {

	private static final ContainerSizeResolver DEFAULT_SIZE_RESOLVER = new FixedContainerSizeResolver(2);

	private static final List<JvmNodePromoter> PROMOTERS = Arrays.asList(
		new JavaInterfaceNodePromoter(),
		new JavaMapNodePromoter(),
		new JavaObjectNodePromoter()
	);

	private static final JavaNodeContext CONTEXT = JavaNodeContext.builder()
		.seed(-1L)
		.nodePromoters(PROMOTERS)
		.containerSizeResolver(DEFAULT_SIZE_RESOLVER)
		.build();

	@Test
	void simpleListWithPathBasedSize() {
		// given - List<String> with custom size 5
		PathResolverContext resolvers = PathResolverContext.builder()
			.addContainerSizeResolver("$", 5)  // root is the List
			.build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(
			CONTEXT,
			new JvmNodeCandidateTreeContext(),
			resolvers
		);

		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(List.class,
				Collections.singletonList(new JavaType(String.class)),
				Collections.emptyList()),
			CONTEXT
		).build();

		// when
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// then
		JvmNode root = nodeTree.getRootNode();
		List<JvmNode> elements = nodeTree.getChildren(root);
		then(elements).hasSize(5);  // configured by "$" -> 5
	}

	@Test
	void simpleListWithDefaultSize() {
		// given - List<String> with default size
		PathResolverContext resolvers = PathResolverContext.empty();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(
			CONTEXT,
			new JvmNodeCandidateTreeContext(),
			resolvers
		);

		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(List.class,
				Collections.singletonList(new JavaType(String.class)),
				Collections.emptyList()),
			CONTEXT
		).build();

		// when
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// then
		JvmNode root = nodeTree.getRootNode();
		List<JvmNode> elements = nodeTree.getChildren(root);
		then(elements).hasSize(2);  // default size from DEFAULT_SIZE_RESOLVER
	}

	@SuppressWarnings("unused")
	private static class SimpleListHolder {
		private List<String> items;
	}

	@Test
	void objectWithListFieldAndPathBasedSize() {
		// given - SimpleListHolder with items having custom size
		PathResolverContext resolvers = PathResolverContext.builder()
			.addContainerSizeResolver("$.items", 4)
			.build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(
			CONTEXT,
			new JvmNodeCandidateTreeContext(),
			resolvers
		);

		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(SimpleListHolder.class), CONTEXT
		).build();

		// when
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// then
		JvmNode root = nodeTree.getRootNode();
		List<JvmNode> rootChildren = nodeTree.getChildren(root);
		then(rootChildren).hasSize(1);

		JvmNode itemsNode = rootChildren.get(0);
		then(itemsNode.getNodeName()).isEqualTo("items");

		List<JvmNode> elements = nodeTree.getChildren(itemsNode);
		then(elements).hasSize(4);  // configured by "$.items" -> 4
	}

	@SuppressWarnings("unused")
	private static class ListOfObjectsHolder {
		private List<InnerObject> items;
	}

	@SuppressWarnings("unused")
	private static class InnerObject {
		private String name;
	}

	@Test
	void listOfObjectsWithPathBasedSize() {
		// given - List<InnerObject> with custom size 4
		PathResolverContext resolvers = PathResolverContext.builder()
			.addContainerSizeResolver("$.items", 4)
			.build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(
			CONTEXT,
			new JvmNodeCandidateTreeContext(),
			resolvers
		);

		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(ListOfObjectsHolder.class), CONTEXT
		).build();

		// when
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// then
		JvmNode root = nodeTree.getRootNode();
		List<JvmNode> rootChildren = nodeTree.getChildren(root);
		then(rootChildren).hasSize(1);

		JvmNode itemsNode = rootChildren.get(0);
		then(itemsNode.getNodeName()).isEqualTo("items");

		List<JvmNode> elements = nodeTree.getChildren(itemsNode);
		then(elements).hasSize(4);  // configured by "$.items" -> 4

		// Each element should have the InnerObject's fields
		for (JvmNode element : elements) {
			List<JvmNode> elementChildren = nodeTree.getChildren(element);
			then(elementChildren).hasSize(1);  // name field
			then(elementChildren.get(0).getNodeName()).isEqualTo("name");
		}
	}

	@SuppressWarnings("unused")
	private static class NestedListHolder {
		private List<List<String>> items;
	}

	@Test
	void nestedListWithPathBasedSize() {
		// given - List<List<String>> with outer size 3, inner size 5
		PathResolverContext resolvers = PathResolverContext.builder()
			.addContainerSizeResolver("$.items", 3)        // outer list
			.addContainerSizeResolver("$.items[*]", 5)     // inner lists
			.build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(
			CONTEXT,
			new JvmNodeCandidateTreeContext(),
			resolvers
		);

		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(NestedListHolder.class), CONTEXT
		).build();

		// when
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// then
		JvmNode root = nodeTree.getRootNode();
		List<JvmNode> rootChildren = nodeTree.getChildren(root);
		then(rootChildren).hasSize(1);

		JvmNode itemsNode = rootChildren.get(0);
		then(itemsNode.getNodeName()).isEqualTo("items");

		// Outer list should have 3 elements
		List<JvmNode> outerElements = nodeTree.getChildren(itemsNode);
		then(outerElements).hasSize(3);  // configured by "$.items" -> 3

		// Each inner list should have 5 elements
		for (JvmNode outerElement : outerElements) {
			List<JvmNode> innerElements = nodeTree.getChildren(outerElement);
			then(innerElements).hasSize(5);  // configured by "$.items[*]" -> 5
		}
	}

	@Test
	void directNestedListType() {
		// given - List<List<String>> as root type
		PathResolverContext resolvers = PathResolverContext.builder()
			.addContainerSizeResolver("$", 3)       // outer list
			.addContainerSizeResolver("$[*]", 4)    // inner lists
			.build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(
			CONTEXT,
			new JvmNodeCandidateTreeContext(),
			resolvers
		);

		// List<List<String>>
		JavaType innerListType = new JavaType(List.class,
			Collections.singletonList(new JavaType(String.class)),
			Collections.emptyList());
		JavaType outerListType = new JavaType(List.class,
			Collections.singletonList(innerListType),
			Collections.emptyList());

		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			outerListType, CONTEXT
		).build();

		// when
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// then
		JvmNode root = nodeTree.getRootNode();
		List<JvmNode> outerElements = nodeTree.getChildren(root);
		then(outerElements).hasSize(3);  // configured by "$" -> 3

		// Each inner list should have 4 elements
		for (JvmNode outerElement : outerElements) {
			List<JvmNode> innerElements = nodeTree.getChildren(outerElement);
			then(innerElements).hasSize(4);  // configured by "$[*]" -> 4
		}
	}
}
