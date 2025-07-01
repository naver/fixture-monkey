package com.navercorp.objectfarm.api.nodecandidate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.navercorp.objectfarm.api.nodecontext.JvmContainerNodeContext;
import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;

public final class JavaArrayElementNodeCandidateGenerator implements JvmNodeCandidateGenerator {
	private final JvmContainerNodeContext context;

	public JavaArrayElementNodeCandidateGenerator(JvmContainerNodeContext context) {
		this.context = context;
	}

	@Override
	public List<JvmNodeCandidate> generateNextNodeCandidates(JvmType jvmType) {
		Class<?> componentType = jvmType.getRawType().getComponentType();

		int containerSize = context.resolveContainerSize(jvmType);

		List<JvmNodeCandidate> nodePossibilities = new ArrayList<>();
		for (int i = 0; i < containerSize; i++) {
			nodePossibilities.add(new JavaNodeCandidate(new JavaType(componentType), null));
		}

		return Collections.unmodifiableList(nodePossibilities);
	}

	@Override
	public boolean isSupported(JvmType jvmType) {
		Class<?> rawType = jvmType.getRawType();
		return rawType.isArray();
	}
}
