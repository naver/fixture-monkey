package com.navercorp.objectfarm.api.nodepromoter;

import java.lang.reflect.Modifier;
import java.util.function.Function;

import com.navercorp.objectfarm.api.node.JavaNode;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.nodecontext.JvmNodeContext;
import com.navercorp.objectfarm.api.type.JvmType;

public final class JavaInterfaceNodePromoter implements JvmNodePromoter {
	private final Function<JvmType, JvmType> interfaceTypeResolver;

	public JavaInterfaceNodePromoter(Function<JvmType, JvmType> interfaceTypeResolver) {
		this.interfaceTypeResolver = interfaceTypeResolver;
	}

	@Override
	public boolean canPromote(JvmNodeCandidate node) {
		return Modifier.isInterface(node.getJvmType().getRawType().getModifiers());
	}

	@Override
	public JvmNode promote(JvmNodeCandidate node, JvmNodeContext context) {
		return new JavaNode(
			interfaceTypeResolver.apply(node.getJvmType()),
			node.getName(),
			context
		);
	}
}
