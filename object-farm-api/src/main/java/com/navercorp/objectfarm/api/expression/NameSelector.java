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
 * A name selector that selects an object member by name.
 * <p>
 * Corresponds to the "name-selector" in JSONPath RFC 9535.
 * <p>
 * Example: In the path "$.items", ".items" is a NameSelector with name "items".
 *
 * @see <a href="https://datatracker.ietf.org/doc/rfc9535/">RFC 9535 - JSONPath</a>
 */
public final class NameSelector implements Selector {
	private final String name;

	public NameSelector(String name) {
		this.name = Objects.requireNonNull(name, "name must not be null");
	}

	public String getName() {
		return name;
	}

	@Override
	public String toExpression() {
		return name;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		NameSelector that = (NameSelector)obj;
		return Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public String toString() {
		return "NameSelector{name='" + name + "'}";
	}
}
