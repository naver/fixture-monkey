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
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.ContainerSizeResolver;
import com.navercorp.objectfarm.api.node.GenericTypeResolver;
import com.navercorp.objectfarm.api.node.InterfaceResolver;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;

/**
 * A context that holds path-based resolvers for tree transformation.
 * <p>
 * PathResolverContext enables fine-grained customization of node generation
 * based on the path within the tree structure. This is particularly useful for:
 * <ul>
 *   <li>Setting different container sizes at different levels of nesting</li>
 *   <li>Using different interface implementations at specific paths</li>
 *   <li>Resolving generic types differently based on location</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 * // For type: class User { List&lt;List&lt;String&gt;&gt; items; }
 * // Configure outer List to have 3 elements, inner Lists to have 5 elements
 *
 * PathResolverContext resolvers = PathResolverContext.builder()
 *     .addContainerSizeResolver("$.items", 3)      // outer List size
 *     .addContainerSizeResolver("$.items[*]", 5)   // inner List size
 *     .build();
 *
 * JvmNodeTreeTransformer transformer = new JvmNodeTreeTransformer(
 *     context, treeContext, resolvers
 * );
 * </pre>
 * <p>
 * Complex example with nested Maps:
 * <pre>
 * // For type: Map&lt;String, List&lt;Order&gt;&gt; userOrders
 * // where Order { List&lt;Item&gt; items; }
 *
 * PathResolverContext resolvers = PathResolverContext.builder()
 *     .addContainerSizeResolver("$.userOrders", 10)           // Map size
 *     .addContainerSizeResolver("$.userOrders[*]", 3)         // List&lt;Order&gt; size
 *     .addContainerSizeResolver("$.userOrders[*][*].items", 5) // items List size
 *     .build();
 * </pre>
 */
public final class PathResolverContext {

	private static final PathResolverContext EMPTY = new PathResolverContext(
		Collections.emptyList(),
		Collections.emptyList(),
		Collections.emptyList(),
		ResolutionListener.noOp(),
		null,
		null,
		null,
		null
	);

	private final List<PathResolver<ContainerSizeResolver>> containerSizeResolvers;
	private final List<PathResolver<InterfaceResolver>> interfaceResolvers;
	private final List<PathResolver<GenericTypeResolver>> genericTypeResolvers;

	/**
	 * Listener for tracking resolution decisions.
	 * Used for debugging and tracing purposes.
	 */
	private final ResolutionListener resolutionListener;

	/**
	 * Optional override for the default container size resolver.
	 * When set (non-null), this resolver is used instead of the JvmNodeContext's
	 * default resolver for containers without explicit size configuration.
	 * Used by fixed() to ensure deterministic container sizes.
	 */
	private final @Nullable ContainerSizeResolver defaultContainerSizeResolver;

	private final @Nullable AncestorAwareResolver<List<JvmNodeCandidate>> ancestorAwareChildCandidateResolver;

	private final @Nullable AncestorAwareResolver<ContainerSizeResolver> preWildcardContainerSizeResolver;

	private final @Nullable AncestorAwareResolver<ContainerSizeResolver> postWildcardContainerSizeResolver;

	private PathResolverContext(
		List<PathResolver<ContainerSizeResolver>> containerSizeResolvers,
		List<PathResolver<InterfaceResolver>> interfaceResolvers,
		List<PathResolver<GenericTypeResolver>> genericTypeResolvers,
		ResolutionListener resolutionListener,
		@Nullable ContainerSizeResolver defaultContainerSizeResolver,
		@Nullable AncestorAwareResolver<List<JvmNodeCandidate>> ancestorAwareChildCandidateResolver,
		@Nullable AncestorAwareResolver<ContainerSizeResolver> preWildcardContainerSizeResolver,
		@Nullable AncestorAwareResolver<ContainerSizeResolver> postWildcardContainerSizeResolver
	) {
		this.containerSizeResolvers = containerSizeResolvers;
		this.interfaceResolvers = interfaceResolvers;
		this.genericTypeResolvers = genericTypeResolvers;
		this.resolutionListener = resolutionListener;
		this.defaultContainerSizeResolver = defaultContainerSizeResolver;
		this.ancestorAwareChildCandidateResolver = ancestorAwareChildCandidateResolver;
		this.preWildcardContainerSizeResolver = preWildcardContainerSizeResolver;
		this.postWildcardContainerSizeResolver = postWildcardContainerSizeResolver;
	}

	/**
	 * Returns an empty context with no resolvers.
	 *
	 * @return an empty PathResolverContext
	 */
	public static PathResolverContext empty() {
		return EMPTY;
	}

