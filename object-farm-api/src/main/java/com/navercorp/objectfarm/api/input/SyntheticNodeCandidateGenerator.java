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

package com.navercorp.objectfarm.api.input;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.navercorp.objectfarm.api.nodecandidate.CreationMethod;
import com.navercorp.objectfarm.api.nodecandidate.JavaNodeCandidate;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidateGenerator;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Generator for creating node candidates from {@link SyntheticJvmType}.
 * <p>
 * This generator converts the members of a synthetic type into
 * {@link JvmNodeCandidate} instances, allowing synthetic types to be
 * used within the existing node candidate tree infrastructure.
 * <p>
 * Example usage:
 * <pre>{@code
 * SyntheticNodeCandidateGenerator generator = new SyntheticNodeCandidateGenerator();
 * SyntheticJvmType type = SyntheticJvmType.builder("User")
 *     .member("name", new JavaType(String.class))
 *     .build();
 *
 * List<JvmNodeCandidate> candidates = generator.generateNextNodeCandidates(type);
 * }</pre>
 */
public final class SyntheticNodeCandidateGenerator implements JvmNodeCandidateGenerator {
	@Override
	public List<JvmNodeCandidate> generateNextNodeCandidates(JvmType jvmType) {
		if (!(jvmType instanceof SyntheticJvmType)) {
			return Collections.emptyList();
		}

		SyntheticJvmType syntheticType = (SyntheticJvmType)jvmType;

		return syntheticType.getMembers().stream()
			.map(member -> {
				CreationMethod creationMethod = new SyntheticMemberCreationMethod(
					member.getCreationMethodType(),
					member.getName()
				);
				return new JavaNodeCandidate(member.getType(), member.getName(), creationMethod);
			})
			.collect(Collectors.toList());
	}

	@Override
	public boolean isSupported(JvmType jvmType) {
		return jvmType instanceof SyntheticJvmType;
	}
}
