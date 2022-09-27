package com.navercorp.fixturemonkey.report;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.navercorp.fixturemonkey.resolver.NodeManipulator;
import com.navercorp.fixturemonkey.resolver.NodeResolver;

public class NodeManipulatorHandler implements InvocationHandler, Observable {
	NodeManipulator target;
	Observer observer = DebugInfoObserver.INSTANCE;

	public NodeManipulatorHandler(NodeManipulator target) {
		this.target = target;
	}

	@Override
	public void notify(Integer builder, DebugInfo debugInfo) {

	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object ret = method.invoke(target, args);
		// Todo: Manipulator 정보 모으기
		if (method.getName().equals("resolve")) {

		}
		return ret;
	}
}
