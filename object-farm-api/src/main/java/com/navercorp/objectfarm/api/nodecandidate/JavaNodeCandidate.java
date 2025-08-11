package com.navercorp.objectfarm.api.nodecandidate;

import javax.annotation.Nullable;

import com.navercorp.objectfarm.api.type.JvmType;

public final class JavaNodeCandidate implements JvmNodeCandidate {
	private final JvmType jvmType;
	@Nullable
	private final String name;

	public JavaNodeCandidate(JvmType jvmType, @Nullable String name) {
		this.jvmType = jvmType;
		this.name = name;
	}

	@Override
	public JvmType getJvmType() {
		return jvmType;
	}

	@Override
	@Nullable
	public String getName() {
		return name;
	}
}
