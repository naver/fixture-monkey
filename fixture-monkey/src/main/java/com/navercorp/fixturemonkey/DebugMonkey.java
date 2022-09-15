package com.navercorp.fixturemonkey;

import com.navercorp.fixturemonkey.resolver.ArbitraryManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DebugMonkey implements Observer {
	public static final DebugMonkey INSTANCE = new DebugMonkey();
	private Map<ArbitraryManipulator, String> logs = new HashMap<>();
	private final Logger log = LoggerFactory.getLogger(this.getClass());


	@Override
	public void update(Object obj, Object o) {
		ArbitraryManipulator manipulator = (ArbitraryManipulator)obj;
		logs.put(manipulator, o.toString());
	}

	public void printManipulators(List<ArbitraryManipulator> manipulators) {
		AtomicInteger index = new AtomicInteger();
		String formattedString = "\n#####################################\nArbitraryBuilder was built with the following operations\n";
		List<String> list = manipulators.stream().map(it->"[" + index.getAndIncrement() + "] " + logs.get(it)).collect(Collectors.toList());
		formattedString += String.join("\n", list);
		formattedString += "\n#####################################";
		log.info(formattedString);
	}

	public void printRegistered(List<ArbitraryManipulator> arbitraryManipulators) {

	}

	public void printOptimizedManipulators(List<ArbitraryManipulator> manipulators) {
		AtomicInteger index = new AtomicInteger();
		List<String> list = manipulators.stream().map(it->{
			if (logs.containsKey(it)) {
				return "[" + index.getAndIncrement() + "] " + logs.get(it);
			}
			else {
				return "[" + index.getAndIncrement() + "] " + manipulators.toString();
			}
		}
		).collect(Collectors.toList());
		String formattedString = String.join("\n", list);
		log.info(formattedString);
	}
}
