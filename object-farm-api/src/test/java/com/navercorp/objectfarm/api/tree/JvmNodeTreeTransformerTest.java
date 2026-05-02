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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.FixedContainerSizeResolver;
import com.navercorp.objectfarm.api.node.JavaMapNodePromoter;
import com.navercorp.objectfarm.api.node.JavaNodeContext;
import com.navercorp.objectfarm.api.node.JavaObjectNodePromoter;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.node.JvmNodePromoter;
import com.navercorp.objectfarm.api.type.JavaType;

class JvmNodeTreeTransformerTest {

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
	void expandRecursiveNodeWithUserPaths() {
		// given
		Set<PathExpression> userPaths = new HashSet<>();
		userPaths.add(PathExpression.of("$.recursive.value"));

		ExpansionContext expansionContext = new ExpansionContext(
			userPaths
		);

		JvmNodeCandidateTreeContext treeContext = new JvmNodeCandidateTreeContext();

		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(SelfRecursive.class),
			CONTEXT
		)
			.withTreeContext(treeContext)
			.build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(
			CONTEXT,
			treeContext,
			PathResolverContext.empty(),
			expansionContext,
			null
		);

		// when
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// then
		JvmNode rootNode = nodeTree.getRootNode();
		List<JvmNode> rootChildren = nodeTree.getChildren(rootNode);

		JvmNode recursiveNode = findNodeByName(rootChildren, "recursive");
		then(recursiveNode).isNotNull();

		List<JvmNode> recursiveChildren = nodeTree.getChildren(recursiveNode);
		then(recursiveChildren).isNotEmpty();
		then(recursiveChildren).anyMatch(n -> n.getNodeName().equals("value"));

		JvmNode nestedRecursive = findNodeByName(recursiveChildren, "recursive");
		then(nestedRecursive).isNotNull();

		List<JvmNode> nestedRecursiveChildren = nodeTree.getChildren(nestedRecursive);
		then(nestedRecursiveChildren).isEmpty();
	}

	@Test
	void stopRecursiveNodeWithoutExpansionContext() {
		// given
		JvmNodeCandidateTreeContext treeContext = new JvmNodeCandidateTreeContext();

		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(SelfRecursive.class),
			CONTEXT
		)
			.withTreeContext(treeContext)
			.build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(
			CONTEXT,
			treeContext,
			PathResolverContext.empty()
		);

		// when
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// then
		JvmNode rootNode = nodeTree.getRootNode();
		List<JvmNode> rootChildren = nodeTree.getChildren(rootNode);

		JvmNode recursiveNode = findNodeByName(rootChildren, "recursive");
		then(recursiveNode).isNotNull();

		List<JvmNode> recursiveChildren = nodeTree.getChildren(recursiveNode);
		then(recursiveChildren).isNotEmpty();

		JvmNode nestedRecursive = findNodeByName(recursiveChildren, "recursive");
		then(nestedRecursive).isNotNull();

		List<JvmNode> nestedRecursiveChildren = nodeTree.getChildren(nestedRecursive);
		then(nestedRecursiveChildren).isEmpty();
	}

	@Test
	void expandDeepRecursivePath() {
		// given
		Set<PathExpression> userPaths = new HashSet<>();
		userPaths.add(PathExpression.of("$.next.next.value"));

		ExpansionContext expansionContext = new ExpansionContext(
			userPaths
		);

		JvmNodeCandidateTreeContext treeContext = new JvmNodeCandidateTreeContext();

		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(new JavaType(Node.class), CONTEXT)
			.withTreeContext(treeContext)
			.build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(
			CONTEXT,
			treeContext,
			PathResolverContext.empty(),
			expansionContext,
			null
		);

		// when
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// then
		JvmNode root = nodeTree.getRootNode();

		JvmNode next1 = findNodeByName(nodeTree.getChildren(root), "next");
		then(next1).isNotNull();

		List<JvmNode> next1Children = nodeTree.getChildren(next1);
		then(next1Children).isNotEmpty();

		JvmNode next2 = findNodeByName(next1Children, "next");
		then(next2).isNotNull();

		List<JvmNode> next2Children = nodeTree.getChildren(next2);
		then(next2Children).isNotEmpty();

		JvmNode value = findNodeByName(next2Children, "value");
		then(value).isNotNull();

		JvmNode next3 = findNodeByName(next2Children, "next");
		then(next3).isNotNull();

		List<JvmNode> next3Children = nodeTree.getChildren(next3);
		then(next3Children).isEmpty();
	}

	@Test
	void expandVeryDeepRecursivePath() {
		// given
		Set<PathExpression> userPaths = new HashSet<>();
		userPaths.add(PathExpression.of("$.recursive.recursive.recursive.value"));

		ExpansionContext expansionContext = new ExpansionContext(
			userPaths
		);

		JvmNodeCandidateTreeContext treeContext = new JvmNodeCandidateTreeContext();

		JvmNodeCandidateTree candidateTree = new JvmNodeCandidateTree.Builder(
			new JavaType(SelfRecursive.class),
			CONTEXT
		)
			.withTreeContext(treeContext)
			.build();

		JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(
			CONTEXT,
			treeContext,
			PathResolverContext.empty(),
			expansionContext,
			null
		);

		// when
		JvmNodeTree nodeTree = transformer.transform(candidateTree);

		// then
		JvmNode root = nodeTree.getRootNode();

		JvmNode recursive1 = findNodeByName(nodeTree.getChildren(root), "recursive");
		then(recursive1).isNotNull();

		List<JvmNode> recursive1Children = nodeTree.getChildren(recursive1);
		then(recursive1Children).isNotEmpty();

		JvmNode recursive2 = findNodeByName(recursive1Children, "recursive");
		then(recursive2).isNotNull();

		List<JvmNode> recursive2Children = nodeTree.getChildren(recursive2);
		then(recursive2Children).isNotEmpty();

		JvmNode recursive3 = findNodeByName(recursive2Children, "recursive");
		then(recursive3).isNotNull();

		List<JvmNode> recursive3Children = nodeTree.getChildren(recursive3);
		then(recursive3Children).isNotEmpty();

		JvmNode value = findNodeByName(recursive3Children, "value");
		then(value).isNotNull();

		JvmNode recursive4 = findNodeByName(recursive3Children, "recursive");
		then(recursive4).isNotNull();

		List<JvmNode> recursive4Children = nodeTree.getChildren(recursive4);
		then(recursive4Children).isEmpty();
	}

	private JvmNode findNodeByName(List<JvmNode> nodes, String name) {
		return nodes
			.stream()
			.filter(n -> name.equals(n.getNodeName()))
			.findFirst()
			.orElse(null);
	}

	public static class SelfRecursive {

		private String value;
		private SelfRecursive recursive;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public SelfRecursive getRecursive() {
			return recursive;
		}

		public void setRecursive(SelfRecursive recursive) {
			this.recursive = recursive;
		}
	}

	public static class Node {

		private String value;
		private Node next;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public Node getNext() {
			return next;
		}

		public void setNext(Node next) {
			this.next = next;
		}
	}
}
