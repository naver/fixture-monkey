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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.navercorp.objectfarm.api.nodecandidate.ContainerElementCreationMethod;
import com.navercorp.objectfarm.api.nodecandidate.CreationMethod;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.Types;

/**
 * A {@link JvmContainerNodeGenerator} implementation for generating element nodes
 * for linear containers such as List and Set.
 * <p>
 * This generator creates child nodes for each element in a Collection type,
 * using the ContainerSizeResolver from the context to determine the number of elements.
 */
public final class JavaLinearContainerElementNodeGenerator implements JvmContainerNodeGenerator {

	/**
	 * Creates a new generator for linear container elements.
	 */
	public JavaLinearContainerElementNodeGenerator() {
	}

	@Override
	public boolean isSupported(JvmType containerType) {
		Class<?> rawType = containerType.getRawType();
		boolean collectionType = Types.isAssignable(rawType, Collection.class)
			&& !Types.isAssignable(rawType, Map.class)
			&& !rawType.isArray();

		boolean singleTypeVariable = containerType.getTypeVariables().size() == 1;
		return collectionType && singleTypeVariable;
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
		JvmType elementType = containerType.getTypeVariables().get(0);

		int containerSize = sizeResolver.resolveContainerSize(containerType);

		List<JvmNode> elementNodes = new ArrayList<>();
		for (int i = 0; i < containerSize; i++) {
			CreationMethod creationMethod = new ContainerElementCreationMethod(i);
			elementNodes.add(new JavaNode(elementType, "[" + i + "]", i, creationMethod));
		}

		return Collections.unmodifiableList(elementNodes);
	}
}
