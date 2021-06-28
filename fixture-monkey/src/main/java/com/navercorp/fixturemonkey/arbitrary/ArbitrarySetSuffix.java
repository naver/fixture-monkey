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

public final class ArbitrarySetSuffix extends AbstractArbitrarySet<String> {
	private final Arbitrary<String> value;

	public ArbitrarySetSuffix(ArbitraryExpression fixtureExpression, Arbitrary<String> value) {
		super(fixtureExpression);
		this.value = value;
	}

	@Override
	public Arbitrary<String> getValue() {
		return value;
	}

	@Override
	public Arbitrary<String> apply(Arbitrary<String> from) {
		return Combinators.combine(value, from)
			.as((suffix, fromValue) -> {
				String concatString = fromValue + suffix;
				int remainLength = concatString.length() - suffix.length();
				return concatString.substring(Math.min(remainLength, suffix.length()));
			});
	}

	@Override
	public ArbitrarySetSuffix copy() {
		return new ArbitrarySetSuffix(this.getArbitraryExpression(), this.value);
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
		ArbitrarySetSuffix that = (ArbitrarySetSuffix)obj;
		return value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), value);
	}
}
