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

import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.CompositeArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FailoverIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.resolver.ArbitraryBuilderCandidateFactory;
import com.navercorp.fixturemonkey.resolver.ArbitraryBuilderCandidateList;
import com.navercorp.fixturemonkey.tests.java.specs.ConstructorPropertySpecs.ConsturctorAndProperty;
import com.navercorp.fixturemonkey.tests.java.specs.ConstructorSpecs.FieldAndConstructorParameterMismatchObject;
import com.navercorp.fixturemonkey.tests.java.specs.DepthSpecs.DepthStringValueList;
import com.navercorp.fixturemonkey.tests.java.specs.DepthSpecs.OneDepthStringValue;
import com.navercorp.fixturemonkey.tests.java.specs.DepthSpecs.TwoDepthStringValue;
import com.navercorp.fixturemonkey.tests.java.specs.ImmutableSpecs.JavaTypeObject;
import com.navercorp.fixturemonkey.tests.java.specs.MixedIntrospectorsSpecs.MixedJavaTypeObject;
import com.navercorp.fixturemonkey.tests.java.specs.NoArgsConstructorSpecs.NestedObject;

class IntrospectorTest {
	@RepeatedTest(TEST_COUNT)
	void registerListWouldNotCached() {
		AtomicInteger sequence = new AtomicInteger();
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.registerGroup(() -> ArbitraryBuilderCandidateList.create()
				.add(
					ArbitraryBuilderCandidateFactory
						.of(DepthStringValueList.class)
						.builder(builder -> builder.size("twoDepthList", 3))
				)
				.add(
					ArbitraryBuilderCandidateFactory
						.of(OneDepthStringValue.class)
						.builder(builder -> builder.set(
							"value",
							Arbitraries.ofSuppliers(() -> String.valueOf(sequence.getAndIncrement()))
						))
				)
			)
			.build();

		Set<String> actual = sut.giveMe(DepthStringValueList.class, 3).stream()
			.flatMap(it -> it.getTwoDepthList().stream())
			.map(TwoDepthStringValue::getValue)
			.map(OneDepthStringValue::getValue)
			.collect(Collectors.toSet());

		then(actual).hasSize(9);
	}

	@Test
	void failoverIntrospector() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(new FailoverIntrospector(
					Arrays.asList(
						FieldReflectionArbitraryIntrospector.INSTANCE,
						ConstructorPropertiesArbitraryIntrospector.INSTANCE
					)
				)
			)
			.build();

		thenNoException().isThrownBy(() -> sut.giveMeOne(JavaTypeObject.class));
	}

	@Test
	void failoverIntrospectorMixed() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(new FailoverIntrospector(
					Arrays.asList(
						FieldReflectionArbitraryIntrospector.INSTANCE,
						ConstructorPropertiesArbitraryIntrospector.INSTANCE
					)
				)
			)
			.defaultNotNull(true)
			.build();

		thenNoException().isThrownBy(() -> sut.giveMeOne(MixedJavaTypeObject.class));
	}

	@RepeatedTest(TEST_COUNT)
	void compositeArbitraryIntrospector() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(
				new CompositeArbitraryIntrospector(
					Arrays.asList(
						ConstructorPropertiesArbitraryIntrospector.INSTANCE,
						FieldReflectionArbitraryIntrospector.INSTANCE
					)
				)
			)
			.defaultNotNull(true)
			.build();

		ConsturctorAndProperty actual =
			sut.giveMeOne(ConsturctorAndProperty.class);

		then(actual.getValue()).isNotNull();
		then(actual.getPropertyNotInConstructor()).isNotNull();
	}

	@Test
	void beanArbitraryIntrospectorSampleTwiceResultNotMutated() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.build();
		NestedObject stringObject = sut.giveMeOne(NestedObject.class);
		String actual = stringObject.getObject().getValue();

		// when
		sut.giveMeOne(NestedObject.class);

		// then
		String expected = stringObject.getObject().getValue();
		then(actual).isEqualTo(expected);
	}

	@Test
	void fieldReflectionArbitraryIntrospectorSampleTwiceResultNotMutated() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.build();
		NestedObject stringObject = sut.giveMeOne(NestedObject.class);
		String actual = stringObject.getObject().getValue();

		// when
		sut.giveMeOne(NestedObject.class);

		// then
		String expected = stringObject.getObject().getValue();
		then(actual).isEqualTo(expected);
	}

	@Test
	void fieldAndConstructorParameterMismatch() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushExactTypeArbitraryIntrospector(
				FieldAndConstructorParameterMismatchObject.class,
				ConstructorPropertiesArbitraryIntrospector.INSTANCE
			)
			.build();

		String actual = sut.giveMeOne(FieldAndConstructorParameterMismatchObject.class).getValue();

		then(actual).isNotNull();
	}
}
