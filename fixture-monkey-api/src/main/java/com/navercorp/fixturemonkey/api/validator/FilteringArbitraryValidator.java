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

import java.util.Optional;
import java.util.Set;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.exception.ValidationFailedException;

@API(since = "1.1.16", status = Status.EXPERIMENTAL)
public final class FilteringArbitraryValidator implements ArbitraryValidator {
	private final ArbitraryValidator delegate;
	private ValidationFailedException lastFailure;

	public FilteringArbitraryValidator(ArbitraryValidator delegate) {
		this.delegate = delegate;
	}

	@Override
	public void validate(Object arbitrary) {
		delegate.validate(arbitrary);
	}

	public boolean validateSafely(Object arbitrary) {
		try {
			delegate.validate(arbitrary);
			lastFailure = null;
			return true;
		} catch (ValidationFailedException ex) {
			lastFailure = ex;
			return false;
		}
	}

	public Optional<ValidationFailedException> consumeFailure() {
		ValidationFailedException failure = lastFailure;
		lastFailure = null;
		return Optional.ofNullable(failure);
	}

	public void markFailure(ValidationFailedException failure) {
		lastFailure = failure;
	}

	public void markFailure(String message, Set<String> constraintViolationPropertyNames) {
		this.lastFailure = new ValidationFailedException(message, constraintViolationPropertyNames);
	}
}
