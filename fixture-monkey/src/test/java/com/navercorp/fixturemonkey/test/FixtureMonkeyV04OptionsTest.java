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

import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.ALWAYS_NULL_INJECT;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNoException;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.lang.reflect.AnnotatedType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.validation.ConstraintViolationException;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Property;
import net.jqwik.api.TooManyFilterMissesException;
import net.jqwik.api.arbitraries.StringArbitrary;
import net.jqwik.time.api.DateTimes;
import net.jqwik.time.api.arbitraries.InstantArbitrary;

import com.navercorp.fixturemonkey.LabMonkey;
import com.navercorp.fixturemonkey.api.customizer.FixtureCustomizer;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ChildArbitraryContext;
import com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.DefaultObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.JavaArbitraryResolver;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeArbitraryResolver;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.introspector.JavaTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.matcher.ExactTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.type.TypeReference;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.resolver.DecomposableContainerValue;
import com.navercorp.fixturemonkey.resolver.IdentityNodeResolver;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04OptionsAdditionalTestSpecs.BuilderInteger;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04OptionsAdditionalTestSpecs.CustomBuildMethodInteger;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04OptionsAdditionalTestSpecs.CustomBuilderMethodInteger;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04OptionsAdditionalTestSpecs.Pair;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04OptionsAdditionalTestSpecs.PairContainerPropertyGenerator;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04OptionsAdditionalTestSpecs.PairIntrospector;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04OptionsAdditionalTestSpecs.RegisterGroup;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04OptionsAdditionalTestSpecs.SimpleObjectChild;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04TestSpecs.ComplexObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04TestSpecs.Interface;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04TestSpecs.InterfaceFieldObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04TestSpecs.InterfaceImplementation;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04TestSpecs.ListStringObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04TestSpecs.NullableObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04TestSpecs.SimpleObject;

class FixtureMonkeyV04OptionsTest {
	@Property
	void strictModeSetWrongExpressionThrows() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder().useExpressionStrictMode().build();

