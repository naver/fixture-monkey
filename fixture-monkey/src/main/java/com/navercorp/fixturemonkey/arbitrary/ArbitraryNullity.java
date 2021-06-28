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

import com.navercorp.fixturemonkey.ArbitraryBuilder;

public final class ArbitraryNullity extends AbstractArbitraryExpressionManipulator
	implements BuilderManipulator {
	private final boolean toNull;

	public ArbitraryNullity(ArbitraryExpression fixtureExpression, boolean toNull) {
		super(fixtureExpression);
		this.toNull = toNull;
	}

	public Boolean toNull() {
		return toNull;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void accept(ArbitraryBuilder fixtureBuilder) {
		fixtureBuilder.setNullity(this);
	}

	@Override
	public ArbitraryNullity copy() {
		return new ArbitraryNullity(this.getArbitraryExpression(), this.toNull);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ArbitraryNullity that = (ArbitraryNullity)obj;
		return toNull == that.toNull && getArbitraryExpression().equals(that.getArbitraryExpression());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getArbitraryExpression(), toNull);
	}
}