	/**
	 * Creates a new builder for constructing a PathResolverContext.
	 *
	 * @return a new builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Finds a ContainerSizeResolver that matches the given path.
	 * When multiple resolvers match the same path, the last one added wins (highest priority).
	 *
	 * @param path the path to match
	 * @return an Optional containing the matching resolver, or empty if none match
	 */
	public Optional<ContainerSizeResolver> findContainerSizeResolver(PathExpression path) {
		ContainerSizeResolver last = null;
		for (PathResolver<ContainerSizeResolver> r : containerSizeResolvers) {
			if (r.matches(path)) {
				last = r.getCustomizer();
			}
		}
		return Optional.ofNullable(last);
	}

	/**
	 * Finds a ContainerSizeResolver that exactly matches the given path (no wildcard matching).
	 * This is used to prioritize explicitly set paths over pattern-based matches.
	 * When multiple resolvers match the same path, the last one added wins (highest priority).
	 *
	 * @param path the path to match exactly
	 * @return an Optional containing the matching resolver, or empty if none match
	 */
	public Optional<ContainerSizeResolver> findExactContainerSizeResolver(PathExpression path) {
		ContainerSizeResolver last = null;
		for (PathResolver<ContainerSizeResolver> r : containerSizeResolvers) {
			if (r instanceof PathContainerSizeResolver) {
				PathContainerSizeResolver pcsr = (PathContainerSizeResolver)r;
				if (pcsr.getPattern().equals(path)) {
					last = pcsr.getCustomizer();
				}
			}
		}
		return Optional.ofNullable(last);
	}

	/**
	 * Finds a ContainerSizeResolver that matches the given path using wildcard patterns.
	 * This excludes exact matches which should be handled separately.
	 * When multiple resolvers match the same path, the last one added wins (highest priority).
	 *
	 * @param path the path to match
	 * @return an Optional containing the matching resolver, or empty if none match
	 */
	public Optional<ContainerSizeResolver> findWildcardContainerSizeResolver(PathExpression path) {
		ContainerSizeResolver last = null;
		for (PathResolver<ContainerSizeResolver> r : containerSizeResolvers) {
			if (r instanceof PathContainerSizeResolver) {
				PathContainerSizeResolver pcsr = (PathContainerSizeResolver)r;
				if (!pcsr.getPattern().equals(path) && pcsr.matches(path)) {
					last = pcsr.getCustomizer();
				}
			}
		}
		return Optional.ofNullable(last);
	}

	/**
	 * Finds an InterfaceResolver that matches the given path.
	 *
	 * @param path the path to match
	 * @return an Optional containing the matching resolver, or empty if none match
	 */
	public Optional<InterfaceResolver> findInterfaceResolver(PathExpression path) {
		InterfaceResolver last = null;
		for (PathResolver<InterfaceResolver> r : interfaceResolvers) {
			if (r.matches(path)) {
				last = r.getCustomizer();
			}
		}
		return Optional.ofNullable(last);
	}

	/**
	 * Finds a GenericTypeResolver that matches the given path.
	 *
	 * @param path the path to match
	 * @return an Optional containing the matching resolver, or empty if none match
	 */
	public Optional<GenericTypeResolver> findGenericTypeResolver(PathExpression path) {
		GenericTypeResolver last = null;
		for (PathResolver<GenericTypeResolver> r : genericTypeResolvers) {
			if (r.matches(path)) {
				last = r.getCustomizer();
			}
		}
		return Optional.ofNullable(last);
	}

