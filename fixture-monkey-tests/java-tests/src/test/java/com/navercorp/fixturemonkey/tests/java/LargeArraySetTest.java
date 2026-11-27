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

package com.navercorp.fixturemonkey.tests.java;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

class LargeArraySetTest {
	@Test
	void setLargeArrayDoesNotProbeEachElementAsContainer() {
		// given
		AtomicInteger unsupportedContainerProbes = new AtomicInteger();
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultDecomposedContainerValueFactory(obj -> {
				unsupportedContainerProbes.incrementAndGet();
				throw new IllegalArgumentException(
					"given type is not supported container : " + obj.getClass().getTypeName()
				);
			})
			.build();
		byte[] data = new byte[10_000];

		// when
		Payload actual = sut.giveMeBuilder(Payload.class)
			.set("data", data)
			.sample();

		// then
		then(actual.getData()).isEqualTo(data);
		then(unsupportedContainerProbes.get()).isLessThan(data.length);
	}

	public static class Payload {
		private byte[] data;

		public byte[] getData() {
			return data;
		}
	}
}
