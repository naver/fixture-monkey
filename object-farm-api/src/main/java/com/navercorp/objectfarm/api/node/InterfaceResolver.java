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

import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.type.JvmType;

/**
 * A functional interface for resolving interface types to their concrete implementation types.
 * <p>
 * This resolver is used to determine which concrete type should be used when generating
 * nodes for an interface type.
 * <p>
 * For deterministic resolution when multiple implementations are available,
 * implementations should store a seed in their constructor and use it internally.
 * <p>
 * Example implementations:
 * <pre>
 * InterfaceResolver resolver = interfaceType -&gt; {
 *     if (interfaceType.getRawType() == List.class) {
 *         return JvmTypes.of(ArrayList.class, interfaceType.getTypeVariables());
 *     }
 *     if (interfaceType.getRawType() == Map.class) {
 *         return JvmTypes.of(HashMap.class, interfaceType.getTypeVariables());
 *     }
 *     return interfaceType;
 * };
 * </pre>
 */
@FunctionalInterface
public interface InterfaceResolver extends NodeCustomizer {
	/**
	 * Resolves an interface type to its concrete implementation type.
	 *
	 * @param interfaceType the interface type to resolve
	 * @return the concrete implementation type to use for node generation, or null if not resolvable
	 */
	@Nullable
	JvmType resolve(JvmType interfaceType);


	/**
	 * Resolves an interface type to all possible concrete implementation types.
	 * <p>
	 * This method is used during pre-building to cache all possible implementation subtrees,
	 * enabling per-sample selection of implementations during assembly.
	 *
	 * @param interfaceType the interface type to resolve
	 * @return list of all candidate concrete types, or empty list if not resolvable
	 */
	default List<JvmType> resolveAll(JvmType interfaceType) {
		JvmType resolved = resolve(interfaceType);
		return resolved != null ? Collections.singletonList(resolved) : Collections.emptyList();
	}
}

