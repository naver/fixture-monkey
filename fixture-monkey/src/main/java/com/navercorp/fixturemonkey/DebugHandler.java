package com.navercorp.fixturemonkey;

import com.navercorp.fixturemonkey.builder.DefaultArbitraryBuilder;
import com.navercorp.fixturemonkey.resolver.ArbitraryManipulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.groups.Default;

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

public class DebugHandler implements InvocationHandler, com.navercorp.fixturemonkey.Observable {
	ArbitraryBuilder target;
	Observer observer = DebugMonkey.INSTANCE;

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

		if (manipulateMethods.equals(method.getName())) {
			notify(
				((DefaultArbitraryBuilder)target).hashCode(),
				new UserInputManipulatorsInfo(method.getName(), new ArrayList(Arrays.asList(args)))
			);

			return (ArbitraryBuilder)Proxy.newProxyInstance(
				this.getClass().getClassLoader(),
				new Class[] {ArbitraryBuilder.class},
				new DebugHandler((ArbitraryBuilder)ret)
			);
		}
		return ret;
	}

	@Override
	public void notify(int builder, Object s) {
		observer.update(builder, s);
	}

	public ArbitraryBuilder getTarget() {
		return target;
	}
}
