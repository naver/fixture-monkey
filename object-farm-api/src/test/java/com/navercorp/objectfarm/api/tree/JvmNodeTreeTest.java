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
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.FixedContainerSizeResolver;
import com.navercorp.objectfarm.api.node.JavaDefaultNodePromoter;
import com.navercorp.objectfarm.api.node.JavaInterfaceNodePromoter;
import com.navercorp.objectfarm.api.node.JavaMapNodePromoter;
import com.navercorp.objectfarm.api.node.JavaNodeContext;
import com.navercorp.objectfarm.api.node.JavaObjectNodePromoter;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.node.JvmNodePromoter;
import com.navercorp.objectfarm.api.nodecandidate.JvmMapNodeCandidate;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.Types;

class JvmNodeTreeTest {
	private static final ContainerSizeResolver FIXED_SIZE_RESOLVER = new FixedContainerSizeResolver(2);

	private static final List<JvmNodePromoter> PROMOTERS = Arrays.asList(
		new JavaMapNodePromoter(),
		new JavaObjectNodePromoter()
	);

	private static final JavaNodeContext CONTEXT = JavaNodeContext.builder()
		.seed(-1L)
		.nodePromoters(PROMOTERS)
		.containerSizeResolver(FIXED_SIZE_RESOLVER)
		.build();

	@Test
	void transformSimpleTypeShouldCreateSingleNode() {
		// given
		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(String.class), CONTEXT
		).build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(CONTEXT);

