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

package com.navercorp.fixturemonkey.api.arbitrary;

import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.api.exception.RetryableFilterMissException;
import com.navercorp.fixturemonkey.api.exception.ValidationFailedException;
import com.navercorp.fixturemonkey.api.validator.FilteringArbitraryValidator;

class FilteredCombinableArbitraryTest {
	@Test
	void throwsRetryableMissWhenValidationFails() {
		ValidationFailedException failure =
			new ValidationFailedException("validation failed", Collections.singleton("value"));
		FilteringArbitraryValidator filteringValidator = new FilteringArbitraryValidator(arbitrary -> {
			throw failure;
		});

		thenThrownBy(() -> CombinableArbitrary.from("candidate")
			.filter(5, candidate -> filteringValidator.validateSafely(candidate), filteringValidator)
			.combined())
			.isInstanceOf(RetryableFilterMissException.class)
			.hasCause(failure)
			.hasMessageContaining("value");
	}
}
