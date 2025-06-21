package com.navercorp.objectfarm.api.nodecontext;

import com.navercorp.objectfarm.api.type.JvmType;

public final class JavaContainerNodeContext implements JvmContainerNodeContext {
	private final int size;

	public JavaContainerNodeContext(int size) {
		this.size = size;
	}

	@Override
	public int resolveContainerSize(JvmType containerType) {
		return size;
	}
}
