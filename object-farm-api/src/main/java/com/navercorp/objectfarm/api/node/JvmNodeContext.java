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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.nodecandidate.JavaNodeCandidateFactory;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidateGenerator;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.Types;

/**
 * A context interface that provides necessary objects for consistent JVM node creation.
 * <p>
 * This interface bundles required components (seed values, node promoters, candidate node generators,
 * container node generators, and resolvers) so that nodes within the same node tree can share
 * identical configurations and creation policies.
 * </p>
 * <p>
 * Different node trees can have their own independent {@code JvmNodeContext},
 * allowing different creation strategies to be applied per tree.
 * </p>
 * <p>
 * Key components:
 * <ul>
 *   <li>Candidate node generators - for generating JvmNodeCandidate instances (excluding containers)</li>
 *   <li>Container node generators - for generating container element JvmNode instances</li>
 *   <li>Resolvers - for customizing node generation (ContainerSize, Interface, GenericType)</li>
 * </ul>
 */
public interface JvmNodeContext {
	SeedState getSeedState();

	List<JvmNodePromoter> getNodePromoters();

	/**
	 * Returns the list of candidate node generators.
	 * <p>
	 * These generators are used for creating JvmNodeCandidate instances.
	 * Container generators are NOT included here - use {@link #getContainerNodeGenerators()} instead.
	 *
	 * @return list of candidate node generators
	 */
	List<JvmNodeCandidateGenerator> getCandidateNodeGenerators();

	/**
	 * Returns the list of container node generators.
	 * <p>
	 * These generators are used for creating container element JvmNode instances
	 * (e.g., List elements, Map entries, Array elements).
	 *
	 * @return list of container node generators
	 */
	List<JvmContainerNodeGenerator> getContainerNodeGenerators();

	/**
	 * Returns the container size resolver used to determine container sizes.
	 *
	 * @return the container size resolver
	 */
	ContainerSizeResolver getContainerSizeResolver();

	/**
	 * Returns the interface resolver used to resolve interface types to concrete implementations.
	 *
	 * @return the interface resolver, or null if not configured
	 */
	@Nullable
	InterfaceResolver getInterfaceResolver();

	/**
	 * Returns the generic type resolver used to resolve generic type parameters.
	 *
	 * @return the generic type resolver, or null if not configured
	 */
	@Nullable
	GenericTypeResolver getGenericTypeResolver();

	/**
	 * Returns the maximum recursion depth for type resolution and nested operations.
	 * <p>
	 * This value is used to prevent infinite loops when resolving nested types
	 * (e.g., interface to concrete type resolution).
	 *
	 * @return the maximum recursion depth, defaults to 5
	 */
	default int getMaxRecursionDepth() {
		return 5;
	}

	/**
	 * Checks if the given type is a container type recognized by registered generators.
	 */
	default boolean isContainerType(JvmType jvmType) {
		Class<?> rawType = jvmType.getRawType();
		if (rawType.isArray() || Collection.class.isAssignableFrom(rawType) || Map.class.isAssignableFrom(rawType)) {
			return true;
		}
		for (JvmContainerNodeGenerator generator : getContainerNodeGenerators()) {
			if (generator.isSupported(jvmType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the given type should be treated as a leaf node (no children to expand).
	 * <p>
	 * By default, Java standard types ({@code java.*}, {@code sun.*}, primitives) are leaf types.
	 * Override to add custom leaf type detection (e.g., Kotlin standard library types).
	 *
	 * @param jvmType the type to check
	 * @return true if the type should be treated as a leaf, false otherwise
	 */
	default boolean isLeafType(JvmType jvmType) {
		return Types.isJavaType(jvmType.getRawType());
	}

	/**
	 * Creates a root candidate node for the given type.
	 * <p>
	 * Override this method to provide type-specific root candidates.
	 * The default implementation delegates to {@link JavaNodeCandidateFactory#INSTANCE}.
	 *
	 * @param rootType the root type
	 * @return a candidate node appropriate for the type
	 */
	default JvmNodeCandidate createRootCandidate(JvmType rootType) {
		return JavaNodeCandidateFactory.INSTANCE.create(rootType, "root", null);
	}
}