	/**
	 * Finds the direct child candidates of a node chosen from its ancestors.
	 *
	 * @param node      the node
	 * @param ancestors its ancestor nodes, outermost first
	 * @return the child candidates to use, or empty when the tree's own context generates them
	 */
	public Optional<List<JvmNodeCandidate>> findChildCandidates(JvmNode node, List<JvmNode> ancestors) {
		if (ancestorAwareChildCandidateResolver == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(ancestorAwareChildCandidateResolver.resolve(node, ancestors));
	}

	/**
	 * Finds the size resolver for a container node chosen from its ancestors, consulted before the path wildcard
	 * resolvers.
	 *
	 * @param containerNode the container node
	 * @param ancestors     its ancestor nodes, outermost first
	 * @return the size resolver, or empty when no ancestor-aware resolver sizes the container
	 */
	public Optional<ContainerSizeResolver> findPreWildcardContainerSizeResolver(
		JvmNode containerNode,
		List<JvmNode> ancestors
	) {
		if (preWildcardContainerSizeResolver == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(preWildcardContainerSizeResolver.resolve(containerNode, ancestors));
	}

	/**
	 * Finds the size resolver for a container node chosen from its ancestors, consulted after the path wildcard
	 * resolvers: it sizes a container only when no path resolver does.
	 *
	 * @param containerNode the container node
	 * @param ancestors     its ancestor nodes, outermost first
	 * @return the size resolver, or empty when no ancestor-aware resolver sizes the container
	 */
	public Optional<ContainerSizeResolver> findPostWildcardContainerSizeResolver(
		JvmNode containerNode,
		List<JvmNode> ancestors
	) {
		if (postWildcardContainerSizeResolver == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(postWildcardContainerSizeResolver.resolve(containerNode, ancestors));
	}

	public boolean hasAncestorAwareResolvers() {
		return ancestorAwareChildCandidateResolver != null
			|| preWildcardContainerSizeResolver != null
			|| postWildcardContainerSizeResolver != null;
	}

	/**
	 * Returns the resolution listener for tracking resolution decisions.
	 *
	 * @return the resolution listener (never null)
	 */
	public ResolutionListener getResolutionListener() {
		return resolutionListener;
	}

	/**
	 * Returns the optional default container size resolver override.
	 * When non-null, this should be used instead of the JvmNodeContext's default resolver.
	 *
	 * @return the default container size resolver override, or null if not set
	 */
	@Nullable
	public ContainerSizeResolver getDefaultContainerSizeResolver() {
		return defaultContainerSizeResolver;
	}

	/**
	 * Returns whether this context has path-specific interface or generic type resolvers.
	 * When path-specific resolvers exist, the same type at different paths may resolve differently,
	 * making type-based caching of promoted subtrees unsafe.
	 *
	 * @return true if path-specific resolvers exist
	 */
	public boolean hasPathSpecificResolvers() {
		return !interfaceResolvers.isEmpty()
			|| !genericTypeResolvers.isEmpty()
			|| (ancestorAwareChildCandidateResolver != null && ancestorAwareChildCandidateResolver.dependsOnAncestors())
			|| (preWildcardContainerSizeResolver != null
			&& preWildcardContainerSizeResolver.dependsOnAncestors())
			|| (postWildcardContainerSizeResolver != null
			&& postWildcardContainerSizeResolver.dependsOnAncestors());
	}

	/**
	 * Builder for constructing a PathResolverContext.
	 */
	public static final class Builder {

		private final List<PathResolver<ContainerSizeResolver>> containerSizeResolvers = new ArrayList<>();
		private final List<PathResolver<InterfaceResolver>> interfaceResolvers = new ArrayList<>();
		private final List<PathResolver<GenericTypeResolver>> genericTypeResolvers = new ArrayList<>();
		private ResolutionListener resolutionListener = ResolutionListener.noOp();
		private ContainerSizeResolver defaultContainerSizeResolver;
		private AncestorAwareResolver<List<JvmNodeCandidate>> ancestorAwareChildCandidateResolver;
		private AncestorAwareResolver<ContainerSizeResolver> preWildcardContainerSizeResolver;
		private AncestorAwareResolver<ContainerSizeResolver> postWildcardContainerSizeResolver;

		private Builder() {
		}

		/**
		 * Adds a ContainerSizeResolver for the given pattern.
		 *
		 * @param pattern   the pattern expression (e.g., "$.items[*]")
		 * @param size      the fixed container size
		 * @return this builder
		 */
		public Builder addContainerSizeResolver(String pattern, int size) {
			return addContainerSizeResolver(pattern, containerType -> size);
		}

		/**
		 * Adds a ContainerSizeResolver for the given pattern.
		 *
		 * @param pattern   the pattern expression (e.g., "$.items[*]")
		 * @param resolver  the container size resolver
		 * @return this builder
		 */
		public Builder addContainerSizeResolver(String pattern, ContainerSizeResolver resolver) {
			containerSizeResolvers.add(new PathContainerSizeResolver(PathExpression.of(pattern), resolver));
			return this;
		}

		/**
		 * Adds a ContainerSizeResolver for the given pattern.
		 *
		 * @param resolver  the path resolver
		 * @return this builder
		 */
		public Builder addContainerSizeResolver(PathResolver<ContainerSizeResolver> resolver) {
			containerSizeResolvers.add(resolver);
			return this;
		}

		/**
		 * Adds an InterfaceResolver for the given pattern.
		 *
		 * @param pattern   the pattern expression (e.g., "$.items")
		 * @param resolver  the interface resolver
		 * @return this builder
		 */
		public Builder addInterfaceResolver(String pattern, InterfaceResolver resolver) {
			interfaceResolvers.add(new PathInterfaceResolver(PathExpression.of(pattern), resolver));
			return this;
		}

		/**
		 * Adds an InterfaceResolver for the given pattern.
		 *
		 * @param resolver  the path resolver
		 * @return this builder
		 */
		public Builder addInterfaceResolver(PathResolver<InterfaceResolver> resolver) {
			interfaceResolvers.add(resolver);
			return this;
		}

		/**
		 * Adds a GenericTypeResolver for the given pattern.
		 *
		 * @param pattern   the pattern expression (e.g., "$.data")
		 * @param resolver  the generic type resolver
		 * @return this builder
		 */
		public Builder addGenericTypeResolver(String pattern, GenericTypeResolver resolver) {
			genericTypeResolvers.add(new PathGenericTypeResolver(PathExpression.of(pattern), resolver));
			return this;
		}

		/**
		 * Adds a GenericTypeResolver for the given pattern.
		 *
		 * @param resolver  the path resolver
		 * @return this builder
		 */
		public Builder addGenericTypeResolver(PathResolver<GenericTypeResolver> resolver) {
			genericTypeResolvers.add(resolver);
			return this;
		}

		/**
		 * Sets the resolution listener for tracking resolution decisions.
		 *
		 * @param listener the resolution listener (null will use no-op)
		 * @return this builder
		 */
		public Builder resolutionListener(ResolutionListener listener) {
			this.resolutionListener = listener != null ? listener : ResolutionListener.noOp();
			return this;
		}

		/**
		 * Sets the default container size resolver override.
		 * When set, this resolver is used instead of the JvmNodeContext's default resolver
		 * for containers without explicit size configuration.
		 *
		 * @param resolver the default container size resolver (null to use context default)
		 * @return this builder
		 */
		public Builder defaultContainerSizeResolver(ContainerSizeResolver resolver) {
			this.defaultContainerSizeResolver = resolver;
			return this;
		}

		/**
		 * Sets the resolver that chooses a node's direct child candidates from the types of its ancestors.
		 *
		 * @param resolver the resolver (null to always generate them with the tree's own context)
		 * @return this builder
		 */
		public Builder ancestorAwareChildCandidateResolver(AncestorAwareResolver<List<JvmNodeCandidate>> resolver) {
			this.ancestorAwareChildCandidateResolver = resolver;
			return this;
		}

		/**
		 * Sets the resolver that sizes a container node from its ancestors, consulted before the path wildcard
		 * resolvers.
		 *
		 * @param resolver the resolver (null to size containers without looking at their ancestors)
		 * @return this builder
		 */
		public Builder preWildcardContainerSizeResolver(AncestorAwareResolver<ContainerSizeResolver> resolver) {
			this.preWildcardContainerSizeResolver = resolver;
			return this;
		}

		/**
		 * Sets the resolver that sizes a container node from its ancestors when no path resolver, exact or
		 * wildcard, sizes it.
		 *
		 * @param resolver the resolver (null for none)
		 * @return this builder
		 */
		public Builder postWildcardContainerSizeResolver(
			AncestorAwareResolver<ContainerSizeResolver> resolver
		) {
			this.postWildcardContainerSizeResolver = resolver;
			return this;
		}


		/**
		 * Builds the PathResolverContext.
		 *
		 * @return a new PathResolverContext with the configured resolvers
		 */
		public PathResolverContext build() {
			if (containerSizeResolvers.isEmpty()
				&& interfaceResolvers.isEmpty()
				&& genericTypeResolvers.isEmpty()
				&& resolutionListener == ResolutionListener.noOp()
				&& defaultContainerSizeResolver == null
				&& ancestorAwareChildCandidateResolver == null
				&& preWildcardContainerSizeResolver == null
				&& postWildcardContainerSizeResolver == null) {
				return EMPTY;
			}
			return new PathResolverContext(
				Collections.unmodifiableList(new ArrayList<>(containerSizeResolvers)),
				Collections.unmodifiableList(new ArrayList<>(interfaceResolvers)),
				Collections.unmodifiableList(new ArrayList<>(genericTypeResolvers)),
				resolutionListener,
				defaultContainerSizeResolver,
				ancestorAwareChildCandidateResolver,
				preWildcardContainerSizeResolver,
				postWildcardContainerSizeResolver
			);
		}
	}

}
