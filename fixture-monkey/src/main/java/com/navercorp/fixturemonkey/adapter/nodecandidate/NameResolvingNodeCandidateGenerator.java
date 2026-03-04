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

package com.navercorp.fixturemonkey.adapter.nodecandidate;

import java.util.ArrayList;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.nodecandidate.JavaNodeCandidateFactory;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidateGenerator;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Wraps a {@link JvmNodeCandidateGenerator} to apply name resolution to generated candidates.
 * <p>
 * This generator delegates candidate generation to the wrapped generator,
 * then renames each candidate using the provided {@code nameResolver}.
 * This ensures node names in the tree match user-facing names
 * (e.g., {@code @JsonProperty} annotations) regardless of the underlying generator.
 * <p>
 * The resolver takes the parent type, child type, and original candidate name
 * to look up the appropriate {@code PropertyNameResolver} and resolve the field name.
 *
 * @since 1.1.17
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
public final class NameResolvingNodeCandidateGenerator implements JvmNodeCandidateGenerator {
	private final JvmNodeCandidateGenerator delegate;
	private final ChildNameResolver nameResolver;

	/**
	 * @param delegate the generator to wrap
	 * @param nameResolver resolves a child candidate's name using parent type, child type, and original name
	 */
	public NameResolvingNodeCandidateGenerator(JvmNodeCandidateGenerator delegate, ChildNameResolver nameResolver) {
		this.delegate = delegate;
		this.nameResolver = nameResolver;
	}

	@Override
	public List<JvmNodeCandidate> generateNextNodeCandidates(JvmType jvmType) {
		List<JvmNodeCandidate> candidates = delegate.generateNextNodeCandidates(jvmType);
		if (candidates.isEmpty()) {
			return candidates;
		}

		List<JvmNodeCandidate> result = new ArrayList<>(candidates.size());
		for (JvmNodeCandidate candidate : candidates) {
			result.add(resolveCandidate(jvmType, candidate));
		}
		return result;
	}

	@Override
	public boolean isSupported(JvmType jvmType) {
		return delegate.isSupported(jvmType);
	}

	@Override
	public boolean isSupported(JvmType jvmType, com.navercorp.objectfarm.api.node.JvmNodeContext context) {
		return delegate.isSupported(jvmType, context);
	}

	private JvmNodeCandidate resolveCandidate(JvmType parentType, JvmNodeCandidate candidate) {
		@Nullable
		String resolvedName = nameResolver.resolve(parentType, candidate.getType(), candidate.getName());
		if (resolvedName == null || resolvedName.equals(candidate.getName())) {
			return candidate;
		}
		return JavaNodeCandidateFactory.INSTANCE.create(
			candidate.getType(),
			resolvedName,
			candidate.getCreationMethod()
		);
	}

	/**
	 * Resolves a child candidate's name.
	 *
	 * @since 1.1.17
	 */
	@FunctionalInterface
	public interface ChildNameResolver {
		/**
		 * @param parentType the parent type (used for matcher-based resolver lookup)
		 * @param childType the child type (carries field annotations)
		 * @param candidateName the original candidate name (Java field name)
		 * @return the resolved name, or null to keep the original
		 */
		@Nullable
		String resolve(JvmType parentType, JvmType childType, @Nullable String candidateName);
	}
}
