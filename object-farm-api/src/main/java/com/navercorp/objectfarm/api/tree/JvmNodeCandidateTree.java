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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.node.GenericTypeResolver;
import com.navercorp.objectfarm.api.node.InterfaceResolver;
import com.navercorp.objectfarm.api.node.JvmNodeContext;
import com.navercorp.objectfarm.api.nodecandidate.JvmMapEntryNodeCandidate;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidateGenerator;
import com.navercorp.objectfarm.api.tree.JvmNodeCandidateTreeContext.SubtreeSnapshot;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.Types;

/**
 * Represents a deterministic tree structure of JvmNodeCandidate instances.
 * <p>
 * This class maintains parent-child relationships between JvmNodeCandidate objects
 * using a Map structure where each parent candidate maps to a list of its children.
 * The tree structure is purely type-based and deterministic - the same type will
 * always produce the same tree structure.
 * <p>
 * Key principles:
 * <ul>
 *   <li>Deterministic: Same type always produces the same tree</li>
 *   <li>Type-based: Tree structure is determined only by types, not customizers</li>
 *   <li>Container types are leaf nodes: Container elements are generated at JvmNode level</li>
 * </ul>
 * <p>
 * Use the {@link Builder} to create trees:
 * <pre>{@code
 * JvmNodeCandidateTree tree = new JvmNodeCandidateTree.Builder(rootType, context)
 *     .withTreeContext(treeContext)
 *     .build();
 * }</pre>
 */
public final class JvmNodeCandidateTree {
	private static final int DEFAULT_MAX_DEPTH = 10;

	private final JvmNodeCandidate rootNode;
	private final JvmNodeContext jvmNodeContext;
	private final JvmNodeCandidateTreeContext treeContext;
	private final boolean preBuildResolvedTypes;
	private final boolean skipAbstractLeafCheck;
	private final Map<JvmNodeCandidate, List<JvmNodeCandidate>> parentChildMap;
	private final List<JvmNodeCandidate> allCandidates;
	private final Map<JvmType, List<JvmType>> abstractTypeImplementations;

	/**
	 * Internal constructor for creating JvmNodeCandidateTree instances.
	 * This constructor is private to enforce the use of Builder pattern.
	 * Use {@link Builder} to create instances.
	 *
	 * @param rootNode the root node of the tree, can be null
	 * @param jvmNodeContext the context containing node generators
	 * @param treeContext the tree context for caching subtree information, can be null
	 * @param initialAncestors the initial ancestor types for cycle detection
	 * @param preBuildResolvedTypes whether to pre-build candidate trees for resolved types
	 */
	private JvmNodeCandidateTree(
		JvmNodeCandidate rootNode,
		JvmNodeContext jvmNodeContext,
		JvmNodeCandidateTreeContext treeContext,
		Set<Class<?>> initialAncestors,
		boolean preBuildResolvedTypes,
		boolean skipAbstractLeafCheck
	) {
		this.rootNode = rootNode;
		this.jvmNodeContext = jvmNodeContext;
		this.treeContext = treeContext;
		this.preBuildResolvedTypes = preBuildResolvedTypes;
		this.skipAbstractLeafCheck = skipAbstractLeafCheck;
		this.parentChildMap = new HashMap<>();
		this.allCandidates = new ArrayList<>();
		this.abstractTypeImplementations = new ConcurrentHashMap<>();

		// Build the initial tree structure with ancestor tracking for circular reference detection
		buildTree(rootNode, 0, DEFAULT_MAX_DEPTH, initialAncestors);
	}

	/**
	 * Returns the root node of the tree.
	 *
	 * @return the root node candidate
	 */
	public JvmNodeCandidate getRootNode() {
		return rootNode;
	}

	/**
	 * Gets the children of the specified parent candidate.
	 * Returns the existing children list or an empty list if no children exist.
	 *
	 * @param parent the parent candidate
	 * @return list of child candidates, never null
	 */
	public List<JvmNodeCandidate> getChildren(JvmNodeCandidate parent) {
		return parentChildMap.computeIfAbsent(parent, k -> new ArrayList<>());
	}

	/**
	 * Returns all candidates in the tree.
	 *
	 * @return list of all candidates
	 */
	List<JvmNodeCandidate> getAllCandidates() {
		return new ArrayList<>(allCandidates);
	}

	/**
	 * Checks if the tree contains the specified candidate.
	 *
	 * @param candidate the candidate to check
	 * @return true if the candidate exists in the tree
	 */
	public boolean contains(JvmNodeCandidate candidate) {
		return allCandidates.contains(candidate);
	}

