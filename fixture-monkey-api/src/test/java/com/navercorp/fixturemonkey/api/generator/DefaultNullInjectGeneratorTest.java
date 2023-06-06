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
package com.navercorp.fixturemonkey.api.generator;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Collections;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.type.TypeReference;

@SuppressWarnings("OptionalGetWithoutIsPresent")
class DefaultNullInjectGeneratorTest {
	@Test
	void generateNullable() {
		// given
		DefaultNullInjectGenerator sut = new DefaultNullInjectGenerator();
		TypeReference<SampleWithAnnotation> typeReference = new TypeReference<SampleWithAnnotation>() {
		};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "nullable").get();
		ObjectPropertyGeneratorContext context = new ObjectPropertyGeneratorContext(
			property,
			null,
			null,
			false,
			GenerateOptions.DEFAULT_GENERATE_OPTIONS
		);

		// when
		double actual = sut.generate(context);

		then(actual).isEqualTo(0.2d);
	}

	@Test
	void generateNonNull() {
		// given
		DefaultNullInjectGenerator sut = new DefaultNullInjectGenerator();
		TypeReference<SampleWithAnnotation> typeReference = new TypeReference<SampleWithAnnotation>() {
		};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "nonnull").get();
		ObjectPropertyGeneratorContext context = new ObjectPropertyGeneratorContext(
			property,
			null,
			null,
			false,
			GenerateOptions.DEFAULT_GENERATE_OPTIONS
		);

		// when
		double actual = sut.generate(context);

		then(actual).isEqualTo(0.0d);
	}

	@Test
	void generateNotNull() {
		// given
		DefaultNullInjectGenerator sut = new DefaultNullInjectGenerator();
		TypeReference<SampleWithAnnotation> typeReference = new TypeReference<SampleWithAnnotation>() {
		};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "notNull").get();
		ObjectPropertyGeneratorContext context = new ObjectPropertyGeneratorContext(
			property,
			null,
			null,
			false,
			GenerateOptions.DEFAULT_GENERATE_OPTIONS
		);

		// when
		double actual = sut.generate(context);

		then(actual).isEqualTo(DefaultNullInjectGenerator.NOT_NULL_INJECT);
	}

	@Test
	void generateDefaultNotNullFalse() {
		// given
		DefaultNullInjectGenerator sut = new DefaultNullInjectGenerator(
			0.2,
			false,
			false,
			false,
			Collections.emptySet(),
			Collections.emptySet()
		);
		TypeReference<SampleWithAnnotation> typeReference = new TypeReference<SampleWithAnnotation>() {
		};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "defaultValue").get();
		ObjectPropertyGeneratorContext context = new ObjectPropertyGeneratorContext(
			property,
			null,
			null,
			false,
			GenerateOptions.DEFAULT_GENERATE_OPTIONS
		);

		// when
		double actual = sut.generate(context);

		then(actual).isEqualTo(0.2d);
	}

	@Test
	void generateDefaultNotNullTrue() {
		// given
		DefaultNullInjectGenerator sut = new DefaultNullInjectGenerator(
			0.2,
			false,
			true,
			false,
			Collections.emptySet(),
			Collections.emptySet()
		);
		TypeReference<SampleWithAnnotation> typeReference = new TypeReference<SampleWithAnnotation>() {
		};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "defaultValue").get();
		ObjectPropertyGeneratorContext context = new ObjectPropertyGeneratorContext(
			property,
			null,
			null,
			false,
			GenerateOptions.DEFAULT_GENERATE_OPTIONS
		);

		// when
		double actual = sut.generate(context);

		then(actual).isEqualTo(0.0d);
	}

	@Test
	void generatePrimitive() {
		// given
		DefaultNullInjectGenerator sut = new DefaultNullInjectGenerator();
		TypeReference<SampleWithAnnotation> typeReference = new TypeReference<SampleWithAnnotation>() {
		};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "primitive").get();
		ObjectPropertyGeneratorContext context = new ObjectPropertyGeneratorContext(
			property,
			null,
			null,
			false,
			GenerateOptions.DEFAULT_GENERATE_OPTIONS
		);

		// when
		double actual = sut.generate(context);

		then(actual).isEqualTo(0.0d);
	}

	@Test
	void generateContainer() {
		// given
		DefaultNullInjectGenerator sut = new DefaultNullInjectGenerator(
			0.2,
			false,
			true,
			false,
			Collections.emptySet(),
			Collections.emptySet()
		);
		TypeReference<SampleWithAnnotation> typeReference = new TypeReference<SampleWithAnnotation>() {
		};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "container").get();
		ObjectPropertyGeneratorContext context = new ObjectPropertyGeneratorContext(
			property,
			null,
			null,
			true,
			GenerateOptions.DEFAULT_GENERATE_OPTIONS
		);

		// when
		double actual = sut.generate(context);

		then(actual).isEqualTo(0.0d);
	}

	@Test
	void generateNullableContainer() {
		// given
		DefaultNullInjectGenerator sut = new DefaultNullInjectGenerator(
			0.2,
			true,
			true,
			false,
			Collections.emptySet(),
			Collections.emptySet()
		);
		TypeReference<SampleWithAnnotation> typeReference = new TypeReference<SampleWithAnnotation>() {
		};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "container").get();
		ObjectPropertyGeneratorContext context = new ObjectPropertyGeneratorContext(
			property,
			null,
			null,
			true,
			GenerateOptions.DEFAULT_GENERATE_OPTIONS
		);

		// when
		double actual = sut.generate(context);

		then(actual).isEqualTo(0.2d);
	}

	static class SampleWithAnnotation {
		@Nullable
		private String nullable;

		@Nonnull
		private String nonnull;

		private String defaultValue;

		@Nullable
		private int primitive;

		private Optional<String> container;
	}
}
