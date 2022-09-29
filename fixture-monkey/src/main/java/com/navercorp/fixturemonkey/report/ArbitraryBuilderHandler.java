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

public class ArbitraryBuilderHandler implements InvocationHandler, Observable {
	ArbitraryBuilder target;
	Observer observer = DebugInfoObserver.INSTANCE;

	private static final Set<String> manipulateMethods =
		Stream.of("spec", "specAny", "set", "setInner", "setLazy",
				"setNull", "setNotNull", "setPostCondition", "size", "minSize",
				"maxSize", "apply", "acceptIf", "fixed")
			.collect(Collectors.toCollection(HashSet::new));

	public ArbitraryBuilderHandler(ArbitraryBuilder target) {
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object ret = method.invoke(target, args);
		List<ArbitraryManipulator> manipulators = ((DefaultArbitraryBuilder)target).getManipulators();

		if (manipulateMethods.contains(method.getName())) {
			notify(
				((DefaultArbitraryBuilder)target).getId(),
				new UserInputManipulatorInfo(method.getName(), new ArrayList(Arrays.asList(args)))
			);

			return (ArbitraryBuilder)Proxy.newProxyInstance(
				this.getClass().getClassLoader(),
				new Class[] {ArbitraryBuilder.class},
				new ArbitraryBuilderHandler((ArbitraryBuilder)ret)
			);
		} else if (method.getName().equals("sample")) {
			//Todo: 해당 notify 삭제하고 ArbitrryResolverHandler에서 받기~
			notify(
				((DefaultArbitraryBuilder)target).getId(),
				new OptimizedManipulatorsInfo(manipulators)
			);


			notify(
				((DefaultArbitraryBuilder)target).getId(),
				new ArbitraryBuilderSampledInfo(manipulators.size())
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
