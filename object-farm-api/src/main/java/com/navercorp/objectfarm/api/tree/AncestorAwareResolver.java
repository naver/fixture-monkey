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

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.node.JvmNode;

/**
 * Chooses what a node gets from its ancestors, so the same node can be treated differently depending on what
 * encloses it. The counterpart of {@link PathResolver}, which chooses by path.
 *
 * @param <T> what is chosen, such as a container size resolver or the node's child candidates
 */
public interface AncestorAwareResolver<T> {
	/**
	 * Returns what the node gets.
	 *
	 * @param node      the node
	 * @param ancestors its ancestor nodes, outermost first
	 * @return what the node gets, or {@code null} when this resolver does not decide for the node
	 */
	@Nullable
	T resolve(JvmNode node, List<JvmNode> ancestors);

	/**
	 * Returns whether the result depends on ancestors other than the node's parent. When it does not, every node
	 * of the same name in a parent of the same type is customized the same way, so a subtree built once can be
	 * reused at another position.
	 *
	 * @return {@code false} when the types of the node's parent and the node decide the result
	 */
	default boolean dependsOnAncestors() {
		return true;
	}
}
