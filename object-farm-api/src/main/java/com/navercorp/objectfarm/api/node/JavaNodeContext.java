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

package com.navercorp.objectfarm.api.node;

import static com.navercorp.objectfarm.api.node.JavaNodeContext.Builder.DEFAULT_MAX_DEPTH;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.nodecandidate.DefaultGenericNodeCandidateGenerator;
import com.navercorp.objectfarm.api.nodecandidate.DefaultInterfaceNodeCandidateGenerator;
import com.navercorp.objectfarm.api.nodecandidate.JavaFieldNodeCandidateGenerator;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidateGenerator;
import com.navercorp.objectfarm.api.nodecandidate.ObjectFarmJdkVariantOptions;
import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Context for Java node creation that provides necessary components for consistent node generation.
 * <p>
 * Use the {@link Builder} to create instances with properly configured components:
 * <pre>{@code
 * JavaNodeContext context = JavaNodeContext.builder()
 *     .seed(12345L)
 *     .nodePromoters(promoterList)
 *     .containerSizeResolver(sizeResolver)
 *     .genericResolver(genericResolver)        // optional
 *     .interfaceResolver(interfaceResolver)    // optional
 *     .maxDepth(10)                            // optional, default is 10
 *     .build();
 * }</pre>
 * <p>
 * Key design principles:
 * <ul>
 *   <li>Candidate generators do NOT include container generators (elements are generated at JvmNode level)</li>
 *   <li>Container node generators are separate and used for generating container element nodes</li>
 *   <li>Resolvers (ContainerSizeResolver, InterfaceResolver, GenericTypeResolver) are accessible for customization</li>
 * </ul>
 */
public final class JavaNodeContext implements JvmNodeContext {

	private final SeedState seedState;
	private final List<JvmNodePromoter> nodePromoters;
	private final List<JvmNodeCandidateGenerator> candidateNodeGenerators;
	private final List<JvmContainerNodeGenerator> containerNodeGenerators;
	private final ContainerSizeResolver containerSizeResolver;

	@Nullable
	private final InterfaceResolver interfaceResolver;

	@Nullable
	private final GenericTypeResolver genericTypeResolver;

	private final int maxRecursionDepth;

	private final LeafTypeResolver leafTypeResolver;

	/**
	 * Creates a JavaNodeContext with the specified components.
	 * <p>
	 * Private constructor to enforce use of {@link Builder}.
	 */
	private JavaNodeContext(
		SeedState seedState,
		List<JvmNodePromoter> nodePromoters,
		List<JvmNodeCandidateGenerator> candidateNodeGenerators,
		List<JvmContainerNodeGenerator> containerNodeGenerators,
		ContainerSizeResolver containerSizeResolver,
		@Nullable InterfaceResolver interfaceResolver,
		@Nullable GenericTypeResolver genericTypeResolver,
		int maxRecursionDepth,
		LeafTypeResolver leafTypeResolver
	) {
		this.seedState = seedState;
		this.nodePromoters = nodePromoters;
		this.candidateNodeGenerators = candidateNodeGenerators;
		this.containerNodeGenerators = containerNodeGenerators;
		this.containerSizeResolver = containerSizeResolver;
		this.interfaceResolver = interfaceResolver;
		this.genericTypeResolver = genericTypeResolver;
		this.maxRecursionDepth = maxRecursionDepth;
		this.leafTypeResolver = leafTypeResolver;
	}

	@Override
	public SeedState getSeedState() {
		return this.seedState;
	}

	@Override
	public List<JvmNodePromoter> getNodePromoters() {
		return this.nodePromoters;
	}

	@Override
	public List<JvmNodeCandidateGenerator> getCandidateNodeGenerators() {
		return this.candidateNodeGenerators;
	}

	@Override
	public List<JvmContainerNodeGenerator> getContainerNodeGenerators() {
		return this.containerNodeGenerators;
	}

	@Override
	public ContainerSizeResolver getContainerSizeResolver() {
		return this.containerSizeResolver;
	}

	@Override
	@Nullable
	public InterfaceResolver getInterfaceResolver() {
		return this.interfaceResolver;
	}

	@Override
	@Nullable
	public GenericTypeResolver getGenericTypeResolver() {
		return this.genericTypeResolver;
	}

