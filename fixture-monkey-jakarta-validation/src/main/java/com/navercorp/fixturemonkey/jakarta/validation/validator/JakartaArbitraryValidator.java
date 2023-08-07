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

package com.navercorp.fixturemonkey.jakarta.validation.validator;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Size;
import jakarta.validation.metadata.ConstraintDescriptor;

import com.navercorp.fixturemonkey.api.exception.IgnoredSizeValidationException;
import com.navercorp.fixturemonkey.api.exception.ValidationFailedException;
import com.navercorp.fixturemonkey.api.validator.ArbitraryValidator;

@API(since = "0.5.6", status = Status.MAINTAINED)
public final class JakartaArbitraryValidator implements ArbitraryValidator {
	int maxSizeFailure = 500;
	private Validator validator;
	private Map<Annotation, Integer> sizeFailureCount = new HashMap<>();

	private Set<Annotation> ignoredAnnotations = new HashSet<>();

	public JakartaArbitraryValidator() {
		try {
			this.validator = Validation.buildDefaultValidatorFactory().getValidator();
		} catch (Exception e) {
			this.validator = null;
		}
	}

	@Override
	public void validate(Object arbitrary) {
		if (this.validator != null) {
			Set<ConstraintViolation<Object>> violations = this.validator.validate(arbitrary);

			Set<String> constraintViolationPropertyNames = violations.stream()
				.map(ConstraintViolation::getPropertyPath)
				.map(Path::toString)
				.collect(Collectors.toSet());

			Set<Annotation> constraintViolationAnnotations = violations.stream()
				.map(ConstraintViolation::getConstraintDescriptor)
				.map(ConstraintDescriptor::getAnnotation)
				.filter(annotation -> !ignoredAnnotations.contains(annotation))
				.filter(annotation -> annotation.annotationType().equals(Size.class))
				.collect(Collectors.toSet());

			constraintViolationAnnotations.forEach(annotation -> {
				if (!sizeFailureCount.containsKey(annotation)) {
					sizeFailureCount.put(annotation, 1);
				} else {
					sizeFailureCount.replace(annotation, sizeFailureCount.get(annotation) + 1);
				}
			});

			sizeFailureCount.forEach((annotation, integer) -> {
				if (!constraintViolationAnnotations.contains(annotation)) {
					sizeFailureCount.replace(annotation, 0);
				}
				if (integer > maxSizeFailure) {
					ignoredAnnotations.add(annotation);
				}
			});

			if (!violations.isEmpty()) {
				if (!violations.stream().allMatch(objectConstraintViolation ->
					ignoredAnnotations.contains(objectConstraintViolation.getConstraintDescriptor().getAnnotation())
				)) {
					throw new ValidationFailedException(
						"DefaultArbitrayValidator ConstraintViolations. type: " + arbitrary.getClass(),
						constraintViolationPropertyNames
					);
				} else {
					throw new IgnoredSizeValidationException(
						"DefaultArbitrayValidator ConstraintViolations. type: " + arbitrary.getClass(),
						constraintViolationPropertyNames
					);
				}
			}
		}
	}
}
