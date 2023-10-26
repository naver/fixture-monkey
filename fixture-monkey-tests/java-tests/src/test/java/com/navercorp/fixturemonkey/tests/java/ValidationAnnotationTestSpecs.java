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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.constraints.NotNull;

import lombok.Value;

@SuppressWarnings("unused")
class ValidationAnnotationTestSpecs {

	@Value
	@NullOrLessThan5
	public static class CustomAnnotationStringObject {
		String nullOrLessThan5String;
	}

	@Value
	public static class StringNotNullAnnotationObject {
		@NotNull
		String value;
	}

	@Constraint(validatedBy = NullOrLess5Validator.class)
	@Target(TYPE)
	@Retention(RUNTIME)
	public @interface NullOrLessThan5 {
		String message() default "It is not null or greater than or equal to 5";

		Class<?>[] groups() default {};

		Class<? extends Payload>[] payload() default {};
	}

	public static class NullOrLess5Validator implements
		ConstraintValidator<NullOrLessThan5, CustomAnnotationStringObject> {

		@Override
		public boolean isValid(CustomAnnotationStringObject value, ConstraintValidatorContext context) {
			if (value.getNullOrLessThan5String() == null) {
				return true;
			}

			return value.getNullOrLessThan5String().length() < 5;
		}
	}
}