	@Override
	public int getMaxRecursionDepth() {
		return this.maxRecursionDepth;
	}

	@Override
	public boolean isLeafType(JvmType jvmType) {
		return leafTypeResolver.isLeafType(jvmType);
	}

	/**
	 * Creates a new Builder instance for constructing JavaNodeContext.
	 *
	 * @return a new Builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Returns a new context with the given generator prepended to the candidate generators list.
	 *
	 * @param generator the generator to add
	 * @return a new context with the generator added at highest priority
	 */
	public JavaNodeContext withAdditionalGenerator(JvmNodeCandidateGenerator generator) {
		List<JvmNodeCandidateGenerator> newGenerators = new ArrayList<>(this.candidateNodeGenerators.size() + 1);
		newGenerators.add(generator);
		newGenerators.addAll(this.candidateNodeGenerators);
		return new JavaNodeContext(
			this.seedState,
			this.nodePromoters,
			Collections.unmodifiableList(newGenerators),
			this.containerNodeGenerators,
			this.containerSizeResolver,
			this.interfaceResolver,
			this.genericTypeResolver,
			this.maxRecursionDepth,
			this.leafTypeResolver
		);
	}

	/**
	 * Builder for creating JavaNodeContext with properly configured components.
	 * <p>
	 * The builder enforces the correct ordering of generators:
	 * <ol>
	 *   <li>Custom generators (optional) - highest priority for custom handling</li>
	 *   <li>Generic type generator (optional) - handles generic type parameters</li>
	 *   <li>Interface generator (always present) - handles interface resolution</li>
	 *   <li>Object property generator (default provided) - fallback for general objects</li>
	 * </ol>
	 * <p>
	 * Note: Container generators are NOT included in candidateNodeGenerators.
	 * They are stored separately and accessed via {@link #getContainerNodeGenerators()}.
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * JavaNodeContext context = JavaNodeContext.builder()
	 *     .seed(12345L)
	 *     .nodePromoters(Arrays.asList(promoter1, promoter2))
	 *     .containerSizeResolver(new RandomContainerSizeResolver())
	 *     .interfaceResolver(customResolver)  // optional
	 *     .build();
	 * }</pre>
	 */
	public static class Builder {

		static final int DEFAULT_MAX_DEPTH = 10;

		private SeedState seedState = new SeedState(0L);
		private List<JvmNodePromoter> nodePromoters = Collections.emptyList();
		private ContainerSizeResolver containerSizeResolver;

		@Nullable
		private GenericTypeResolver genericResolver;

		@Nullable
		private InterfaceResolver interfaceResolver;

		private JvmNodeCandidateGenerator objectPropertyGenerator = new JavaFieldNodeCandidateGenerator();
		private final List<JvmNodeCandidateGenerator> customGenerators = new ArrayList<>();
		private final List<JvmContainerNodeGenerator> customContainerGenerators = new ArrayList<>();
		private int maxDepth = DEFAULT_MAX_DEPTH;

		private final List<LeafTypeResolver> leafTypeResolvers = new ArrayList<>();
		@Nullable
		private Predicate<JvmType> leafTypeExclusion = null;

		/**
		 * Sets the seed state for random generation.
		 *
		 * @param seedState the seed state
		 * @return this builder
		 */
		public Builder seedState(SeedState seedState) {
			this.seedState = seedState;
			return this;
		}

		/**
		 * Sets the seed for random generation.
		 *
		 * @param seed the seed value
		 * @return this builder
		 */
		public Builder seed(long seed) {
			this.seedState = new SeedState(seed);
			return this;
		}

		/**
		 * Sets the node promoters.
		 *
		 * @param nodePromoters the list of node promoters
		 * @return this builder
		 */
		public Builder nodePromoters(List<JvmNodePromoter> nodePromoters) {
			this.nodePromoters = nodePromoters;
			return this;
		}

		/**
		 * Sets the container size resolver which will be used to determine container sizes.
		 * This is required for generating container elements (List, Set, Array, Map).
		 *
		 * @param containerSizeResolver the container size resolver
		 * @return this builder
		 */
		public Builder containerSizeResolver(ContainerSizeResolver containerSizeResolver) {
			this.containerSizeResolver = containerSizeResolver;
			return this;
		}

