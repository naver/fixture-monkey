package com.navercorp.fixturemonkey;

import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.stream.Stream;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.TooManyFilterMissesException;

public class FixtureMonkey {
	public <T> Stream<T> giveMe(Class<T> type) {
		return this.giveMe(type, true);
	}

	// TODO: implementation
	public <T> Stream<T> giveMe(Class<T> type, boolean validOnly) {
		// TODO: type to Arbitrary with generator
		return Stream.empty();
	}

	public <T> Stream<T> giveMe(Arbitrary<T> arbitrary) {
		return this.giveMe(arbitrary, true);
	}

	public <T> Stream<T> giveMe(Arbitrary<T> arbitrary, boolean validOnly) {
		return this.fixtures(arbitrary, validOnly);
	}

	public <T> List<T> giveMe(Class<T> type, int size) {
		return this.giveMe(type, size, true);
	}

	public <T> List<T> giveMe(Class<T> type, int size, boolean validOnly) {
		return this.giveMe(type, validOnly)
			.limit(size)
			.collect(toList());
	}

	public <T> List<T> giveMe(Arbitrary<T> arbitrary, int size) {
		return this.giveMe(arbitrary, size, true);
	}

	public <T> List<T> giveMe(Arbitrary<T> arbitrary, int size, boolean validOnly) {
		return this.giveMe(arbitrary, validOnly)
			.limit(size)
			.collect(toList());
	}

	public <T> T giveMeOne(Class<T> type) {
		return this.giveMeOne(type, true);
	}

	public <T> T giveMeOne(Class<T> type, boolean validOnly) {
		return this.giveMe(type, 1, validOnly).get(0);
	}

	public <T> T giveMeOne(Arbitrary<T> arbitrary) {
		return this.giveMeOne(arbitrary, true);
	}

	public <T> T giveMeOne(Arbitrary<T> arbitrary, boolean validOnly) {
		return this.giveMe(arbitrary, 1, validOnly).get(0);
	}

	private <T> Stream<T> fixtures(Arbitrary<T> arbitrary, boolean validOnly) {
		try {
			return arbitrary.sampleStream();	// TODO: filter with validator
		} catch (TooManyFilterMissesException ex) {
			// TODO: log error message with constraint violation messages.
			throw ex;
		}
	}
}
