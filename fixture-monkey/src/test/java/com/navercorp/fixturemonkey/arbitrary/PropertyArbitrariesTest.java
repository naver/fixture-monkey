package com.navercorp.fixturemonkey.arbitrary;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Example;

class PropertyArbitrariesTest {
	@Example
	void propertyArbitrariesConstructorNullDoesNotThrowsException() {
		PropertyArbitraries actual = new PropertyArbitraries(null);
		then(actual.getPropertyArbitraries()).isEmpty();
	}

	@Example
	void propertyArbitrariesConstructorMapIsCoppied() {
		// given
		Map<String, Arbitrary> propertyArbitraries = new HashMap<>();
		propertyArbitraries.put("test", Arbitraries.strings());

		// when
		PropertyArbitraries actual = new PropertyArbitraries(propertyArbitraries);

		then(actual).isNotSameAs(propertyArbitraries);

		propertyArbitraries.put("test2", Arbitraries.shorts());
		then(actual.getPropertyArbitraries()).hasSize(1);
		then(propertyArbitraries).hasSize(2);
	}

	@Example
	void getPropertyArbitraries() {
		// given
		Map<String, Arbitrary> propertyArbitraries = new HashMap<>();
		propertyArbitraries.put("test", Arbitraries.of("test_value"));
		propertyArbitraries.put("test2", Arbitraries.of("test_value2"));
		propertyArbitraries.put("test3", Arbitraries.of("test_value3"));
		PropertyArbitraries sut = new PropertyArbitraries(propertyArbitraries);

		// when
		Set<Map.Entry<String, Arbitrary>> actual = sut.getPropertyArbitraries();

		then(actual).hasSize(3);
		actual.forEach(it -> {
			then(propertyArbitraries.containsKey(it.getKey())).isTrue();
			then(it.getValue()).isEqualTo(propertyArbitraries.get(it.getKey()));
		});
	}
}