		/**
		 * Sets the generic type resolver for resolving generic type parameters.
		 * If provided, a {@link DefaultGenericNodeCandidateGenerator} will be created automatically.
		 *
		 * @param genericResolver the generic type resolver
		 * @return this builder
		 */
		public Builder genericResolver(@Nullable GenericTypeResolver genericResolver) {
			this.genericResolver = genericResolver;
			return this;
		}

		/**
		 * Sets the interface resolver for resolving interface types to concrete implementations.
		 * A {@link DefaultInterfaceNodeCandidateGenerator} will be created using this resolver
		 * (or identity resolver if not provided).
		 *
		 * @param interfaceResolver the interface resolver
		 * @return this builder
		 */
		public Builder interfaceResolver(@Nullable InterfaceResolver interfaceResolver) {
			this.interfaceResolver = interfaceResolver;
			return this;
		}

		/**
		 * Sets a custom object property generator for generating node candidates from object properties.
		 * This generator handles general objects by extracting their properties (fields, getters, setters, etc.).
		 * If not set, uses the default {@link JavaFieldNodeCandidateGenerator} which generates candidates from fields.
		 *
		 * @param objectPropertyGenerator the object property generator
		 * @return this builder
		 */
		public Builder objectPropertyGenerator(JvmNodeCandidateGenerator objectPropertyGenerator) {
			this.objectPropertyGenerator = objectPropertyGenerator;
			return this;
		}

		/**
		 * Adds a custom generator that will be executed with highest priority.
		 * Use this for special cases that need custom handling before the standard generators.
		 *
		 * @param generator the custom generator
		 * @return this builder
		 */
		public Builder addCustomGenerator(JvmNodeCandidateGenerator generator) {
			this.customGenerators.add(generator);
			return this;
		}

		/**
		 * Adds a custom container node generator.
		 * Use this for handling custom container types that are not covered by the default generators.
		 *
		 * @param generator the custom container node generator
		 * @return this builder
		 */
		public Builder addContainerNodeGenerator(JvmContainerNodeGenerator generator) {
			this.customContainerGenerators.add(generator);
			return this;
		}

		/**
		 * Sets the maximum depth for recursive resolution of interface and generic types.
		 * This prevents infinite loops when resolving circular references.
		 * Default value is 10.
		 *
		 * @param maxDepth the maximum recursion depth
		 * @return this builder
		 */
		public Builder maxDepth(int maxDepth) {
			this.maxDepth = maxDepth;
			return this;
		}

		/**
		 * Adds a resolver that identifies additional leaf types.
		 * The default {@link JavaLeafTypeResolver} is always included.
		 * Additional resolvers (e.g., {@code KotlinLeafTypeResolver}) can be added for
		 * platform-specific leaf types.
		 *
		 * @param leafTypeResolver the resolver to add
		 * @return this builder
		 */
		public Builder addLeafTypeResolver(LeafTypeResolver leafTypeResolver) {
			this.leafTypeResolvers.add(leafTypeResolver);
			return this;
		}

		/**
		 * Sets a predicate that excludes certain types from being treated as leaf types.
		 * When set, if this predicate returns {@code true} for a type, that type will NOT
		 * be treated as a leaf even if a {@link LeafTypeResolver} would classify it as one.
		 * <p>
		 * This is useful when a generator (e.g., {@code ConstructorArbitraryIntrospector})
		 * needs to expand Java standard types that would normally be leaf nodes.
		 *
		 * @param exclusion predicate returning true for types that should NOT be leaf
		 * @return this builder
		 */
		public Builder leafTypeExclusion(Predicate<JvmType> exclusion) {
			this.leafTypeExclusion = exclusion;
			return this;
		}

