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

import static com.navercorp.fixturemonkey.api.experimental.TypedExpressionGenerator.typedRoot;
import static com.navercorp.fixturemonkey.api.experimental.TypedExpressionGenerator.typedString;
import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.exception.RetryableFilterMissException;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.customizer.InnerSpec;
import com.navercorp.fixturemonkey.customizer.Values;
import com.navercorp.fixturemonkey.tests.java.specs.FunctionalInterfaceSpecs.FunctionObject;
import com.navercorp.fixturemonkey.tests.java.specs.FunctionalInterfaceSpecs.SupplierObject;
import com.navercorp.fixturemonkey.tests.java.specs.MutableSpecs.ConstantObject;

class CustomizationTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@Test
	void sampleJavaTypeReturnsDiff() {
		ArbitraryBuilder<String> builder = SUT.giveMeBuilder(String.class);

		String actual = builder.sample();

		String notExpected = builder.sample();
		then(actual).isNotEqualTo(notExpected);
	}

	@Test
	void setPostConditionFailed() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(String.class)
				.setPostCondition(it -> it.equals("test"))
				.sample()
		)
			.getCause()
			.isExactlyInstanceOf(RetryableFilterMissException.class);
	}

	@RepeatedTest(TEST_COUNT)
	void thenApplyAndSizeMap() {
		Map<String, String> actual = SUT.giveMeBuilder(new TypeReference<Map<String, Map<String, String>>>() {
			})
			.setInner(new InnerSpec()
				.size(1)
				.value(m -> m.size(0))
			)
			.thenApply((it, builder) ->
				builder.setInner(new InnerSpec()
					.size(1)
					.value(m -> m.size(1))
				)
			)
			.sample()
			.values()
			.stream().findFirst()
			.orElse(null);

		then(actual).hasSize(1);
	}

	@RepeatedTest(TEST_COUNT)
	void setLazyJust() {
		AtomicInteger atomicInteger = new AtomicInteger();
		ArbitraryBuilder<Integer> builder = SUT.giveMeBuilder(Integer.class)
			.setLazy("$", () -> Values.just(atomicInteger.getAndIncrement()));

		int actual = builder.sample();

		int notExpected = builder.sample();
		then(actual).isNotEqualTo(notExpected);
	}

	@RepeatedTest(TEST_COUNT)
	void setArbitraryJust() {
		int expected = 1;

		int actual = SUT.giveMeBuilder(Integer.class)
			.set("$", Arbitraries.just(Values.just(expected)))
			.sample();

		then(actual).isEqualTo(expected);
	}

	@Test
	void constant() {
		thenNoException().isThrownBy(
			() -> SUT.giveMeOne(ConstantObject.class)
		);
	}

	@Test
	void sampleFunction() {
		Function<Integer, String> actual = SUT.giveMeBuilder(new TypeReference<Function<Integer, String>>() {
			})
			.sample();

		then(actual.apply(1)).isNotNull();
	}

	@Test
	void decomposeFunctionObject() {
		Function<Integer, String> actual = SUT.giveMeBuilder(FunctionObject.class)
			.thenApply((function, builder) -> {
			})
			.sample()
			.getValue();

		then(actual.apply(1)).isNotNull();
	}

	@Test
	void decomposeSupplierObject() {
		Supplier<String> actual = SUT.giveMeBuilder(SupplierObject.class)
			.thenApply((function, builder) -> {
			})
			.sample()
			.getValue();

		then(actual.get()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void unique() {
		List<Integer> actual = SUT.giveMeBuilder(new TypeReference<List<Integer>>() {
			})
			.size("$", 3)
			.set("$[*]", Values.unique(() -> Arbitraries.integers().between(0, 3).sample()))
			.sample();

		Set<Integer> expected = new HashSet<>(actual);
		then(actual).hasSize(expected.size());
	}

	@RepeatedTest(TEST_COUNT)
	void customizePropertyUnique() {
		List<Integer> actual = SUT.giveMeExperimentalBuilder(new TypeReference<List<Integer>>() {
			})
			.<Integer>customizeProperty(
				typedString("$[*]"),
				it -> it.filter(integer -> 0 <= integer && integer < 4)
			)
			.<List<Integer>>customizeProperty(typedRoot(), CombinableArbitrary::unique)
			.size("$", 3)
			.sample();

		Set<Integer> expected = new HashSet<>(actual);
		then(actual).hasSize(expected.size());
	}

	@Test
	void registerJavaTypebuilder() {
		String expected = "test";
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(String.class, it -> it.giveMeJavaBuilder(expected))
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isEqualTo(expected);
	}

	@RepeatedTest(TEST_COUNT)
	void registerSizeLessThanThree() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(
				new MatcherOperator<>(
					it -> it.getType().equals(new TypeReference<List<String>>() {
					}.getType()),
					fixture -> fixture.giveMeBuilder(new TypeReference<List<String>>() {
						})
						.maxSize("$", 2)
				)
			)
			.build();

		List<String> actual = sut.giveMeOne(new TypeReference<List<String>>() {
		});

		then(actual).hasSizeLessThan(3);
	}
}
