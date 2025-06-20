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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.expression.IndexSelector;
import com.navercorp.objectfarm.api.expression.KeySelector;
import com.navercorp.objectfarm.api.expression.NameSelector;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.expression.Segment;
import com.navercorp.objectfarm.api.expression.Selector;
import com.navercorp.objectfarm.api.expression.ValueSelector;
import com.navercorp.objectfarm.api.node.JvmMapEntryNode;
import com.navercorp.objectfarm.api.node.JvmMapNode;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;

/**
 * Represents a tree structure of JvmNode instances transformed from a JvmNodeCandidateTree.
 * <p>
 * This class maintains:
 * <ul>
 *   <li>Parent-child relationships between JvmNode objects</li>
 *   <li>Mapping between JvmNode and JvmNodeCandidate (1:N supported)</li>
 *   <li>Complete list of all nodes in the tree</li>
 * </ul>
 * <p>
 * The mapping supports flexible transformations:
 * <ul>
 *   <li>1:1 - One candidate produces one node</li>
 *   <li>1:N - One candidate produces multiple nodes (e.g., Map entry → key + value)</li>
 *   <li>1:0 - One candidate produces no nodes (filtered out)</li>
 * </ul>
 */
public final class JvmNodeTree {
	private final JvmNode rootNode;
	private final Map<JvmNode, List<JvmNode>> parentChildMap;
	private final List<JvmNode> allNodes;

	// Mapping: Node → Candidate (N:1, each node has exactly one source candidate)
	private final Map<JvmNode, JvmNodeCandidate> nodeToCandidate;
	// Mapping: Candidate → Nodes (1:N, one candidate can produce multiple nodes)
	private final Map<JvmNodeCandidate, List<JvmNode>> candidateToNodes;

	// Lazy-initialized path mappings
	private volatile Map<JvmNode, PathExpression> nodePaths;
	private volatile Map<JvmNode, JvmNode> childToParent;

	/**
	 * Creates a new JvmNodeTree with the specified root node and mappings.
	 *
	 * @param rootNode the root node of the tree
	 * @param parentChildMap the parent-child relationship map
	 * @param allNodes all nodes in the tree
	 * @param nodeToCandidate mapping from node to source candidate
	 * @param candidateToNodes mapping from candidate to promoted nodes (1:N)
	 */
	JvmNodeTree(
		JvmNode rootNode,
		Map<JvmNode, List<JvmNode>> parentChildMap,
		List<JvmNode> allNodes,
		Map<JvmNode, JvmNodeCandidate> nodeToCandidate,
		Map<JvmNodeCandidate, List<JvmNode>> candidateToNodes
	) {
		this.rootNode = rootNode;
		this.parentChildMap = new HashMap<>(parentChildMap);
		this.allNodes = new ArrayList<>(allNodes);
		this.nodeToCandidate = new HashMap<>(nodeToCandidate);
		this.candidateToNodes = new HashMap<>();
		// Deep copy the lists
		candidateToNodes.forEach((k, v) -> this.candidateToNodes.put(k, new ArrayList<>(v)));
	}

	/**
	 * Returns the root node of the tree.
	 *
	 * @return the root JvmNode
	 */
	public JvmNode getRootNode() {
		return rootNode;
	}

	/**
	 * Gets the children of the specified parent node.
	 * Returns an empty list if no children exist.
	 *
	 * @param parent the parent node
	 * @return list of child nodes, never null
	 */
	public List<JvmNode> getChildren(JvmNode parent) {
		List<JvmNode> children = parentChildMap.get(parent);
		return children != null ? Collections.unmodifiableList(children) : Collections.emptyList();
	}

	/**
	 * Returns all nodes in the tree.
	 *
	 * @return unmodifiable list of all nodes
	 */
	public List<JvmNode> getAllNodes() {
		return Collections.unmodifiableList(allNodes);
	}

	/**
	 * Checks if the tree contains the specified node.
	 *
	 * @param node the node to check
	 * @return true if the node exists in the tree
	 */
	public boolean contains(JvmNode node) {
		return allNodes.contains(node);
	}

