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

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.GenericTypeResolver;
import com.navercorp.objectfarm.api.node.InterfaceResolver;
import com.navercorp.objectfarm.api.node.JavaMapEntryNode;
import com.navercorp.objectfarm.api.node.JavaMapNode;
import com.navercorp.objectfarm.api.node.JavaNode;
import com.navercorp.objectfarm.api.node.JvmContainerNodeGenerator;
import com.navercorp.objectfarm.api.node.JvmMapEntryNode;
import com.navercorp.objectfarm.api.node.JvmMapNode;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.node.JvmNodeContext;
import com.navercorp.objectfarm.api.node.JvmNodePromoter;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Transforms a JvmNodeCandidateTree into a JvmNodeTree.
 * <p>
 * The transformer traverses the candidate tree and promotes each candidate to JvmNode(s).
 * During transformation, it handles:
 * <ul>
 *   <li>Type resolution (Interface → Implementation, Generic → Resolved)</li>
 *   <li>Container element generation (List/Map/Array elements)</li>
 *   <li>1:N mapping (e.g., Map entry → key + value nodes)</li>
 *   <li>Path-based customization via {@link PathResolverContext}</li>
 * </ul>
 * <p>
 * Path-based resolution allows fine-grained control over container sizes,
 * interface resolution, and generic type resolution at specific paths.
 * <p>
 * Example usage with path-based resolvers:
 * <pre>
 * PathResolverContext resolvers = PathResolverContext.builder()
 *     .addContainerSizeResolver("$.items", 3)      // outer List size
 *     .addContainerSizeResolver("$.items[*]", 5)   // inner List size
 *     .build();
 *
 * JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(
 *     context, treeContext, resolvers
 * );
 * JvmNodeTree tree = transformer.transform(candidateTree);
 * </pre>
 */
public final class JvmNodeTreeTransformer {
	private final JvmNodeContext context;
	private final JvmNodeCandidateTreeContext treeContext;
	private final PathResolverContext resolverContext;
	private final @Nullable ExpansionContext expansionContext;
	private final @Nullable JvmNodeSubtreeContext subtreeContext;

	public JvmNodeTreeTransformer(JvmNodeContext context) {
		this(context, new JvmNodeCandidateTreeContext(), PathResolverContext.empty(), null, null);
	}

	public JvmNodeTreeTransformer(
		JvmNodeContext context,
		JvmNodeCandidateTreeContext treeContext,
		PathResolverContext resolverContext
	) {
		this(context, treeContext, resolverContext, null, null);
	}

	public JvmNodeTreeTransformer(
		JvmNodeContext context,
		JvmNodeCandidateTreeContext treeContext,
		PathResolverContext resolverContext,
		@Nullable ExpansionContext expansionContext,
		@Nullable JvmNodeSubtreeContext subtreeContext
	) {
		this.context = context;
		this.treeContext = treeContext;
		this.resolverContext = resolverContext;
		this.expansionContext = expansionContext;
		this.subtreeContext = subtreeContext;
	}

	/**
	 * Transforms a JvmNodeCandidateTree into a JvmNodeTree.
	 */
	public JvmNodeTree transform(JvmNodeCandidateTree candidateTree) {
		TransformContext ctx = new TransformContext();
		PathExpression rootPath = PathExpression.root();

		// Transform root
		JvmNodeCandidate rootCandidate = candidateTree.getRootNode();
		List<JvmNode> rootNodes = promoteCandidate(rootCandidate, ctx);

		if (rootNodes.isEmpty()) {
			throw new IllegalStateException("Root candidate must produce at least one node");
		}

		JvmNode rootNode = rootNodes.get(0);
		ctx.allNodes.addAll(rootNodes);

		// Transform children
		transformFromCandidateTree(rootCandidate, rootNode, candidateTree, ctx, new HashSet<>(), rootPath);

		return new JvmNodeTree(rootNode, ctx.parentChildMap, ctx.allNodes, ctx.nodeToCandidate, ctx.candidateToNodes);
	}

	/**
	 * Transforms children following an existing CandidateTree structure.
	 */
	private void transformFromCandidateTree(
		JvmNodeCandidate parentCandidate,
		JvmNode parentNode,
		JvmNodeCandidateTree candidateTree,
		TransformContext ctx,
		Set<Class<?>> ancestors,
		PathExpression currentPath
	) {
		List<JvmNodeCandidate> childCandidates = candidateTree.getChildren(parentCandidate);

		// Leaf node in CandidateTree - may need dynamic expansion
		if (childCandidates.isEmpty()) {
			expandChildren(parentNode, ctx, ancestors, currentPath);
			return;
		}

		List<JvmNode> childNodes = new ArrayList<>();

		for (JvmNodeCandidate childCandidate : childCandidates) {
			// Skip if already promoted
			List<JvmNode> existing = ctx.candidateToNodes.get(childCandidate);
			if (existing != null) {
				childNodes.addAll(existing);
				continue;
			}

			childNodes.addAll(
				promoteAndExpandCandidates(childCandidate, parentNode, candidateTree, ctx, ancestors, currentPath)
			);
		}

		if (!childNodes.isEmpty()) {
			ctx.parentChildMap.put(parentNode, childNodes);
		}
	}

