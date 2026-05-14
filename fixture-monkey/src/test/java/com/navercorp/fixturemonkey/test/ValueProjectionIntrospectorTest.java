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

package com.navercorp.fixturemonkey.test;

import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.jqwik.ArbitraryUtils;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.test.ValueProjectionAssembleSpecs.ComplexObject;
import com.navercorp.fixturemonkey.test.ValueProjectionAssembleSpecs.SimpleObject;
import com.navercorp.fixturemonkey.test.ValueProjectionAssembleSpecs.SimpleObjectChild;

class ValueProjectionIntrospectorTest {

	private static final long SEED = 12345L;

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@Test
	void pushExactTypeArbitraryIntrospector() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.pushExactTypeArbitraryIntrospector(SimpleObjectChild.class, context ->
				new ArbitraryIntrospectorResult(ArbitraryUtils.toCombinableArbitrary(Arbitraries.just(null)))
			)
			.build();

		// when
		SimpleObjectChild actual = sut.giveMeOne(SimpleObjectChild.class);

		// then
		then(actual).isNull();
	}

	@Test
	void pushAssignableTypeArbitraryIntrospector() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.pushAssignableTypeArbitraryIntrospector(SimpleObject.class, context ->
				new ArbitraryIntrospectorResult(ArbitraryUtils.toCombinableArbitrary(Arbitraries.just(null)))
			)
			.build();

		// when
		SimpleObjectChild actual = sut.giveMeOne(SimpleObjectChild.class);

		// then
		then(actual).isNull();
	}

	@Test
	void pushArbitraryIntrospector() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.pushArbitraryIntrospector(
				MatcherOperator.exactTypeMatchOperator(SimpleObjectChild.class, context ->
					new ArbitraryIntrospectorResult(ArbitraryUtils.toCombinableArbitrary(Arbitraries.just(null)))
				)
			)
			.build();

		// when
		SimpleObjectChild actual = sut.giveMeOne(SimpleObjectChild.class);

		// then
		then(actual).isNull();
	}

	@Test
	void objectIntrospector() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(context ->
				new ArbitraryIntrospectorResult(ArbitraryUtils.toCombinableArbitrary(Arbitraries.just(null)))
			)
			.build();

		// when
		SimpleObject simpleObject = sut.giveMeOne(SimpleObject.class);
		ComplexObject complexObject = sut.giveMeOne(ComplexObject.class);

		// then
		then(simpleObject).isNull();
		then(complexObject).isNull();
	}
}
