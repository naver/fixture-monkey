/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.fixturemonkey.arbitrary;

import static org.assertj.core.api.BDDAssertions.*;

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
