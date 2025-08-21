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

package com.navercorp.objectfarm.api.nodecontext;

import java.util.List;

import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidateGenerator;
import com.navercorp.objectfarm.api.nodepromoter.JvmNodePromoter;

public final class JavaNodeContext implements JvmNodeContext {
	private final long seed;
	private final List<JvmNodePromoter> nodeResolvers;
	private final List<JvmNodeCandidateGenerator> candidateNodeGenerators;

	public JavaNodeContext(
		long seed,
		List<JvmNodePromoter> nodeResolvers,
		List<JvmNodeCandidateGenerator> candidateNodeGenerators
	) {
		this.seed = seed;
		this.nodeResolvers = nodeResolvers;
		this.candidateNodeGenerators = candidateNodeGenerators;
	}

	@Override
	public long getSeed() {
		return this.seed;
	}

	@Override
	public List<JvmNodePromoter> getNodeResolvers() {
		return this.nodeResolvers;
	}

	@Override
	public List<JvmNodeCandidateGenerator> getCandidateNodeGenerators() {
		return this.candidateNodeGenerators;
	}
}
