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

import java.math.BigInteger;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class JavaxValidationStringConstraint {
	@Nullable
	private final BigInteger minSize;

	@Nullable
	private final BigInteger maxSize;

	private final boolean digits;

	private final boolean notBlank;

	public JavaxValidationStringConstraint(
		@Nullable BigInteger minSize,
		@Nullable BigInteger maxSize,
		boolean digits,
		boolean notBlank
	) {
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.digits = digits;
		this.notBlank = notBlank;
	}

	@Nullable
	public BigInteger getMinSize() {
		return this.minSize;
	}

	@Nullable
	public BigInteger getMaxSize() {
		return this.maxSize;
	}

	public boolean isDigits() {
		return this.digits;
	}

	public boolean isNotBlank() {
		return this.notBlank;
	}
}
