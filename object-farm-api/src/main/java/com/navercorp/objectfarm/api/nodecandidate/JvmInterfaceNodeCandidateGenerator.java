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

import java.util.List;

import com.navercorp.objectfarm.api.node.InterfaceResolver;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * A specialized {@link JvmNodeCandidateGenerator} for generating node candidates for interface types.
 * <p>
 * This interface extends {@link JvmNodeCandidateGenerator} to handle JVM interface types where
 * different implementations may produce different numbers of child nodes. Unlike container types
 * that have predictable structures, interface types may have varying node structures depending on
 * their concrete implementation.
 * <p>
 * Implementations of this interface use an {@link InterfaceResolver} to resolve interface types to their
 * concrete implementation types, then delegate the actual node candidate generation to the
 * appropriate generator for that implementation.
 *
 * @see JvmNodeCandidateGenerator
 * @see InterfaceResolver
 */
public interface JvmInterfaceNodeCandidateGenerator extends JvmNodeCandidateGenerator {
	/**
	 * Generates child node candidates for the given interface type using the default interface resolver.
	 * <p>
	 * This default implementation delegates to
	 * {@link #generateNextNodeCandidates(JvmType, InterfaceResolver)}
	 * using the resolver returned by {@link #getInterfaceResolver()}.
	 *
	 * @param jvmType the interface type to generate node candidates for
	 * @return a list of generated child node candidates for the interface
	 */
	@Override
	default List<JvmNodeCandidate> generateNextNodeCandidates(JvmType jvmType) {
		return generateNextNodeCandidates(jvmType, getInterfaceResolver());
	}

	/**
	 * Returns the interface resolver that maps interface types to concrete implementation types.
	 * <p>
	 * The resolver is used to determine which concrete type should be used when generating node candidates
	 * for an interface type.
	 *
	 * @return the {@link InterfaceResolver} that resolves interface types to implementation types
	 */
	InterfaceResolver getInterfaceResolver();

	/**
	 * Generates child node candidates for the given interface type with a custom interface resolver.
	 * <p>
	 * This method allows users to specify a custom interface resolver to control which concrete
	 * implementation should be used during generation. This is useful when users want to resolve
	 * interfaces to specific implementations rather than using the default resolver.
	 * <p>
	 * The provided resolver overrides the default resolver returned by {@link #getInterfaceResolver()}
	 * for this specific generation operation.
	 *
	 * @param jvmType the interface type to generate node candidates for
	 * @param interfaceResolver the custom interface resolver to use for mapping interface to concrete types
	 * @return a list of generated child node candidates for the interface
	 */
	List<JvmNodeCandidate> generateNextNodeCandidates(JvmType jvmType, InterfaceResolver interfaceResolver);
}
