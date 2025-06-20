package com.navercorp.objectfarm.api.nodecandidate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.navercorp.objectfarm.api.nodecontext.JvmContainerNodeContext;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.Types;

public final class JavaLinearContainerElementNodeCandidateGenerator implements JvmNodeCandidateGenerator {
	private final JvmContainerNodeContext context;

	public JavaLinearContainerElementNodeCandidateGenerator(JvmContainerNodeContext context) {
		this.context = context;
	}

	@Override
	public List<JvmNodeCandidate> generateNextNodeCandidates(JvmType jvmType) {
		JvmType typeVariables = jvmType.getTypeVariables().get(0);

		int containerSize = context.resolveContainerSize(jvmType);

		List<JvmNodeCandidate> elementNodeCandidates = new ArrayList<>();
		for (int i = 0; i < containerSize; i++) {
			elementNodeCandidates.add(new JavaNodeCandidate(typeVariables, null));
		}

		return Collections.unmodifiableList(elementNodeCandidates);
	}

	@Override
	public boolean isSupported(JvmType jvmType) {
		Class<?> rawType = jvmType.getRawType();
		boolean collectionType = Types.isAssignable(rawType, Collection.class)
			&& !Types.isAssignable(rawType, Map.class)
			&& !rawType.isArray();

		boolean singleTypeVariable = jvmType.getTypeVariables().size() == 1;
		return collectionType && singleTypeVariable;
	}
}
