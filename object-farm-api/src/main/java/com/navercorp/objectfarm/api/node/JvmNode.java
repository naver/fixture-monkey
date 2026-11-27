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
	 * Returns the type the node is built with: its children are expanded from this type,
	 * and the object is created as this type.
	 * <p>
	 * It is not always instantiable:
	 * <ul>
	 *   <li>The root node has the type the tree is built for, as it is given. Choosing an implementation
	 *   for it is up to the one that builds the tree.</li>
	 *   <li>A child node has the type resolved for its path, such as an implementation of a container
	 *   interface (e.g., ArrayList for List).</li>
	 *   <li>An interface or abstract type that is not a container is kept as it is declared,
	 *   until an implementation is chosen at assembly time.</li>
	 * </ul>
	 *
	 * @return the type the node is built with
	 */
	JvmType getConcreteType();

	/**
	 * Returns the type the node is declared with where it is referenced: a field's type, a container's element
	 * type, the type a sample was requested with. It differs from {@link #getConcreteType()} when an
	 * implementation was chosen for an interface or abstract declared type. Matching what a node is (a
	 * registered matcher, a scope) uses this type; building the node uses the concrete one.
	 *
	 * @return the declared type
	 */
	default JvmType getDeclaredType() {
		return getConcreteType();
	}

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