	/**
	 * Creates the path for a child node based on the candidate's name or index.
	 */
	private PathExpression createChildPath(PathExpression parentPath, JvmNodeCandidate candidate, JvmNode childNode) {
		String name = candidate.getName();
		Integer index = childNode.getIndex();

		if (index != null) {
			return parentPath.index(index);
		} else if (name != null) {
			return parentPath.child(name);
		}
		return parentPath;
	}

	/**
	 * Dispatches child expansion based on node type category.
	 * <p>
	 * Type categories (checked in this order):
	 * <ol>
	 *   <li><b>Map key/value nodes</b> (JvmMapNode, JvmMapEntryNode) — expands to key/value children</li>
	 *   <li><b>Container types</b> (List, Set, Map, Array, custom) — generates elements with size resolution.
	 *       Checked BEFORE interface/abstract because container interfaces (List, Set, Map) need
	 *       concrete implementation resolution (e.g., List → ArrayList).
	 *       Also checked BEFORE leaf types because containers are Java standard types</li>
	 *   <li><b>Interface/Abstract types</b> (non-container) — NOT expanded here; deferred to assembly time
	 *       so that each sample() call can independently select a different implementation</li>
	 *   <li><b>Cycle detection</b> — prevents infinite recursion before object expansion</li>
	 *   <li><b>Leaf types</b> — determined by {@link JvmNodeContext#isLeafType}, no children to expand</li>
	 *   <li><b>Regular objects</b> (POJO) — expanded via CandidateTree</li>
	 * </ol>
	 */
	private void expandChildren(
		JvmNode node,
		TransformContext ctx,
		Set<Class<?>> ancestors,
		PathExpression currentPath
	) {
		// Map key/value nodes — generated by container expansion or promoted from Map.Entry fields
		if (node instanceof JvmMapNode) {
			JvmMapNode mapNode = (JvmMapNode)node;
			expandMapKeyValue(mapNode, mapNode.getKeyNode(), mapNode.getValueNode(), ctx, ancestors, currentPath);
			return;
		}
		if (node instanceof JvmMapEntryNode) {
			JvmMapEntryNode entryNode = (JvmMapEntryNode)node;
			expandMapKeyValue(entryNode, entryNode.getKeyNode(), entryNode.getValueNode(), ctx, ancestors, currentPath);
			return;
		}

		JvmType nodeType = node.getConcreteType();
		Class<?> rawType = nodeType.getRawType();

		// Container types (List, Set, Map, Array, custom)
		// Must be checked BEFORE interface/abstract AND leaf, because containers like
		// java.util.List are both interfaces and Java standard types
		if (context.isContainerType(nodeType)) {
			expandContainerElements(node, ctx, ancestors, currentPath);
			return;
		}

		// Interface/Abstract types (non-container) — deferred to assembly time
		// for per-sample implementation selection
		if (Modifier.isInterface(rawType.getModifiers()) || Modifier.isAbstract(rawType.getModifiers())) {
			return;
		}

		// Cycle detection — prevent infinite recursion before object expansion
		if (ancestors.contains(rawType)) {
			if (expansionContext == null || !expansionContext.shouldExpandPath(currentPath, rawType, ancestors)) {
				return;
			}
		}

		// Leaf types — no children to expand
		if (context.isLeafType(nodeType)) {
			return;
		}

		// Regular objects (POJO)
		expandObjectChildren(node, ctx, ancestors, currentPath);
	}

	/**
	 * Expands a map-like node (JvmMapNode or JvmMapEntryNode) to key and value children.
	 * <p>
	 * Map containers generate JvmMapNode elements, while standalone Map.Entry fields
	 * are promoted as JvmMapEntryNode. Both have the same key/value expansion logic.
	 */
	private void expandMapKeyValue(
		JvmNode mapNode,
		JvmNode keyNode,
		JvmNode valueNode,
		TransformContext ctx,
		Set<Class<?>> ancestors,
		PathExpression currentPath
	) {
		PathExpression keyPath = currentPath.key();
		PathExpression valuePath = currentPath.value();

		JvmNode resolvedKey = resolveNodeType(keyNode, keyPath);
		JvmNode resolvedValue = resolveNodeType(valueNode, valuePath);

		List<JvmNode> children = new ArrayList<>();
		children.add(resolvedKey);
		children.add(resolvedValue);

		ctx.allNodes.addAll(children);
		ctx.parentChildMap.put(mapNode, children);

		expandChildren(resolvedKey, ctx, ancestors, keyPath);
		expandChildren(resolvedValue, ctx, ancestors, valuePath);
	}

