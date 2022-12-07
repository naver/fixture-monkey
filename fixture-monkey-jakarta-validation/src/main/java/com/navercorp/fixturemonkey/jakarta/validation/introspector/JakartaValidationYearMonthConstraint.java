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

package com.navercorp.fixturemonkey.jakarta.validation.introspector;

import java.time.YearMonth;

import javax.annotation.Nullable;

public final class JakartaValidationYearMonthConstraint {
	@Nullable
	private final YearMonth min;

	@Nullable
	private final YearMonth max;

	public JakartaValidationYearMonthConstraint(@Nullable YearMonth min, @Nullable YearMonth max) {
		this.min = min;
		this.max = max;
	}

	@Nullable
	public YearMonth getMin() {
		return this.min;
	}

	@Nullable
	public YearMonth getMax() {
		return this.max;
	}
}
