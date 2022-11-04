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

import java.math.BigDecimal;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class JavaxValidationDecimalConstraint {
	@Nullable
	private final BigDecimal min;

	@Nullable
	private final Boolean minInclusive;

	@Nullable
	private final BigDecimal max;

	@Nullable
	private final Boolean maxInclusive;

	@Nullable
	private final Integer scale;

	public JavaxValidationDecimalConstraint(
		@Nullable BigDecimal min,
		@Nullable Boolean minInclusive,
		@Nullable BigDecimal max,
		@Nullable Boolean maxInclusive,
		@Nullable Integer scale
	) {
		this.min = min;
		this.minInclusive = minInclusive;
		this.max = max;
		this.maxInclusive = maxInclusive;
		this.scale = scale;
	}

	@Nullable
	public BigDecimal getMin() {
		return this.min;
	}

	@Nullable
	public Boolean getMinInclusive() {
		return this.minInclusive;
	}

	@Nullable
	public BigDecimal getMax() {
		return this.max;
	}

	@Nullable
	public Boolean getMaxInclusive() {
		return this.maxInclusive;
	}

	@Nullable
	public Integer getScale() {
		return scale;
	}
}
