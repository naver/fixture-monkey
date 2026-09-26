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

package com.navercorp.fixturemonkey.tree;

import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.tree.JvmNodeTree;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Builds {@link JvmNodeTree}s with one set of rules, whether planning or assembly asks for them: planning builds
 * the root tree with it, and assembly builds the subtrees it needs on demand.
 * <p>
 * Assembly uses it when it encounters a deferred shape — e.g. when an interface/abstract type
 * resolves to a concrete implementation, when an anonymous proxy must be generated for an
 * interface, or when a container grows to a value known only during assembly — and a tree needs
 * to be expanded for that runtime-determined type.
 * <p>
 * A tree is built with the same options, path resolvers and {@code instantiate(...)} declarations, as if it were
 * the subtree at the requested position, so a root scope's size or set on a path inside it applies as it would to
 * the root tree.
 */
@API(since = "1.2.0", status = Status.EXPERIMENTAL)
public interface NodeTreeFactory {
	/**
	 * Creates a node tree for a concrete type whose root sits at {@code path}.
	 *
	 * @param concreteType the concrete type of the tree's root
	 * @param declaredType the type the tree's root is declared with where it is referenced
	 * @param path         the path the tree's root sits at
	 * @param ancestors    the nodes above the tree's root, outermost first
	 * @return the tree, or {@code null} when this factory cannot build one
	 */
	@Nullable
	JvmNodeTree createConcreteNodeTree(
		JvmType concreteType,
		JvmType declaredType,
		PathExpression path,
		List<JvmNode> ancestors
	);

	/**
	 * Creates a node tree for a container whose root sits at {@code path} and has exactly {@code size} elements,
	 * for a container whose value only became known during assembly.
	 *
	 * @param containerType the concrete type of the container
	 * @param declaredType  the type the container is declared with where it is referenced
	 * @param path          the path the container sits at
	 * @param ancestors     the nodes above the container, outermost first
	 * @param size          the number of elements
	 * @return the tree, or {@code null} when this factory cannot build one
	 */
	@Nullable
	JvmNodeTree createContainerNodeTree(
		JvmType containerType,
		JvmType declaredType,
		PathExpression path,
		List<JvmNode> ancestors,
		int size
	);

	/**
	 * Creates a node tree for an anonymous instance of an interface/abstract type whose root sits at
	 * {@code path}, with children derived from the type's no-argument methods.
	 *
	 * @param interfaceType the interface type of the tree's root
	 * @param path          the path the tree's root sits at
	 * @param ancestors the nodes above the tree's root, outermost first
	 * @return the tree, or {@code null} when this factory cannot build one
	 */
	@Nullable
	JvmNodeTree createAnonymousNodeTree(JvmType interfaceType, PathExpression path, List<JvmNode> ancestors);
}
