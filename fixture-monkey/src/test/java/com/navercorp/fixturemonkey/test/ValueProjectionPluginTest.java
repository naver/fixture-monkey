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

import static org.assertj.core.api.BDDAssertions.then;

import java.time.Instant;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.arbitraries.StringArbitrary;
import net.jqwik.time.api.DateTimes;
import net.jqwik.time.api.arbitraries.InstantArbitrary;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.jqwik.JavaArbitraryResolver;
import com.navercorp.fixturemonkey.api.jqwik.JavaTimeArbitraryResolver;
import com.navercorp.fixturemonkey.api.jqwik.JavaTimeTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.jqwik.JavaTypeArbitraryGenerator;
import com.navercorp.fixturemonkey.api.jqwik.JqwikPlugin;
import com.navercorp.fixturemonkey.test.ValueProjectionAssembleSpecs.InstantObject;
import com.navercorp.fixturemonkey.test.ValueProjectionAssembleSpecs.SimpleObject;

class ValueProjectionPluginTest {

	private static final long SEED = 12345L;

	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
		.defaultNotNull(true)
		.build();

	@Test
	void javaTypeArbitraryGenerator() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new JqwikPlugin().javaTypeArbitraryGenerator(
					new JavaTypeArbitraryGenerator() {
						@Override
						public StringArbitrary strings() {
							return Arbitraries.strings().numeric();
						}
					}
				)
			)
			.build();

		// when
		String actual = sut.giveMeOne(String.class);

		// then
		then(actual).matches(Pattern.compile("\\d*"));
	}

	@Test
	void javaTypeArbitraryGeneratorAffectsField() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new JqwikPlugin().javaTypeArbitraryGenerator(
					new JavaTypeArbitraryGenerator() {
						@Override
						public StringArbitrary strings() {
							return Arbitraries.strings().numeric();
						}
					}
				)
			)
			.build();

		// when
		String actual = sut.giveMeOne(SimpleObject.class).getStr();

		// then
		then(actual).matches(it -> it == null || Pattern.compile("\\d*").matcher(it).matches());
	}

	@Test
	void javaArbitraryResolver() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new JqwikPlugin().javaArbitraryResolver(
					new JavaArbitraryResolver() {
						@Override
						public Arbitrary<String> strings(
							StringArbitrary stringArbitrary,
							ArbitraryGeneratorContext context
						) {
							return Arbitraries.just("test");
						}
					}
				)
			)
			.build();

		// when
		String actual = sut.giveMeOne(String.class);

		// then
		then(actual).isEqualTo("test");
	}

	@Test
	void javaArbitraryResolverAffectsField() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new JqwikPlugin().javaArbitraryResolver(
					new JavaArbitraryResolver() {
						@Override
						public Arbitrary<String> strings(
							StringArbitrary stringArbitrary,
							ArbitraryGeneratorContext context
						) {
							return Arbitraries.just("test");
						}
					}
				)
			)
			.build();

		// when
		String actual = sut.giveMeOne(SimpleObject.class).getStr();

		// then
		then(actual).isIn("test", null);
	}

	@Test
	void javaTimeTypeArbitraryGenerator() {
		// given
		Instant expected = Instant.now();
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new JqwikPlugin().javaTimeTypeArbitraryGenerator(
					new JavaTimeTypeArbitraryGenerator() {
						@Override
						public InstantArbitrary instants() {
							return DateTimes.instants().between(expected, expected);
						}
					}
				)
			)
			.build();

		// when
		Instant actual = sut.giveMeOne(Instant.class);

		// then
		then(actual).isEqualTo(expected);
	}

	@Test
	void javaTimeTypeArbitraryGeneratorAffectsField() {
		// given
		Instant expected = Instant.now();
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new JqwikPlugin().javaTimeTypeArbitraryGenerator(
					new JavaTimeTypeArbitraryGenerator() {
						@Override
						public InstantArbitrary instants() {
							return DateTimes.instants().between(expected, expected);
						}
					}
				)
			)
			.build();

		// when
		Instant actual = sut.giveMeOne(InstantObject.class).getInstant();

		// then
		then(actual).isIn(null, expected);
	}

	@Test
	void javaTimeArbitraryResolver() {
		// given
		Instant expected = Instant.now();
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new JqwikPlugin().javaTimeArbitraryResolver(
					new JavaTimeArbitraryResolver() {
						@Override
						public Arbitrary<Instant> instants(
							InstantArbitrary instantArbitrary,
							ArbitraryGeneratorContext context
						) {
							return Arbitraries.just(expected);
						}
					}
				)
			)
			.build();

		// when
		Instant actual = sut.giveMeOne(Instant.class);

		// then
		then(actual).isEqualTo(expected);
	}

	@Test
	void javaTimeArbitraryResolverAffectsField() {
		// given
		Instant expected = Instant.now();
		FixtureMonkey sut = FixtureMonkey.builder()
			.plugin(
				new JqwikPlugin().javaTimeArbitraryResolver(
					new JavaTimeArbitraryResolver() {
						@Override
						public Arbitrary<Instant> instants(
							InstantArbitrary instantArbitrary,
							ArbitraryGeneratorContext context
						) {
							return Arbitraries.just(expected);
						}
					}
				)
			)
			.build();

		// when
		Instant actual = sut.giveMeOne(InstantObject.class).getInstant();

		// then
		then(actual).isIn(expected, null);
	}
}
