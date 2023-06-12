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

package com.navercorp.fixturemonkey.api.introspector;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitraries;

class CompositeArbitraryIntrospectorTest {
	@Test
	void introspect() {
		// given
		CompositeArbitraryIntrospector sut = new CompositeArbitraryIntrospector(
			Arrays.asList(
				(context) -> ArbitraryIntrospectorResult.EMPTY,
				(context) -> new ArbitraryIntrospectorResult(Arbitraries.strings()),
				(context) -> new ArbitraryIntrospectorResult(Arbitraries.integers())
			)
		);

		// when
		ArbitraryIntrospectorResult actual = sut.introspect(null);

		then(actual).isNotEqualTo(ArbitraryIntrospectorResult.EMPTY);
		then(actual.getValue()).isNotNull();
		then(actual.getValue().combined()).isExactlyInstanceOf(String.class);
	}
}
