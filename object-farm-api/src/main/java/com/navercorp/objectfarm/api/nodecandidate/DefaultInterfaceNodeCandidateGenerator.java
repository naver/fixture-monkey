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

package com.navercorp.objectfarm.api.nodecandidate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.navercorp.objectfarm.api.node.InterfaceResolver;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Default implementation of {@link JvmInterfaceNodeCandidateGenerator} that handles interface types where
 * different implementations may produce different numbers of child nodes.
 * <p>
 * This generator is specifically designed for interface types that are not containers
 * but require implementation-specific node generation strategies. Unlike container types
 * that have predictable structures, interface types may have varying node structures
 * depending on their concrete implementation.
 * </p>
 * <p>
 * The generator uses a Function to resolve the interface type to a concrete
 * implementation type, then delegates the actual node candidate generation to
 * another JvmNodeCandidateGenerator that understands the specific implementation.
 * </p>
 * <p>
 * Example usage for interface types with implementation-specific node counts:
 * <pre>
 * InterfaceResolver resolver = interfaceType -&gt; {
 *     if (interfaceType.getRawType() == Comparable.class) {
 *         return JvmTypes.of(String.class); // String has different fields than Integer
 *     }
 *     if (interfaceType.getRawType() == Serializable.class) {
 *         return JvmTypes.of(HashMap.class, interfaceType.getTypeVariables());
 *     }
 *     return interfaceType;
 * };
 *
 * JvmNodeCandidateGenerator delegate = new JavaFieldNodeCandidateGenerator();
 * DefaultInterfaceNodeCandidateGenerator generator = new DefaultInterfaceNodeCandidateGenerator(resolver, delegate);
 * </pre>
 */
public final class DefaultInterfaceNodeCandidateGenerator implements JvmInterfaceNodeCandidateGenerator {
	private final InterfaceResolver interfaceResolver;
	private final JvmNodeCandidateGenerator delegate;

	/**
	 * Creates a new DefaultInterfaceNodeCandidateGenerator with the specified interface resolver
	 * and delegate generator.
	 *
	 * @param interfaceResolver a resolver that resolves interface types to concrete implementation types
	 * @param delegate the generator to delegate actual node candidate creation to
	 */
	public DefaultInterfaceNodeCandidateGenerator(
		InterfaceResolver interfaceResolver,
		JvmNodeCandidateGenerator delegate
	) {
		this.interfaceResolver = interfaceResolver;
		this.delegate = delegate;
	}

	@Override
	public InterfaceResolver getInterfaceResolver() {
		return interfaceResolver;
	}

	@Override
	public List<JvmNodeCandidate> generateNextNodeCandidates(JvmType jvmType, InterfaceResolver interfaceResolver) {
		JvmType implementationType = interfaceResolver.resolve(jvmType);

		// If resolver returns null, it means the type cannot be resolved
		if (implementationType == null) {
			return Collections.emptyList();
		}

		return delegate.generateNextNodeCandidates(implementationType);
	}

	@Override
	public boolean isSupported(JvmType jvmType) {
		Class<?> rawType = jvmType.getRawType();

		// Container types are handled by dedicated generators
		if (Collection.class.isAssignableFrom(rawType)
			|| Map.class.isAssignableFrom(rawType)
			|| rawType.isArray()) {
			return false;
		}

		return rawType.isInterface();
	}
}

