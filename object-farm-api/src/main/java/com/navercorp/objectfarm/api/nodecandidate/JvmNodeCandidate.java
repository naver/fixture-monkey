package com.navercorp.objectfarm.api.nodecandidate;

import javax.annotation.Nullable;

import com.navercorp.objectfarm.api.type.JvmType;

// 무엇을 생성할 것인가?
public interface JvmNodeCandidate {
	JvmType getJvmType();

	@Nullable
	String getName();
}
