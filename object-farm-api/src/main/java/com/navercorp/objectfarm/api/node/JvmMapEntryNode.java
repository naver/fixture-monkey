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
 * Represents a Map.Entry node in the JVM node structure.
 * <p>
 * JvmMapEntryNode extends JvmNode to provide access to key and value nodes
 * that represent a standalone Map.Entry via {@link #getKeyNode()} and {@link #getValueNode()}.
 * <p>
 * This is different from JvmMapNode which represents a Map container.
 * JvmMapEntryNode is used when Map.Entry itself is a field type.
 */
public interface JvmMapEntryNode extends JvmNode {
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