		// when
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// then
		then(nodeTree).isNotNull();
		then(nodeTree.getRootNode()).isNotNull();
		then(nodeTree.size()).isEqualTo(1);
	}

	@Test
	void transformShouldMaintainNodeToCandidateMapping() {
		// given
		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(TestClass.class), CONTEXT
		).build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(CONTEXT);

		// when
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// then - verify nodes with source candidates have proper mappings
		// Note: Container element nodes (generated during transformation) don't have source candidates
		int nodesWithCandidates = 0;
		for (JvmNode node : nodeTree.getAllNodes()) {
			JvmNodeCandidate candidate = nodeTree.getSourceCandidate(node);
			if (candidate != null) {
				nodesWithCandidates++;
				// Verify reverse mapping
				List<JvmNode> promotedNodes = nodeTree.getPromotedNodes(candidate);
				then(promotedNodes).contains(node);
			}
		}
		// At minimum, root and its direct children should have candidates
		then(nodesWithCandidates).isGreaterThanOrEqualTo(4); // TestClass + name + value + items
	}

	@Test
	void transformMapTypeShouldReturn1toNMapping() {
		// given
		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(Map.class,
				Arrays.asList(new JavaType(String.class), new JavaType(Integer.class)),
				Collections.emptyList()),
			CONTEXT
		).build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(CONTEXT);

		// when
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// then - Map entries should produce 2 nodes each (key + value)
		then(nodeTree).isNotNull();

		// Verify 1:N mapping for map entries
		List<JvmNodeCandidate> candidateChildren = candidateTree.getChildren(candidateTree.getRootNode());
		for (JvmNodeCandidate candidate : candidateChildren) {
			if (candidate instanceof JvmMapNodeCandidate) {
				List<JvmNode> promotedNodes = nodeTree.getPromotedNodes(candidate);
				then(promotedNodes).hasSize(2); // key + value
				then(promotedNodes.get(0).getConcreteType().getRawType()).isEqualTo(String.class);
				then(promotedNodes.get(1).getConcreteType().getRawType()).isEqualTo(Integer.class);
			}
		}
	}

	@Test
	void getChildrenShouldReturnEmptyListForLeafNode() {
		// given
		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(String.class), CONTEXT
		).build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(CONTEXT);
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// when
		List<JvmNode> children = nodeTree.getChildren(nodeTree.getRootNode());

		// then
		then(children).isEmpty();
	}

	@Test
	void getSourceCandidateShouldReturnCorrectCandidate() {
		// given
		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(String.class), CONTEXT
		).build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(CONTEXT);
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// when
		JvmNodeCandidate sourceCandidate = nodeTree.getSourceCandidate(nodeTree.getRootNode());

		// then
		then(sourceCandidate).isEqualTo(candidateTree.getRootNode());
	}

	@Test
	void getPromotedNodesShouldReturnAllNodesFor1toNMapping() {
		// given
		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(Map.class,
				Arrays.asList(new JavaType(String.class), new JavaType(Integer.class)),
				Collections.emptyList()),
			CONTEXT
		).build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(CONTEXT);
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// when & then - each map entry candidate produces 2 nodes
		List<JvmNodeCandidate> children = candidateTree.getChildren(candidateTree.getRootNode());
		for (JvmNodeCandidate child : children) {
			if (child instanceof JvmMapNodeCandidate) {
				List<JvmNode> nodes = nodeTree.getPromotedNodes(child);
				then(nodes).hasSize(2);
			}
		}
	}

	@Test
	void getPromotedNodeShouldReturnFirstNodeForConvenience() {
		// given
		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(String.class), CONTEXT
		).build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(CONTEXT);
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// when
		JvmNode promotedNode = nodeTree.getPromotedNode(candidateTree.getRootNode());

		// then
		then(promotedNode).isEqualTo(nodeTree.getRootNode());
	}

	@Test
	void containsShouldReturnTrueForExistingNode() {
		// given
		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(String.class), CONTEXT
		).build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(CONTEXT);
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// when & then
		then(nodeTree.contains(nodeTree.getRootNode())).isTrue();
	}

	@Test
	void transformListTypeShouldWork() {
		// given
		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(List.class,
				Collections.singletonList(new JavaType(String.class)),
				Collections.emptyList()),
			CONTEXT
		).build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(CONTEXT);

		// when
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// then - Container elements are generated during transformation
		then(nodeTree.getRootNode()).isNotNull();

		List<JvmNode> elements = nodeTree.getChildren(nodeTree.getRootNode());
		then(elements).hasSize(2); // FIXED_SIZE_RESOLVER returns 2
		then(elements).allMatch(it -> it.getConcreteType().getRawType() == String.class);
	}

	@Test
	void transformCircularReferenceShouldNotCauseInfiniteLoop() {
		// given
		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(SelfReferenceClass.class), CONTEXT
		).build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(CONTEXT);

		// when
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// then
		then(nodeTree).isNotNull();
		then(nodeTree.size()).isGreaterThan(0);
	}

	@Test
	void transformWithNoPromoterShouldThrowException() {
		// given
		JavaNodeContext emptyPromoterContext = JavaNodeContext.builder()
			.seed(-1L)
			.nodePromoters(Collections.emptyList())
			.containerSizeResolver(FIXED_SIZE_RESOLVER)
			.build();

		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(String.class), emptyPromoterContext
		).build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(emptyPromoterContext);

		// when & then
		thenThrownBy(() -> transformer.transform(candidateTree))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("No suitable promoter found");
	}

	@Test
	void getAllNodesShouldReturnUnmodifiableList() {
		// given
		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(String.class), CONTEXT
		).build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(CONTEXT);
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// when & then
		List<JvmNode> allNodes = nodeTree.getAllNodes();
		thenThrownBy(() -> allNodes.add(nodeTree.getRootNode()))
			.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void getPromotedNodesForUnknownCandidateShouldReturnEmptyList() {
		// given
		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(String.class), CONTEXT
		).build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(CONTEXT);
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// Create a different candidate tree
		JvmNodeCandidateTree otherTree = new JvmNodeCandidateTree.Builder(
			new JavaType(Integer.class), CONTEXT
		).build();

		// when
		List<JvmNode> nodes = nodeTree.getPromotedNodes(otherTree.getRootNode());

		// then
		then(nodes).isEmpty();
	}

	@Test
	void transformShouldGenerateChildrenForContainerElements() {
		// given
		JvmNodeCandidateTreeContext treeContext = new JvmNodeCandidateTreeContext();
		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(CONTEXT, treeContext,
			PathResolverContext.empty());

		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(ObjectContainerClass.class), CONTEXT
		).build();

		// when
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// then - verify tree is built correctly
		then(nodeTree).isNotNull();
		List<JvmNode> rootChildren = nodeTree.getChildren(nodeTree.getRootNode());
		then(rootChildren).hasSize(1); // listOfObjects field

		JvmNode listNode = rootChildren.get(0);
		List<JvmNode> listElements = nodeTree.getChildren(listNode);
		then(listElements).hasSize(2); // FIXED_SIZE_RESOLVER returns 2

		// Verify each element has children (InnerObject's field)
		for (JvmNode element : listElements) {
			List<JvmNode> elementChildren = nodeTree.getChildren(element);
			then(elementChildren).hasSize(1); // name field
			then(elementChildren.get(0).getNodeName()).isEqualTo("name");
		}
	}

	@Test
	void transformWithSharedTreeContextShouldPopulateCache() {
		// given
		JvmNodeCandidateTreeContext sharedContext = new JvmNodeCandidateTreeContext();
		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(CONTEXT, sharedContext,
			PathResolverContext.empty());

		// Transform with nested container elements
		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(NestedObjectContainerClass.class), CONTEXT
		).build();

		// when
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// then - verify nested structure is built correctly
		then(nodeTree).isNotNull();
		List<JvmNode> rootChildren = nodeTree.getChildren(nodeTree.getRootNode());
		then(rootChildren).hasSize(1); // listOfNestedObjects field

		JvmNode listNode = rootChildren.get(0);
		List<JvmNode> listElements = nodeTree.getChildren(listNode);
		then(listElements).hasSize(2); // FIXED_SIZE_RESOLVER returns 2

		// Verify NestedObject has inner field
		JvmNode nestedElement = listElements.get(0);
		List<JvmNode> nestedChildren = nodeTree.getChildren(nestedElement);
		then(nestedChildren).hasSize(1); // inner field

		// Verify InnerObject has name field
		JvmNode innerNode = nestedChildren.get(0);
		List<JvmNode> innerChildren = nodeTree.getChildren(innerNode);
		then(innerChildren).hasSize(1); // name field
		then(innerChildren.get(0).getNodeName()).isEqualTo("name");

		// Cache should be populated for NestedObject (which has non-Java children)
		then(sharedContext.getCacheSize()).isGreaterThan(0);
	}

	@Test
	void transformInterfaceFieldShouldGenerateChildrenAfterResolution() {
		// given - Interface resolves to TestInterfaceImpl which has 'implField'
		JavaNodeContext contextWithInterfaceResolver = JavaNodeContext.builder()
			.seed(-1L)
			.nodePromoters(Arrays.asList(
				new JavaInterfaceNodePromoter(),
				new JavaDefaultNodePromoter(PROMOTERS)
			))
			.containerSizeResolver(FIXED_SIZE_RESOLVER)
			.interfaceResolver(type -> {
				if (Types.isAssignable(TestInterface.class, type.getRawType())) {
					return new JavaType(TestInterfaceImpl.class);
				}
				return null;
			})
			.build();

		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(ClassWithInterfaceField.class), contextWithInterfaceResolver
		).build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(contextWithInterfaceResolver);

		// when
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// then
		JvmNode root = nodeTree.getRootNode();
		List<JvmNode> rootChildren = nodeTree.getChildren(root);
		then(rootChildren).hasSize(1);

		// interfaceField should be resolved to TestInterfaceImpl
		JvmNode interfaceFieldNode = rootChildren.get(0);
		then(interfaceFieldNode.getNodeName()).isEqualTo("interfaceField");
		then(interfaceFieldNode.getConcreteType().getRawType()).isEqualTo(TestInterfaceImpl.class);

		// TestInterfaceImpl has 'implField' - this should be generated!
		List<JvmNode> implChildren = nodeTree.getChildren(interfaceFieldNode);
		then(implChildren).hasSize(1);  // Expected to fail if interface children not generated
		then(implChildren.get(0).getNodeName()).isEqualTo("implField");
	}

	// Test classes
	@SuppressWarnings("unused")
	private static class TestClass {
		private String name;
		private int value;
		private List<String> items;
	}

	@SuppressWarnings("unused")
	private static class SelfReferenceClass {
		private SelfReferenceClass self;
	}

	@SuppressWarnings("unused")
	private static class InnerObject {
		private String name;
	}

	@SuppressWarnings("unused")
	private static class ObjectContainerClass {
		private List<InnerObject> listOfObjects;
	}

	@SuppressWarnings("unused")
	private static class NestedObject {
		private InnerObject inner;
	}

	// Interface test classes
	private interface TestInterface {
		void doSomething();
	}

	@SuppressWarnings("unused")
	private static class TestInterfaceImpl implements TestInterface {
		private String implField;

		@Override
		public void doSomething() {
		}
	}

	@SuppressWarnings("unused")
	private static class ClassWithInterfaceField {
		private TestInterface interfaceField;
	}

	@SuppressWarnings("unused")
	private static class NestedObjectContainerClass {
		private List<NestedObject> listOfNestedObjects;
	}

	@SuppressWarnings("unused")
	private static class SimpleListClass {
		private List<String> items;
	}

	@Test
	void transformWithPathBasedContainerSizeResolver() {
		// given - List with custom size 5
		PathResolverContext resolvers = PathResolverContext.builder()
			.addContainerSizeResolver("$.items", 5)
			.build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(
			CONTEXT,
			new JvmNodeCandidateTreeContext(),
			resolvers
		);

		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(SimpleListClass.class), CONTEXT
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
		then(elements).hasSize(5);  // configured by "$.items" -> 5
	}

	@Test
	void transformWithPathBasedResolverShouldFallbackToDefault() {
		// given - no matching path, should use default (FIXED_SIZE_RESOLVER = 2)
		PathResolverContext resolvers = PathResolverContext.builder()
			.addContainerSizeResolver("$.other", 10)  // non-matching path
			.build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(
			CONTEXT,
			new JvmNodeCandidateTreeContext(),
			resolvers
		);

		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(SimpleListClass.class), CONTEXT
		).build();

		// when
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// then
		JvmNode root = nodeTree.getRootNode();
		JvmNode itemsNode = nodeTree.getChildren(root).get(0);

		List<JvmNode> elements = nodeTree.getChildren(itemsNode);
		then(elements).hasSize(2);  // default from FIXED_SIZE_RESOLVER
	}

	@Test
	void transformWithEmptyResolverContextShouldUseDefaults() {
		// given
		PathResolverContext resolvers = PathResolverContext.empty();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(
			CONTEXT,
			new JvmNodeCandidateTreeContext(),
			resolvers
		);

		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(SimpleListClass.class), CONTEXT
		).build();

		// when
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// then - container should use default size (2)
		JvmNode root = nodeTree.getRootNode();
		JvmNode itemsNode = nodeTree.getChildren(root).get(0);

		List<JvmNode> elements = nodeTree.getChildren(itemsNode);
		then(elements).hasSize(2);  // default
	}
}
