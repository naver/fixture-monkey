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

import java.time.LocalDateTime;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.jspecify.annotations.Nullable;

@API(since = "0.6.8", status = Status.MAINTAINED)
public final class JavaDateTimeConstraint {
	@Nullable
	private final Supplier<LocalDateTime> min;
	@Nullable
	private final Supplier<LocalDateTime> max;

	public JavaDateTimeConstraint(@Nullable Supplier<LocalDateTime> min, @Nullable Supplier<LocalDateTime> max) {
		this.min = min;
		this.max = max;
	}

	@Nullable
	public LocalDateTime getMin() {
		if (this.min == null) {
			return null;
		}
		return this.min.get();
	}

	@Nullable
	public LocalDateTime getMax() {
		if (this.max == null) {
			return null;
		}
		return this.max.get();
	}
}
