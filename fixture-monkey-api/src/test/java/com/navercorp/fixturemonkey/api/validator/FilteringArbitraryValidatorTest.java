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

package com.navercorp.fixturemonkey.api.validator;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.api.exception.ValidationFailedException;

class FilteringArbitraryValidatorTest {
	@Test
	void validateSafelyStoresAndClearsFailure() {
		AtomicBoolean shouldFail = new AtomicBoolean(true);
		FilteringArbitraryValidator validator = new FilteringArbitraryValidator(arbitrary -> {
			if (shouldFail.getAndSet(false)) {
				throw new ValidationFailedException("failed", Collections.singleton("candidate"));
			}
		});

		boolean firstAttempt = validator.validateSafely(new Object());
		then(firstAttempt).isFalse();
		then(validator.consumeFailure())
			.hasValueSatisfying(ex -> then(ex.getConstraintViolationPropertyNames()).containsExactly("candidate"));

		boolean secondAttempt = validator.validateSafely(new Object());
		then(secondAttempt).isTrue();
		then(validator.consumeFailure()).isEmpty();
	}
}