	/**
	 * Returns the number of nodes in the tree.
	 *
	 * @return total number of nodes
	 */
	public int size() {
		return allNodes.size();
	}

	/**
	 * Gets the source candidate that was promoted to create the given node.
	 * This provides reverse mapping from JvmNode back to JvmNodeCandidate.
	 *
	 * @param node the promoted node
	 * @return the source JvmNodeCandidate, or null if not found
	 */
	@Nullable
	public JvmNodeCandidate getSourceCandidate(JvmNode node) {
		return nodeToCandidate.get(node);
	}

	/**
	 * Gets all promoted nodes that were created from the given candidate.
	 * This provides forward mapping from JvmNodeCandidate to JvmNodes (1:N).
	 * <p>
	 * A single candidate may produce:
	 * <ul>
	 *   <li>One node (1:1 mapping)</li>
	 *   <li>Multiple nodes (1:N mapping, e.g., Map entry → key + value)</li>
	 *   <li>No nodes (1:0 mapping, filtered out)</li>
	 * </ul>
	 *
	 * @param candidate the source candidate
	 * @return list of promoted JvmNodes, or empty list if none
	 */
	public List<JvmNode> getPromotedNodes(JvmNodeCandidate candidate) {
		List<JvmNode> nodes = candidateToNodes.get(candidate);
		return nodes != null ? Collections.unmodifiableList(nodes) : Collections.emptyList();
	}

	/**
	 * Gets the first promoted node that was created from the given candidate.
	 * Convenience method for 1:1 mapping cases.
	 *
	 * @param candidate the source candidate
	 * @return the first promoted JvmNode, or null if none
	 */
	@Nullable
	public JvmNode getPromotedNode(JvmNodeCandidate candidate) {
		List<JvmNode> nodes = candidateToNodes.get(candidate);
		return (nodes != null && !nodes.isEmpty()) ? nodes.get(0) : null;
	}

	/**
	 * Resolves a PathExpression to a JvmNode in this tree.
	 * <p>
	 * Traverses the tree following the path segments to find the target node.
	 * Supports name selectors, index selectors, key selectors, and value selectors.
	 * <p>
	 * Example:
	 * <pre>
	 * JvmNode nameNode = tree.resolve(PathExpression.of("$.name"));
	 * JvmNode firstItem = tree.resolve(PathExpression.of("$.items[0]"));
	 * JvmNode mapKey = tree.resolve(PathExpression.of("$.map[0][key]"));
	 * </pre>
	 *
	 * @param path the path expression to resolve
	 * @return the JvmNode at the path, or null if the path cannot be resolved
	 */
	@Nullable
	public JvmNode resolve(PathExpression path) {
		if (path.isRoot()) {
			return rootNode;
		}

		JvmNode current = rootNode;
		for (Segment segment : path.getSegments()) {
			if (current == null) {
				return null;
			}

			Selector selector = segment.getFirstSelector();
			current = resolveSelector(current, selector);
		}

		return current;
	}

	/**
	 * Resolves a path expression string to a JvmNode.
	 * <p>
	 * Convenience method that parses the path string and resolves it.
	 *
	 * @param pathExpression the path expression string (e.g., "$.name", "$.items[0]")
	 * @return the JvmNode at the path, or null if the path cannot be resolved
	 */
	@Nullable
	public JvmNode resolve(String pathExpression) {
		return resolve(PathExpression.of(pathExpression));
	}

	/**
	 * Gets the path expression for a node in this tree.
	 * <p>
	 * This provides reverse mapping from JvmNode to its PathExpression.
	 * The path is computed lazily and cached for subsequent calls.
	 *
	 * @param node the node to get the path for
	 * @return the PathExpression for the node, or null if the node is not in this tree
	 */
	@Nullable
	public PathExpression getPath(JvmNode node) {
		ensurePathMappingsInitialized();
		return nodePaths.get(node);
	}

