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

package com.navercorp.fixturemonkey.api.constraint;

import java.math.BigDecimal;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

@API(since = "0.6.8", status = Status.MAINTAINED)
public final class JavaDecimalConstraint {
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

	public JavaDecimalConstraint(
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
		return min;
	}

	@Nullable
	public Boolean getMinInclusive() {
		return minInclusive;
	}

	@Nullable
	public BigDecimal getMax() {
		return max;
	}

	@Nullable
	public Boolean getMaxInclusive() {
		return maxInclusive;
	}

	@Nullable
	public Integer getScale() {
		return scale;
	}
}
