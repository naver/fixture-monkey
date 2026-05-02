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

/**
 * Represents a Map entry node in the JVM node structure.
 * <p>
 * JvmMapNode extends JvmNode to provide access to key and value nodes
 * that represent a Map entry via {@link #getKeyNode()} and {@link #getValueNode()}.
 * <p>
 * This interface ensures 1:1 topology mapping between JvmNodeCandidateTree
 * and JvmNodeTree by treating Map entries as single nodes with key/value as children.
 */
public interface JvmMapNode extends JvmNode {
	/**
	 * Returns the key node of this map entry.
	 *
	 * @return the JvmNode representing the key
	 */
	JvmNode getKeyNode();

	/**
	 * Returns the value node of this map entry.
	 *
	 * @return the JvmNode representing the value
	 */
	JvmNode getValueNode();
}
