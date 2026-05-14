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

import static com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary.NOT_GENERATED;
import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.ALWAYS_NULL_INJECT;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.lang.reflect.AnnotatedType;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import org.junit.jupiter.api.Test;
import net.jqwik.api.arbitraries.StringArbitrary;
import net.jqwik.time.api.DateTimes;
import net.jqwik.time.api.arbitraries.InstantArbitrary;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.arbitrary.MonkeyStringArbitrary;
import com.navercorp.fixturemonkey.api.container.DecomposableJavaContainer;
import com.navercorp.fixturemonkey.api.exception.ValidationFailedException;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.CompositeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.IntrospectedArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.MatchArbitraryGenerator;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.jqwik.ArbitraryUtils;
import com.navercorp.fixturemonkey.api.jqwik.JavaArbitraryResolver;
import com.navercorp.fixturemonkey.api.jqwik.JavaTimeArbitraryResolver;
import com.navercorp.fixturemonkey.api.jqwik.JavaTimeTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.jqwik.JavaTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.jqwik.JqwikPlugin;
import com.navercorp.fixturemonkey.api.matcher.ExactTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.AbstractNoneConcreteIntValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.AbstractNoneConcreteStringValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.AbstractNoneValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.AbstractSamePropertyValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.AbstractValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.BuilderInteger;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.ChildBuilderGroup;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.ConcreteIntValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.ConcreteSamePropertyValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.ConcreteStringValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.CustomBuildMethodInteger;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.CustomBuilderMethodInteger;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.GenericGetFixedValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.GetFixedValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.GetFixedValueChild;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.GetIntegerFixedValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.GetIntegerFixedValueChild;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.GetStringFixedValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.GetterInterface;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.GetterInterfaceImplementation;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.GetterInterfaceImplementation2;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.NestedStringListWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.Pair;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.PairContainerPropertyGenerator;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.PairInterface;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.PairIntrospector;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.RegisterGroup;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.SelfRecursiveAbstractValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.SelfRecursiveImplementationValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.SimpleObjectChild;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.UniqueArbitraryGenerator;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ComplexObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.Interface;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.InterfaceImplHolder;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.InterfaceImplementation;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.NullableObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SimpleObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringListWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringWrapper;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.StringWrapperList;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.TwoEnum;

class FixtureMonkeyOptionsTest {
	@Test
	void strictModeSetWrongExpressionThrows() {
		FixtureMonkey sut = FixtureMonkey.builder().useExpressionStrictMode().build();

		thenThrownBy(
			() -> sut.giveMeBuilder(String.class)
				.set("nonExistentField", 0)
				.sample()
		).isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No matching results for given NodeResolvers.");
	}

	@Test
	void strictModeSizeWrongExpressionThrows() {
		FixtureMonkey sut = FixtureMonkey.builder().useExpressionStrictMode().build();

		thenThrownBy(
			() -> sut.giveMeBuilder(StringWrapperList.class)
				.size("nonExistentField", 1)
				.sample()
		).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No matching results for given container expression.");
	}

	@Test
	void strictModeSizeNestedWrongExpressionThrows() {
		FixtureMonkey sut = FixtureMonkey.builder().useExpressionStrictMode().build();

		thenThrownBy(
			() -> sut.giveMeBuilder(StringWrapperList.class)
				.size("values.nonExistentField", 1)
				.sample()
		).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No matching results for given container expression.");
	}

	@Test
	void strictModeSetWrongExpressionAfterPushAssignableTypePropertyNameResolverThrows() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.useExpressionStrictMode()
			.pushAssignableTypePropertyNameResolver(String.class, p -> "prop_" + p.getName())
			.build();