	/**
	 * Generates and expands container elements.
	 * <p>
	 * After generating elements, applies path-based type resolution
	 * and recursively expands each element's children.
	 */
	private void expandContainerElements(
		JvmNode containerNode,
		TransformContext ctx,
		Set<Class<?>> ancestors,
		PathExpression currentPath
	) {
		SizeResolution sizeResolution = resolveContainerSize(containerNode, ctx, currentPath);
		List<JvmNode> elements = generateContainerElements(containerNode, sizeResolution.resolver);

		resolverContext
			.getResolutionListener()
			.onContainerSizeResolved(
				currentPath.toString(),
				containerNode.getConcreteType().getRawType().getSimpleName(),
				elements.size(),
				sizeResolution.source,
				sizeResolution.metadata
			);

		if (elements.isEmpty()) {
			return;
		}

		List<JvmNode> resolvedElements = resolveContainerElementTypes(elements, currentPath);
		ctx.allNodes.addAll(resolvedElements);
		ctx.parentChildMap.put(containerNode, resolvedElements);

		for (JvmNode element : resolvedElements) {
			ctx.nodeToParent.put(element, containerNode);
			Integer index = element.getIndex();
			PathExpression elementPath = index != null ? currentPath.index(index) : currentPath;
			expandChildren(element, ctx, ancestors, elementPath);
		}
	}

	/**
	 * Resolves container size using 4-level priority:
	 * <ol>
	 *   <li>Exact path match (builder explicitly set this path)</li>
	 *   <li>Type-based match (registered builder for the owning type)</li>
	 *   <li>Wildcard path match (registered builder with wildcard pattern)</li>
	 *   <li>Default resolver</li>
	 * </ol>
	 */
	private SizeResolution resolveContainerSize(
		JvmNode containerNode,
		TransformContext ctx,
		PathExpression currentPath
	) {
		// 1. Exact path match
		Optional<ContainerSizeResolver> exactResolver = resolverContext.findExactContainerSizeResolver(currentPath);
		if (exactResolver.isPresent()) {
			return new SizeResolution(exactResolver.get(), "EXACT_PATH");
		}

		// 2. Type-based resolver
		JvmNode parentNode = ctx.nodeToParent.get(containerNode);
		String fieldName = containerNode.getNodeName();
		if (parentNode != null && fieldName != null) {
			Optional<ContainerSizeResolver> typedResolver = resolverContext.findTypedContainerSizeResolver(
				parentNode.getConcreteType(),
				fieldName
			);
			if (typedResolver.isPresent()) {
				return new SizeResolution(typedResolver.get(), "TYPE_BASED");
			}
		}

		// 3. Wildcard path match
		Optional<ContainerSizeResolver> wildcardResolver = resolverContext.findWildcardContainerSizeResolver(
			currentPath
		);
		if (wildcardResolver.isPresent()) {
			return new SizeResolution(wildcardResolver.get(), "WILDCARD");
		}

		// 4. Default — use override from resolverContext if available (e.g., for fixed()),
		// otherwise fall back to the context's default resolver
		ContainerSizeResolver overrideResolver = resolverContext.getDefaultContainerSizeResolver();
		if (overrideResolver != null) {
			return new SizeResolution(overrideResolver, "DEFAULT", "fixed");
		}
		return new SizeResolution(context.getContainerSizeResolver(), "DEFAULT");
	}

	/**
	 * Resolves element types for all container elements using path-based resolvers.
	 */
	private List<JvmNode> resolveContainerElementTypes(List<JvmNode> elements, PathExpression currentPath) {
		List<JvmNode> resolved = new ArrayList<>(elements.size());
		for (JvmNode element : elements) {
			Integer index = element.getIndex();
			PathExpression elementPath = index != null ? currentPath.index(index) : currentPath;
			resolved.add(resolveNodeType(element, elementPath));
		}
		return resolved;
	}

	private static final class SizeResolution {

		final ContainerSizeResolver resolver;
		final String source;
		final @Nullable String metadata;

		SizeResolution(ContainerSizeResolver resolver, String source) {
			this(resolver, source, null);
		}

		SizeResolution(ContainerSizeResolver resolver, String source, @Nullable String metadata) {
			this.resolver = resolver;
			this.source = source;
			this.metadata = metadata;
		}
	}

	private JvmNode resolveNodeType(JvmNode element, PathExpression elementPath) {
		JvmType originalType = element.getConcreteType();
		Class<?> rawType = originalType.getRawType();
		boolean isAbstractType =
			Modifier.isInterface(rawType.getModifiers()) || Modifier.isAbstract(rawType.getModifiers());
		boolean isContainerType = context.isContainerType(originalType);

		JvmType afterInterface = resolveInterfaceType(originalType, elementPath, isAbstractType, isContainerType);
		JvmType resolvedType = resolveGenericType(afterInterface, elementPath);

		if (resolvedType.equals(originalType)) {
			return element;
		}

		JvmType finalType = mergeAnnotations(originalType, resolvedType);

		boolean pathBasedResolution = resolverContext.findInterfaceResolver(elementPath).isPresent();
		resolverContext
			.getResolutionListener()
			.onInterfaceResolved(
				elementPath.toString(),
				originalType.getRawType().getSimpleName(),
				finalType.getRawType().getSimpleName(),
				pathBasedResolution ? "PATH_BASED" : "DEFAULT"
			);

		return createResolvedNode(element, finalType);
	}

