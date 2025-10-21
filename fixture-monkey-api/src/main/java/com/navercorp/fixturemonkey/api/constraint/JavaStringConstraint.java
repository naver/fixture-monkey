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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

@API(since = "0.6.8", status = Status.MAINTAINED)
public final class JavaStringConstraint {
	@Nullable
	private final BigInteger minSize;

	@Nullable
	private final BigInteger maxSize;

	private final boolean digits;

	private final boolean notNull;

	private final boolean notBlank;

	@Nullable
	private final PatternConstraint pattern;

	private final boolean email;

	public JavaStringConstraint(
		@Nullable BigInteger minSize,
		@Nullable BigInteger maxSize,
		boolean digits,
		boolean notNull,
		boolean notBlank,
		@Nullable PatternConstraint pattern,
		boolean email
	) {
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.digits = digits;
		this.notNull = notNull;
		this.notBlank = notBlank;
		this.pattern = pattern;
		this.email = email;
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

	public boolean isNotNull() {
		return notNull;
	}

	public boolean isNotBlank() {
		return this.notBlank;
	}

	@Nullable
	public PatternConstraint getPattern() {
		return pattern;
	}

	public boolean isEmail() {
		return this.email;
	}

	public static class PatternConstraint {
		private final String regexp;
		private final int[] flags;

		public PatternConstraint(String regexp, int[] flags) {
			this.regexp = regexp;
			this.flags = flags;
		}

		public String getRegexp() {
			return regexp;
		}

		public int[] getFlags() {
			return flags;
		}
	}
}
