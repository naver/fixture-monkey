package com.navercorp.objectfarm.api.nodepromoter;

import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.nodecontext.JvmNodeContext;

// 재실행 가능해야 한다.
// 사용 사례, 인터페이스의 구현체 결정
public interface JvmNodePromoter {
	default boolean canPromote(JvmNodeCandidate node) {
		return true;
	}

	JvmNode promote(JvmNodeCandidate node, JvmNodeContext context);
}