		/**
		 * Builds the JavaNodeContext with properly configured components.
		 * <p>
		 * The final order of candidate node generators will be:
		 * <ol>
		 *   <li>Custom generators (if any) - highest priority for custom handling</li>
		 *   <li>Generic generator (if genericResolver is set)</li>
		 *   <li>Interface generator (always present, using interfaceResolver or identity resolver)</li>
		 *   <li>Object property generator (always present)</li>
		 * </ol>
		 * <p>
		 * Container node generators are stored separately and include:
		 * <ol>
		 *   <li>Custom container generators (if any)</li>
		 *   <li>JavaLinearContainerElementNodeGenerator (for List, Set, etc.)</li>
		 *   <li>JavaArrayElementNodeGenerator (for arrays)</li>
		 *   <li>JavaMapElementNodeGenerator (for Map)</li>
		 * </ol>
		 *
		 * @return the constructed JavaNodeContext
		 * @throws IllegalStateException if containerSizeResolver is not set
		 */
		public JavaNodeContext build() {
			if (containerSizeResolver == null) {
				throw new IllegalStateException("containerSizeResolver is required");
			}

			// JDK variant options (e.g. record support on Java 17+)
			new ObjectFarmJdkVariantOptions().apply(this);

			// Build candidate node generators (NO container generators!)
			// Interface and Generic types are treated as leaf nodes at Candidate level.
			// Resolution happens at Node level in JvmNodeTreeTransformer.
			List<JvmNodeCandidateGenerator> generators = new ArrayList<>(customGenerators);

			// Object property generator only - interfaces/generics become leaf nodes naturally
			// (interfaces have no instance fields, so JavaFieldNodeCandidateGenerator returns empty)
			generators.add(objectPropertyGenerator);

			// Build resolvers for Node-level resolution
			GenericTypeResolver recursiveGenericResolver = null;
			if (genericResolver != null) {
				recursiveGenericResolver = new RecursiveGenericTypeResolver(genericResolver, maxDepth);
			}

			// Interface resolver with default mapping
			InterfaceResolver baseResolver;
			if (interfaceResolver != null) {
				InterfaceResolver userResolver = interfaceResolver;
				InterfaceResolver defaultMapping = createDefaultInterfaceMapping();
				baseResolver = jvmType -> {
					JvmType resolved = userResolver.resolve(jvmType);
					if (resolved == null) {
						return defaultMapping.resolve(jvmType);
					}
					return resolved;
				};
			} else {
				baseResolver = createDefaultInterfaceMapping();
			}

			InterfaceResolver actualInterfaceResolver = new RecursiveInterfaceResolver(baseResolver, maxDepth);

			// Build container node generators
			List<JvmContainerNodeGenerator> containerGenerators = new ArrayList<>(customContainerGenerators);
			containerGenerators.add(new JavaSingleElementContainerNodeGenerator());
			containerGenerators.add(new JavaLinearContainerElementNodeGenerator());
			containerGenerators.add(new JavaArrayElementNodeGenerator());
			containerGenerators.add(new JavaMapElementNodeGenerator());

			// Build composite leaf type resolver: Java standard types + any additional resolvers
			List<LeafTypeResolver> allLeafResolvers = new ArrayList<>();
			allLeafResolvers.add(JavaLeafTypeResolver.INSTANCE);
			allLeafResolvers.addAll(leafTypeResolvers);
			Predicate<JvmType> exclusion = leafTypeExclusion;
			LeafTypeResolver compositeLeafResolver = jvmType -> {
				// Exclusion takes precedence: if excluded, it's never a leaf
				if (exclusion != null && exclusion.test(jvmType)) {
					return false;
				}
				for (LeafTypeResolver resolver : allLeafResolvers) {
					if (resolver.isLeafType(jvmType)) {
						return true;
					}
				}
				return false;
			};

			return new JavaNodeContext(
				seedState,
				nodePromoters,
				Collections.unmodifiableList(generators),
				Collections.unmodifiableList(containerGenerators),
				containerSizeResolver,
				actualInterfaceResolver,
				recursiveGenericResolver,
				maxDepth,
				compositeLeafResolver
			);
		}
	}

	/**
	 * Creates a basic interface mapping resolver that maps common interface types to concrete implementations.
	 * This resolver does NOT recursively process type variables - use {@link RecursiveInterfaceResolver}
	 * to wrap this for recursive behavior.
	 *
	 * @return a basic interface resolver
	 */
	private static InterfaceResolver createDefaultInterfaceMapping() {
		return jvmType -> {
			Class<?> rawType = jvmType.getRawType();

			// Not an interface, return null (not resolvable)
			if (!rawType.isInterface()) {
				return null;
			}

			Class<?> implementation = getDefaultImplementation(rawType);

			// No default mapping exists, return null
			if (implementation == rawType) {
				return null;
			}

			// Return new type with concrete implementation
			return new JavaType(implementation, jvmType.getTypeVariables(), jvmType.getAnnotations());
		};
	}

