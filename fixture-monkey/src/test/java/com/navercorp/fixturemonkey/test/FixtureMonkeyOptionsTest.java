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
import net.jqwik.api.Property;
import net.jqwik.api.arbitraries.StringArbitrary;
import net.jqwik.time.api.DateTimes;
import net.jqwik.time.api.arbitraries.InstantArbitrary;

import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.arbitrary.MonkeyStringArbitrary;
import com.navercorp.fixturemonkey.api.container.DecomposableJavaContainer;
import com.navercorp.fixturemonkey.api.exception.FilterMissException;
import com.navercorp.fixturemonkey.api.exception.ValidationFailedException;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.CompositeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.DefaultObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.IntrospectedArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.JavaArbitraryResolver;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeArbitraryResolver;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.introspector.JavaTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.matcher.ExactTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.test.ExpressionGeneratorTestSpecs.StringValue;
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
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.NestedListStringObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.Pair;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.PairContainerPropertyGenerator;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.PairIntrospector;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.RegisterGroup;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.SelfRecursiveAbstractValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.SelfRecursiveImplementationValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.SimpleObjectChild;
import com.navercorp.fixturemonkey.test.FixtureMonkeyOptionsAdditionalTestSpecs.UniqueArbitraryGenerator;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ComplexObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.Interface;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.InterfaceFieldImplementationValue;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.InterfaceImplementation;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.ListStringObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.NestedStringList;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.NullableObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.SimpleObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyTestSpecs.TwoEnum;

class FixtureMonkeyOptionsTest {
	@Property
	void strictModeSetWrongExpressionThrows() {
		FixtureMonkey sut = FixtureMonkey.builder().useExpressionStrictMode().build();

		thenThrownBy(
			() -> sut.giveMeBuilder(String.class)
				.set("nonExistentField", 0)
				.sample()
		).isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No matching results for given NodeResolvers.");
	}

	@Property
	void notStrictModeSetWrongExpressionDoesNotThrows() {
		FixtureMonkey sut = FixtureMonkey.builder().build();

		thenNoException()
			.isThrownBy(() -> sut.giveMeBuilder(String.class)
				.set("nonExistentField", 0)
				.sample());
	}

	@Property
	void alterDefaultArbitraryPropertyGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultObjectPropertyGenerator(
				(context) -> DefaultObjectPropertyGenerator.INSTANCE.generate(context)
					.withNullInject(1.0)
			)
			.build();

		ComplexObject actual = sut.giveMeOne(ComplexObject.class);

