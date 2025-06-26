package com.navercorp.objectfarm.api.nodecontext;

import com.navercorp.objectfarm.api.type.JvmType;

public interface JvmContainerNodeContext {
	// seed에 따라 고정
	int resolveContainerSize(JvmType containerType);
}
