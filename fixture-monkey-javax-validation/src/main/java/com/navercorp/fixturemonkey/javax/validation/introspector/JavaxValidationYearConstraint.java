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

import java.time.Year;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class JavaxValidationYearConstraint {
	@Nullable
	private final Year min;

	@Nullable
	private final Year max;

	public JavaxValidationYearConstraint(@Nullable Year min, @Nullable Year max) {
		this.min = min;
		this.max = max;
	}

	@Nullable
	public Year getMin() {
		return this.min;
	}

	@Nullable
	public Year getMax() {
		return this.max;
	}
}