		thenThrownBy(
			() -> sut.giveMeBuilder(String.class)
				.set("prop_non_existent_str", "test")
				.sample()
		).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No matching results for given NodeResolvers.");
	}

	@Test
	void strictModeSizeWrongExpressionAfterPushAssignableTypePropertyNameResolverThrows() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.useExpressionStrictMode()
			.pushAssignableTypePropertyNameResolver(String.class, p -> "prop_" + p.getName())
			.build();

		thenThrownBy(
			() -> sut.giveMeBuilder(StringWrapperList.class)
				.size("prop_non_existent_container", 1)
				.sample()
		).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No matching results for given container expression.");
	}

	@Test
	void strictModeSetWrongExpressionAfterDefaultPropertyNameResolverThrows() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.useExpressionStrictMode()
			.defaultPropertyNameResolver(p -> "prop_" + p.getName())
			.build();

		thenThrownBy(
			() -> sut.giveMeBuilder(String.class)
				.set("prop_non_existent_str", 1)
				.sample()
		).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No matching results for given NodeResolvers.");
	}

	@Test
	void strictModeSizeWrongExpressionAfterDefaultPropertyNameResolverThrows() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.useExpressionStrictMode()
			.defaultPropertyNameResolver(p -> "prop_" + p.getName())
			.build();

		thenThrownBy(
			() -> sut.giveMeBuilder(StringWrapperList.class)
				.size("prop_non_existent_container", 1)
				.sample()
		).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No matching results for given container expression.");
	}

	@Test
	void notStrictModeSetWrongExpressionDoesNotThrows() {
		FixtureMonkey sut = FixtureMonkey.builder().build();

		thenNoException()
			.isThrownBy(() -> sut.giveMeBuilder(String.class)
				.set("nonExistentField", 0)
				.sample());
	}

	@Test
	void notStrictModeSizeWrongExpressionDoesNotThrows() {
		FixtureMonkey sut = FixtureMonkey.builder().build();

		thenNoException()
			.isThrownBy(() -> sut.giveMeBuilder(StringWrapperList.class)
				.size("values.nonExistentField", 1)
				.sample());
	}

	@Test
	void strictModeMultiOperationValidExpression() {
		FixtureMonkey sut = FixtureMonkey.builder().useExpressionStrictMode().build();

		String actual = sut.giveMeBuilder(new TypeReference<List<List<SimpleObject>>>() {
			})
			.size("$", 1)
			.size("$[0]", 1)
			.set("$[0][0].str", "expected")
			.sample()
			.get(0)
			.get(0)
			.getStr();

		then(actual).isEqualTo("expected");
	}

	@Test
	void pushAssignableTypeNullInjectGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushAssignableTypeNullInjectGenerator(
				SimpleObject.class,
				(context) -> 1.0d
			)
			.build();

		SimpleObjectChild actual = sut.giveMeOne(SimpleObjectChild.class);

		then(actual).isNull();
	}

	@Test
	void pushExactTypeNullInjectGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushExactTypeNullInjectGenerator(
				SimpleObject.class,
				(context) -> 1.0d
			)
			.build();

		SimpleObject actual = sut.giveMeOne(SimpleObject.class);

		then(actual).isNull();
	}

	@Test
	void pushNullInjectGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushNullInjectGenerator(
				MatcherOperator.exactTypeMatchOperator(SimpleObject.class, (context) -> 1.0d)
			)
			.build();

		SimpleObject actual = sut.giveMeOne(SimpleObject.class);

		then(actual).isNull();
	}

	@Test
	void defaultNullInjectGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNullInjectGenerator((context) -> 1.0d)
			.build();

		SimpleObject actual = sut.giveMeOne(SimpleObject.class);

		then(actual).isNull();
	}

	@Test
	void pushExactTypePropertyNameResolver() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushExactTypePropertyNameResolver(String.class, (property) -> "string")
			.build();
		String expected = "test";

		String actual = sut.giveMeBuilder(SimpleObject.class)
			.set("string", expected)
			.sample()
			.getStr();

		then(actual).isEqualTo(expected);
	}

	@Test
	void pushAssignableTypePropertyNameResolver() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushAssignableTypePropertyNameResolver(Interface.class, (property) -> "interface")
			.build();
		InterfaceImplementation expected = new InterfaceImplementation();
		expected.setValue("test");

		InterfaceImplementation actual = sut.giveMeBuilder(InterfaceImplHolder.class)
			.set("interface", expected)
			.sample()
			.getValue();

		then(actual).isEqualTo(expected);
	}

	@Test
	void pushPropertyNameResolver() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushPropertyNameResolver(MatcherOperator.exactTypeMatchOperator(String.class, (property) -> "string"))
			.build();
		String expected = "test";

		String actual = sut.giveMeBuilder(SimpleObject.class)
			.set("string", expected)
			.sample()
			.getStr();

		then(actual).isEqualTo(expected);
	}

	@Test
	void defaultPropertyNameResolver() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultPropertyNameResolver((property) -> "'" + property.getName() + "'")
			.build();
		String expected = "test";

		String actual = sut.giveMeBuilder(SimpleObject.class)
			.set("'str'", expected)
			.sample()
			.getStr();

		then(actual).isEqualTo(expected);
	}

	@Test
	void pushAssignableTypeArbitraryIntrospector() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushAssignableTypeArbitraryIntrospector(
				SimpleObject.class,
				(context) -> new ArbitraryIntrospectorResult(
					ArbitraryUtils.toCombinableArbitrary(Arbitraries.just(null))
				)
			)
			.build();

		SimpleObjectChild actual = sut.giveMeOne(SimpleObjectChild.class);

		then(actual).isNull();
	}

	@Test
	void pushExactTypeArbitraryIntrospector() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushExactTypeArbitraryIntrospector(
				SimpleObjectChild.class,
				(context) -> new ArbitraryIntrospectorResult(
					ArbitraryUtils.toCombinableArbitrary(Arbitraries.just(null))
				)
			)
			.build();

		SimpleObjectChild actual = sut.giveMeOne(SimpleObjectChild.class);

		then(actual).isNull();
	}

	@Test
	void pushArbitraryIntrospector() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushArbitraryIntrospector(
				MatcherOperator.exactTypeMatchOperator(
					SimpleObjectChild.class,
					(context) -> new ArbitraryIntrospectorResult(
						ArbitraryUtils.toCombinableArbitrary(Arbitraries.just(null))
					)
				)
			)
			.build();

		SimpleObjectChild actual = sut.giveMeOne(SimpleObjectChild.class);

		then(actual).isNull();
	}

	@Test
	void objectIntrospector() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(
				(context) -> new ArbitraryIntrospectorResult(
					ArbitraryUtils.toCombinableArbitrary(Arbitraries.just(null))
				)
			)
			.build();

		SimpleObject simpleObject = sut.giveMeOne(SimpleObject.class);
		ComplexObject complexObject = sut.giveMeOne(ComplexObject.class);

		then(simpleObject).isNull();
		then(complexObject).isNull();
	}

	@Test
	void pushArbitraryContainerInfoGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushArbitraryContainerInfoGenerator(
				new MatcherOperator<>(
					(property) -> {
						if (property.getJvmType().getRawType().isArray()
							|| property.getJvmType().getTypeVariables().isEmpty()) {
							return false;
						}

						com.navercorp.objectfarm.api.type.JvmType elementType = property.getJvmType().getTypeVariables().get(0);
						Class<?> type = elementType.getRawType();
						return type.isAssignableFrom(String.class);
					},
					(context) -> new ArbitraryContainerInfo(5, 5)
				)
			)
			.build();

		List<String> actual = sut.giveMeOne(ComplexObject.class)
			.getStrList();

		then(actual).hasSize(5);
	}

	@Test
	void pushArbitraryContainerInfoGeneratorNotMatching() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushArbitraryContainerInfoGenerator(
				new MatcherOperator<>(
					(property) -> {
						if (property.getJvmType().getRawType().isArray()
							|| property.getJvmType().getTypeVariables().isEmpty()) {
							return false;
						}

						com.navercorp.objectfarm.api.type.JvmType elementType = property.getJvmType().getTypeVariables().get(0);
						Class<?> type = elementType.getRawType();
						return type.isAssignableFrom(String.class);
					},
					(context) -> new ArbitraryContainerInfo(5, 5)
				)
			)
			.build();

		List<SimpleObject> actual = sut.giveMeOne(ComplexObject.class)
			.getList();

		then(actual).hasSizeBetween(0, 3);
	}

	@Test
	void defaultArbitraryContainerMaxSize() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(0, 1))
			.build();

		List<SimpleObject> actual = sut.giveMeOne(ComplexObject.class)
			.getList();

		then(actual).hasSizeLessThanOrEqualTo(1);
	}

	@Test
	void defaultArbitraryContainerInfo() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(3, 3))
			.build();

		List<SimpleObject> actual = sut.giveMeOne(ComplexObject.class)
			.getList();

		then(actual).hasSize(3);
	}

	@Test
	void javaTypeArbitraryGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new JqwikPlugin()
					.javaTypeArbitraryGenerator(new JavaTypeArbitraryGenerator() {
						@Override
						public StringArbitrary strings() {
							return Arbitraries.strings().numeric();
						}
					})
			)
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).matches(Pattern.compile("\\d*"));
	}

	@Test
	void javaTypeArbitraryGeneratorAffectsField() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new JqwikPlugin()
					.javaTypeArbitraryGenerator(new JavaTypeArbitraryGenerator() {
						@Override
						public StringArbitrary strings() {
							return Arbitraries.strings().numeric();
						}
					})
			)
			.build();

		String actual = sut.giveMeOne(SimpleObject.class)
			.getStr();

		then(actual).matches(it -> it == null || Pattern.compile("\\d*").matcher(it).matches());
	}

	@Test
	void javaArbitraryResolver() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new JqwikPlugin()
					.javaArbitraryResolver(new JavaArbitraryResolver() {
						@Override
						public Arbitrary<String> strings(StringArbitrary stringArbitrary,
							ArbitraryGeneratorContext context) {
							return Arbitraries.just("test");
						}
					})
			)
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isEqualTo("test");
	}

	@Test
	void javaArbitraryResolverAffectsField() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new JqwikPlugin()
					.javaArbitraryResolver(new JavaArbitraryResolver() {
						@Override
						public Arbitrary<String> strings(StringArbitrary stringArbitrary,
							ArbitraryGeneratorContext context) {
							return Arbitraries.just("test");
						}
					})
			)
			.build();

		String actual = sut.giveMeOne(SimpleObject.class)
			.getStr();

		then(actual).isIn("test", null);
	}

	@Test
	void javaTimeTypeArbitraryGenerator() {
		Instant expected = Instant.now();
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new JqwikPlugin()
					.javaTimeTypeArbitraryGenerator(new JavaTimeTypeArbitraryGenerator() {
						@Override
						public InstantArbitrary instants() {
							return DateTimes.instants().between(expected, expected);
						}
					})
			)
			.build();

		Instant actual = sut.giveMeOne(Instant.class);

		then(actual).isEqualTo(expected);
	}

	@Test
	void javaTimeTypeArbitraryGeneratorAffectsField() {
		Instant expected = Instant.now();
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new JqwikPlugin()
					.javaTimeTypeArbitraryGenerator(new JavaTimeTypeArbitraryGenerator() {
						@Override
						public InstantArbitrary instants() {
							return DateTimes.instants().between(expected, expected);
						}
					})
			)
			.build();

		Instant actual = sut.giveMeOne(SimpleObject.class)
			.getInstant();

		then(actual).isIn(null, expected);
	}

	@Test
	void javaTimeArbitraryResolver() {
		Instant expected = Instant.now();
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new JqwikPlugin()
					.javaTimeArbitraryResolver(new JavaTimeArbitraryResolver() {
						@Override
						public Arbitrary<Instant> instants(
							InstantArbitrary instantArbitrary,
							ArbitraryGeneratorContext context
						) {
							return Arbitraries.just(expected);
						}
					})
			)
			.build();

		Instant actual = sut.giveMeOne(Instant.class);

		then(actual).isEqualTo(expected);
	}

	@Test
	void javaTimeArbitraryResolverAffectsField() {
		Instant expected = Instant.now();
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new JqwikPlugin()
					.javaTimeArbitraryResolver(new JavaTimeArbitraryResolver() {
						@Override
						public Arbitrary<Instant> instants(
							InstantArbitrary instantArbitrary,
							ArbitraryGeneratorContext context
						) {
							return Arbitraries.just(expected);
						}
					})
			)
			.build();

		Instant actual = sut.giveMeOne(SimpleObject.class)
			.getInstant();

		then(actual).isIn(expected, null);
	}

	@Test
	void alterArbitraryValidator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.arbitraryValidator(obj -> {
				throw new ValidationFailedException("thrown by test ArbitraryValidator", new HashSet<>());
			})
			.build();

		thenThrownBy(() -> sut.giveMeOne(String.class))
			.isExactlyInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void defaultNotNull() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.build();

		String actual = sut.giveMeOne(SimpleObject.class)
			.getStr();

		then(actual).isNotNull();
	}

	@Test
	void defaultNotNullNotWorksWhenSetDefaultNullInjectGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNullInjectGenerator(
				new DefaultNullInjectGenerator(
					ALWAYS_NULL_INJECT,
					false,
					false,
					false,
					Collections.emptySet(),
					Collections.emptySet()
				)
			)
			.defaultNotNull(true)
			.build();

		String actual = sut.giveMeOne(SimpleObject.class)
			.getStr();

		then(actual).isNull();
	}

	@Test
	void pushExceptGenerateType() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushExceptGenerateType(new ExactTypeMatcher(String.class))
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isNull();
	}

	@Test
	void addExceptGenerateClass() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.addExceptGenerateClass(Timestamp.class)
			.build();

		Timestamp actual = sut.giveMeOne(Timestamp.class);

		then(actual).isNull();
	}

	@Test
	void addExceptGenerateClassNotGenerateField() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.addExceptGenerateClass(String.class)
			.build();

		String actual = sut.giveMeOne(SimpleObject.class)
			.getStr();

		then(actual).isNull();
	}

	@Test
	void addExceptGeneratePackage() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.addExceptGeneratePackage("java.lang")
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isNull();
	}

	@Test
	void addExceptGeneratePackageNotGenerateField() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.addExceptGeneratePackage("java.lang")
			.build();

		String actual = sut.giveMeOne(SimpleObject.class)
			.getStr();

		then(actual).isNull();
	}

	@Test
	void registerInstance() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(String.class, monkey -> monkey.giveMeBuilder("test"))
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isEqualTo("test");
	}

	@Test
	void registerField() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(String.class, monkey -> monkey.giveMeBuilder("test"))
			.build();

		String actual = sut.giveMeOne(SimpleObject.class)
			.getStr();

		then(actual).isEqualTo("test");
	}

	@Test
	void registerGroup() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(RegisterGroup.class)
			.build();

		String actual = sut.giveMeOne(SimpleObject.class)
			.getStr();
		List<String> actual2 = sut.giveMeOne(new TypeReference<List<String>>() {
		});
		ConcreteIntValue actual3 = sut.giveMeOne(ConcreteIntValue.class);

		then(actual).hasSizeBetween(1, 3);
		then(actual2).hasSizeLessThan(5);
		then(actual3.getIntValue()).isEqualTo(RegisterGroup.FIXED_INT_VALUE.getIntValue());
	}

	@Test
	void registerBuilderGroup() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.registerGroup(new ChildBuilderGroup())
			.build();

		String actual = sut.giveMeOne(SimpleObject.class)
			.getStr();
		List<String> actual2 = sut.giveMeOne(new TypeReference<List<String>>() {
		});
		ConcreteIntValue actual3 = sut.giveMeOne(ConcreteIntValue.class);

		then(actual).hasSizeBetween(1, 3);
		then(actual2).hasSizeLessThan(5);
		then(actual3.getIntValue()).isEqualTo(ChildBuilderGroup.FIXED_INT_VALUE.getIntValue());
	}

	@Test
	void registerSetFirst() {
		String expected = "test2";
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(String.class, monkey -> monkey.giveMeBuilder("test"))
			.build();

		String actual = sut.giveMeBuilder(String.class)
			.set("$", expected)
			.sample();

		then(actual).isEqualTo(expected);
	}

	@Test
	void registerWithPriority() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(String.class, monkey -> monkey.giveMeBuilder("test2"), 2)
			.register(String.class, monkey -> monkey.giveMeBuilder("test"), 1)
			.build();

		String actual = sut.giveMeBuilder(String.class)
			.sample();

		then(actual).isEqualTo("test");
	}


	@Test
	void nullableElement() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNullInjectGenerator(
				new DefaultNullInjectGenerator(
					1.0d,
					false,
					false,
					true,
					Collections.emptySet(),
					Collections.emptySet()
				)
			)
			.build();

		List<String> actual = sut.giveMeOne(new TypeReference<List<String>>() {
		});

		then(actual).allMatch(Objects::isNull);
	}

	@Test
	void generateNewContainer() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushAssignableTypeContainerPropertyGenerator(Pair.class, new PairContainerPropertyGenerator())
			.pushContainerIntrospector(new PairIntrospector())
			.build();

		Pair<String, String> pair = sut.giveMeBuilder(new TypeReference<Pair<String, String>>() {
			})
			.sample();

		then(pair).isNotNull();
	}

	@Test
	void decomposeNewContainer() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushAssignableTypeContainerPropertyGenerator(Pair.class, new PairContainerPropertyGenerator())
			.pushContainerIntrospector(new PairIntrospector())
			.defaultDecomposedContainerValueFactory(
				(obj) -> {
					if (obj instanceof Pair) {
						Pair<?, ?> pair = (Pair<?, ?>)obj;
						List<Object> list = new ArrayList<>();
						list.add(pair.getFirst());
						list.add(pair.getSecond());
						return new DecomposableJavaContainer(list, 2);
					}
					throw new IllegalArgumentException(
						"given type is not supported container : " + obj.getClass().getTypeName()
					);
				}
			)
			.build();
		ArbitraryBuilder<Pair<String, String>> builder = sut.giveMeBuilder(new TypeReference<Pair<String, String>>() {
			})
			.fixed();

		Pair<String, String> actual1 = builder.sample();
		Pair<String, String> actual2 = builder.sample();

		then(actual1).isEqualTo(actual2);
	}

	@Test
	void decomposeNewContainerByAddContainerType() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.addContainerType(
				Pair.class,
				new PairContainerPropertyGenerator(),
				new PairIntrospector(),
				(obj) -> {
					Pair<?, ?> pair = (Pair<?, ?>)obj;
					List<Object> list = new ArrayList<>();
					list.add(pair.getFirst());
					list.add(pair.getSecond());
					return new DecomposableJavaContainer(list, 2);
				}
			)
			.build();
		ArbitraryBuilder<Pair<String, String>> builder = sut.giveMeBuilder(new TypeReference<Pair<String, String>>() {
			})
			.fixed();

		Pair<String, String> actual1 = builder.sample();
		Pair<String, String> actual2 = builder.sample();

		then(actual1).isEqualTo(actual2);
	}

	@Test
	void decomposeNewContainerByAddContainerTypeInterface() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.addContainerType(
				PairInterface.class,
				new PairContainerPropertyGenerator(),
				new PairIntrospector(),
				(obj) -> {
					Pair<?, ?> pair = (Pair<?, ?>)obj;
					List<Object> list = new ArrayList<>();
					list.add(pair.getFirst());
					list.add(pair.getSecond());
					return new DecomposableJavaContainer(list, 2);
				}
			)
			.build();

		thenNoException()
			.isThrownBy(() -> sut.giveMeBuilder(new TypeReference<Pair<String, String>>() {
				})
				.set(sut.giveMeOne(new TypeReference<Pair<String, String>>() {
				})));
	}

	@Test
	void plugin() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin((optionsBuilder) -> optionsBuilder.insertFirstNullInjectGenerators(String.class, (context) -> 1.0d))
			.build();

		String actual = sut.giveMeBuilder(SimpleObject.class)
			.sample()
			.getStr();

		then(actual).isEqualTo(null);
	}

	@Test
	void registerMultipleTimesWithHierarchyReturnsCorrectOrder() {
		String expected = "test";
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(String.class, fixture -> fixture.giveMeBuilder(String.class).set(expected))
			.register(SimpleObject.class, fixture -> fixture.giveMeBuilder(SimpleObject.class).set("integer", 1))
			.build();

		String actual = sut.giveMeBuilder(SimpleObject.class)
			.setNotNull("str")
			.sample()
			.getStr();

		then(actual).isEqualTo(expected);
	}

	@Test
	void generateWithBuilderArbitraryIntrospector() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushExactTypeArbitraryIntrospector(BuilderInteger.class, BuilderArbitraryIntrospector.INSTANCE)
			.build();

		// when
		BuilderInteger actual = sut.giveMeOne(BuilderInteger.class);

		then(actual.getValue()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Test
	void generateWithBuilderArbitraryIntrospectorDefaultBuilderMethod() {
		// given
		BuilderArbitraryIntrospector builderArbitraryIntrospector = new BuilderArbitraryIntrospector();
		builderArbitraryIntrospector.setDefaultBuilderMethodName("customBuilder");

		FixtureMonkey sut = FixtureMonkey.builder()
			.pushExactTypeArbitraryIntrospector(CustomBuilderMethodInteger.class, builderArbitraryIntrospector)
			.build();

		// when
		CustomBuilderMethodInteger actual = sut.giveMeOne(CustomBuilderMethodInteger.class);

		then(actual.getValue()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Test
	void generateWithBuilderArbitraryIntrospectorDefaultBuildMethod() {
		// given
		BuilderArbitraryIntrospector builderArbitraryIntrospector = new BuilderArbitraryIntrospector();
		builderArbitraryIntrospector.setDefaultBuildMethodName("customBuild");

		FixtureMonkey sut = FixtureMonkey.builder()
			.pushExactTypeArbitraryIntrospector(CustomBuildMethodInteger.class, builderArbitraryIntrospector)
			.build();

		// when
		CustomBuildMethodInteger actual = sut.giveMeOne(CustomBuildMethodInteger.class);

		then(actual.getValue()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Test
	void registerRootAndChildElementGeneratingRoot() {
		// given
		String expected = "test";
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(ComplexObject.class, fixture -> fixture.giveMeBuilder(ComplexObject.class))
			.register(String.class, fixture -> fixture.giveMeBuilder(String.class).set(expected))
			.build();

		// when
		List<String> actual = sut.giveMeBuilder(ComplexObject.class)
			.size("map", 1)
			.sample()
			.getList().stream()
			.map(SimpleObject::getStr)
			.collect(Collectors.toList());

		then(actual).allMatch(expected::equals);
	}

	@Test
	void registerSize() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(ComplexObject.class, fixture -> fixture.giveMeBuilder(ComplexObject.class).size("strList", 1))
			.build();

		List<String> actual = sut.giveMeOne(ComplexObject.class)
			.getStrList();

		then(actual).hasSize(1);
	}

	@Test
	void registerFieldSet() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(StringWrapper.class, fixture -> fixture.giveMeBuilder(StringWrapper.class).set("value", "test"))
			.build();

		List<StringWrapper> actual = sut.giveMeOne(StringWrapperList.class)
			.getValues();

		then(actual).allMatch(it -> "test".equals(it.getValue()));
	}

	@Test
	void registerFieldSize() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(
				StringListWrapper.class,
				fixture -> fixture.giveMeBuilder(StringListWrapper.class).size("values", 1)
			)
			.build();

		List<StringListWrapper> actual = sut.giveMeOne(NestedStringListWrapper.class)
			.getValues();

		then(actual).allMatch(it -> it.getValues().size() == 1);
	}

	@Test
	void sizeRegisteredElement() {
		String expected = "test";
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(String.class, fixture -> fixture.giveMeBuilder(String.class).set(expected))
			.build();

		List<String> actual = sut.giveMeBuilder(StringListWrapper.class)
			.size("values", 5)
			.sample()
			.getValues();

		then(actual).allMatch(expected::equals);
	}

	@Test
	void sizeBiggerThanRegisterSized() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(ComplexObject.class, fixture -> fixture.giveMeBuilder(ComplexObject.class).size("strList", 3))
			.build();

		List<String> actual = sut.giveMeBuilder(ComplexObject.class)
			.size("strList", 10)
			.sample()
			.getStrList();

		then(actual).hasSize(10);
	}

	@Test
	void registerObjectNotFixed() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(String.class, it -> it.giveMeBuilder(String.class).set("$", Arbitraries.strings().ofLength(10)))
			.build();

		List<String> sampled = sut.giveMeBuilder(new TypeReference<List<String>>() {
			})
			.minSize("$", 3)
			.sample();

		Set<String> actual = new HashSet<>(sampled);
		then(actual).hasSizeGreaterThan(1);
	}

	@Test
	void registerParentSetNullChildAndChildRegistered() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(SimpleObject.class, fixture -> fixture.giveMeBuilder(SimpleObject.class).set("str", "test"))
			.register(ComplexObject.class, fixture -> fixture.giveMeBuilder(ComplexObject.class).setNull("object"))
			.build();

		SimpleObject actual = sut.giveMeBuilder(new TypeReference<List<ComplexObject>>() {
			})
			.size("$", 1)
			.sample()
			.get(0)
			.getObject();

		then(actual).isNull();
	}

	@Test
	void sampleNullableContainerWhenOptionNullableContainerIsSetReturnsNull() {
		DefaultNullInjectGenerator nullInjectGenerator = new DefaultNullInjectGenerator(
			ALWAYS_NULL_INJECT,
			true,
			false,
			false,
			Collections.emptySet(),
			Collections.emptySet()
		);
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNullInjectGenerator(nullInjectGenerator)
			.build();

		List<String> values = sut.giveMeOne(NullableObject.class)
			.getValues();

		then(values).isNull();
	}

	@Test
	void applySizeWhenRegisteredWithSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(
				StringListWrapper.class,
				fixture -> fixture.giveMeBuilder(StringListWrapper.class).size("values", 5)
			)
			.build();

		// when
		List<String> actual = sut.giveMeBuilder(StringListWrapper.class)
			.thenApply((it, builder) -> builder.size("values", 10))
			.sample()
			.getValues();

		then(actual).hasSize(10);
	}

	@Test
	void sizeWhenRegisterSizeInApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(
				StringListWrapper.class,
				fixture -> fixture.giveMeBuilder(StringListWrapper.class)
					.thenApply((it, builder) -> builder.size("values", 1))
			)
			.build();

		// when
		List<String> actual = sut.giveMeBuilder(StringListWrapper.class)
			.size("values", 2)
			.sample()
			.getValues();

		then(actual).hasSize(2);
	}

	@Test
	void sizeWhenRegisterApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(
				StringListWrapper.class,
				fixture -> fixture.giveMeBuilder(StringListWrapper.class)
					.size("values", 1)
					.thenApply((it, builder) -> {
					})
			)
			.build();

		// when
		List<String> actual = sut.giveMeBuilder(StringListWrapper.class)
			.size("values", 2)
			.sample()
			.getValues();

		then(actual).hasSize(2);
	}

	@Test
	void sampleEnumMapWithEnumSizeIsLessThanContainerInfoMaxSize() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(0, 5))
			.build();

		Map<TwoEnum, String> values = sut.giveMeOne(new TypeReference<Map<TwoEnum, String>>() {
		});

		then(values).hasSizeLessThanOrEqualTo(2);
	}

	@Test
	void sampleEnumMapWithEnumSizeIsLessThanContainerInfoMinSize() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(3, 5))
			.build();

		Map<TwoEnum, String> values = sut.giveMeOne(new TypeReference<Map<TwoEnum, String>>() {
		});

		then(values).hasSizeLessThanOrEqualTo(2);
	}

	@Test
	void sampleEnumSetWithEnumSizeIsLessThanContainerInfoMinSize() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(3, 5))
			.build();

		Set<TwoEnum> values = sut.giveMeOne(new TypeReference<Set<TwoEnum>>() {
		});

		then(values).hasSizeLessThanOrEqualTo(2);
	}

	@Test
	void interfaceImplements() {
		// given
		List<Class<? extends GetFixedValue>> implementations = new ArrayList<>();
		implementations.add(GetIntegerFixedValue.class);
		implementations.add(GetStringFixedValue.class);

		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new InterfacePlugin()
					.interfaceImplements(GetFixedValue.class, implementations)
			)
			.build();

		// when
		Object actual = sut.giveMeOne(GetFixedValue.class).get();

		then(actual).isIn(1, "fixed");
	}

	@Test
	void sampleGenericInterface() {
		// given
		List<Class<? extends GetFixedValue>> implementations = new ArrayList<>();
		implementations.add(GetIntegerFixedValue.class);
		implementations.add(GetStringFixedValue.class);

		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new InterfacePlugin()
					.interfaceImplements(GetFixedValue.class, implementations)
			)
			.build();

		// when
		Object actual = sut.giveMeBuilder(new TypeReference<GenericGetFixedValue<GetFixedValue>>() {
			})
			.setNotNull("value")
			.sample()
			.getValue()
			.get();

		then(actual).isIn(1, "fixed");
	}

	@Test
	void sampleGenericInterfaceReturnsDiff() {
		// given
		List<Class<? extends GetFixedValue>> implementations = new ArrayList<>();
		implementations.add(GetIntegerFixedValue.class);
		implementations.add(GetStringFixedValue.class);

		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new InterfacePlugin()
					.interfaceImplements(GetFixedValue.class, implementations)
			)
			.build();

		// when
		Set<Class<? extends GetFixedValue>> actual = sut.giveMeBuilder(
				new TypeReference<GenericGetFixedValue<GetFixedValue>>() {
				})
			.setNotNull("value")
			.sampleList(100)
			.stream()
			.map(it -> it.getValue().getClass())
			.collect(Collectors.toSet());

		then(actual).hasSize(2);
	}

	@Test
	void sampleInterfaceChildWhenOptionHasHierarchy() {
		// given
		List<Class<? extends GetFixedValue>> implementations = new ArrayList<>();
		implementations.add(GetIntegerFixedValue.class);
		implementations.add(GetStringFixedValue.class);

		List<Class<? extends GetFixedValueChild>> childImplementations = new ArrayList<>();
		childImplementations.add(GetIntegerFixedValueChild.class);

		// when
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new InterfacePlugin()
					.interfaceImplements(GetFixedValueChild.class, childImplementations)
					.interfaceImplements(GetFixedValue.class, implementations)
			)
			.build();

		Object actual = sut.giveMeOne(new TypeReference<GetFixedValueChild>() {
			})
			.get();

		then(actual).isEqualTo(2);
	}

	@Test
	void sampleConcreteWhenHasSameNameProperty() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new InterfacePlugin()
					.abstractClassExtends(
						AbstractSamePropertyValue.class,
						Collections.singletonList(ConcreteSamePropertyValue.class)
					)
			)
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.build();

		AbstractSamePropertyValue actual = sut.giveMeOne(AbstractSamePropertyValue.class);

		then(actual).isNotNull();
	}

	@Test
	void setConcreteListWithNoParentValue() {
		// given
		List<Class<? extends AbstractNoneValue>> implementations = new ArrayList<>();
		implementations.add(AbstractNoneConcreteStringValue.class);
		implementations.add(AbstractNoneConcreteIntValue.class);

		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new InterfacePlugin()
					.abstractClassExtends(AbstractNoneValue.class, implementations)
			)
			.build();

		AbstractNoneConcreteStringValue abstractNoneConcreteStringValue = new AbstractNoneConcreteStringValue();
		abstractNoneConcreteStringValue.setStringValue("test");
		AbstractNoneConcreteIntValue abstractNoneConcreteIntValue = new AbstractNoneConcreteIntValue();
		abstractNoneConcreteIntValue.setIntValue(-999);
		List<AbstractNoneValue> expected = new ArrayList<>();
		expected.add(abstractNoneConcreteStringValue);
		expected.add(abstractNoneConcreteIntValue);

		// when
		List<AbstractNoneValue> actual = sut.giveMeBuilder(new TypeReference<List<AbstractNoneValue>>() {
			})
			.set(expected)
			.sample();

		then(actual).isEqualTo(expected);
	}

	@Test
	void setConcreteClassWhenHasParentValue() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new InterfacePlugin()
					.abstractClassExtends(AbstractValue.class, Collections.singletonList(ConcreteStringValue.class))
			)
			.build();

		ConcreteStringValue expected = new ConcreteStringValue();
		expected.setValue("stringValue");

		// when
		AbstractValue actual = sut.giveMeBuilder(new TypeReference<AbstractValue>() {
			})
			.set(expected)
			.sample();

		then(actual).isEqualTo(expected);
	}

	@Test
	void setConcreteClassWhenHasNoParentValue() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new InterfacePlugin()
					.abstractClassExtends(
						AbstractNoneValue.class,
						Collections.singletonList(AbstractNoneConcreteStringValue.class)
					)
			)
			.build();

		AbstractNoneConcreteStringValue expected = new AbstractNoneConcreteStringValue();
		expected.setStringValue("stringValue");

		// when
		AbstractNoneValue actual = sut.giveMeBuilder(new TypeReference<AbstractNoneValue>() {
			})
			.set(expected)
			.sample();

		then(actual).isEqualTo(expected);
	}

	@Test
	void setConcreteList() {
		// given
		List<Class<? extends AbstractValue>> implementations = new ArrayList<>();
		implementations.add(ConcreteStringValue.class);
		implementations.add(ConcreteIntValue.class);

		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new InterfacePlugin()
					.abstractClassExtends(AbstractValue.class, implementations)
			)
			.build();

		ConcreteStringValue concreteStringWrapper = new ConcreteStringValue();
		concreteStringWrapper.setValue("stringValue");
		concreteStringWrapper.setStringValue("test");
		ConcreteIntValue concreteIntValue = new ConcreteIntValue();
		concreteIntValue.setValue("intValue");
		concreteIntValue.setIntValue(-999);
		List<AbstractValue> expected = new ArrayList<>();
		expected.add(concreteStringWrapper);
		expected.add(concreteIntValue);

		// when
		List<AbstractValue> actual = sut.giveMeBuilder(new TypeReference<List<AbstractValue>>() {
			})
			.set(expected)
			.sample();

		then(actual).isEqualTo(expected);
	}

	@Test
	void sampleSelfRecursiveAbstract() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new InterfacePlugin()
					.abstractClassExtends(
						SelfRecursiveAbstractValue.class,
						Collections.singletonList(SelfRecursiveImplementationValue.class)
					)
			)
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.build();

		SelfRecursiveAbstractValue actual = sut.giveMeOne(SelfRecursiveAbstractValue.class);

		then(actual).isNotNull();
	}

	@Test
	void sizeElementWhenRegisteredSize() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(
				NestedStringListWrapper.class,
				fixture -> fixture.giveMeBuilder(new TypeReference<NestedStringListWrapper>() {
					})
					.size("values", 5)
			)
			.build();

		List<StringListWrapper> actual = sut.giveMeBuilder(NestedStringListWrapper.class)
			.size("values[*].values", 3, 5)
			.sample()
			.getValues();

		then(actual).allMatch(it -> it.getValues().size() >= 3 && it.getValues().size() <= 5);
	}

	@Test
	void samePropertyDiffImplementations() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new InterfacePlugin()
					.interfaceImplements(
						GetterInterface.class,
						Arrays.asList(
							GetterInterfaceImplementation.class,
							GetterInterfaceImplementation2.class
						)
					)
			)
			.build();

		String actual = sut.giveMeBuilder(GetterInterface.class)
			.set("value", "expected")
			.sample()
			.getValue();

		then(actual).isEqualTo("expected");
	}

	@Test
	void sampleWithMonkeyStringArbitrary() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new JqwikPlugin()
					.javaTypeArbitraryGenerator(
						new JavaTypeArbitraryGenerator() {
							@Override
							public StringArbitrary strings() {
								return new MonkeyStringArbitrary();
							}
						}
					)
			)
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isNotNull();
	}

	@Test
	void filterWithMonkeyStringArbitrary() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new JqwikPlugin()
					.javaTypeArbitraryGenerator(new JavaTypeArbitraryGenerator() {
						@Override
						public StringArbitrary strings() {
							return new MonkeyStringArbitrary().filterCharacter(Character::isUpperCase);
						}
					})
			)
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isUpperCase();
	}

	@Test
	void filterIsoControlCharacterWithMonkeyStringArbitrary() {
		FixtureMonkey sut = FixtureMonkey.builder().build();

		String actual = sut.giveMeOne(String.class);

		then(actual).matches(value -> {
			for (char c : value.toCharArray()) {
				if (Character.isISOControl(c)) {
					return false;
				}
			}
			return true;
		});
	}

	@Test
	void multipleFiltersWithMonkeyStringArbitrary() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new JqwikPlugin()
					.javaTypeArbitraryGenerator(
						new JavaTypeArbitraryGenerator() {
							@Override
							public MonkeyStringArbitrary monkeyStrings() {
								return new MonkeyStringArbitrary().filterCharacter(c -> !Character.isISOControl(c))
									.filterCharacter(Character::isUpperCase);
							}
						}
					)
			)
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).matches(value -> {
			for (char c : value.toCharArray()) {
				if (Character.isISOControl(c) || !Character.isUpperCase(c)) {
					return false;
				}
			}
			return true;
		});
	}

	@Test
	void alterDefaultArbitraryGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultArbitraryGenerator(generator -> new MatchArbitraryGenerator(
				Arrays.asList(
					new IntrospectedArbitraryGenerator(context ->
						new ArbitraryIntrospectorResult(NOT_GENERATED)
					),
					generator
				)
			))
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isNull();
	}

	@Test
	void skipArbitraryGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultArbitraryGenerator(generator -> new CompositeArbitraryGenerator(
				Arrays.asList(
					context -> NOT_GENERATED,
					generator
				)
			))
			.defaultNotNull(true)
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isNotNull();
	}

	@Test
	void uniqueArbitraryGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultArbitraryGenerator(UniqueArbitraryGenerator::new)
			.build();

		List<String> actual = sut.giveMe(String.class, 100);

		Set<String> expected = new HashSet<>(actual);
		then(actual).hasSameSizeAs(expected);
	}

	@Test
	void allArbitraryGeneratorSkipReturnsNull() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultArbitraryGenerator(generator -> (
				new CompositeArbitraryGenerator(
					Arrays.asList(
						context -> NOT_GENERATED,
						context -> NOT_GENERATED
					)
				)
			))
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isNull();
	}

	@Test
	void setConcrete() {
		// given
		List<Class<? extends AbstractValue>> implementations = new ArrayList<>();
		implementations.add(ConcreteStringValue.class);
		implementations.add(ConcreteIntValue.class);

		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new InterfacePlugin()
					.abstractClassExtends(AbstractValue.class, implementations)
			)
			.build();

		ConcreteStringValue expected = new ConcreteStringValue();
		expected.setValue("stringValue");
		expected.setStringValue("test");

		// when
		AbstractValue actual = sut.giveMeBuilder(AbstractValue.class)
			.set(expected)
			.sample();

		then(actual).isEqualTo(expected);
	}
}
