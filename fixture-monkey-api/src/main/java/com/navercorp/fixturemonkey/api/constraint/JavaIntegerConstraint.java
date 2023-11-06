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

import java.math.BigInteger;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.6.8", status = Status.MAINTAINED)
public final class JavaIntegerConstraint {
	@Nullable
	private final BigInteger positiveMin;

	@Nullable
	private final BigInteger positiveMax;

	@Nullable
	private final BigInteger negativeMin;

	@Nullable
	private final BigInteger negativeMax;

	public JavaIntegerConstraint(
		@Nullable BigInteger positiveMin,
		@Nullable BigInteger positiveMax,
		@Nullable BigInteger negativeMin,
		@Nullable BigInteger negativeMax
	) {
		this.positiveMin = positiveMin;
		this.positiveMax = positiveMax;
		this.negativeMin = negativeMin;
		this.negativeMax = negativeMax;
	}

	@Nullable
	public BigInteger getPositiveMin() {
		return positiveMin;
	}

	@Nullable
	public BigInteger getPositiveMax() {
		return positiveMax;
	}

	@Nullable
	public BigInteger getNegativeMin() {
		return negativeMin;
	}

	@Nullable
	public BigInteger getNegativeMax() {
		return negativeMax;
	}
}
