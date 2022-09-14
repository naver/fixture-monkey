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

package com.navercorp.fixturemonkey.javax.validation.generator;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Collections;
import java.util.Optional;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import com.navercorp.fixturemonkey.api.generator.DefaultNullInjectGenerator;
import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.type.TypeReference;

@SuppressWarnings("OptionalGetWithoutIsPresent")
class JavaxValidationNullInjectGeneratorTest {
	@net.jqwik.api.Property
	void generateNull() {
		// given
		JavaxValidationNullInjectGenerator sut = new JavaxValidationNullInjectGenerator();
		TypeReference<SampleWithAnnotation> typeReference = new TypeReference<SampleWithAnnotation>() {
		};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "nullValue").get();
		ObjectPropertyGeneratorContext context = new ObjectPropertyGeneratorContext(
			property,
			null,
			null,
			false,
			GenerateOptions.DEFAULT_GENERATE_OPTIONS
		);

		// when
		double actual = sut.generate(context);

		then(actual).isEqualTo(1.0d);
	}

	@net.jqwik.api.Property
	void generateNotNull() {
		// given
		JavaxValidationNullInjectGenerator sut = new JavaxValidationNullInjectGenerator(
			new DefaultNullInjectGenerator(
				0.2d,
				false,
				false,
				false,
				Collections.emptySet(),
				Collections.emptySet()
			)
		);
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

		then(actual).isEqualTo(0.0d);
	}

	@net.jqwik.api.Property
	void generateNotBlank() {
		// given
		JavaxValidationNullInjectGenerator sut = new JavaxValidationNullInjectGenerator();
		TypeReference<SampleWithAnnotation> typeReference = new TypeReference<SampleWithAnnotation>() {
		};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "notBlank").get();
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

	@net.jqwik.api.Property
	void generateNotEmpty() {
		// given
		JavaxValidationNullInjectGenerator sut = new JavaxValidationNullInjectGenerator();
		TypeReference<SampleWithAnnotation> typeReference = new TypeReference<SampleWithAnnotation>() {
		};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "notEmpty").get();
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

	@net.jqwik.api.Property
	void generateDefaultValue() {
		// given
		JavaxValidationNullInjectGenerator sut = new JavaxValidationNullInjectGenerator(
			new DefaultNullInjectGenerator(
				0.2d,
				false,
				false,
				false,
				Collections.emptySet(),
				Collections.emptySet()
			)
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

	@net.jqwik.api.Property
	void generateDefaultValueDefaultNotNull() {
		// given
		JavaxValidationNullInjectGenerator sut = new JavaxValidationNullInjectGenerator(
			new DefaultNullInjectGenerator(
				0.2d,
				false,
				true,
				false,
				Collections.emptySet(),
				Collections.emptySet()
			)
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

	@net.jqwik.api.Property
	void generateNotEmptyContainer() {
		// given
		JavaxValidationNullInjectGenerator sut = new JavaxValidationNullInjectGenerator(
			new DefaultNullInjectGenerator(
				0.2d,
				true,
				false,
				false,
				Collections.emptySet(),
				Collections.emptySet()
			)
		);
		TypeReference<SampleWithAnnotation> typeReference = new TypeReference<SampleWithAnnotation>() {
		};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "notEmptyContainer").get();
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

	static class SampleWithAnnotation {
		@Null
		private String nullValue;

		@NotNull
		private String notNull;

		@NotBlank
		private String notBlank;

		@NotEmpty
		private String notEmpty;

		private String defaultValue;

		@NotEmpty
		private Optional<String> notEmptyContainer;
	}
}
