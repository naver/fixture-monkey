package com.navercorp.fixturemonkey.report;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import sun.reflect.Reflection;

import com.navercorp.fixturemonkey.builder.DefaultArbitraryBuilder;
import com.navercorp.fixturemonkey.resolver.ArbitraryResolver;
import com.navercorp.fixturemonkey.resolver.NodeResolver;

public class ArbitraryResolverHandler implements InvocationHandler, Observable {
	ArbitraryResolver target;
	Observer observer = DebugInfoObserver.INSTANCE;

	public ArbitraryResolverHandler(ArbitraryResolver target) {
		this.target = target;
	}

	@Override
	public void notify(Integer builder, DebugInfo debugInfo) {
		observer.update(builder, debugInfo);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object ret = method.invoke(target, args);
		if (method.getName().equals("getOptimizedManipulator")) {
			// notify(
			// 	((DefaultArbitraryBuilder)target).getId(),
			// 	new OptimizedManipulatorsInfo(manipulators)
			// );
		}
		return ret;
	}
}
