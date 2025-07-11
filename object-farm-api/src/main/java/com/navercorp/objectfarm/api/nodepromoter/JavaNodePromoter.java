package com.navercorp.objectfarm.api.nodepromoter;

import com.navercorp.objectfarm.api.node.JavaNode;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.nodecontext.JvmNodeContext;

public final class JavaNodePromoter implements JvmNodePromoter {
	@Override
	public JvmNode promote(JvmNodeCandidate node, JvmNodeContext context) {
		return new JavaNode(
			node.getJvmType(),
			node.getName(),
			context
		);
	}
}
