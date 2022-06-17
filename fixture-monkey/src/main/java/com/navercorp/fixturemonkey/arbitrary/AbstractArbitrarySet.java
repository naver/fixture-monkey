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

import com.navercorp.fixturemonkey.OldArbitraryBuilderImpl;

public abstract class AbstractArbitrarySet<T> extends AbstractArbitraryExpressionManipulator
	implements PreArbitraryManipulator {

	public AbstractArbitrarySet(ArbitraryExpression arbitraryExpression) {
		super(arbitraryExpression);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void accept(OldArbitraryBuilderImpl arbitraryBuilder) {
		arbitraryBuilder.apply(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		AbstractArbitrarySet<?> that = (AbstractArbitrarySet<?>)obj;
		return getArbitraryExpression().equals(that.getArbitraryExpression());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getArbitraryExpression());
	}
}
