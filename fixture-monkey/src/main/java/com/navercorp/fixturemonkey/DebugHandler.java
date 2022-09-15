package com.navercorp.fixturemonkey;

import com.navercorp.fixturemonkey.builder.DefaultArbitraryBuilder;
import com.navercorp.fixturemonkey.resolver.ArbitraryManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.groups.Default;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

public class DebugHandler implements InvocationHandler, com.navercorp.fixturemonkey.Observable {
	ArbitraryBuilder target;
	Observer observer = DebugMonkey.INSTANCE;
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	public DebugHandler(ArbitraryBuilder target) {
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		List<ArbitraryManipulator> manipulators = ((DefaultArbitraryBuilder)target).getManipulators();
		String s = "";
		Object ret = method.invoke(target, args);
		if (method.getName().equals("set")) {
			String[] strArgs = Arrays.stream(args).map(Object::toString).toArray(String[]::new);
			s = ".set(" + String.join(", ", strArgs) + ")";
			notify(manipulators.get(manipulators.size()-1), s);
			return (ArbitraryBuilder) Proxy.newProxyInstance(
				this.getClass().getClassLoader(),
				new Class[]{ArbitraryBuilder.class},
				new DebugHandler((ArbitraryBuilder) ret)
			);
		} else if (method.getName().equals("size")) {
			String[] strArgs = Arrays.stream(args).map(Object::toString).toArray(String[]::new);
			s = ".size(" + String.join(", ", strArgs) + ")";
			notify(manipulators.get(manipulators.size() - 1), s);
			return (ArbitraryBuilder) Proxy.newProxyInstance(
				this.getClass().getClassLoader(),
				new Class[]{ArbitraryBuilder.class},
				new DebugHandler((ArbitraryBuilder) ret)
			);
		}
		return ret;
	}

	@Override
	public void notify(Object manipulator, Object s) {
		observer.update(manipulator, s);
	}

	public ArbitraryBuilder getTarget() {
		return target;
	}
}
