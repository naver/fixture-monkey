package com.navercorp.objectfarm.api.node;

import java.util.Collections;
import java.util.List;

import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;

public final class JavaDefaultNodePromoter implements JvmNodePromoter {

	private final List<JvmNodePromoter> nodePromoters;

	public JavaDefaultNodePromoter(List<JvmNodePromoter> nodePromoters) {
		this.nodePromoters = nodePromoters;
	}

	@Override
	public boolean canPromote(JvmNodeCandidate node) {
		return nodePromoters.stream().anyMatch(nodePromoter -> nodePromoter.canPromote(node));
	}

	@Override
	public List<JvmNode> promote(JvmNodeCandidate node, JvmNodeContext context) {
		for (JvmNodePromoter promoter : nodePromoters) {
			if (promoter.canPromote(node)) {
				return promoter.promote(node, context);
			}
		}
		return Collections.emptyList();
	}
}
