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
 * A wildcard selector that matches any child element.
 * <p>
 * Corresponds to the "wildcard-selector" in JSONPath RFC 9535.
 * <p>
 * In pattern matching, this selector matches:
 * <ul>
 *   <li>{@link IndexSelector} - any array/list index</li>
 *   <li>{@link KeySelector} - any Map entry key</li>
 *   <li>{@link ValueSelector} - any Map entry value</li>
 * </ul>
 * <p>
 * Example: In the pattern "$.items[*]", "[*]" is a WildcardSelector.
 *
 * @see <a href="https://datatracker.ietf.org/doc/rfc9535/">RFC 9535 - JSONPath</a>
 */
public final class WildcardSelector implements Selector {

	public WildcardSelector() {
	}

	@Override
	public String toExpression() {
		return "*";
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
		return WildcardSelector.class.hashCode();
	}

	@Override
	public String toString() {
		return "WildcardSelector{}";
	}
}
