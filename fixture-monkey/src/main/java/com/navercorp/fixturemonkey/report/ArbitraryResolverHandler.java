package com.navercorp.fixturemonkey.report;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import sun.reflect.Reflection;

import com.navercorp.fixturemonkey.builder.DefaultArbitraryBuilder;
import com.navercorp.fixturemonkey.resolver.ArbitraryManipulator;
import com.navercorp.fixturemonkey.resolver.ArbitraryResolver;
import com.navercorp.fixturemonkey.resolver.NodeResolver;

public class ArbitraryResolverHandler implements MethodInterceptor {
	ArbitraryResolver target;
	Observer observer = DebugInfoObserver.INSTANCE;

	public ArbitraryResolverHandler(ArbitraryResolver target) {
		this.target = target;
	}

	public void notify(Integer builder, DebugInfo debugInfo) {
		observer.update(builder, debugInfo);
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		Object result = null;
		result = proxy.invokeSuper(obj, args);
		if (method.getName().equals("getOptimizedManipulator")) {
			notify(
				((DefaultArbitraryBuilder)obj).getId(),
				new OptimizedManipulatorsInfo((List<ArbitraryManipulator>)result)
			);
		}
		return result;
	}
}
