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
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.exception.FilterMissException;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FailoverIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.customizer.InnerSpec;
import com.navercorp.fixturemonkey.customizer.Values;
import com.navercorp.fixturemonkey.resolver.ArbitraryBuilderCandidateFactory;
import com.navercorp.fixturemonkey.resolver.ArbitraryBuilderCandidateList;
import com.navercorp.fixturemonkey.tests.java.ImmutableDepthTestSpecs.DepthStringValueList;
import com.navercorp.fixturemonkey.tests.java.ImmutableDepthTestSpecs.OneDepthStringValue;
import com.navercorp.fixturemonkey.tests.java.ImmutableDepthTestSpecs.TwoDepthStringValue;
import com.navercorp.fixturemonkey.tests.java.ImmutableGenericTypeSpecs.GenericArrayObject;
import com.navercorp.fixturemonkey.tests.java.ImmutableGenericTypeSpecs.GenericImplementationObject;
import com.navercorp.fixturemonkey.tests.java.ImmutableGenericTypeSpecs.GenericObject;
import com.navercorp.fixturemonkey.tests.java.ImmutableGenericTypeSpecs.ThreeGenericObject;
import com.navercorp.fixturemonkey.tests.java.ImmutableGenericTypeSpecs.TwoGenericImplementationObject;
import com.navercorp.fixturemonkey.tests.java.ImmutableGenericTypeSpecs.TwoGenericObject;
import com.navercorp.fixturemonkey.tests.java.ImmutableJavaTestSpecs.ContainerObject;
import com.navercorp.fixturemonkey.tests.java.ImmutableJavaTestSpecs.Enum;
import com.navercorp.fixturemonkey.tests.java.ImmutableJavaTestSpecs.JavaTypeObject;
import com.navercorp.fixturemonkey.tests.java.ImmutableJavaTestSpecs.RootJavaTypeObject;
import com.navercorp.fixturemonkey.tests.java.ImmutableJavaTestSpecs.SetImplementationWithoutGeneric;
import com.navercorp.fixturemonkey.tests.java.ImmutableMixedIntrospectorsTypeSpecs.MixedJavaTypeObject;
import com.navercorp.fixturemonkey.tests.java.ImmutableRecursiveTypeSpecs.SelfRecursiveListObject;
import com.navercorp.fixturemonkey.tests.java.ImmutableRecursiveTypeSpecs.SelfRecursiveObject;

