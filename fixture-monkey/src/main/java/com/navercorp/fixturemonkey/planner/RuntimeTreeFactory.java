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

package com.navercorp.fixturemonkey.planner;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.objectfarm.api.tree.JvmNodeTree;
import com.navercorp.objectfarm.api.tree.PathResolverContext;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Builds {@link JvmNodeTree}s on demand during assembly.
 * <p>
 * Used when assembly encounters a deferred shape — e.g. when an interface/abstract type
 * resolves to a concrete implementation, or when an anonymous proxy must be generated for an
 * interface — and a tree needs to be expanded for that runtime-determined type.
 * <p>
 * This is distinct from {@link AssemblyPlanner}: planning happens once up-front, while
 * runtime tree construction happens during assembly as concrete types are resolved.
 *
 * @see AssemblyPlanner
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
public interface RuntimeTreeFactory {
	/**
	 * Creates a {@link JvmNodeTree} for the given concrete type.
	 * <p>
	 * Used when generating with interface/abstract implementations to obtain
	 * the proper tree structure with child nodes. The underlying candidate tree
	 * may be cached, but the JvmNodeTree is created fresh each time so container
	 * sizes vary across calls.
	 *
	 * @param concreteType the concrete JvmType to create the tree for
	 * @param options      the FixtureMonkey options for tree building
	 * @return the JvmNodeTree, or null if not available
	 */
	@Nullable
	JvmNodeTree createConcreteNodeTree(JvmType concreteType, FixtureMonkeyOptions options);

	/**
	 * Creates a {@link JvmNodeTree} for an anonymous instance of the given interface/abstract type.
	 * <p>
	 * The tree has children derived from no-argument methods of the interface,
	 * enabling anonymous proxy generation with path-based value customization.
	 * Always created fresh (no caching). The {@code resolverContext} is the same instance produced
	 * during planning and must be passed in by the caller so anonymous-tree creation sees the
	 * same resolution decisions.
	 *
	 * @param interfaceType   the interface/abstract JvmType to create the tree for
	 * @param options         the FixtureMonkey options for tree building
	 * @param resolverContext the path resolver context produced during planning
	 * @return the JvmNodeTree for anonymous instance generation, or null if not available
	 */
	@Nullable
	JvmNodeTree createAnonymousNodeTree(
		JvmType interfaceType,
		FixtureMonkeyOptions options,
		PathResolverContext resolverContext
	);
}