	/**
	 * Returns the number of candidates in the tree.
	 *
	 * @return total number of candidates
	 */
	public int size() {
		return allCandidates.size();
	}

	/**
	 * Builds the tree recursively starting from the given node.
	 * Uses the context's generators to create child nodes for each parent.
	 * If a tree context is available, tries to reuse cached subtree information.
	 * <p>
	 * Container types are treated as leaf nodes - their children (elements) are
	 * generated at the JvmNode level, not here.
	 * <p>
	 * Circular references are detected by tracking ancestor types. When a type
	 * that already exists in the ancestor chain is encountered, it becomes a
	 * leaf node to prevent infinite recursion.
	 * <p>
	 * When preBuildResolvedTypes is enabled, candidate trees for resolved types
	 * (Interface implementations, Generic resolutions, Container elements) are
	 * pre-built and cached for faster transformation.
	 * <p>
	 * Generators are evaluated in priority order (custom generators first, then object property generator).
	 * The first generator that produces results wins — subsequent generators are skipped.
	 *
	 * @param node the current node to build children for
	 * @param currentDepth the current depth in the tree
	 * @param maxDepth the maximum depth to build to prevent infinite recursion
	 * @param ancestorTypes set of types in the current path from root to this node
	 */
	private void buildTree(JvmNodeCandidate node, int currentDepth, int maxDepth, Set<Class<?>> ancestorTypes) {
		trackNode(node);

		// Stop recursion if we've reached the maximum depth
		if (currentDepth >= maxDepth) {
			return;
		}

		JvmType jvmType = node.getType();
		Class<?> rawType = jvmType.getRawType();

		// Container types are leaf nodes - their elements are generated at JvmNode level
		// Check this BEFORE circular reference detection to avoid false positives
		// (e.g., List<List<T>> should not trigger cycle detection)
		if (isContainerType(jvmType)) {
			// Pre-build element type trees if enabled
			if (preBuildResolvedTypes && treeContext != null) {
				// Don't add container type to ancestors - container elements are separate
				preBuildContainerElementTypes(jvmType, ancestorTypes);
			}
			return;
		}

		// Circular reference detection: if this type is already in ancestor chain, stop
		if (ancestorTypes.contains(rawType)) {
			return;
		}

		// Add current type to ancestors for children
		Set<Class<?>> childAncestors = new HashSet<>(ancestorTypes);
		childAncestors.add(rawType);

		// JvmMapEntryNodeCandidate is treated as a leaf - its key/value are expanded
		// during JvmNodeTree transformation by expandMapKeyValue, similar to containers.
		// Don't add key/value as children here to avoid duplicate node creation.
		if (node instanceof JvmMapEntryNodeCandidate) {
			return;
		}

		// Abstract/Interface types are leaf nodes - resolved at JvmNode level
		boolean isAbstractOrInterface = rawType.isInterface() || Modifier.isAbstract(rawType.getModifiers());

		if (isAbstractOrInterface && !skipAbstractLeafCheck) {
			// Pre-build implementation type tree if enabled
			if (preBuildResolvedTypes && treeContext != null) {
				preBuildAbstractImplementations(jvmType, childAncestors);
			}
			return;
		}

		// Leaf types (Java standard types, Kotlin standard types, etc.) are not expanded
		if (jvmNodeContext.isLeafType(jvmType)) {
			return;
		}

		// Try to use cached subtree if available
		if (treeContext != null && treeContext.isCached(jvmType)) {
			SubtreeSnapshot snapshot = treeContext.getCachedSubtree(jvmType);
			// Reuse cached subtree
			List<JvmNodeCandidate> cachedChildren = snapshot.getChildren();
			Map<JvmNodeCandidate, List<JvmNodeCandidate>> cachedParentChildMap = snapshot.getParentChildMap();

			List<JvmNodeCandidate> nodeChildren = getChildren(node);
			for (JvmNodeCandidate child : cachedChildren) {
				if (!nodeChildren.contains(child)) {
					nodeChildren.add(child);

					// Add all descendants from the cached subtree
					doCachedSubtree(child, cachedParentChildMap);
				}
			}
			return;
		}

		// Track the state before generating children for caching
		Map<JvmNodeCandidate, List<JvmNodeCandidate>> subtreeMap = new HashMap<>();
		List<JvmNodeCandidate> directChildren = new ArrayList<>();

		// Generate children using the first matching generator (first-match-wins).
		// Generators are ordered by priority: custom generators first, then object property generator.
		for (JvmNodeCandidateGenerator generator : jvmNodeContext.getCandidateNodeGenerators()) {
			if (!generator.isSupported(jvmType)) {
				continue;
			}

			List<JvmNodeCandidate> children = generator.generateNextNodeCandidates(jvmType);

			if (!children.isEmpty()) {
				// Add generated children as children of this node
				List<JvmNodeCandidate> nodeChildren = getChildren(node);
				for (JvmNodeCandidate child : children) {
					if (!nodeChildren.contains(child)) {
						nodeChildren.add(child);
						directChildren.add(child);

						// Recursively build tree for each child with updated ancestors
						buildTree(child, currentDepth + 1, maxDepth, childAncestors);
					}
				}
				break;
			}
		}

		// Cache the subtree if tree context is available and we have children
		if (treeContext != null && !directChildren.isEmpty()) {
			// Collect all nodes in the subtree
			collectSubtreeMap(node, subtreeMap, new HashSet<>());
			treeContext.cacheSubtree(jvmType, directChildren, subtreeMap);
		}
	}