		thenThrownBy(
			() -> sut.giveMeBuilder(String.class)
				.set("nonExistentField", 0)
				.sample()
		).isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("No matching results for given NodeResolvers.");
	}

	@Property
	void notStrictModeSetWrongExpressionDoesNotThrows() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder().build();

		thenNoException()
			.isThrownBy(() -> sut.giveMeBuilder(String.class)
				.set("nonExistentField", 0)
				.sample());
	}

	@Property
	void alterMonkeyFactory() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.monkeyExpressionFactory((expression) -> () -> IdentityNodeResolver.INSTANCE)
			.build();
		String expected = "expected";

		String actual = sut.giveMeBuilder(String.class)
			.set("test", expected)
			.sample();

		then(actual).isEqualTo(expected);
	}

	@Property
	void alterDefaultArbitraryPropertyGenerator() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.pushNullInjectGenerator(
				MatcherOperator.exactTypeMatchOperator(SimpleObject.class, (context) -> 1.0d)
			)
			.build();

		SimpleObject actual = sut.giveMeOne(SimpleObject.class);

		then(actual).isNull();
	}

	@Property
	void defaultNullInjectGenerator() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.defaultNullInjectGenerator((context) -> 1.0d)
			.build();

		SimpleObject actual = sut.giveMeOne(SimpleObject.class);

		then(actual).isNull();
	}

	@Property
	void pushExactTypePropertyNameResolver() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.pushAssignableTypePropertyNameResolver(Interface.class, (property) -> "interface")
			.build();
		InterfaceImplementation expected = new InterfaceImplementation();
		expected.setValue("test");

		InterfaceImplementation actual = sut.giveMeBuilder(InterfaceFieldObject.class)
			.set("interface", expected)
			.sample()
			.getValue();

		then(actual).isEqualTo(expected);
	}

	@Property
	void pushPropertyNameResolver() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.pushAssignableTypeArbitraryIntrospector(
				SimpleObject.class,
				(context) -> new ArbitraryIntrospectorResult(null)
			)
			.build();

		SimpleObjectChild actual = sut.giveMeOne(SimpleObjectChild.class);

		then(actual).isNull();
	}

	@Property
	void pushExactTypeArbitraryIntrospector() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.pushExactTypeArbitraryIntrospector(
				SimpleObjectChild.class,
				(context) -> new ArbitraryIntrospectorResult(null)
			)
			.build();

		SimpleObjectChild actual = sut.giveMeOne(SimpleObjectChild.class);

		then(actual).isNull();
	}

	@Property
	void pushArbitraryIntrospector() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.pushArbitraryIntrospector(
				MatcherOperator.exactTypeMatchOperator(
					SimpleObjectChild.class,
					(context) -> new ArbitraryIntrospectorResult(null)
				)
			)
			.build();

		SimpleObjectChild actual = sut.giveMeOne(SimpleObjectChild.class);

		then(actual).isNull();
	}

	@Property
	void objectIntrospector() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
					(context) -> new ArbitraryContainerInfo(5, 5, false)
				)
			)
			.build();

		List<String> actual = sut.giveMeOne(ComplexObject.class)
			.getStrList();

		then(actual).hasSize(5);
	}

	@Property
	void pushArbitraryContainerInfoGeneratorNotMatching() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
					(context) -> new ArbitraryContainerInfo(5, 5, false)
				)
			)
			.build();

		List<SimpleObject> actual = sut.giveMeOne(ComplexObject.class)
			.getList();

		then(actual).hasSizeBetween(0, 3);
	}

	@Property
	void defaultArbitraryContainerMaxSize() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.defaultArbitraryContainerMaxSize(1)
			.build();

		List<SimpleObject> actual = sut.giveMeOne(ComplexObject.class)
			.getList();

		then(actual).hasSizeLessThanOrEqualTo(1);
	}

	@Property
	void defaultArbitraryContainerInfo() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.defaultArbitraryContainerInfo(new ArbitraryContainerInfo(3, 3, false))
			.build();

		List<SimpleObject> actual = sut.giveMeOne(ComplexObject.class)
			.getList();

		then(actual).hasSize(3);
	}

	@Property
	void javaTypeArbitraryGenerator() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.arbitraryValidator(obj -> {
				throw new ConstraintViolationException("thrown by test ArbitraryValidator", new HashSet<>());
			})
			.build();

		thenThrownBy(() -> sut.giveMeOne(String.class))
			.isExactlyInstanceOf(TooManyFilterMissesException.class);
	}

	@Property
	void defaultNotNull() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.defaultNotNull(true)
			.build();

		String actual = sut.giveMeOne(SimpleObject.class)
			.getStr();

		then(actual).isNotNull();
	}

	@Property
	void defaultNotNullNotWorksWhenSetDefaultNullInjectGenerator() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.pushExceptGenerateType(new ExactTypeMatcher(String.class))
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isNull();
	}

	@Property
	void addExceptGenerateClass() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.addExceptGenerateClass(String.class)
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isNull();
	}

	@Property
	void addExceptGenerateClassNotGenerateField() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.addExceptGenerateClass(String.class)
			.build();

		String actual = sut.giveMeOne(SimpleObject.class)
			.getStr();

		then(actual).isNull();
	}

	@Property
	void addExceptGeneratePackage() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.addExceptGeneratePackage("java.lang")
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isNull();
	}

	@Property
	void addExceptGeneratePackageNotGenerateField() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.addExceptGeneratePackage("java.lang")
			.build();

		String actual = sut.giveMeOne(SimpleObject.class)
			.getStr();

		then(actual).isNull();
	}

	@Property
	void registerInstance() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.register(String.class, monkey -> monkey.giveMeBuilder("test"))
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isEqualTo("test");
	}

	@Property
	void registerField() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.register(String.class, monkey -> monkey.giveMeBuilder("test"))
			.build();

		String actual = sut.giveMeOne(SimpleObject.class)
			.getStr();

		then(actual).isEqualTo("test");
	}

	@Property
	void registerGroup() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.registerGroup(RegisterGroup.class)
			.build();

		String actual = sut.giveMeOne(SimpleObject.class)
			.getStr();

		then(actual).isEqualTo("test");
	}

	@Property
	void registerSameInstancesTwiceWorksLast() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.register(String.class, monkey -> monkey.giveMeBuilder("test"))
			.build();

		String actual = sut.giveMeBuilder(String.class)
			.set("$", expected)
			.sample();

		then(actual).isEqualTo(expected);
	}

	@Property
	void pushArbitraryCustomizerCustomizeFixtureSetValue() {
		String expected = "test";
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.pushExactTypeArbitraryCustomizer(String.class, (object) -> expected)
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isEqualTo(expected);
	}

	@SuppressWarnings("ConstantConditions")
	@Property
	void pushArbitraryCustomizerCustomizeFixtureModifyValue() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.pushExactTypeArbitraryCustomizer(String.class, String::toLowerCase)
			.build();

		String actual = sut.giveMeOne(String.class);

		then(actual).isLowerCase();
	}

	@Property
	void pushArbitraryCustomizerCustomizeFields() {
		String expected = "test";
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.pushExactTypeArbitraryCustomizer(SimpleObject.class, new FixtureCustomizer<SimpleObject>() {
				@Override
				public void customizeProperties(ChildArbitraryContext childArbitraryContext) {
					childArbitraryContext.replaceArbitrary(
						property -> "str".equals(property.getName()),
						Arbitraries.just(expected)
					);
				}

				@Nullable
				@Override
				public SimpleObject customizeFixture(@Nullable SimpleObject object) {
					return object;
				}
			})
			.build();

		String actual = sut.giveMeOne(SimpleObject.class)
			.getStr();

		then(actual).isEqualTo(expected);
	}

	@Property
	void nullableElement() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.defaultNullInjectGenerator(
				new DefaultNullInjectGenerator(
					1.0d,
					false,
					false,
					true,
					new HashSet<>(),
					new HashSet<>()
				)
			)
			.build();

		List<String> actual = sut.giveMeOne(new TypeReference<List<String>>() {
		});

		then(actual).allMatch(Objects::isNull);
	}

	@Property
	void generateNewContainer() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.pushAssignableTypeContainerPropertyGenerator(Pair.class, new PairContainerPropertyGenerator())
			.pushContainerIntrospector(new PairIntrospector())
			.defaultDecomposedContainerValueFactory(
				(obj) -> {
					if (obj instanceof Pair) {
						Pair<?, ?> pair = (Pair<?, ?>)obj;
						List<Object> list = new ArrayList<>();
						list.add(pair.getFirst());
						list.add(pair.getSecond());
						return new DecomposableContainerValue(list, 1);
					}
					throw new IllegalArgumentException(
						"given type is not supported container : " + obj.getClass().getTypeName()
					);
				}
			)
			.build();

		Pair<String, String> pair = sut.giveMeBuilder(new TypeReference<Pair<String, String>>() {
			})
			.fixed()
			.sample();

		then(pair).isNotNull();
	}

	@Property
	void decomposeNewContainerByAddContainerType() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.addContainerType(
				Pair.class,
				new PairContainerPropertyGenerator(),
				new PairIntrospector(),
				(obj) -> {
					Pair<?, ?> pair = (Pair<?, ?>)obj;
					List<Object> list = new ArrayList<>();
					list.add(pair.getFirst());
					list.add(pair.getSecond());
					return new DecomposableContainerValue(list, 1);
				}
			)
			.build();

		Pair<String, String> pair = sut.giveMeBuilder(new TypeReference<Pair<String, String>>() {
			})
			.fixed()
			.sample();

		then(pair).isNotNull();
	}

	@Property
	void plugin() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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

		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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

		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.register(ComplexObject.class, fixture -> fixture.giveMeBuilder(ComplexObject.class).size("strList", 1))
			.build();

		List<String> actual = sut.giveMeOne(ComplexObject.class)
			.getStrList();

		then(actual).hasSize(1);
	}

	@Property
	void sizeRegisteredElement() {
		String expected = "test";
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.register(String.class, it -> it.giveMeBuilder(String.class).set("$", Arbitraries.strings().alpha()))
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
			1.0,
			true,
			false,
			false,
			Collections.emptySet(),
			Collections.emptySet()
		);
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.defaultNullInjectGenerator(nullInjectGenerator)
			.build();

		List<String> values = sut.giveMeOne(NullableObject.class)
			.getValues();

		then(values).isNull();
	}
}
