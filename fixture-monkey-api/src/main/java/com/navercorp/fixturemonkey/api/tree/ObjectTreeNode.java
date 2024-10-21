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

import com.navercorp.fixturemonkey.api.property.TypeDefinition;

/**
 * In Fixture Monkey a generated JVM instance consists of {@link ObjectTreeNode}s.
 * The node has a vertical relationship, parent and child.
 */
public interface ObjectTreeNode {
	/**
	 * expands the {@link ObjectTreeNode}. In result, it generates the child {@link ObjectTreeNode}s.
	 * It generates the child {@link ObjectTreeNode}s by all {@link TypeDefinition}s.
	 * <p>
	 * It can be called multiple times with metadata generated on expanding of the parent {@link ObjectTreeNode}.
	 * <p>
	 * The leaf {@link ObjectTreeNode} does not always generate child {@link ObjectTreeNode}s.
	 */
	void expand();

	/**
	 * expands the {@link ObjectTreeNode} forcibly. In result, it always generates the child {@link ObjectTreeNode}s.
	 * It generates the child {@link ObjectTreeNode}s by all {@link TypeDefinition}s.
	 * {@code Force} means that it expands as if it were a root node, even if it is not.
	 * Unlike {@link #expand()}, it expands without metadata generated
	 * on expanding of the parent {@link ObjectTreeNode}.
	 * <p>
	 * The leaf {@link ObjectTreeNode} may generate child {@link ObjectTreeNode}s.
	 * For example, the {@link ObjectTreeNode} with a self-reference.
	 */
	void forceExpand();
}
