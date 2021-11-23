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

package com.navercorp.fixturemonkey.generator;

import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.arbitraries.EmailArbitrary;
import net.jqwik.api.arbitraries.StringArbitrary;

public class NewStringAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<String> {
	public static final NewStringAnnotatedArbitraryGenerator INSTANCE = new NewStringAnnotatedArbitraryGenerator();

	private static final StringAnnotationIntrospector stringAnnotationIntrospector = new StringAnnotationIntrospector();
	private static final java.util.regex.Pattern EMPTY_PATTERN = java.util.regex.Pattern.compile("");
	private static final java.util.regex.Pattern SPACE_PATTERN = java.util.regex.Pattern.compile(" ");
	private static final java.util.regex.Pattern BLANK_PATTERN = java.util.regex.Pattern.compile("[\n\t ]");
	private static final java.util.regex.Pattern CONTROL_BLOCK_PATTERN =
		java.util.regex.Pattern.compile("[\u0000-\u001f\u007f]");
	private static final RegexGenerator REGEX_GENERATOR = new RegexGenerator();

	@Override
	public Arbitrary<String> generate(
		AnnotationSource<String> annotationSource
	) {
		Arbitrary<String> defaultArbitrary = generateDefaultArbitrary(annotationSource);
		return stringAnnotationIntrospector.getArbitrary(defaultArbitrary, annotationSource);
	}

	private Arbitrary<String> generateDefaultArbitrary(
		AnnotationSource<String> annotationSource
	) {
		Arbitrary<String> arbitrary = annotationSource.getArbitrary();
		if (annotationSource.findAnnotation(Email.class).isPresent()) {
			return Arbitraries.emails();
		} else if (arbitrary instanceof StringArbitrary) {
			StringArbitrary stringArbitrary = (StringArbitrary)arbitrary;
			if (annotationSource.findAnnotation(Digits.class).isPresent()) {
				stringArbitrary = stringArbitrary.numeric();
			} else {
				stringArbitrary = stringArbitrary.ascii();
			}
			arbitrary = stringArbitrary;
		}

		return arbitrary;
	}

	private static class StringAnnotationIntrospector implements AnnotationIntrospector<String> {
		@Override
		public Arbitrary<String> getArbitrary(
			Arbitrary<String> arbitrary,
			AnnotationSource<String> annotationSource
		) {
			BigDecimal min = null;
			BigDecimal max = null;
			if (annotationSource.findAnnotation(NotBlank.class).isPresent()
				|| annotationSource.findAnnotation(NotEmpty.class).isPresent()) {
				min = BigDecimal.ONE;
			}

			Optional<Digits> digitsAnnotations = annotationSource.findAnnotation(Digits.class);
			if (digitsAnnotations.isPresent()) {
				int digitInteger = digitsAnnotations.get().integer();
				min = BigDecimal.valueOf(digitInteger);
				max = BigDecimal.valueOf(digitInteger);
			}

			Optional<Size> size = annotationSource.findAnnotation(Size.class);
			if (size.isPresent()) {
				BigDecimal minValue = BigDecimal.valueOf(size.map(Size::min).get());
				if (min == null) {
					min = minValue;
				} else if (min.compareTo(minValue) < 0) {
					min = minValue;
				}

				max = BigDecimal.valueOf(size.map(Size::max).get());
			}

			Optional<Pattern> pattern = annotationSource.findAnnotation(Pattern.class);
			if (pattern.isPresent()) {
				Integer minIntValue = min != null ? min.intValue() : null;
				Integer maxIntValue = max != null ? max.intValue() : null;
				List<String> values = REGEX_GENERATOR.generateAll(
					pattern.get(),
					minIntValue,
					maxIntValue
				);

				boolean notBlank = min != null && min.compareTo(BigDecimal.ONE) >= 0;
				if (notBlank) {
					values = values.stream()
						.filter(this::isNotBlank)
						.collect(toList());
				}
				return Arbitraries.of(values);
			}

			BigDecimal minFinal = min;

			if (arbitrary instanceof EmailArbitrary) {
				if (min != null) {
					int emailMinLength = min.intValue();
					arbitrary = arbitrary.filter(it -> it != null && it.length() >= emailMinLength);
				}
				if (max != null) {
					int emailMaxLength = max.intValue();
					arbitrary = arbitrary.filter(it -> it != null && it.length() <= emailMaxLength);
				}
			} else if (arbitrary instanceof StringArbitrary) {
				StringArbitrary stringArbitrary = (StringArbitrary)arbitrary;

				if (min != null) {
					stringArbitrary = stringArbitrary.ofMinLength(min.intValue());
				}
				if (max != null) {
					stringArbitrary = stringArbitrary.ofMaxLength(max.intValue());
				}
				arbitrary = stringArbitrary;
			}

			return arbitrary.map(value -> {
				boolean notBlank = minFinal != null && minFinal.compareTo(BigDecimal.ONE) >= 0;
				int originalLength = value.length();
				if (notBlank && !isNotBlank(value)) {
					value = EMPTY_PATTERN.matcher(value).replaceAll("a");
					value = SPACE_PATTERN.matcher(value).replaceAll("b");
					value = BLANK_PATTERN.matcher(value).replaceAll("c");
					value = CONTROL_BLOCK_PATTERN.matcher(value).replaceAll("d");
				}
				return value.substring(0, originalLength);
			});

		}

		private boolean isNotBlank(String value) {
			return value != null && value.trim().length() > 0;
		}
	}
}