	/**
	 * Resolves interface/abstract types using path-based or default resolver.
	 * <p>
	 * For non-container abstract types without an explicit path-based resolver,
	 * resolution is skipped — they will be resolved at assembly time to allow
	 * per-sample selection of different implementations.
	 */
	private JvmType resolveInterfaceType(
		JvmType originalType,
		PathExpression path,
		boolean isAbstractType,
		boolean isContainerType
	) {
		Optional<InterfaceResolver> pathBasedResolver = resolverContext.findInterfaceResolver(path);
		if (pathBasedResolver.isPresent()) {
			JvmType resolved = pathBasedResolver.get().resolve(originalType);
			if (resolved != null) {
				return resolved;
			}
		} else if (!isAbstractType || isContainerType) {
			InterfaceResolver defaultResolver = context.getInterfaceResolver();
			if (defaultResolver != null) {
				JvmType resolved = defaultResolver.resolve(originalType);
				if (resolved != null) {
					return resolved;
				}
			}
		}
		return originalType;
	}

	/**
	 * Resolves generic types using path-based or default resolver.
	 */
	private JvmType resolveGenericType(JvmType inputType, PathExpression path) {
		Optional<GenericTypeResolver> pathBasedResolver = resolverContext.findGenericTypeResolver(path);
		if (pathBasedResolver.isPresent()) {
			JvmType resolved = pathBasedResolver.get().resolve(inputType);
			if (resolved != null) {
				return resolved;
			}
		} else {
			GenericTypeResolver defaultResolver = context.getGenericTypeResolver();
			if (defaultResolver != null) {
				JvmType resolved = defaultResolver.resolve(inputType);
				if (resolved != null) {
					return resolved;
				}
			}
		}
		return inputType;
	}

	/**
	 * Preserves original annotations on the resolved type.
	 * <p>
	 * When a container type is resolved (e.g., List → ArrayList), the resolved type
	 * loses field-level annotations (e.g., {@literal @}JsonTypeInfo, {@literal @}JsonSubTypes).
	 * Merging them ensures downstream consumers can still find these annotations.
	 */
	private JvmType mergeAnnotations(JvmType originalType, JvmType resolvedType) {
		List<Annotation> originalAnnotations = originalType.getAnnotations();
		if (originalAnnotations.isEmpty()) {
			return resolvedType;
		}

		Set<Class<? extends Annotation>> resolvedAnnotationTypes = resolvedType
			.getAnnotations()
			.stream()
			.map(Annotation::annotationType)
			.collect(Collectors.toSet());

		List<Annotation> merged = new ArrayList<>(resolvedType.getAnnotations());
		for (Annotation ann : originalAnnotations) {
			if (!resolvedAnnotationTypes.contains(ann.annotationType())) {
				merged.add(ann);
			}
		}
		return new JavaType(resolvedType.getRawType(), resolvedType.getTypeVariables(), merged);
	}

	/**
	 * Creates a new node with the resolved type, preserving Map/MapEntry semantics.
	 */
	private JvmNode createResolvedNode(JvmNode node, JvmType resolvedType) {
		if (node instanceof JvmMapNode) {
			JvmMapNode mapNode = (JvmMapNode)node;
			return new JavaMapNode(
				resolvedType,
				node.getNodeName(),
				node.getIndex(),
				mapNode.getKeyNode(),
				mapNode.getValueNode(),
				node.getCreationMethod()
			);
		}
		if (node instanceof JvmMapEntryNode) {
			JvmMapEntryNode mapEntryNode = (JvmMapEntryNode)node;
			return new JavaMapEntryNode(
				resolvedType,
				node.getNodeName(),
				node.getIndex(),
				mapEntryNode.getKeyNode(),
				mapEntryNode.getValueNode(),
				mapEntryNode.getCreationMethod()
			);
		}
		return new JavaNode(resolvedType, node.getNodeName(), node.getIndex(), node.getCreationMethod());
	}

