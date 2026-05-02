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

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Promotes interface type node candidates to concrete JvmNode instances.
 * <p>
 * This promoter uses the {@link InterfaceResolver} from the context to resolve
 * interface types to their concrete implementations during node promotion.
 * <p>
 * If the context provides an InterfaceResolver, it will be used to determine the
 * concrete type. If no resolver is available or it returns null, the original
 * interface type is used (which may cause issues during instantiation).
 */
public final class JavaInterfaceNodePromoter implements JvmNodePromoter {

	public JavaInterfaceNodePromoter() {
	}

	@Override
	public boolean canPromote(JvmNodeCandidate node) {
		return Modifier.isInterface(node.getType().getRawType().getModifiers());
	}

	@Override
	public List<JvmNode> promote(JvmNodeCandidate node, JvmNodeContext context) {
		JvmType nodeType = node.getType();

		// Try to resolve interface to concrete type using context's resolver
		InterfaceResolver resolver = context.getInterfaceResolver();
		if (resolver != null) {
			// Recursively resolve type until we get a concrete type
			JvmType resolvedType = resolveRecursively(nodeType, resolver, context.getMaxRecursionDepth());
			if (resolvedType != null) {
				nodeType = resolvedType;
			}
		}

		return Collections.singletonList(new JavaNode(
			nodeType,
			node.getName(),
			null,
			node.getCreationMethod()
		));
	}

	/**
	 * Recursively resolves a type using the interface resolver until a concrete type is found.
	 * This handles chained type resolution (e.g., Collection -> List -> LinkedList).
	 *
	 * @param type the type to resolve
	 * @param resolver the interface resolver
	 * @param maxDepth maximum number of resolution iterations to prevent infinite loops
	 * @return the resolved concrete type, or the input type if resolution fails
	 */
	private JvmType resolveRecursively(JvmType type, InterfaceResolver resolver, int maxDepth) {
		JvmType currentType = type;
		Set<Class<?>> visited = new HashSet<>();

		for (int depth = 0; depth < maxDepth; depth++) {
			Class<?> rawType = currentType.getRawType();

			// Cycle detection
			if (!visited.add(rawType)) {
				return currentType;
			}

			JvmType resolved = resolver.resolve(currentType);
			if (resolved == null) {
				return currentType;
			}

			// Check if the resolved type is concrete (not interface/abstract)
			int modifiers = resolved.getRawType().getModifiers();
			if (!Modifier.isInterface(modifiers) && !Modifier.isAbstract(modifiers)) {
				return resolved;
			}

			currentType = resolved;
		}

		return currentType;
	}
}