	/**
	 * Adds a cached subtree to the current tree structure.
	 *
	 * @param node the root of the cached subtree
	 * @param cachedParentChildMap the parent-child map from the cache
	 */
	private void doCachedSubtree(
		JvmNodeCandidate node,
		Map<JvmNodeCandidate, List<JvmNodeCandidate>> cachedParentChildMap
	) {
		trackNode(node);

		// Add children from cache
		List<JvmNodeCandidate> cachedChildren = cachedParentChildMap.get(node);
		if (cachedChildren != null) {
			List<JvmNodeCandidate> nodeChildren = getChildren(node);
			for (JvmNodeCandidate child : cachedChildren) {
				if (!nodeChildren.contains(child)) {
					nodeChildren.add(child);
					// Recursively add cached children
					doCachedSubtree(child, cachedParentChildMap);
				}
			}
		}
	}

	/**
	 * Collects parent-child relationships in a subtree.
	 *
	 * @param node the root of the subtree
	 * @param subtreeMap the map to populate with relationships
	 * @param visited set of already visited nodes to prevent cycles
	 */
	private void collectSubtreeMap(
		JvmNodeCandidate node,
		Map<JvmNodeCandidate, List<JvmNodeCandidate>> subtreeMap,
		Set<JvmNodeCandidate> visited
	) {
		if (visited.contains(node)) {
			return;
		}
		visited.add(node);

		List<JvmNodeCandidate> children = parentChildMap.get(node);
		if (children != null && !children.isEmpty()) {
			subtreeMap.put(node, new ArrayList<>(children));
			for (JvmNodeCandidate child : children) {
				collectSubtreeMap(child, subtreeMap, visited);
			}
		}
	}

	/**
	 * Tracks a node by adding it to allCandidates.
	 *
	 * @param node the node to track
	 */
	private void trackNode(JvmNodeCandidate node) {
		if (!allCandidates.contains(node)) {
			allCandidates.add(node);
		}
	}

	/**
	 * Checks if the given type represents a container type (Collection, Map, or Array).
	 * Container types are treated as leaf nodes in JvmNodeCandidateTree because their
	 * elements are generated dynamically at the JvmNode level.
	 * <p>
	 * Only standard Java library collections (java.*, sun.*) are treated as containers.
	 * Custom classes that implement Collection or Map interfaces are treated as regular
	 * objects, allowing their declared fields to be processed normally.
	 *
	 * @param jvmType the type to check
	 * @return true if the type is a container type
	 */
	private boolean isContainerType(JvmType jvmType) {
		Class<?> rawType = jvmType.getRawType();
		if (rawType.isArray()) {
			return true;
		}
		// Only treat standard Java library collections as container types
		// Custom classes that implement Collection/Map should be treated as regular objects
		boolean isCollectionOrMap = Collection.class.isAssignableFrom(rawType) || Map.class.isAssignableFrom(rawType);
		return isCollectionOrMap && Types.isJavaType(rawType);
	}

