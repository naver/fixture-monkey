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

package com.navercorp.fixturemonkey.api.introspector;

import java.util.Objects;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class ArbitraryIntrospectorResult {
	public static final ArbitraryIntrospectorResult EMPTY = new ArbitraryIntrospectorResult(null);

	@Nullable
	private final Arbitrary<?> value;

	public ArbitraryIntrospectorResult(@Nullable Arbitrary<?> value) {
		this.value = value;
	}

	@Nullable
	public Arbitrary<?> getValue() {
		return this.value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ArbitraryIntrospectorResult that = (ArbitraryIntrospectorResult)obj;
		return Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public String toString() {
		return "ArbitraryIntrospectorResult{"
			+ "value=" + value + '}';
	}
}
