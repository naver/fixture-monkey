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

package com.navercorp.fixturemonkey.adapter;

import java.util.concurrent.ConcurrentHashMap;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.adapter.analysis.AdaptationResult;
import com.navercorp.fixturemonkey.adapter.tracing.TraceContext;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.customizer.ManipulatorSet;
import com.navercorp.objectfarm.api.tree.JvmNodeTree;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Adapter interface for converting fixture-monkey's ManipulatorSet information
 * to object-farm-api's JvmNodeTree.
 * <p>
 * This adapter bridges the gap between fixture-monkey's ArbitraryManipulator-based
 * customization system and object-farm-api's immutable JvmNodeTree structure.
 * <p>
 * The adapter analyzes the ManipulatorSet to extract:
 * <ul>
 *   <li>Container size information from ContainerInfoManipulator</li>
 *   <li>Interface resolution from NodeSetDecomposedValueManipulator</li>
 *   <li>Generic type hints from value types</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * NodeTreeAdapter adapter = new DefaultNodeTreeAdapter(seed);
 * AdaptationResult result = adapter.adapt(rootType, manipulatorSet, options);
 * }</pre>
 *
 * @see DefaultNodeTreeAdapter
 * @see ManipulatorSet
 * @see JvmNodeTree
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
public interface NodeTreeAdapter {
	/**
	 * Converts a root type and ManipulatorSet into an AdaptationResult containing
	 * the JvmNodeTree and extracted value information.
	 *
	 * @param rootType       the root JvmType to create the tree for
	 * @param manipulatorSet the ManipulatorSet containing customization information
	 * @return an AdaptationResult containing the JvmNodeTree and value information
	 */
	AdaptationResult adapt(JvmType rootType, ManipulatorSet manipulatorSet);

	/**
	 * Converts a root type and ManipulatorSet into an AdaptationResult with options.
	 * <p>
	 * This overload allows passing FixtureMonkeyOptions for container size generation.
	 *
	 * @param rootType       the root JvmType to create the tree for
	 * @param manipulatorSet the ManipulatorSet containing customization information
	 * @param options        the FixtureMonkey options for container info generation (nullable)
	 * @return an AdaptationResult containing the JvmNodeTree and value information
	 */
	AdaptationResult adapt(
		JvmType rootType,
		ManipulatorSet manipulatorSet,
		@Nullable FixtureMonkeyOptions options
	);

	/**
	 * Converts a root type and ManipulatorSet into an AdaptationResult with tracing support.
	 * <p>
	 * This overload allows passing a TraceContext to capture resolution decisions
	 * (interface resolution, container size resolution) during tree transformation.
	 *
	 * @param rootType       the root JvmType to create the tree for
	 * @param manipulatorSet the ManipulatorSet containing customization information
	 * @param options        the FixtureMonkey options for container info generation (nullable)
	 * @param traceContext   the trace context for capturing resolution decisions (nullable)
	 * @return an AdaptationResult containing the JvmNodeTree and value information
	 */
	AdaptationResult adapt(
		JvmType rootType,
		ManipulatorSet manipulatorSet,
		@Nullable FixtureMonkeyOptions options,
		@Nullable TraceContext traceContext
	);

	/**
	 * Creates a JvmNodeTree for the given concrete type.
	 * <p>
	 * Used when generating with interface/abstract implementations to obtain
	 * the proper tree structure with child nodes. The underlying JvmNodeCandidateTree
	 * may be cached, but the JvmNodeTree is created fresh each time to ensure
	 * container sizes vary across calls.
	 *
	 * @param concreteType the concrete JvmType to create the tree for
	 * @param options      the FixtureMonkey options for tree building
	 * @return the JvmNodeTree, or null if not available
	 */
	@Nullable
	JvmNodeTree createConcreteNodeTree(JvmType concreteType, FixtureMonkeyOptions options);

	/**
	 * Creates a JvmNodeTree for an anonymous instance of the given interface/abstract type.
	 * <p>
	 * The tree has children derived from no-argument methods of the interface,
	 * enabling anonymous proxy generation with path-based value customization.
	 * Always created fresh (no caching).
	 *
	 * @param interfaceType the interface/abstract JvmType to create the tree for
	 * @param options       the FixtureMonkey options for tree building
	 * @return the JvmNodeTree for anonymous instance generation, or null if not available
	 */
	@Nullable
	JvmNodeTree createAnonymousNodeTree(JvmType interfaceType, FixtureMonkeyOptions options);

	/**
	 * Checks whether the given type is a leaf type that should not be expanded further.
	 * <p>
	 * Leaf types (e.g., Java standard library types, Kotlin standard types) are terminal nodes
	 * in the object tree and their internal fields should not be traversed.
	 *
	 * @param type the class to check
	 * @return true if the type is a leaf type
	 */
	boolean isLeafType(Class<?> type);

	/**
	 * Returns the cross-call node metadata cache for assembly optimization.
	 * <p>
	 * This cache maps JvmNode instances to their derived metadata (Property, resolvers, etc.)
	 * across multiple assembly calls. Returns null if caching is not supported or disabled.
	 *
	 * @return the node metadata cache, or null if not available
	 */
	@Nullable
	ConcurrentHashMap<?, ?> getNodeMetadataCache();
}
