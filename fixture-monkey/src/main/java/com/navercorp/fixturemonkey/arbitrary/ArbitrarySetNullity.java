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

import com.navercorp.fixturemonkey.ArbitraryBuilder;

public final class ArbitrarySetNullity<T> implements PreArbitraryManipulator<T> {
	private ArbitraryExpression fixtureExpression;
	private final boolean toNull;

	public ArbitrarySetNullity(ArbitraryExpression fixtureExpression, boolean toNull) {
		this.fixtureExpression = fixtureExpression;
		this.toNull = toNull;
	}

	public Boolean toNull() {
		return toNull;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Arbitrary<T> apply(Arbitrary<?> from) {
		return (Arbitrary<T>)from;
	}

	@Override
	public ArbitraryExpression getArbitraryExpression() {
		return fixtureExpression;
	}

	@Override
	public void addPrefix(String expression) {
		fixtureExpression = fixtureExpression.appendLeft(expression);
	}

	@Override
	public void accept(ArbitraryBuilder<T> fixtureBuilder) {
		fixtureBuilder.setNullity(this);
	}

	@Override
	public ArbitrarySetNullity<T> copy() {
		return new ArbitrarySetNullity<>(this.fixtureExpression, this.toNull);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ArbitrarySetNullity<?> that = (ArbitrarySetNullity<?>)obj;
		return toNull == that.toNull && fixtureExpression.equals(that.fixtureExpression);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fixtureExpression, toNull);
	}
}