@SuppressWarnings("rawtypes")
class JavaTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@RepeatedTest(TEST_COUNT)
	void sampleGenericObjectWithoutGeneric() {
		GenericObject actual = SUT.giveMeOne(GenericObject.class);

		then(actual).isNotNull();
		then(actual.getValue()).isInstanceOf(Object.class);
	}

	@RepeatedTest(TEST_COUNT)
	void fixedGenericObjectWithoutGeneric() {
		GenericObject actual = SUT.giveMeBuilder(GenericObject.class)
			.fixed()
			.sample();

		then(actual).isNotNull();
		then(actual.getValue()).isInstanceOf(Object.class);
	}

	@RepeatedTest(TEST_COUNT)
	void sampleGenericObject() {
		String actual = SUT.giveMeOne(new TypeReference<GenericObject<String>>() {
			})
			.getValue();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedGenericObject() {
		String actual = SUT.giveMeBuilder(new TypeReference<GenericObject<String>>() {
			})
			.fixed()
			.sample()
			.getValue();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleGenericImplementationObjectWithoutGeneric() {
		GenericImplementationObject actual = SUT.giveMeOne(GenericImplementationObject.class);

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedGenericImplementationObjectWithoutGeneric() {
		GenericImplementationObject actual = SUT.giveMeBuilder(GenericImplementationObject.class)
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleGenericImplementationObject() {
		String actual = SUT.giveMeOne(new TypeReference<GenericImplementationObject<String>>() {
			})
			.getValue();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedGenericImplementationObject() {
		String actual = SUT.giveMeBuilder(new TypeReference<GenericImplementationObject<String>>() {
			})
			.fixed()
			.sample()
			.getValue();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleTwoGenericImplementationObjectWithoutGeneric() {
		TwoGenericImplementationObject actual = SUT.giveMeOne(TwoGenericImplementationObject.class);

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedTwoGenericImplementationObjectWithoutGeneric() {
		TwoGenericImplementationObject actual = SUT.giveMeBuilder(TwoGenericImplementationObject.class)
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleTwoGenericImplementationObject() {
		TwoGenericImplementationObject<String, Integer> actual = SUT.giveMeOne(
			new TypeReference<TwoGenericImplementationObject<String, Integer>>() {
			}
		);

		then(actual.getUValue()).isNotNull();
		then(actual.getTValue()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedTwoGenericImplementationObject() {
		TwoGenericImplementationObject<String, Integer> actual = SUT.giveMeBuilder(
				new TypeReference<TwoGenericImplementationObject<String, Integer>>() {
				}
			)
			.fixed()
			.sample();

		then(actual.getUValue()).isNotNull();
		then(actual.getTValue()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleSelfRecursiveObject() {
		SelfRecursiveObject actual = SUT.giveMeOne(SelfRecursiveObject.class);

		then(actual.getValue()).isNotNull();
		then(actual.getSelfRecursiveObject()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedSelfRecursiveObject() {
		SelfRecursiveObject actual = SUT.giveMeBuilder(SelfRecursiveObject.class)
			.fixed()
			.sample();

		then(actual.getValue()).isNotNull();
		then(actual.getSelfRecursiveObject()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleSelfRecursiveListObject() {
		SelfRecursiveListObject actual = SUT.giveMeOne(SelfRecursiveListObject.class);

		then(actual.getValue()).isNotNull();
		then(actual.getSelfRecursiveListObjects()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedSelfRecursiveListObject() {
		SelfRecursiveListObject actual = SUT.giveMeBuilder(SelfRecursiveListObject.class)
			.fixed()
			.sample();

		then(actual.getValue()).isNotNull();
		then(actual.getSelfRecursiveListObjects()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleContainerType() {
		ContainerObject actual = SUT.giveMeOne(ContainerObject.class);

		then(actual.getPrimitiveArray()).isNotNull();
		then(actual.getArray()).isNotNull();
		then(actual.getComplexArray()).isNotNull();
		then(actual.getList()).isNotNull();
		then(actual.getComplexList()).isNotNull();
		then(actual.getSet()).isNotNull();
		then(actual.getComplexSet()).isNotNull();
		then(actual.getMap()).isNotNull();
		then(actual.getComplexMap()).isNotNull();
		then(actual.getMapEntry()).isNotNull();
		then(actual.getComplexMapEntry()).isNotNull();
		then(actual.getOptional()).isNotNull();
		then(actual.getOptionalInt()).isNotNull();
		then(actual.getOptionalLong()).isNotNull();
		then(actual.getOptionalDouble()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleListType() {
		List<JavaTypeObject> actual = SUT.giveMeOne(new TypeReference<List<JavaTypeObject>>() {
		});

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedListType() {
		List<JavaTypeObject> actual = SUT.giveMeBuilder(new TypeReference<List<JavaTypeObject>>() {
			})
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleSetType() {
		Set<JavaTypeObject> actual = SUT.giveMeOne(new TypeReference<Set<JavaTypeObject>>() {
		});

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedSetType() {
		Set<JavaTypeObject> actual = SUT.giveMeBuilder(new TypeReference<Set<JavaTypeObject>>() {
			})
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleArrayType() {
		JavaTypeObject[] actual = SUT.giveMeOne(new TypeReference<JavaTypeObject[]>() {
		});

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedArrayType() {
		JavaTypeObject[] actual = SUT.giveMeBuilder(new TypeReference<JavaTypeObject[]>() {
			})
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleOptionalType() {
		Optional<JavaTypeObject> actual = SUT.giveMeOne(new TypeReference<Optional<JavaTypeObject>>() {
		});

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedOptionalType() {
		Optional<JavaTypeObject> actual = SUT.giveMeBuilder(new TypeReference<Optional<JavaTypeObject>>() {
			})
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleMapType() {
		Map<String, JavaTypeObject> actual = SUT.giveMeOne(new TypeReference<Map<String, JavaTypeObject>>() {
		});

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedMapType() {
		Map<String, JavaTypeObject> actual = SUT.giveMeBuilder(new TypeReference<Map<String, JavaTypeObject>>() {
			})
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleMapEntryType() {
		Map.Entry<String, JavaTypeObject> actual = SUT.giveMeOne(
			new TypeReference<Map.Entry<String, JavaTypeObject>>() {
			});

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedMapEntryType() {
		Map.Entry<String, JavaTypeObject> actual = SUT.giveMeBuilder(
				new TypeReference<Map.Entry<String, JavaTypeObject>>() {
				})
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleSetImplementationWithoutGeneric() {
		String actual = SUT.giveMeOne(SetImplementationWithoutGeneric.class)
			.getValue();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedSetImplementationWithoutGeneric() {
		String actual = SUT.giveMeBuilder(SetImplementationWithoutGeneric.class)
			.fixed()
			.sample()
			.getValue();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleGenericArrayType() {
		GenericImplementationObject<String>[] values = SUT.giveMeOne(new TypeReference<GenericArrayObject<String>>() {
			})
			.getValues();

		then(values).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedGenericArrayType() {
		GenericImplementationObject<String>[] values = SUT.giveMeBuilder(
				new TypeReference<GenericArrayObject<String>>() {
				})
			.fixed()
			.sample()
			.getValues();

		then(values).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleTwoGenericObject() {
		TwoGenericObject<String, Integer> actual = SUT.giveMeOne(
			new TypeReference<TwoGenericObject<String, Integer>>() {
			});

		then(actual.getValue1()).isNotNull();
		then(actual.getValue2()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedTwoGenericObject() {
		TwoGenericObject<String, Integer> actual = SUT.giveMeBuilder(
				new TypeReference<TwoGenericObject<String, Integer>>() {

				})
			.fixed()
			.sample();

		then(actual.getValue1()).isNotNull();
		then(actual.getValue2()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleTwoGenericObjectProperty() {
		TwoGenericObject<String, Integer> actual = SUT.giveMeOne(
				new TypeReference<GenericObject<TwoGenericObject<String, Integer>>>() {
				})
			.getValue();

		then(actual.getValue1()).isNotNull();
		then(actual.getValue2()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedTwoGenericObjectProperty() {
		TwoGenericObject<String, Integer> actual = SUT.giveMeOne(
				new TypeReference<GenericObject<TwoGenericObject<String, Integer>>>() {
				})
			.getValue();

		then(actual.getValue1()).isNotNull();
		then(actual.getValue2()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void sampleThreeGenericObjectProperty() {
		ThreeGenericObject<String, Integer, Instant> actual = SUT.giveMeOne(
				new TypeReference<GenericObject<ThreeGenericObject<String, Integer, Instant>>>() {
				})
			.getValue();

		then(actual.getValue1()).isNotNull();
		then(actual.getValue2()).isNotNull();
		then(actual.getValue3()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedThreeGenericObjectProperty() {
		ThreeGenericObject<String, Integer, Instant> actual = SUT.giveMeOne(
				new TypeReference<GenericObject<ThreeGenericObject<String, Integer, Instant>>>() {
				})
			.getValue();

		then(actual.getValue1()).isNotNull();
		then(actual.getValue2()).isNotNull();
		then(actual.getValue3()).isNotNull();
	}

	@Test
	void sampleUniqueSet() {
		Set<String> actual = SUT.giveMeBuilder(new TypeReference<Set<String>>() {
			})
			.size("$", 200)
			.sample();

		then(actual).hasSize(200);
	}

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
		).isExactlyInstanceOf(FilterMissException.class);
	}

	@RepeatedTest(TEST_COUNT)
	void setEnumSet() {
		Set<Enum> set = new HashSet<>();
		set.add(Enum.ONE);
		set.add(Enum.TWO);
		set.add(Enum.THREE);

		Set<Enum> actual = SUT.giveMeBuilder(new TypeReference<Set<Enum>>() {
			})
			.set("$", set)
			.sample();

		then(actual).hasSize(3);
	}

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

	@Test
	void logRoot() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(String.class)
				.setPostCondition(it -> it.equals("test"))
				.sample()
		)
			.hasMessageContaining("\"$\"");
	}

	@Test
	void logProperty() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(JavaTypeObject.class)
				.setPostCondition("string", String.class, it -> it.equals("test"))
				.sample()
		)
			.hasMessageContaining("\"string\"");
	}

	@Test
	void logNestedProperty() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(RootJavaTypeObject.class)
				.setPostCondition("value.string", String.class, it -> it.equals("test"))
				.sample()
		)
			.hasMessageContaining("\"value.string\"");
	}

	@Test
	void logArrayElement() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(ContainerObject.class)
				.size("array", 1)
				.setPostCondition("array[0]", String.class, it -> it.equals("test"))
				.sample()
		)
			.hasMessageContaining("\"array[0]\"");
	}

	@Test
	void logListElement() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(ContainerObject.class)
				.size("list", 1)
				.setPostCondition("list[0]", String.class, it -> it.equals("test"))
				.sample()
		)
			.hasMessageContaining("\"list[0]\"");
	}

	@Test
	void logListElementProperty() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(ContainerObject.class)
				.size("complexList", 1)
				.setPostCondition("complexList[0].string", String.class, it -> it.equals("test"))
				.sample()
		)
			.hasMessageContaining("\"complexList[0].string\"");
	}

	@Test
	void logMapElementKeyProperty() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(ContainerObject.class)
				.setInner(
					new InnerSpec()
						.property("map", m ->
							m.size(1)
								.key(v -> v.postCondition(String.class, it -> it.equals("test")))
						)
				)
				.sample()
		)
			.hasMessageContaining("\"map{key}\"");
	}

	@Test
	void logMapElementValueProperty() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(ContainerObject.class)
				.setInner(
					new InnerSpec()
						.property("map", m ->
							m.size(1)
								.value(v -> v.postCondition(Integer.class, it -> it == -987654321))
						)
				)
				.sample()
		)
			.hasMessageContaining("\"map{value}\"");
	}

	@RepeatedTest(TEST_COUNT)
	void sampleUniqueList() {
		List<String> actual = SUT.giveMeBuilder(new TypeReference<List<String>>() {
			})
			.size("$", 100)
			.set(
				"$[*]",
				Values.just(CombinableArbitrary.from(LazyArbitrary.lazy(() -> Arbitraries.strings().sample())).unique())
			)
			.sample();

		Set<String> expected = new HashSet<>(actual);
		then(actual).hasSameSizeAs(expected);
	}
}
