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

import com.navercorp.fixturemonkey.api.property.TypeDefinition;

/**
 * In Fixture Monkey a generated JVM instance consists of {@link TraverseNode}s.
 * The node has a vertical relationship, parent and child.
 * It is used to create child nodes and traverse them by expression.
 * In case of providing more functionality, such as instantiating an object, use it with {@link TraverseNodeContext}.
 */
public interface TraverseNode {
	/**
	 * expands the {@link TraverseNode}. In result, it generates the child {@link TraverseNode}s.
	 * It generates the child {@link TraverseNode}s by all {@link TypeDefinition}s.
	 * <p>
	 * It can be called multiple times with metadata generated on expanding of the parent {@link TraverseNode}.
	 * It makes no change if the {@link TypeDefinition}s are the same.
	 * <p>
	 * The leaf {@link TraverseNode} does not always generate child {@link TraverseNode}s.
	 *
	 * @return whether the node is expanded.
	 */
	boolean expand();

	/**
	 * expands the {@link TraverseNode}. In result, it generates the child {@link TraverseNode}s.
	 * It generates the child {@link TraverseNode}s by given {@link TypeDefinition}.
	 * <p>
	 * It can be called multiple times with metadata generated on expanding of the parent {@link TraverseNode}.
	 * It makes no change if the {@link TypeDefinition} is the same.
	 * <p>
	 * The leaf {@link TraverseNode} does not always generate child {@link TraverseNode}s.
	 *
	 * @param typeDefinition the {@link TypeDefinition} to generate the child nodes.
	 * @return whether the node is expanded.
	 */
	boolean expand(TypeDefinition typeDefinition);

	/**
	 * expands the {@link TraverseNode} forcibly. In result, it always generates the child {@link TraverseNode}s.
	 * It generates the child {@link TraverseNode}s by all {@link TypeDefinition}s.
	 * {@code Force} means that it expands as if it were a root node, even if it is not.
	 * Unlike {@link #expand()}, it expands without metadata generated
	 * on expanding of the parent {@link TraverseNode}.
	 * <p>
	 * The leaf {@link TraverseNode} may generate child {@link TraverseNode}s.
	 * For example, the {@link TraverseNode} with a self-reference.
	 */
	void forceExpand();

	/**
	 * expands the {@link TraverseNode} forcibly. In result, it always generates the child {@link TraverseNode}s.
	 * It generates the child {@link TraverseNode}s by given {@link TypeDefinition}.
	 * {@code Force} means that it expands as if it were a root node, even if it is not.
	 * Unlike {@link #expand()}, it expands without metadata generated
	 * on expanding of the parent {@link TraverseNode}.
	 * <p>
	 * The leaf {@link TraverseNode} may generate child {@link TraverseNode}s.
	 * For example, the {@link TraverseNode} with a self-reference.
	 */
	void forceExpand(TypeDefinition typeDefinition);

	TraverseNodeMetadata getMetadata();

	NodeList resolveChildren();

	@Nullable
	TraverseNode getParent();

	NodeList getChildren();
}
