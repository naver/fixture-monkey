package com.navercorp.fixturemonkey;

import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.stream.Stream;

public class FixtureMonkey {

	public <T> Stream<T> giveMe(Class<T> type) {
		return this.giveMe(type, true);
	}

	public <T> List<T> giveMe(Class<T> type, int size) {
		return this.giveMe(type, size, true);
	}

	// TODO: implementation
	public <T> Stream<T> giveMe(Class<T> type, boolean validOnly) {
		return Stream.empty();
	}

	public <T> List<T> giveMe(Class<T> type, int size, boolean validOnly) {
		return this.giveMe(type, validOnly)
			.limit(size)
			.collect(toList());
	}

	public <T> T giveMeOne(Class<T> type) {
		return this.giveMeOne(type, true);
	}

	// TODO: implementation
	public <T> T giveMeOne(Class<T> type, boolean validOnly) {
		return null;
	}
}