	/**
	 * Gets the parent of the specified node.
	 *
	 * @param node the node to get the parent for
	 * @return the parent node, or null if the node is the root or not in this tree
	 */
	@Nullable
	public JvmNode getParent(JvmNode node) {
		ensurePathMappingsInitialized();
		return childToParent.get(node);
	}

	private void ensurePathMappingsInitialized() {
		if (nodePaths == null) {
			synchronized (this) {
				if (nodePaths == null) {
					buildPathMappings();
				}
			}
		}
	}

	private void buildPathMappings() {
		Map<JvmNode, PathExpression> paths = new HashMap<>();
		Map<JvmNode, JvmNode> parents = new HashMap<>();

		// Root node has root path
		paths.put(rootNode, PathExpression.root());

		// Build paths by traversing the tree
		buildPathsRecursive(rootNode, PathExpression.root(), paths, parents);

		this.nodePaths = paths;
		this.childToParent = parents;
	}

	private void buildPathsRecursive(
		JvmNode parent,
		PathExpression parentPath,
		Map<JvmNode, PathExpression> paths,
		Map<JvmNode, JvmNode> parents
	) {
		List<JvmNode> children = parentChildMap.get(parent);
		if (children == null || children.isEmpty()) {
			return;
		}

		// Handle JvmMapNode specially - children are key and value nodes
		if (parent instanceof JvmMapNode) {
			JvmMapNode mapNode = (JvmMapNode) parent;
			JvmNode keyNode = mapNode.getKeyNode();
			JvmNode valueNode = mapNode.getValueNode();

			if (keyNode != null && children.contains(keyNode)) {
				PathExpression keyPath = parentPath.key();
				paths.put(keyNode, keyPath);
				parents.put(keyNode, parent);
				buildPathsRecursive(keyNode, keyPath, paths, parents);
			}

			if (valueNode != null && children.contains(valueNode)) {
				PathExpression valuePath = parentPath.value();
				paths.put(valueNode, valuePath);
				parents.put(valueNode, parent);
				buildPathsRecursive(valueNode, valuePath, paths, parents);
			}
			return;
		}

		// Handle JvmMapEntryNode specially - standalone Map.Entry with key and value nodes
		// Use children directly (resolved nodes from expandMapKeyValue), not mapEntryNode.getKeyNode()/getValueNode()
		// because resolveNodeType may have returned different node instances
		if (parent instanceof JvmMapEntryNode) {
			// By convention, children[0] is key node, children[1] is value node
			if (children.size() >= 1) {
				JvmNode keyNode = children.get(0);
				PathExpression keyPath = parentPath.key();
				paths.put(keyNode, keyPath);
				parents.put(keyNode, parent);
				buildPathsRecursive(keyNode, keyPath, paths, parents);
			}

			if (children.size() >= 2) {
				JvmNode valueNode = children.get(1);
				PathExpression valuePath = parentPath.value();
				paths.put(valueNode, valuePath);
				parents.put(valueNode, parent);
				buildPathsRecursive(valueNode, valuePath, paths, parents);
			}
			return;
		}

		// Regular children - use name or index
		for (JvmNode child : children) {
			PathExpression childPath = createChildPathExpression(parentPath, child);
			paths.put(child, childPath);
			parents.put(child, parent);
			buildPathsRecursive(child, childPath, paths, parents);
		}
	}

	private PathExpression createChildPathExpression(PathExpression parentPath, JvmNode child) {
		Integer index = child.getIndex();
		String name = child.getNodeName();

		if (index != null) {
			return parentPath.index(index);
		} else if (name != null) {
			return parentPath.child(name);
		}
		// Fallback - shouldn't happen in normal usage
		return parentPath;
	}

	@Nullable
	private JvmNode resolveSelector(JvmNode current, Selector selector) {
		if (selector instanceof NameSelector) {
			return findChildByName(current, ((NameSelector) selector).getName());
		} else if (selector instanceof IndexSelector) {
			return findChildByIndex(current, ((IndexSelector) selector).getIndex());
		} else if (selector instanceof KeySelector) {
			if (current instanceof JvmMapNode) {
				return ((JvmMapNode) current).getKeyNode();
			}
			if (current instanceof JvmMapEntryNode) {
				return ((JvmMapEntryNode) current).getKeyNode();
			}
			return null;
		} else if (selector instanceof ValueSelector) {
			if (current instanceof JvmMapNode) {
				return ((JvmMapNode) current).getValueNode();
			}
			if (current instanceof JvmMapEntryNode) {
				return ((JvmMapEntryNode) current).getValueNode();
			}
			return null;
		}
		return null;
	}

