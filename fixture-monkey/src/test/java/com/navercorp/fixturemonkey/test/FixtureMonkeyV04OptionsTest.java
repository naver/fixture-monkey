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
import java.time.temporal.Temporal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import javax.validation.ConstraintViolationException;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Property;
import net.jqwik.api.TooManyFilterMissesException;
import net.jqwik.api.arbitraries.StringArbitrary;
import net.jqwik.time.api.DateTimes;
import net.jqwik.time.api.arbitraries.InstantArbitrary;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectArbitraryPropertyGenerator;
import com.navercorp.fixturemonkey.api.introspector.JavaArbitraryResolver;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeArbitraryResolver;
import com.navercorp.fixturemonkey.api.introspector.JavaTimeTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.introspector.JavaTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.matcher.ExactTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.MatcherOperator;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.fixturemonkey.resolver.RootNodeResolver;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04OptionsAdditionalTestSpecs.RegisterGroup;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04OptionsAdditionalTestSpecs.SimpleObjectChild;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04TestSpecs.ComplexObject;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04TestSpecs.SimpleObject;

class FixtureMonkeyV04OptionsTest {
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
	void alterMonkeyFactory() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.monkeyExpressionFactory((expression) -> RootNodeResolver::new)
			.build();
		String expected = "expected";

		String actual = sut.giveMeBuilder(String.class)
			.set("test", expected)
			.sample();

		then(actual).isEqualTo(expected);
	}

	@Property
	void alterDefaultArbitraryPropertyGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultArbitraryPropertyGenerator(
				(context) -> ObjectArbitraryPropertyGenerator.INSTANCE.generate(context)
					.withNullInject(1.0)
			)
			.build();

		ComplexObject actual = sut.giveMeOne(ComplexObject.class);

		then(actual).isNull();
	}

	@Property
	void pushAssignableTypeArbitraryPropertyGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushAssignableTypeArbitraryPropertyGenerator(
				SimpleObject.class,
				(context) -> ObjectArbitraryPropertyGenerator.INSTANCE.generate(context)
					.withNullInject(1.0)
			)
			.build();

		SimpleObjectChild actual = sut.giveMeOne(SimpleObjectChild.class);

		then(actual).isNull();
	}

	@Property
	void pushExactTypeArbitraryPropertyGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushExactTypeArbitraryPropertyGenerator(
				SimpleObjectChild.class,
				(context) -> ObjectArbitraryPropertyGenerator.INSTANCE.generate(context)
					.withNullInject(1.0)
			)
			.build();

		SimpleObjectChild actual = sut.giveMeOne(SimpleObjectChild.class);

		then(actual).isNull();
	}

	@Property
	void pushExactTypeArbitraryPropertyGeneratorNotAffectsAssignable() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushExactTypeArbitraryPropertyGenerator(
				SimpleObject.class,
				(context) -> ObjectArbitraryPropertyGenerator.INSTANCE.generate(context)
					.withNullInject(1.0)
			)
			.build();

		SimpleObjectChild actual = sut.giveMeOne(SimpleObjectChild.class);

		then(actual).isNotNull();
	}

	@Property
	void pushArbitraryPropertyGenerator() {
		ArbitraryPropertyGenerator arbitraryPropertyGenerator = (context) ->
			ObjectArbitraryPropertyGenerator.INSTANCE.generate(context)
				.withNullInject(1.0);
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushArbitraryPropertyGenerator(
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
				(context, containerInfo) -> 1.0d
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
				(context, containerInfo) -> 1.0d
			)
			.build();

		SimpleObject actual = sut.giveMeOne(SimpleObject.class);

		then(actual).isNull();
	}

	@Property
	void pushNullInjectGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.pushNullInjectGenerator(
				MatcherOperator.exactTypeMatchOperator(SimpleObject.class, (context, containerInfo) -> 1.0d)
			)
			.build();

		SimpleObject actual = sut.giveMeOne(SimpleObject.class);

		then(actual).isNull();
	}

	@Property
	void defaultNullInjectGenerator() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultNullInjectGenerator((context, containerInfo) -> 1.0d)
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
			.pushAssignableTypePropertyNameResolver(Temporal.class, (property) -> "temporal")
			.build();
		Instant expected = Instant.now();

		Instant actual = sut.giveMeBuilder(SimpleObject.class)
			.set("temporal", expected)
			.sample()
			.getInstant();

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
			.defaultArbitraryContainerMaxSize(1)
			.build();

		List<SimpleObject> actual = sut.giveMeOne(ComplexObject.class)
			.getList();

		then(actual).hasSizeLessThanOrEqualTo(1);
	}

	@Property
	void defaultArbitraryContainerInfo() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.defaultArbitraryContainerInfo(new ArbitraryContainerInfo(3, 3))
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
				throw new ConstraintViolationException("thrown by test ArbitraryValidator", new HashSet<>());
			})
			.build();

		thenThrownBy(() -> sut.giveMeOne(String.class))
			.isExactlyInstanceOf(TooManyFilterMissesException.class);
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

		then(actual).isEqualTo("test");
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
}