		then(actual).isNull();
	}

	@Property
	void pushAssignableTypeArbitraryPropertyGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushAssignableTypeObjectPropertyGenerator(
				SimpleObject.class,
				(context) -> DefaultObjectPropertyGenerator.INSTANCE.generate(context)
					.withNullInject(1.0)
			)
			.build();

		SimpleObjectChild actual = sut.giveMeOne(SimpleObjectChild.class);

		then(actual).isNull();
	}

	@Property
	void pushExactTypeArbitraryPropertyGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushExactTypeObjectPropertyGenerator(
				SimpleObjectChild.class,
				(context) -> DefaultObjectPropertyGenerator.INSTANCE.generate(context)
					.withNullInject(1.0)
			)
			.build();

		SimpleObjectChild actual = sut.giveMeOne(SimpleObjectChild.class);

		then(actual).isNull();
	}

	@Property
	void pushExactTypeArbitraryPropertyGeneratorNotAffectsAssignable() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushExactTypeObjectPropertyGenerator(
				SimpleObject.class,
				(context) -> DefaultObjectPropertyGenerator.INSTANCE.generate(context)
					.withNullInject(1.0)
			)
			.build();

		SimpleObjectChild actual = sut.giveMeOne(SimpleObjectChild.class);

		then(actual).isNotNull();
	}

	@Property
	void pushObjectPropertyGenerator() {
		ObjectPropertyGenerator arbitraryPropertyGenerator = (context) ->
			DefaultObjectPropertyGenerator.INSTANCE.generate(context)
				.withNullInject(1.0);
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushObjectPropertyGenerator(
				MatcherOperator.exactTypeMatchOperator(
					SimpleObject.class,
					arbitraryPropertyGenerator
				)
			)
			.build();

		SimpleObject actual = sut.giveMeOne(SimpleObject.class);

		then(actual).isNull();
	}

	@Property
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

	@Property
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

	@Property
	void pushNullInjectGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushNullInjectGenerator(
				MatcherOperator.exactTypeMatchOperator(SimpleObject.class, (context) -> 1.0d)
			)
			.build();

		SimpleObject actual = sut.giveMeOne(SimpleObject.class);

		then(actual).isNull();
	}

	@Property
	void defaultNullInjectGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNullInjectGenerator((context) -> 1.0d)
			.build();

		SimpleObject actual = sut.giveMeOne(SimpleObject.class);

		then(actual).isNull();
	}

	@Property
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

	@Property
	void pushAssignableTypePropertyNameResolver() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushAssignableTypePropertyNameResolver(Interface.class, (property) -> "interface")
			.build();
		InterfaceImplementation expected = new InterfaceImplementation();
		expected.setValue("test");

		InterfaceImplementation actual = sut.giveMeBuilder(InterfaceFieldImplementationValue.class)
			.set("interface", expected)
			.sample()
			.getValue();

		then(actual).isEqualTo(expected);
	}

	@Property
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

	@Property
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

	@Property
	void pushAssignableTypeArbitraryIntrospector() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushAssignableTypeArbitraryIntrospector(
				SimpleObject.class,
				(context) -> new ArbitraryIntrospectorResult(Arbitraries.just(null))
			)
			.build();

		SimpleObjectChild actual = sut.giveMeOne(SimpleObjectChild.class);

		then(actual).isNull();
	}

	@Property
	void pushExactTypeArbitraryIntrospector() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushExactTypeArbitraryIntrospector(
				SimpleObjectChild.class,
				(context) -> new ArbitraryIntrospectorResult(Arbitraries.just(null))
			)
			.build();

		SimpleObjectChild actual = sut.giveMeOne(SimpleObjectChild.class);

		then(actual).isNull();
	}

	@Property
	void pushArbitraryIntrospector() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushArbitraryIntrospector(
				MatcherOperator.exactTypeMatchOperator(
					SimpleObjectChild.class,
					(context) -> new ArbitraryIntrospectorResult(Arbitraries.just(null))
				)
			)
			.build();

		SimpleObjectChild actual = sut.giveMeOne(SimpleObjectChild.class);

		then(actual).isNull();
	}

	@Property
	void objectIntrospector() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(
				(context) -> new ArbitraryIntrospectorResult(Arbitraries.just(null))
			)
			.build();

		SimpleObject simpleObject = sut.giveMeOne(SimpleObject.class);
		ComplexObject complexObject = sut.giveMeOne(ComplexObject.class);

		then(simpleObject).isNull();
		then(complexObject).isNull();
	}

	@Property
	void pushArbitraryContainerInfoGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushArbitraryContainerInfoGenerator(
				new MatcherOperator<>(
					(property) -> {
						if (Types.getActualType(property.getType()).isArray()) {
							return false;
						}

						AnnotatedType elementType = Types.getGenericsTypes(property.getAnnotatedType()).get(0);
						Class<?> type = Types.getActualType(elementType);
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

	@Property
	void pushArbitraryContainerInfoGeneratorNotMatching() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushArbitraryContainerInfoGenerator(
				new MatcherOperator<>(
					(property) -> {
						if (Types.getActualType(property.getType()).isArray()) {
							return false;
						}

						AnnotatedType elementType = Types.getGenericsTypes(property.getAnnotatedType()).get(0);
						Class<?> type = Types.getActualType(elementType);
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

	@Property
	void defaultArbitraryContainerMaxSize() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(0, 1))
			.build();

		List<SimpleObject> actual = sut.giveMeOne(ComplexObject.class)
			.getList();

		then(actual).hasSizeLessThanOrEqualTo(1);
	}

	@Property
	void defaultArbitraryContainerInfo() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(3, 3))
			.build();

		List<SimpleObject> actual = sut.giveMeOne(ComplexObject.class)
			.getList();

		then(actual).hasSize(3);
	}

	@Property
	void javaTypeArbitraryGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.javaTypeArbitraryGenerator(new JavaTypeArbitraryGenerator() {
				@Override
				public StringArbitrary strings() {
					return Arbitraries.strings().numeric();
				}
			})
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).matches(Pattern.compile("\\d*"));
	}

	@Property
	void javaTypeArbitraryGeneratorAffectsField() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.javaTypeArbitraryGenerator(new JavaTypeArbitraryGenerator() {
				@Override
				public StringArbitrary strings() {
					return Arbitraries.strings().numeric();
				}
			})
			.build();

		String actual = sut.giveMeOne(SimpleObject.class)
			.getStr();

		then(actual).matches(it -> it == null || Pattern.compile("\\d*").matcher(it).matches());
	}

	@Property
	void javaArbitraryResolver() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.javaArbitraryResolver(new JavaArbitraryResolver() {
				@Override
				public Arbitrary<String> strings(StringArbitrary stringArbitrary, ArbitraryGeneratorContext context) {
					return Arbitraries.just("test");
				}
			})
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isEqualTo("test");
	}

	@Property
	void javaArbitraryResolverAffectsField() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.javaArbitraryResolver(new JavaArbitraryResolver() {
				@Override
				public Arbitrary<String> strings(StringArbitrary stringArbitrary, ArbitraryGeneratorContext context) {
					return Arbitraries.just("test");
				}
			})
			.build();

		String actual = sut.giveMeOne(SimpleObject.class)
			.getStr();

		then(actual).isIn("test", null);
	}

	@Property
	void javaTimeTypeArbitraryGenerator() {
		Instant expected = Instant.now();
		FixtureMonkey sut = FixtureMonkey.builder()
			.javaTimeTypeArbitraryGenerator(new JavaTimeTypeArbitraryGenerator() {
				@Override
				public InstantArbitrary instants() {
					return DateTimes.instants().between(expected, expected);
				}
			})
			.build();

		Instant actual = sut.giveMeOne(Instant.class);

		then(actual).isEqualTo(expected);
	}

	@Property
	void javaTimeTypeArbitraryGeneratorAffectsField() {
		Instant expected = Instant.now();
		FixtureMonkey sut = FixtureMonkey.builder()
			.javaTimeTypeArbitraryGenerator(new JavaTimeTypeArbitraryGenerator() {
				@Override
				public InstantArbitrary instants() {
					return DateTimes.instants().between(expected, expected);
				}
			})
			.build();

		Instant actual = sut.giveMeOne(SimpleObject.class)
			.getInstant();

		then(actual).isIn(null, expected);
	}

	@Property
	void javaTimeArbitraryResolver() {
		Instant expected = Instant.now();
		FixtureMonkey sut = FixtureMonkey.builder()
			.javaTimeArbitraryResolver(new JavaTimeArbitraryResolver() {
				@Override
				public Arbitrary<Instant> instants(
					InstantArbitrary instantArbitrary,
					ArbitraryGeneratorContext context
				) {
					return Arbitraries.just(expected);
				}
			})
			.build();

		Instant actual = sut.giveMeOne(Instant.class);

		then(actual).isEqualTo(expected);
	}

	@Property
	void javaTimeArbitraryResolverAffectsField() {
		Instant expected = Instant.now();
		FixtureMonkey sut = FixtureMonkey.builder()
			.javaTimeArbitraryResolver(new JavaTimeArbitraryResolver() {
				@Override
				public Arbitrary<Instant> instants(
					InstantArbitrary instantArbitrary,
					ArbitraryGeneratorContext context
				) {
					return Arbitraries.just(expected);
				}
			})
			.build();

		Instant actual = sut.giveMeOne(SimpleObject.class)
			.getInstant();

		then(actual).isIn(expected, null);
	}

	@Property(tries = 1)
	void alterArbitraryValidator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.arbitraryValidator(obj -> {
				throw new ValidationFailedException("thrown by test ArbitraryValidator", new HashSet<>());
			})
			.build();

		thenThrownBy(() -> sut.giveMeOne(String.class))
			.isExactlyInstanceOf(FilterMissException.class);
	}

	@Property
	void defaultNotNull() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNotNull(true)
			.build();

		String actual = sut.giveMeOne(SimpleObject.class)
			.getStr();

		then(actual).isNotNull();
	}

	@Property
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

	@Property
	void pushExceptGenerateType() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushExceptGenerateType(new ExactTypeMatcher(String.class))
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isNull();
	}

	@Property
	void addExceptGenerateClass() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.addExceptGenerateClass(String.class)
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isNull();
	}

	@Property
	void addExceptGenerateClassNotGenerateField() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.addExceptGenerateClass(String.class)
			.build();

		String actual = sut.giveMeOne(SimpleObject.class)
			.getStr();

		then(actual).isNull();
	}

	@Property
	void addExceptGeneratePackage() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.addExceptGeneratePackage("java.lang")
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isNull();
	}

	@Property
	void addExceptGeneratePackageNotGenerateField() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.addExceptGeneratePackage("java.lang")
			.build();

		String actual = sut.giveMeOne(SimpleObject.class)
			.getStr();

		then(actual).isNull();
	}

	@Property
	void registerInstance() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(String.class, monkey -> monkey.giveMeBuilder("test"))
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isEqualTo("test");
	}

	@Property
	void registerField() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(String.class, monkey -> monkey.giveMeBuilder("test"))
			.build();

		String actual = sut.giveMeOne(SimpleObject.class)
			.getStr();

		then(actual).isEqualTo("test");
	}

	@Property
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
		then(actual3).isEqualTo(RegisterGroup.FIXED_INT_VALUE);

	}

	@Property
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
		then(actual3).isEqualTo(ChildBuilderGroup.FIXED_INT_VALUE);
	}

	@Property
	void registerSameInstancesTwiceWorksLast() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(String.class, monkey -> monkey.giveMeBuilder("test"))
			.register(String.class, monkey -> monkey.giveMeBuilder("test2"))
			.build();

		String actual = sut.giveMeOne(SimpleObject.class)
			.getStr();

		then(actual).isEqualTo("test2");
	}

	@Property
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

	@Property
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

	@Property
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

	@Property
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

	@Property
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

	@Property
	void plugin() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin((optionsBuilder) -> optionsBuilder.insertFirstNullInjectGenerators(String.class, (context) -> 1.0d))
			.build();

		String actual = sut.giveMeBuilder(SimpleObject.class)
			.sample()
			.getStr();

		then(actual).isEqualTo(null);
	}

	@Property
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

	@Property
	void generateWithBuilderArbitraryIntrospector() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushExactTypeArbitraryIntrospector(BuilderInteger.class, BuilderArbitraryIntrospector.INSTANCE)
			.build();

		// when
		BuilderInteger actual = sut.giveMeOne(BuilderInteger.class);

		then(actual.getValue()).isBetween(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Property
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

	@Property
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

	@Property
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

	@Property
	void registerSize() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(ComplexObject.class, fixture -> fixture.giveMeBuilder(ComplexObject.class).size("strList", 1))
			.build();

		List<String> actual = sut.giveMeOne(ComplexObject.class)
			.getStrList();

		then(actual).hasSize(1);
	}

	@Property
	void registerFieldSet() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(StringValue.class, fixture -> fixture.giveMeBuilder(StringValue.class).set("value", "test"))
			.build();

		List<StringValue> actual = sut.giveMeOne(NestedStringList.class)
			.getValues();

		then(actual).allMatch(it -> "test".equals(it.getValue()));
	}

	@Property
	void registerFieldSize() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(
				ListStringObject.class,
				fixture -> fixture.giveMeBuilder(ListStringObject.class).size("values", 1)
			)
			.build();

		List<ListStringObject> actual = sut.giveMeOne(NestedListStringObject.class)
			.getValues();

		then(actual).allMatch(it -> it.getValues().size() == 1);
	}

	@Property
	void sizeRegisteredElement() {
		String expected = "test";
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(String.class, fixture -> fixture.giveMeBuilder(String.class).set(expected))
			.build();

		List<String> actual = sut.giveMeBuilder(ListStringObject.class)
			.size("values", 5)
			.sample()
			.getValues();

		then(actual).allMatch(expected::equals);
	}

	@Property
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

	@Property
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

	@Property
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

	@Property
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

	@Property
	void applySizeWhenRegisteredWithSize() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(
				ListStringObject.class,
				fixture -> fixture.giveMeBuilder(ListStringObject.class).size("values", 5)
			)
			.build();

		// when
		List<String> actual = sut.giveMeBuilder(ListStringObject.class)
			.apply((it, builder) -> builder.size("values", 10))
			.sample()
			.getValues();

		then(actual).hasSize(10);
	}

	@Property
	void sizeWhenRegisterSizeInApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(
				ListStringObject.class,
				fixture -> fixture.giveMeBuilder(ListStringObject.class)
					.apply((it, builder) -> builder.size("values", 1))
			)
			.build();

		// when
		List<String> actual = sut.giveMeBuilder(ListStringObject.class)
			.size("values", 2)
			.sample()
			.getValues();

		then(actual).hasSize(2);
	}

	@Property
	void sizeWhenRegisterApply() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(
				ListStringObject.class,
				fixture -> fixture.giveMeBuilder(ListStringObject.class)
					.size("values", 1)
					.apply((it, builder) -> {
					})
			)
			.build();

		// when
		List<String> actual = sut.giveMeBuilder(ListStringObject.class)
			.size("values", 2)
			.sample()
			.getValues();

		then(actual).hasSize(2);
	}

	@Property
	void sampleEnumMapWithEnumSizeIsLessThanContainerInfoMaxSize() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(0, 5))
			.build();

		Map<TwoEnum, String> values = sut.giveMeOne(new TypeReference<Map<TwoEnum, String>>() {
		});

		then(values).hasSizeLessThanOrEqualTo(2);
	}

	@Property
	void sampleEnumMapWithEnumSizeIsLessThanContainerInfoMinSize() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(3, 5))
			.build();

		Map<TwoEnum, String> values = sut.giveMeOne(new TypeReference<Map<TwoEnum, String>>() {
		});

		then(values).hasSizeLessThanOrEqualTo(2);
	}

	@Property
	void sampleEnumSetWithEnumSizeIsLessThanContainerInfoMinSize() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultArbitraryContainerInfoGenerator(context -> new ArbitraryContainerInfo(3, 5))
			.build();

		Set<TwoEnum> values = sut.giveMeOne(new TypeReference<Set<TwoEnum>>() {
		});

		then(values).hasSizeLessThanOrEqualTo(2);
	}

	@Property
	void interfaceImplements() {
		// given
		List<Class<? extends GetFixedValue>> implementations = new ArrayList<>();
		implementations.add(GetIntegerFixedValue.class);
		implementations.add(GetStringFixedValue.class);

		FixtureMonkey sut = FixtureMonkey.builder()
			.interfaceImplements(GetFixedValue.class, implementations)
			.build();

		// when
		Object actual = sut.giveMeOne(GetFixedValue.class).get();

		then(actual).isIn(1, "fixed");
	}

	@Property
	void sampleGenericInterface() {
		// given
		List<Class<? extends GetFixedValue>> implementations = new ArrayList<>();
		implementations.add(GetIntegerFixedValue.class);
		implementations.add(GetStringFixedValue.class);

		FixtureMonkey sut = FixtureMonkey.builder()
			.interfaceImplements(GetFixedValue.class, implementations)
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

	@Property
	void sampleGenericInterfaceReturnsDiff() {
		// given
		List<Class<? extends GetFixedValue>> implementations = new ArrayList<>();
		implementations.add(GetIntegerFixedValue.class);
		implementations.add(GetStringFixedValue.class);

		FixtureMonkey sut = FixtureMonkey.builder()
			.interfaceImplements(GetFixedValue.class, implementations)
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

	@Property
	void sampleInterfaceChildWhenOptionHasHierarchy() {
		// given
		List<Class<? extends GetFixedValue>> implementations = new ArrayList<>();
		implementations.add(GetIntegerFixedValue.class);
		implementations.add(GetStringFixedValue.class);

		List<Class<? extends GetFixedValueChild>> childImplementations = new ArrayList<>();
		childImplementations.add(GetIntegerFixedValueChild.class);

		// when
		FixtureMonkey sut = FixtureMonkey.builder()
			.interfaceImplements(GetFixedValueChild.class, childImplementations)
			.interfaceImplements(GetFixedValue.class, implementations)
			.build();

		Object actual = sut.giveMeOne(new TypeReference<GetFixedValueChild>() {
			})
			.get();

		then(actual).isEqualTo(2);
	}

	@Property
	void sampleConcreteWhenHasSameNameProperty() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.interfaceImplements(
				AbstractSamePropertyValue.class,
				Collections.singletonList(ConcreteSamePropertyValue.class)
			)
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.build();

		AbstractSamePropertyValue actual = sut.giveMeOne(AbstractSamePropertyValue.class);

		then(actual).isNotNull();
	}

	@Property
	void setConcreteListWithNoParentValue() {
		// given
		List<Class<? extends AbstractNoneValue>> implementations = new ArrayList<>();
		implementations.add(AbstractNoneConcreteStringValue.class);
		implementations.add(AbstractNoneConcreteIntValue.class);

		FixtureMonkey sut = FixtureMonkey.builder()
			.interfaceImplements(AbstractNoneValue.class, implementations)
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

	@Property
	void setConcreteClassWhenHasParentValue() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.interfaceImplements(AbstractValue.class, Collections.singletonList(ConcreteStringValue.class))
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

	@Property
	void setConcreteClassWhenHasNoParentValue() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.interfaceImplements(
				AbstractNoneValue.class,
				Collections.singletonList(AbstractNoneConcreteStringValue.class)
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

	@Property
	void setConcreteList() {
		// given
		List<Class<? extends AbstractValue>> implementations = new ArrayList<>();
		implementations.add(ConcreteStringValue.class);
		implementations.add(ConcreteIntValue.class);

		FixtureMonkey sut = FixtureMonkey.builder()
			.interfaceImplements(AbstractValue.class, implementations)
			.build();

		ConcreteStringValue concreteStringValue = new ConcreteStringValue();
		concreteStringValue.setValue("stringValue");
		concreteStringValue.setStringValue("test");
		ConcreteIntValue concreteIntValue = new ConcreteIntValue();
		concreteIntValue.setValue("intValue");
		concreteIntValue.setIntValue(-999);
		List<AbstractValue> expected = new ArrayList<>();
		expected.add(concreteStringValue);
		expected.add(concreteIntValue);

		// when
		List<AbstractValue> actual = sut.giveMeBuilder(new TypeReference<List<AbstractValue>>() {
			})
			.set(expected)
			.sample();

		then(actual).isEqualTo(expected);
	}

	@Property
	void sampleSelfRecursiveAbstract() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.interfaceImplements(
				SelfRecursiveAbstractValue.class,
				Collections.singletonList(SelfRecursiveImplementationValue.class)
			)
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.build();

		SelfRecursiveAbstractValue actual = sut.giveMeOne(SelfRecursiveAbstractValue.class);

		then(actual).isNotNull();
	}

	@Property
	void sizeElementWhenRegisteredSize() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.register(
				NestedListStringObject.class,
				fixture -> fixture.giveMeBuilder(new TypeReference<NestedListStringObject>() {
					})
					.size("values", 5)
			)
			.build();

		List<ListStringObject> actual = sut.giveMeBuilder(NestedListStringObject.class)
			.size("values[*].values", 3, 5)
			.sample()
			.getValues();

		then(actual).allMatch(it -> it.getValues().size() >= 3 && it.getValues().size() <= 5);
	}

	@Property
	void samePropertyDiffImplementations() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.interfaceImplements(
				GetterInterface.class,
				Arrays.asList(
					GetterInterfaceImplementation.class,
					GetterInterfaceImplementation2.class
				)
			)
			.build();

		String actual = sut.giveMeBuilder(GetterInterface.class)
			.set("value", "expected")
			.sample()
			.getValue();

		then(actual).isEqualTo("expected");
	}

	@Property
	void sampleWithMonkeyStringArbitrary() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.javaTypeArbitraryGenerator(
				new JavaTypeArbitraryGenerator() {
					@Override
					public StringArbitrary strings() {
						return new MonkeyStringArbitrary();
					}
				}
			)
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isNotNull();
	}

	@Property
	void filterWithMonkeyStringArbitrary() {
		FixtureMonkey sut = FixtureMonkey.builder().javaTypeArbitraryGenerator(new JavaTypeArbitraryGenerator() {
			@Override
			public StringArbitrary strings() {
				return new MonkeyStringArbitrary().filterCharacter(Character::isUpperCase);
			}
		}).build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isUpperCase();
	}

	@Property
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

	@Property
	void multipleFiltersWithMonkeyStringArbitrary() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.javaTypeArbitraryGenerator(
				new JavaTypeArbitraryGenerator() {
					@Override
					public MonkeyStringArbitrary monkeyStrings() {
						return new MonkeyStringArbitrary().filterCharacter(c -> !Character.isISOControl(c))
							.filterCharacter(Character::isUpperCase);
					}
				}
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

	@Property
	void alterDefaultArbitraryGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultArbitraryGenerator(generator -> new CompositeArbitraryGenerator(
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

	@Property
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

	@Property
	void uniqueArbitraryGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultArbitraryGenerator(UniqueArbitraryGenerator::new)
			.build();

		List<String> actual = sut.giveMe(String.class, 100);

		Set<String> expected = new HashSet<>(actual);
		then(actual).hasSameSizeAs(expected);
	}

	@Property
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
}