	/**
	 * Pre-builds candidate trees for container element types.
	 * This caches the element type's tree structure for faster transformation.
	 *
	 * @param containerType the container type
	 * @param ancestorTypes the ancestor types for cycle detection
	 */
	private void preBuildContainerElementTypes(JvmType containerType, Set<Class<?>> ancestorTypes) {
		Class<?> rawType = containerType.getRawType();
		List<? extends JvmType> typeVars = containerType.getTypeVariables();

		if (rawType.isArray()) {
			// Array element type - try to preserve generic information
			JvmType componentJvmType = getArrayComponentType(containerType);
			if (componentJvmType != null && !Types.isJavaType(componentJvmType.getRawType())) {
				preBuildTypeTree(componentJvmType, ancestorTypes);
			}
		} else if (Map.class.isAssignableFrom(rawType)) {
			// Map key and value types
			if (typeVars.size() >= 2) {
				JvmType keyType = typeVars.get(0);
				JvmType valueType = typeVars.get(1);
				if (!Types.isJavaType(keyType.getRawType())) {
					preBuildTypeTree(keyType, ancestorTypes);
				}
				if (!Types.isJavaType(valueType.getRawType())) {
					preBuildTypeTree(valueType, ancestorTypes);
				}
			}
		} else if (Collection.class.isAssignableFrom(rawType)) {
			// Collection element type
			if (typeVars.size() >= 1) {
				JvmType elementType = typeVars.get(0);
				if (!Types.isJavaType(elementType.getRawType())) {
					preBuildTypeTree(elementType, ancestorTypes);
				}
			}
		}
	}

	/**
	 * Gets the component type of an array, preserving generic type information when available.
	 *
	 * @param arrayType the array type
	 * @return the component type with generic information preserved if available, or null if not found
	 */
	@Nullable
	private JvmType getArrayComponentType(JvmType arrayType) {
		JvmType componentType = arrayType.getComponentType();
		if (componentType != null) {
			return componentType;
		}
		// Fallback: use raw component type (loses generic info)
		Class<?> rawComponentType = arrayType.getRawType().getComponentType();
		return rawComponentType != null ? new com.navercorp.objectfarm.api.type.JavaType(rawComponentType) : null;
	}

	/**
	 * Pre-builds candidate tree for all abstract/interface implementation types.
	 * This caches all concrete implementation types' tree structures for faster transformation.
	 *
	 * @param abstractType the abstract class or interface type
	 * @param ancestorTypes the ancestor types for cycle detection
	 */
	private void preBuildAbstractImplementations(JvmType abstractType, Set<Class<?>> ancestorTypes) {
		InterfaceResolver resolver = jvmNodeContext.getInterfaceResolver();
		if (resolver == null) {
			return;
		}

		List<JvmType> allCandidates = resolver.resolveAll(abstractType);
		List<JvmType> concreteImplementations = new ArrayList<>();

		for (JvmType candidateType : allCandidates) {
			if (candidateType != null && !candidateType.equals(abstractType)) {
				concreteImplementations.add(candidateType);
				preBuildTypeTree(candidateType, ancestorTypes);
			}
		}

		if (!concreteImplementations.isEmpty()) {
			abstractTypeImplementations.put(abstractType, concreteImplementations);
		}
	}

	/**
	 * Returns the list of concrete implementations for an abstract type.
	 *
	 * @param abstractType the abstract class or interface type
	 * @return list of concrete implementation types, empty list if none found
	 */
	public List<JvmType> getImplementations(JvmType abstractType) {
		return abstractTypeImplementations.getOrDefault(abstractType, Collections.emptyList());
	}

	/**
	 * Pre-builds candidate tree for generic resolved type.
	 * This caches the resolved type's tree structure for faster transformation.
	 *
	 * @param genericType the generic type
	 * @param ancestorTypes the ancestor types for cycle detection
	 */
	private void preBuildGenericResolution(JvmType genericType, Set<Class<?>> ancestorTypes) {
		GenericTypeResolver resolver = jvmNodeContext.getGenericTypeResolver();
		if (resolver == null) {
			return;
		}

		JvmType resolvedType = resolver.resolve(genericType);
		if (resolvedType != null && !resolvedType.equals(genericType)) {
			preBuildTypeTree(resolvedType, ancestorTypes);
		}
	}

	/**
	 * Pre-builds and caches a candidate tree for the given type.
	 * The tree is built using a new Builder with the same context and tree context.
	 * The tree will be automatically cached during buildTree.
	 *
	 * @param type the type to pre-build tree for
	 * @param ancestorTypes the ancestor types for cycle detection
	 */
	private void preBuildTypeTree(JvmType type, Set<Class<?>> ancestorTypes) {
		// Skip if already cached
		if (treeContext.isCached(type)) {
			return;
		}

		// Skip if would cause cycle
		if (ancestorTypes.contains(type.getRawType())) {
			return;
		}

		// Build the tree - it will be automatically cached by buildTree
		new Builder(type, jvmNodeContext)
			.withTreeContext(treeContext)
			.withAncestorTypes(ancestorTypes)
			.withPreBuildResolvedTypes(true)
			.build();
	}