	@Nullable
	private JvmNode findChildByName(JvmNode parent, String name) {
		List<JvmNode> children = parentChildMap.get(parent);
		if (children == null) {
			return null;
		}
		for (JvmNode child : children) {
			if (name.equals(child.getNodeName())) {
				return child;
			}
		}
		return null;
	}


	/**
	 * Diagnostic information about why a path resolution failed.
	 */
	public static final class ResolutionDiagnostic {

		private final String failedSegment;
		private final String resolvedPrefix;
		private final String reason;
		private final List<String> availableNames;

		public ResolutionDiagnostic(
			String failedSegment,
			String resolvedPrefix,
			String reason,
			List<String> availableNames
		) {
			this.failedSegment = failedSegment;
			this.resolvedPrefix = resolvedPrefix;
			this.reason = reason;
			this.availableNames = Collections.unmodifiableList(new ArrayList<>(availableNames));
		}

		public String getFailedSegment() {
			return failedSegment;
		}

		public String getResolvedPrefix() {
			return resolvedPrefix;
		}

		public String getReason() {
			return reason;
		}

		public List<String> getAvailableNames() {
			return availableNames;
		}
	}

	/**
	 * Diagnoses why a path resolution failed.
	 * <p>
	 * This method traverses the path like {@link #resolve(PathExpression)} but when
	 * resolution fails, it returns diagnostic information about the failure point.
	 *
	 * @param path the path expression that failed to resolve
	 * @return diagnostic information, or null if the path actually resolves successfully
	 */
	@Nullable
	public ResolutionDiagnostic diagnose(PathExpression path) {
		if (path.isRoot()) {
			return null; // root always resolves
		}

		JvmNode current = rootNode;
		StringBuilder resolvedPrefix = new StringBuilder("$");

		for (Segment segment : path.getSegments()) {
			if (current == null) {
				return new ResolutionDiagnostic(
					segment.toExpression(),
					resolvedPrefix.toString(),
					"NO_PARENT_NODE",
					Collections.emptyList()
				);
			}

			Selector selector = segment.getFirstSelector();
			JvmNode next = resolveSelector(current, selector);

			if (next == null) {
				// Collect available children names for diagnostic
				List<String> availableNames = new ArrayList<>();
				List<JvmNode> children = parentChildMap.get(current);
				if (children != null) {
					for (JvmNode child : children) {
						String name = child.getNodeName();
						if (name != null) {
							availableNames.add(name);
						} else if (child.getIndex() != null) {
							availableNames.add("[" + child.getIndex() + "]");
						}
					}
				}
				Collections.sort(availableNames);

				String reason;
				if (selector instanceof NameSelector) {
					reason = children == null || children.isEmpty() ? "NO_CHILDREN" : "FIELD_NOT_FOUND";
				} else if (selector instanceof IndexSelector) {
					reason = "INDEX_OUT_OF_BOUNDS";
				} else {
					reason = "SELECTOR_NOT_MATCHED";
				}

				return new ResolutionDiagnostic(
					segment.toExpression(),
					resolvedPrefix.toString(),
					reason,
					availableNames
				);
			}

			current = next;
			resolvedPrefix.append(segment.toExpression());
		}

		return null; // path resolved successfully
	}

	@Nullable
	private JvmNode findChildByIndex(JvmNode parent, int index) {
		List<JvmNode> children = parentChildMap.get(parent);
		if (children == null) {
			return null;
		}
		for (JvmNode child : children) {
			Integer childIndex = child.getIndex();
			if (childIndex != null && childIndex == index) {
				return child;
			}
		}
		return null;
	}
}
