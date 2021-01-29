package com.navercorp.fixturemonkey.arbitrary;

import static java.util.stream.Collectors.toList;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

public class ArbitraryUtils {
	public static Arbitrary<UUID> uuid() {
		return Arbitraries.create(UUID::randomUUID);
	}

	public static Arbitrary<Instant> currentTime() {
		return Arbitraries.create(Instant::now);
	}

	public static <T> List<T> list(Arbitrary<T> arbitrary, int size) {
		return arbitrary.sampleStream()
			.limit(size)
			.collect(toList());
	}
}
