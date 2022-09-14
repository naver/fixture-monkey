package com.navercorp.fixturemonkey;

import com.navercorp.fixturemonkey.builder.DefaultArbitraryBuilder;
import com.navercorp.fixturemonkey.resolver.ArbitraryManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.groups.Default;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;

public class DebugHandler implements InvocationHandler {
	ArbitraryBuilder target;
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	public DebugHandler(ArbitraryBuilder target) {
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		System.out.println(method.getName());
		Object ret = method.invoke(target, args);
		if (method.getName().equals("set")) {
			String[] strArgs = Arrays.stream(args).map(Object::toString).toArray(String[]::new);
			log.info(".set(" + String.join(", ", strArgs) + ")");
		}
		List<ArbitraryManipulator> manipulators = ((DefaultArbitraryBuilder)target).getManipulators();
		return ret;
	}
}
