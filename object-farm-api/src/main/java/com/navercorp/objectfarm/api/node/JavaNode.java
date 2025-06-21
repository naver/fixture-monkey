package com.navercorp.objectfarm.api.node;

import java.util.List;
import java.util.stream.Collectors;

import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.nodecontext.JvmNodeContext;
import com.navercorp.objectfarm.api.nodepromoter.JvmNodePromoter;
import com.navercorp.objectfarm.api.type.JvmType;

public final class JavaNode implements JvmNode {
	private final JvmType type;
	private final String nodeName;
	private final JvmNodeContext context;

	public JavaNode(JvmType type, String nodeName, JvmNodeContext context) {
		this.type = type;
		this.nodeName = nodeName;
		this.context = context;
	}

	@Override
	public JvmType getType() {
		return type;
	}

	@Override
	public String getNodeName() {
		return nodeName;
	}

	@Override
	public List<JvmNodeCandidate> getCandidateChildren() {
		return this.context.getCandidateNodeGenerators().stream()
			.filter(it -> it.isSupported(type))
			.flatMap(it -> it.generateNextNodeCandidates(type).stream())
			.collect(Collectors.toList());
	}

	@Override
	public List<JvmNode> getChildren() {
		return getCandidateChildren().stream()
			.map(nextNode -> {
				JvmNodePromoter activeResolver = getResolver(nextNode);
				return activeResolver.promote(nextNode, context);
			})
			.collect(Collectors.toList());
	}

	private JvmNodePromoter getResolver(JvmNodeCandidate candidateNode) {
		return context.getNodeResolvers().stream()
			.filter(it -> it.canPromote(candidateNode))
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("No resolver found for candidate node: " + candidateNode));
	}
}
