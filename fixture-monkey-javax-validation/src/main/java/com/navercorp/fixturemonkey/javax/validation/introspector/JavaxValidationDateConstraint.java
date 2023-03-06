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

package com.navercorp.fixturemonkey.javax.validation.introspector;

import java.time.LocalDate;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class JavaxValidationDateConstraint {
	@Nullable
	private final LocalDate min;

	@Nullable
	private final LocalDate max;

	public JavaxValidationDateConstraint(@Nullable LocalDate min, @Nullable LocalDate max) {
		this.min = min;
		this.max = max;
	}

	@Nullable
	public LocalDate getMin() {
		return this.min;
	}

	@Nullable
	public LocalDate getMax() {
		return this.max;
	}
}
