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

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.6.8", status = Status.EXPERIMENTAL)
public final class JavaDecimalConstraint {
	@Nullable
	private final BigDecimal positiveMin;

	@Nullable
	private final Boolean positiveMinInclusive;

	@Nullable
	private final BigDecimal positiveMax;

	@Nullable
	private final Boolean positiveMaxInclusive;

	@Nullable
	private final BigDecimal negativeMin;

	@Nullable
	private final Boolean negativeMinInclusive;

	@Nullable
	private final BigDecimal negativeMax;

	@Nullable
	private final Boolean negativeMaxInclusive;

	@Nullable
	private final Integer scale;

	public JavaDecimalConstraint(
		@Nullable BigDecimal positiveMin,
		@Nullable Boolean positiveMinInclusive,
		@Nullable BigDecimal positiveMax,
		@Nullable Boolean positiveMaxInclusive,
		@Nullable BigDecimal negativeMin,
		@Nullable Boolean negativeMinInclusive,
		@Nullable BigDecimal negativeMax,
		@Nullable Boolean negativeMaxInclusive,
		@Nullable Integer scale
	) {
		this.positiveMin = positiveMin;
		this.positiveMinInclusive = positiveMinInclusive;
		this.positiveMax = positiveMax;
		this.positiveMaxInclusive = positiveMaxInclusive;
		this.negativeMin = negativeMin;
		this.negativeMinInclusive = negativeMinInclusive;
		this.negativeMax = negativeMax;
		this.negativeMaxInclusive = negativeMaxInclusive;
		this.scale = scale;
	}

	@Nullable
	public BigDecimal getPositiveMin() {
		return positiveMin;
	}

	@Nullable
	public Boolean getPositiveMinInclusive() {
		return positiveMinInclusive;
	}

	@Nullable
	public BigDecimal getPositiveMax() {
		return positiveMax;
	}

	@Nullable
	public Boolean getPositiveMaxInclusive() {
		return positiveMaxInclusive;
	}

	@Nullable
	public BigDecimal getNegativeMin() {
		return negativeMin;
	}

	@Nullable
	public Boolean getNegativeMinInclusive() {
		return negativeMinInclusive;
	}

	@Nullable
	public BigDecimal getNegativeMax() {
		return negativeMax;
	}

	@Nullable
	public Boolean getNegativeMaxInclusive() {
		return negativeMaxInclusive;
	}

	@Nullable
	public Integer getScale() {
		return scale;
	}
}
