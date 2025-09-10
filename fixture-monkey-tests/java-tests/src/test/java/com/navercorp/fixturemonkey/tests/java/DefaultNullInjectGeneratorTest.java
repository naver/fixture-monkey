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

import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.ALWAYS_NULL_INJECT;
import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.DEFAULT_NOTNULL_ANNOTATION_TYPES;
import static com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator.DEFAULT_NULLABLE_ANNOTATION_TYPES;
import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.HashSet;

import org.junit.jupiter.api.RepeatedTest;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.tests.java.specs.DefaultNullInjectGeneratorSpecs.NonNullAnnotationObject;
import com.navercorp.fixturemonkey.tests.java.specs.DefaultNullInjectGeneratorSpecs.NullableAnnotationObject;

class DefaultNullInjectGeneratorTest {
	@RepeatedTest(TEST_COUNT)
	void nonNullAnnotations() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNullInjectGenerator(
				new DefaultNullInjectGenerator(
					ALWAYS_NULL_INJECT,
					false,
					false,
					false,
					new HashSet<>(DEFAULT_NULLABLE_ANNOTATION_TYPES),
					new HashSet<>(DEFAULT_NOTNULL_ANNOTATION_TYPES)
				)
			)
			.build();

		NonNullAnnotationObject actual = sut.giveMeOne(NonNullAnnotationObject.class);

		then(actual.getJavaxNonNullField()).isNotNull();
		then(actual.getJspecifyNonNullField()).isNotNull();
		then(actual.getCheckerNonNullField()).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void nullableAnnotations() {
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNullInjectGenerator(
				new DefaultNullInjectGenerator(
					ALWAYS_NULL_INJECT,
					false,
					true,
					false,
					new HashSet<>(DEFAULT_NULLABLE_ANNOTATION_TYPES),
					new HashSet<>(DEFAULT_NOTNULL_ANNOTATION_TYPES)
				)
			)
			.build();

		NullableAnnotationObject actual = sut.giveMeOne(NullableAnnotationObject.class);

		then(actual.getJavaxNullableField()).isNull();
		then(actual.getJspecifyNullableField()).isNull();
		then(actual.getCheckerNullableField()).isNull();
	}
}