	/**
	 * Expands a regular object (POJO) node by creating a new CandidateTree for its type.
	 * <p>
	 * When a {@link JvmNodeSubtreeContext} is available and conditions permit,
	 * this method reuses a cached snapshot of the promoted POJO subtree structure,
	 * only dynamically expanding container nodes within it.
	 */
	@SuppressWarnings({"argument", "dereference.of.nullable"})
	private void expandObjectChildren(
		JvmNode node,
		TransformContext ctx,
		Set<Class<?>> ancestors,
		PathExpression currentPath
	) {
		JvmType nodeType = node.getConcreteType();
		Class<?> rawType = nodeType.getRawType();

		// Cache applicability: no cycles, no path-specific resolvers
		boolean canCacheBase =
			subtreeContext != null && !ancestors.contains(rawType) && !resolverContext.hasPathSpecificResolvers();
		if (canCacheBase) {
			JvmNodeSubtreeContext.Snapshot snapshot = subtreeContext.get(nodeType);
			if (snapshot != null) {
				// Skip cache when:
				// 1. Ancestors differ from capture time — the snapshot's cycle truncation
				//    may not match the current context (types expanded/truncated differently).
				// 2. ExpansionContext is active and subtree is self-recursive —
				//    the cached structure was cycle-truncated but ExpansionContext
				//    needs to expand deeper at specific paths.
				boolean ancestorsCompatible = snapshot.isCompatibleWithAncestors(ancestors);
				boolean expansionNeedsDeeper = expansionContext != null && snapshot.selfRecursive;
				if (!ancestorsCompatible || expansionNeedsDeeper) {
					String skipReason = !ancestorsCompatible
						? "ancestors incompatible"
						: "self-recursive with expansion context";
					resolverContext
						.getResolutionListener()
						.onSubtreeCacheSkip(currentPath.toString(), rawType.getSimpleName(), skipReason);
				} else {
					resolverContext
						.getResolutionListener()
						.onSubtreeCacheHit(currentPath.toString(), rawType.getSimpleName(), snapshot.allNodes.size());
					applyCachedSubtree(node, snapshot, ctx, ancestors, currentPath);
					return;
				}
			} else {
				resolverContext
					.getResolutionListener()
					.onSubtreeCacheMiss(currentPath.toString(), rawType.getSimpleName());
			}
		}

		// Add to ancestors for cycle detection
		Set<Class<?>> childAncestors = new HashSet<>(ancestors);
		childAncestors.add(rawType);

		// Create CandidateTree for this type
		// When ExpansionContext is active and we need to expand this path,
		// pass empty ancestors and null treeContext to bypass cycle detection.
		final boolean isSpecialCycleExpansion =
			expansionContext != null
				&& ancestors.contains(rawType)
				&& expansionContext.shouldExpandPath(currentPath, rawType, ancestors);

		// Record ctx state before expansion for snapshot collection
		int allNodesBefore = ctx.allNodes.size();

		JvmNodeCandidateTree tree = new JvmNodeCandidateTree.Builder(nodeType, context)
			.withTreeContext(isSpecialCycleExpansion ? null : treeContext)
			.withAncestorTypes(isSpecialCycleExpansion ? new HashSet<>() : ancestors)
			.build();

		JvmNodeCandidate rootCandidate = tree.getRootNode();
		List<JvmNodeCandidate> childCandidates = tree.getChildren(rootCandidate);

		if (childCandidates.isEmpty()) {
			return;
		}

		List<JvmNode> childNodes = new ArrayList<>();
		for (JvmNodeCandidate childCandidate : childCandidates) {
			childNodes.addAll(promoteAndExpandCandidates(childCandidate, node, tree, ctx, childAncestors, currentPath));
		}

		if (!childNodes.isEmpty()) {
			ctx.parentChildMap.put(node, childNodes);
		}

		// Store snapshot for future reuse
		// Self-recursive subtrees are excluded when ExpansionContext is active because
		// the cycle-expansion logic may have produced a deeper structure than normal.
		if (canCacheBase && !childNodes.isEmpty()) {
			JvmNodeSubtreeContext.Snapshot snapshot = collectSnapshot(
				node,
				childNodes,
				ctx,
				allNodesBefore,
				currentPath,
				ancestors
			);
			if (expansionContext == null || !snapshot.selfRecursive) {
				subtreeContext.put(nodeType, snapshot);
				resolverContext
					.getResolutionListener()
					.onSubtreeCacheStore(
						currentPath.toString(),
						rawType.getSimpleName(),
						snapshot.allNodes.size(),
						snapshot.selfRecursive
					);
			}
		}
	}