	/**
	 * Returns the default concrete implementation for a given interface type.
	 *
	 * @param interfaceType the interface type
	 * @return the concrete implementation class, or the original type if no mapping exists
	 */
	private static Class<?> getDefaultImplementation(Class<?> interfaceType) {
		// SortedSet before Set (more specific first)
		if (SortedSet.class.isAssignableFrom(interfaceType)) {
			return TreeSet.class;
		}
		if (Set.class.isAssignableFrom(interfaceType)) {
			return HashSet.class;
		}

		// List
		if (List.class.isAssignableFrom(interfaceType)) {
			return ArrayList.class;
		}

		// SortedMap before Map (more specific first)
		if (SortedMap.class.isAssignableFrom(interfaceType)) {
			return TreeMap.class;
		}
		if (Map.class.isAssignableFrom(interfaceType)) {
			return HashMap.class;
		}

		// Collection (fallback for other Collection types)
		if (Collection.class.isAssignableFrom(interfaceType)) {
			return ArrayList.class;
		}

		// Iterable (fallback)
		if (Iterable.class.isAssignableFrom(interfaceType)) {
			return ArrayList.class;
		}

		// No default mapping, return as-is
		return interfaceType;
	}

	/**
	 * An {@link InterfaceResolver} implementation that recursively resolves
	 * interface types until a concrete implementation is found.
	 */
	private static class RecursiveInterfaceResolver implements InterfaceResolver {

		private final InterfaceResolver delegate;
		private final int maxDepth;

		public RecursiveInterfaceResolver(InterfaceResolver delegate) {
			this(delegate, DEFAULT_MAX_DEPTH);
		}

		public RecursiveInterfaceResolver(InterfaceResolver delegate, int maxDepth) {
			this.delegate = delegate;
			this.maxDepth = maxDepth;
		}

		@Override
		@Nullable
		public JvmType resolve(JvmType interfaceType) {
			Set<JvmType> visited = new HashSet<>();
			JvmType current = interfaceType;
			int depth = 0;

			while (depth < maxDepth) {
				// Check for circular resolution
				if (visited.contains(current)) {
					return null;
				}

				visited.add(current);

				// Apply the delegate resolver
				JvmType resolved = delegate.resolve(current);

				// If the resolver returned null, can't resolve further
				if (resolved == null) {
					return null;
				}

				// If the type didn't change, we're done
				if (resolved.equals(current)) {
					return resolved;
				}

				// If the resolved type is not an interface, we're done
				if (!resolved.getRawType().isInterface()) {
					return resolved;
				}

				current = resolved;
				depth++;
			}

			// Maximum depth reached, can't resolve
			return null;
		}
	}

	/**
	 * A wrapper {@link GenericTypeResolver} that recursively resolves generic types
	 * until no more resolution is possible.
	 */
	private static final class RecursiveGenericTypeResolver implements GenericTypeResolver {

		private final GenericTypeResolver delegate;
		private final int maxDepth;

		RecursiveGenericTypeResolver(GenericTypeResolver delegate) {
			this(delegate, DEFAULT_MAX_DEPTH);
		}

		RecursiveGenericTypeResolver(GenericTypeResolver delegate, int maxDepth) {
			this.delegate = delegate;
			this.maxDepth = maxDepth;
		}

		@Override
		@Nullable
		public JvmType resolve(JvmType jvmType) {
			Set<JvmType> visited = new HashSet<>();
			JvmType current = jvmType;
			int depth = 0;

			while (depth < maxDepth) {
				// Check for circular resolution
				if (visited.contains(current)) {
					return null;
				}

				visited.add(current);

				// Apply the delegate resolver
				JvmType resolved = delegate.resolve(current);

				// If the resolver returned null, can't resolve further
				if (resolved == null) {
					return null;
				}

				// If the type didn't change, we're done
				if (resolved.equals(current)) {
					return resolved;
				}

				current = resolved;
				depth++;
			}

			// Maximum depth reached, can't resolve
			return null;
		}
	}
}
