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

import java.util.Set;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import com.navercorp.fixturemonkey.api.exception.ValidationFailedException;
import com.navercorp.fixturemonkey.api.validator.ArbitraryValidator;

@API(since = "0.5.6", status = Status.MAINTAINED)
public final class JakartaArbitraryValidator implements ArbitraryValidator {
	private @Nullable Validator validator;

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
				.<@NonNull Path>map(ConstraintViolation::getPropertyPath)
				.map(Path::toString)
				.collect(Collectors.toSet());

			if (!violations.isEmpty()) {
				throw new ValidationFailedException(
					"DefaultArbitraryValidator ConstraintViolations. type: " + arbitrary.getClass(),
					constraintViolationPropertyNames
				);
			}
		}
	}
}
