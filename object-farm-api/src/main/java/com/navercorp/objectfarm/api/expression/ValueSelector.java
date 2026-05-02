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

package com.navercorp.objectfarm.api.expression;

import org.jspecify.annotations.Nullable;

/**
 * A selector for Map entry values.
 * <p>
 * This is an extension to JSONPath RFC 9535 for Map support.
 * It selects the value node of a Map entry.
 * <p>
 * Example: In the path "$.map[0][value]", "[value]" is a ValueSelector.
 *
 * @see KeySelector
 */
public final class ValueSelector implements Selector {

	public ValueSelector() {
	}

	@Override
	public String toExpression() {
		return "value";
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		return obj != null && getClass() == obj.getClass();
	}

	@Override
	public int hashCode() {
		return ValueSelector.class.hashCode();
	}

	@Override
	public String toString() {
		return "ValueSelector{}";
	}
}
