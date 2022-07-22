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

import com.navercorp.fixturemonkey.LabMonkey;
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
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04OptionsAdditionalTestSpecs.SimpleObjectChild;
import com.navercorp.fixturemonkey.test.FixtureMonkeyV04TestSpecs.ComplexObject;
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.pushNullInjectGenerator(
				MatcherOperator.exactTypeMatchOperator(SimpleObject.class, (context, containerInfo) -> 1.0d)
			)
			.build();

		SimpleObject actual = sut.giveMeOne(SimpleObject.class);

		then(actual).isNull();
	}

	@Property
	void defaultNullInjectGenerator() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.defaultNullInjectGenerator((context, containerInfo) -> 1.0d)
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
	void pushArbitraryContainerInfoGenerator() {
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.pushArbitraryContainerInfoGenerator(
				new MatcherOperator<>(
					(property) -> {
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
		LabMonkey sut = LabMonkey.labMonkeyBuilder()
			.pushArbitraryContainerInfoGenerator(
				new MatcherOperator<>(
					(property) -> {
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
			.defaultArbitraryContainerInfo(new ArbitraryContainerInfo(3, 3))
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
}
