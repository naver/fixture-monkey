package com.navercorp.fixturemonkey.report;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.resolver.NodeResolver;

public class NodeResolverHandler implements InvocationHandler, Observable {
	NodeResolver target;
	Observer observer = DebugInfoObserver.INSTANCE;

	public NodeResolverHandler(NodeResolver target) {
		this.target = target;
	}

	@Override
	public void notify(Integer builder, DebugInfo debugInfo) {

	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object ret = method.invoke(target, args);
		// ToDo: manipulate() 했을 때 ArbitraryNode가 어떻게 변했는지
		if (method.getName().equals("resolve")) {

		}
		return ret;
	}
}
