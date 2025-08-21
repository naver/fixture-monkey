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

import java.util.List;

import javax.annotation.Nullable;

import com.navercorp.objectfarm.api.nodepromoter.JvmNodePromoter;
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
 *   <li>Hierarchical structure with parent-child relationships</li>
 *   <li>Child nodes are created on-demand through NodePromoter</li>
 *   <li>Child nodes can be regenerated and replaced as needed</li>
 *   <li>Maintains type safety through JvmType association</li>
 * </ul>
 * <p>
 * The on-demand child creation approach allows for:
 * <ul>
 *   <li>Lazy evaluation of child nodes</li>
 *   <li>Flexible node promotion through NodePromoter</li>
 *   <li>Efficient memory usage through delayed instantiation</li>
 *   <li>Support for complex object graph modifications</li>
 * </ul>
 */
public interface JvmNode {
	/**
	 * Returns the JVM type that this node represents.
	 * 
	 * @return the JvmType associated with this node
	 */
	JvmType getType();

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
	 * Returns a list of child nodes by promoting candidates using the provided promoter.
	 * This method enables on-demand child creation, allowing for lazy evaluation
	 * and efficient memory usage. Child nodes can be regenerated and replaced
	 * as needed during various operations including creation, validation, and analysis.
	 * 
	 * @param promoter the NodePromoter to use for converting candidates to actual nodes
	 * @return a list of child JvmNode instances
	 */
	List<JvmNode> getChildren(JvmNodePromoter promoter);
}
