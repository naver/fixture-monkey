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
import javax.validation.constraints.Pattern;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.arbitraries.StringArbitrary;
import net.jqwik.web.api.Web;

public class StringAnnotatedArbitraryGenerator implements AnnotatedArbitraryGenerator<String> {
	public static final StringAnnotatedArbitraryGenerator INSTANCE = new StringAnnotatedArbitraryGenerator();
	private static final java.util.regex.Pattern EMPTY_PATTERN = java.util.regex.Pattern.compile("");
	private static final java.util.regex.Pattern SPACE_PATTERN = java.util.regex.Pattern.compile(" ");
	private static final java.util.regex.Pattern BLANK_PATTERN = java.util.regex.Pattern.compile("[\n\t ]");
	private static final java.util.regex.Pattern CONTROL_BLOCK_PATTERN = java.util.regex.Pattern.compile(
		"[\u0000-\u001f\u007f]");
	private static final RegexGenerator REGEX_GENERATOR = new RegexGenerator();

	@Override
	public Arbitrary<String> generate(AnnotationSource annotationSource) {
		AnnotatedGeneratorConstraint constraint =
			AnnotatedGeneratorConstraints.findConstraintByClass(String.class, annotationSource);
		return generate(constraint, annotationSource);
	}

	private Arbitrary<String> generate(AnnotatedGeneratorConstraint constraint, AnnotationSource annotationSource) {
		BigDecimal min = constraint.getMin();
		BigDecimal max = constraint.getMax();
		boolean digits = false;
		boolean notBlank = false;

		Optional<Digits> digitsAnnotations = annotationSource.findAnnotation(Digits.class);
		if (digitsAnnotations.isPresent()) {
			digits = true;
			max = BigDecimal.valueOf(digitsAnnotations.get().integer());
		}

		Optional<NotBlank> notBlankAnnotations = annotationSource.findAnnotation(NotBlank.class);
		if (notBlankAnnotations.isPresent()) {
			notBlank = true;
			min = BigDecimal.ONE;
		}

		Optional<Pattern> pattern = annotationSource.findAnnotation(Pattern.class);
		if (pattern.isPresent()) {
			Integer minValue = min != null ? min.intValue() : null;
			Integer maxValue = max != null ? max.intValue() : null;
			List<String> values = REGEX_GENERATOR.generateAll(pattern.get(), minValue, maxValue);
			if (notBlank) {
				values = values.stream()
					.filter(this::isNotBlank)
					.collect(toList());
			}
			return Arbitraries.of(values);
		}

		Arbitrary<String> arbitrary;
		if (annotationSource.findAnnotation(Email.class).isPresent()) {
			arbitrary = Web.emails();
			if (min != null) {
				int emailMinLength = min.intValue();
				arbitrary = arbitrary.filter(it -> it != null && it.length() >= emailMinLength);
			}
			if (max != null) {
				int emailMaxLength = max.intValue();
				arbitrary = arbitrary.filter(it -> it != null && it.length() <= emailMaxLength);
			}
		} else {
			StringArbitrary stringArbitrary = Arbitraries.strings();
			if (min != null) {
				stringArbitrary = stringArbitrary.ofMinLength(min.intValue());
			}
			if (max != null) {
				stringArbitrary = stringArbitrary.ofMaxLength(max.intValue());
			}
			if (digits) {
				stringArbitrary = stringArbitrary.numeric();
			} else {
				stringArbitrary = stringArbitrary.ascii();
			}
			arbitrary = stringArbitrary;
		}

		boolean shouldReplaceBlankCharacter = notBlank;
		return arbitrary.map(v -> {
			String value = v;
			if (value != null && shouldReplaceBlankCharacter && !isNotBlank(value)) {
				value = EMPTY_PATTERN.matcher(value).replaceAll("a");
				value = SPACE_PATTERN.matcher(value).replaceAll("b");
				value = BLANK_PATTERN.matcher(value).replaceAll("c");
				value = CONTROL_BLOCK_PATTERN.matcher(value).replaceAll("d");
			}
			return value;
		});
	}

	private boolean isNotBlank(String value) {
		return value != null && value.trim().length() > 0;
	}
}