	/**
	 * Applies a cached subtree snapshot to the current TransformContext.
	 * <p>
	 * Creates fresh clones of all snapshot nodes so that each cache application
	 * gets independent node objects. This prevents shared-reference issues when
	 * container nodes within the subtree are expanded with different sizes
	 * at different paths.
	 */
	private void applyCachedSubtree(
		JvmNode parentNode,
		JvmNodeSubtreeContext.Snapshot snapshot,
		TransformContext ctx,
		Set<Class<?>> ancestors,
		PathExpression currentPath
	) {
		// Build old→new node mapping by cloning all snapshot nodes
		Map<JvmNode, JvmNode> cloneMap = new IdentityHashMap<>(snapshot.allNodes.size());
		for (JvmNode original : snapshot.allNodes) {
			cloneMap.put(original, cloneNode(original));
		}

		// 1. Register cloned direct children under the parent node
		List<JvmNode> clonedDirectChildren = new ArrayList<>(snapshot.directChildren.size());
		for (JvmNode child : snapshot.directChildren) {
			clonedDirectChildren.add(requireClone(cloneMap, child));
		}
		ctx.parentChildMap.put(parentNode, clonedDirectChildren);

		// 2. Restore subtree structure with cloned nodes
		for (Map.Entry<JvmNode, List<JvmNode>> entry : snapshot.parentChildMap.entrySet()) {
			JvmNode clonedParent = requireClone(cloneMap, entry.getKey());
			List<JvmNode> clonedChildren = new ArrayList<>(entry.getValue().size());
			for (JvmNode child : entry.getValue()) {
				clonedChildren.add(requireClone(cloneMap, child));
			}
			ctx.parentChildMap.put(clonedParent, clonedChildren);
		}

		// 3. Restore all nodes and mappings with cloned nodes
		for (JvmNode original : snapshot.allNodes) {
			ctx.allNodes.add(requireClone(cloneMap, original));
		}
		for (Map.Entry<JvmNode, JvmNodeCandidate> entry : snapshot.nodeToCandidate.entrySet()) {
			ctx.nodeToCandidate.put(requireClone(cloneMap, entry.getKey()), entry.getValue());
		}
		for (Map.Entry<JvmNodeCandidate, List<JvmNode>> entry : snapshot.candidateToNodes.entrySet()) {
			List<JvmNode> clonedNodes = new ArrayList<>(entry.getValue().size());
			for (JvmNode node : entry.getValue()) {
				clonedNodes.add(requireClone(cloneMap, node));
			}
			ctx.candidateToNodes.put(entry.getKey(), clonedNodes);
		}

		// 4. Restore nodeToParent with cloned nodes
		for (JvmNode child : snapshot.directChildren) {
			ctx.nodeToParent.put(requireClone(cloneMap, child), parentNode);
		}
		for (Map.Entry<JvmNode, JvmNode> entry : snapshot.nodeToParent.entrySet()) {
			ctx.nodeToParent.put(requireClone(cloneMap, entry.getKey()), requireClone(cloneMap, entry.getValue()));
		}

		// 5. Dynamically expand container nodes (sizes vary per call)
		Set<Class<?>> childAncestors = new HashSet<>(ancestors);
		childAncestors.add(parentNode.getConcreteType().getRawType());

		for (JvmNode containerNode : snapshot.containerNodes) {
			JvmNode clonedContainer = requireClone(cloneMap, containerNode);
			PathExpression relativePath = snapshot.relativePathFromRoot.get(containerNode);
			if (relativePath == null) {
				continue;
			}
			PathExpression absolutePath = currentPath.append(relativePath);
			expandContainerElements(clonedContainer, ctx, childAncestors, absolutePath);
		}
	}

	private static JvmNode requireClone(Map<JvmNode, JvmNode> cloneMap, JvmNode original) {
		JvmNode clone = cloneMap.get(original);
		if (clone == null) {
			throw new IllegalStateException("Missing clone for node: " + original.getConcreteType().getRawType());
		}
		return clone;
	}

	/**
	 * Creates a shallow clone of a JvmNode with the same type, name, index, and creation method.
	 * Since JvmNode fields are all immutable, a new instance with the same values is sufficient.
	 */
	private static JvmNode cloneNode(JvmNode original) {
		return new JavaNode(
			original.getConcreteType(),
			original.getNodeName(),
			original.getIndex(),
			original.getCreationMethod()
		);
	}

