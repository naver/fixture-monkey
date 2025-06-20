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
