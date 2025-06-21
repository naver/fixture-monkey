package com.navercorp.objectfarm.api.node;

import java.util.List;

import javax.annotation.Nullable;

import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.type.JvmType;

// 타입에 상관없이 JvmNode로 구분
// 하위 노드 생성. 하위 노드는 재생성할 수 있다.
public interface JvmNode {
	JvmType getType();

	@Nullable
	String getNodeName();

	List<JvmNodeCandidate> getCandidateChildren();

	List<JvmNode> getChildren();
}
