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

import static com.navercorp.fixturemonkey.api.experimental.JavaGetterMethodPropertySelector.javaGetter;
import static com.navercorp.fixturemonkey.api.experimental.TypedExpressionGenerator.typedRoot;
import static com.navercorp.fixturemonkey.api.experimental.TypedExpressionGenerator.typedString;
import static com.navercorp.fixturemonkey.api.instantiator.Instantiator.constructor;
import static com.navercorp.fixturemonkey.api.instantiator.Instantiator.factoryMethod;
import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.exception.RetryableFilterMissException;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.CompositeArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FailoverIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.customizer.InnerSpec;
import com.navercorp.fixturemonkey.customizer.Values;
import com.navercorp.fixturemonkey.javax.validation.plugin.JavaxValidationPlugin;
import com.navercorp.fixturemonkey.resolver.ArbitraryBuilderCandidateFactory;
import com.navercorp.fixturemonkey.resolver.ArbitraryBuilderCandidateList;
import com.navercorp.fixturemonkey.tests.java.ConstructorAndPropertyTestSpecs.ConsturctorAndProperty;
import com.navercorp.fixturemonkey.tests.java.ConstructorTestSpecs.FieldAndConstructorParameterMismatchObject;
import com.navercorp.fixturemonkey.tests.java.ConstructorTestSpecs.JavaxValidationObject;
import com.navercorp.fixturemonkey.tests.java.ConstructorTestSpecs.SimpleContainerObject;
import com.navercorp.fixturemonkey.tests.java.ImmutableDepthTestSpecs.DepthStringValueList;
import com.navercorp.fixturemonkey.tests.java.ImmutableDepthTestSpecs.OneDepthStringValue;
import com.navercorp.fixturemonkey.tests.java.ImmutableDepthTestSpecs.TwoDepthStringValue;
import com.navercorp.fixturemonkey.tests.java.ImmutableFunctionalInterfaceSpecs.FunctionObject;
import com.navercorp.fixturemonkey.tests.java.ImmutableFunctionalInterfaceSpecs.SupplierObject;
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
import com.navercorp.fixturemonkey.tests.java.ImmutableRecursiveTypeSpecs.SelfRecursiveMapObject;
import com.navercorp.fixturemonkey.tests.java.ImmutableRecursiveTypeSpecs.SelfRecursiveObject;
import com.navercorp.fixturemonkey.tests.java.InterfaceTestSpecs.InterfaceIntegerObject;
import com.navercorp.fixturemonkey.tests.java.InterfaceTestSpecs.InterfaceListObject;
import com.navercorp.fixturemonkey.tests.java.InterfaceTestSpecs.InterfaceObject;
import com.navercorp.fixturemonkey.tests.java.InterfaceTestSpecs.InterfaceStringObject;
import com.navercorp.fixturemonkey.tests.java.InterfaceTestSpecs.InterfaceWrapperObject;
import com.navercorp.fixturemonkey.tests.java.MutableJavaTestSpecs.ConstantObject;
import com.navercorp.fixturemonkey.tests.java.NestedClassTestSpecs.Inner;
import com.navercorp.fixturemonkey.tests.java.NoArgsConstructorJavaTestSpecs.NestedObject;

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
		)
			.getCause()
			.isExactlyInstanceOf(RetryableFilterMissException.class);
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
			.getCause()
			.hasMessageContaining("\"$\"");
	}

	@Test
	void logProperty() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(JavaTypeObject.class)
				.setPostCondition("string", String.class, it -> it.equals("test"))
				.sample()
		)
			.getCause()
			.hasMessageContaining("\"string\"");
	}

	@Test
	void logNestedProperty() {
		thenThrownBy(
			() -> SUT.giveMeBuilder(RootJavaTypeObject.class)
				.setPostCondition("value.string", String.class, it -> it.equals("test"))
				.sample()
		)
			.getCause()
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
			.getCause()
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
			.getCause()
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
			.getCause()
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
			.getCause()
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
			.getCause()
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

	@RepeatedTest(TEST_COUNT)
	void sampleSelfRecursiveMapObject() {
		Map<Integer, SelfRecursiveMapObject> actual = SUT.giveMeOne(
			new TypeReference<Map<Integer, SelfRecursiveMapObject>>() {
			});

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void fixedSelfRecursiveMapObject() {
		Map<Integer, SelfRecursiveMapObject> actual = SUT.giveMeBuilder(
				new TypeReference<Map<Integer, SelfRecursiveMapObject>>() {
				})
			.fixed()
			.sample();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void setSelfRecursiveObjectList() {
		List<SelfRecursiveListObject> expected = SUT.giveMeOne(
			new TypeReference<List<SelfRecursiveListObject>>() {
			});

		List<SelfRecursiveListObject> actual = SUT.giveMeBuilder(new TypeReference<List<SelfRecursiveListObject>>() {
			})
			.size("$", 1)
			.set("$[0].selfRecursiveListObjects", expected)
			.sample()
			.get(0)
			.getSelfRecursiveListObjects();

		then(actual).isEqualTo(expected);
	}

	@RepeatedTest(TEST_COUNT)
	void setNestedSelfRecursiveObjectList() {
		List<SelfRecursiveListObject> expected = SUT.giveMeBuilder(
				new TypeReference<List<SelfRecursiveListObject>>() {
				}
			)
			.size("$", 1)
			.set("$[0].selfRecursiveListObjects", SUT.giveMeOne(new TypeReference<List<SelfRecursiveListObject>>() {
			}))
			.sample();

		List<SelfRecursiveListObject> actual = SUT.giveMeBuilder(new TypeReference<List<SelfRecursiveListObject>>() {
			})
			.size("$", 1)
			.set("$[0].selfRecursiveListObjects", expected)
			.sample()
			.get(0)
			.getSelfRecursiveListObjects();

		then(actual).isEqualTo(expected);
	}

	@RepeatedTest(TEST_COUNT)
	void setSelfRecursiveObject() {
		SelfRecursiveObject actual = SUT.giveMeOne(SelfRecursiveObject.class);

		SelfRecursiveObject expected = SUT.giveMeBuilder(SelfRecursiveObject.class)
			.set("selfRecursiveObject", actual)
			.sample()
			.getSelfRecursiveObject();

		then(actual).isEqualTo(expected);
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

	@RepeatedTest(TEST_COUNT)
	void instantiateParametersInOrder() {
		String actual = SUT.giveMeBuilder(ConstructorTestSpecs.JavaTypeObject.class)
			.instantiate(
				ConstructorTestSpecs.JavaTypeObject.class,
				constructor()
					.parameter(int.class)
					.parameter(float.class)
					.parameter(long.class)
					.parameter(double.class)
					.parameter(byte.class)
					.parameter(char.class)
					.parameter(short.class)
					.parameter(boolean.class)
			)
			.sample()
			.getString();

		then(actual).isEqualTo("first");
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateNoArgsConstructor() {
		String actual = SUT.giveMeBuilder(ConstructorTestSpecs.JavaTypeObject.class)
			.instantiate(
				ConstructorTestSpecs.JavaTypeObject.class,
				constructor()
			)
			.sample()
			.getString();

		then(actual).isEqualTo("second");
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateParameterNameHint() {
		String actual = SUT.giveMeBuilder(ConstructorTestSpecs.JavaTypeObject.class)
			.instantiate(
				ConstructorTestSpecs.JavaTypeObject.class,
				constructor()
					.parameter(String.class, "str")
			)
			.set("str", "third")
			.sample()
			.getString();

		then(actual).isEqualTo("third");
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateConstructorContainer() {
		List<ConstructorTestSpecs.JavaTypeObject> actual = SUT.giveMeBuilder(SimpleContainerObject.class)
			.instantiate(
				SimpleContainerObject.class,
				constructor()
					.parameter(new TypeReference<List<ConstructorTestSpecs.JavaTypeObject>>() {
					}, "list")
			)
			.instantiate(
				ConstructorTestSpecs.JavaTypeObject.class,
				constructor()
					.parameter(int.class)
					.parameter(float.class)
					.parameter(long.class)
					.parameter(double.class)
					.parameter(byte.class)
					.parameter(char.class)
					.parameter(short.class)
					.parameter(boolean.class)
			)
			.size("list", 1)
			.sample()
			.getList();

		then(actual).hasSize(1);
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateConstructorGenericContainer() {
		String actual = SUT.giveMeBuilder(ConstructorTestSpecs.ContainerObject.class)
			.instantiate(
				ConstructorTestSpecs.ContainerObject.class,
				constructor()
					.parameter(new TypeReference<List<String>>() {
					})
					.parameter(new TypeReference<List<ConstructorTestSpecs.JavaTypeObject>>() {
					})
					.parameter(new TypeReference<Set<String>>() {
					})
					.parameter(new TypeReference<Set<ConstructorTestSpecs.JavaTypeObject>>() {
					})
					.parameter(new TypeReference<Map<String, Integer>>() {
					})
					.parameter(new TypeReference<Map<String, ConstructorTestSpecs.JavaTypeObject>>() {
					})
					.parameter(new TypeReference<Entry<String, Integer>>() {
					})
					.parameter(new TypeReference<Entry<String, ConstructorTestSpecs.JavaTypeObject>>() {
					})
					.parameter(new TypeReference<Optional<String>>() {
					})
					.parameter(new TypeReference<OptionalInt>() {
					})
					.parameter(new TypeReference<OptionalLong>() {
					})
					.parameter(new TypeReference<OptionalDouble>() {
					})
			)
			.instantiate(
				ConstructorTestSpecs.JavaTypeObject.class,
				constructor()
					.parameter(int.class)
					.parameter(float.class)
					.parameter(long.class)
					.parameter(double.class)
					.parameter(byte.class)
					.parameter(char.class)
					.parameter(short.class)
					.parameter(boolean.class)
			)
			.sample()
			.getArray()[0];

		then(actual).isEqualTo("test");
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateGenericObjectByConstructor() {
		ConstructorTestSpecs.GenericObject<String> actual = SUT.giveMeBuilder(
				new TypeReference<ConstructorTestSpecs.GenericObject<String>>() {
				})
			.instantiate(
				new TypeReference<ConstructorTestSpecs.GenericObject<String>>() {
				},
				constructor()
					.parameter(String.class)
			)
			.sample();

		then(actual).isNotNull();
		then(actual.getValue()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateTwoGenericObjectByConstructor() {
		ConstructorTestSpecs.TwoGenericObject<String, Integer> actual = SUT.giveMeBuilder(
				new TypeReference<ConstructorTestSpecs.TwoGenericObject<String, Integer>>() {
				})
			.instantiate(
				new TypeReference<ConstructorTestSpecs.TwoGenericObject<String, Integer>>() {
				},
				constructor()
					.parameter(String.class)
					.parameter(Integer.class)
			)
			.sample();

		then(actual).isNotNull();
		then(actual.getTValue()).isNotNull();
		then(actual.getUValue()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateGenericObjectWithHintByConstructor() {
		ConstructorTestSpecs.GenericObject<String> actual = SUT.giveMeBuilder(
				new TypeReference<ConstructorTestSpecs.GenericObject<String>>() {
				})
			.instantiate(
				new TypeReference<ConstructorTestSpecs.GenericObject<String>>() {
				},
				constructor()
					.parameter(String.class)
			)
			.sample();

		then(actual).isNotNull();
		then(actual.getValue()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateByFactoryMethod() {
		String actual = SUT.giveMeBuilder(ConstructorTestSpecs.JavaTypeObject.class)
			.instantiate(
				factoryMethod("from")
			)
			.sample()
			.getString();

		then(actual).isEqualTo("factory");
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateByFactoryMethodWithParameter() {
		String actual = SUT.giveMeBuilder(ConstructorTestSpecs.JavaTypeObject.class)
			.instantiate(
				factoryMethod("from")
					.parameter(String.class)
			)
			.sample()
			.getString();

		then(actual).isEqualTo("factory");
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateFactoryMethodAndField() {
		Integer actual = SUT.giveMeBuilder(ConstructorTestSpecs.JavaTypeObject.class)
			.instantiate(
				factoryMethod("from")
					.parameter(String.class)
					.field()
			)
			.sample()
			.getWrapperInteger();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateConstructorField() {
		String actual = SUT.giveMeBuilder(MutableJavaTestSpecs.JavaTypeObject.class)
			.instantiate(constructor().field())
			.sample()
			.getString();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateConstructorJavaBeansProperty() {
		String actual = SUT.giveMeBuilder(MutableJavaTestSpecs.JavaTypeObject.class)
			.instantiate(constructor().javaBeansProperty())
			.sample()
			.getString();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateConstructorFieldFilter() {
		MutableJavaTestSpecs.JavaTypeObject actual =
			SUT.giveMeBuilder(MutableJavaTestSpecs.JavaTypeObject.class)
				.instantiate(
					constructor()
						.field(it -> it.filter(field -> !Modifier.isPrivate(field.getModifiers())))
				)
				.sample();

		then(actual.getString()).isNull();
		then(actual.getWrapperBoolean()).isNull();
	}

	@RepeatedTest(TEST_COUNT)
	void instantiateConstructorJavaBeansPropertyFilter() {
		MutableJavaTestSpecs.JavaTypeObject actual =
			SUT.giveMeBuilder(MutableJavaTestSpecs.JavaTypeObject.class)
				.instantiate(
					constructor()
						.javaBeansProperty(it -> it.filter(property -> !"string".equals(property.getName())))
				)
				.sample();

		then(actual.getString()).isNull();
		then(actual.getWrapperBoolean()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void nestedObject() {
		Inner actual = SUT.giveMeOne(Inner.class);

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void setJavaGetter() {
		String actual = SUT.giveMeBuilder(JavaTypeObject.class)
			.set(javaGetter(JavaTypeObject::getString), "test")
			.sample()
			.getString();

		then(actual).isEqualTo("test");
	}

	@RepeatedTest(TEST_COUNT)
	void setJavaGetterInto() {
		String actual = SUT.giveMeBuilder(RootJavaTypeObject.class)
			.set(javaGetter(RootJavaTypeObject::getValue).into(JavaTypeObject::getString), "test")
			.sample()
			.getValue()
			.getString();

		then(actual).isEqualTo("test");
	}

	@RepeatedTest(TEST_COUNT)
	void setJavaGetterCollection() {
		String actual = SUT.giveMeBuilder(ContainerObject.class)
			.size("list", 1)
			.set(javaGetter(ContainerObject::getList).index(String.class, 0), "test")
			.sample()
			.getList()
			.get(0);

		then(actual).isEqualTo("test");
	}

	@RepeatedTest(TEST_COUNT)
	void setJavaGetterCollectionElement() {
		String actual = SUT.giveMeBuilder(ContainerObject.class)
			.size("complexList", 1)
			.set(
				javaGetter(ContainerObject::getComplexList)
					.index(JavaTypeObject.class, 0)
					.into(JavaTypeObject::getString), "test"
			)
			.sample()
			.getComplexList()
			.get(0)
			.getString();

		then(actual).isEqualTo("test");
	}

	@RepeatedTest(TEST_COUNT)
	void setJavaGetterCollectionAllElement() {
		String expected = "test";

		List<String> actual = SUT.giveMeBuilder(ContainerObject.class)
			.size("complexList", 3)
			.set(
				javaGetter(ContainerObject::getComplexList)
					.allIndex(JavaTypeObject.class)
					.into(JavaTypeObject::getString), expected
			)
			.sample()
			.getComplexList()
			.stream()
			.map(JavaTypeObject::getString)
			.collect(Collectors.toList());

		then(actual).allMatch(expected::equals);
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

	@Test
	void constant() {
		thenNoException().isThrownBy(
			() -> SUT.giveMeOne(ConstantObject.class)
		);
	}

	@Test
	void collectionNotThrows() {
		thenNoException().isThrownBy(
			() -> SUT.giveMeOne(new TypeReference<Collection<String>>() {
			})
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

	@RepeatedTest(TEST_COUNT)
	void setExp() {
		String actual = SUT.giveMeJavaBuilder(JavaTypeObject.class)
			.setExpGetter(JavaTypeObject::getString, "test")
			.sample()
			.getString();

		then(actual).isEqualTo("test");
	}

	@RepeatedTest(TEST_COUNT)
	void setExpCollectionElement() {
		String actual = SUT.giveMeJavaBuilder(ContainerObject.class)
			.size("complexList", 1)
			.setExpGetter(
				javaGetter(ContainerObject::getComplexList)
					.index(JavaTypeObject.class, 0)
					.into(JavaTypeObject::getString),
				"test"
			)
			.sample()
			.getComplexList()
			.get(0)
			.getString();

		then(actual).isEqualTo("test");
	}

	@RepeatedTest(TEST_COUNT)
	void setListRecursiveImplementations() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.plugin(new InterfacePlugin()
				.interfaceImplements(
					InterfaceObject.class,
					Arrays.asList(
						InterfaceStringObject.class,
						InterfaceIntegerObject.class,
						InterfaceListObject.class
					)
				)
			)
			.build();

		List<InterfaceObject> element = sut.giveMeOne(new TypeReference<List<InterfaceStringObject>>() {
			}).stream()
			.map(InterfaceObject.class::cast)
			.collect(Collectors.toList());
		InterfaceListObject expected = new InterfaceListObject(element);

		// when
		InterfaceWrapperObject actual = sut.giveMeBuilder(InterfaceWrapperObject.class)
			.set("value", expected)
			.sample();

		then(actual.getValue()).isEqualTo(expected);
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
	void constructorValidator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(new JavaxValidationPlugin())
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.build();

		JavaxValidationObject actual = sut.giveMeOne(JavaxValidationObject.class);

		then(actual.getValue()).isEqualTo(100);
	}
}
