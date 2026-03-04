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

import java.util.Collections;
import java.util.List;

import com.navercorp.objectfarm.api.node.GenericTypeResolver;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Default implementation of {@link JvmGenericNodeCandidateGenerator} that handles types where
 * generic type parameters need to be resolved or modified during node generation.
 * <p>
 * This generator allows customization of generic type parameters before generating node candidates.
 * It uses a {@link GenericTypeResolver} to resolve or modify the type's generic parameters,
 * then delegates the actual node candidate generation to another {@link JvmNodeCandidateGenerator}
 * that understands the resolved type.
 * </p>
 * <p>
 * Example usage for modifying generic type parameters:
 * <pre>
 * GenericTypeResolver resolver = genericType -&gt; {
 *     if (genericType.getRawType() == List.class) {
 *         // Change List&lt;?&gt; to List&lt;String&gt;
 *         return JvmTypes.of(List.class, JvmTypes.of(String.class));
 *     }
 *     if (genericType.getRawType() == Map.class &amp;&amp; genericType.getTypeVariables().isEmpty()) {
 *         // Change Map&lt;?, ?&gt; to Map&lt;String, Integer&gt;
 *         return JvmTypes.of(Map.class, JvmTypes.of(String.class), JvmTypes.of(Integer.class));
 *     }
 *     return genericType;
 * };
 *
 * JvmNodeCandidateGenerator delegate = new JavaFieldNodeCandidateGenerator();
 * DefaultGenericNodeCandidateGenerator generator = new DefaultGenericNodeCandidateGenerator(resolver, delegate);
 * </pre>
 */
public final class DefaultGenericNodeCandidateGenerator implements JvmGenericNodeCandidateGenerator {
	private final GenericTypeResolver genericTypeResolver;
	private final JvmNodeCandidateGenerator delegate;

	/**
	 * Creates a new DefaultGenericNodeCandidateGenerator with the specified generic type resolver
	 * and delegate generator.
	 *
	 * @param genericTypeResolver a resolver that resolves types to types with desired generic type parameters
	 * @param delegate the generator to delegate actual node candidate creation to
	 */
	public DefaultGenericNodeCandidateGenerator(
		GenericTypeResolver genericTypeResolver,
		JvmNodeCandidateGenerator delegate
	) {
		this.genericTypeResolver = genericTypeResolver;
		this.delegate = delegate;
	}

	@Override
	public GenericTypeResolver getGenericTypeResolver() {
		return genericTypeResolver;
	}

	@Override
	public List<JvmNodeCandidate> generateNextNodeCandidates(JvmType jvmType, GenericTypeResolver genericTypeResolver) {
		JvmType resolvedType = genericTypeResolver.resolve(jvmType);

		// If resolver returns null, it means the type cannot be resolved
		if (resolvedType == null) {
			return Collections.emptyList();
		}

		return delegate.generateNextNodeCandidates(resolvedType);
	}

	@Override
	public boolean isSupported(JvmType jvmType) {
		return !jvmType.getTypeVariables().isEmpty();
	}
}

