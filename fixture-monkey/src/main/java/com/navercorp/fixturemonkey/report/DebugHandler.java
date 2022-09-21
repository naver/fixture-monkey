package com.navercorp.fixturemonkey.report;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.builder.DefaultArbitraryBuilder;
import com.navercorp.fixturemonkey.resolver.ArbitraryManipulator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DebugHandler implements InvocationHandler, Observable {
	ArbitraryBuilder target;
	Observer observer = DebugInfoObserver.INSTANCE;

	private static final Set<String> manipulateMethods =
		Stream.of("spec", "specAny", "set", "setInner", "setLazy",
				"setNull", "setNotNull", "setPostCondition", "size", "minSize",
				"maxSize", "apply", "acceptIf", "fixed")
			.collect(Collectors.toCollection(HashSet::new));

	public DebugHandler(ArbitraryBuilder target) {
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		List<ArbitraryManipulator> manipulators = ((DefaultArbitraryBuilder)target).getManipulators();

		Object ret = method.invoke(target, args);

		if (manipulateMethods.contains(method.getName())) {
			notify(
				((DefaultArbitraryBuilder)target).hashCode(),
				new UserInputManipulatorsInfo(method.getName(), new ArrayList(Arrays.asList(args)))
			);

			return (ArbitraryBuilder)Proxy.newProxyInstance(
				this.getClass().getClassLoader(),
				new Class[] {ArbitraryBuilder.class},
				new DebugHandler((ArbitraryBuilder)ret)
			);
		} else if (method.getName().equals("sample")) {
			notify(
				((DefaultArbitraryBuilder)target).hashCode(),
				new OptimizedManipulatorsInfo(manipulators)
			);
		}
		return ret;
	}

	@Override
	public void notify(Integer builder, DebugInfo debugInfo) {
		observer.update(builder, debugInfo);
	}

	public ArbitraryBuilder getTarget() {
		return target;
	}
}
