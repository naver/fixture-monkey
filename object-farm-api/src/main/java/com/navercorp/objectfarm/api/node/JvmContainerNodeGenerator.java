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

import com.navercorp.objectfarm.api.type.JvmType;

/**
 * A generator interface for creating container element nodes (JvmNode instances)
 * for container types such as arrays, collections, and maps.
 * <p>
 * This interface generates actual JvmNode instances dynamically at the JvmNode level,
 * enabling different container sizes based on runtime customization.
 * <p>
 * Implementations of this interface are responsible for:
 * <ul>
 *   <li>Generating element nodes for their respective container types</li>
 *   <li>Determining the number of elements based on the ContainerSizeResolver from context</li>
 *   <li>Creating properly typed element nodes</li>
 * </ul>
 *
 * @see JvmNodeContext#getContainerNodeGenerators()
 */
public interface JvmContainerNodeGenerator {
	/**
	 * Checks whether this generator supports the given container type.
	 *
	 * @param containerType the container type to check
	 * @return true if this generator can generate elements for the container type
	 */
	boolean isSupported(JvmType containerType);

	/**
	 * Generates container element nodes for the given container node.
	 * <p>
	 * The number of elements generated is typically determined by the
	 * {@link ContainerSizeResolver}
	 * available in the context.
	 *
	 * @param containerNode the container node to generate elements for
	 * @param context the node context containing resolvers and generators
	 * @return a list of generated element nodes
	 */
	List<JvmNode> generateContainerElements(JvmNode containerNode, JvmNodeContext context);

	/**
	 * Generates container element nodes for the given container node using the specified size resolver.
	 * <p>
	 * This method allows explicit control over the container size resolver, enabling
	 * path-based customization of container sizes during tree transformation.
	 *
	 * @param containerNode the container node to generate elements for
	 * @param context the node context containing generators and other resolvers
	 * @param sizeResolver the container size resolver to use
	 * @return a list of generated element nodes
	 */
	default List<JvmNode> generateContainerElements(
		JvmNode containerNode,
		JvmNodeContext context,
		ContainerSizeResolver sizeResolver
	) {
		return generateContainerElements(containerNode, context);
	}
}
