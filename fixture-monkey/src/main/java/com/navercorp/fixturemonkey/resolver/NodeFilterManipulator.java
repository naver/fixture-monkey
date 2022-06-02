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

package com.navercorp.fixturemonkey.resolver;

import java.util.function.Predicate;

import com.navercorp.fixturemonkey.api.type.Types;

@SuppressWarnings("rawtypes")
public final class NodeFilterManipulator implements NodeManipulator {
	private final Class<?> type;
	private final Predicate filter;

	public NodeFilterManipulator(Class<?> type, Predicate filter) {
		this.type = type;
		this.filter = filter;
	}

	@Override
	public void manipulate(ArbitraryNode arbitraryNode) {
		Class<?> actualType = Types.getActualType(arbitraryNode.getProperty().getType());
		if (!actualType.isAssignableFrom(type)) {
			throw new IllegalArgumentException(
				"Wrong type filter is applied. Expected: " + type + ", Actual: " + actualType
			);
		}
		arbitraryNode.addArbitraryFilter(filter);
	}
}
