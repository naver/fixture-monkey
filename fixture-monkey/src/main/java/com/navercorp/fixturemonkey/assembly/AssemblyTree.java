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

package com.navercorp.fixturemonkey.assembly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.tree.JvmNodeTree;

/**
 * The tree one assembly generates, the only way assembly sees a tree: the plan's tree, which it never changes, with
 * containers resized to the value they hold, concrete subtrees attached where the plan's tree has none, and the
 * nodes assembly placed at each path.
 */
final class AssemblyTree {
	private final JvmNodeTree plannedTree;
	private final Map<JvmNode, List<JvmNode>> resizedChildrenByNode = new IdentityHashMap<>();
	private final Map<JvmNode, JvmNodeTree> concreteTreeByNode = new IdentityHashMap<>();
	private final Map<String, JvmNode> nodeByPath = new HashMap<>();

	AssemblyTree(JvmNodeTree plannedTree) {
		this.plannedTree = plannedTree;
	}

	JvmNode getRootNode() {
		return plannedTree.getRootNode();
	}

	List<JvmNode> childrenOf(JvmNode node) {
		List<JvmNode> resized = resizedChildrenByNode.get(node);
		if (resized != null) {
			return resized;
		}
		List<JvmNode> children = plannedTree.getChildren(node);
		if (!children.isEmpty()) {
			return children;
		}
		JvmNodeTree concreteTree = concreteTreeByNode.get(node);
		if (concreteTree != null) {
			return concreteTree.getChildren(node);
		}
		return Collections.emptyList();
	}

	@Nullable
	JvmNode parentOf(JvmNode node) {
		JvmNode parent = plannedTree.getParent(node);
		if (parent != null) {
			return parent;
		}
		JvmNodeTree concreteTree = concreteTreeByNode.get(node);
		if (concreteTree != null) {
			return concreteTree.getParent(node);
		}
		return null;
	}

	@Nullable
	JvmNode nodeAt(PathExpression path) {
		JvmNode node = nodeByPath.get(path.toExpression());
		if (node != null) {
			return node;
		}
		return plannedTree.resolve(path);
	}

	@Nullable
	JvmNode plannedNodeAt(PathExpression path) {
		return plannedTree.resolve(path);
	}

	List<JvmNode> ancestorNodes(PathExpression path) {
		int segmentCount = path.getSegments().size();
		List<JvmNode> nodes = new ArrayList<>(segmentCount);
		for (int depth = 0; depth < segmentCount; depth++) {
			PathExpression ancestor = path.truncateTo(depth);
			JvmNode node = nodeAt(ancestor);
			if (node != null) {
				nodes.add(node);
			}
		}
		return nodes;
	}

	void placeNode(PathExpression path, JvmNode node) {
		nodeByPath.put(path.toExpression(), node);
	}

	void resize(JvmNode containerNode, List<JvmNode> children) {
		resizedChildrenByNode.put(containerNode, children);
	}

	void attach(JvmNodeTree concreteTree) {
		for (JvmNode treeNode : concreteTree.getAllNodes()) {
			concreteTreeByNode.put(treeNode, concreteTree);
		}
	}
}