	/**
	 * Collects a snapshot of the subtree that was just expanded under the given root node.
	 * <p>
	 * Traverses the TransformContext to extract all nodes, relationships, and metadata
	 * added during the expansion. Container nodes are identified and recorded for
	 * dynamic expansion on future cache hits.
	 */
	@SuppressWarnings({"argument", "dereference.of.nullable"})
	private JvmNodeSubtreeContext.Snapshot collectSnapshot(
		JvmNode rootNode,
		List<JvmNode> directChildren,
		TransformContext ctx,
		int allNodesBefore,
		PathExpression rootPath,
		Set<Class<?>> ancestors
	) {
		// Collect all nodes added during this expansion
		List<JvmNode> subtreeNodes = new ArrayList<>(ctx.allNodes.subList(allNodesBefore, ctx.allNodes.size()));

		// Build the set of subtree nodes for fast lookup
		Set<JvmNode> subtreeNodeSet = Collections.newSetFromMap(new IdentityHashMap<>());
		subtreeNodeSet.addAll(subtreeNodes);

		// Collect parent-child map entries within the subtree (excluding rootNode's own entry and container entries)
		Map<JvmNode, List<JvmNode>> subtreeParentChildMap = new HashMap<>();
		Map<JvmNode, JvmNode> subtreeNodeToParent = new HashMap<>();
		Map<JvmNode, JvmNodeCandidate> subtreeNodeToCandidate = new HashMap<>();
		Map<JvmNodeCandidate, List<JvmNode>> subtreeCandidateToNodes = new HashMap<>();
		List<JvmNode> containerNodes = new ArrayList<>();
		Map<JvmNode, PathExpression> relativePathFromRoot = new IdentityHashMap<>();
		Set<Class<?>> subtreeTypes = new HashSet<>();

		// Compute relative paths via BFS from rootNode
		Deque<JvmNode> bfsQueue = new ArrayDeque<>();
		Set<JvmNode> visited = Collections.newSetFromMap(new IdentityHashMap<>());

		for (JvmNode child : directChildren) {
			bfsQueue.add(child);
			JvmNodeCandidate candidate = ctx.nodeToCandidate.get(child);
			PathExpression childRelativePath =
				candidate != null && candidate.getName() != null
					? PathExpression.root().child(candidate.getName())
					: PathExpression.root();
			relativePathFromRoot.put(child, childRelativePath);
		}

		while (!bfsQueue.isEmpty()) {
			JvmNode current = bfsQueue.poll();
			if (!visited.add(current)) {
				continue;
			}

			PathExpression currentRelPath = relativePathFromRoot.get(current);

			// Collect all types present in the subtree for cycle detection at cache-read time
			subtreeTypes.add(current.getConcreteType().getRawType());

			// Collect nodeToCandidate mapping
			JvmNodeCandidate candidate = ctx.nodeToCandidate.get(current);
			if (candidate != null) {
				subtreeNodeToCandidate.put(current, candidate);
				subtreeCandidateToNodes.computeIfAbsent(candidate, k -> new ArrayList<>()).add(current);
			}

			// Collect nodeToParent (excluding direct children whose parent will be set dynamically)
			JvmNode parent = ctx.nodeToParent.get(current);
			if (parent != null && parent != rootNode) {
				subtreeNodeToParent.put(current, parent);
			}

			// Check if this is a container node
			if (context.isContainerType(current.getConcreteType())) {
				containerNodes.add(current);
				relativePathFromRoot.put(current, currentRelPath);
				// Don't traverse into container children — they're dynamic
				continue;
			}

			// Collect parent-child relationships for non-container POJO nodes
			List<JvmNode> children = ctx.parentChildMap.get(current);
			if (children != null && !children.isEmpty()) {
				List<JvmNode> inSetChildren = new ArrayList<>();

				for (JvmNode child : children) {
					if (subtreeNodeSet.contains(child)) {
						inSetChildren.add(child);
						JvmNodeCandidate childCandidate = ctx.nodeToCandidate.get(child);
						PathExpression childRelPath;
						Integer index = child.getIndex();
						if (index != null) {
							childRelPath = currentRelPath.index(index);
						} else if (childCandidate != null && childCandidate.getName() != null) {
							childRelPath = currentRelPath.child(childCandidate.getName());
						} else {
							childRelPath = currentRelPath;
						}
						relativePathFromRoot.put(child, childRelPath);
						bfsQueue.add(child);
					}
				}

				if (!inSetChildren.isEmpty()) {
					subtreeParentChildMap.put(current, inSetChildren);
				}
			}
		}

		boolean selfRecursive = subtreeTypes.contains(rootNode.getConcreteType().getRawType());

		return new JvmNodeSubtreeContext.Snapshot(
			directChildren,
			subtreeParentChildMap,
			subtreeNodes,
			subtreeNodeToCandidate,
			subtreeCandidateToNodes,
			relativePathFromRoot,
			containerNodes,
			subtreeNodeToParent,
			selfRecursive,
			subtreeTypes,
			ancestors
		);
	}

	/**
	 * Promotes a single candidate to node(s), resolves types, and recursively expands children.
	 * <p>
	 * Shared logic between {@link #transformFromCandidateTree} and {@link #expandObjectChildren}.
	 *
	 * @param childCandidate the candidate to promote
	 * @param parentNodeForMapping if non-null, sets nodeToParent mapping for promoted nodes
	 * @param candidateTree the candidate tree for recursive traversal
	 * @param ctx transform context
	 * @param ancestors ancestor types for cycle detection
	 * @param currentPath current path expression
	 * @return list of promoted and resolved child nodes
	 */
	private List<JvmNode> promoteAndExpandCandidates(
		JvmNodeCandidate childCandidate,
		@Nullable JvmNode parentNodeForMapping,
		JvmNodeCandidateTree candidateTree,
		TransformContext ctx,
		Set<Class<?>> ancestors,
		PathExpression currentPath
	) {
		List<JvmNode> result = new ArrayList<>();
		List<JvmNode> promotedNodes = promoteCandidate(childCandidate, ctx);

		for (JvmNode childNode : promotedNodes) {
			PathExpression childPath = createChildPath(currentPath, childCandidate, childNode);

			JvmNode resolvedChild = resolveNodeType(childNode, childPath);

			if (resolvedChild != childNode) {
				updateNodeMappings(ctx, childCandidate, childNode, resolvedChild);
			}

			ctx.allNodes.add(resolvedChild);
			if (parentNodeForMapping != null) {
				ctx.nodeToParent.put(resolvedChild, parentNodeForMapping);
			}
			result.add(resolvedChild);

			if (isTypeChanged(childCandidate.getType(), resolvedChild.getConcreteType())) {
				expandChildren(resolvedChild, ctx, ancestors, childPath);
			} else {
				transformFromCandidateTree(childCandidate, resolvedChild, candidateTree, ctx, ancestors, childPath);
			}
		}

		return result;
	}

