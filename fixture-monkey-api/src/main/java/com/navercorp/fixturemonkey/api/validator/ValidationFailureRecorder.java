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
import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.exception.ValidationFailedException;

@API(since = "1.1.16", status = Status.EXPERIMENTAL)
public final class ValidationFailureRecorder {
	@Nullable
	private ValidationFailedException lastFailure;

	public Optional<ValidationFailedException> consume() {
		ValidationFailedException failure = lastFailure;
		lastFailure = null;
		return Optional.ofNullable(failure);
	}

	public void record(ValidationFailedException failure) {
		lastFailure = failure;
	}

	public void record(String message, Set<String> constraintViolationPropertyNames) {
		lastFailure = new ValidationFailedException(message, constraintViolationPropertyNames);
	}

	public void clear() {
		lastFailure = null;
	}
}
