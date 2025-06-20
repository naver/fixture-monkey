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

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.nodecandidate.CreationMethod;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Represents a unified node structure for all JVM types in object graph operations.
 * <p>
 * JvmNode serves as the primary building block for representing object structures,
 * regardless of the underlying type complexity. It provides a consistent interface
 * for navigating and manipulating object graphs across various operations such as
 * object creation, validation, analysis, and transformation.
 * <p>
 * Key characteristics:
 * <ul>
 *   <li>Type-agnostic representation - all types are unified under JvmNode</li>
 *   <li>Hierarchical structure with parent-child relationships managed by JvmNodeTree</li>
 *   <li>Maintains type safety through JvmType association</li>
 * </ul>
 * <p>
 * Child nodes are managed by {@link com.navercorp.objectfarm.api.tree.JvmNodeTree},
 * which provides the tree structure and parent-child relationships.
 */
public interface JvmNode {
	/**
	 * Returns the concrete JVM type that this node represents.
	 * <p>
	 * Unlike NodeCandidate which may contain abstract types (e.g., List interface),
	 * JvmNode must always return a concrete, instantiable type (e.g., ArrayList class).
	 * This ensures that the node represents a specific implementation that can be
	 * directly instantiated during object creation.
	 * 
	 * @return the concrete JvmType associated with this node
	 */
	JvmType getConcreteType();

	/**
	 * Returns the name of this node, if available.
	 * The name typically represents the field name, map key, or other identifier
	 * that distinguishes this node within its parent context.
	 *
	 * @return the node name, or null if this node has no specific name
	 */
	@Nullable
	String getNodeName();

	/**
	 * Returns the index of this node within its parent container.
	 * <p>
	 * This is only applicable for nodes that are elements of a container
	 * (e.g., elements in a List, Set, Array, or Map entries). For non-container
	 * elements, this will return null.
	 *
	 * @return the index within the parent container, or null if not a container element
	 */
	@Nullable
	default Integer getIndex() {
		return null;
	}

	/**
	 * Returns how this property will be created/set during object instantiation.
	 * <p>
	 * This metadata is propagated from the JvmNodeCandidate during promotion
	 * and indicates the creation strategy used for this node.
	 *
	 * @return the creation method, or null if not specified
	 */
	@Nullable
	default CreationMethod getCreationMethod() {
		return null;
	}
}