	/**
	 * Generates container element nodes using the specified size resolver.
	 */
	private List<JvmNode> generateContainerElements(JvmNode containerNode, ContainerSizeResolver sizeResolver) {
		for (JvmContainerNodeGenerator generator : context.getContainerNodeGenerators()) {
			if (generator.isSupported(containerNode.getConcreteType())) {
				return generator.generateContainerElements(containerNode, context, sizeResolver);
			}
		}
		return new ArrayList<>();
	}

	/**
	 * Promotes a candidate to JvmNode(s).
	 */
	private List<JvmNode> promoteCandidate(JvmNodeCandidate candidate, TransformContext ctx) {
		JvmNodePromoter promoter = findPromoter(candidate);
		List<JvmNode> promotedNodes = promoter.promote(candidate, context);

		// Register mappings
		ctx.candidateToNodes.put(candidate, new ArrayList<>(promotedNodes));
		for (JvmNode node : promotedNodes) {
			ctx.nodeToCandidate.put(node, candidate);
		}

		return promotedNodes;
	}

	/**
	 * Updates node mappings when a node is replaced during type resolution.
	 * <p>
	 * This ensures the bidirectional mapping between nodes and candidates
	 * remains consistent after type resolution (e.g., List → ArrayList).
	 *
	 * @param ctx the transform context containing mappings
	 * @param candidate the candidate that was promoted
	 * @param oldNode the original promoted node
	 * @param newNode the resolved node that replaces the original
	 */
	private void updateNodeMappings(
		TransformContext ctx,
		JvmNodeCandidate candidate,
		JvmNode oldNode,
		JvmNode newNode
	) {
		// Update nodeToCandidate: remove old, add new
		ctx.nodeToCandidate.remove(oldNode);
		ctx.nodeToCandidate.put(newNode, candidate);

		// Update candidateToNodes: replace old node with new node in the list
		List<JvmNode> nodes = ctx.candidateToNodes.get(candidate);
		if (nodes != null) {
			int index = nodes.indexOf(oldNode);
			if (index >= 0) {
				nodes.set(index, newNode);
			} else {
				nodes.add(newNode);
			}
		}
	}

	/**
	 * Finds a suitable promoter for the candidate.
	 */
	private JvmNodePromoter findPromoter(JvmNodeCandidate candidate) {
		for (JvmNodePromoter promoter : context.getNodePromoters()) {
			if (promoter.canPromote(candidate)) {
				return promoter;
			}
		}
		throw new IllegalStateException(
			String.format(
				"No suitable promoter found for candidate '%s' of type '%s'",
				candidate.getName(),
				candidate.getType().getRawType().getName()
			)
		);
	}

	/**
	 * Checks if the candidate type and resolved node type differ.
	 */
	private boolean isTypeChanged(JvmType candidateType, JvmType nodeType) {
		// Different raw types
		if (!candidateType.getRawType().equals(nodeType.getRawType())) {
			return true;
		}

		// Check type variables
		List<? extends JvmType> candidateVars = candidateType.getTypeVariables();
		List<? extends JvmType> nodeVars = nodeType.getTypeVariables();

		if (candidateVars.size() != nodeVars.size()) {
			return true;
		}

		for (int i = 0; i < candidateVars.size(); i++) {
			if (!candidateVars.get(i).equals(nodeVars.get(i))) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Internal context for transformation state.
	 * Initial capacities are tuned for typical object graphs (16-64 nodes).
	 */
	private static class TransformContext {

		private static final int INITIAL_CAPACITY = 32;

		final Map<JvmNode, List<JvmNode>> parentChildMap = new HashMap<>(INITIAL_CAPACITY);
		final Map<JvmNode, JvmNode> nodeToParent = new HashMap<>(INITIAL_CAPACITY);
		final List<JvmNode> allNodes = new ArrayList<>(INITIAL_CAPACITY);
		final Map<JvmNode, JvmNodeCandidate> nodeToCandidate = new HashMap<>(INITIAL_CAPACITY);
		final Map<JvmNodeCandidate, List<JvmNode>> candidateToNodes = new HashMap<>(INITIAL_CAPACITY);
	}
}
