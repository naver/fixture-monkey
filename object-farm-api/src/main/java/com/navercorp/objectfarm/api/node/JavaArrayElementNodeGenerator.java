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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.navercorp.objectfarm.api.nodecandidate.ContainerElementCreationMethod;
import com.navercorp.objectfarm.api.nodecandidate.CreationMethod;
import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * A {@link JvmContainerNodeGenerator} implementation for generating element nodes
 * for array types.
 * <p>
 * This generator creates child nodes for each element in an array,
 * using the ContainerSizeResolver from the context to determine the array length.
 */
public final class JavaArrayElementNodeGenerator implements JvmContainerNodeGenerator {

	/**
	 * Creates a new generator for array elements.
	 */
	public JavaArrayElementNodeGenerator() {
	}

	@Override
	public boolean isSupported(JvmType containerType) {
		return containerType.getRawType().isArray();
	}

	@Override
	public List<JvmNode> generateContainerElements(JvmNode containerNode, JvmNodeContext context) {
		return generateContainerElements(containerNode, context, context.getContainerSizeResolver());
	}

	@Override
	public List<JvmNode> generateContainerElements(
		JvmNode containerNode,
		JvmNodeContext context,
		ContainerSizeResolver sizeResolver
	) {
		JvmType containerType = containerNode.getConcreteType();
		Class<?> componentType = containerType.getRawType().getComponentType();

		// Preserve generic type variables from the container type
		// For GenericImplementation<String>[], the type variables [String] should be preserved
		JvmType elementType = new JavaType(
			componentType,
			containerType.getTypeVariables(),
			containerType.getAnnotations()
		);

		int arraySize = sizeResolver.resolveContainerSize(containerType);

		List<JvmNode> elementNodes = new ArrayList<>();
		for (int i = 0; i < arraySize; i++) {
			CreationMethod creationMethod = new ContainerElementCreationMethod(i);
			elementNodes.add(new JavaNode(elementType, "[" + i + "]", i, creationMethod));
		}

		return Collections.unmodifiableList(elementNodes);
	}
}
