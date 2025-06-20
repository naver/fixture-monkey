package com.navercorp.objectfarm.api.nodecontext;

import java.util.List;

import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidateGenerator;
import com.navercorp.objectfarm.api.nodepromoter.JvmNodePromoter;

// immutable
public interface JvmNodeContext {
	long getSeed();

	List<JvmNodePromoter> getNodeResolvers();

	List<JvmNodeCandidateGenerator> getCandidateNodeGenerators();
}
