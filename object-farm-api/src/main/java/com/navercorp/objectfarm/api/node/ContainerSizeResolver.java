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

import com.navercorp.objectfarm.api.type.JvmType;

/**
 * A functional interface for resolving the size of container types during node generation.
 * <p>
 * This resolver is used to determine the size of containers (arrays, lists, sets, maps, etc.)
 * when generating nodes. It allows users to control the size of generated containers.
 * <p>
 * For deterministic random size generation, implementations should store a seed in
 * their constructor and use it internally.
 * <p>
 * Example implementations:
 * <pre>
 * // Fixed size resolver
 * ContainerSizeResolver fixedSize = containerType -&gt; 5;
 *
 * // Type-specific size resolver
 * ContainerSizeResolver typeSpecific = containerType -&gt; {
 *     if (containerType.getRawType() == List.class) {
 *         return 10;
 *     }
 *     if (containerType.getRawType() == Set.class) {
 *         return 5;
 *     }
 *     return 3;
 * };
 *
 * // Deterministic random size resolver (see RandomContainerSizeResolver)
 * ContainerSizeResolver randomSize = new RandomContainerSizeResolver(seed, 1, 10);
 * </pre>
 */
@FunctionalInterface
public interface ContainerSizeResolver extends NodeCustomizer {
	/**
	 * Resolves the size of a container based on the given container type.
	 * The size is determined by the implementation strategy to ensure appropriate results.
	 *
	 * @param containerType the type of the container
	 * @return the resolved size of the container
	 */
	int resolveContainerSize(JvmType containerType);
}

