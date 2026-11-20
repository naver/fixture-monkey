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
import java.util.Objects;

import com.navercorp.objectfarm.api.nodecandidate.ContainerElementCreationMethod;
import com.navercorp.objectfarm.api.nodecandidate.JavaNodeCandidateFactory;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.ReflectiveJvmType;

/**
 * A {@link JvmContainerNodeGenerator} implementation for generating element candidates
 * for array types.
 * <p>
 * This generator creates a candidate for each element in an array,
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
	public List<JvmNodeCandidate> generateContainerElements(JvmNode containerNode, JvmNodeContext context) {
		return generateContainerElements(containerNode, context, context.getContainerSizeResolver());
	}

	@Override
	@SuppressWarnings("argument")
	public List<JvmNodeCandidate> generateContainerElements(
		JvmNode containerNode,
		JvmNodeContext context,
		ContainerSizeResolver sizeResolver
	) {
		JvmType containerType = containerNode.getConcreteType();
		Class<?> componentType = Objects.requireNonNull(containerType.getRawType().getComponentType());

		// Preserve generic type variables from the container type
		// For GenericImplementation<String>[], the type variables [String] should be preserved
		JvmType elementType = new ReflectiveJvmType(
			componentType,
			containerType.getTypeVariables(),
			containerType.getAnnotations()
		);

		int arraySize = sizeResolver.resolveContainerSize(containerType);

		List<JvmNodeCandidate> elementCandidates = new ArrayList<>();
		for (int i = 0; i < arraySize; i++) {
			elementCandidates.add(JavaNodeCandidateFactory.INSTANCE.create(
				elementType,
				"[" + i + "]",
				new ContainerElementCreationMethod(i)
			));
		}

		return Collections.unmodifiableList(elementCandidates);
	}
}
