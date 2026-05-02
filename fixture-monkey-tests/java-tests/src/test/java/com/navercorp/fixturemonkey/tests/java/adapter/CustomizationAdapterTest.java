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

package com.navercorp.fixturemonkey.tests.java.adapter;

import static com.navercorp.fixturemonkey.api.experimental.TypedExpressionGenerator.typedRoot;
import static com.navercorp.fixturemonkey.api.experimental.TypedExpressionGenerator.typedString;
import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.Arrays;
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
import com.navercorp.fixturemonkey.adapter.JavaNodeTreeAdapterPlugin;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.exception.RetryableFilterMissException;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.customizer.InnerSpec;
import com.navercorp.fixturemonkey.customizer.Values;
import com.navercorp.fixturemonkey.tests.java.specs.FunctionalInterfaceSpecs.FunctionObject;
import com.navercorp.fixturemonkey.tests.java.specs.FunctionalInterfaceSpecs.SupplierObject;
import com.navercorp.fixturemonkey.tests.java.specs.MutableSpecs.ConstantObject;
import com.navercorp.fixturemonkey.tests.java.specs.MutableSpecs.ContainerObject;
import com.navercorp.fixturemonkey.tests.java.specs.MutableSpecs.JavaTypeObject;

class CustomizationAdapterTest {

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.plugin(new JavaNodeTreeAdapterPlugin())
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
		thenThrownBy(() ->
			SUT.giveMeBuilder(String.class)
				.setPostCondition(it -> it.equals("test"))
				.sample()
		)
			.getCause()
			.isExactlyInstanceOf(RetryableFilterMissException.class);
	}

	@Test
	void thenApplyAndSizeMap() {
		Map<String, String> actual = SUT.giveMeBuilder(new TypeReference<Map<String, Map<String, String>>>() {
			})
			.setInner(new InnerSpec().size(1).value(m -> m.size(0)))
			.thenApply((it, builder) -> builder.setInner(new InnerSpec().size(1).value(m -> m.size(1))))
			.sample()
			.values()
			.stream()
			.findFirst()
			.orElse(null);

		then(actual).hasSize(1);
	}

	@RepeatedTest(TEST_COUNT)
	void setLazyJust() {
		AtomicInteger atomicInteger = new AtomicInteger();
		ArbitraryBuilder<Integer> builder = SUT.giveMeBuilder(Integer.class).setLazy("$", () ->
			Values.just(atomicInteger.getAndIncrement())
		);

		int actual = builder.sample();

		int notExpected = builder.sample();
		then(actual).isNotEqualTo(notExpected);
	}

	@RepeatedTest(TEST_COUNT)
	void setArbitraryJust() {
		int expected = 1;

		int actual = SUT.giveMeBuilder(Integer.class).set("$", Arbitraries.just(Values.just(expected))).sample();

		then(actual).isEqualTo(expected);
	}

	@Test
	void constant() {
		thenNoException().isThrownBy(() -> SUT.giveMeOne(ConstantObject.class));
	}

	@Test
	void sampleFunction() {
		Function<Integer, String> actual = SUT.giveMeBuilder(
			new TypeReference<Function<Integer, String>>() {
			}
		).sample();

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
			.<Integer>customizeProperty(typedString("$[*]"), it -> it.filter(integer -> 0 <= integer && integer < 4))
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
			.plugin(new JavaNodeTreeAdapterPlugin())
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
					}).maxSize("$", 2)
				)
			)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		List<String> actual = sut.giveMeOne(new TypeReference<List<String>>() {
		});

		then(actual).hasSizeLessThan(3);
	}

	@Test
	void nestedMapSetInnerValue() {
		// given
		Map<String, Map<String, Integer>> actual = SUT.giveMeBuilder(
				new TypeReference<Map<String, Map<String, Integer>>>() {
				}
			)
			.setInner(new InnerSpec().size(1).entry("outerKey", inner -> inner.size(1).entry("innerKey", 42)))
			.sample();

		// when
		Map<String, Integer> innerMap = actual.get("outerKey");

		// then
		then(actual).hasSize(1);
		then(actual).containsKey("outerKey");
		then(innerMap).isNotNull();
		then(innerMap).hasSize(1);
		then(innerMap).containsEntry("innerKey", 42);
	}

	@Test
	void nestedMapSetInnerSize() {
		// given
		Map<String, Map<String, String>> actual = SUT.giveMeBuilder(
				new TypeReference<Map<String, Map<String, String>>>() {
				}
			)
			.setInner(new InnerSpec().size(2).allValue(inner -> inner.size(3)))
			.sample();

		// then
		then(actual).hasSize(2);
		for (Map<String, String> innerMap : actual.values()) {
			then(innerMap).hasSize(3);
		}
	}

	@Test
	void nestedMapSize() {
		// given
		Map<String, Map<String, Integer>> actual = SUT.giveMeBuilder(
				new TypeReference<Map<String, Map<String, Integer>>>() {
				}
			)
			.setInner(new InnerSpec().size(5).allValue(inner -> inner.size(5)))
			.sample();

		// then
		then(actual).hasSize(5);
		for (Map<String, Integer> innerMap : actual.values()) {
			then(innerMap).hasSize(5);
		}
	}

	@Test
	void thenApplyAndSizeFieldContainer() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		ContainerObject actual = sut
			.giveMeBuilder(ContainerObject.class)
			.size("$.list", 1)
			.thenApply((it, builder) -> builder.size("$.list", 3))
			.sample();

		// then
		then(actual.getList()).hasSize(3);
	}

	@Test
	void thenApplyAndSizeNestedFieldContainer() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(
				com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector.INSTANCE
			)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.build();

		NestedListObject actual = sut
			.giveMeBuilder(NestedListObject.class)
			.size("$.nestedList", 2)
			.size("$.nestedList[*]", 1)
			.thenApply((it, builder) -> builder.size("$.nestedList", 2).size("$.nestedList[*]", 3))
			.sample();

		// then
		then(actual.getNestedList()).hasSize(2);
		for (List<String> inner : actual.getNestedList()) {
			then(inner).hasSize(3);
		}
	}

	@Test
	void setListValueOverridesRegisteredList() {
		// given
		JavaTypeObject registered = new JavaTypeObject();
		registered.setString("x");

		JavaTypeObject expectedElement1 = new JavaTypeObject();
		expectedElement1.setString("a");
		JavaTypeObject expectedElement2 = new JavaTypeObject();
		expectedElement2.setString("b");
		JavaTypeObject expectedElement3 = new JavaTypeObject();
		expectedElement3.setString("c");
		JavaTypeObject expectedElement4 = new JavaTypeObject();
		expectedElement4.setString("d");
		JavaTypeObject expectedElement5 = new JavaTypeObject();
		expectedElement5.setString("e");

		List<JavaTypeObject> expected = new java.util.ArrayList<>(Arrays.asList(
			expectedElement1, expectedElement2, expectedElement3, expectedElement4, expectedElement5
		));

		FixtureMonkey registerSut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm ->
				fm
					.giveMeBuilder(ContainerObject.class)
					.set("complexList", new java.util.ArrayList<>(Arrays.asList(registered)))
			)
			.build();

		// when
		ContainerObject actual = registerSut.giveMeBuilder(ContainerObject.class).set("complexList", expected).sample();

		// then
		then(actual.getComplexList()).hasSize(5);
	}

	@Test
	void setScalarFieldOverridesRegisteredScalarField() {
		// given
		String expected = "overridden";

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(JavaTypeObject.class, fm -> fm.giveMeBuilder(JavaTypeObject.class).set("string", "registered"))
			.build();

		// when
		JavaTypeObject actual = sut.giveMeBuilder(JavaTypeObject.class).set("string", expected).sample();

		// then
		then(actual.getString()).isEqualTo(expected);
	}

	@Test
	void setNestedObjectFieldOverridesRegistered() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm ->
				fm.giveMeBuilder(ContainerObject.class).set("complexList[0].string", "registered")
			)
			.build();

		// when
		ContainerObject actual = sut
			.giveMeBuilder(ContainerObject.class)
			.size("complexList", 1)
			.set("complexList[0].string", "overridden")
			.sample();

		// then
		then(actual.getComplexList()).hasSize(1);
		then(actual.getComplexList().get(0).getString()).isEqualTo("overridden");
	}

	@Test
	void setContainerSmallerThanRegistered() {
		// given
		JavaTypeObject registeredElement1 = new JavaTypeObject();
		registeredElement1.setString("r1");
		JavaTypeObject registeredElement2 = new JavaTypeObject();
		registeredElement2.setString("r2");
		JavaTypeObject registeredElement3 = new JavaTypeObject();
		registeredElement3.setString("r3");

		JavaTypeObject userElement = new JavaTypeObject();
		userElement.setString("u1");

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm ->
				fm
					.giveMeBuilder(ContainerObject.class)
					.set("complexList", new java.util.ArrayList<>(Arrays.asList(
						registeredElement1, registeredElement2, registeredElement3
					)))
			)
			.build();

		// when
		ContainerObject actual = sut
			.giveMeBuilder(ContainerObject.class)
			.set("complexList", new java.util.ArrayList<>(Arrays.asList(userElement)))
			.sample();

		// then
		then(actual.getComplexList()).hasSize(1);
		then(actual.getComplexList().get(0).getString()).isEqualTo("u1");
	}

	@Test
	void setNonOverlappingFieldsWithRegister() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(JavaTypeObject.class, fm -> fm.giveMeBuilder(JavaTypeObject.class).set("string", "fromRegister"))
			.build();

		// when
		JavaTypeObject actual = sut.giveMeBuilder(JavaTypeObject.class).set("wrapperInteger", 42).sample();

		// then
		then(actual.getString()).isEqualTo("fromRegister");
		then(actual.getWrapperInteger()).isEqualTo(42);
	}

	@Test
	void setElementOverridesRegisteredContainer() {
		// given
		JavaTypeObject registeredElement1 = new JavaTypeObject();
		registeredElement1.setString("r1");
		JavaTypeObject registeredElement2 = new JavaTypeObject();
		registeredElement2.setString("r2");

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm ->
				fm
					.giveMeBuilder(ContainerObject.class)
					.set("complexList",
						new java.util.ArrayList<>(Arrays.asList(registeredElement1, registeredElement2)))
			)
			.build();

		JavaTypeObject overriddenElement = new JavaTypeObject();
		overriddenElement.setString("overridden");

		// when
		ContainerObject actual = sut
			.giveMeBuilder(ContainerObject.class)
			.size("complexList", 2)
			.set("complexList[0]", overriddenElement)
			.sample();

		// then
		then(actual.getComplexList()).hasSize(2);
		then(actual.getComplexList().get(0).getString()).isEqualTo("overridden");
	}

	@Test
	void setWholeContainerOverridesRegisteredElement() {
		// given
		JavaTypeObject userElement1 = new JavaTypeObject();
		userElement1.setString("u1");
		JavaTypeObject userElement2 = new JavaTypeObject();
		userElement2.setString("u2");
		JavaTypeObject userElement3 = new JavaTypeObject();
		userElement3.setString("u3");

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm ->
				fm
					.giveMeBuilder(ContainerObject.class)
					.size("complexList", 1)
					.set("complexList[0].string", "registered")
			)
			.build();

		// when
		ContainerObject actual = sut
			.giveMeBuilder(ContainerObject.class)
			.set("complexList", new java.util.ArrayList<>(Arrays.asList(userElement1, userElement2, userElement3)))
			.sample();

		// then
		then(actual.getComplexList()).hasSize(3);
		then(actual.getComplexList().get(0).getString()).isEqualTo("u1");
		then(actual.getComplexList().get(1).getString()).isEqualTo("u2");
		then(actual.getComplexList().get(2).getString()).isEqualTo("u3");
	}

	@Test
	void setOverridesRegisteredThenApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(JavaTypeObject.class, fm ->
				fm
					.giveMeBuilder(JavaTypeObject.class)
					.set("string", "initial")
					.thenApply((it, builder) -> builder.set("string", "fromThenApply"))
			)
			.build();

		// when
		JavaTypeObject actual = sut.giveMeBuilder(JavaTypeObject.class).set("string", "overridden").sample();

		// then
		then(actual.getString()).isEqualTo("overridden");
	}

	@Test
	void setMapOverridesRegisteredMap() {
		// given
		Map<String, Integer> registered = new java.util.HashMap<>();
		registered.put("regKey", 1);

		Map<String, Integer> expected = new java.util.HashMap<>();
		expected.put("key1", 10);
		expected.put("key2", 20);
		expected.put("key3", 30);

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm -> fm.giveMeBuilder(ContainerObject.class).set("map", registered))
			.build();

		// when
		ContainerObject actual = sut.giveMeBuilder(ContainerObject.class).set("map", expected).sample();

		// then
		then(actual.getMap()).hasSize(3);
		then(actual.getMap()).containsEntry("key1", 10);
		then(actual.getMap()).containsEntry("key2", 20);
		then(actual.getMap()).containsEntry("key3", 30);
	}

	@Test
	void setNullOverridesRegisteredSet() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(JavaTypeObject.class, fm -> fm.giveMeBuilder(JavaTypeObject.class).set("string", "registered"))
			.build();

		// when
		JavaTypeObject actual = sut.giveMeBuilder(JavaTypeObject.class).setNull("string").sample();

		// then
		then(actual.getString()).isNull();
	}

	@Test
	void setOverridesRegisteredSetNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(JavaTypeObject.class, fm -> fm.giveMeBuilder(JavaTypeObject.class).setNull("string"))
			.build();

		// when
		JavaTypeObject actual = sut.giveMeBuilder(JavaTypeObject.class).set("string", "overridden").sample();

		// then
		then(actual.getString()).isEqualTo("overridden");
	}

	@RepeatedTest(TEST_COUNT)
	void setLazyOverridesRegisteredSet() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(JavaTypeObject.class, fm -> fm.giveMeBuilder(JavaTypeObject.class).set("string", "registered"))
			.build();

		// when
		JavaTypeObject actual = sut
			.giveMeBuilder(JavaTypeObject.class)
			.setLazy("string", () -> "lazy")
			.sample();

		// then
		then(actual.getString()).isEqualTo("lazy");
	}

	@RepeatedTest(TEST_COUNT)
	void setPostConditionAppliesOnRegisteredSet() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(JavaTypeObject.class, fm -> fm.giveMeBuilder(JavaTypeObject.class).set("wrapperInteger", 10))
			.build();

		// when
		JavaTypeObject actual = sut
			.giveMeBuilder(JavaTypeObject.class)
			.setPostCondition("wrapperInteger", Integer.class, i -> i > 5)
			.sample();

		// then
		then(actual.getWrapperInteger()).isGreaterThan(5);
	}

	@Test
	void setContainerOverridesRegisteredSize() {
		// given
		JavaTypeObject userElement1 = new JavaTypeObject();
		userElement1.setString("u1");
		JavaTypeObject userElement2 = new JavaTypeObject();
		userElement2.setString("u2");

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm -> fm.giveMeBuilder(ContainerObject.class).size("complexList", 5))
			.build();

		// when
		ContainerObject actual = sut
			.giveMeBuilder(ContainerObject.class)
			.set("complexList", new java.util.ArrayList<>(Arrays.asList(userElement1, userElement2)))
			.sample();

		// then
		then(actual.getComplexList()).hasSize(2);
		then(actual.getComplexList().get(0).getString()).isEqualTo("u1");
		then(actual.getComplexList().get(1).getString()).isEqualTo("u2");
	}

	@Test
	void sizeOverridesRegisteredSetContainer() {
		// given
		JavaTypeObject registeredElement = new JavaTypeObject();
		registeredElement.setString("r1");

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm ->
				fm.giveMeBuilder(ContainerObject.class)
					.set("complexList", new java.util.ArrayList<>(Arrays.asList(registeredElement)))
			)
			.build();

		// when
		ContainerObject actual = sut.giveMeBuilder(ContainerObject.class).size("complexList", 3).sample();

		// then
		then(actual.getComplexList()).hasSize(3);
	}

	@Test
	void thenApplySetOverridesRegisteredSet() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(JavaTypeObject.class, fm -> fm.giveMeBuilder(JavaTypeObject.class).set("string", "registered"))
			.build();

		// when
		JavaTypeObject actual = sut
			.giveMeBuilder(JavaTypeObject.class)
			.thenApply((it, builder) -> builder.set("string", "fromThenApply"))
			.sample();

		// then
		then(actual.getString()).isEqualTo("fromThenApply");
	}

	@Test
	void setInnerOverridesRegisteredMap() {
		// given
		Map<String, Integer> registered = new java.util.HashMap<>();
		registered.put("regKey", 1);

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm -> fm.giveMeBuilder(ContainerObject.class).set("map", registered))
			.build();

		// when
		ContainerObject actual = sut
			.giveMeBuilder(ContainerObject.class)
			.setInner(new InnerSpec().property("map", m -> m.size(2).entry("k1", 100).entry("k2", 200)))
			.sample();

		// then
		then(actual.getMap()).hasSize(2);
		then(actual.getMap()).containsEntry("k1", 100);
		then(actual.getMap()).containsEntry("k2", 200);
	}

	@RepeatedTest(TEST_COUNT)
	void setOverridesRegisteredSetLazy() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(JavaTypeObject.class, fm ->
				fm.giveMeBuilder(JavaTypeObject.class).setLazy("string", () -> "fromLazy")
			)
			.build();

		// when
		JavaTypeObject actual = sut.giveMeBuilder(JavaTypeObject.class).set("string", "eager").sample();

		// then
		then(actual.getString()).isEqualTo("eager");
	}

	@Test
	void setOverridesRegisteredSetInner() {
		// given
		JavaTypeObject userElement1 = new JavaTypeObject();
		userElement1.setString("u1");
		JavaTypeObject userElement2 = new JavaTypeObject();
		userElement2.setString("u2");

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm ->
				fm
					.giveMeBuilder(ContainerObject.class)
					.setInner(new InnerSpec().property("complexList", l -> l.size(5)))
			)
			.build();

		// when
		ContainerObject actual = sut
			.giveMeBuilder(ContainerObject.class)
			.set("complexList", new java.util.ArrayList<>(Arrays.asList(userElement1, userElement2)))
			.sample();

		// then
		then(actual.getComplexList()).hasSize(2);
		then(actual.getComplexList().get(0).getString()).isEqualTo("u1");
		then(actual.getComplexList().get(1).getString()).isEqualTo("u2");
	}

	@Test
	void sizeOverridesRegisteredSetInner() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm ->
				fm
					.giveMeBuilder(ContainerObject.class)
					.setInner(new InnerSpec().property("complexList", l -> l.size(5)))
			)
			.build();

		// when
		ContainerObject actual = sut.giveMeBuilder(ContainerObject.class).size("complexList", 2).sample();

		// then
		then(actual.getComplexList()).hasSize(2);
	}

	@Test
	void setLazyOverridesRegisteredSetNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(JavaTypeObject.class, fm -> fm.giveMeBuilder(JavaTypeObject.class).setNull("string"))
			.build();

		// when
		JavaTypeObject actual = sut
			.giveMeBuilder(JavaTypeObject.class)
			.setLazy("string", () -> "fromLazy")
			.sample();

		// then
		then(actual.getString()).isEqualTo("fromLazy");
	}

	@Test
	void setNullOverridesRegisteredSetNotNull() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(JavaTypeObject.class, fm -> fm.giveMeBuilder(JavaTypeObject.class).setNotNull("string"))
			.build();

		// when
		JavaTypeObject actual = sut.giveMeBuilder(JavaTypeObject.class).setNull("string").sample();

		// then
		then(actual.getString()).isNull();
	}

	@Test
	void userSizeOverridesRegisteredSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm -> fm.giveMeBuilder(ContainerObject.class).size("complexList", 5))
			.build();

		// when
		ContainerObject actual = sut.giveMeBuilder(ContainerObject.class).size("complexList", 2).sample();

		// then
		then(actual.getComplexList()).hasSize(2);
	}

	@Test
	void registerOnlyAppliesWithoutUserOverride() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(JavaTypeObject.class, fm ->
				fm.giveMeBuilder(JavaTypeObject.class).set("string", "fromRegister").set("wrapperInteger", 99)
			)
			.build();

		// when
		JavaTypeObject actual = sut.giveMeOne(JavaTypeObject.class);

		// then
		then(actual.getString()).isEqualTo("fromRegister");
		then(actual.getWrapperInteger()).isEqualTo(99);
	}

	@Test
	void setSpecificElementOverridesRegisteredWildcard() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm ->
				fm.giveMeBuilder(ContainerObject.class).size("list", 3).set("list[*]", "registered")
			)
			.build();

		// when
		ContainerObject actual = sut.giveMeBuilder(ContainerObject.class).set("list[1]", "overridden").sample();

		// then
		then(actual.getList()).hasSize(3);
		then(actual.getList().get(1)).isEqualTo("overridden");
	}

	@Test
	void setRootObjectOverridesRegisteredField() {
		// register sets a field, user sets the whole root object via "$"
		// given
		JavaTypeObject wholeObject = new JavaTypeObject();
		wholeObject.setString("fromRootSet");
		wholeObject.setWrapperInteger(77);

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(JavaTypeObject.class, fm -> fm.giveMeBuilder(JavaTypeObject.class).set("string", "registered"))
			.build();

		// when
		JavaTypeObject actual = sut.giveMeBuilder(JavaTypeObject.class).set("$", wholeObject).sample();

		// then
		then(actual.getString()).isEqualTo("fromRootSet");
		then(actual.getWrapperInteger()).isEqualTo(77);
	}

	@Test
	void setFieldOverridesRegisteredRootObject() {
		// register sets the whole root object via "$", user sets a specific field
		// given
		JavaTypeObject registered = new JavaTypeObject();
		registered.setString("fromRegisterRoot");
		registered.setWrapperInteger(99);

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(JavaTypeObject.class, fm -> fm.giveMeBuilder(JavaTypeObject.class).set("$", registered))
			.build();

		// when
		JavaTypeObject actual = sut.giveMeBuilder(JavaTypeObject.class).set("string", "overridden").sample();

		// then
		then(actual.getString()).isEqualTo("overridden");
	}

	@Test
	void setRootObjectOverridesRegisteredRootObject() {
		// both register and user set whole root object via "$"
		// given
		JavaTypeObject registered = new JavaTypeObject();
		registered.setString("fromRegister");
		registered.setWrapperInteger(1);

		JavaTypeObject userObj = new JavaTypeObject();
		userObj.setString("fromUser");
		userObj.setWrapperInteger(2);

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(JavaTypeObject.class, fm -> fm.giveMeBuilder(JavaTypeObject.class).set("$", registered))
			.build();

		// when
		JavaTypeObject actual = sut.giveMeBuilder(JavaTypeObject.class).set("$", userObj).sample();

		// then
		then(actual.getString()).isEqualTo("fromUser");
		then(actual.getWrapperInteger()).isEqualTo(2);
	}

	@Test
	void setElementObjectOverridesRegisteredElementField() {
		// register sets a field inside a list element, user sets the whole element as object
		// given
		JavaTypeObject userElement = new JavaTypeObject();
		userElement.setString("userObj");
		userElement.setWrapperInteger(55);

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm ->
				fm
					.giveMeBuilder(ContainerObject.class)
					.size("complexList", 2)
					.set("complexList[0].string", "registered")
			)
			.build();

		// when
		ContainerObject actual = sut.giveMeBuilder(ContainerObject.class).set("complexList[0]", userElement).sample();

		// then
		then(actual.getComplexList().get(0).getString()).isEqualTo("userObj");
		then(actual.getComplexList().get(0).getWrapperInteger()).isEqualTo(55);
	}

	@Test
	void setElementFieldOverridesRegisteredElementObject() {
		// register sets the whole element as object, user sets a field inside it
		// given
		JavaTypeObject registeredElement = new JavaTypeObject();
		registeredElement.setString("regObj");
		registeredElement.setWrapperInteger(33);

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm ->
				fm.giveMeBuilder(ContainerObject.class).size("complexList", 2).set("complexList[0]", registeredElement)
			)
			.build();

		// when
		ContainerObject actual = sut
			.giveMeBuilder(ContainerObject.class)
			.set("complexList[0].string", "overridden")
			.sample();

		// then
		then(actual.getComplexList().get(0).getString()).isEqualTo("overridden");
	}

	@Test
	void setParentObjectOverridesRegisteredContainerField() {
		// register sets a container field, user sets the parent object containing that field
		// given
		JavaTypeObject childElement = new JavaTypeObject();
		childElement.setString("e1");

		ContainerObject userParent = new ContainerObject();
		userParent.setList(Arrays.asList("x", "y"));
		userParent.setComplexList(new java.util.ArrayList<>(Arrays.asList(childElement)));

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm ->
				fm.giveMeBuilder(ContainerObject.class).size("list", 5).set("list[*]", "registered")
			)
			.build();

		// when
		ContainerObject actual = sut.giveMeBuilder(ContainerObject.class).set("$", userParent).sample();

		// then
		then(actual.getList()).hasSize(2);
		then(actual.getList()).containsExactly("x", "y");
		then(actual.getComplexList()).hasSize(1);
		then(actual.getComplexList().get(0).getString()).isEqualTo("e1");
	}

	// ================================
	// Container setNull combinations
	// ================================

	@Test
	void setNullContainerOverridesRegisteredSetContainer() {
		// given
		JavaTypeObject registeredElement = new JavaTypeObject();
		registeredElement.setString("r1");

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm ->
				fm.giveMeBuilder(ContainerObject.class)
					.set("complexList", new java.util.ArrayList<>(Arrays.asList(registeredElement)))
			)
			.build();

		// when
		ContainerObject actual = sut.giveMeBuilder(ContainerObject.class).setNull("complexList").sample();

		// then
		then(actual.getComplexList()).isNull();
	}

	@Test
	void setNullContainerOverridesRegisteredSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm -> fm.giveMeBuilder(ContainerObject.class).size("complexList", 5))
			.build();

		// when
		ContainerObject actual = sut.giveMeBuilder(ContainerObject.class).setNull("complexList").sample();

		// then
		then(actual.getComplexList()).isNull();
	}

	@Test
	void setContainerOverridesRegisteredSetNull() {
		// given
		JavaTypeObject userElement = new JavaTypeObject();
		userElement.setString("u1");

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm -> fm.giveMeBuilder(ContainerObject.class).setNull("complexList"))
			.build();

		// when
		ContainerObject actual = sut
			.giveMeBuilder(ContainerObject.class)
			.set("complexList", new java.util.ArrayList<>(Arrays.asList(userElement)))
			.sample();

		// then
		then(actual.getComplexList()).hasSize(1);
		then(actual.getComplexList().get(0).getString()).isEqualTo("u1");
	}

	@Test
	void sizeOverridesRegisteredSetNullContainer() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm -> fm.giveMeBuilder(ContainerObject.class).setNull("complexList"))
			.build();

		// when
		ContainerObject actual = sut.giveMeBuilder(ContainerObject.class).size("complexList", 3).sample();

		// then
		then(actual.getComplexList()).hasSize(3);
	}

	// ================================
	// Container setLazy combinations
	// ================================

	@Test
	void setContainerOverridesRegisteredSetLazyContainer() {
		// given
		JavaTypeObject userElement = new JavaTypeObject();
		userElement.setString("u1");

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm ->
				fm.giveMeBuilder(ContainerObject.class).setLazy("list", () -> Arrays.asList("lazy1", "lazy2", "lazy3"))
			)
			.build();

		// when
		ContainerObject actual = sut
			.giveMeBuilder(ContainerObject.class)
			.set("list", new java.util.ArrayList<>(Arrays.asList("eager")))
			.sample();

		// then
		then(actual.getList()).hasSize(1);
		then(actual.getList().get(0)).isEqualTo("eager");
	}

	@RepeatedTest(TEST_COUNT)
	void setLazyContainerOverridesRegisteredSetContainer() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm ->
				fm
					.giveMeBuilder(ContainerObject.class)
					.set("list", new java.util.ArrayList<>(Arrays.asList("reg1", "reg2")))
			)
			.build();

		// when
		ContainerObject actual = sut
			.giveMeBuilder(ContainerObject.class)
			.setLazy("list", () -> Arrays.asList("lazy1"))
			.sample();

		// then
		then(actual.getList()).hasSize(1);
		then(actual.getList().get(0)).isEqualTo("lazy1");
	}

	// ================================
	// Wildcard field path combinations
	// ================================

	@Test
	void setSpecificElementFieldOverridesRegisteredWildcardField() {
		// register sets all elements' field via wildcard, user sets specific element's field
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm ->
				fm.giveMeBuilder(ContainerObject.class).size("complexList", 3).set("complexList[*].string", "wildcard")
			)
			.build();

		// when
		ContainerObject actual = sut
			.giveMeBuilder(ContainerObject.class)
			.set("complexList[1].string", "specific")
			.sample();

		// then
		then(actual.getComplexList()).hasSize(3);
		then(actual.getComplexList().get(1).getString()).isEqualTo("specific");
	}

	@Test
	void setWildcardFieldOverridesRegisteredSpecificElementField() {
		// register sets specific element's field, user sets all via wildcard
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm ->
				fm.giveMeBuilder(ContainerObject.class).size("complexList", 3).set("complexList[0].string", "specific")
			)
			.build();

		// when
		ContainerObject actual = sut
			.giveMeBuilder(ContainerObject.class)
			.set("complexList[*].string", "wildcard")
			.sample();

		// then
		then(actual.getComplexList()).hasSize(3);
		for (JavaTypeObject elem : actual.getComplexList()) {
			then(elem.getString()).isEqualTo("wildcard");
		}
	}

	// ================================
	// setInner vs setInner
	// ================================

	@Test
	void setInnerOverridesRegisteredSetInner() {
		// both register and user use setInner on same container
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm ->
				fm
					.giveMeBuilder(ContainerObject.class)
					.setInner(new InnerSpec().property("map", m -> m.size(5).entry("regKey", 999)))
			)
			.build();

		// when
		ContainerObject actual = sut
			.giveMeBuilder(ContainerObject.class)
			.setInner(new InnerSpec().property("map", m -> m.size(2).entry("userKey", 1)))
			.sample();

		// then
		then(actual.getMap()).hasSize(2);
		then(actual.getMap()).containsKey("userKey");
	}

	// ================================
	// Size range combinations
	// ================================

	@RepeatedTest(TEST_COUNT)
	void exactSizeOverridesRegisteredSizeRange() {
		// register uses size range, user uses exact size
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm -> fm.giveMeBuilder(ContainerObject.class).size("list", 1, 10))
			.build();

		// when
		ContainerObject actual = sut.giveMeBuilder(ContainerObject.class).size("list", 3).sample();

		// then
		then(actual.getList()).hasSize(3);
	}

	// ================================
	// Edge cases: empty container, size 0
	// ================================

	@Test
	void sizeExpandsRegisteredEmptyContainer() {
		// register sets empty container, user expands with size
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm ->
				fm.giveMeBuilder(ContainerObject.class).set("list", new java.util.ArrayList<>())
			)
			.build();

		// when
		ContainerObject actual = sut.giveMeBuilder(ContainerObject.class).size("list", 3).sample();

		// then
		then(actual.getList()).hasSize(3);
	}

	@Test
	void setNonEmptyContainerOverridesRegisteredSizeZero() {
		// register sets size to 0, user sets non-empty container
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm -> fm.giveMeBuilder(ContainerObject.class).size("list", 0))
			.build();

		// when
		ContainerObject actual = sut
			.giveMeBuilder(ContainerObject.class)
			.set("list", new java.util.ArrayList<>(Arrays.asList("a", "b")))
			.sample();

		// then
		then(actual.getList()).hasSize(2);
		then(actual.getList()).containsExactly("a", "b");
	}

	// ================================
	// Dual register: parent + child type
	// ================================

	@Test
	void dualRegisterBothApplyWithoutUserOverride() {
		// register ContainerObject (size) + register JavaTypeObject (field)
		// both should apply when no user override
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm -> fm.giveMeBuilder(ContainerObject.class).size("complexList", 3))
			.register(JavaTypeObject.class, fm ->
				fm.giveMeBuilder(JavaTypeObject.class).set("string", "fromInnerRegister")
			)
			.build();

		// when
		ContainerObject actual = sut.giveMeOne(ContainerObject.class);

		// then
		then(actual.getComplexList()).hasSize(3);
		for (JavaTypeObject elem : actual.getComplexList()) {
			then(elem.getString()).isEqualTo("fromInnerRegister");
		}
	}

	@Test
	void dualRegisterUserOverridesElementField() {
		// register ContainerObject (size) + register JavaTypeObject (field)
		// user overrides specific element's field
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm -> fm.giveMeBuilder(ContainerObject.class).size("complexList", 3))
			.register(JavaTypeObject.class, fm ->
				fm.giveMeBuilder(JavaTypeObject.class).set("string", "fromInnerRegister")
			)
			.build();

		// when
		ContainerObject actual = sut
			.giveMeBuilder(ContainerObject.class)
			.set("complexList[0].string", "userOverride")
			.sample();

		// then
		then(actual.getComplexList()).hasSize(3);
		then(actual.getComplexList().get(0).getString()).isEqualTo("userOverride");
		then(actual.getComplexList().get(1).getString()).isEqualTo("fromInnerRegister");
		then(actual.getComplexList().get(2).getString()).isEqualTo("fromInnerRegister");
	}

	@Test
	void dualRegisterUserOverridesElementObject() {
		// register ContainerObject (size) + register JavaTypeObject (field)
		// user overrides a whole element object — should bypass inner register
		// given
		JavaTypeObject userObj = new JavaTypeObject();
		userObj.setString("userObj");
		userObj.setWrapperInteger(77);

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm -> fm.giveMeBuilder(ContainerObject.class).size("complexList", 3))
			.register(JavaTypeObject.class, fm ->
				fm.giveMeBuilder(JavaTypeObject.class).set("string", "fromInnerRegister")
			)
			.build();

		// when
		ContainerObject actual = sut.giveMeBuilder(ContainerObject.class).set("complexList[0]", userObj).sample();

		// then
		then(actual.getComplexList()).hasSize(3);
		then(actual.getComplexList().get(0).getString()).isEqualTo("userObj");
		then(actual.getComplexList().get(0).getWrapperInteger()).isEqualTo(77);
		then(actual.getComplexList().get(1).getString()).isEqualTo("fromInnerRegister");
	}

	@Test
	void dualRegisterUserOverridesWholeContainer() {
		// register ContainerObject (container set) + register JavaTypeObject (field)
		// user overrides whole container — should bypass both registers
		// given
		JavaTypeObject userElement = new JavaTypeObject();
		userElement.setString("u1");

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm -> fm.giveMeBuilder(ContainerObject.class).size("complexList", 5))
			.register(JavaTypeObject.class, fm ->
				fm.giveMeBuilder(JavaTypeObject.class).set("string", "fromInnerRegister")
			)
			.build();

		// when
		ContainerObject actual = sut
			.giveMeBuilder(ContainerObject.class)
			.set("complexList", new java.util.ArrayList<>(Arrays.asList(userElement)))
			.sample();

		// then
		then(actual.getComplexList()).hasSize(1);
		then(actual.getComplexList().get(0).getString()).isEqualTo("u1");
	}

	@Test
	void dualRegisterUserOverridesContainerSize() {
		// register ContainerObject (size=5) + register JavaTypeObject (field)
		// user overrides only size — inner register should still apply
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm -> fm.giveMeBuilder(ContainerObject.class).size("complexList", 5))
			.register(JavaTypeObject.class, fm ->
				fm.giveMeBuilder(JavaTypeObject.class).set("string", "fromInnerRegister")
			)
			.build();

		// when
		ContainerObject actual = sut.giveMeBuilder(ContainerObject.class).size("complexList", 2).sample();

		// then
		then(actual.getComplexList()).hasSize(2);
		for (JavaTypeObject elem : actual.getComplexList()) {
			then(elem.getString()).isEqualTo("fromInnerRegister");
		}
	}

	@Test
	void dualRegisterParentSetAndChildRootSet() {
		// register ContainerObject (set container field) + register JavaTypeObject (set "$" root)
		// given
		JavaTypeObject registeredRoot = new JavaTypeObject();
		registeredRoot.setString("rootObj");
		registeredRoot.setWrapperInteger(42);

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm -> fm.giveMeBuilder(ContainerObject.class).size("complexList", 2))
			.register(JavaTypeObject.class, fm -> fm.giveMeBuilder(JavaTypeObject.class).set("$", registeredRoot))
			.build();

		// when
		ContainerObject actual = sut.giveMeOne(ContainerObject.class);

		// then
		then(actual.getComplexList()).hasSize(2);
		for (JavaTypeObject elem : actual.getComplexList()) {
			then(elem.getString()).isEqualTo("rootObj");
			then(elem.getWrapperInteger()).isEqualTo(42);
		}
	}

	// ================================
	// Arbitrary set combinations
	// ================================

	@RepeatedTest(TEST_COUNT)
	void setValueOverridesRegisteredArbitrary() {
		// register sets field via Arbitrary, user sets plain value
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(JavaTypeObject.class, fm ->
				fm.giveMeBuilder(JavaTypeObject.class).set("wrapperInteger", Arbitraries.integers().between(100, 200))
			)
			.build();

		// when
		JavaTypeObject actual = sut.giveMeBuilder(JavaTypeObject.class).set("wrapperInteger", 42).sample();

		// then
		then(actual.getWrapperInteger()).isEqualTo(42);
	}

	@RepeatedTest(TEST_COUNT)
	void setArbitraryOverridesRegisteredValue() {
		// register sets plain value, user sets via Arbitrary
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(JavaTypeObject.class, fm -> fm.giveMeBuilder(JavaTypeObject.class).set("wrapperInteger", 999))
			.build();

		// when
		JavaTypeObject actual = sut
			.giveMeBuilder(JavaTypeObject.class)
			.set("wrapperInteger", Arbitraries.integers().between(1, 5))
			.sample();

		// then
		then(actual.getWrapperInteger()).isBetween(1, 5);
	}

	@RepeatedTest(TEST_COUNT)
	void setArbitraryOverridesRegisteredArbitrary() {
		// both register and user set via Arbitrary
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(JavaTypeObject.class, fm ->
				fm.giveMeBuilder(JavaTypeObject.class).set("wrapperInteger", Arbitraries.integers().between(100, 200))
			)
			.build();

		// when
		JavaTypeObject actual = sut
			.giveMeBuilder(JavaTypeObject.class)
			.set("wrapperInteger", Arbitraries.integers().between(1, 5))
			.sample();

		// then
		then(actual.getWrapperInteger()).isBetween(1, 5);
	}

	@RepeatedTest(TEST_COUNT)
	void registeredArbitraryAppliesWithoutUserOverride() {
		// register sets via Arbitrary, no user override — Arbitrary should generate
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(JavaTypeObject.class, fm ->
				fm.giveMeBuilder(JavaTypeObject.class).set("wrapperInteger", Arbitraries.integers().between(100, 200))
			)
			.build();

		// when
		JavaTypeObject actual = sut.giveMeOne(JavaTypeObject.class);

		// then
		then(actual.getWrapperInteger()).isBetween(100, 200);
	}

	@RepeatedTest(TEST_COUNT)
	void dualRegisterUserSetsArbitraryOnElement() {
		// dual register + user sets Arbitrary on a specific element field
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm -> fm.giveMeBuilder(ContainerObject.class).size("complexList", 3))
			.register(JavaTypeObject.class, fm -> fm.giveMeBuilder(JavaTypeObject.class).set("wrapperInteger", 999))
			.build();

		// when
		ContainerObject actual = sut
			.giveMeBuilder(ContainerObject.class)
			.set("complexList[0].wrapperInteger", Arbitraries.integers().between(1, 5))
			.sample();

		// then
		then(actual.getComplexList()).hasSize(3);
		then(actual.getComplexList().get(0).getWrapperInteger()).isBetween(1, 5);
		then(actual.getComplexList().get(1).getWrapperInteger()).isEqualTo(999);
		then(actual.getComplexList().get(2).getWrapperInteger()).isEqualTo(999);
	}

	@Test
	void setElementOverridesRegisteredArbitraryOnWildcard() {
		// register sets Arbitrary via wildcard, user sets specific element
		// given
		JavaTypeObject userObj = new JavaTypeObject();
		userObj.setString("userObj");

		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm ->
				fm
					.giveMeBuilder(ContainerObject.class)
					.size("complexList", 3)
					.set("complexList[*].string", Arbitraries.strings().alpha().ofLength(10))
			)
			.build();

		// when
		ContainerObject actual = sut.giveMeBuilder(ContainerObject.class).set("complexList[1]", userObj).sample();

		// then
		then(actual.getComplexList()).hasSize(3);
		then(actual.getComplexList().get(1).getString()).isEqualTo("userObj");
	}

	@Test
	void sizeOverridesRegisteredArbitraryContainer() {
		// register sets container via Arbitrary.just(list), user overrides with size
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.plugin(new JavaNodeTreeAdapterPlugin())
			.register(ContainerObject.class, fm ->
				fm
					.giveMeBuilder(ContainerObject.class)
					.set("list", Arbitraries.just(new java.util.ArrayList<>(Arrays.asList("a"))))
			)
			.build();

		// when
		ContainerObject actual = sut.giveMeBuilder(ContainerObject.class).size("list", 4).sample();

		// then
		then(actual.getList()).hasSize(4);
	}

	@lombok.Data
	static class NestedListObject {

		private List<List<String>> nestedList;
	}
}
