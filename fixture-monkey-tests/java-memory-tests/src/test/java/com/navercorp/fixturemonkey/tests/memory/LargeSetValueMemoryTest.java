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

package com.navercorp.fixturemonkey.tests.memory;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenCode;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

class LargeSetValueMemoryTest {
	private static final long MAX_HEAP_BYTES = 512L * 1024 * 1024;
	private static final int LARGE_ARRAY_SIZE = 256_000;

	@Test
	void heapIsLimitedTo512Mb() {
		// when
		long actual = Runtime.getRuntime().maxMemory();

		// then
		then(actual).isLessThanOrEqualTo(MAX_HEAP_BYTES);
	}

	@Test
	void setLargeArrayFitsInLimitedHeap() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.build();
		byte[] data = new byte[LARGE_ARRAY_SIZE];

		// when, then
		thenCode(() -> sut.giveMeBuilder(Payload.class).set("data", data).sample())
			.doesNotThrowAnyException();
	}

	@Test
	void setLargeArrayInStrictModeFitsInLimitedHeap() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.useExpressionStrictMode()
			.build();
		byte[] data = new byte[LARGE_ARRAY_SIZE];

		// when, then
		thenCode(() -> sut.giveMeBuilder(Payload.class).set("data", data).sample())
			.doesNotThrowAnyException();
	}

	public static class Payload {
		private byte[] data;

		public byte[] getData() {
			return data;
		}
	}
}