	/**
	 * Builder for creating JvmNodeCandidateTree instances.
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * JvmNodeCandidateTree tree = new JvmNodeCandidateTree.Builder(rootType, context)
	 *     .withTreeContext(treeContext)
	 *     .withAncestorTypes(ancestorTypes)
	 *     .build();
	 * }</pre>
	 */
	public static class Builder {

		private final JvmNodeCandidate rootNode;
		private final JvmNodeContext jvmNodeContext;
		private JvmNodeCandidateTreeContext treeContext;
		private Set<Class<?>> ancestorTypes;
		private boolean preBuildResolvedTypes = false;
		private boolean skipAbstractLeafCheck = false;

		/**
		 * Creates a new builder for JvmNodeCandidateTree with a root node.
		 *
		 * @param rootNode the root node of the tree
		 * @param jvmNodeContext the context containing node generators
		 */
		public Builder(JvmNodeCandidate rootNode, JvmNodeContext jvmNodeContext) {
			this.rootNode = rootNode;
			this.jvmNodeContext = jvmNodeContext;
		}

		/**
		 * Creates a new builder for JvmNodeCandidateTree with a JVM type.
		 * A candidate is automatically created from the type using
		 * {@link JvmNodeContext#createRootCandidate(JvmType)}.
		 *
		 * @param rootType the type of the root node
		 * @param jvmNodeContext the context containing node generators
		 */
		public Builder(JvmType rootType, JvmNodeContext jvmNodeContext) {
			this.rootNode = jvmNodeContext.createRootCandidate(rootType);
			this.jvmNodeContext = jvmNodeContext;
		}

		/**
		 * Sets the tree context for caching subtree information.
		 *
		 * @param treeContext the tree context
		 * @return this builder
		 */
		public Builder withTreeContext(JvmNodeCandidateTreeContext treeContext) {
			this.treeContext = treeContext;
			return this;
		}

		/**
		 * Sets the ancestor types for cycle detection.
		 * <p>
		 * When building a subtree for container elements, the ancestor types from
		 * the parent tree should be passed to ensure consistent cycle detection.
		 *
		 * @param ancestorTypes the set of ancestor types
		 * @return this builder
		 */
		public Builder withAncestorTypes(Set<Class<?>> ancestorTypes) {
			this.ancestorTypes = ancestorTypes;
			return this;
		}

		/**
		 * Enables or disables pre-building of candidate trees for resolved types.
		 * <p>
		 * When enabled, the builder will proactively build and cache candidate trees
		 * for types that will be resolved at transformation time:
		 * <ul>
		 *   <li>Container element types (List/Set elements, Map keys/values, Array components)</li>
		 *   <li>Interface implementation types (resolved via InterfaceResolver)</li>
		 *   <li>Generic resolved types (resolved via GenericTypeResolver)</li>
		 * </ul>
		 * <p>
		 * This can improve transformation performance by avoiding on-demand tree building,
		 * but requires resolvers to be configured in the context.
		 * <p>
		 * Requires a tree context to be set via {@link #withTreeContext(JvmNodeCandidateTreeContext)}.
		 *
		 * @param preBuildResolvedTypes true to enable pre-building, false to disable (default)
		 * @return this builder
		 */
		public Builder withPreBuildResolvedTypes(boolean preBuildResolvedTypes) {
			this.preBuildResolvedTypes = preBuildResolvedTypes;
			return this;
		}

		/**
		 * Skips the abstract/interface leaf check during tree building.
		 * When enabled, abstract and interface types are expanded using generators
		 * instead of being treated as leaf nodes.
		 *
		 * @param skipAbstractLeafCheck true to skip the check
		 * @return this builder
		 */
		public Builder withSkipAbstractLeafCheck(boolean skipAbstractLeafCheck) {
			this.skipAbstractLeafCheck = skipAbstractLeafCheck;
			return this;
		}

		/**
		 * Builds the tree.
		 *
		 * @return the constructed tree
		 */
		public JvmNodeCandidateTree build() {
			Set<Class<?>> initialAncestors = ancestorTypes != null ? new HashSet<>(ancestorTypes) : new HashSet<>();
			return new JvmNodeCandidateTree(
				rootNode,
				jvmNodeContext,
				treeContext,
				initialAncestors,
				preBuildResolvedTypes,
				skipAbstractLeafCheck
			);
		}
	}
}
