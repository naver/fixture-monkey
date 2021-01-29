package com.navercorp.fixturemonkey.arbitrary;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.BDDAssertions.then;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

class ArbitraryUtilsTest {
	@Property(tries = 10)
	void uuid(@ForAll @IntRange(min = 2, max = 100000) int size) {
		Set<UUID> actual = ArbitraryUtils.uuid()
			.sampleStream()
			.limit(size)
			.collect(toSet());
		then(actual).hasSize(size);
	}

	@Property(tries = 10)
	void currentTime(@ForAll @IntRange(min = 2, max = 100000) int size) {
		// when
		List<Instant> actual = ArbitraryUtils.currentTime()
			.sampleStream()
			.limit(size)
			.collect(toList());

		// then
		Instant now = Instant.now();
		actual.forEach(it ->
			then(it).isBetween(now.minus(10, ChronoUnit.SECONDS), now)
		);
	}

	@Property(tries = 10)
	void list(@ForAll @IntRange(min = 2, max = 100000) int size) {
		// given
		Arbitrary<String> arbitrary = Arbitraries.strings().all();

		// when
		List<String> actual = ArbitraryUtils.list(arbitrary, size);

		then(actual).hasSize(size);
	}
}
