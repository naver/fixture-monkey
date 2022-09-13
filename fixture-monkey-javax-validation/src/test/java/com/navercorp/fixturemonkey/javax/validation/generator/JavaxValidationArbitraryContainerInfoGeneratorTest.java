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

import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.option.GenerateOptions;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyCache;
import com.navercorp.fixturemonkey.api.type.TypeReference;

@SuppressWarnings("OptionalGetWithoutIsPresent")
class JavaxValidationArbitraryContainerInfoGeneratorTest {
	@net.jqwik.api.Property
	void generateContainer() {
		// given
		JavaxValidationArbitraryContainerInfoGenerator sut = new JavaxValidationArbitraryContainerInfoGenerator();
		TypeReference<SampleWithContainerAnnotation> typeReference =
			new TypeReference<SampleWithContainerAnnotation>() {
			};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "container").get();
		ContainerPropertyGeneratorContext context = new ContainerPropertyGeneratorContext(
			property,
			null,
			null,
			GenerateOptions.DEFAULT_GENERATE_OPTIONS
		);

		// when
		ArbitraryContainerInfo actual = sut.generate(context);

		then(actual.getElementMinSize()).isEqualTo(0);
		then(actual.getElementMaxSize()).isEqualTo(3);
	}

	@net.jqwik.api.Property
	void generateDefaultSizeContainer() {
		// given
		JavaxValidationArbitraryContainerInfoGenerator sut = new JavaxValidationArbitraryContainerInfoGenerator();
		TypeReference<SampleWithContainerAnnotation> typeReference =
			new TypeReference<SampleWithContainerAnnotation>() {
			};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "defaultSizeContainer")
			.get();
		ContainerPropertyGeneratorContext context = new ContainerPropertyGeneratorContext(
			property,
			null,
			null,
			GenerateOptions.DEFAULT_GENERATE_OPTIONS
		);

		// when
		ArbitraryContainerInfo actual = sut.generate(context);

		then(actual.getElementMinSize()).isEqualTo(0);
		then(actual.getElementMaxSize()).isEqualTo(3);
	}

	@net.jqwik.api.Property
	void generateSizeContainer() {
		// given
		JavaxValidationArbitraryContainerInfoGenerator sut = new JavaxValidationArbitraryContainerInfoGenerator();
		TypeReference<SampleWithContainerAnnotation> typeReference =
			new TypeReference<SampleWithContainerAnnotation>() {
			};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "sizeContainer")
			.get();
		ContainerPropertyGeneratorContext context = new ContainerPropertyGeneratorContext(
			property,
			null,
			null,
			GenerateOptions.DEFAULT_GENERATE_OPTIONS
		);

		// when
		ArbitraryContainerInfo actual = sut.generate(context);

		then(actual.getElementMinSize()).isEqualTo(5);
		then(actual.getElementMaxSize()).isEqualTo(10);
	}

	@net.jqwik.api.Property
	void generateMinSizeContainer() {
		// given
		JavaxValidationArbitraryContainerInfoGenerator sut = new JavaxValidationArbitraryContainerInfoGenerator();
		TypeReference<SampleWithContainerAnnotation> typeReference =
			new TypeReference<SampleWithContainerAnnotation>() {
			};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "minSizeContainer")
			.get();
		ContainerPropertyGeneratorContext context = new ContainerPropertyGeneratorContext(
			property,
			null,
			null,
			GenerateOptions.DEFAULT_GENERATE_OPTIONS
		);

		// when
		ArbitraryContainerInfo actual = sut.generate(context);

		then(actual.getElementMinSize()).isEqualTo(3);
		then(actual.getElementMaxSize()).isEqualTo(6);
	}

	@net.jqwik.api.Property
	void generateMinSizeContainerWithDefaultSize() {
		// given
		JavaxValidationArbitraryContainerInfoGenerator sut = new JavaxValidationArbitraryContainerInfoGenerator();
		TypeReference<SampleWithContainerAnnotation> typeReference =
			new TypeReference<SampleWithContainerAnnotation>() {
			};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "minSizeContainer")
			.get();
		GenerateOptions generateOptions = GenerateOptions.builder()
			.defaultArbitraryContainerMaxSize(10)
			.build();

		ContainerPropertyGeneratorContext context = new ContainerPropertyGeneratorContext(
			property,
			null,
			null,
			generateOptions
		);

		// when
		ArbitraryContainerInfo actual = sut.generate(context);

		then(actual.getElementMinSize()).isEqualTo(3);
		then(actual.getElementMaxSize()).isEqualTo(13);
	}

	@net.jqwik.api.Property
	void generateMaxSizeContainer() {
		// given
		JavaxValidationArbitraryContainerInfoGenerator sut = new JavaxValidationArbitraryContainerInfoGenerator();
		TypeReference<SampleWithContainerAnnotation> typeReference =
			new TypeReference<SampleWithContainerAnnotation>() {
			};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "maxSizeContainer")
			.get();
		ContainerPropertyGeneratorContext context = new ContainerPropertyGeneratorContext(
			property,
			null,
			null,
			GenerateOptions.DEFAULT_GENERATE_OPTIONS
		);

		// when
		ArbitraryContainerInfo actual = sut.generate(context);

		then(actual.getElementMinSize()).isEqualTo(0);
		then(actual.getElementMaxSize()).isEqualTo(5);
	}

	@net.jqwik.api.Property
	void generateNotEmptyContainer() {
		// given
		JavaxValidationArbitraryContainerInfoGenerator sut = new JavaxValidationArbitraryContainerInfoGenerator();
		TypeReference<SampleWithContainerAnnotation> typeReference =
			new TypeReference<SampleWithContainerAnnotation>() {
			};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "notEmptyContainer")
			.get();
		ContainerPropertyGeneratorContext context = new ContainerPropertyGeneratorContext(
			property,
			null,
			null,
			GenerateOptions.DEFAULT_GENERATE_OPTIONS
		);

		// when
		ArbitraryContainerInfo actual = sut.generate(context);

		then(actual.getElementMinSize()).isEqualTo(1);
		then(actual.getElementMaxSize()).isEqualTo(4);
	}

	@net.jqwik.api.Property
	void generateNotEmptyContainerWithDefaultSize() {
		// given
		JavaxValidationArbitraryContainerInfoGenerator sut = new JavaxValidationArbitraryContainerInfoGenerator();
		TypeReference<SampleWithContainerAnnotation> typeReference =
			new TypeReference<SampleWithContainerAnnotation>() {
			};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "notEmptyContainer")
			.get();
		GenerateOptions generateOptions = GenerateOptions.builder()
			.defaultArbitraryContainerMaxSize(10)
			.build();
		ContainerPropertyGeneratorContext context = new ContainerPropertyGeneratorContext(
			property,
			null,
			null,
			generateOptions
		);

		// when
		ArbitraryContainerInfo actual = sut.generate(context);

		then(actual.getElementMinSize()).isEqualTo(1);
		then(actual.getElementMaxSize()).isEqualTo(11);
	}

	@net.jqwik.api.Property
	void generateNotEmptyAndMaxSizeContainer() {
		// given
		JavaxValidationArbitraryContainerInfoGenerator sut = new JavaxValidationArbitraryContainerInfoGenerator();
		TypeReference<SampleWithContainerAnnotation> typeReference =
			new TypeReference<SampleWithContainerAnnotation>() {
			};
		Property property = PropertyCache.getProperty(typeReference.getAnnotatedType(), "notEmptyAndMaxSizeContainer")
			.get();
		ContainerPropertyGeneratorContext context = new ContainerPropertyGeneratorContext(
			property,
			null,
			null,
			GenerateOptions.DEFAULT_GENERATE_OPTIONS
		);

		// when
		ArbitraryContainerInfo actual = sut.generate(context);

		then(actual.getElementMinSize()).isEqualTo(1);
		then(actual.getElementMaxSize()).isEqualTo(5);
	}

	static class SampleWithContainerAnnotation {
		private List<String> container;

		@Size
		private List<String> defaultSizeContainer;

		@Size(min = 5, max = 10)
		private List<String> sizeContainer;

		@Size(min = 3)
		private List<String> minSizeContainer;

		@Size(max = 5)
		private List<String> maxSizeContainer;

		@NotEmpty
		private List<String> notEmptyContainer;

		@Size(max = 5)
		@NotEmpty
		private List<String> notEmptyAndMaxSizeContainer;
	}
}
