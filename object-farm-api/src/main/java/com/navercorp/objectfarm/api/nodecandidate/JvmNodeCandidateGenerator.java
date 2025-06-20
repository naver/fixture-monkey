package com.navercorp.objectfarm.api.nodecandidate;

import java.util.List;

import com.navercorp.objectfarm.api.type.JvmType;

// 순서 보장
public interface JvmNodeCandidateGenerator {
	List<JvmNodeCandidate> generateNextNodeCandidates(JvmType jvmType);

	default boolean isSupported(JvmType jvmType) {
		return true;
	}
}
