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
import java.util.Map;

import com.navercorp.objectfarm.api.nodecandidate.ContainerElementCreationMethod;
import com.navercorp.objectfarm.api.nodecandidate.JavaMapNodeCandidate;
import com.navercorp.objectfarm.api.nodecandidate.JavaNodeCandidateFactory;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.Types;

/**
 * A {@link JvmContainerNodeGenerator} implementation for generating element candidates
 * for Map types.
 * <p>
 * This generator creates a map element candidate with a key and a value candidate for each entry in a Map,
 * using the ContainerSizeResolver from the context to determine the number of entries.
 */
public final class JavaMapElementNodeGenerator implements JvmContainerNodeGenerator {

	/**
	 * Creates a new generator for map elements.
	 */
	public JavaMapElementNodeGenerator() {
	}

	@Override
	public boolean isSupported(JvmType containerType) {
		return Types.isAssignable(containerType.getRawType(), Map.class)
			&& containerType.getTypeVariables().size() == 2;
	}

	@Override
	public List<JvmNodeCandidate> generateContainerElements(JvmNode containerNode, JvmNodeContext context) {
		return generateContainerElements(containerNode, context, context.getContainerSizeResolver());
	}

	@Override
	public List<JvmNodeCandidate> generateContainerElements(
		JvmNode containerNode,
		JvmNodeContext context,
		ContainerSizeResolver sizeResolver
	) {
		JvmType containerType = containerNode.getConcreteType();
		List<? extends JvmType> typeVariables = containerType.getTypeVariables();
		JvmType keyType = typeVariables.get(0);
		JvmType valueType = typeVariables.get(1);

		int mapSize = sizeResolver.resolveContainerSize(containerType);

		List<JvmNodeCandidate> elementCandidates = new ArrayList<>();
		for (int i = 0; i < mapSize; i++) {
			elementCandidates.add(new JavaMapNodeCandidate(
				containerType,
				"[" + i + "]",
				JavaNodeCandidateFactory.INSTANCE.create(keyType, "key", null),
				JavaNodeCandidateFactory.INSTANCE.create(valueType, "value", null),
				new ContainerElementCreationMethod(i)
			));
		}

		return Collections.unmodifiableList(elementCandidates);
	}
}
