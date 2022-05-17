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

package com.navercorp.fixturemonkey.validator;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;

public final class DefaultArbitraryValidator<T> implements ArbitraryValidator<T> {
	private Validator validator;

	public DefaultArbitraryValidator() {
		try {
			this.validator = Validation.buildDefaultValidatorFactory().getValidator();
		} catch (Exception e) {
			this.validator = null;
		}
	}

	@Override
	public void validate(T arbitrary) {
		if (this.validator != null) {
			Set<ConstraintViolation<T>> violations = this.validator.validate(arbitrary);
			if (!violations.isEmpty()) {
				throw new ConstraintViolationException(
					"DefaultArbitrayValidator ConstraintViolations. type: " + arbitrary.getClass(), violations);
			}
		}
	}
}
