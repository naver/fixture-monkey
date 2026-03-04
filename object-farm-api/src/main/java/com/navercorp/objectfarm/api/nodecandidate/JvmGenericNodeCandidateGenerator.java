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

import com.navercorp.objectfarm.api.node.GenericTypeResolver;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * A specialized {@link JvmNodeCandidateGenerator} for generating node candidates with customizable generic types.
 * <p>
 * This interface extends {@link JvmNodeCandidateGenerator} to handle JVM types where
 * generic type parameters need to be resolved or modified. It allows users to control
 * what concrete types should be used for generic type parameters during node generation.
 * <p>
 * Implementations of this interface use a {@link GenericTypeResolver} to resolve types to their
 * desired generic type parameters, then delegate the actual node candidate generation to the
 * appropriate generator for that resolved type.
 *
 * @see JvmNodeCandidateGenerator
 * @see GenericTypeResolver
 */
public interface JvmGenericNodeCandidateGenerator extends JvmNodeCandidateGenerator {
	/**
	 * Generates child node candidates for the given type using the default generic type resolver.
	 * <p>
	 * This default implementation delegates to {@link #generateNextNodeCandidates(JvmType, GenericTypeResolver)}
	 * using the resolver returned by {@link #getGenericTypeResolver()}.
	 *
	 * @param jvmType the type to generate node candidates for
	 * @return a list of generated child node candidates
	 */
	@Override
	default List<JvmNodeCandidate> generateNextNodeCandidates(JvmType jvmType) {
		return generateNextNodeCandidates(jvmType, getGenericTypeResolver());
	}

	/**
	 * Returns the generic type resolver that modifies generic type parameters.
	 * <p>
	 * The resolver is used to determine what concrete types should be used for generic type parameters
	 * when generating node candidates.
	 *
	 * @return the {@link GenericTypeResolver} that resolves types to desired generic type parameters
	 */
	GenericTypeResolver getGenericTypeResolver();

	/**
	 * Generates child node candidates for the given type with a custom generic type resolver.
	 * <p>
	 * This method allows users to specify a custom generic type resolver to control which concrete
	 * types should be used for generic type parameters during generation. This is useful when users
	 * want to customize generic type parameters rather than using the default resolver.
	 * <p>
	 * The provided resolver overrides the default resolver returned by {@link #getGenericTypeResolver()}
	 * for this specific generation operation.
	 *
	 * @param jvmType the type to generate node candidates for
	 * @param genericTypeResolver the custom generic type resolver to use for modifying generic type parameters
	 * @return a list of generated child node candidates
	 */
	List<JvmNodeCandidate> generateNextNodeCandidates(JvmType jvmType, GenericTypeResolver genericTypeResolver);
}

