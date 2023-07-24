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

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.lazy.LazyArbitrary;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class ArbitraryIntrospectorResult {
	@Deprecated
	public static final ArbitraryIntrospectorResult EMPTY = new ArbitraryIntrospectorResult(
		CombinableArbitrary.from(new Object())
	);

	public static final ArbitraryIntrospectorResult NOT_INTROSPECTED = EMPTY;

	private static final Object LOCK = new Object();

	private final CombinableArbitrary<?> value;

	public ArbitraryIntrospectorResult(@Nullable Arbitrary<?> value) {
		this.value = CombinableArbitrary.from(LazyArbitrary.lazy(
			() -> {
				if (value != null) {
					synchronized (LOCK) {
						return value.sample();
					}
				}
				return null;
			}
		));
	}

	public ArbitraryIntrospectorResult(CombinableArbitrary<?> value) {
		this.value = value;
	}

	public CombinableArbitrary<?> getValue() {
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
