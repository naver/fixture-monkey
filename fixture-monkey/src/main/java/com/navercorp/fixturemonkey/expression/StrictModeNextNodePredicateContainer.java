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

package com.navercorp.fixturemonkey.expression;

import java.util.AbstractList;
import java.util.List;

import com.navercorp.fixturemonkey.tree.NextNodePredicate;

public class StrictModeNextNodePredicateContainer extends AbstractList<NextNodePredicate> {
	private final List<NextNodePredicate> delegate;

	public StrictModeNextNodePredicateContainer(List<NextNodePredicate> delegate, Class<?> rootClass) {
		this.delegate = delegate;
		if (!ExpressionPathValidator.isValidFieldPath(rootClass, delegate)) {
			throw new IllegalArgumentException("No matching results for given container expression.");
		}
	}

	@Override
	public NextNodePredicate get(int index) {
		return delegate.get(index);
	}

	@Override
	public int size() {
		return delegate.size();
	}
}
