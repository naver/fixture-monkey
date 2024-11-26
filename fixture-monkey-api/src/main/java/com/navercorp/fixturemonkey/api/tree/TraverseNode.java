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

package com.navercorp.fixturemonkey.api.tree;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.TypeDefinition;

/**
 * A generated JVM instance consists of {@link TraverseNode}s.
 * The node has a vertical relationship, parent and child.
 * It is used to create child nodes and traverse them by expression.
 * In case of providing more functionality, such as instantiating an object, use it with {@link TraverseNodeContext}.
 */
@API(since = "1.1.4", status = Status.EXPERIMENTAL)
public interface TraverseNode {
	/**
	 * expands the {@link TraverseNode}. In result, it generates the child {@link TraverseNode}s.
	 * It generates the child {@link TraverseNode}s by all {@link TypeDefinition}s.
	 * <p>
	 * It can be called multiple times with metadata generated on expanding of the parent {@link TraverseNode},
	 * but it makes no change.
	 * <p>
	 * The leaf {@link TraverseNode} does not always generate child {@link TraverseNode}s.
	 *
	 * @return whether the node is expanded.
	 */
	boolean expand();

	/**
	 * expands the {@link TraverseNode} forcibly. In result, it always generates the child {@link TraverseNode}s.
	 * It generates the child {@link TraverseNode}s by all {@link TypeDefinition}s.
	 * {@code Force} means that it expands as if it were a root node, even if it is not.
	 * Unlike {@link #expand()}, it expands without metadata generated
	 * on expanding of the parent {@link TraverseNode}.
	 * <p>
	 * It will always append the {@link TraverseNode} nodes,
	 * not remove already created nodes unless it is not a container.
	 * The container's element nodes will shrink when the node is set by the fixed container object.
	 * <p>
	 * The leaf {@link TraverseNode} may generate child {@link TraverseNode}s.
	 * For example, the {@link TraverseNode} with a self-reference.
	 */
	void forceExpand();

	/**
	 * retrieves the metadata to traverse the tree. Some of its properties can be mutated during traversal.
	 *
	 * @return the metadata to traverse the tree
	 */
	TraverseNodeMetadata getMetadata();

	/**
	 * retrieves the parent node of this node.
	 *
	 * @return the parent node of this node or null if it is root node.
	 */
	@Nullable
	TraverseNode getParent();

	/**
	 * retrieves the child nodes of this node. Use {@link NodeList} to prevent generic type casting compile error.
	 *
	 * @return the child nodes of this node or null if not called {@link #expand()}
	 */
	@Nullable
	NodeList getChildren();
}
