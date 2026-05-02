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

import java.util.Objects;

import org.jspecify.annotations.Nullable;

/**
 * An index selector that selects an array element by index.
 * <p>
 * Corresponds to the "index-selector" in JSONPath RFC 9535.
 * <p>
 * Example: In the path "$.items[0]", "[0]" is an IndexSelector with index 0.
 *
 * @see <a href="https://datatracker.ietf.org/doc/rfc9535/">RFC 9535 - JSONPath</a>
 */
public final class IndexSelector implements Selector {
	private final int index;

	public IndexSelector(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public String toExpression() {
		return String.valueOf(index);
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		IndexSelector that = (IndexSelector)obj;
		return index == that.index;
	}

	@Override
	public int hashCode() {
		return Objects.hash(index);
	}

	@Override
	public String toString() {
		return "IndexSelector{index=" + index + "}";
	}
}
