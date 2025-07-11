package com.navercorp.objectfarm.api.nodecandidate;

import javax.annotation.Nullable;

import com.navercorp.objectfarm.api.type.JvmType;

public final class JavaMapNodeCandidate implements JvmMapNodeCandidate {
	private final JvmType jvmType;
	@Nullable // It may be null if nested in a container
	private final String name;
	private final JvmNodeCandidate keyNode;
	private final JvmNodeCandidate valueNode;

	public JavaMapNodeCandidate(JvmType jvmType, @Nullable String name, JvmNodeCandidate keyNode,
		JvmNodeCandidate valueNode) {
		this.jvmType = jvmType;
		this.name = name;
		this.keyNode = keyNode;
		this.valueNode = valueNode;
	}

	@Override
	public JvmType getJvmType() {
		return this.jvmType;
	}

	@Override
	public JvmNodeCandidate getKey() {
		return this.keyNode;
	}

	@Override
	public JvmNodeCandidate getValue() {
		return this.valueNode;
	}

	@Override
	@Nullable
	public String getName() {
		return this.name;
	}
}
