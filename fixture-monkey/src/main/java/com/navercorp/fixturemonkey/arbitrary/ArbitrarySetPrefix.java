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

package com.navercorp.fixturemonkey.arbitrary;

import java.util.Objects;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

public final class ArbitrarySetPrefix extends AbstractArbitrarySet<String> {
	private final Arbitrary<String> value;

	public ArbitrarySetPrefix(ArbitraryExpression fixtureExpression, Arbitrary<String> value) {
		super(fixtureExpression);
		this.value = value;
	}

	@Override
	public Arbitrary<String> getValue() {
		return value;
	}

	@Override
	public Arbitrary<String> apply(Arbitrary<?> from) {
		return Combinators.combine(value, from)
			.as(
				(prefix, fromValue) -> {
					String arbitraryString = (String)fromValue;
					String concatString = prefix + arbitraryString;
					int remainLength = concatString.length() - prefix.length();
					return concatString.substring(0, Math.max(prefix.length(), remainLength));
				}
			);
	}

	@Override
	public ArbitrarySetPrefix copy() {
		return new ArbitrarySetPrefix(this.getArbitraryExpression(), this.value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		if (!super.equals(obj)) {
			return false;
		}
		ArbitrarySetPrefix that = (ArbitrarySetPrefix)obj;
		return value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), value);
	}
}
